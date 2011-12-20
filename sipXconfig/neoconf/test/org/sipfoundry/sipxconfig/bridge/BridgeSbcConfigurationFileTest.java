/*
 *
 *
 * Copyright (C) 2009 Pingtel Corp., certain elements licensed under a Contributor Agreement.
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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.easymock.classextension.EasyMock;
import org.junit.Test;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.commserver.LocationsManager;
import org.sipfoundry.sipxconfig.device.DeviceDefaults;
import org.sipfoundry.sipxconfig.device.ProfileLocation;
import org.sipfoundry.sipxconfig.gateway.GatewayContext;
import org.sipfoundry.sipxconfig.gateway.SipTrunk;
import org.sipfoundry.sipxconfig.phone.PhoneTestDriver;
import org.sipfoundry.sipxconfig.proxy.ProxyManager;
import org.sipfoundry.sipxconfig.proxy.ProxySettings;
import org.sipfoundry.sipxconfig.sbc.SbcDescriptor;
import org.sipfoundry.sipxconfig.setting.ModelFilesContext;
import org.sipfoundry.sipxconfig.test.TestHelper;

public class BridgeSbcConfigurationFileTest {

    @Test
    public void testWrite() throws Exception {
//        SipxProxyService proxyService = new SipxProxyService();
//        proxyService.setModelDir("sipxproxy");
//        proxyService.setModelName("sipxproxy.xml");
//        proxyService.setModelFilesContext(TestHelper.getModelFilesContext());
//        proxyService.setBeanName(SipxProxyService.BEAN_ID);
//        m_sipxServiceManager = TestHelper.getMockSipxServiceManager(true, proxyService);
        
        ProxySettings proxySettings = new ProxySettings();
        proxySettings.setModelFilesContext(TestHelper.getModelFilesContext());
        ProxyManager mgr = EasyMock.createMock(ProxyManager.class);
        mgr.getSettings();
        EasyMock.expectLastCall().andReturn(proxySettings).anyTimes();
        EasyMock.replay(mgr);

        ModelFilesContext modelFilesContext = TestHelper.getModelFilesContext();
        DeviceDefaults deviceDefaults = PhoneTestDriver.getDeviceDefaults();
        Location location = TestHelper.createDefaultLocation();
        location.setPublicAddress("192.168.5.240");

        SbcDescriptor descriptor = new SbcDescriptor();
        descriptor.setModelId("sbcSipXbridge");

        BridgeSbc sbc = new BridgeSbc();
        sbc.setLocation(location);
        sbc.setAddress(location.getAddress());
        sbc.setModel(descriptor);
        sbc.setProfileName("sibxbridge.xml");
        sbc.setDefaults(deviceDefaults);
        sbc.setModelFilesContext(modelFilesContext);
        sbc.setPort(5090);
        sbc.setProxyManager(mgr);
        TestHelper.setVelocityProfileGenerator(sbc, TestHelper.getSystemEtcDir());

        GatewayContext gatewayContext = createMock(GatewayContext.class);
        gatewayContext.getGatewayByType(SipTrunk.class);
        expectLastCall().andReturn(Collections.emptyList()).anyTimes();

        LocationsManager locationsManager = createMock(LocationsManager.class);
        locationsManager.getLocationByAddress(location.getAddress());
        expectLastCall().andReturn(location);
        replay(gatewayContext, locationsManager);

        sbc.setGatewayContext(gatewayContext);
        sbc.setLocationsManager(locationsManager);
        
        
        final ByteArrayOutputStream actual = new ByteArrayOutputStream();
        ProfileLocation profiles = new ProfileLocation() {
            @Override
            public void removeProfile(String profileName) {
            }
            
            @Override
            public OutputStream getOutput(String profileName) {
                return actual;
            }
            
            @Override
            public void closeOutput(OutputStream stream) {
                IOUtils.closeQuietly(stream);
            }
        }; 
        sbc.generateFiles(profiles);
        InputStream expected = getClass().getResourceAsStream("sipxbridge-no-itsp.test.xml");
        assertEquals(IOUtils.toString(expected), actual.toString());
    }
}
