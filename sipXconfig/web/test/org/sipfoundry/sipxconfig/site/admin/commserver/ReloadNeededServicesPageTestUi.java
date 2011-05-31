/**
 *
 *
 * Copyright (c) 2010 / 2011 eZuce, Inc. All rights reserved.
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
package org.sipfoundry.sipxconfig.site.admin.commserver;

import junit.framework.Test;

import org.sipfoundry.sipxconfig.site.SiteTestHelper;

import net.sourceforge.jwebunit.junit.WebTestCase;

public class ReloadNeededServicesPageTestUi extends WebTestCase {

    public static Test suite() throws Exception {
        return SiteTestHelper.webTestSuite(ReloadNeededServicesPageTestUi.class);
    }

    @Override
    protected void setUp() throws Exception {
        getTestContext().setBaseUrl(SiteTestHelper.getBaseUrl());
        SiteTestHelper.home(getTester());
    }

    public void testDisplay() {
        SiteTestHelper.assertNoUserError(tester);
        clickLink("link:clearRestartNeeded");
        assertLinkNotPresent("reloadNeededLink");

        clickLink("link:markForReload");
        assertLinkPresent("reloadNeededLink");
        clickLink("reloadNeededLink");
        SiteTestHelper.assertNoUserError(tester);

        assertLinkNotPresent("reloadNeededLink");
        assertTablePresent("reloadNeededServices:list");
        assertButtonPresent("services:reload");
        assertButtonPresent("services:ignore");

        int rowCount = SiteTestHelper.getRowCount(tester, "reloadNeededServices:list");
        // at least one service restart is needed
        assertTrue(1 < rowCount);

        SiteTestHelper.selectRow(tester, 0, true);
        submit("services:reload");
        SiteTestHelper.assertNoException(tester);
    }
}
