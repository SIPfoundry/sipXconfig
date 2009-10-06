/*
 *
 *
 * Copyright (C) 2009 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */

package org.sipfoundry.sipxconfig.rest;

import java.io.StringWriter;

import junit.framework.TestCase;
import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContextHolder;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sipfoundry.sipxconfig.common.CoreContext;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.security.TestAuthenticationToken;
import org.sipfoundry.sipxconfig.vm.MailboxManager;
import org.sipfoundry.sipxconfig.vm.MailboxPreferences;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

public class UserGravatarResourceTest extends TestCase {

    private User m_user;
    private CoreContext m_coreContext;

    @Override
    protected void setUp() throws Exception {
        m_user = new User();
        m_user.setUniqueId();
        m_user.setUserName("200");

        Authentication token = new TestAuthenticationToken(m_user, false, false).authenticateToken();
        SecurityContextHolder.getContext().setAuthentication(token);

        m_coreContext = createMock(CoreContext.class);
        m_coreContext.loadUser(m_user.getId());
        expectLastCall().andReturn(m_user);
        m_coreContext.saveUser(m_user);
        expectLastCall().andReturn(false);
        replay(m_coreContext);
    }

    @Override
    protected void tearDown() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    public void testGetGravatar() throws Exception {
        MailboxPreferences mailboxPreferences = new MailboxPreferences();
        mailboxPreferences.setEmailAddress("iHaveAn@email.com");

        MailboxManager mailboxManager = createMock(MailboxManager.class);
        mailboxManager.getMailboxPreferencesForUser(m_user);
        expectLastCall().andReturn(mailboxPreferences);
        replay(mailboxManager);

        UserAvatarResource resource = new UserAvatarResource();
        resource.setCoreContext(m_coreContext);
        resource.setMailboxManager(mailboxManager);
        ChallengeResponse challengeResponse = new ChallengeResponse(null, "200", new char[0]);

        Request request = new Request();
        request.getAttributes().put("userid", m_user.getId().toString());
        request.setChallengeResponse(challengeResponse);
        resource.init(null, request, null);

        Representation representation = resource.represent(new Variant(MediaType.TEXT_ALL));
        StringWriter writer = new StringWriter();
        representation.write(writer);
        String generated = writer.toString();

        assertEquals("http://www.gravatar.com/avatar/3b3be63a4c2a439b013787725dfce802", generated);
        verify(mailboxManager);
    }
}
