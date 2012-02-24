/*
 * Copyright (C) 2011 eZuce Inc., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the AGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.ivr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.sipfoundry.sipxconfig.address.Address;
import org.sipfoundry.sipxconfig.address.AddressManager;
import org.sipfoundry.sipxconfig.address.AddressProvider;
import org.sipfoundry.sipxconfig.address.AddressType;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigManager;
import org.sipfoundry.sipxconfig.common.UserException;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.dialplan.DialPlanContext;
import org.sipfoundry.sipxconfig.dns.DnsManager;
import org.sipfoundry.sipxconfig.dns.DnsProvider;
import org.sipfoundry.sipxconfig.dns.ResourceRecords;
import org.sipfoundry.sipxconfig.feature.FeatureListener;
import org.sipfoundry.sipxconfig.feature.FeatureManager;
import org.sipfoundry.sipxconfig.feature.FeatureProvider;
import org.sipfoundry.sipxconfig.feature.GlobalFeature;
import org.sipfoundry.sipxconfig.feature.LocationFeature;
import org.sipfoundry.sipxconfig.freeswitch.FreeswitchFeature;
import org.sipfoundry.sipxconfig.setting.BeanWithSettingsDao;
import org.sipfoundry.sipxconfig.snmp.ProcessDefinition;
import org.sipfoundry.sipxconfig.snmp.ProcessProvider;
import org.sipfoundry.sipxconfig.snmp.SnmpManager;

public class IvrImpl implements FeatureProvider, AddressProvider, FeatureListener, Ivr, ProcessProvider, DnsProvider {
    private static final Collection<AddressType> ADDRESSES = Arrays.asList(new AddressType[] {
        REST_API, SIP_ADDRESS
    });
    private BeanWithSettingsDao<IvrSettings> m_settingsDao;
    private BeanWithSettingsDao<CallPilotSettings> m_pilotSettingsDao;
    private ConfigManager m_configManager;
    private FeatureManager m_featureManager;

    public IvrSettings getSettings() {
        return m_settingsDao.findOrCreateOne();
    }

    public CallPilotSettings getCallPilotSettings() {
        return m_pilotSettingsDao.findOrCreateOne();
    }

    public void saveSettings(IvrSettings settings) {
        m_settingsDao.upsert(settings);
    }

    public void saveCallPilotSettings(CallPilotSettings settings) {
        m_pilotSettingsDao.upsert(settings);
    }

    @Override
    public Collection<GlobalFeature> getAvailableGlobalFeatures() {
        return Collections.singleton(CALLPILOT);
    }

    @Override
    public Collection<LocationFeature> getAvailableLocationFeatures(Location l) {
        return Collections.singleton(FEATURE);
    }

    public void setSettingsDao(BeanWithSettingsDao<IvrSettings> settingsDao) {
        m_settingsDao = settingsDao;
    }

    public void setCallPilotSettingsDao(BeanWithSettingsDao<CallPilotSettings> settingsDao) {
        m_pilotSettingsDao = settingsDao;
    }

    @Override
    public Collection<AddressType> getSupportedAddressTypes(AddressManager manager) {
        return ADDRESSES;
    }

    @Override
    public void enableLocationFeature(FeatureManager manager, FeatureEvent event, LocationFeature feature,
            Location location) {
        if (!feature.equals(Ivr.FEATURE)) {
            return;
        }

        switch (event) {
        case PRE_ENABLE:
            if (m_featureManager.isFeatureEnabled(Ivr.FEATURE)) {
                throw new UserException("&error.ivr.enabled");
            }
            IvrSettings settings = getSettings();
            if (settings.isNew()) {
                saveSettings(settings);
            }
            break;
        case POST_DISABLE:
        case POST_ENABLE:
            m_configManager.configureEverywhere(DnsManager.FEATURE, DialPlanContext.FEATURE,
                    FreeswitchFeature.FEATURE);
            break;
        default:
            break;
        }
    }

    @Override
    public void enableGlobalFeature(FeatureManager manager, FeatureEvent event, GlobalFeature feature) {
    }

    @Override
    public Collection<Address> getAvailableAddresses(AddressManager manager, AddressType type, Object requester) {
        if (!ADDRESSES.contains(type)) {
            return null;
        }
        IvrSettings settings = getSettings();
        List<Location> locations = manager.getFeatureManager().getLocationsForEnabledFeature(FEATURE);
        List<Address> addresses = new ArrayList<Address>(locations.size());
        for (Location location : locations) {
            Address address = null;
            if (type.equals(SIP_ADDRESS)) {
                // TODO take port from FS
                address = new Address(SIP_ADDRESS, location.getAddress(), 15060);
            } else if (type.equals(REST_API)) {
                address = new Address(REST_API, location.getFqdn(), settings.getHttpsPort());
            }
            addresses.add(address);
        }
        return addresses;
    }

    @Override
    public Address getAddress(DnsManager manager, AddressType t, Collection<Address> addresses, Location whoIsAsking) {
        if (!t.equals(SIP_ADDRESS)) {
            return null;
        }

        return new Address(t, String.format("vm.%s", whoIsAsking.getFqdn()));
    }

    @Override
    public List<ResourceRecords> getResourceRecords(DnsManager manager, Location whoIsAsking) {
        ResourceRecords tcpRecords = new ResourceRecords("_sip._tcp", "vm");
        ResourceRecords udpRecords = new ResourceRecords("_sip._udp", "vm");
        List<ResourceRecords> records = new LinkedList<ResourceRecords>();
        Collection<Address> addresses = getAvailableAddresses(manager.getAddressManager(), SIP_ADDRESS, whoIsAsking);
        if (addresses != null && addresses.isEmpty()) {
            return records;
        }
        tcpRecords.addAddresses(addresses);
        udpRecords.addAddresses(addresses);
        records.add(tcpRecords);
        records.add(udpRecords);
        return records;
    }

    public void setPilotSettingsDao(BeanWithSettingsDao<CallPilotSettings> pilotSettingsDao) {
        m_pilotSettingsDao = pilotSettingsDao;
    }

    public void setConfigManager(ConfigManager configManager) {
        m_configManager = configManager;
    }

    public void setFeatureManager(FeatureManager featureManager) {
        m_featureManager = featureManager;
    }

    @Override
    public Collection<ProcessDefinition> getProcessDefinitions(SnmpManager manager, Location location) {
        boolean enabled = manager.getFeatureManager().isFeatureEnabled(FEATURE, location);
        return (enabled ? Collections.singleton(new ProcessDefinition("sipxivr", ".*\\s-Dprocname=sipxivr\\s.*"))
                : null);
    }

}
