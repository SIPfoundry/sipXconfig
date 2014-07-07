/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.phone.polycom;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.replay;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.easymock.EasyMock;
import org.sipfoundry.sipxconfig.address.Address;
import org.sipfoundry.sipxconfig.address.AddressManager;
import org.sipfoundry.sipxconfig.address.AddressType;
import org.sipfoundry.sipxconfig.device.FileSystemProfileLocation;
import org.sipfoundry.sipxconfig.device.VelocityProfileGenerator;
import org.sipfoundry.sipxconfig.feature.FeatureManager;
import org.sipfoundry.sipxconfig.moh.MusicOnHoldManager;
import org.sipfoundry.sipxconfig.mwi.Mwi;
import org.sipfoundry.sipxconfig.phone.Line;
import org.sipfoundry.sipxconfig.phone.LineInfo;
import org.sipfoundry.sipxconfig.phone.PhoneTestDriver;
import org.sipfoundry.sipxconfig.phone.polycom.PolycomPhone.FormatFilter;
import org.sipfoundry.sipxconfig.rls.Rls;
import org.sipfoundry.sipxconfig.setting.Setting;
import org.sipfoundry.sipxconfig.test.TestHelper;

public class PolycomPhoneTest extends TestCase {

    private PolycomPhone m_phone;

    private PhoneTestDriver m_tester;

    private FileSystemProfileLocation m_location;

    private final String m_root = TestHelper.getTestDirectory() + "/testPolycom";

    @Override
    protected void setUp() {
        FeatureManager featureManagerMock = createMock(FeatureManager.class);
        featureManagerMock.isFeatureEnabled(Mwi.FEATURE);
        EasyMock.expectLastCall().andReturn(true).anyTimes();

        featureManagerMock.isFeatureEnabled(MusicOnHoldManager.FEATURE);
        EasyMock.expectLastCall().andReturn(true).anyTimes();

        featureManagerMock.isFeatureEnabled(Rls.FEATURE);
        EasyMock.expectLastCall().andReturn(true).anyTimes();

        m_phone = new PolycomPhone();
        PolycomModel model = new PolycomModel();
        model.setDefaultVersion(PolycomModel.VER_3_1_X);
        m_phone.setModel(model);
        model.setModelId("polycom600");
        Set<String> features = new HashSet<String>();
        features.add("disableCallList");
        features.add("intercom");
        features.add("voiceQualityMonitoring");
        features.add("OTHERS_CodecPref");
        features.add("pre_3.2.0_model");
        model.setSupportedFeatures(features);
        m_tester = PhoneTestDriver.supplyTestData(m_phone,true,false,false,true);
        m_location = new FileSystemProfileLocation();
        m_location.setParentDir(m_root);

        VelocityProfileGenerator profileGenerator = TestHelper.getProfileGenerator(TestHelper.getEtcDir());
        m_phone.setProfileGenerator(profileGenerator);
        m_phone.setFeatureManager(featureManagerMock);
        EasyMock.replay(featureManagerMock);
    }

    // Firmware version 1.6 is no longer supported. For firmware version 2.0 and beyound,
    // as the formats are compatible, thus version selection is no longer needed at this point.
    public void testVersionArray() {
        // assertSame(new PolycomModel().getVersions()[0], PolycomModel.VER_1_6);
    }

    public void testGenerateProfiles() throws Exception {
        AddressManager addressManager = createMock(AddressManager.class);
        addressManager.getAddresses(new AddressType("provisionService", "http://%s:%d/"));
        expectLastCall().andReturn(new ArrayList<Address>()).anyTimes();
        replay(addressManager);
        m_phone.setAddressManager(addressManager);
        ApplicationConfiguration cfg = new ApplicationConfiguration(m_phone);
        m_phone.generateProfiles(m_location);

        File phonebook = new File(m_root, cfg.getDirectoryFilename());
        assertTrue(phonebook.exists());

        // content of profiles is tested in individual base classes of ConfigurationTemplate
        File appFile = new File(m_root, cfg.getAppFilename());
        assertTrue(appFile.exists());

        File phoneFile = new File(m_root, cfg.getPhoneFilename());
        assertTrue(phoneFile.exists());

        File sipFile = new File(m_root, cfg.getSipFilename());
        assertTrue(sipFile.exists());

        File deviceFile = new File(m_root, cfg.getDeviceFilename());
        assertTrue(deviceFile.exists());


        m_phone.removeProfiles(m_location);
        assertTrue(phonebook.exists());
        assertFalse(appFile.exists());
        assertFalse(phoneFile.exists());
        assertFalse(sipFile.exists());
        assertFalse(deviceFile.exists());
    }

    public void testRestartFailureNoLine() throws Exception {
        m_phone.getLines().clear();
        m_phone.restart();
        m_tester.getSipControl().verify();
    }

    public void testRestart() throws Exception {

        String uri = m_phone.getLine(0).getUri();
        assertEquals("\"Joe User\"<sip:juser@sipfoundry.org>", uri);

        m_phone.restart();
        m_tester.getSipControl().verify();
    }

    public void testLineDefaults() throws Exception {
        Setting settings = m_tester.getPrimaryLine().getSettings();
        Setting address = settings.getSetting("reg/server/1/address");
        assertEquals("sipfoundry.org", address.getValue());

        Setting missedCallTracking = settings.getSetting("call/missedCallTracking/enabled");
        assertTrue((Boolean)missedCallTracking.getTypedValue());
    }

    public void testLineDefaultsNoUser() throws Exception {
        Line secondLine = m_phone.createLine();
        m_phone.addLine(secondLine);
        Setting settings = secondLine.getSettings();
        Setting userId = settings.getSetting("reg/auth.userId");
        assertEquals(null, userId.getValue());
    }

    public void testFormat() throws Exception {
        InputStream in = getClass().getResourceAsStream("unformatted.xml");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        FormatFilter format = new PolycomPhone.FormatFilter();
        format.copy(in, out);
        Reader expected = new InputStreamReader(getClass().getResourceAsStream("formatted.xml"));
        Reader actual = new StringReader(out.toString());
        IOUtils.contentEquals(expected, actual);
    }

    public void testExternalLine() throws Exception {

        LineInfo li = new LineInfo();
        li.setDisplayName("George Bush");
        li.setUserId("2000");
        li.setRegistrationServer("example.org");
        li.setPassword("1234");
        li.setVoiceMail("1001");

        Line externalLine = m_phone.createLine();
        m_phone.addLine(externalLine);
        externalLine.setLineInfo(li);

        assertEquals("\"George Bush\"<sip:2000@example.org>", externalLine.getUri());
        assertEquals("2000", externalLine.getSettingValue("msg.mwi/subscribe"));
        assertEquals("contact", externalLine.getSettingValue("msg.mwi/callBackMode"));
        assertEquals("1001", externalLine.getSettingValue("msg.mwi/callBack"));
    }

}
