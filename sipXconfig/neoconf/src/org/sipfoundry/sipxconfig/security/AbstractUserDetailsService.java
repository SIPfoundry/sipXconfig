/*
 *
 *
 * Copyright (C) 2009 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */

package org.sipfoundry.sipxconfig.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.sipfoundry.sipxconfig.acd.AcdContext;
import org.sipfoundry.sipxconfig.common.CoreContext;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.im.ImAccount;
import org.springframework.beans.factory.annotation.Required;

import static org.sipfoundry.sipxconfig.permission.PermissionName.RECORD_SYSTEM_PROMPTS;
import static org.sipfoundry.sipxconfig.security.UserRole.AcdSupervisor;
import static org.sipfoundry.sipxconfig.security.UserRole.Admin;
import static org.sipfoundry.sipxconfig.security.UserRole.AttendantAdmin;
import static org.sipfoundry.sipxconfig.security.UserRole.User;

public abstract class AbstractUserDetailsService implements UserDetailsService {

    private CoreContext m_coreContext;
    private AcdContext m_acdContext;
    private AdditionalAuthoritiesLoader m_authLoader;

    public final UserDetails loadUserByUsername(String userNameOrAliasOrImId) {
        User user = m_coreContext.loadUserByUserNameOrAlias(userNameOrAliasOrImId);
        if (user == null) {
            // 2nd attempt - try to login as an imID
            user = getUserForImId(userNameOrAliasOrImId);
        }

        if (user == null) {
            throw new UsernameNotFoundException(userNameOrAliasOrImId);
        }

        List<GrantedAuthority> gas = new ArrayList<GrantedAuthority>(4);
        gas.add(User.toAuth());

        if (user.isAdmin()) {
            gas.add(Admin.toAuth());
        }
        if (user.isSupervisor()) {
            gas.add(AcdSupervisor.toAuth());
        }

        if (user.hasPermission(RECORD_SYSTEM_PROMPTS)) {
            gas.add(AttendantAdmin.toAuth());
        }
        if (m_authLoader != null) {
            m_authLoader.addUserAuthorities(user, gas);
        }

        return createUserDetails(userNameOrAliasOrImId, user, gas);
    }

    private User getUserForImId(String imId) {
        User user = m_coreContext.loadUserByConfiguredImId(imId);
        if (user != null) {
            ImAccount imAccount = new ImAccount(user);
            if (!imAccount.isEnabled()) {
                return null;
            }
        }

        return user;
    }

    protected abstract UserDetails createUserDetails(String userNameOrAlias, User user, List<GrantedAuthority> gas);

    @Required
    public void setCoreContext(CoreContext coreContext) {
        m_coreContext = coreContext;
    }

    @Required
    public void setAcdContext(AcdContext acdContext) {
        m_acdContext = acdContext;
    }

    public void setAdditionalAuthoritiesLoader(AdditionalAuthoritiesLoader loader) {
        m_authLoader = loader;
    }
}
