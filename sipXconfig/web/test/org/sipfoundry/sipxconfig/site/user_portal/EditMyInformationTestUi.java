/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.site.user_portal;

import net.sourceforge.jwebunit.junit.WebTestCase;

import org.sipfoundry.sipxconfig.site.SiteTestHelper;

public class EditMyInformationTestUi extends WebTestCase {

    @Override
    public void setUp() {
        getTestContext().setBaseUrl(SiteTestHelper.getBaseUrl());
        SiteTestHelper.home(getTester());
        SiteTestHelper.setScriptingEnabled(getTester(), false);
        clickLink("seedTestUser");
        clickLink("loginFirstTestUser");
        clickLink("toggleNavigation");
    }

    /**
     * Test that the My Information page displays correctly. The selected tab when the page is
     * first displayed should be the My Information tab
     */
    public void testDisplay() {
        clickLink("menu.myInformation");
        SiteTestHelper.assertNoUserError(tester);

        assertLinkPresent("link:extendedInfo");
        assertElementPresent("contact_information");
    }

    public void testTabNavigation() {
        clickLink("menu.myInformation");
        clickLink("link:menu");
        SiteTestHelper.assertNoUserError(tester);
        assertTextPresent("Override default AutoAttendant language");

        clickLink("link:distributionLists");
        SiteTestHelper.assertNoUserError(tester);
        assertTextPresent("Dialpad");
    }

    public void testAutoAttendantTabDisplay() {
        SiteTestHelper.home(getTester());
        clickLink("loginTestUserWithAutoAttendantPermission");
        clickLink("toggleNavigation");
        clickLink("menu.myInformation");
        assertLinkPresent("link:menu");

        SiteTestHelper.home(getTester());
        clickLink("loginTestUserWithoutAutoAttendantPermission");
        clickLink("toggleNavigation");
        clickLink("menu.myInformation");
        assertLinkNotPresent("link:menu");
    }

    public void testMoHTabDisplay() {
        SiteTestHelper.home(getTester());
        clickLink("loginTestUserWithMoHPermission");
        clickLink("toggleNavigation");
        clickLink("menu.myInformation");
        assertLinkPresent("link:moh");

        SiteTestHelper.home(getTester());
        clickLink("loginTestUserWithoutMoHPermission");
        clickLink("toggleNavigation");
        clickLink("menu.myInformation");
        assertLinkNotPresent("link:moh");
    }

    public void testTabConferencesDisplay() {
        clickLink("menu.myInformation");
        clickLink("link:conferences");
        SiteTestHelper.assertNoUserError(tester);
        assertTextPresent("Conferences");
        assertButtonPresent("conference:lock");
        assertButtonPresent("conference:unlock");
        assertButtonPresent("refresh");
    }

    public void testTabImDisplay() {
        clickLink("menu.myInformation");
        clickLink("link:openfire");
        SiteTestHelper.assertNoException(tester);
        SiteTestHelper.assertNoUserError(tester);
        assertElementPresent("user:imId");
        assertElementPresent("user:imDisplayName");
    }

    public void testTabInfoDisplay() {
        clickLink("menu.myInformation");
        clickLink("link:info");
        SiteTestHelper.assertNoException(tester);
        SiteTestHelper.assertNoUserError(tester);
        assertElementPresent("user:emailAddress");
        // the pin component
        assertElementPresent("cp:password");
        assertElementPresent("cp:confirmPassword");
        assertButtonPresent("form:apply");
    }

    public void testFaxExtension() {
        clickLink("menu.myInformation");
        clickLink("link:info");

        SiteTestHelper.assertNoException(tester);
        SiteTestHelper.assertNoUserError(tester);
        assertElementPresent("user:emailAddress");
        assertElementPresent("user:alternateEmailAddress");

        setTextField("user:emailAddress", "");
        setTextField("user:alternateEmailAddress", "");
        clickButton("form:apply");
        assertElementPresentByXPath("//input[@type = 'text' and @id='faxExtension' and @disabled='disabled']");
        assertElementPresentByXPath("//input[@type = 'text' and @id='faxDid' and @disabled='disabled']");

        setTextField("user:emailAddress", "ciuc@ciuc.com");
        clickButton("form:apply");
        assertElementNotPresentByXPath("//input[@type = 'text' and @id='faxExtension' and @disabled='disabled']");
        assertElementNotPresentByXPath("//input[@type = 'text' and @id='faxDid' and @disabled='disabled']");

        setTextField("user:emailAddress", "");
        setTextField("user:alternateEmailAddress", "lala@blabla.cc");
        clickButton("form:apply");
        assertElementNotPresentByXPath("//input[@type = 'text' and @id='faxExtension' and @disabled='disabled']");
        assertElementNotPresentByXPath("//input[@type = 'text' and @id='faxDid' and @disabled='disabled']");

        setTextField("faxExtension", "400");
        setTextField("faxDid", "+12345678");
        clickButton("form:apply");
        SiteTestHelper.assertNoUserError(tester);
        assertTextFieldEquals("faxExtension", "400");
        assertTextFieldEquals("faxDid", "+12345678");

        setTextField("faxExtension", "&^(*&^(");
        clickButton("form:apply");
        SiteTestHelper.assertUserError(tester);

        setTextField("faxDid", "&^(*&^(");
        clickButton("form:apply");
        SiteTestHelper.assertUserError(tester);

        setTextField("faxExtension", "");
        setTextField("faxDid", "+12345678");
        clickButton("form:apply");
        SiteTestHelper.assertUserError(tester);

    }
}
