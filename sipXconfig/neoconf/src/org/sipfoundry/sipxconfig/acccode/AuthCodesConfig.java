/*
 *
 *
 * Copyright (C) 2010 Avaya, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipfoundry.sipxconfig.acccode;

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
import org.sipfoundry.sipxconfig.cfgmgt.ConfigUtils;
import org.sipfoundry.sipxconfig.cfgmgt.KeyValueConfiguration;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.domain.Domain;
import org.sipfoundry.sipxconfig.freeswitch.FreeswitchFeature;
import org.springframework.beans.factory.annotation.Required;

public class AuthCodesConfig implements ConfigProvider {
    private AuthCodesImpl m_authCodes;

    @Override
    public void replicate(ConfigManager manager, ConfigRequest request) throws IOException {
        if (!request.applies(AuthCodes.FEATURE)) {
            return;
        }
        if (!m_authCodes.isEnabled()) {
            return;
        }

        Collection<Location> locations = manager.getFeatureManager().
            getLocationsForEnabledFeature(AuthCodes.FEATURE);
        Address fs = manager.getAddressManager().getSingleAddress(FreeswitchFeature.ACC_EVENT_ADDRESS);
        Domain domain = manager.getDomainManager().getDomain();
        for (Location location : locations) {
            AuthCodeSettings settings = m_authCodes.getSettings();
            File dir = manager.getLocationDataDirectory(location);
            ConfigUtils.enableCfengineClass(dir, "sipxacccode.cfdat", "sipxacccode", true);
            Writer flat = new FileWriter(new File(dir, "sipxacccode.properties.part"));
            try {
                writeConfig(flat, settings, domain, fs.getPort());
            } finally {
                IOUtils.closeQuietly(flat);
            }
        }
    }

    void writeConfig(Writer wtr, AuthCodeSettings settings, Domain domain, int freeswithPort) throws IOException {
        KeyValueConfiguration config = KeyValueConfiguration.equalsSeparated(wtr);
        config.write(settings.getSettings().getSetting("acccode-config"));
        config.write("freeswitch.eventSocketPort", freeswithPort);
    }

    @Required
    public void setAuthCodes(AuthCodesImpl authCodes) {
        m_authCodes = authCodes;
    }
}
