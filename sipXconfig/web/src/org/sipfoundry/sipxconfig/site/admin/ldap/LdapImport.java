/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.site.admin.ldap;

import org.apache.tapestry.BaseComponent;
import org.apache.tapestry.IPage;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.annotations.ComponentClass;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.sipfoundry.sipxconfig.bulk.ldap.LdapImportManager;
import org.sipfoundry.sipxconfig.bulk.ldap.LdapManager;
import org.sipfoundry.sipxconfig.common.CronSchedule;
import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;
import org.sipfoundry.sipxconfig.components.TapestryUtils;

@ComponentClass(allowBody = false, allowInformalParameters = false)
public abstract class LdapImport extends BaseComponent implements PageBeginRenderListener {

    public abstract LdapImportManager getLdapImportManager();

    public abstract LdapManager getLdapManager();

    public abstract CronSchedule getSchedule();

    public abstract void setSchedule(CronSchedule schedule);

    @Parameter(required = true)
    public abstract int getCurrentConnectionId();

    public void pageBeginRender(PageEvent event) {
        if (getSchedule() == null) {
            setSchedule(getLdapManager().getSchedule(getCurrentConnectionId()));
        }
    }

    public void importLdap() {
        if (!TapestryUtils.isValid(this)) {
            return;
        }
        getLdapImportManager().insert(getCurrentConnectionId());
        SipxValidationDelegate validator = (SipxValidationDelegate) TapestryUtils
                .getValidator(this);
        validator.recordSuccess(getMessages().getMessage("msg.success"));
    }

    public IPage verifyLdap(IRequestCycle cycle) {
        LdapImportPreview ldapImportPreview = (LdapImportPreview) cycle.getPage(LdapImportPreview.PAGE);
        ldapImportPreview.setExample(null);
        ldapImportPreview.setCurrentConnectionId(getCurrentConnectionId());
        return ldapImportPreview;
    }

    public void applySchedule() {
        getLdapManager().setSchedule(getSchedule(), getCurrentConnectionId());
    }
}
