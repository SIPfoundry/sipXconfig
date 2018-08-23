package org.sipfoundry.sipxconfig.kamailio;

import java.util.Arrays;
import java.util.Collection;

import org.sipfoundry.sipxconfig.cfgmgt.DeployConfigOnEdit;
import org.sipfoundry.sipxconfig.feature.Feature;
import org.sipfoundry.sipxconfig.setting.PersistableSettings;
import org.sipfoundry.sipxconfig.setting.Setting;

public class KamailioSettings extends PersistableSettings implements DeployConfigOnEdit {
    /* Commong Settings */
    public static final String ROOT_SETTING = "kamailio-configuration";
    public static final String LOG_SETTING = ROOT_SETTING + "/LOG_LEVEL";
    public static final String TABLE_DIALOG_VERSION = ROOT_SETTING + "/TABLE_DIALOG_VERSION";
    public static final String TABLE_DIALOG_VARS_VERSION = ROOT_SETTING + "/TABLE_DIALOG_VARS_VERSION";    
    public static final String DEFAULT_SHARED_MEMORY = ROOT_SETTING + "/DEFAULT_SHARED_MEMORY";
    public static final String DEFAULT_PRIVATE_MEMORY = ROOT_SETTING + "/DEFAULT_PRIVATE_MEMORY";
    public static final String DEFAULT_CHILDREN = ROOT_SETTING + "/DEFAULT_CHILDREN";
    public static final String TCP_WRITE_QUEUE_SIZE = ROOT_SETTING + "/TCP_WRITE_QUEUE_SIZE";
    public static final String TCP_READ_BUFFER_SIZE = ROOT_SETTING + "/TCP_READ_BUFFER_SIZE";
    public static final String TCP_CONNECTION_LIFETIME = ROOT_SETTING + "/TCP_CONNECTION_LIFETIME";
    public static final String FD_LIMIT = ROOT_SETTING + "/FD_LIMIT";
    public static final String DEFAULT_DUMP_CORE = ROOT_SETTING + "/DEFAULT_DUMP_CORE";
    
    /* Proxy Settings */
    public static final String PROXY_ROOT_SETTING = "kamailio-proxy-configuration";
    public static final String PROXY_SIP_TCP_PORT_SETTING = PROXY_ROOT_SETTING + "/TCP_PORT";
    public static final String PROXY_SIP_UDP_PORT_SETTING = PROXY_ROOT_SETTING + "/UDP_PORT";
    public static final String PROXY_SIP_TLS_PORT_SETTING = PROXY_ROOT_SETTING + "/TLS_PORT";
    public static final String PROXY_SIP_INGRESS_SRV_ROUTING_SETTING = PROXY_ROOT_SETTING + "/INGRESS_SRV_ROUTING";
    
    /* Presence Settings */
    public static final String PRESENCE_ROOT_SETTING = "kamailio-presence-configuration";
    public static final String PRESENCE_SIP_TCP_PORT_SETTING = PRESENCE_ROOT_SETTING + "/TCP_PORT";
    public static final String PRESENCE_SIP_UDP_PORT_SETTING = PRESENCE_ROOT_SETTING + "/UDP_PORT";
    public static final String PRESENCE_SIP_TLS_PORT_SETTING = PRESENCE_ROOT_SETTING + "/TLS_PORT";
    public static final String ENABLE_BLF_SIPX_PLUGIN_SETTING = PRESENCE_ROOT_SETTING + "/ENABLE_BLF_SIPX_PLUGIN";
    public static final String BLF_SIPX_PLUGIN_LOG_SETTING = PRESENCE_ROOT_SETTING + "/BLF_SIPX_PLUGIN_LOG_SETTING";
    public static final String ENABLE_BLA_MESSAGE_QUEUE = PRESENCE_ROOT_SETTING + "/ENABLE_BLA_MESSAGE_QUEUE";
    public static final String ENABLE_POLL_BLA_USER_SETTING = PRESENCE_ROOT_SETTING + "/ENABLE_POLL_BLA_USER_SETTING";
    public static final String BLA_USER_POLL_INTERVAL_SETTING = PRESENCE_ROOT_SETTING + "/BLA_USER_POLL_INTERVAL_SETTING";
    public static final String ENABLE_ACTIVE_DIALOG_CHECK = PRESENCE_ROOT_SETTING + "/ENABLE_ACTIVE_DIALOG_CHECK";
    public static final String ENABLE_ACTIVE_DIALOG_COLLATE = PRESENCE_ROOT_SETTING + "/ENABLE_ACTIVE_DIALOG_COLLATE";
    public static final String ACTIVE_DIALOG_CHECK_PERIOD = PRESENCE_ROOT_SETTING + "/ACTIVE_DIALOG_CHECK_PERIOD";
    public static final String ACTIVE_DIALOG_COLLATE_PERIOD = PRESENCE_ROOT_SETTING + "/ACTIVE_DIALOG_COLLATE_PERIOD";

    public int getLogSetting() {
        return (Integer) getSettingTypedValue(LOG_SETTING);
    }
    
    public int getDialogVersion() {
        return (Integer) getSettingTypedValue(TABLE_DIALOG_VERSION);
    }
    
    public int getDialogVarsVersion() {
        return (Integer) getSettingTypedValue(TABLE_DIALOG_VARS_VERSION);
    }

    public int getDefaultSharedMemory() {
        return (Integer) getSettingTypedValue(DEFAULT_SHARED_MEMORY);
    }
    
    public int getDefaultPrivateMemory() {
        return (Integer) getSettingTypedValue(DEFAULT_PRIVATE_MEMORY);
    }
    
    public int getDefaultChildren() {
        return (Integer) getSettingTypedValue(DEFAULT_CHILDREN);
    }
    
    public int getTcpWriteQueueSize() {
        return (Integer) getSettingTypedValue(TCP_WRITE_QUEUE_SIZE);
    }
    
    public int getTcpReadBufferSize() {
        return (Integer) getSettingTypedValue(TCP_READ_BUFFER_SIZE);
    }
    
    public int getTcpConnectionLifetime() {
        return (Integer) getSettingTypedValue(TCP_CONNECTION_LIFETIME);
    }
    
	public int getFdLimit() {
		return (Integer) getSettingTypedValue(FD_LIMIT);
	}
    
    public String getDumpCore() {
        return getSettingValue(DEFAULT_DUMP_CORE);
    }
    
    public boolean isEnableDumpCore() {
    	return (Boolean) getSettingTypedValue(DEFAULT_DUMP_CORE);
    }
    
    public int getProxySipTcpPort() {
        return (Integer) getSettingTypedValue(PROXY_SIP_TCP_PORT_SETTING);
    }

    public int getProxySipUdpPort() {
        return (Integer) getSettingTypedValue(PROXY_SIP_UDP_PORT_SETTING);
    }

    public int getProxySipTlsPort() {
        return (Integer) getSettingTypedValue(PROXY_SIP_TLS_PORT_SETTING);
    }
    
    public boolean isEnableIngressSrvRouting() {
        return (Boolean) getSettingTypedValue(PROXY_SIP_INGRESS_SRV_ROUTING_SETTING);
    }
    
    public int getPresenceSipTcpPort() {
        return (Integer) getSettingTypedValue(PRESENCE_SIP_TCP_PORT_SETTING);
    }

    public int getPresenceSipUdpPort() {
        return (Integer) getSettingTypedValue(PRESENCE_SIP_UDP_PORT_SETTING);
    }

    public int getPresenceSipTlsPort() {
        return (Integer) getSettingTypedValue(PRESENCE_SIP_TLS_PORT_SETTING);
    }
    
    public boolean isEnableBLFSipXPlugin() {
        return (Boolean) getSettingTypedValue(ENABLE_BLF_SIPX_PLUGIN_SETTING);
    }

    public int getBLFSipXPluginLogSetting() {
        return (Integer) getSettingTypedValue(BLF_SIPX_PLUGIN_LOG_SETTING);
    }
    
    public boolean isEnablePollBLAUser() {
        return (Boolean) getSettingTypedValue(ENABLE_POLL_BLA_USER_SETTING);
    }
    
    public boolean isEnableActiveDialogCheck() {
        return (Boolean) getSettingTypedValue(ENABLE_ACTIVE_DIALOG_CHECK);
    }
    
    public boolean isEnableActiveDialogCollate() {
        return (Boolean) getSettingTypedValue(ENABLE_ACTIVE_DIALOG_COLLATE);
    }
    
    public int getActiveDialogCheckPeriod() {
        return (Integer) getSettingTypedValue(ACTIVE_DIALOG_CHECK_PERIOD);
    }
    
    public int getActiveDialogCollatePeriod() {
        return (Integer) getSettingTypedValue(ACTIVE_DIALOG_COLLATE_PERIOD);
    }
    
    public boolean isEnableBLAMessageQueue() {
        return (Boolean) getSettingTypedValue(ENABLE_BLA_MESSAGE_QUEUE);
    }
    
    public int getBLAUserPollInterval() {
        return (Integer) getSettingTypedValue(BLA_USER_POLL_INTERVAL_SETTING);
    }

    @Override
    protected Setting loadSettings() {
        return getModelFilesContext().loadModelFile("kamailio/kamailio.xml");
    }

    @Override
    public Collection<Feature> getAffectedFeaturesOnChange() {
        return Arrays.asList((Feature) KamailioManager.FEATURE_PRESENCE
                , (Feature) KamailioManager.FEATURE_PROXY);
    }

    @Override
    public String getBeanId() {
        return "kamailioSettings";
    }
}
