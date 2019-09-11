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

import static org.sipfoundry.sipxconfig.permission.PermissionName.TUI_CHANGE_PIN;

import org.apache.commons.lang.RandomStringUtils;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.sipfoundry.sipxconfig.common.AbstractUser;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.mail.EmailNotifier;

public class VoicemailGeneratePinResource extends UserResource {
    private EmailNotifier m_emailNotifier;
    private String m_queryUser;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);

        m_queryUser = (String) getRequest().getAttributes().get("user");

        try {
            setModifiable(getUserToQuery().hasPermission(TUI_CHANGE_PIN));
        } catch(ResourceException e) {
            setModifiable(false);
        }
        setReadable(false);
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    @Override
    public void acceptRepresentation(Representation entity) throws ResourceException {
        User user = getUserToQuery();
        String randomPin = RandomStringUtils.randomNumeric(AbstractUser.VOICEMAIL_PIN_LEN);
        user.setVoicemailPin(randomPin);
        user.setForcePinChange(true);
        getCoreContext().saveUser(user);

        m_emailNotifier.sendMail(user.getUserName(), "vmpin.generate", createMailArgument(user, randomPin));
    }
    
    public User getUserToQuery() throws ResourceException {
        User user = getUser();
        if(user.isAdmin()) {
           // Allow query by user only if logged user is an admin
           User queryUser = getCoreContext().loadUserByUserName(m_queryUser);
           if (queryUser == null) {
               throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid User");
           }
           return queryUser;
        }
        
        throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN); 
    }

    public Object[] createMailArgument(User user, String pin) {
        Object[] args = new Object[2];
        args[0] = pin;
        args[1] = user.getUserName();
        return args;
    }

    public void setEmailNotifier(EmailNotifier emailNotifier) {
        m_emailNotifier = emailNotifier;
    }

}
