/*
 * Copyright (C) 2012 eZuce Inc., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the AGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.setup;

import org.sipfoundry.sipxconfig.cfgmgt.ConfigManager;
import org.sipfoundry.sipxconfig.common.SystemTaskEntryPoint;

public class SetupMain implements SystemTaskEntryPoint {
    private ConfigManager m_configManager;

    @Override
    public void runSystemTask(String[] args) {
        m_configManager.setup();
    }

    public void setConfigManager(ConfigManager configManager) {
        m_configManager = configManager;
    }
}
