/*
 * Copyright (C) 2012 eZuce Inc., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the AGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.snmp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigManager;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigProvider;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigRequest;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.feature.FeatureListener;
import org.sipfoundry.sipxconfig.feature.FeatureManager;
import org.sipfoundry.sipxconfig.feature.GlobalFeature;
import org.sipfoundry.sipxconfig.feature.LocationFeature;

public class SnmpConfig implements ConfigProvider, FeatureListener {
    private SnmpManager m_snmp;
    private ConfigManager m_configManager;

    @Override
    public void replicate(ConfigManager manager, ConfigRequest request) throws IOException {
        if (!request.applies(SnmpManager.FEATURE)) {
            return;
        }
        Set<Location> locations = request.locations(manager);
        for (Location location : locations) {
            File dir = manager.getLocationDataDirectory(location);
            List<ProcessDefinition> defs = m_snmp.getProcessDefinitions(location);
            Writer wtr = new FileWriter(new File(dir, "snmpd.conf.part"));
            try {
                writeProcesses(wtr, defs);
            } finally {
                IOUtils.closeQuietly(wtr);
            }
        }
    }

    void writeProcesses(Writer w, List<ProcessDefinition> defs) throws IOException {
        for (ProcessDefinition def : defs) {
            w.write("proc ");
            w.write(def.getProcess());
            w.write("\n");
        }
    }

    public void setSnmpManager(SnmpManager snmp) {
        m_snmp = snmp;
    }

    @Override
    public void enableLocationFeature(FeatureManager manager, FeatureEvent event, LocationFeature feature,
            Location location) {
        // cannot tell what processes will become alive/dead
        m_configManager.configureEverywhere(SnmpManager.FEATURE);
    }

    @Override
    public void enableGlobalFeature(FeatureManager manager, FeatureEvent event, GlobalFeature feature) {
        // cannot tell what processes will become alive/dead
        m_configManager.configureEverywhere(SnmpManager.FEATURE);
    }

    public void setConfigManager(ConfigManager configManager) {
        m_configManager = configManager;
    }
}
