package org.sipfoundry.sipxconfig.rest;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.sipfoundry.sipxconfig.common.User;

public class VoicemailPreferencesResource extends UserVoicemailPreferencesResource {

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
