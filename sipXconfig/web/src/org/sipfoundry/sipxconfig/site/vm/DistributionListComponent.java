/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.site.vm;

import org.apache.tapestry.BaseComponent;
import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.annotations.Lifecycle;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.bean.EvenOdd;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.common.UserException;
import org.sipfoundry.sipxconfig.components.RowInfo;
import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;
import org.sipfoundry.sipxconfig.components.TapestryUtils;
import org.sipfoundry.sipxconfig.vm.DistributionList;
import org.sipfoundry.sipxconfig.vm.MailboxManager;

public abstract class DistributionListComponent extends BaseComponent implements PageBeginRenderListener {

    @Parameter(required = true)
    public abstract User getUser();

    @InjectObject(value = "spring:mailboxManager")
    public abstract MailboxManager getMailboxManager();

    @Bean()
    public RowInfo getRowInfo() {
        return RowInfo.UNSELECTABLE;
    }

    @Bean(lifecycle = Lifecycle.PAGE)
    public abstract EvenOdd getRowClass();

    public String getExtensionsString() {
        return TapestryUtils.joinBySpace(getDistributionList().getExtensions());
    }

    public abstract DistributionList[] getDistributionLists();
    public abstract void setDistributionLists(DistributionList[] distributionLists);
    public abstract DistributionList getDistributionList();


    public void pageBeginRender(PageEvent event) {
        DistributionList[] lists = getDistributionLists();
        if (lists == null) {
            try {
                lists = getMailboxManager().loadDistributionLists(getUser());
                setDistributionLists(lists);
            } catch (UserException e) {
                SipxValidationDelegate validator = (SipxValidationDelegate) TapestryUtils.getValidator(getPage());
                validator.record(e, getMessages());
                setDistributionLists(DistributionList.createBlankList());
            }
        }
    }

    public void save() {
        if (TapestryUtils.isValid(this)) {
            getMailboxManager().saveDistributionLists(getUser().getId(), getDistributionLists());
            setDistributionLists(null);
        }
    }

    public void setExtensionsString(String extensions) {
        String[] ext = TapestryUtils.splitBySpace(extensions);
        if (ext == null) {
            ext = new String[0];
        }
        getDistributionList().setExtensions(ext);
    }
}
