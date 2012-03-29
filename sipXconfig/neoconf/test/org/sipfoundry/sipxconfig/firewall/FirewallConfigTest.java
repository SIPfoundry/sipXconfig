/**
 * Copyright (c) 2012 eZuce, Inc. All rights reserved.
 * Contributed to SIPfoundry under a Contributor Agreement
 *
 * This software is free software; you can redistribute it and/or modify it under
 * the terms of the Affero General Public License (AGPL) as published by the
 * Free Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 */
package org.sipfoundry.sipxconfig.firewall;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.sipfoundry.sipxconfig.address.Address;
import org.sipfoundry.sipxconfig.address.AddressManager;
import org.sipfoundry.sipxconfig.address.AddressType;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.test.TestHelper;

public class FirewallConfigTest {
    private FirewallConfig m_config;
    private FirewallSettings m_settings;
    private StringWriter m_actual;        
    
    @Before
    public void setUp() {
        m_config = new FirewallConfig();
        m_settings = new FirewallSettings();
        m_settings.setModelFilesContext(TestHelper.getModelFilesContext());
        m_actual = new StringWriter();        
    }
    
    @Test
    public void sysctl() throws IOException {
        m_config.writeSysctl(m_actual, m_settings);
        String expected = IOUtils.toString(getClass().getResourceAsStream("expected-sysctl.part"));
        assertEquals(expected, m_actual.toString());
    }

    @Test
    public void iptables() throws IOException {        
        List<FirewallRule> rules = new ArrayList<FirewallRule>();
        AddressType testType = new AddressType("test");
        rules.add(new DefaultFirewallRule(testType, FirewallRule.SystemId.PUBLIC));
        Location location = TestHelper.createDefaultLocation();

        AddressManager addressManager = createMock(AddressManager.class);
        addressManager.getAddresses(testType, location);
        Address a1 = new Address(testType, "10.1.1.1", 100);
        Address a2 = new Address(testType, "10.1.1.1", 200);
        expectLastCall().andReturn(Arrays.asList(a1, a2)).once();
        replay(addressManager);
        m_config.setAddressManager(addressManager);
        
        m_config.writeIptables(m_actual, rules, location);
        String expected = IOUtils.toString(getClass().getResourceAsStream("expected-firewall.yaml"));
        assertEquals(expected, m_actual.toString());
        
        verify(addressManager);
    }
}
