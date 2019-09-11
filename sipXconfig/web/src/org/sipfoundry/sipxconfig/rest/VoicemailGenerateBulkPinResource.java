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

import java.util.List;

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

public class VoicemailGenerateBulkPinResource extends UserResource {
    private EmailNotifier m_emailNotifier;
    private String m_queryUser;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setModifiable(true);
        setReadable(false);
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    @Override
    public void acceptRepresentation(Representation entity) throws ResourceException {
        if(!getUser().isAdmin()) {
            // Only admin can execute this api.
            throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN); 
        }

        UserLists bean = JacksonConvert.fromRepresentation(entity, UserLists.class);
        if (bean.getUsers() != null && !bean.getUsers().isEmpty()) {
            for (String queryUser : bean.getUsers()) {
                User user = getCoreContext().loadUserByUserName(queryUser);        
                if (user != null) {
                    String randomPin = RandomStringUtils.randomNumeric(AbstractUser.VOICEMAIL_PIN_LEN);
                    user.setVoicemailPin(randomPin);
                    user.setForcePinChange(true);
                    getCoreContext().saveUser(user);

                    m_emailNotifier.sendMail(user.getUserName(), "vmpin.generate", createMailArgument(user, randomPin));
                }
            }
        }
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

    private static class UserLists {
        private List<String> m_users;

        public List<String> getUsers() {
            return m_users;
        }

        public void setUsers(List<String> users) {
            m_users = users;
        }
    }
}
