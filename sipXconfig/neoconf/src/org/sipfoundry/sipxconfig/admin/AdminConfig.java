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
package org.sipfoundry.sipxconfig.admin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.sipfoundry.sipxconfig.cdr.CdrManager;
import org.sipfoundry.sipxconfig.cfgmgt.CfengineModuleConfiguration;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigManager;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigProvider;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigRequest;
import org.sipfoundry.sipxconfig.cfgmgt.KeyValueConfiguration;
import org.sipfoundry.sipxconfig.cfgmgt.LoggerKeyValueConfiguration;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.proxy.ProxyManager;
import org.sipfoundry.sipxconfig.setting.Setting;
import org.sipfoundry.sipxconfig.setting.SettingUtil;

public class AdminConfig implements ConfigProvider {
    private AdminContext m_adminContext;
    private String m_adminSettingsKey = "configserver-config";

    @Override
    public void replicate(ConfigManager manager, ConfigRequest request) throws IOException {
        if (!request.applies(AdminContext.FEATURE)) {
            return;
        }

        Set<Location> locations = request.locations(manager);
        AdminSettings settings = m_adminContext.getSettings();
        Setting adminSettings = settings.getSettings().getSetting(m_adminSettingsKey);

        for (Location l : locations) {
            File dir = manager.getLocationDataDirectory(l);
            if (l.isPrimary() || manager.getFeatureManager().isFeatureEnabled(ProxyManager.FEATURE, l)
                || manager.getFeatureManager().isFeatureEnabled(CdrManager.FEATURE, l)) {
                Writer pwd = new FileWriter(new File(dir, "postgres-pwd.properties"));
                Writer pwdCfdat = new FileWriter(new File(dir, "postgres-pwd.cfdat"));
                try {
                    KeyValueConfiguration cfg = KeyValueConfiguration.equalsSeparated(pwd);
                    CfengineModuleConfiguration cfgCfdat = new CfengineModuleConfiguration(pwdCfdat);
                    cfg.write("password", settings.getPostgresPassword());
                    cfgCfdat.write("NEW_POSTGRESQL_PASSWORD", settings.getPostgresPassword());
                } finally {
                    IOUtils.closeQuietly(pwd);
                    IOUtils.closeQuietly(pwdCfdat);
                }
            }
            if (!l.isPrimary()) {
                continue;
            }
            String log4jFileName = "log4j.properties.part";
            String[] logLevelKeys = settings.getLogLevelKeys();
            SettingUtil.writeLog4jSetting(adminSettings, dir, log4jFileName, logLevelKeys);

            Writer w = new FileWriter(new File(dir, "sipxconfig.properties.ui"));
            try {
                writeConfig(w, settings);
            } finally {
                IOUtils.closeQuietly(w);
            }
        }
    }

    void writeConfig(Writer w, AdminSettings settings) throws IOException {
        LoggerKeyValueConfiguration config = LoggerKeyValueConfiguration.equalsSeparated(w);
        config.writeSettings(settings.getSettings().getSetting(m_adminSettingsKey));
    }

    public void setAdminContext(AdminContext adminContext) {
        m_adminContext = adminContext;
    }
}
