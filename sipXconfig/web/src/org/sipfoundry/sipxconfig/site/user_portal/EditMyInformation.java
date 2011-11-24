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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.tapestry.annotations.InitialValue;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.components.Block;
import org.apache.tapestry.event.PageEvent;
import org.apache.tapestry.form.IPropertySelectionModel;
import org.sipfoundry.sipxconfig.admin.update.XmppContactInformationUpdate;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.common.UserException;
import org.sipfoundry.sipxconfig.components.FaxServicePanel;
import org.sipfoundry.sipxconfig.components.TapestryUtils;
import org.sipfoundry.sipxconfig.conference.Conference;
import org.sipfoundry.sipxconfig.conference.ConferenceBridgeContext;
import org.sipfoundry.sipxconfig.openacd.OpenAcdAgent;
import org.sipfoundry.sipxconfig.openacd.OpenAcdContext;
import org.sipfoundry.sipxconfig.permission.Permission;
import org.sipfoundry.sipxconfig.service.SipxImbotService;
import org.sipfoundry.sipxconfig.service.SipxServiceManager;
import org.sipfoundry.sipxconfig.setting.Setting;
import org.sipfoundry.sipxconfig.site.user.EditPinComponent;
import org.sipfoundry.sipxconfig.site.user.UserForm;
import org.sipfoundry.sipxconfig.vm.MailboxManager;
import org.sipfoundry.sipxconfig.vm.MailboxPreferences;
import org.sipfoundry.sipxconfig.vm.attendant.PersonalAttendant;

public abstract class EditMyInformation extends UserBasePage implements EditPinComponent {
    public static final String TAB_CONFERENCES = "conferences";
    public static final String TAB_OPENACD = "openAcd";

    //private static final String OPERATOR_SETTING = "personal-attendant" + Setting.PATH_DELIM + "operator";

    @InjectObject(value = "spring:mailboxManager")
    public abstract MailboxManager getMailboxManager();

    @InjectObject(value = "spring:conferenceBridgeContext")
    public abstract ConferenceBridgeContext getConferenceBridgeContext();

    @InjectObject("spring:sipxServiceManager")
    public abstract SipxServiceManager getSipxServiceManager();

    @InjectObject("spring:xmppContactInformationUpdate")
    public abstract XmppContactInformationUpdate getXmppContactInformationUpdate();

    @InjectObject("spring:openAcdContext")
    public abstract OpenAcdContext getOpenAcdContext();

    public abstract String getPin();

    public abstract Conference getCurrentRow();

    public abstract void setCurrentRow(Conference currentRow);

    public abstract User getUserForEditing();

    public abstract void setUserForEditing(User user);

    public abstract MailboxPreferences getMailboxPreferences();

    public abstract void setMailboxPreferences(MailboxPreferences preferences);

    public abstract IPropertySelectionModel getLanguageList();

    public abstract void setLanguageList(IPropertySelectionModel languageList);

    @Persist
    public abstract PersonalAttendant getPersonalAttendant();

    public abstract void setPersonalAttendant(PersonalAttendant pa);

    public abstract Collection<String> getAvailableTabNames();

    public abstract void setAvailableTabNames(Collection<String> tabNames);

    @Persist
    @InitialValue(value = "literal:extendedInfo")
    public abstract String getTab();

    public abstract Block getActionBlockForConferencesTab();

    public abstract void setActionBlockForConferencesTab(Block b);

    public abstract Setting getImNotificationSettings();

    public abstract void setImNotificationSettings(Setting paSetting);

    public abstract String getOpenAcdPin();

    public abstract void setOpenAcdPin(String pin);

    public abstract Setting getParentSetting();
    public abstract void setParentSetting(Setting setting);

    public void save() {
        if (!TapestryUtils.isValid(this)) {
            return;
        }

        User user = getUserForEditing();
        UserForm.updatePin(this, user, getCoreContext().getAuthorizationRealm());

        FaxServicePanel fs = (FaxServicePanel) getComponent("faxServicePanel");
        fs.update(user);
        getMailboxPreferences().updateUser(user);
        getMailboxManager().storePersonalAttendant(getPersonalAttendant());
        getCoreContext().saveUser(user);
    }

    @Override
    public void pageBeginRender(PageEvent event) {
        super.pageBeginRender(event);

        if (getAvailableTabNames() == null) {
            initAvailableTabs();
        }

        User user = getUserForEditing();
        if (user == null) {
            user = getUser();
            Setting personalAttendantSetting = user.getSettings().getSetting("personal-attendant");
            setParentSetting(personalAttendantSetting);
            setUserForEditing(user);
        }

        UserForm.initializePin(getComponent("pin"), this, user);

        MailboxManager mailMgr = getMailboxManager();
        if (getMailboxPreferences() == null && mailMgr.isEnabled()) {
            setMailboxPreferences(new MailboxPreferences(user));
        }

        if (getImNotificationSettings() == null) {
            setImNotificationSettings(getUser().getSettings().getSetting("im_notification"));
        }

        PersonalAttendant personalAttendant = getPersonalAttendant();
        if (personalAttendant == null) {
            PersonalAttendant pa = mailMgr.loadPersonalAttendantForUser(user);
            setPersonalAttendant(pa);
        }

        if (getTab().equals(TAB_CONFERENCES)) {
            Block b = (Block) getComponent("userConferencesPanel").getComponent("conferencesPanel").getComponent(
                    "conferenceActions");
            setActionBlockForConferencesTab(b);
        }

        if (getTab().equals(TAB_OPENACD)) {
            OpenAcdAgent agent = getOpenAcdContext().getAgentByUser(user);
            setOpenAcdPin(agent.getPin());
        }
    }

    public void syncXmppContacts() {
        try {
            getXmppContactInformationUpdate().notifyChange(getUser());
        } catch (Exception e) {
            throw new UserException(getMessages().getMessage("xmpp.sync.error"), e.getMessage());
        }
    }

    private void initAvailableTabs() {
        List<String> tabNames = new ArrayList<String>();
        tabNames.add("extendedInfo");
        tabNames.add("info");
        tabNames.add("distributionLists");
        tabNames.add(TAB_CONFERENCES);
        tabNames.add("openfire");

        String mohPermissionValue = getUser().getSettingValue("permission/application/music-on-hold");
        if (Permission.isEnabled(mohPermissionValue)) {
            tabNames.add("moh");
        }

        String paPermissionValue = getUser().getSettingValue("permission/application/personal-auto-attendant");
        if (Permission.isEnabled(paPermissionValue)) {
            tabNames.add("menu");
        }

        if (getSipxServiceManager().getServiceByBeanId(SipxImbotService.BEAN_ID).isAvailable()) {
            tabNames.add("myAssistant");
        }

        if (getOpenAcdContext().isOpenAcdAgent(getUser())) {
            tabNames.add(TAB_OPENACD);
        }

        setAvailableTabNames(tabNames);
    }

    public void saveOpenAcdPin() {
        OpenAcdAgent agent = getOpenAcdContext().getAgentByUser(getUser());
        agent.setPin(getOpenAcdPin());
        getOpenAcdContext().saveAgent(agent);
        getValidator().recordSuccess(getMessages().getMessage("label.openAcdPin.changed"));
    }

}
