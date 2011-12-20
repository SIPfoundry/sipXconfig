/*
 * Copyright (C) 2011 eZuce Inc., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the AGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.registrar;


import static java.lang.String.format;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.sipfoundry.sipxconfig.address.Address;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigManager;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigProvider;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigRequest;
import org.sipfoundry.sipxconfig.cfgmgt.KeyValueConfiguration;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.domain.Domain;
import org.sipfoundry.sipxconfig.im.ImManager;
import org.sipfoundry.sipxconfig.parkorbit.ParkOrbitContext;
import org.sipfoundry.sipxconfig.proxy.ProxyManager;
import org.sipfoundry.sipxconfig.setting.PatternSettingFilter;
import org.sipfoundry.sipxconfig.setting.Setting;
import org.sipfoundry.sipxconfig.setting.SettingFilter;
import org.sipfoundry.sipxconfig.setting.SettingUtil;
import org.springframework.beans.factory.annotation.Required;

public class RegistrarConfiguration implements ConfigProvider {
    private static final SettingFilter NO_UNDERSCORE = new PatternSettingFilter(".*/_.*");
    private Registrar m_registrar;

    @Override
    public void replicate(ConfigManager manager, ConfigRequest request) throws IOException {
        if (!request.applies(Registrar.FEATURE) || !manager.getFeatureManager().isFeatureEnabled(Registrar.FEATURE)) {
            return;
        }

        Collection<Location> locations = manager.getFeatureManager()
                .getLocationsForEnabledFeature(Registrar.FEATURE);
        if (locations.isEmpty()) {
            return;
        }

        RegistrarSettings settings = m_registrar.getSettings();
        Domain domain = manager.getDomainManager().getDomain();
        Address imApi = manager.getAddressManager().getSingleAddress(ImManager.XMLRPC_ADDRESS);
        Address presenceApi = manager.getAddressManager().getSingleAddress(Registrar.PRESENCE_MONITOR_ADDRESS);
        Address proxy = manager.getAddressManager().getSingleAddress(ProxyManager.TCP_ADDRESS);
        Address park = manager.getAddressManager().getSingleAddress(ParkOrbitContext.SIP_TCP_PORT);
        for (Location location : locations) {
            File dir = manager.getLocationDataDirectory(location);
            Writer w = new FileWriter(new File(dir, "registrar-config.cfdat"));
            try {
                write(w, settings, domain, location, proxy, imApi, presenceApi, park);
            } finally {
                IOUtils.closeQuietly(w);
            }
        }
    }

    void write(Writer wtr, RegistrarSettings settings, Domain domain, Location location, Address proxy,
            Address imApi, Address presenceApi, Address park) throws IOException {
        KeyValueConfiguration file = new KeyValueConfiguration(wtr);
        Setting root = settings.getSettings();
        file.write(SettingUtil.filter(NO_UNDERSCORE, root.getSetting("registrar-config")));
        file.write("SIP_REGISTRAR_AUTHENTICATE_REALM", domain.getSipRealm());
        file.write("SIP_REGISTRAR_DOMAIN_NAME", domain.getName());
        file.write("SIP_REGISTRAR_PROXY_PORT", proxy.getPort());
        file.write("SIP_REGISTRAR_NAME", location.getFqdn());
        file.write("SIP_REGISTRAR_SYNC_WITH", "obsolete");
        file.write("SIP_REGISTRAR_BIND_IP", location.getAddress());
        file.write(root.getSetting("userparam"));
        file.write(root.getSetting("call-pick-up"));

        String parkUri = format("%s;transport=tcp?Route=sip:%s:%d", domain.getName(), park.getAddress(),
                park.getPort());
        file.write("SIP_REDIRECT.100-PICKUP.PARK_SERVER", parkUri);

        file.write("SIP_REDIRECT.130-MAPPING.", root.getSetting("mapping"));
        file.write(root.getSetting("isn"));
        file.write(root.getSetting("enum"));

        if (imApi != null) {
            String openfireUrl = format("http://%s:%d/plugins/sipx-openfire-presence/status", imApi.getAddress(),
                    imApi.getPort());
            file.write("SIP_REDIRECT.900-PRESENCE.OPENFIRE_PRESENCE_SERVER_URL", openfireUrl);
            file.write("SIP_REDIRECT.900-PRESENCE.LOCAL_PRESENCE_MONITOR_SERVER_URL", presenceApi.toString());
            file.write("SIP_REDIRECT.900-PRESENCE.REALM", domain.getSipRealm());
            file.write("SIP_REDIRECT.900-PRESENCE.SIP_DOMAIN", domain.getName());
        }
        file.write("SIP_REDIRECT.999-AUTHROUTER.SIPX_PROXY", domain.getName() + ";transport=tcp");
    }

    @Required
    public void setRegistrar(Registrar registrar) {
        m_registrar = registrar;
    }
}
