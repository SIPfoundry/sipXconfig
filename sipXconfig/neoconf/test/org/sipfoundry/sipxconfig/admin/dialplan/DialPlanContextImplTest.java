/*
 * 
 * 
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.  
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 * 
 * $
 */
package org.sipfoundry.sipxconfig.admin.dialplan;

import java.util.Arrays;
import java.util.Collections;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.sipfoundry.sipxconfig.admin.dialplan.config.ConfigGenerator;
import org.sipfoundry.sipxconfig.admin.dialplan.config.ConfigGeneratorTest;
import org.sipfoundry.sipxconfig.gateway.Gateway;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 * DialPlanContextImplTest
 */
public class DialPlanContextImplTest extends TestCase {

    public void testActivateDialPlan() throws Exception {
        IMocksControl bfCtrl = EasyMock.createControl();
        BeanFactory bf = bfCtrl.createMock(ListableBeanFactory.class);
        bf.getBean(ConfigGenerator.BEAN_NAME, ConfigGenerator.class);
        bfCtrl.andReturn(ConfigGeneratorTest.createConfigGenerator());
        bfCtrl.andReturn(ConfigGeneratorTest.createConfigGenerator());
        bfCtrl.andReturn(ConfigGeneratorTest.createConfigGenerator());
        bfCtrl.replay();

        DialPlanContextImpl manager = new MockDialPlanContextImpl(new DialPlan());
        manager.setBeanFactory(bf);

        final ConfigGenerator g1 = manager.getGenerator();
        final ConfigGenerator g2 = manager.generateDialPlan();
        final ConfigGenerator g3 = manager.getGenerator();
        assertNotNull(g1);
        assertNotNull(g2);
        assertNotSame(g1, g2);
        assertNotSame(g2, g3);

        bfCtrl.verify();
    }

    public void testMoveRules() throws Exception {
        IMocksControl mock = org.easymock.classextension.EasyMock.createNiceControl();
        DialPlan plan = mock.createMock(DialPlan.class);
        plan.moveRules(Collections.singletonList(new Integer(5)), 3);
        mock.replay();

        MockDialPlanContextImpl manager = new MockDialPlanContextImpl(plan);
        manager.moveRules(Collections.singletonList(new Integer(5)), 3);
        mock.verify();
    }

    public void testLikelyVoiceMail() {
        DialPlan plan = new DialPlan();
        DialPlanContextImpl manager = new MockDialPlanContextImpl(plan);
        assertEquals("101", manager.getVoiceMail());

        DialingRule[] rules = new DialingRule[] {
            new CustomDialingRule()
        };
        plan.setRules(Arrays.asList(rules));
        assertEquals("101", manager.getVoiceMail());

        InternalRule irule = new InternalRule();
        irule.setVoiceMail("2000");
        rules = new DialingRule[] {
            new CustomDialingRule(), irule
        };
        plan.setRules(Arrays.asList(rules));
        assertEquals("2000", manager.getVoiceMail());
    }

    public void testLikelyEmergencyGateway() {
        DialPlan plan = new DialPlan();
        DialPlanContextImpl manager = new MockDialPlanContextImpl(plan);
        assertNull(manager.getLikelyEmergencyInfo());

        EmergencyRule emergency = new EmergencyRule();
        DialingRule[] rules = new DialingRule[] {
                emergency
        };
        emergency.setEmergencyNumber("sos");
        plan.setRules(Arrays.asList(rules));        
        assertNull(manager.getLikelyEmergencyInfo());

        Gateway gateway = new Gateway();
        gateway.setAddress("pstn.example.org");
        gateway.setAddressPort(9050);        
        emergency.setGateways(Collections.singletonList(gateway));
        EmergencyInfo info = manager.getLikelyEmergencyInfo();         
        assertEquals("pstn.example.org", info.getAddress());
        assertEquals((Integer) 9050, info.getPort());
        assertEquals("sos", info.getNumber());
        
        Gateway gatewayWithSbc = new Gateway() {
            public String getRoute() {
                return "sbc.example.org";
            }
        };
        rules[0].setGateways(Collections.singletonList(gatewayWithSbc));
        assertNull(manager.getLikelyEmergencyInfo());                
    }

    private static class MockDialPlanContextImpl extends DialPlanContextImpl {
        private DialPlan m_plan;

        MockDialPlanContextImpl(DialPlan plan) {
            m_plan = plan;
            IMocksControl mock = org.easymock.classextension.EasyMock.createNiceControl();
            setHibernateTemplate(mock.createMock(HibernateTemplate.class));
        }

        DialPlan getDialPlan() {
            return m_plan;
        }

        public EmergencyRouting getEmergencyRouting() {
            return new EmergencyRouting();
        }
    }
}
