/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.speeddial;

import java.util.Collection;
import java.util.Collections;

import org.sipfoundry.sipxconfig.cfgmgt.DeployConfigOnEdit;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.feature.Feature;
import org.sipfoundry.sipxconfig.rls.Rls;
import org.sipfoundry.sipxconfig.systemaudit.ConfigChangeType;
import org.sipfoundry.sipxconfig.systemaudit.SystemAuditable;

/**
 * Collection of speeddial buttons associated with the user.
 */
public class SpeedDial extends SpeedDialButtons implements DeployConfigOnEdit, SystemAuditable {
    private User m_user;

    public User getUser() {
        return m_user;
    }

    public void setUser(User user) {
        m_user = user;
    }

    /**
     * Returns the URL of the resource list corresponding to this speed dial.
     *
     * @param consolidated needs to be set to true for phones that are not RFC compliant and
     *        expect "consolidated" view of resource list (see XCF-1453)
     */
    public String getResourceListId(boolean consolidated) {
        return getResourceListId(getUser().getUserName(), consolidated);
    }

    public static String getResourceListId(String name, boolean consolidated) {
        StringBuilder listId = new StringBuilder("~~rl~");
        listId.append(consolidated ? 'C' : 'F');
        listId.append('~');
        listId.append(name);
        return listId.toString();
    }

    public String getResourceListName() {
        return getUser().getUserName();
    }

    /**
     * If at least one button supports BLF we need to register this list as BLF
     */
    public boolean isBlf() {
        for (Button button : getButtons()) {
            if (button.isBlf()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Collection<Feature> getAffectedFeaturesOnChange() {
        return Collections.singleton((Feature) Rls.FEATURE);
    }

    @Override
    public String getEntityIdentifier() {
        return m_user.getEntityIdentifier();
    }

    @Override
    public ConfigChangeType getConfigChangeType() {
        return ConfigChangeType.SPEED_DIAL;
    }
}
