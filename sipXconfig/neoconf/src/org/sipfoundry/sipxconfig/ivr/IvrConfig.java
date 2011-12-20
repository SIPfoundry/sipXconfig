/*
 * Copyright (C) 2011 eZuce Inc., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the AGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.ivr;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.sipfoundry.sipxconfig.address.Address;
import org.sipfoundry.sipxconfig.admin.AdminContext;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigManager;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigProvider;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigRequest;
import org.sipfoundry.sipxconfig.cfgmgt.KeyValueConfiguration;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.dialplan.DialPlanContext;
import org.sipfoundry.sipxconfig.domain.Domain;
import org.sipfoundry.sipxconfig.feature.FeatureManager;
import org.sipfoundry.sipxconfig.freeswitch.FreeswitchFeature;
import org.sipfoundry.sipxconfig.im.ImManager;
import org.sipfoundry.sipxconfig.imbot.ImBot;
import org.sipfoundry.sipxconfig.mwi.Mwi;
import org.sipfoundry.sipxconfig.restserver.RestServer;
import org.springframework.beans.factory.annotation.Required;

public class IvrConfig implements ConfigProvider {
    private Ivr m_ivr;

    @Override
    public void replicate(ConfigManager manager, ConfigRequest request) throws IOException {
        if (!request.applies(DialPlanContext.FEATURE, Ivr.FEATURE)) {
            return;
        }

        FeatureManager featureManager = manager.getFeatureManager();
        Collection<Location> locations = featureManager.getLocationsForEnabledFeature(Ivr.FEATURE);
        if (locations.isEmpty()) {
            return;
        }
        Address mwiApi = manager.getAddressManager().getSingleAddress(Mwi.HTTP_API);
        Address adminApi = manager.getAddressManager().getSingleAddress(AdminContext.HTTPS_ADDRESS);
        Address restApi = manager.getAddressManager().getSingleAddress(RestServer.HTTPS_API);
        Address imApi = manager.getAddressManager().getSingleAddress(ImManager.XMLRPC_ADDRESS);
        Address imbotApi = manager.getAddressManager().getSingleAddress(ImBot.XML_RPC);
        Address fsEvent = manager.getAddressManager().getSingleAddress(FreeswitchFeature.EVENT_ADDRESS);
        IvrSettings settings = m_ivr.getSettings();
        Domain domain = manager.getDomainManager().getDomain();
        for (Location location : locations) {
            File f = new File(manager.getLocationDataDirectory(location), "sipxivr.properties.cfdat");
            Writer wtr = new FileWriter(f);
            write(wtr, settings, domain, location, mwiApi, restApi, adminApi, imApi, imbotApi, fsEvent);
            IOUtils.closeQuietly(wtr);
        }
    }

    void write(Writer wtr, IvrSettings settings, Domain domain, Location location, Address mwiApi, Address restApi,
            Address adminApi, Address imApi, Address imbotApi, Address fsEvent) throws IOException {
        KeyValueConfiguration config = new KeyValueConfiguration(wtr, "=");
        config.write(settings.getSettings());
        config.write("freeswitch.eventSocketPort", fsEvent.getPort());

        // potential bug: name "operator" could be changed by admin. this should be configurable
        // and linked with vm dialing rule
        config.write("ivr.operatorAddr", "sip:operator@" + domain.getName());

        config.write("ivr.sipxchangeDomainName", domain.getName());
        config.write("ivr.realm", domain.getSipRealm());
        config.write("ivr.httpsPort", settings.getHttpsPort());
        config.write("ivr.mwiUrl", mwiApi.toString());
        config.write("ivr.configUrl", adminApi.toString());
        config.write("ivr.3pccSecureUrl", restApi.toString());
        config.write("ivr.callHistoryUrl", restApi.toString() + "/cdr/");

        if (imApi != null) {
            config.write("ivr.openfireHost", imApi.getAddress());
            config.write("ivr.openfireXmlRpcPort", imApi.getPort());
        }

        if (imbotApi != null) {
            config.write("ivr.sendIMUrl", imbotApi.toString());
        }
    }

    @Required
    public void setIvr(Ivr ivr) {
        m_ivr = ivr;
    }
}
