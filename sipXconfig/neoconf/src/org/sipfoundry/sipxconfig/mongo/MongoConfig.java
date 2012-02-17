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
import java.io.Writer;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.sipfoundry.sipxconfig.cfgmgt.CfengineModuleConfiguration;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigManager;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigProvider;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigRequest;
import org.sipfoundry.sipxconfig.cfgmgt.KeyValueConfiguration;
import org.sipfoundry.sipxconfig.cfgmgt.PostConfigListener;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.commserver.LocationsManager;
import org.sipfoundry.sipxconfig.feature.FeatureManager;

public class MongoConfig implements ConfigProvider, PostConfigListener {
    private MongoManager m_mongoManager;
    private MongoReplicaSetManager m_mongoReplicaSetManager;

    @Override
    public void replicate(ConfigManager manager, ConfigRequest request) throws IOException {
        if (!request.applies(MongoManager.FEATURE_ID, LocationsManager.FEATURE, MongoManager.ARBITER_FEATURE)) {
            return;
        }
        FeatureManager fm = manager.getFeatureManager();
        Location[] all = manager.getLocationManager().getLocations();
        //TODO  - get firewall/encryption details from system
        boolean encrypt = false;
        List<Location> secondary = fm.getLocationsForEnabledFeature(MongoManager.FEATURE_ID);
        Location primary = manager.getLocationManager().getPrimaryLocation();
        MongoSettings settings = m_mongoManager.getSettings();
        int port = settings.getPort();
        String connStr = getConnectionString(primary, secondary, port, encrypt);
        String connUrl = getConnectionUrl(primary, secondary, port, encrypt);
        for (Location location : all) {

            // CLIENT
            File dir = manager.getLocationDataDirectory(location);
            FileWriter client = new FileWriter(new File(dir, "mongo-client.ini"));
            try {
                writeClientConfig(client, connStr, connUrl);
            } finally {
                IOUtils.closeQuietly(client);
            }

            // SERVERS
            boolean mongod = fm.isFeatureEnabled(MongoManager.FEATURE_ID, location) || location.isPrimary();
            boolean arbiter = fm.isFeatureEnabled(MongoManager.ARBITER_FEATURE, location);
            FileWriter server = new FileWriter(new File(dir, "mongodb.cfdat"));
            try {
                writeServerConfig(server, mongod, arbiter);
            } finally {
                IOUtils.closeQuietly(server);
            }
        }
    }


    @Override
    public void postReplicate(ConfigManager manager, ConfigRequest request) throws IOException {
        if (!request.applies(MongoManager.FEATURE_ID, LocationsManager.FEATURE, MongoManager.ARBITER_FEATURE)) {
            return;
        }

        // NOTE:  live updating of mongo settings.
        m_mongoReplicaSetManager.checkMembers();
    }

    void writeServerConfig(Writer w, boolean mongod, boolean arbiter) throws IOException {
        String bindToAll = "0.0.0.0";
        CfengineModuleConfiguration config = new CfengineModuleConfiguration(w);
        config.writeClass("mongod", mongod);
        config.write("mongoBindIp", bindToAll);
        config.write("mongoPort", MongoSettings.SERVER_PORT);
        config.writeClass("mongod_arbiter", arbiter);
        config.write("mongoArbiterBindIp", bindToAll);
        config.write("mongoArbiterPort", MongoSettings.ARBITER_PORT);
    }

    void writeClientConfig(Writer w, String connStr, String connUrl) throws IOException {
        KeyValueConfiguration config = KeyValueConfiguration.equalsSeparated(w);
        config.write("connectionUrl", connUrl);
        config.write("connectionString", connStr);
    }

    String getConnectionString(Location primary, List<Location> secondary, int port,
            boolean encrypt) {
        StringBuilder r = new StringBuilder("sipxecs/").append(primary.getFqdn()).append(':').append(port);
        if (secondary != null) {
            for (Location location : secondary) {
                r.append(',').append(location.getFqdn()).append(':').append(port);
            }
        }
        return r.toString();
    }

    String getConnectionUrl(Location primary, List<Location> secondary, int port, boolean encrypt) {
        StringBuilder r = new StringBuilder("mongodb://").append(primary.getFqdn());
        r.append(':').append(port);
        if (secondary != null) {
            for (Location location : secondary) {
                r.append(',').append(location.getFqdn()).append(':').append(port);
            }
        }
        r.append("/?slaveOk=true");
        return r.toString();
    }

    public void setMongoManager(MongoManager mongoManager) {
        m_mongoManager = mongoManager;
    }

    public void setMongoReplicaSetManager(MongoReplicaSetManager mongoReplicaSetManager) {
        m_mongoReplicaSetManager = mongoReplicaSetManager;
    }
}
