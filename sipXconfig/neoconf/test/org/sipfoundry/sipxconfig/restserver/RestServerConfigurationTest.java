/**
 *
 *
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
package org.sipfoundry.sipxconfig.restserver;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.domain.Domain;
import org.sipfoundry.sipxconfig.test.TestHelper;


public class RestServerConfigurationTest {

    @Test
    public void testConfig() throws Exception {
        RestConfiguration config = new RestConfiguration();
        config.setVelocityEngine(TestHelper.getVelocityEngine());
        RestServerSettings settings = new RestServerSettings();
        settings.setModelFilesContext(TestHelper.getModelFilesContext());
        Domain domain = new Domain("example.org");
        Location location = TestHelper.createDefaultLocation();
        StringWriter actual = new StringWriter();
        config.write(actual, settings, location, domain,"192.168.1.100", "192.168.1.101");
        String expected = IOUtils.toString(getClass().getResourceAsStream("expected-sipxrest-config"));
        assertEquals(expected, actual.toString());
    }
}
