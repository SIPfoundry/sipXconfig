/*
 * Copyright (C) 2011 eZuce Inc., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the AGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.mwi;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.sipfoundry.sipxconfig.address.Address;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigManager;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigProvider;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigRequest;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigUtils;
import org.sipfoundry.sipxconfig.cfgmgt.KeyValueConfiguration;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.domain.Domain;
import org.sipfoundry.sipxconfig.ivr.Ivr;
import org.springframework.beans.factory.annotation.Required;

public class MwiConfig implements ConfigProvider {
    private Mwi m_mwi;
    private VelocityEngine m_velocityEngine;

    @Override
    public void replicate(ConfigManager manager, ConfigRequest request) throws IOException {
        if (!request.applies(Mwi.FEATURE, Ivr.FEATURE)) {
            return;
        }

        Set<Location> locations = request.locations(manager);
        Address ivrApi = manager.getAddressManager().getSingleAddress(Ivr.REST_API);
        Domain domain = manager.getDomainManager().getDomain();
        List<Location> allLocations = manager.getLocationManager().getLocationsList();
        MwiSettings settings = m_mwi.getSettings();
        for (Location location : locations) {
            File dir = manager.getLocationDataDirectory(location);
            boolean enabled = manager.getFeatureManager().isFeatureEnabled(Mwi.FEATURE, location);
            ConfigUtils.enableCfengineClass(dir, "sipxpublisher.cfdat", "sipxpublisher", enabled);
            if (!enabled) {
                continue;
            }

            Writer wtr = new FileWriter(new File(dir, "status-config.part"));
            try {
                write(wtr, settings, location, allLocations, domain);
            } finally {
                IOUtils.closeQuietly(wtr);
            }

            Writer plugin = new FileWriter(new File(dir, "status-plugin.xml"));
            try {
                writePlugin(plugin, ivrApi);
            } finally {
                IOUtils.closeQuietly(plugin);
            }
        }
    }

    void write(Writer wtr, MwiSettings settings, Location location, List<Location> allLocations, Domain domain)
        throws IOException {
        KeyValueConfiguration config = KeyValueConfiguration.colonSeparated(wtr);
        config.write(settings.getSettings().getSetting("status-config"));
        config.write("SIP_STATUS_AUTHENTICATE_REALM", domain.getSipRealm());
        config.write("SIP_STATUS_DOMAIN_NAME", domain.getName());

        StringBuilder validIps = new StringBuilder(allLocations.get(0).getAddress());
        for (int i = 1; i < allLocations.size(); i++) {
            validIps.append(",").append(allLocations.get(i).getAddress());
        }
        config.write("SIP_STATUS_HTTP_VALID_IPS", validIps.toString());
    }

    void writePlugin(Writer wtr, Address ivrApi) throws IOException {
        VelocityContext context = new VelocityContext();
        context.put("mwiUrl", ivrApi.toString() + "/mwi");
        try {
            m_velocityEngine.mergeTemplate("sipxstatus/status-plugin.vm", context, wtr);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Required
    public void setMwi(Mwi mwi) {
        m_mwi = mwi;
    }

    public void setVelocityEngine(VelocityEngine velocityEngine) {
        m_velocityEngine = velocityEngine;
    }
}
