package org.sipfoundry.sipxconfig.kamailio;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.sipfoundry.sipxconfig.cfgmgt.CfengineModuleConfiguration;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigManager;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigProvider;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigRequest;
import org.sipfoundry.sipxconfig.cfgmgt.KeyValueConfiguration;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.mysql.MySql;

public class KamailioConfiguration implements ConfigProvider {

    private KamailioManager m_kamailioManager;
    
    @Override
    public void replicate(ConfigManager manager, ConfigRequest request) throws IOException {
        if (!request.applies(KamailioManager.FEATURE_PROXY) && 
            !request.applies(KamailioManager.FEATURE_PRESENCE)) {
            return;
        }
        
        KamailioSettings settings = m_kamailioManager.getSettings();
        
        Set<Location> locations = request.locations(manager);
        for (Location location : locations) {
            File dir = manager.getLocationDataDirectory(location);
            boolean proxyEnabled = manager.getFeatureManager().isFeatureEnabled(KamailioManager.FEATURE_PROXY, location);
            boolean presenceEnabled = manager.getFeatureManager().isFeatureEnabled(KamailioManager.FEATURE_PRESENCE, location);
            
            Writer kamailioCf = new FileWriter(new File(dir, "kamailio.cfdat"));
            try {
                writeCfConfig(kamailioCf, settings, location, proxyEnabled, presenceEnabled);
            } finally {
                IOUtils.closeQuietly(kamailioCf);
            }
            
            Writer kamailioDefault = new FileWriter(new File(dir, "kamailio.default.part"));
            try {
                writeKamailioDefault(kamailioDefault, settings);
            } finally {
                IOUtils.closeQuietly(kamailioDefault);
            }
            
            Writer kamailioCfgProxyGlobal = new FileWriter(new File(dir, "kamailio-proxy.cfg.global.part"));
            try {
                writeKamailioProxyGlobal(kamailioCfgProxyGlobal, settings, location);
            } finally {
                IOUtils.closeQuietly(kamailioCfgProxyGlobal);
            }
            
            Writer kamailioCfgPresenceGlobal = new FileWriter(new File(dir, "kamailio-presence.cfg.global.part"));
            try {
                writeKamailioPresenceGlobal(kamailioCfgPresenceGlobal, settings, location);
            } finally {
                IOUtils.closeQuietly(kamailioCfgPresenceGlobal);
            }
        }
        
    }
    
    private void writeCfConfig(Writer wtr, KamailioSettings settings, Location location, boolean proxy, boolean presence)
            throws IOException {
        CfengineModuleConfiguration config = new CfengineModuleConfiguration(wtr);
        config.writeClass(KamailioManager.FEATURE_PROXY.getId(), proxy);
        config.writeClass(KamailioManager.FEATURE_PRESENCE.getId(), presence);
        
        if(presence) {
            config.write("kamailioPresenceBindIp", location.getAddress());
            config.write("kamailioPresenceBindPort", settings.getPresenceSipTcpPort());
            config.write("kamailioPresenceBlaPollInterval", settings.getBLAUserPollInterval());
            config.write("kamailioPresenceEnableSipXPlugin", settings.isEnableBLFSipXPlugin() ? 1 : 0);
            config.write("kamailioPresenceEnablePollBlaUser", settings.isEnablePollBLAUser() ? 1 : 0);
            config.write("kamailioPresenceSipXPluginLogLevel", settings.getBLFSipXPluginLogSetting());
        } else {
            config.write("kamailioPresenceBindIp", "");
            config.write("kamailioPresenceBindPort", "");
            config.write("kamailioPresenceBlaPollInterval", 60);
            config.write("kamailioPresenceEnableSipXPlugin", 0);
            config.write("kamailioPresenceEnablePollBlaUser", 0);
            config.write("kamailioPresenceSipXPluginLogLevel", 0);
        }

        config.writeClass(MySql.FEATURE.getId(), proxy || presence);
    }
    
    private void writeKamailioDefault(Writer wtr, KamailioSettings settings)
            throws IOException {
        KeyValueConfiguration config = KeyValueConfiguration.equalsSeparated(wtr);
        
        config.write("RUN_KAMAILIO", "yes");
        config.write("USER", "kamailio");
        config.write("GROUP", "kamailio");
        
        config.write("SHM_MEMORY", settings.getDefaultSharedMemory());
        config.write("PKG_MEMORY", settings.getDefaultPrivateMemory());
        config.write("DUMP_CORE", settings.getDumpCore());
    }
    
    private void writeKamailioProxyGlobal(Writer wtr, KamailioSettings settings, Location location)
            throws IOException {
        KeyValueConfiguration config = KeyValueConfiguration.equalsSeparated(wtr);
        
        //Configure Kamailio logging
        config.write("debug", settings.getLogSetting());
        config.write("log_stderror", "no");
        
        //Configure Kamailio ports
        int port = settings.getProxySipTcpPort();
        config.write("listen", "udp:" + location.getAddress() + ':' + port);
        config.write("listen", "tcp:" + location.getAddress() + ':' + port);
    }
    
    private void writeKamailioPresenceGlobal(Writer wtr, KamailioSettings settings, Location location)
            throws IOException {
        KeyValueConfiguration config = KeyValueConfiguration.equalsSeparated(wtr);
        
        //Configure Kamailio logging
        config.write("debug", settings.getLogSetting());
        config.write("log_stderror", "no");
        
        //Configure Kamailio ports
        int port = settings.getPresenceSipTcpPort();
        config.write("listen", "udp:" + location.getAddress() + ':' + port);
        config.write("listen", "tcp:" + location.getAddress() + ':' + port);
    }

    public KamailioManager getKamailioManager() {
        return m_kamailioManager;
    }

    public void setKamailioManager(KamailioManager kamailioManager) {
        this.m_kamailioManager = kamailioManager;
    }

    
}
