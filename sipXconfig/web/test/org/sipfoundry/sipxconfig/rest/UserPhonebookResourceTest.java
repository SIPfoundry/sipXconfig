/*
 *
 *
 * Copyright (C) 2009 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.rest;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;

import java.util.Collection;

import org.sipfoundry.sipxconfig.TestHelper;
import org.sipfoundry.sipxconfig.phonebook.Phonebook;
import org.sipfoundry.sipxconfig.phonebook.PhonebookManager;

public class UserPhonebookResourceTest extends UserPhonebookSearchResourceTest {

    @Override
    protected void setUp() throws Exception {
        Collection<Phonebook> phonebooks = TestHelper.getMockAllPhonebooks();

        m_phonebookManager = createMock(PhonebookManager.class);

        m_phonebookManager.getAllPhonebooksByUser(null);
        expectLastCall().andReturn(phonebooks);
        m_phonebookManager.getEntries(phonebooks, m_user);
        expectLastCall().andReturn(getMockPhonebookEntries());

        replay(m_phonebookManager);

        m_resource = new UserPhonebookResource();
        UserPhonebookResource resource = (UserPhonebookResource)m_resource;
        resource.setPhonebookManager(m_phonebookManager);
    }
}
