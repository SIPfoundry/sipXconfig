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
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.sipfoundry.sipxconfig.common.User;

public class PersonalAttendantOperatorResource extends UserPersonalAttendantOperatorResource {
    private String m_operator;
    private String m_queryUser;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);

        setModifiable(true);
        setReadable(false);

        m_queryUser = (String) getRequest().getAttributes().get("user");
        m_operator = (String) getRequest().getAttributes().get("operator");
    }

    @Override
    public User getUserToQuery() {
        User user = getUser();
        if(user.isAdmin()) {
           // Allow query by user only if logged user is an admin
           User queryUser = getCoreContext().loadUserByUserName(m_queryUser);
           return queryUser; 
        }
        
        return user;
    }
}
