/*
 *
 *
 * Copyright (C) 2010 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 */
package org.sipfoundry.sipxconfig.common;

import org.sipfoundry.sipxconfig.systemaudit.ConfigChangeType;

/**
 * Internal user, can be a TLS Peer or auth code
 */
public class InternalUser extends AbstractUser {

    @Override
    public String getEntityIdentifier() {
        return getUserName();
    }

    @Override
    public ConfigChangeType getConfigChangeType() {
        return ConfigChangeType.TLS_PEER;
    }

}
