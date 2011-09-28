/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.admin.commserver.imdb;

import java.util.ArrayList;
import java.util.Collection;

import org.sipfoundry.sipxconfig.common.User;

public class AliasesIntegrationTest extends ImdbTestCase {
    private Aliases m_aliases;
    private User m_user;

    @Override
    protected void onSetUpBeforeTransaction() {
        m_aliases = new Aliases();
        Collection<AliasMapping> aliases = new ArrayList<AliasMapping>();
        aliases.add(new AliasMapping("301@example.org", "\"John Doe\"<sip:john.doe@" + DOMAIN + ">", "alias"));
        m_user = new User();
        m_user.setUniqueId(1);
        m_user.setDomainManager(getDomainManager());
        m_user.setPermissionManager(getPermissionManager());
        m_aliases.setDbCollection(getEntityCollection());
        m_aliases.setCoreContext(getCoreContext());
    }

    public void testGenerate() {
        m_aliases.generate(m_user, m_aliases.findOrCreate(m_user));
        assertObjectWithIdPresent("User1");
    }
}
