/*
 *
 *
 * Copyright (C) 2008 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */
package org.sipfoundry.sipxconfig.site.service;

import static org.sipfoundry.sipxconfig.components.LocalizationUtils.localizeString;

import org.apache.tapestry.annotations.InjectObject;
import org.sipfoundry.sipxconfig.acd.AcdContext;
import org.sipfoundry.sipxconfig.acd.AcdServer;
import org.sipfoundry.sipxconfig.commserver.SipxReplicationContext;

public abstract class EditPresenceService extends EditSipxService {
    @SuppressWarnings("hiding")
    public static final String PAGE = "service/EditPresenceService";

    @InjectObject(value = "spring:sipxReplicationContext")
    public abstract SipxReplicationContext getSipxReplicationContext();

    @InjectObject(value = "spring:acdContext")
    public abstract AcdContext getAcdContext();

    @Override
    public String getMyBeanId() {
        return SipxPresenceService.BEAN_ID;
    }

    @Override
    public void apply() {
        super.apply();
        for (AcdServer acdServer : getAcdContext().getServers()) {
            getSipxReplicationContext().generate(acdServer);
        }
    }

    public String getHelpText() {
        String helpText = "";
        if (isMultipleAcdsPresent()) {
            helpText += localizeString(getMessages(), "&multiple.servers.help");
            for (Object acdServer : getAcdContext().getServers()) {
                String locationId = String.valueOf(((AcdServer) acdServer).getLocation().getId());
                String locationFqdn = ((AcdServer) acdServer).getLocation().getFqdn();
                helpText += "<em>" + locationFqdn + "</em>" + ": " + "<b>" + locationId
                    + "</b>" + "<br>";
            }
        }
        return helpText;
    }

    public boolean isMultipleAcdsPresent() {
        return 1 < getAcdContext().getServers().size();
    }
}
