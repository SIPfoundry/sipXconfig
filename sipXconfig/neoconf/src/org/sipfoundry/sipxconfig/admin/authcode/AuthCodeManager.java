/*
 *
 *
 * Copyright (C) 2010 Avaya, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */

package org.sipfoundry.sipxconfig.admin.authcode;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.sipfoundry.sipxconfig.alias.AliasOwner;
import org.sipfoundry.sipxconfig.common.DataObjectSource;
import org.sipfoundry.sipxconfig.common.ReplicableProvider;

public interface AuthCodeManager extends DataObjectSource, AliasOwner, ReplicableProvider {

    String CONTEXT_BEAN_NAME = "authCodeManager";

    AuthCode getAuthCode(Integer authCodeId);

    void saveAuthCode(AuthCode authCode);

    void deleteAuthCode(AuthCode authCode);

    void deleteAuthCodes(Collection<Integer> allSelected);

    List<AuthCode> getAuthCodes();

    List<AuthCode> getAuthCodes(Collection<Integer> ids);

    AuthCode getAuthCodeByCode(String code);

    AuthCode newAuthCode();

    //from DataSource interface
    Object load(Class c, Serializable id);
}
