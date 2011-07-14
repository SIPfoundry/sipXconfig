/*
 *
 *
 * Copyright (C) 2009 Nortel., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */
package org.sipfoundry.sipxconfig.moh;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.classextension.EasyMock.createMock;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.sipfoundry.sipxconfig.TestHelper;
import org.sipfoundry.sipxconfig.admin.ConfigurationFile;
import org.sipfoundry.sipxconfig.admin.commserver.SipxReplicationContext;
import org.sipfoundry.sipxconfig.admin.commserver.imdb.AliasMapping;
import org.sipfoundry.sipxconfig.service.ServiceConfigurator;
import org.sipfoundry.sipxconfig.service.SipxFreeswitchService;
import org.sipfoundry.sipxconfig.service.SipxServiceManager;
import org.sipfoundry.sipxconfig.service.freeswitch.LocalStreamConfiguration;

public class MusicOnHoldManagerTest extends TestCase {

    private MusicOnHoldManagerImpl m_musicOnHoldManager;

    @Override
    protected void setUp() throws Exception {
        m_musicOnHoldManager = new MusicOnHoldManagerImpl();
        m_musicOnHoldManager.setMohUser("~~testMohUser~");
    }

    public void testIsAudioDirectoryEmpty() {
        File audioDirectory = new File(TestHelper.getTestDirectory() + File.separator + "moh");
        if (audioDirectory.exists()) {
            try {
                FileUtils.deleteDirectory(audioDirectory);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        assertTrue(audioDirectory.mkdir());
        m_musicOnHoldManager.setAudioDirectory(audioDirectory.getAbsolutePath());
        assertTrue(m_musicOnHoldManager.isAudioDirectoryEmpty());
        File mohFile = new File(audioDirectory + File.separator + "test.wav");

        try {
            assertTrue(mohFile.createNewFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertTrue(!m_musicOnHoldManager.isAudioDirectoryEmpty());
    }

    public void testReplicateMohConfiguration() {
        LocalStreamConfiguration localStreamConfiguration = new LocalStreamConfiguration();
        localStreamConfiguration.setName("/test/localconf.xml");
        Set<ConfigurationFile> configurationFiles = new HashSet<ConfigurationFile>();
        configurationFiles.add(localStreamConfiguration);

        SipxFreeswitchService service = new SipxFreeswitchService();
        service.setConfigurations(configurationFiles);

        SipxServiceManager sipxServiceManager = createMock(SipxServiceManager.class);
        sipxServiceManager.getServiceByBeanId(SipxFreeswitchService.BEAN_ID);
        expectLastCall().andReturn(service).atLeastOnce();
        replay(sipxServiceManager);

        SipxReplicationContext replicationContext = createMock(SipxReplicationContext.class);
        replicationContext.replicate(localStreamConfiguration);
        expectLastCall().atLeastOnce();
        replay(replicationContext);

        ServiceConfigurator serviceConfigurator = createMock(ServiceConfigurator.class);
        serviceConfigurator.markServiceForRestart(service);
        expectLastCall().atLeastOnce();
        replay(serviceConfigurator);

        m_musicOnHoldManager.setSipxServiceManager(sipxServiceManager);
        m_musicOnHoldManager.setReplicationContext(replicationContext);
        m_musicOnHoldManager.setServiceConfigurator(serviceConfigurator);

        m_musicOnHoldManager.replicateMohConfiguration();
    }

/*    public void testReplicateAliasData() {
        SipxReplicationContext replicationContext = createMock(SipxReplicationContext.class);
        replicationContext.generate(DataSet.ALIAS);
        expectLastCall().atLeastOnce();
        replay(replicationContext);

        m_musicOnHoldManager.setReplicationContext(replicationContext);
    }*/

    public void testGetBeanIdsOfObjectsWithAlias() {

        SipxFreeswitchService service = new SipxFreeswitchService() {
            @Override
            public Integer getId() {
                return new Integer(2);
            }
        };

        SipxServiceManager sipxServiceManager = createMock(SipxServiceManager.class);
        sipxServiceManager.getServiceByBeanId(SipxFreeswitchService.BEAN_ID);
        expectLastCall().andReturn(service).atLeastOnce();
        replay(sipxServiceManager);

        m_musicOnHoldManager.setSipxServiceManager(sipxServiceManager);

        assertTrue(!CollectionUtils.isEmpty(m_musicOnHoldManager.getBeanIdsOfObjectsWithAlias("~~testMohUser~")));
        assertTrue(!CollectionUtils.isEmpty(m_musicOnHoldManager.getBeanIdsOfObjectsWithAlias("~~testMohUser~asdf")));
        assertTrue(CollectionUtils.isEmpty(m_musicOnHoldManager.getBeanIdsOfObjectsWithAlias("~~testUser~")));
    }

    public void testIsAliasInUse() {
        assertTrue(m_musicOnHoldManager.isAliasInUse("~~testMohUser~"));
        assertTrue(m_musicOnHoldManager.isAliasInUse("~~testMohUser~asdf"));
        assertTrue(!m_musicOnHoldManager.isAliasInUse("~~testUser~"));
    }

    public boolean checkAliasMappings(AliasMapping alias, String contactToTest) {
        if ("sip:~~testMohUser~@randomAddress.test".equals(alias.getIdentity())) {
            assertEquals(contactToTest, alias.getContact());
        } else if ("sip:~~testMohUser~l@randomAddress.test".equals(alias.getIdentity())) {
            assertEquals("<sip:IVR@FSaddr:42;action=moh;moh=l>", alias.getContact());
        } else if ("sip:~~testMohUser~p@randomAddress.test".equals(alias.getIdentity())) {
            assertEquals("<sip:IVR@FSaddr:42;action=moh;moh=p>", alias.getContact());
        } else if ("sip:~~testMohUser~n@randomAddress.test".equals(alias.getIdentity())) {
            assertEquals("<sip:IVR@FSaddr:42;action=moh;moh=n>", alias.getContact());
        } else {
            return false;
        }

        return true;
    }

    public boolean checkAliasMappingsHA(AliasMapping alias, String contactToTest) {
        if ("sip:~~testMohUser~@randomAddress.test".equals(alias.getIdentity())) {
            assertEquals(contactToTest, alias.getContact());
        } else if ("sip:~~testMohUser~l@randomAddress.test".equals(alias.getIdentity())) {
            assertEquals("<sip:IVR@192.168.1.1:42;action=moh;moh=l>", alias.getContact());
        } else if ("sip:~~testMohUser~p@randomAddress.test".equals(alias.getIdentity())) {
            assertEquals("<sip:IVR@192.168.1.1:42;action=moh;moh=p>", alias.getContact());
        } else if ("sip:~~testMohUser~n@randomAddress.test".equals(alias.getIdentity())) {
            assertEquals("<sip:IVR@192.168.1.1:42;action=moh;moh=n>", alias.getContact());
        } else {
            return false;
        }

        return true;
    }
}
