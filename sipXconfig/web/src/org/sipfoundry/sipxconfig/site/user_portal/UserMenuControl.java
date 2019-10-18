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

import java.util.Arrays;
import java.util.List;

public class UserMenuControl {
    public static final String CONTEXT_BEAN_NAME = "userMenu";

    // overrideable in skin
    private List<String> m_hideMenus;
    private List<String> m_hideInfoTabs;
    private List<String> m_hideInfoFields;
    private List<String> m_hideOptions;

    public UserMenuControl() {

    }

    public boolean isHideMenu(String menu) {
        if (m_hideMenus != null) {
            return m_hideMenus.contains(menu);
        }

        return false;
    }

    public boolean isHideInfoTab(String tab) {
        if (m_hideInfoTabs != null) {
            return m_hideInfoTabs.contains(tab);
        }

        return false;
    }

    public boolean isHideInfoField(String field) {
        if (m_hideInfoFields != null) {
            return m_hideInfoFields.contains(field);
        }

        return false;
    }

    public boolean isHideOptions(String field) {
        if (m_hideOptions != null) {
            return m_hideOptions.contains(field);
        }

        return false;
    }

    public void setHideMenus(String menu) {
        if (menu != null) {
            m_hideMenus = Arrays.asList(menu.split(","));
        }
    }

    public void setHideInfoTabs(String tab) {
        if (tab != null) {
            m_hideInfoTabs = Arrays.asList(tab.split(","));
        }
    }

    public void setHideInfoFields(String field) {
        if (field != null) {
            m_hideInfoFields = Arrays.asList(field.split(","));
        }
    }

    public void setHideOptions(String field) {
        if (field != null) {
            m_hideOptions = Arrays.asList(field.split(","));
        }
    }
}
