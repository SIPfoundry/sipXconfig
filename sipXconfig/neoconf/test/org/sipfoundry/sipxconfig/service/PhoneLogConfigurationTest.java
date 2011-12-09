/*
 *
 *
 * Copyright (C) 2009 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */
package org.sipfoundry.sipxconfig.service;

import org.easymock.EasyMock;
import org.sipfoundry.sipxconfig.phonelog.PhoneLogConfiguration;
import org.sipfoundry.sipxconfig.setting.Setting;
import org.sipfoundry.sipxconfig.test.TestHelper;

public class PhoneLogConfigurationTest extends SipxServiceTestBase {

    public void testWrite() throws Exception {
        PhoneLogConfiguration out = new PhoneLogConfiguration();
        out.setTemplate("sipxconfig/phonelog-config.vm");

        SipxConfigService configService = new SipxConfigService();
        Setting configSettings = TestHelper.loadSettings("sipxconfig/sipxconfig.xml");
        configSettings.getSetting("configserver-config").getSetting("PHONELOG_ENABLED").setValue("TRUE");
        configService.setSettings(configSettings);

        SipxServiceManager sipxServiceManager = EasyMock.createMock(SipxServiceManager.class);
        sipxServiceManager.getServiceByBeanId(SipxConfigService.BEAN_ID);
        EasyMock.expectLastCall().andReturn(configService).atLeastOnce();
        EasyMock.replay(sipxServiceManager);

        out.setSipxServiceManager(sipxServiceManager);

        assertCorrectFileGeneration(out, "expected-phonelog-config");
    }
}
