/**
 * Copyright (c) 2014 eZuce, Inc. All rights reserved.
 * Contributed to SIPfoundry under a Contributor Agreement
 *
 * This software is free software; you can redistribute it and/or modify it under
 * the terms of the Affero General Public License (AGPL) as published by the
 * Free Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 */
package org.sipfoundry.sipxconfig.rest;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.sipfoundry.sipxconfig.common.User;

public class PersonalAttendantResource extends UserPersonalAttendantResource {
    private String m_queryUser;
    
    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        m_queryUser = (String) getRequest().getAttributes().get("user");
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
