package org.sipfoundry.sipxconfig.kamailio;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.sipfoundry.sipxconfig.cfgmgt.CfengineModuleConfiguration;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigManager;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigProvider;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigRequest;
import org.sipfoundry.sipxconfig.cfgmgt.KeyValueConfiguration;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.mongo.MongoConfig;
import org.sipfoundry.sipxconfig.mongo.MongoManager;
import org.sipfoundry.sipxconfig.mysql.MySql;
import org.sipfoundry.sipxconfig.redis.Redis;

public class KamailioConfiguration implements ConfigProvider {
	
    private static String KAMAILIO_PROXY_DB = "kamailio-proxy";
    private static String KAMAILIO_PRESENCE_DB = "kamailio-presence";
    private static String KAMAILIO_SIPXUSER_DB = "imdb";

    private KamailioManager m_kamailioManager;
    private MongoConfig m_mongoConfig; 
    
    @Override
    public void replicate(ConfigManager manager, ConfigRequest request) throws IOException {
        if (!request.applies(KamailioManager.FEATURE_PROXY) && 
            !request.applies(KamailioManager.FEATURE_PRESENCE) &&
            !request.applies(MongoManager.FEATURE_ID)) {
            return;
        }
        
        List<Location> presenceLocations = manager.getFeatureManager().getLocationsForEnabledFeature(KamailioManager.FEATURE_PRESENCE);
        if (presenceLocations.isEmpty()) {
        	return;
        }
        
        KamailioSettings settings = m_kamailioManager.getSettings();
        Set<Location> locations = request.locations(manager);
        Location presenceLocation = presenceLocations.get(0);
        for (Location location : locations) {
            File dir = manager.getLocationDataDirectory(location);
            boolean proxyEnabled = manager.getFeatureManager().isFeatureEnabled(KamailioManager.FEATURE_PROXY, location);
            boolean presenceEnabled = manager.getFeatureManager().isFeatureEnabled(KamailioManager.FEATURE_PRESENCE, location);
            
            Writer kamailioCf = new FileWriter(new File(dir, "kamailio.cfdat"));
            try {
                writeCfConfig(kamailioCf, settings, location, presenceLocation, proxyEnabled, presenceEnabled);
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
    
    private void writeCfConfig(Writer wtr, KamailioSettings settings, Location location, Location presenceLocation, boolean proxy, boolean presence)
            throws IOException {
        CfengineModuleConfiguration config = new CfengineModuleConfiguration(wtr);
        config.writeClass(KamailioManager.FEATURE_PROXY.getId(), proxy);
        config.writeClass(KamailioManager.FEATURE_PRESENCE.getId(), presence);
        
        String proxyConnectionUrl = m_mongoConfig.generateConnectionUrl(KAMAILIO_PROXY_DB, MongoConfig.GLOBAL_REPLSET, location);
        String presenceConnectionUrl = m_mongoConfig.generateConnectionUrl(KAMAILIO_PRESENCE_DB, MongoConfig.GLOBAL_REPLSET, location);
        String userConnectionUrl = m_mongoConfig.generateConnectionUrl(KAMAILIO_SIPXUSER_DB, MongoConfig.GLOBAL_REPLSET, location);
        
        
        config.write("kamailioForkChildren", settings.getDefaultChildren());
        config.write("kamailioTcpWriteQueueSize", settings.getTcpWriteQueueSize());
        config.write("kamailioTcpReadBufferSize", settings.getTcpReadBufferSize());
        config.write("kamailioTcpConnectionLifetime", settings.getTcpConnectionLifetime());
        
        config.write("kamailioPresenceBindIp", presenceLocation.getAddress());
        config.write("kamailioPresenceBindPort", settings.getPresenceSipTcpPort());
        config.write("kamailioPresenceBlaPollInterval", settings.getBLAUserPollInterval());
        config.write("kamailioPresenceEnableSipXPlugin", settings.isEnableBLFSipXPlugin() ? 1 : 0);
        config.write("kamailioPresenceEnablePollBlaUser", settings.isEnablePollBLAUser() ? 1 : 0);
        config.write("kamailioPresenceSipXPluginLogLevel", settings.getBLFSipXPluginLogSetting());
        
        config.write("kamailioPresenceEnableBlaMessageQueue", settings.isEnableBLAMessageQueue() ? 1 : 0);
        config.write("kamailioPresenceBLARedisAddress", presenceLocation.getAddress());
        config.write("kamailioPresenceBLARedisPort", Redis.SERVER.getCanonicalPort());
        
        config.write("kamailioProxyBindIp", location.getAddress());
        config.write("kamailioProxyBindPort", settings.getProxySipTcpPort());
        
        config.write("kamailioPresenceDB", presenceConnectionUrl);
        config.write("kamailioProxyDB", proxyConnectionUrl);
        config.write("kamailioSIPXUserDB", userConnectionUrl);

        config.writeClass(MySql.FEATURE.getId(), proxy || presence);
        config.writeClass(Redis.FEATURE.getId(), proxy || presence);
    }
    
    private void writeKamailioDefault(Writer wtr, KamailioSettings settings)
            throws IOException {
        KeyValueConfiguration config = KeyValueConfiguration.equalsSeparated(wtr);
        
        config.write("RUN_KAMAILIO", "yes");
        config.write("USER", "$(sipx.SIPXPBXUSER)");
        config.write("GROUP", "$(sipx.SIPXPBXGROUP)");
        
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
        
        //Configure core dump
        boolean enableDumpCore = settings.isEnableDumpCore();
        config.write("disable_core_dump", enableDumpCore ? "no" : "yes");
        
        //Configure Kamailio ports
        int port = settings.getProxySipTcpPort();
        config.write("listen", "udp:" + location.getAddress() + ':' + port);
        config.write("listen", "tcp:" + location.getAddress() + ':' + port);
        config.write("listen", "tls:" + location.getAddress() + ':' + settings.getProxySipTlsPort());
    }
    
    private void writeKamailioPresenceGlobal(Writer wtr, KamailioSettings settings, Location location)
            throws IOException {
        KeyValueConfiguration config = KeyValueConfiguration.equalsSeparated(wtr);
        
        //Configure Kamailio logging
        config.write("debug", settings.getLogSetting());
        config.write("log_stderror", "no");
        
        //Configure core dump
        boolean enableDumpCore = settings.isEnableDumpCore();
        config.write("disable_core_dump", enableDumpCore ? "no" : "yes");
        
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

    public MongoConfig getMongoConfig() {
        return m_mongoConfig;
	}

    public void setMongoConfig(MongoConfig mongoConfig) {
        this.m_mongoConfig = mongoConfig;
    }
    
}
