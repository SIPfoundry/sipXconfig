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

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.sipfoundry.sipxconfig.TestHelper;

public class AutoAttendantTest extends TestCase {

    public void testGetSystemName() {
        AutoAttendant aa = new AutoAttendant();
        assertEquals("xcf-1", aa.getSystemName());
        assertFalse(aa.isAfterhour());
        assertFalse(aa.isOperator());
        assertFalse(aa.isPermanent());

        AutoAttendant operator = new AutoAttendant();
        operator.setSystemId(AutoAttendant.OPERATOR_ID);
        assertEquals("operator", operator.getSystemName());
        assertFalse(operator.isAfterhour());
        assertTrue(operator.isOperator());
        assertTrue(operator.isPermanent());

        AutoAttendant afterhour = new AutoAttendant();
        afterhour.setSystemId(AutoAttendant.AFTERHOUR_ID);
        assertEquals("afterhour", afterhour.getSystemName());
        assertTrue(afterhour.isAfterhour());
        assertFalse(afterhour.isOperator());
        assertTrue(afterhour.isPermanent());
    }

    public void testGetIdFromSystemId() {
        assertEquals(1, AutoAttendant.getIdFromSystemId("xcf-1").intValue());
        assertNull(AutoAttendant.getIdFromSystemId("operator"));
    }

    public void testUpdatePrompt() {
        AutoAttendant aa = new AutoAttendant();
        File promptFile = TestHelper.getResourceAsFile(getClass(), "prompt.txt");
        aa.setPromptsDirectory(TestHelper.getTestOutputDirectory());
        aa.setPrompt("prompt.txt");
        try {
            aa.updatePrompt(promptFile.getParentFile());
            File f = new File(TestHelper.getTestOutputDirectory() + "/prompt.txt");
            assertTrue(f.exists());
            if (f.exists()) {
                f.delete();
            }
        } catch (IOException ex) {
            assertTrue(false);
        }
    }
}
