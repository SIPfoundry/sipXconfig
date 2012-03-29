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
package org.sipfoundry.sipxconfig.firewall;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.sipfoundry.sipxconfig.address.Address;
import org.sipfoundry.sipxconfig.address.AddressManager;
import org.sipfoundry.sipxconfig.address.AddressType;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigManager;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigProvider;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigRequest;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigUtils;
import org.sipfoundry.sipxconfig.cfgmgt.KeyValueConfiguration;
import org.sipfoundry.sipxconfig.cfgmgt.YamlConfiguration;
import org.sipfoundry.sipxconfig.commserver.Location;

public class FirewallConfig implements ConfigProvider {
    private FirewallManager m_firewallManager;
    private AddressManager m_addressManager;

    @Override
    public void replicate(ConfigManager manager, ConfigRequest request) throws IOException {
        if (!request.applies(FirewallManager.FEATURE)) {
            return;
        }

        File gdir = manager.getGlobalDataDirectory();
        boolean enabled = manager.getFeatureManager().isFeatureEnabled(FirewallManager.FEATURE);
        ConfigUtils.enableCfengineClass(gdir, "firewall.cfdat", enabled, "firewall");
        if (!enabled) {
            return;
        }

        FirewallSettings settings = m_firewallManager.getSettings();
        Writer sysctl = new FileWriter(new File(gdir, "sysctl.part"));
        try {
            writeSysctl(sysctl, settings);
        } finally {
            IOUtils.closeQuietly(sysctl);
        }

        List<FirewallRule> rules = m_firewallManager.getFirewallRules();
        for (Location location : request.locations(manager)) {
            File dir = manager.getLocationDataDirectory(location);
            Writer config = new FileWriter(new File(dir, "firewall.yaml"));
            try {
                writeIptables(config, rules, location);
            } finally {
                IOUtils.closeQuietly(config);
            }
        }
    }

    void writeSysctl(Writer w, FirewallSettings settings) throws IOException {
        KeyValueConfiguration c = KeyValueConfiguration.equalsSeparated(w);
        c.write(settings.getSettings().getSetting("sysctl"));
    }

    void writeIptables(Writer w, List<FirewallRule> rules, Location location) throws IOException {
        YamlConfiguration c = new YamlConfiguration(w);
        c.startArray("rules");
        for (FirewallRule rule : rules) {
            AddressType type = rule.getAddressType();
            List<Address> addresses = m_addressManager.getAddresses(type, location);
            if (addresses != null) {
                for (Address address : addresses) {
                    c.write("destination", address.getPort());
                    c.write("priority", rule.isPriority());
                    c.nextElement();
                }
            }
        }
        c.endArray();
    }

    public void setFirewallManager(FirewallManager firewallManager) {
        m_firewallManager = firewallManager;
    }

    public void setAddressManager(AddressManager addressManager) {
        m_addressManager = addressManager;
    }
}
