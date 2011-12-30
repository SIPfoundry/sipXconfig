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
package org.sipfoundry.sipxconfig.site.admin.ldap;

import org.apache.tapestry.BaseComponent;
import org.apache.tapestry.annotations.ComponentClass;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.apache.tapestry.form.IPropertySelectionModel;
import org.sipfoundry.sipxconfig.bulk.ldap.LdapManager;
import org.sipfoundry.sipxconfig.bulk.ldap.LdapSystemSettings;
import org.sipfoundry.sipxconfig.components.EnumPropertySelectionModel;
import org.sipfoundry.sipxconfig.components.LocalizedOptionModelDecorator;

@ComponentClass(allowBody = false, allowInformalParameters = false)
public abstract class LdapSettings extends BaseComponent implements PageBeginRenderListener {

    @InjectObject("spring:ldapManager")
    public abstract LdapManager getLdapManager();

    public abstract LdapSystemSettings getSettings();

    public abstract void setSettings(LdapSystemSettings settings);

    public abstract IPropertySelectionModel getAuthenticationModel();

    public abstract void setAuthenticationModel(IPropertySelectionModel model);

    public void pageBeginRender(PageEvent event_) {
        setSettings(getLdapManager().getSystemSettings());
        if (getAuthenticationModel() == null) {
            setAuthenticationModel(createAuthenticationModel());
        }
    }

    public void ok() {
        LdapManager ldapManager = getLdapManager();
        ldapManager.saveSystemSettings(getSettings());

        // write openfire.xml file /  mark sipxopenfire service for restart
//        ldapManager.replicateOpenfireConfig();
    }

    private IPropertySelectionModel createAuthenticationModel() {
        EnumPropertySelectionModel rawModel = new EnumPropertySelectionModel();
        rawModel.setEnumClass(LdapSystemSettings.AuthenticationOptions.class);
        return new LocalizedOptionModelDecorator(rawModel, getMessages(), "authentication.");
    }
}
