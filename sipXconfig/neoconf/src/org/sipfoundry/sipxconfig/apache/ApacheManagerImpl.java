/*
 * Copyright (C) 2012 eZuce Inc., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the AGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.apache;


import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.sipfoundry.sipxconfig.cfgmgt.ConfigManager;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigProvider;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigRequest;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigUtils;
import org.sipfoundry.sipxconfig.common.SipxHibernateDaoSupport;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.firewall.DefaultFirewallRule;
import org.sipfoundry.sipxconfig.firewall.FirewallManager;
import org.sipfoundry.sipxconfig.firewall.FirewallProvider;
import org.sipfoundry.sipxconfig.firewall.FirewallRule;
import org.sipfoundry.sipxconfig.setup.SetupListener;
import org.sipfoundry.sipxconfig.setup.SetupManager;

public class ApacheManagerImpl extends SipxHibernateDaoSupport<Object> implements ApacheManager, ConfigProvider,
    SetupListener, FirewallProvider {

    @Override
    public void replicate(ConfigManager manager, ConfigRequest request) throws IOException {
        if (!request.applies(FEATURE)) {
            return;
        }

        Set<Location> locations = request.locations(manager);
        for (Location l : locations) {
            File dir = manager.getLocationDataDirectory(l);
            boolean enabled = manager.getFeatureManager().isFeatureEnabled(FEATURE, l);
            ConfigUtils.enableCfengineClass(dir, "apache.cfdat", enabled, "apache");
        }
    }

    @Override
    public void setup(SetupManager manager) {
        if (manager.isSetup(FEATURE.getId())) {
            Location primary = manager.getConfigManager().getLocationManager().getPrimaryLocation();
            manager.getFeatureManager().enableLocationFeature(FEATURE, primary, true);
            manager.setSetup(FEATURE.getId());
        }
    }

    @Override
    public void avoidCheckstyleError() {
    }

    @Override
    public Collection<DefaultFirewallRule> getFirewallRules(FirewallManager manager) {
        return DefaultFirewallRule.rules(Arrays.asList(HTTP_ADDRESS, HTTPS_ADDRESS), FirewallRule.SystemId.PUBLIC);
    }
}
