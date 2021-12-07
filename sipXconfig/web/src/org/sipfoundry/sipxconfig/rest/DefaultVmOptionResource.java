/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.rest;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.sipfoundry.sipxconfig.common.User;

public class DefaultVmOptionResource extends UserResource {
    private String m_flag;
    private String m_queryUser;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);

        setModifiable(true);
        setReadable(false);

        m_queryUser = (String) getRequest().getAttributes().get("user");
        m_flag = (String) getRequest().getAttributes().get("flag");
    }

    public User getUserToQuery() throws ResourceException {
        User user = getUser();
        if(user.isAdmin()) {
           // Allow query by user only if logged user is an admina
           User queryUser = getCoreContext().loadUserByUserName(m_queryUser);
           return queryUser; 
        }
        
        throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, "Must be an admin user.");
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    @Override
    public void storeRepresentation(Representation entity) throws ResourceException {
        User user = getUserToQuery();
        user.setPlayVmDefaultOptions(Boolean.valueOf(m_flag));
        getCoreContext().saveUser(user);
    }
}
