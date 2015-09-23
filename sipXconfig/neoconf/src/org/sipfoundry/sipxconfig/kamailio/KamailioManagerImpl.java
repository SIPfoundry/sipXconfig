/**
 *
 * Copyright (C) 2015 SIPFoundry., certain elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 */
package org.sipfoundry.sipxconfig.kamailio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.sipfoundry.sipxconfig.address.Address;
import org.sipfoundry.sipxconfig.address.AddressManager;
import org.sipfoundry.sipxconfig.address.AddressProvider;
import org.sipfoundry.sipxconfig.address.AddressType;
import org.sipfoundry.sipxconfig.alarm.AlarmDefinition;
import org.sipfoundry.sipxconfig.alarm.AlarmProvider;
import org.sipfoundry.sipxconfig.alarm.AlarmServerManager;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigManager;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.dialplan.DialPlanContext;
import org.sipfoundry.sipxconfig.dns.DnsManager;
import org.sipfoundry.sipxconfig.dns.DnsProvider;
import org.sipfoundry.sipxconfig.dns.ResourceRecord;
import org.sipfoundry.sipxconfig.dns.ResourceRecords;
import org.sipfoundry.sipxconfig.feature.Bundle;
import org.sipfoundry.sipxconfig.feature.FeatureChangeRequest;
import org.sipfoundry.sipxconfig.feature.FeatureChangeValidator;
import org.sipfoundry.sipxconfig.feature.FeatureManager;
import org.sipfoundry.sipxconfig.feature.FeatureProvider;
import org.sipfoundry.sipxconfig.feature.GlobalFeature;
import org.sipfoundry.sipxconfig.feature.LocationFeature;
import org.sipfoundry.sipxconfig.firewall.DefaultFirewallRule;
import org.sipfoundry.sipxconfig.firewall.FirewallManager;
import org.sipfoundry.sipxconfig.firewall.FirewallProvider;
import org.sipfoundry.sipxconfig.firewall.FirewallRule;
import org.sipfoundry.sipxconfig.setting.BeanWithSettingsDao;
import org.sipfoundry.sipxconfig.setup.SetupListener;
import org.sipfoundry.sipxconfig.setup.SetupManager;
import org.sipfoundry.sipxconfig.snmp.ProcessDefinition;
import org.sipfoundry.sipxconfig.snmp.ProcessProvider;
import org.sipfoundry.sipxconfig.snmp.SnmpManager;

public class KamailioManagerImpl implements KamailioManager, FeatureProvider, AddressProvider
            , ProcessProvider, AlarmProvider, FirewallProvider, DnsProvider
            , SetupListener {

    private static final Collection<AddressType> ADDRESS_TYPES = Arrays.asList(new AddressType[] {
            TCP_ADDRESS, UDP_ADDRESS, TLS_ADDRESS
        });
    
    private FeatureManager m_featureManager;
    private BeanWithSettingsDao<KamailioSettings> m_settingsDao;
    private ConfigManager m_configManager;
    
    @Override
    public void featureChangePrecommit(FeatureManager manager, FeatureChangeValidator validator) {
        //Add later on for dependency in configuration
    }

    @Override
    public void featureChangePostcommit(FeatureManager manager, FeatureChangeRequest request) {
        if (request.hasChanged(FEATURE)) {
            m_configManager.configureEverywhere(DnsManager.FEATURE, DialPlanContext.FEATURE);
        }
    }

    @Override
    public Address getAddress(DnsManager manager, AddressType t, Collection<Address> addresses, Location whoIsAsking) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<ResourceRecords> getResourceRecords(DnsManager manager) {
        FeatureManager fm = manager.getAddressManager().getFeatureManager();
        List<Location> locations = fm.getLocationsForEnabledFeature(FEATURE);
        if (locations == null || locations.isEmpty()) {
            return Collections.emptyList();
        }

        String root = ""; // THE sip root resource and no resource name
        ResourceRecords[] records = new ResourceRecords[] {
            new ResourceRecords("_sip._tcp", root, false),
            new ResourceRecords("_sip._udp", root, false),
            new ResourceRecords("_sips._tcp", root, false),
            new ResourceRecords("_sip._tls", root, false)
        };
        
        KamailioSettings settings = getSettings();
        int[] ports = new int[] {
                settings.getSipTcpPort(),
                settings.getSipUdpPort(),
                settings.getSecureSipPort(),
                settings.getSecureSipPort()
        };
        for (int i = 0; i < records.length; i++) {
            for (Location l : locations) {
                records[i].addRecord(new ResourceRecord(l.getHostname(), ports[i], l.getRegionId()));
            }
        }
        
        return Arrays.asList(records);
    }

    @Override
    public Collection<DefaultFirewallRule> getFirewallRules(FirewallManager manager) {
        return DefaultFirewallRule.rules(ADDRESS_TYPES, FirewallRule.SystemId.PUBLIC);
    }

    @Override
    public Collection<AlarmDefinition> getAvailableAlarms(AlarmServerManager manager) {
        return null;
    }

    @Override
    public Collection<ProcessDefinition> getProcessDefinitions(SnmpManager manager, Location location) {
        FeatureManager featureManager = manager.getFeatureManager();
        if (!featureManager.isFeatureEnabled(FEATURE, location)) {
            return null;
        }

        ProcessDefinition def = ProcessDefinition.sysv("kamailio", true);
        return Collections.singleton(def);
    }

    @Override
    public Collection<Address> getAvailableAddresses(AddressManager manager, AddressType type, Location requester) {
        if (!ADDRESS_TYPES.contains(type)) {
            return null;
        }
        Collection<Address> addresses = null;
        Collection<Location> locations = m_featureManager.getLocationsForEnabledFeature(FEATURE);
        addresses = new ArrayList<Address>(locations.size());
        for (Location location : locations) {
            Address address = null;
            if (type.equals(TCP_ADDRESS)) {
                address = new Address(TCP_ADDRESS, location.getAddress(), 5060);
            } else if (type.equals(UDP_ADDRESS)) {
                address = new Address(UDP_ADDRESS, location.getAddress(), 5060);
            } else if (type.equals(TLS_ADDRESS)) {
                address = new Address(TCP_ADDRESS, location.getAddress(), 5061);
            }
            addresses.add(address);
        }

        return addresses;
    }

    @Override
    public Collection<GlobalFeature> getAvailableGlobalFeatures(FeatureManager featureManager) {
        return null;
    }

    @Override
    public Collection<LocationFeature> getAvailableLocationFeatures(FeatureManager featureManager, Location l) {
        return Collections.singleton(FEATURE);
    }

    @Override
    public void getBundleFeatures(FeatureManager featureManager, Bundle b) {
        if (b == Bundle.CORE_TELEPHONY) {
            b.addFeature(FEATURE);
        }
    }

    @Override
    public KamailioSettings getSettings() {
        return m_settingsDao.findOrCreateOne();
    }

    @Override
    public void saveSettings(KamailioSettings settings) {
        m_settingsDao.upsert(settings);
    }
    
    @Override
    public boolean setup(SetupManager manager) {
        if (manager.isFalse(FEATURE.getId())) {
            Location primary = manager.getConfigManager().getLocationManager().getPrimaryLocation();
            manager.getFeatureManager().enableLocationFeature(FEATURE, primary, true);
            manager.setTrue(FEATURE.getId());
        }
        return true;
    }
    
    public FeatureManager getFeatureManager() {
        return m_featureManager;
    }

    public void setFeatureManager(FeatureManager featureManager) {
        this.m_featureManager = featureManager;
    }

    public ConfigManager getConfigManager() {
        return m_configManager;
    }

    public void setConfigManager(ConfigManager configManager) {
        this.m_configManager = configManager;
    }

    public BeanWithSettingsDao<KamailioSettings> getSettingsDao() {
        return m_settingsDao;
    }

    public void setSettingsDao(BeanWithSettingsDao<KamailioSettings> settingsDao) {
        this.m_settingsDao = settingsDao;
    }
    
    

}
