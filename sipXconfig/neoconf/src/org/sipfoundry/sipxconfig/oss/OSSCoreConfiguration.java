package org.sipfoundry.sipxconfig.oss;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.sipfoundry.sipxconfig.cfgmgt.CfengineModuleConfiguration;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigManager;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigProvider;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigRequest;
import org.sipfoundry.sipxconfig.cfgmgt.KeyValueConfiguration;
import org.sipfoundry.sipxconfig.commserver.Location;

public class OSSCoreConfiguration implements ConfigProvider {
    private OSSCoreManager m_ossCoreManager;
    
    @Override
    public void replicate(ConfigManager manager, ConfigRequest request) throws IOException {
        if (!request.applies(OSSCoreManager.FEATURE)) {
            return;
        }
        
        OSSCoreSettings settings = m_ossCoreManager.getSettings();
        
        Set<Location> locations = request.locations(manager);
        for (Location location : locations) {
            File dir = manager.getLocationDataDirectory(location);
            boolean sbcEnabled = manager.getFeatureManager().isFeatureEnabled(OSSCoreManager.FEATURE, location);
            
            Writer sbcCf = new FileWriter(new File(dir, "sbc.cfdat"));
            try {
                writeCfConfig(sbcCf, settings, location, sbcEnabled);
            } finally {
                IOUtils.closeQuietly(sbcCf);
            }
            
            Writer kamailioDefault = new FileWriter(new File(dir, "sbc_config.cfg.part"));
            try {
                writeSbcConfig(kamailioDefault, settings, location);
            } finally {
                IOUtils.closeQuietly(kamailioDefault);
            }
        }
    }

    private void writeCfConfig(Writer wtr, OSSCoreSettings settings, Location location, boolean sbc) 
            throws IOException {
        CfengineModuleConfiguration config = new CfengineModuleConfiguration(wtr);
        config.writeClass(OSSCoreManager.FEATURE.getId(), sbc);
                
        config.write("sbcLogLevel", settings.getLogSetting());
        config.write("sbcAdvertiseIp", settings.getAdvertiseIp());
        config.write("sbcInternalIp", location.getAddress());
        config.write("sbcInternalPort", settings.getInternalSipPort());
        config.write("sbcInternalTransPort", settings.getInternalSipTransport());
        config.write("sbcPublicTcpPort", settings.getPublicSipTcpPort());
        config.write("sbcPublicUdpPort", settings.getPublicSipUdpPort());
        config.write("sbcPublicTlsPort", settings.getPublicSipTlsPort());
    }
    
    private void writeSbcConfig(Writer wtr, OSSCoreSettings settings, Location location) 
            throws IOException {
        KeyValueConfiguration config = KeyValueConfiguration.equalsSeparated(wtr);
        
        config.write("log-file", "$(sipx.SIPX_LOGDIR)/sbc.log");
        config.write("log-level", settings.getLogSetting());
        
        // Public Address
        config.write("interface-address", location.getAddress());
        config.write("port", settings.getPublicSipTcpPort());
        config.write("tls-port", settings.getPublicSipTlsPort());
        config.write("ws-port", settings.getPublicSipWSPort());
        
        // Target Proxy
        config.write("target-address", "$(sipx.kamailioProxyBindIp):$(sipx.kamailioProxyBindPort)"); // Must be on the same network
        config.write("target-interface", location.getAddress());
        config.write("target-interface-port", settings.getInternalSipPort());
        config.write("target-transport", settings.getInternalSipTransport());
        config.write("target-domain", "$(sipx.domain)");

        config.write("rtp-port-low", settings.getRtpLowestPort());
        config.write("rtp-port-high", settings.getRtpHighestPort());
        
        if(!StringUtils.isEmpty(settings.getAdvertiseIp())) {
            config.write("external-address", settings.getAdvertiseIp());
        } else {
            config.write("guess-external-address", 1);
        }
        
        config.write("tls-cert", "$(sipx.SIPX_CONFDIR)/ssl/ssl.crt");
        config.write("tls-private-key", "$(sipx.SIPX_CONFDIR)/ssl/ssl.key");
        config.write("tls-peer-ca-directory", "$(sipx.SIPX_CONFDIR)/ssl/authorities");
        
        if(settings.isVerifyTlsPeerCerticate()) {
            config.write("tls-verify-peer", settings.isVerifyTlsPeerCerticate());
        }
    }

    public OSSCoreManager getManager() {
        return m_ossCoreManager;
    }

    public void setManager(OSSCoreManager ossCoreManager) {
        this.m_ossCoreManager = ossCoreManager;
    }
    
}