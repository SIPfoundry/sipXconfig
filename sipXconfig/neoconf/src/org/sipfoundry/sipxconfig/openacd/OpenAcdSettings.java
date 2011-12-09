/*
 * Copyright (C) 2011 eZuce Inc., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the AGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.openacd;

import java.util.ArrayList;
import java.util.List;

import org.sipfoundry.sipxconfig.domain.DomainManager;
import org.sipfoundry.sipxconfig.setting.BeanWithSettings;
import org.sipfoundry.sipxconfig.setting.Setting;
import org.sipfoundry.sipxconfig.setting.SettingEntry;

public class OpenAcdSettings extends BeanWithSettings implements OpenAcdConfigObjectProvider {
    private static final String FS_ENABLED = "freeswitch_media_manager/FREESWITCH_ENABLED";
    private static final String C_NODE = "freeswitch_media_manager/C_NODE";
    private static final String DIAL_STRING = "freeswitch_media_manager/DIAL_STRING";
    private static final String DIALPLAN_LISTENER = "agent_configuration/DIALPLAN_LISTENER";
    private static final String LOG_DIR = "openacd-config/log_dir";
    private DomainManager m_domainManager;
    private String m_logDirectory;

    public OpenAcdSettings() {
        this(null);
    }

    OpenAcdSettings(Object defaults) {
        addDefaultBeanSettingHandler(defaults != null ? defaults : new Defaults(this));
    }

    static class Defaults {
        private OpenAcdSettings m_settings;

        Defaults(OpenAcdSettings settings) {
            m_settings = settings;
        }

        @SettingEntry(path = C_NODE)
        public String getCNode() {
            // change this when installing on different locations will be supported
            return String.format("%s@127.0.0.1", "freeswitch");
        }

        @SettingEntry(path = DIAL_STRING)
        public String getDialString() {
            // change this when installing on different locations will be supported
            return String.format(
                    "{ignore_early_media=true}sofia/%s/$1;sipx-noroute=VoiceMail;sipx-userforward=false",
                    m_settings.m_domainManager.getDomainName());
        }

        @SettingEntry(path = LOG_DIR)
        public String getLogDir() {
            return m_settings.m_logDirectory;
        }
    }

    public String getCNode() {
        return getSettingValue(C_NODE);
    }

    public boolean isEnabled() {
        return (Boolean) getSettingTypedValue(FS_ENABLED);
    }

    public String getLogLevel() {
        return getSettingValue("openacd-config/log_level");
    }

    public String getLogDir() {
        return getSettingValue(LOG_DIR);
    }

    public String getDialString() {
        return getSettingValue(DIAL_STRING);
    }

    public boolean getDialPlanListener() {
        return (Boolean) getSettingTypedValue(DIALPLAN_LISTENER);
    }

    @Override
    protected Setting loadSettings() {
        return getModelFilesContext().loadModelFile("sipxopenacd/sipxopenacd.xml");
    }

    public void setDomainManager(DomainManager domainManager) {
        m_domainManager = domainManager;
    }

    @Override
    public List<OpenAcdConfigObject> getConfigObjects() {
        List<OpenAcdConfigObject> objects = new ArrayList<OpenAcdConfigObject>();
        objects.add(new FreeswitchMediaCommand(isEnabled(), getCNode(), getDialString()));
        objects.add(new OpenAcdAgentConfigCommand(getDialPlanListener()));
        objects.add(new OpenAcdLogConfigCommand(getLogLevel(), getLogDir()));
        return objects;
    }

    public void setLogDirectory(String logDirectory) {
        m_logDirectory = logDirectory;
    }
}
