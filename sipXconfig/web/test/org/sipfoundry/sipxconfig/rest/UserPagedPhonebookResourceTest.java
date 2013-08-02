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

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.easymock.classextension.EasyMock.createMock;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sipfoundry.sipxconfig.common.CoreContext;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.phonebook.AddressBookEntry;
import org.sipfoundry.sipxconfig.phonebook.PagedPhonebook;
import org.sipfoundry.sipxconfig.phonebook.Phonebook;
import org.sipfoundry.sipxconfig.phonebook.PhonebookEntry;
import org.sipfoundry.sipxconfig.phonebook.PhonebookManager;
import org.sipfoundry.sipxconfig.security.TestAuthenticationToken;
import org.sipfoundry.sipxconfig.test.TestHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class UserPagedPhonebookResourceTest extends TestCase {
    protected PhonebookManager m_phonebookManager;
    protected UserResource m_resource;
    protected User m_user;
    protected CoreContext m_coreContext;

    @Override
    protected void setUp() throws Exception {
        m_user = new User();
        m_user.setUniqueId();
        m_user.setUserName("200");
        m_coreContext = createMock(CoreContext.class);
        m_coreContext.loadUser(m_user.getId());
        expectLastCall().andReturn(m_user);
        m_coreContext.loadUserByUserName(m_user.getUserName());
        expectLastCall().andReturn(m_user);
        m_coreContext.saveUser(m_user);
        expectLastCall().andReturn(false);
        replay(m_coreContext);

        Authentication token = new TestAuthenticationToken(m_user, false, false).authenticateToken();
        SecurityContextHolder.getContext().setAuthentication(token);

        Collection<Phonebook> phonebooks = TestHelper.getMockPublicPhonebooks();

        m_phonebookManager = createMock(PhonebookManager.class);
        m_phonebookManager.getPublicPhonebooksByUser(m_user);
        expectLastCall().andReturn(phonebooks);
        m_phonebookManager.getPagedPhonebook(phonebooks, m_user, "0", "3", "filter");
        expectLastCall().andReturn(getMockPhonebookEntries());
        replay(m_phonebookManager);

        m_resource = new UserPagedPhonebookResource();
        UserPhonebookSearchResource resource = (UserPhonebookSearchResource) m_resource;
        resource.setPhonebookManager(m_phonebookManager);
        resource.setCoreContext(m_coreContext);
        Request request = new Request();
        Reference reference = new Reference();
        reference.addQueryParameter("start", "0");
        reference.addQueryParameter("end", "3");
        reference.addQueryParameter("filter", "filter");
        request.setResourceRef(reference);
        ChallengeResponse challengeResponse = new ChallengeResponse(null, "200", new char[0]);
        request.setChallengeResponse(challengeResponse);
        resource.setRequest(request);
        resource.init(null, request, null);
    }

    protected PagedPhonebook getMockPhonebookEntries() {

        PhonebookEntry entry1 = new PhonebookEntry();
        PhonebookEntry entry2 = new PhonebookEntry();
        PhonebookEntry entry3 = new PhonebookEntry();
        PhonebookEntry entry4 = new PhonebookEntry();
        PhonebookEntry entry5 = new PhonebookEntry();

        entry1.setFirstName("FirstName1");
        entry1.setLastName("LastName1");
        entry1.setNumber("200");
        entry1.setAddressBookEntry(new AddressBookEntry());
        entry2.setFirstName("FirstName2");
        entry2.setLastName("LastName2");
        entry2.setNumber("201");
        AddressBookEntry abe = new AddressBookEntry();
        abe.setEmailAddress("iHaveAn@email.com");
        entry2.setAddressBookEntry(abe);
        entry3.setFirstName("FirstName3");
        entry3.setLastName("LastName3");
        entry3.setNumber("202");
        entry3.setAddressBookEntry(new AddressBookEntry());
        entry4.setFirstName("AnotherName1");
        entry4.setLastName("AnotherGivenName1");
        entry4.setNumber("203");
        entry4.setAddressBookEntry(new AddressBookEntry());
        entry5.setFirstName("AnotherName2");
        entry5.setLastName("AnotherGivenName2");
        entry5.setNumber("204");
        entry5.setAddressBookEntry(new AddressBookEntry());

        return new PagedPhonebook(Arrays.asList(entry1, entry2), 5, "0", "2", null, "mydomain.com");
    }

    public void testRepresentXml() throws Exception {
        Representation representation = m_resource.represent(new Variant(MediaType.TEXT_XML));

        StringWriter writer = new StringWriter();
        representation.write(writer);
        String generated = writer.toString();
        String expected = IOUtils.toString(getClass().getResourceAsStream("user-pagedphonebook.rest.test.xml"));
        assertEquals(expected, generated);

        verify(m_phonebookManager);
    }

    public void testRepresentJson() throws Exception {
        Representation representation = m_resource.represent(new Variant(MediaType.APPLICATION_JSON));

        StringWriter writer = new StringWriter();
        representation.write(writer);
        String generated = writer.toString();
        String expected = IOUtils.toString(getClass().getResourceAsStream("user-pagedphonebook.rest.test.json"));
        assertEquals(expected, generated);

        verify(m_phonebookManager);
    }
}
