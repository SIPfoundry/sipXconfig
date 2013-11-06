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

import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.event.PageEvent;
import org.sipfoundry.sipxconfig.common.SipUri;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.domain.DomainManager;
import org.sipfoundry.sipxconfig.phonebook.PhonebookManager;
import org.sipfoundry.sipxconfig.phonebook.PhonebookManager.PhonebookFormat;
import org.sipfoundry.sipxconfig.sip.SipService;

public abstract class UserPhonebookPage extends UserBasePage {

    @InjectObject("spring:sip")
    public abstract SipService getSipService();

    @InjectObject("spring:domainManager")
    public abstract DomainManager getDomainManager();

    @InjectObject("spring:phonebookManager")
    public abstract PhonebookManager getPhonebookManager();

    public abstract User getLoadedUser();

    public abstract void setLoadedUser(User user);

    @Override
    public void pageBeginRender(PageEvent event) {
        super.pageBeginRender(event);

        if (getLoadedUser() == null) {
            setLoadedUser(getUser());
        }
    }

    public PhonebookFormat getVcardFormat() {
        return PhonebookFormat.VCARD;
    }

    public PhonebookFormat getCsvFormat() {
        return PhonebookFormat.CSV;
    }

    public String getWidgetSrc() {
        return getRequestCycle().getAbsoluteURL('/' + UserPhonebookWidgetPage.PAGE + ".html").replaceFirst(
                "http://", "https://");
    }

    /**
     * Implements click to call link
     *
     * @param number number to call - refer is sent to current user
     */
    public void call(String number) {
        String domain = getDomainManager().getDomain().getName();
        String userAddrSpec = getUser().getAddrSpec(domain);
        String destAddrSpec = SipUri.fix(number, domain);
        String displayName = "ClickToCall";
        getSipService().sendRefer(getUser(), userAddrSpec, displayName, destAddrSpec);
    }

}
