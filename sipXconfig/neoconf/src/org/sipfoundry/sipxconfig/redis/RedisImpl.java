/**
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
package org.sipfoundry.sipxconfig.redis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.sipfoundry.sipxconfig.address.Address;
import org.sipfoundry.sipxconfig.address.AddressManager;
import org.sipfoundry.sipxconfig.address.AddressProvider;
import org.sipfoundry.sipxconfig.address.AddressType;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigManager;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigProvider;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigRequest;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigUtils;
import org.sipfoundry.sipxconfig.cfgmgt.KeyValueConfiguration;
import org.sipfoundry.sipxconfig.commserver.Location;
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
import org.sipfoundry.sipxconfig.snmp.ProcessDefinition;
import org.sipfoundry.sipxconfig.snmp.ProcessProvider;
import org.sipfoundry.sipxconfig.snmp.SnmpManager;

public class RedisImpl implements Redis, ConfigProvider, ProcessProvider, FirewallProvider, AddressProvider,
        FeatureProvider {

    @Override
    public void avoidChecksytleError() {
    }

    @Override
    public Collection<DefaultFirewallRule> getFirewallRules(FirewallManager manager) {
        Set<AddressType> types = Collections.singleton(SERVER);
        return DefaultFirewallRule.rules(types, FirewallRule.SystemId.CLUSTER);
    }

    @Override
    public Collection<ProcessDefinition> getProcessDefinitions(SnmpManager manager, Location location) {
        FeatureManager featureManager = manager.getFeatureManager();
        if (!featureManager.isFeatureEnabled(FEATURE, location)) {
            return null;
        }

        ProcessDefinition def = ProcessDefinition.sysv("redis-server");
        return Collections.singleton(def);
    }

    @Override
    public void replicate(ConfigManager manager, ConfigRequest request) throws IOException {
        if (!request.applies(FEATURE)) {
            return;
        }

        Set<Location> locations = request.locations(manager);
        for (Location location : locations) {
            File dir = manager.getLocationDataDirectory(location);
            boolean on = manager.getFeatureManager().isFeatureEnabled(FEATURE, location);
            ConfigUtils.enableCfengineClass(dir, "redis.cfdat", on, FEATURE.getId());

            Address redisApi = new Address(SERVER, location.getAddress());
            Writer w = new FileWriter(new File(dir, "redis-client.ini"));
            try {
                writeClient(w, on, redisApi);
            } finally {
                IOUtils.closeQuietly(w);
            }
        }
    }

    void writeClient(Writer w, boolean on, Address redis) throws IOException {
        KeyValueConfiguration c = KeyValueConfiguration.equalsSeparated(w);
        c.write("enabled", on);
        c.write("tcp-port", redis.getCanonicalPort());
        c.write("tcp-address", redis.getAddress());
    }

    @Override
    public Collection<Address> getAvailableAddresses(AddressManager manager, AddressType type, Location requester) {
        if (!type.equals(Redis.SERVER)) {
            return null;
        }
        List<Location> locations = manager.getFeatureManager().getLocationsForEnabledFeature(FEATURE);
        List<Address> addresses = Location.toAddresses(SERVER, locations);
        return addresses;
    }

    @Override
    public void featureChangePrecommit(FeatureManager manager, FeatureChangeValidator validator) {
    }

    @Override
    public void featureChangePostcommit(FeatureManager manager, FeatureChangeRequest request) {
    }

    @Override
    public Collection<GlobalFeature> getAvailableGlobalFeatures(FeatureManager featureManager) {
        return null;
    }

    @Override
    public Collection<LocationFeature> getAvailableLocationFeatures(FeatureManager featureManager, Location l) {
        return Arrays.asList(FEATURE);
    }

    @Override
    public void getBundleFeatures(FeatureManager featureManager, Bundle b) {
        if (b == Bundle.CORE_TELEPHONY) {
            b.addFeature(FEATURE);
        }
    }
}
