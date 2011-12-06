/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.admin.callgroup;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import junit.framework.TestCase;

import org.sipfoundry.sipxconfig.admin.commserver.imdb.AliasMapping;
import org.sipfoundry.sipxconfig.common.Replicable;
import org.sipfoundry.sipxconfig.common.User;

public class CallGroupTest extends TestCase {

    private CallGroup m_callGroup;
    private static final int m_numRings = 5;
    private static final String ATMYDOMAIN = "@mydomain.org";
    private static final String MYDOMAIN = "mydomain.org";

    @Override
    protected void setUp() throws Exception {
        m_callGroup = createCallGroupWithUsers("401", "sales", m_numRings, false);
    }

    public void testInsertRingUser() {
        User u = new User();
        u.setUserName("testUser");
        CallGroup group = new CallGroup();
        UserRing ring = group.insertRingForUser(u);
        assertSame(u, ring.getUser());
        List calls = group.getRings();
        assertEquals(1, calls.size());
        assertSame(ring, calls.get(0));
        assertTrue(ring.isFirst());
    }

    public void testGenerateAliases() {
        List<AliasMapping> aliases = m_callGroup.getAliasMappings(MYDOMAIN);

        assertEquals(m_numRings + 1, aliases.size());
        for (int i = 0; i < m_numRings; i++) {
            AliasMapping am = aliases.get(i);
            assertEquals(m_callGroup.getName(), am.getIdentity().toString());
            assertTrue(am.getContact().toString().startsWith("<sip:testUser" + i + ATMYDOMAIN));
            // all of the contacts should contain sipx-noroute=Voicemail
            assertTrue(am.getContact().toString().contains("sipx-noroute=Voicemail"));
        }

        // the last alias is an extension => identity
        AliasMapping am = aliases.get(aliases.size() - 1);
        assertEquals(am.getIdentity().toString(), m_callGroup.getExtension());
        assertTrue(am.getContact().toString().startsWith(m_callGroup.getName() + ATMYDOMAIN));
    }

    public void testGenerateAliasesForDisabledGroup() {
        m_callGroup.setEnabled(false);

        List<AliasMapping> aliases = m_callGroup.getAliasMappings(MYDOMAIN);

        // disabled group should not generate aliases
        assertTrue(aliases.isEmpty());

        // not even when fallback is enabled
        m_callGroup.setVoicemailFallback(true);
        aliases = m_callGroup.getAliasMappings(MYDOMAIN);
        assertTrue(aliases.isEmpty());

        m_callGroup.setFallbackDestination("fallback@kuku.com");
        aliases = m_callGroup.getAliasMappings(MYDOMAIN);
        assertTrue(aliases.isEmpty());

        m_callGroup.setUserForward(true);
        aliases = m_callGroup.getAliasMappings(MYDOMAIN);
        assertTrue(aliases.isEmpty());
    }

    public void testGenerateAliasesWithVoicemailFallback() {
        m_callGroup.setVoicemailFallback(true);
        List<AliasMapping> aliases = m_callGroup.getAliasMappings(MYDOMAIN);
        // assumption: the aliases list is ordered from highest q value to lowest,
        // with last element being the identity alias.

        assertEquals(m_numRings + 2, aliases.size());
        double lastQ = 1;
        for (int i = 0; i < m_numRings; i++) {
            AliasMapping am = aliases.get(i);
            assertEquals(am.getIdentity(), m_callGroup.getName());
            String contact = am.getContact();
            assertTrue(contact.startsWith("<sip:testUser" + i + ATMYDOMAIN));
            // all of the contacts should contain sipx-noroute=Voicemail
            assertTrue(contact.contains("sipx-noroute=Voicemail"));
            double q = parseQ(contact);
            assertTrue(q < lastQ);
            assertTrue(q > 0.8);
            lastQ = q;
        }

        // the second to last alias (lowest q value) should sent last user to voicemail
        AliasMapping vmailMapping = aliases.get(aliases.size() - 2);
        assertEquals(m_callGroup.getName(), vmailMapping.getIdentity());
        String contact = vmailMapping.getContact();
        assertTrue(contact.startsWith("<sip:~~vm~testUser4" + ATMYDOMAIN));
        double q = parseQ(contact);
        assertTrue(q < lastQ);

        // the last alias is an extension => identity
        AliasMapping am = aliases.get(aliases.size() - 1);
        assertEquals(m_callGroup.getExtension(), am.getIdentity());
        assertTrue(am.getContact().startsWith(m_callGroup.getName() + ATMYDOMAIN));
    }

    public void testGenerateAliasesWithVoicemailFallbackParallelGroup() {
        m_callGroup.setVoicemailFallback(true);
        List<AbstractRing> rings = m_callGroup.getRings();
        for (AbstractRing ring : rings) {
            ring.setType(AbstractRing.Type.IMMEDIATE);
        }
        List<AliasMapping> aliases = m_callGroup.getAliasMappings(MYDOMAIN);
        // assumption: the aliases list is ordered from highest q value to lowest,
        // with last element being the identity alias.

        assertEquals(m_numRings + 2, aliases.size());
        double lastQ = 0;
        for (int i = 0; i < m_numRings; i++) {
            AliasMapping am = aliases.get(i);
            assertEquals(am.getIdentity(), m_callGroup.getName()    );
            String contact = am.getContact();
            assertTrue(contact.startsWith("<sip:testUser" + i + ATMYDOMAIN));
            // all of the contacts should contain sipx-noroute=Voicemail
            assertTrue(contact.contains("sipx-noroute=Voicemail"));
            double q = parseQ(contact);
            if (lastQ == 0) {
                lastQ = q;
            }
            assertEquals(q, lastQ);
        }

        // the second to last alias (lowest q value) should sent last user to voicemail
        AliasMapping vmailMapping = aliases.get(aliases.size() - 2);
        assertEquals(m_callGroup.getName(), vmailMapping.getIdentity());
        String contact = vmailMapping.getContact();
        assertTrue(contact.startsWith("<sip:~~vm~testUser4" + ATMYDOMAIN));
        double q = parseQ(contact);
        assertTrue(q < lastQ);

        // the last alias is an extension => identity
        AliasMapping am = aliases.get(aliases.size() - 1);
        assertEquals(am.getIdentity(), m_callGroup.getExtension());
        assertTrue(am.getContact().startsWith(m_callGroup.getName() + ATMYDOMAIN));
    }

    public void testGenerateAliasesWithUserForwardDisabled() {
        m_callGroup.setUserForward(false);

        List<AliasMapping> aliases = m_callGroup.getAliasMappings(MYDOMAIN);
        // assumption: the aliases list is ordered from highest q value to lowest,
        // with last element being the identity alias.

        assertEquals(m_numRings + 1, aliases.size());
        for (int i = 0; i < m_numRings; i++) {
            AliasMapping am = aliases.get(i);
            assertEquals(am.getIdentity(), m_callGroup.getName());
            assertTrue(am.getContact().startsWith("<sip:testUser" + i + ATMYDOMAIN));
            // all of the contacts should contain sipx-noroute=Voicemail
            assertTrue(am.getContact().contains("sipx-userforward=false"));
        }

    }

    public void testGenerateAliasesForEmptyGroupWithVoicemailFallback() {
        CallGroup group = createCallGroupWithUsers("401", "sales", 0, true);
        group.setEnabled(true);

        List<AliasMapping> aliases = group.getAliasMappings(MYDOMAIN);
        // assumption: the aliases list is ordered from highest q value to lowest,
        // with last element being the identity alias.

        assertEquals(1, aliases.size());
        // the last alias is an extension => identity
        AliasMapping am = aliases.get(aliases.size() - 1);
        assertEquals(am.getIdentity(), group.getExtension());
        assertTrue(am.getContact().startsWith(group.getName() + ATMYDOMAIN));
    }

    public void testGenerateAliasesWithFallbackDestinationAsSipUri() {
        String fallbackDestination = "fallback" + ATMYDOMAIN;
        m_callGroup.setFallbackDestination(fallbackDestination);

        List<AliasMapping> aliases = m_callGroup.getAliasMappings(MYDOMAIN);
        // assumption: the aliases list is ordered from highest q value to lowest,
        // with last element being the identity alias.

        assertEquals(m_numRings + 2, aliases.size());
        double lastQ = 1;
        for (int i = 0; i < m_numRings; i++) {
            AliasMapping am = aliases.get(i);
            assertEquals(am.getIdentity(), m_callGroup.getName());
            String contact = am.getContact();
            assertTrue(contact.startsWith("<sip:testUser" + i + ATMYDOMAIN));
            // all of the contacts should contain sipx-noroute=Voicemail
            assertTrue(contact.contains("sipx-noroute=Voicemail"));
            double q = parseQ(contact);
            assertTrue(q < lastQ);
            assertTrue(q > 0.8);
            lastQ = q;
        }

        // the second to last alias (lowest q value) should be the default sip uri
        AliasMapping defaultSipUriMapping = aliases.get(aliases.size() - 2);
        assertEquals(m_callGroup.getName(), defaultSipUriMapping.getIdentity());
        String contact = defaultSipUriMapping.getContact();
        assertTrue(contact.contains("fallback" + ATMYDOMAIN));
        assertTrue(parseQ(contact) < lastQ);

        // the last alias is an extension => identity
        AliasMapping am = aliases.get(aliases.size() - 1);
        assertEquals(am.getIdentity(), m_callGroup.getExtension());
        assertTrue(am.getContact().startsWith(m_callGroup.getName() + ATMYDOMAIN));
    }

    public void testGenerateAliasesWithFallbackDestinationAsExtension() {
        String fallbackDestination = "1234";
        m_callGroup.setFallbackDestination(fallbackDestination);

        List<AliasMapping> aliases = m_callGroup.getAliasMappings(MYDOMAIN);
        // assumption: the aliases list is ordered from highest q value to lowest,
        // with last element being the identity alias.

        assertEquals(m_numRings + 2, aliases.size());
        for (int i = 0; i < m_numRings; i++) {
            AliasMapping am = aliases.get(i);
            assertEquals(am.getIdentity(), m_callGroup.getName());
            assertTrue(am.getContact().startsWith("<sip:testUser" + i + ATMYDOMAIN));
            // all of the contacts should contain sipx-noroute=Voicemail
            // (this is different from case without default sip uri)
            assertTrue(am.getContact().contains("sipx-noroute=Voicemail"));
        }

        // the second to last alias (lowest q value) should be the default sip uri
        AliasMapping defaultSipUriMapping = aliases.get(aliases.size() - 2);
        assertEquals(m_callGroup.getName(), defaultSipUriMapping.getIdentity());
        assertTrue(defaultSipUriMapping.getContact().contains("1234" + ATMYDOMAIN));

        // the last alias is an extension => identity
        AliasMapping am = aliases.get(aliases.size() - 1);
        assertEquals(am.getIdentity(), m_callGroup.getExtension());
        assertTrue(am.getContact().startsWith(m_callGroup.getName() + ATMYDOMAIN));
    }

    public void testGenerateAliasesNameAndExtSame() {
        CallGroup group = createCallGroupWithUsers("402", "402", 0, true);
        Collection<AliasMapping> aliases = group.getAliasMappings(MYDOMAIN);
        assertEquals(0, aliases.size());
    }

    public void testClone() {
        CallGroup group = new CallGroup();
        group.setName("sales");
        group.setExtension("401");

        final int ringsLen = 5;
        for (int i = 0; i < ringsLen; i++) {
            User u = new User();
            u.setUserName("testUser" + i);
            group.insertRingForUser(u);
        }
        assertEquals(ringsLen, group.getRings().size());

        CallGroup clonedGroup = (CallGroup) group.duplicate();
        assertEquals("sales", clonedGroup.getName());
        assertEquals("401", clonedGroup.getExtension());
        List clonedCalls = clonedGroup.getRings();
        assertEquals(ringsLen, clonedCalls.size());
        for (int i = 0; i < ringsLen; i++) {
            UserRing ring = (UserRing) clonedCalls.get(i);
            assertEquals("testUser" + i, ring.getUser().getUserName());
            assertSame(clonedGroup, ring.getCallGroup());
        }
    }

    private double parseQ(String contact) {
        Scanner scanner = new Scanner(contact);
        scanner.findInLine("q=");
        if (scanner.hasNextDouble()) {
            return scanner.nextDouble();
        }
        return 0;
    }

    private CallGroup createCallGroupWithUsers(String extension, String name, int numUsers, boolean vmFallback) {
        CallGroup group = new CallGroup();

        group.setEnabled(true);
        group.setName(name);
        group.setExtension(extension);
        group.setVoicemailFallback(vmFallback);

        for (int i = 0; i < numUsers; i++) {
            User u = new User();
            u.setUserName("testUser" + i);
            group.insertRingForUser(u);
        }

        return group;
    }

}
