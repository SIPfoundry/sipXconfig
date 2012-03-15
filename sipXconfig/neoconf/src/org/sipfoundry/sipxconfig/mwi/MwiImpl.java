/**
 *
 *
 * Copyright (c) 2012 eZuce, Inc. All rights reserved.
 * Contributed to SIPfoundry under a Contributor Agreement
 *
 * This software is free software; you can redistribute it and/or modify it under
 * the terms of the Affero General Public License (AGPL) as published by the
 * Free Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 */
package org.sipfoundry.sipxconfig.mwi;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.sipfoundry.sipxconfig.address.Address;
import org.sipfoundry.sipxconfig.address.AddressManager;
import org.sipfoundry.sipxconfig.address.AddressProvider;
import org.sipfoundry.sipxconfig.address.AddressType;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigManager;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.dialplan.DialPlanContext;
import org.sipfoundry.sipxconfig.dns.DnsManager;
import org.sipfoundry.sipxconfig.dns.DnsProvider;
import org.sipfoundry.sipxconfig.dns.ResourceRecords;
import org.sipfoundry.sipxconfig.feature.Bundle;
import org.sipfoundry.sipxconfig.feature.FeatureListener;
import org.sipfoundry.sipxconfig.feature.FeatureManager;
import org.sipfoundry.sipxconfig.feature.FeatureProvider;
import org.sipfoundry.sipxconfig.feature.GlobalFeature;
import org.sipfoundry.sipxconfig.feature.LocationFeature;
import org.sipfoundry.sipxconfig.setting.BeanWithSettingsDao;
import org.sipfoundry.sipxconfig.snmp.ProcessDefinition;
import org.sipfoundry.sipxconfig.snmp.ProcessProvider;
import org.sipfoundry.sipxconfig.snmp.SnmpManager;

public class MwiImpl implements AddressProvider, FeatureProvider, Mwi, DnsProvider, FeatureListener, ProcessProvider {
    private static final Collection<AddressType> ADDRESSES = Arrays.asList(SIP_UDP, SIP_TCP, HTTP_API);
    private BeanWithSettingsDao<MwiSettings> m_settingsDao;
    private ConfigManager m_configManager;

    public MwiSettings getSettings() {
        return m_settingsDao.findOrCreateOne();
    }

    public void saveSettings(MwiSettings settings) {
        m_settingsDao.upsert(settings);
    }

    @Override
    public Collection<GlobalFeature> getAvailableGlobalFeatures() {
        return null;
    }

    @Override
    public Collection<LocationFeature> getAvailableLocationFeatures(Location l) {
        return Collections.singleton(FEATURE);
    }

    @Override
    public Collection<AddressType> getSupportedAddressTypes(AddressManager manager) {
        return ADDRESSES;
    }

    @Override
    public Collection<Address> getAvailableAddresses(AddressManager manager, AddressType type, Object requester) {
        if (!ADDRESSES.contains(type)) {
            return null;
        }

        MwiSettings settings = getSettings();
        List<Location> locations = manager.getFeatureManager().getLocationsForEnabledFeature(FEATURE);
        List<Address> addresses = new ArrayList<Address>(locations.size());
        for (Location location : locations) {
            Address address;
            if (type.equals(HTTP_API)) {
                address = new Address(HTTP_API, location.getAddress(), settings.getHttpApiPort());
            } else if (type.equals(SIP_UDP)) {
                address = new Address(SIP_UDP, location.getAddress(), settings.getUdpPort());
            } else {
                address = new Address(SIP_TCP, location.getAddress(), settings.getTcp());
            }
            addresses.add(address);
        }
        return addresses;
    }

    public void setSettingsDao(BeanWithSettingsDao<MwiSettings> settingsDao) {
        m_settingsDao = settingsDao;
    }

    @Override
    public Address getAddress(DnsManager manager, AddressType t, Collection<Address> addresses, Location whoIsAsking) {
        if (!t.equals(SIP_TCP)) {
            return null;
        }

        // NOTE: drop port, it's in DNS resource records
        return new Address(t, format("mwi.%s", whoIsAsking.getFqdn()));
    }

    @Override
    public List<ResourceRecords> getResourceRecords(DnsManager manager, Location whoIsAsking) {
        ResourceRecords rr = new ResourceRecords("_sip._tcp", "mwi");
        Collection<Address> addresses = getAvailableAddresses(manager.getAddressManager(), SIP_TCP, whoIsAsking);
        rr.addAddresses(addresses);
        return Collections.singletonList(rr);
    }

    @Override
    public void enableLocationFeature(FeatureManager manager, FeatureEvent event, LocationFeature feature,
            Location location) {
        if (!feature.equals(FEATURE)) {
            return;
        }

        switch (event) {
        case POST_DISABLE:
        case POST_ENABLE:
            m_configManager.configureEverywhere(DnsManager.FEATURE, DialPlanContext.FEATURE);
            break;
        default:
            break;
        }
    }

    @Override
    public void enableGlobalFeature(FeatureManager manager, FeatureEvent event, GlobalFeature feature) {
    }

    public void setConfigManager(ConfigManager configManager) {
        m_configManager = configManager;
    }

    @Override
    public Collection<ProcessDefinition> getProcessDefinitions(SnmpManager manager, Location location) {
        boolean enabled = manager.getFeatureManager().isFeatureEnabled(FEATURE, location);
        return (enabled ? Collections.singleton(new ProcessDefinition("sipstatus")) : null);
    }

    @Override
    public void getBundleFeatures(Bundle b) {
        if (b.isBasic()) {
            b.addFeature(FEATURE);
        }
    }
}
