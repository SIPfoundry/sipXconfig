/*
 * Copyright (C) 2011 eZuce Inc., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the AGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.nattraversal;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.sipfoundry.sipxconfig.address.Address;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigManager;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigProvider;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigRequest;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigUtils;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.commserver.LocationsManager;
import org.sipfoundry.sipxconfig.proxy.ProxyManager;
import org.sipfoundry.sipxconfig.sbc.SbcManager;
import org.sipfoundry.sipxconfig.sbc.SbcRoutes;

public class NatConfiguration implements ConfigProvider {
    private VelocityEngine m_velocityEngine;
    private NatTraversal m_nat;
    private SbcManager m_sbcManager;

    @Override
    public void replicate(ConfigManager manager, ConfigRequest request) throws IOException {
        if (!request.applies(NatTraversal.FEATURE, ProxyManager.FEATURE, LocationsManager.FEATURE)) {
            return;
        }

        boolean relayEnabled = manager.getFeatureManager().isFeatureEnabled(NatTraversal.FEATURE);
        Set<Location> locations = request.locations(manager);
        NatSettings settings = m_nat.getSettings();
        Address proxyTcp = manager.getAddressManager().getSingleAddress(ProxyManager.TCP_ADDRESS);
        Address proxyTls = manager.getAddressManager().getSingleAddress(ProxyManager.TLS_ADDRESS);
        SbcRoutes routes = m_sbcManager.getRoutes();
        for (Location location : locations) {
            File dir = manager.getLocationDataDirectory(location);
            boolean proxyEnabled = manager.getFeatureManager().isFeatureEnabled(ProxyManager.FEATURE, location);
            boolean configEnabled = (relayEnabled || proxyEnabled);
            if (configEnabled) {
                Writer writer = new FileWriter(new File(dir, "nattraversalrules.xml"));
                try {
                    write(writer, settings, location, routes, proxyTcp.getPort(), proxyTls.getPort());
                } finally {
                    IOUtils.closeQuietly(writer);
                }
            }

            boolean serviceEnabled = (relayEnabled && proxyEnabled);
            ConfigUtils.enableCfengineClass(dir, "sipxrelay.cfdat", serviceEnabled, "sipxrelay");
        }
    }

    void write(Writer writer, NatSettings settings, Location location, SbcRoutes routes, int proxyTcpPort,
            int proxyTlsPort) throws IOException {
        VelocityContext context = new VelocityContext();
        context.put("settings", settings);
        context.put("location", location);
        context.put("xmlRpcPort", settings.getXmlRpcPort());
        context.put("proxyTcpPort", proxyTcpPort);
        context.put("proxyTlsPort", proxyTlsPort);
        context.put("routes", routes);
        try {
            m_velocityEngine.mergeTemplate("nattraversal/nattraversalrules.vm", context, writer);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public void setVelocityEngine(VelocityEngine velocityEngine) {
        m_velocityEngine = velocityEngine;
    }

    public void setNat(NatTraversal nat) {
        m_nat = nat;
    }

    public void setSbcManager(SbcManager sbcManager) {
        m_sbcManager = sbcManager;
    }
}
