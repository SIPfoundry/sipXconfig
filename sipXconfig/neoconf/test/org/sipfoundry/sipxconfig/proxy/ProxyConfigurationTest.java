/*
 * Copyright (C) 2011 eZuce Inc., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the AGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.proxy;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.junit.Before;
import org.junit.Test;
import org.sipfoundry.sipxconfig.common.InternalUser;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.domain.Domain;
import org.sipfoundry.sipxconfig.test.TestHelper;
import org.sipfoundry.sipxconfig.tls.TlsPeer;
import org.sipfoundry.sipxconfig.tls.TlsPeerManager;


public class ProxyConfigurationTest {
    private Domain m_domain;
    private Location m_location;
    private ProxyConfiguration m_config;
    private ProxySettings m_settings;
        
    @Before
    public void setUp() {
        m_config = new ProxyConfiguration();
        m_settings = new ProxySettings();
        m_settings.setModelFilesContext(TestHelper.getModelFilesContext());
        m_location = TestHelper.createDefaultLocation();
        m_domain = new Domain("example.org");
        m_domain.setSipRealm("realm.example.org");
    }

    @Test
    public void testConfig() throws Exception {
        StringWriter actual = new StringWriter();
        m_config.write(actual, m_settings, m_location, m_domain, true);
        String expected = IOUtils.toString(getClass().getResourceAsStream("expected-proxy-config"));
        assertEquals(expected, actual.toString());
    }
    
    @Test
    public void testPeersConfig() throws Exception {        
        TlsPeer peer1 = new TlsPeer();
        peer1.setName("trusteddomain.com");
        InternalUser user1 = new InternalUser();
        user1.setUserName("~~tp~trusteddomain.com");
        peer1.setInternalUser(user1);

        TlsPeer peer2 = new TlsPeer();
        peer2.setName("10.10.1.2");
        InternalUser user2 = new InternalUser();
        user2.setUserName("~~tp~10.10.1.2");
        peer2.setInternalUser(user2);
        
        Collection<TlsPeer> peers = Arrays.asList(peer1, peer2);
        
        TlsPeerManager tlsPeerManager = createMock(TlsPeerManager.class);
        replay(tlsPeerManager);
        Document doc = m_config.getDocument(peers);
        String actual = TestHelper.asString(doc);
        String expected = IOUtils.toString(getClass().getResourceAsStream("peeridentities.test.xml"));
        assertEquals(expected, actual);
    }
}
