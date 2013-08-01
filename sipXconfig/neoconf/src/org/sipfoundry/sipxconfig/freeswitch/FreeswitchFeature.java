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
package org.sipfoundry.sipxconfig.freeswitch;

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
import org.sipfoundry.sipxconfig.commserver.SettingsWithLocationDao;
import org.sipfoundry.sipxconfig.commserver.SipxReplicationContext;
import org.sipfoundry.sipxconfig.commserver.imdb.DataSet;
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
import org.sipfoundry.sipxconfig.proxy.ProxyManager;
import org.sipfoundry.sipxconfig.snmp.ProcessDefinition;
import org.sipfoundry.sipxconfig.snmp.ProcessProvider;
import org.sipfoundry.sipxconfig.snmp.SnmpManager;
import org.springframework.beans.factory.annotation.Required;

public class FreeswitchFeature implements FeatureProvider, AddressProvider, ProcessProvider, FirewallProvider {
    public static final LocationFeature FEATURE = new LocationFeature("freeSwitch");
    public static final AddressType SIP_ADDRESS = AddressType.sipTcp("freeswitch-sip");
    public static final AddressType SIP_UDP_ADDRESS = AddressType.sipUdp("freeswitch-sip-udp");
    public static final AddressType RTP_ADDRESS = new AddressType("freeswitch-rtp", "rtp:%s:%d",
            FreeswitchSettings.RTP_START_PORT, AddressType.Protocol.udp);
    public static final AddressType XMLRPC_ADDRESS = new AddressType("freeswitch-xmlrpc", "http://%s:%d/RPC2");
    public static final AddressType EVENT_ADDRESS = new AddressType("freeswitch-event");
    public static final AddressType ACC_EVENT_ADDRESS = new AddressType("acc-freeswitch-event");

    private static final String PROCESS = "sipxfreeswitch";

    private SettingsWithLocationDao<FreeswitchSettings> m_settingsDao;
    private SipxReplicationContext m_sipxReplicationContext;
    private ConfigManager m_configManager;

    public FreeswitchSettings getSettings(Location location) {
        return m_settingsDao.findOrCreate(location);
    }

    public void saveSettings(FreeswitchSettings settings) {
        m_settingsDao.upsert(settings);
        m_sipxReplicationContext.generateAll(DataSet.ALIAS);
        m_configManager.configureEverywhere(FEATURE);
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
    public Collection<DefaultFirewallRule> getFirewallRules(FirewallManager manager) {
        DefaultFirewallRule[] rules = new DefaultFirewallRule[] {
            new DefaultFirewallRule(XMLRPC_ADDRESS), new DefaultFirewallRule(EVENT_ADDRESS),
            new DefaultFirewallRule(ACC_EVENT_ADDRESS), new DefaultFirewallRule(SIP_ADDRESS),
            new DefaultFirewallRule(SIP_UDP_ADDRESS),
            new DefaultFirewallRule(RTP_ADDRESS, FirewallRule.SystemId.PUBLIC, true)
        };
        return Arrays.asList(rules);
    }

    @Override
    public Collection<Address> getAvailableAddresses(AddressManager manager, AddressType type, Location requester) {
        if (!type.equalsAnyOf(SIP_ADDRESS, SIP_UDP_ADDRESS, XMLRPC_ADDRESS, EVENT_ADDRESS, ACC_EVENT_ADDRESS,
                RTP_ADDRESS)) {
            return null;
        }

        List<Location> locations = manager.getFeatureManager().getLocationsForEnabledFeature(FEATURE);
        if (locations.isEmpty()) {
            return null;
        }

        List<Address> addresses = new ArrayList<Address>();
        for (Location location : locations) {
            FreeswitchSettings settings = getSettings(location);
            Address address = null;
            if (type.equals(XMLRPC_ADDRESS)) {
                address = new Address(type, location.getAddress(), settings.getXmlRpcPort());
            } else if (type.equals(EVENT_ADDRESS)) {
                address = new Address(type, location.getAddress(), settings.getEventSocketPort());
            } else if (type.equals(ACC_EVENT_ADDRESS)) {
                address = new Address(type, location.getAddress(), settings.getAccEventSocketPort());
            } else if (type.equals(SIP_ADDRESS) || type.equals(SIP_UDP_ADDRESS)) {
                address = new Address(type, location.getAddress(), settings.getFreeswitchSipPort());
            } else if (type.equalsAnyOf(RTP_ADDRESS)) {
                address = new Address(type, location.getAddress());
                address.setEndPort(FreeswitchSettings.RTP_END_PORT);
            }
            addresses.add(address);
        }

        return addresses;
    }

    public void setSettingsDao(SettingsWithLocationDao<FreeswitchSettings> settingsDao) {
        m_settingsDao = settingsDao;
    }

    @Override
    public Collection<ProcessDefinition> getProcessDefinitions(SnmpManager manager, Location location) {
        if (!manager.getFeatureManager().isFeatureEnabled(FEATURE, location)) {
            return null;
        }
        ProcessDefinition def = ProcessDefinition.sipx("freeswitch", PROCESS, PROCESS);
        return Collections.singleton(def);
    }

    @Override
    public void getBundleFeatures(FeatureManager featureManager, Bundle b) {
        if (b == Bundle.CORE_TELEPHONY) {
            b.addFeature(FEATURE);
        }
    }

    @Override
    public void featureChangePrecommit(FeatureManager manager, FeatureChangeValidator validator) {
        validator.requiresAtLeastOne(FEATURE, ProxyManager.FEATURE);
    }

    @Override
    public void featureChangePostcommit(FeatureManager manager, FeatureChangeRequest request) {
    }

    @Required
    public void setSipxReplicationContext(SipxReplicationContext sipxReplicationContext) {
        m_sipxReplicationContext = sipxReplicationContext;
    }

    public void setConfigManager(ConfigManager configManager) {
        m_configManager = configManager;
    }

    public ConfigManager getConfigManager() {
        return m_configManager;
    }
}
