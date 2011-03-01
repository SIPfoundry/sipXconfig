/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */
package org.sipfoundry.sipxconfig.admin.localization;

import org.easymock.EasyMock;
import org.sipfoundry.sipxconfig.IntegrationTestCase;
import org.sipfoundry.sipxconfig.admin.dialplan.DialPlanActivationManager;
import org.sipfoundry.sipxconfig.common.UserException;
import org.sipfoundry.sipxconfig.service.ServiceConfigurator;
import org.sipfoundry.sipxconfig.service.SipxImbotService;
import org.sipfoundry.sipxconfig.service.SipxService;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

public class LocalizationContextTestIntegration extends IntegrationTestCase {

    // Object Under Test
    private LocalizationContext m_out;
    private ServiceConfigurator m_origServiceConfigurator;
    private LocalizationContextImpl m_localizationContextImpl;
    private DialPlanActivationManager m_origDialPlanActivationManager;

    public void testUpdateRegion() throws Exception {
        DialPlanActivationManager dpam = createMock(DialPlanActivationManager.class);
        dpam.replicateDialPlan(false);
        replay(dpam);
        modifyContext(m_localizationContextImpl, "dialPlanActivationManager", m_origDialPlanActivationManager, dpam);

	try {
	    m_out.updateRegion("xx");
	    fail("bad region error expected");
	} catch (UserException expectedE) {
	    assertTrue("Bad region correctly rejected", true);
	}

        m_out.updateRegion("na");
        EasyMock.verify(dpam);
    }

    private void updateLanguage(String language, String languageDirectory) throws Exception {
        ServiceConfigurator sc = createMock(ServiceConfigurator.class);
        sc.initLocations();
        SipxService service = org.easymock.classextension.EasyMock.createMock(SipxService.class);
        service.getBeanId();
        org.easymock.classextension.EasyMock.expectLastCall().andReturn(SipxImbotService.BEAN_ID);
        org.easymock.classextension.EasyMock.replay(service);
        sc.replicateServiceConfig(service);
        EasyMock.expectLastCall().anyTimes();
        replay(sc);

        modifyContext(m_localizationContextImpl, "serviceConfigurator", m_origServiceConfigurator, sc);

        assertEquals(1, m_out.updateLanguage(language));
        flush();
        assertEquals(1, getConnection().getRowCount("localization", "where language = '" + language + "'"));
        assertEquals(languageDirectory, m_out.getCurrentLanguageDir());

        verify(sc);
    }

    public void testUpdateLanguage() throws Exception {
        updateLanguage("pl", "stdprompts_pl");
        // update back to default
        updateLanguage(LocalizationContext.DEFAULT, LocalizationContext.PROMPTS_DEFAULT);
    }

    public void testDefaults() {
        assertEquals("na", m_out.getCurrentRegionId());
        assertEquals("en", m_out.getCurrentLanguage());
    }

    public void setLocalizationContext(LocalizationContext localizationContext) {
        m_out = localizationContext;
    }

    public void setLocalizationContextImpl(LocalizationContextImpl localizationContextImpl) {
        m_localizationContextImpl = localizationContextImpl;
    }

    public void setServiceConfigurator(ServiceConfigurator serviceConfigurator) {
        m_origServiceConfigurator = serviceConfigurator;
    }

    public void setDialPlanActivationManager(DialPlanActivationManager dialPlanActivationManager) {
        m_origDialPlanActivationManager = dialPlanActivationManager;
    }
}
