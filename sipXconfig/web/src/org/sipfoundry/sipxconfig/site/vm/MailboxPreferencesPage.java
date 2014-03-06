/*
 *
 *
 * Copyright (C) 2009 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */
package org.sipfoundry.sipxconfig.site.vm;

import java.text.MessageFormat;

import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.event.PageEvent;
import org.apache.tapestry.valid.IValidationDelegate;
import org.apache.tapestry.valid.ValidationConstraint;
import org.sipfoundry.sipxconfig.common.DuplicateEntity;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.components.FaxServicePanel;
import org.sipfoundry.sipxconfig.components.TapestryUtils;
import org.sipfoundry.sipxconfig.site.user_portal.UserBasePage;
import org.sipfoundry.sipxconfig.vm.MailboxPreferences;

public abstract class MailboxPreferencesPage extends UserBasePage {
    public static final String PAGE = "vm/MailboxPreferencesPage";

    @Persist
    public abstract boolean isAdvanced();

    public abstract MailboxPreferences getMailboxPreferences();

    public abstract void setMailboxPreferences(MailboxPreferences preferences);

    public abstract User getEditedUser();

    public abstract void setEditedUser(User user);

    @Override
    public void pageBeginRender(PageEvent event) {
        super.pageBeginRender(event);
        if (getEditedUser() == null) {
            setEditedUser(getUser());
        }
        if (getMailboxPreferences() == null) {
            setMailboxPreferences(new MailboxPreferences(getEditedUser()));
        }
    }

    public void onApply() {
        User user = getEditedUser();
        checkForUserIdOrAliasCollision();
        getMailboxPreferences().updateUser(user);
        FaxServicePanel fs = (FaxServicePanel) getComponent("faxServicePanel");
        fs.update(user);
        getCoreContext().saveUser(user);
    }

    // Make sure that the user ID, Fax extension, and aliases don't collide with any other
    // user IDs or aliases. Report an error if there is a collision.
    private boolean checkForUserIdOrAliasCollision() {
        boolean result = false;
        DuplicateEntity dup = getCoreContext().checkForDuplicateNameOrAlias(getEditedUser());
        if (dup != null && dup.getValue() != null) {
            result = true;
            recordError("message.duplicateUserIdOrAlias", dup.getValue());
        }
        return result;
    }

    private void recordError(String messageId, String arg) {
        IValidationDelegate delegate = TapestryUtils.getValidator(getPage());
        String message = null;
        if (arg != null) {
            message = MessageFormat.format(getMessages().getMessage(messageId), arg);
        } else {
            message = getMessages().getMessage(messageId);
        }
        delegate.record(message, ValidationConstraint.CONSISTENCY);
    }

}
