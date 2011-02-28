/*
 *
 *
 * Copyright (C) 2011 eZuce, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package org.sipfoundry.sipxconfig.service;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.sipfoundry.sipxconfig.TestHelper;
import org.sipfoundry.sipxconfig.setting.Setting;

public class SipxOpenAcdVmArgsConfigurationTest extends SipxServiceTestBase {

    public void testWrite() throws Exception {
        SipxOpenAcdService openAcdService = new SipxOpenAcdService();
        initCommonAttributes(openAcdService);
        Setting settings = TestHelper.loadSettings("openacd/sipxopenacd.xml");
        openAcdService.setSettings(settings);

        SipxServiceManager sipxServiceManager = createMock(SipxServiceManager.class);
        sipxServiceManager.getServiceByBeanId(SipxOpenAcdService.BEAN_ID);
        expectLastCall().andReturn(openAcdService).atLeastOnce();
        replay(sipxServiceManager);

        SipxOpenAcdVmArgsConfiguration out = new SipxOpenAcdVmArgsConfiguration();
        out.setEtcDir("/home/sipxecs/etc");
        out.setSipxServiceManager(sipxServiceManager);
        out.setTemplate("openacd/vm.args.vm");

        assertCorrectFileGeneration(out, "expected-vm-args-config");

    }
}
