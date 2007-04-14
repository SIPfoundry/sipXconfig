/*
 * 
 * 
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.  
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 * 
 * $
 */
package org.sipfoundry.sipxconfig.phone.mitel;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.sipfoundry.sipxconfig.TestHelper;
import org.sipfoundry.sipxconfig.device.MemoryProfileLocation;
import org.sipfoundry.sipxconfig.phone.PhoneContext;
import org.sipfoundry.sipxconfig.phone.PhoneTestDriver;

public class MitelPhoneTest extends XMLTestCase {
    public void _testFactoryRegistered() {
        PhoneContext pc = (PhoneContext) TestHelper.getApplicationContext().getBean(
                PhoneContext.CONTEXT_BEAN_NAME);
        assertNotNull(pc.newPhone(new MitelModel()));
    }

    public void testGetFileName() throws Exception {
        MitelPhone phone = new MitelPhone();
        phone.setSerialNumber("001122334455");
        assertEquals("mn_001122334455.txt", phone.getPhoneFilename());
    }

    public void testGenerateTypicalProfile() throws Exception {
        MitelPhone phone = new MitelPhone(new MitelModel());

        // call this to inject dummy data
        PhoneTestDriver.supplyTestData(phone);
        MemoryProfileLocation location = TestHelper.setVelocityProfileGenerator(phone);

        phone.generateProfiles();
        InputStream expectedProfile = getClass().getResourceAsStream("mn.txt");
        assertNotNull(expectedProfile);

        // System.err.println(location.toString());

        XMLUnit.setIgnoreWhitespace(true);
        assertXMLEqual(new InputStreamReader(expectedProfile), location.getReader());
    }
}
