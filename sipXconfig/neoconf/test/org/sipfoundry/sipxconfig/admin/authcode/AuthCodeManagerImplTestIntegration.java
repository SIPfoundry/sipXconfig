/*
 *
 *
 * Copyright (C) 2010 Avaya, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.admin.authcode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.dbunit.dataset.ITable;
import org.sipfoundry.sipxconfig.IntegrationTestCase;
import org.sipfoundry.sipxconfig.common.CoreContext;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.common.UserException;
import org.sipfoundry.sipxconfig.permission.PermissionName;

public class AuthCodeManagerImplTestIntegration extends IntegrationTestCase {

    private AuthCodeManager m_authCodeManager;
    private CoreContext m_coreContext;

    public void testDeleteAuthCode() throws Exception {
        loadDataSet("admin/authcode/auth_code.db.xml");

        assertEquals(2, m_authCodeManager.getAuthCodes().size());

        AuthCode code = m_authCodeManager.getAuthCode(1);
        assertNotNull(code);
        m_authCodeManager.deleteAuthCode(code);

        assertEquals(1, m_authCodeManager.getAuthCodes().size());
    }

    public void testDeleteAuthCodes() throws Exception {
        loadDataSet("admin/authcode/auth_code.db.xml");

        assertEquals(2, m_authCodeManager.getAuthCodes().size());
        assertNotNull(m_authCodeManager.getAuthCodeByCode("12345"));

        Collection<Integer> ids = new ArrayList<Integer>();
        ids.add(1);
        ids.add(2);

        m_authCodeManager.deleteAuthCodes(ids);

        assertEquals(0, m_authCodeManager.getAuthCodes().size());
    }

    public void testNewAuthCode() throws Exception {
        AuthCode authCode = m_authCodeManager.newAuthCode();
        assertNotNull(authCode.getInternalUser().getSipPassword());
        assertEquals(false, authCode.getInternalUser().getSettingTypedValue(PermissionName.VOICEMAIL.getPath()));
        assertEquals(false,
                authCode.getInternalUser().getSettingTypedValue(PermissionName.FREESWITH_VOICEMAIL.getPath()));
    }

    public void saveAuthCode() throws Exception {
        loadDataSet("admin/tls/auth_code.db.xml");

        assertEquals(2, m_authCodeManager.getAuthCodes().size());

        AuthCode authCode = m_authCodeManager.newAuthCode();
        m_authCodeManager.saveAuthCode(authCode);

        assertEquals(3, m_authCodeManager.getAuthCodes().size());
        assertEquals("~~ac~3", authCode.getInternalUser().getUserName());

        AuthCode authCode1 = m_authCodeManager.getAuthCodeByCode("12345");
        m_authCodeManager.saveAuthCode(authCode1);
        assertEquals("~~ac~4", authCode1.getInternalUser().getUserName());

        AuthCode authCode2 = m_authCodeManager.getAuthCodeByCode("67890");
        try {
            m_authCodeManager.saveAuthCode(authCode2);
            fail();
        } catch (UserException ex) {

        }

        // check alias
        User user = m_coreContext.newUser();
        user.setAliasesString("*81");
        user.setUserName("user1");
        user.setPin("123", "domain.com");
        user.setPintoken("123");
        try {
            m_coreContext.saveUser(user);
            fail();
        } catch (UserException e) {
            assertEquals("The user ID or alias \"*81\" duplicates an existing alias for a user or service",
                    e.getMessage());
        }

    }

    public void setAuthCodeManager(AuthCodeManager authCodeManager) {
        m_authCodeManager = authCodeManager;
    }

    public void setCoreContext(CoreContext coreContext) {
        m_coreContext = coreContext;
    }

}
