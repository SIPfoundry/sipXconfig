package org.sipfoundry.sipxconfig.oss;

import java.util.Arrays;
import java.util.Collection;

import org.sipfoundry.sipxconfig.cfgmgt.DeployConfigOnEdit;
import org.sipfoundry.sipxconfig.feature.Feature;
import org.sipfoundry.sipxconfig.kamailio.KamailioManager;
import org.sipfoundry.sipxconfig.setting.PersistableSettings;
import org.sipfoundry.sipxconfig.setting.Setting;

public class OSSCoreSettings extends PersistableSettings implements DeployConfigOnEdit {
   
    public static final String ROOT_SETTING = "oss-configuration";
    public static final String LOG_SETTING = ROOT_SETTING + "/LOG_LEVEL";
    public static final String OSS_SIP_TCP_PUBLIC_PORT_SETTING = ROOT_SETTING + "/PUBLIC_TCP_PORT";
    public static final String OSS_SIP_UDP_PUBLIC_PORT_SETTING = ROOT_SETTING + "/PUBLIC_UDP_PORT";
    public static final String OSS_SIP_TLS_PUBLIC_PORT_SETTING = ROOT_SETTING + "/PUBLIC_TLS_PORT";
    public static final String OSS_SIP_WS_PUBLIC_PORT_SETTING  = ROOT_SETTING + "/PUBLIC_WS_PORT";
    public static final String OSS_SIP_RTP_LOW_PORT_SETTING  = ROOT_SETTING + "/PUBLIC_RTP_PORT_LOW";
    public static final String OSS_SIP_RTP_HIGH_PORT_SETTING  = ROOT_SETTING + "/PUBLIC_RTP_PORT_HIGH";
    
    public static final String OSS_SIP_INTERNAL_PORT_SETTING = ROOT_SETTING + "/INTERNAL_PORT";
    public static final String OSS_SIP_INTERNAL_TRANSPORT_SETTING = ROOT_SETTING + "/INTERNAL_TRANSPORT";
    
    public static final String OSS_SIP_TCP_ADVERTISE_IP_SETTING = ROOT_SETTING + "/ADVERTISE_IP";

    public int getLogSetting() {
        return (Integer) getSettingTypedValue(LOG_SETTING);
    }
    
    public int getPublicSipTcpPort() {
        return (Integer) getSettingTypedValue(OSS_SIP_TCP_PUBLIC_PORT_SETTING);
    }

    public int getPublicSipUdpPort() {
        return (Integer) getSettingTypedValue(OSS_SIP_UDP_PUBLIC_PORT_SETTING);
    }

    public int getPublicSipTlsPort() {
        return (Integer) getSettingTypedValue(OSS_SIP_TLS_PUBLIC_PORT_SETTING);
    }
    
    public int getPublicSipWSPort() {
        return (Integer) getSettingTypedValue(OSS_SIP_WS_PUBLIC_PORT_SETTING);
    }
    
    public int getInternalSipPort() {
        return (Integer) getSettingTypedValue(OSS_SIP_INTERNAL_PORT_SETTING);
    }
    
    public String getInternalSipTransport() {
        return getSettingValue(OSS_SIP_INTERNAL_TRANSPORT_SETTING);
    }
    
    public String getAdvertiseIp() {
        return getSettingValue(OSS_SIP_TCP_ADVERTISE_IP_SETTING);
    }

    public int getRtpLowestPort() {
        return (Integer) getSettingTypedValue(OSS_SIP_RTP_LOW_PORT_SETTING);
    }

    public int getRtpHighestPort() {
        return (Integer) getSettingTypedValue(OSS_SIP_RTP_HIGH_PORT_SETTING);
    }
    
    @Override
    protected Setting loadSettings() {
        return getModelFilesContext().loadModelFile("oss/osscore.xml");
    }

    @Override
    public Collection<Feature> getAffectedFeaturesOnChange() {
        return Arrays.asList((Feature) KamailioManager.FEATURE_PROXY);
    }

    @Override
    public String getBeanId() {
        return "osscoreSettings";
    }
}

