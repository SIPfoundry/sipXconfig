/*
 * Copyright (C) 2011 eZuce Inc., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the AGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.mongo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.sipfoundry.sipxconfig.cfgmgt.CfengineModuleConfiguration;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigManager;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigProvider;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigRequest;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigUtils;
import org.sipfoundry.sipxconfig.cfgmgt.KeyValueConfiguration;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.feature.FeatureManager;

public class MongoConfig implements ConfigProvider {
    private MongoManager m_mongoManager;

    @Override
    public void replicate(ConfigManager manager, ConfigRequest request) throws IOException {
        if (!request.applies(MongoManager.FEATURE_ID)) {
            return;
        }
        FeatureManager fm = manager.getFeatureManager();
        Location[] all = manager.getLocationManager().getLocations();
        MongoSettings settings = m_mongoManager.getSettings();
        int port = settings.getPort();
        String conStr = getConnectionString(all, port);
        String conUrl = getConnectionUrl(all, port);
        String mongod = "mongod";
        for (Location location : all) {
            // every location gets a mongo client config
            File dir = manager.getLocationDataDirectory(location);
            File file = new File(dir, "mongo-client.ini.cfdat");
            FileWriter wtr = new FileWriter(file);
            KeyValueConfiguration config = KeyValueConfiguration.equalsSeparated(wtr);
            config.write("connectionUrl", conUrl);
            config.write("connectionString", conStr);
            IOUtils.closeQuietly(wtr);
            boolean server = fm.isFeatureEnabled(MongoManager.FEATURE_ID, location);
            ConfigUtils.enableCfengineClass(dir, "mongod.cfdat", mongod, server);
            // only mongod servers get mongod config
            if (server) {
                File filed = new File(dir, "mongod.conf.cfdat");
                FileWriter wtrd = new FileWriter(filed);
                CfengineModuleConfiguration configd = new CfengineModuleConfiguration(wtrd);
                configd.write(settings.getSettings().getSetting(mongod));
                // TODO this should be 127.0.0.1 only
                configd.write("bind_ip", "0.0.0.0");
                IOUtils.closeQuietly(wtrd);
            }
        }
    }

    String getConnectionString(Location[] locations, int port) {
        if (locations.length == 1) {
            return "localhost:" + port;
        }
        StringBuilder r = new StringBuilder(locations[0].getAddress());
        for (int i = 1; i < locations.length; i++) {
            r.append(',').append(locations[i].getAddress()).append(':').append(port);
        }
        return r.toString();
    }

    String getConnectionUrl(Location[] locations, int port) {
        if (locations.length == 1) {
            return "mongodb://localhost:" + port + "/?slaveOk=true";
        }
        StringBuilder r = new StringBuilder("mongodb://").append(locations[0].getAddress());
        r.append(':').append(port);
        for (int i = 1; i < locations.length; i++) {
            r.append(',').append(locations[i].getAddress()).append(':').append(port);
        }
        r.append("/?slaveOk=true");
        return r.toString();
    }

    public void setMongoManager(MongoManager mongoManager) {
        m_mongoManager = mongoManager;
    }
}
