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
package org.sipfoundry.sipxconfig.nattraversal;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.sipfoundry.sipxconfig.address.Address;
import org.sipfoundry.sipxconfig.address.AddressManager;
import org.sipfoundry.sipxconfig.address.AddressProvider;
import org.sipfoundry.sipxconfig.address.AddressType;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.feature.Bundle;
import org.sipfoundry.sipxconfig.feature.FeatureListener;
import org.sipfoundry.sipxconfig.feature.FeatureManager;
import org.sipfoundry.sipxconfig.feature.FeatureProvider;
import org.sipfoundry.sipxconfig.feature.GlobalFeature;
import org.sipfoundry.sipxconfig.feature.LocationFeature;
import org.sipfoundry.sipxconfig.firewall.DefaultFirewallRule;
import org.sipfoundry.sipxconfig.firewall.FirewallManager;
import org.sipfoundry.sipxconfig.firewall.FirewallProvider;
import org.sipfoundry.sipxconfig.firewall.FirewallRule;
import org.sipfoundry.sipxconfig.proxy.ProxyManager;
import org.sipfoundry.sipxconfig.setting.BeanWithSettingsDao;
import org.sipfoundry.sipxconfig.snmp.ProcessDefinition;
import org.sipfoundry.sipxconfig.snmp.ProcessProvider;
import org.sipfoundry.sipxconfig.snmp.SnmpManager;

public class NatTraversalImpl implements FeatureListener, NatTraversal, FeatureProvider, ProcessProvider,
    FirewallProvider, AddressProvider {
    private BeanWithSettingsDao<NatSettings> m_settingsDao;

    public NatSettings getSettings() {
        return m_settingsDao.findOrCreateOne();
    }

    public void saveSettings(NatSettings settings) {
        m_settingsDao.upsert(settings);
    }

    @Override
    public void enableLocationFeature(FeatureManager manager, FeatureEvent event, LocationFeature feature,
            Location location) {
    }

    @Override
    public void enableGlobalFeature(FeatureManager manager, FeatureEvent event, GlobalFeature feature) {
        if (feature.equals(FEATURE)) {
            if (event == FeatureEvent.PRE_ENABLE) {
                NatSettings settings = getSettings();
                if (!settings.isBehindNat()) {
                    settings.setBehindNat(true);
                    saveSettings(settings);
                }
            } else if (event == FeatureEvent.PRE_DISABLE) {
                NatSettings settings = getSettings();
                if (settings.isBehindNat()) {
                    settings.setBehindNat(false);
                    saveSettings(settings);
                }
            }
        }
    }

    public void setSettingsDao(BeanWithSettingsDao<NatSettings> settingsDao) {
        m_settingsDao = settingsDao;
    }

    @Override
    public Collection<GlobalFeature> getAvailableGlobalFeatures() {
        return Collections.singleton(FEATURE);
    }

    @Override
    public Collection<LocationFeature> getAvailableLocationFeatures(Location l) {
        return null;
    }

    @Override
    public Collection<ProcessDefinition> getProcessDefinitions(SnmpManager manager, Location location) {
        boolean relayEnabled = manager.getFeatureManager().isFeatureEnabled(FEATURE);
        boolean proxyEnabled = manager.getFeatureManager().isFeatureEnabled(ProxyManager.FEATURE, location);
        return (relayEnabled && proxyEnabled ? Collections.singleton(new ProcessDefinition("sipxrelay",
            ".*\\s-Dprocname=sipxrelay\\s.*")) : null);
    }

    @Override
    public void getBundleFeatures(Bundle b) {
        if (b.isRouter()) {
            // NAT traveral as basic bundle is debatable but proxy requires it ATM AFAIU
            b.addFeature(FEATURE);
        }
    }

    @Override
    public Collection<Address> getAvailableAddresses(AddressManager manager, AddressType type, Location requester) {
        if (!type.equals(RELAY_RTP)) {
            return null;
        }

        boolean relayEnabled = manager.getFeatureManager().isFeatureEnabled(FEATURE);
        if (!relayEnabled) {
            return null;
        }

        List<Address> proxy = manager.getAddresses(ProxyManager.TCP_ADDRESS);
        List<Address> addresses = new ArrayList<Address>(proxy.size());
        for (Address proxyAddress : proxy) {
            Address a = new Address(type, proxyAddress.getAddress(), NatSettings.START_RTP_PORT);
            a.setEndPort(NatSettings.END_RTP_PORT);
            addresses.add(a);
        }

        return addresses;
    }

    @Override
    public Collection<DefaultFirewallRule> getFirewallRules(FirewallManager manager) {
        return Arrays.asList(new DefaultFirewallRule(RELAY_RTP, FirewallRule.SystemId.PUBLIC));
    }
}
