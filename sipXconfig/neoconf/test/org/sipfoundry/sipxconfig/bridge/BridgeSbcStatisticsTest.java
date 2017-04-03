/*
 *
 *
 * Copyright (C) 2008 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */
package org.sipfoundry.sipxconfig.bridge;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.commserver.ServiceStatus;
import org.sipfoundry.sipxconfig.freeswitch.api.FreeswitchApi;
import org.sipfoundry.sipxconfig.freeswitch.api.FreeswitchSofiaStatus;
import org.sipfoundry.sipxconfig.setting.ModelFilesContext;
import org.sipfoundry.sipxconfig.snmp.SnmpManager;
import org.sipfoundry.sipxconfig.test.TestHelper;
import org.sipfoundry.sipxconfig.xmlrpc.ApiProvider;

public class BridgeSbcStatisticsTest {
    private BridgeSbc m_sbc;
    private BridgeSbcStatistics m_sbcStats;

    @Before
    public void setUp() throws Exception {
        ModelFilesContext modelFilesContext = TestHelper.getModelFilesContext();
        Location location = new Location();
        location.setUniqueId();
        location.setAddress("98.65.1.5");
        location.setFqdn("sipx.example.org");
        
        m_sbc = new BridgeSbc();
        m_sbc.setLocation(location);
        m_sbc.setModelFilesContext(modelFilesContext);
        m_sbc.setSettingValue("bridge-configuration/xml-rpc-port", "8888");

        Integer callCountResult = new Integer(10);
        
        String sofiaProfiles = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"
        		+ "<profiles>"
        		+ "<gateway>"
        		+ "<name>id-1</name><type>gateway</type><data>sip:1@47.123.2.34</data><state>REGED</state>"
        		+ "</gateway>"
        		+ "<gateway>"
        		+ "<name>id-2</name><type>gateway</type><data>sip:1@47.123.2.35</data><state>REGED</state>"
        		+ "</gateway>"
        		+ "<gateway>"
        		+ "<name>id-3</name><type>gateway</type><data>sip:1@47.123.2.36</data><state>REGED</state>"
        		+ "</gateway>"
        		+ "</profiles>";

        List<ServiceStatus> stats = new ArrayList<ServiceStatus>();
        ServiceStatus status = new ServiceStatus("sipxbridge", ServiceStatus.Status.Running, false, false);
        stats.add(status);
        SnmpManager snmpMgr = createMock(SnmpManager.class);
        snmpMgr.getServicesStatuses(location);
        expectLastCall().andReturn(stats);
        replay(snmpMgr);

        final FreeswitchApi freeswitchApiProvider = createMock(FreeswitchApi.class);
        freeswitchApiProvider.sofia("xmlstatus");
        expectLastCall().andReturn(callCountResult);
        freeswitchApiProvider.show("calls count");
        expectLastCall().andReturn(sofiaProfiles);
        replay(freeswitchApiProvider);

        ApiProvider<FreeswitchApi> provider = new ApiProvider<FreeswitchApi>() {
            public FreeswitchApi getApi(String serviceUrl) {
                return freeswitchApiProvider;
            }
        };

        m_sbcStats = new BridgeSbcStatistics();
        m_sbcStats.setFreeswitchApiProvider(provider);
        m_sbcStats.setSnmpManager(snmpMgr);
    }

    @Test
    public void testGetCallCount() throws Exception {
        int callCount = m_sbcStats.getCallCount(m_sbc);
        assertEquals(10, callCount);
    }

    @Test
    public void testGetRegistrationRecords() throws Exception {
        BridgeSbcRegistrationRecord[] bridgeSbcRegistrationRecords = m_sbcStats.getRegistrationRecords(m_sbc);
        Map<String, BridgeSbcRegistrationRecord> recordMap = new HashMap<String, BridgeSbcRegistrationRecord>();
        for (BridgeSbcRegistrationRecord record : bridgeSbcRegistrationRecords) {
            recordMap.put(record.getRegisteredAddress(), record);
        }
        
        String expectedStatus = FreeswitchSofiaStatus.State.STATE_REGISTERED.getMessage();

        assertEquals(3, bridgeSbcRegistrationRecords.length);
        assertTrue(recordMap.containsKey("sip:1@47.123.2.34"));
        assertTrue(recordMap.containsKey("sip:1@47.123.2.35"));
        assertTrue(recordMap.containsKey("sip:1@47.123.2.36"));
        assertEquals(expectedStatus, recordMap.get("sip:1@47.123.2.34").getRegistrationStatus());
        assertEquals(expectedStatus, recordMap.get("sip:1@47.123.2.35").getRegistrationStatus());
        assertEquals(expectedStatus, recordMap.get("sip:1@47.123.2.36").getRegistrationStatus());
    }
}
