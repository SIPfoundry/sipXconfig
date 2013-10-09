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
package org.sipfoundry.sipxconfig.commserver.imdb;

import static org.sipfoundry.commons.mongo.MongoConstants.ACCOUNT;
import static org.sipfoundry.commons.mongo.MongoConstants.ACTIVEGREETING;
import static org.sipfoundry.commons.mongo.MongoConstants.ALT_ATTACH_AUDIO;
import static org.sipfoundry.commons.mongo.MongoConstants.ALT_EMAIL;
import static org.sipfoundry.commons.mongo.MongoConstants.ALT_NOTIFICATION;
import static org.sipfoundry.commons.mongo.MongoConstants.ATTACH_AUDIO;
import static org.sipfoundry.commons.mongo.MongoConstants.BUTTONS;
import static org.sipfoundry.commons.mongo.MongoConstants.CALL_FROM_ANY_IM;
import static org.sipfoundry.commons.mongo.MongoConstants.CALL_IM;
import static org.sipfoundry.commons.mongo.MongoConstants.CONF_ENTRY_IM;
import static org.sipfoundry.commons.mongo.MongoConstants.CONF_EXIT_IM;
import static org.sipfoundry.commons.mongo.MongoConstants.DIALPAD;
import static org.sipfoundry.commons.mongo.MongoConstants.DISPLAY_NAME;
import static org.sipfoundry.commons.mongo.MongoConstants.DISTRIB_LISTS;
import static org.sipfoundry.commons.mongo.MongoConstants.EMAIL;
import static org.sipfoundry.commons.mongo.MongoConstants.HASHED_PASSTOKEN;
import static org.sipfoundry.commons.mongo.MongoConstants.HOST;
import static org.sipfoundry.commons.mongo.MongoConstants.IM_ADVERTISE_ON_CALL_STATUS;
import static org.sipfoundry.commons.mongo.MongoConstants.IM_DISPLAY_NAME;
import static org.sipfoundry.commons.mongo.MongoConstants.IM_ENABLED;
import static org.sipfoundry.commons.mongo.MongoConstants.IM_ID;
import static org.sipfoundry.commons.mongo.MongoConstants.IM_ON_THE_PHONE_MESSAGE;
import static org.sipfoundry.commons.mongo.MongoConstants.IM_SHOW_ON_CALL_DETAILS;
import static org.sipfoundry.commons.mongo.MongoConstants.ITEM;
import static org.sipfoundry.commons.mongo.MongoConstants.LANGUAGE;
import static org.sipfoundry.commons.mongo.MongoConstants.LEAVE_MESSAGE_BEGIN_IM;
import static org.sipfoundry.commons.mongo.MongoConstants.LEAVE_MESSAGE_END_IM;
import static org.sipfoundry.commons.mongo.MongoConstants.MOH;
import static org.sipfoundry.commons.mongo.MongoConstants.NOTIFICATION;
import static org.sipfoundry.commons.mongo.MongoConstants.OPERATOR;
import static org.sipfoundry.commons.mongo.MongoConstants.PASSWD;
import static org.sipfoundry.commons.mongo.MongoConstants.PERSONAL_ATT;
import static org.sipfoundry.commons.mongo.MongoConstants.PINTOKEN;
import static org.sipfoundry.commons.mongo.MongoConstants.PLAY_DEFAULT_VM;
import static org.sipfoundry.commons.mongo.MongoConstants.PORT;
import static org.sipfoundry.commons.mongo.MongoConstants.SYNC;
import static org.sipfoundry.commons.mongo.MongoConstants.TLS;
import static org.sipfoundry.commons.mongo.MongoConstants.USERBUSYPROMPT;
import static org.sipfoundry.commons.mongo.MongoConstants.VMONDND;
import static org.sipfoundry.commons.mongo.MongoConstants.VOICEMAILTUI;
import static org.sipfoundry.sipxconfig.vm.DistributionList.SETTING_PATH_DISTRIBUTION_LIST;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.sipfoundry.sipxconfig.common.DialPad;
import org.sipfoundry.sipxconfig.common.Replicable;
import org.sipfoundry.sipxconfig.common.SipUri;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.im.ImAccount;
import org.sipfoundry.sipxconfig.localization.LocalizationContext;
import org.sipfoundry.sipxconfig.moh.MusicOnHoldManager;
import org.sipfoundry.sipxconfig.vm.DistributionList;
import org.sipfoundry.sipxconfig.vm.MailboxManager;
import org.sipfoundry.sipxconfig.vm.MailboxPreferences;
import org.sipfoundry.sipxconfig.vm.attendant.PersonalAttendant;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

//TODO: this one is included in group dataset (ReplicationManagerImpl.GROUP_DATASETS)
//Make sure all are needed, and break this apart if necessary
public class Mailstore extends AbstractDataSetGenerator {
    private MailboxManager m_mailboxManager;
    private LocalizationContext m_localizationContext;
    private MusicOnHoldManager m_mohManager;

    @Override
    public boolean generate(Replicable entity, DBObject top) {
        if (!(entity instanceof User)) {
            return false;
        }
        User user = (User) entity;
        // The following settings used to be in validusers.xml
        putOnlyIfNotNull(top, MOH, getMohSetting(user));
        putOnlyIfNotNull(top, USERBUSYPROMPT, user.getSettingValue("voicemail/mailbox/user-busy-prompt")); // can
        // be
        // null
        putOnlyIfNotNull(top, VOICEMAILTUI, user.getSettingValue("voicemail/mailbox/voicemail-tui")); // can
                                                                                                      // be
        // null
        putOnlyIfNotNull(top, DISPLAY_NAME, user.getDisplayName());
        top.put(HASHED_PASSTOKEN, user.getSipPasswordHash(getCoreContext().getAuthorizationRealm()));
        top.put(PINTOKEN, user.getPintoken());
        MailboxPreferences mp = new MailboxPreferences(user);

        String emailAddress = mp.getEmailAddress();
        boolean enableNotification = StringUtils.isNotBlank(emailAddress) && mp.isEmailNotificationEnabled();
        if (StringUtils.isNotBlank(emailAddress)) {
            top.put(EMAIL, emailAddress);
        } else {
            removeField(top, EMAIL);
        }

        if (enableNotification) {
            top.put(NOTIFICATION, mp.getEmailFormat().name());
            top.put(ATTACH_AUDIO, Boolean.toString(mp.isIncludeAudioAttachment()));
        } else {
            removeField(top, NOTIFICATION);
            removeField(top, ATTACH_AUDIO);
        }

        String alternateEmailAddress = mp.getAlternateEmailAddress();
        boolean enableAltNotification = StringUtils.isNotBlank(alternateEmailAddress)
                && mp.isEmailNotificationAlternateEnabled();
        if (StringUtils.isNotBlank(alternateEmailAddress)) {
            top.put(ALT_EMAIL, alternateEmailAddress);
        }

        if (enableAltNotification) {
            top.put(ALT_NOTIFICATION, mp.getAlternateEmailFormat().name());
            top.put(ALT_ATTACH_AUDIO, Boolean.toString(mp.isIncludeAudioAttachmentAlternateEmail()));
        } else {
            removeField(top, ALT_NOTIFICATION);
            removeField(top, ALT_ATTACH_AUDIO);
        }

        boolean imapServerConfigured = mp.isImapServerConfigured();
        if (imapServerConfigured) {
            top.put(SYNC, mp.isSynchronizeWithImapServer());
            top.put(HOST, mp.getImapHost());
            top.put(PORT, mp.getImapPort());
            top.put(TLS, mp.getImapTLS());
            top.put(ACCOUNT, StringUtils.defaultString(mp.getImapAccount()));
            String pwd = StringUtils.defaultString(mp.getImapPassword());
            String encodedPwd = new String(Base64.encodeBase64(pwd.getBytes()));
            top.put(PASSWD, encodedPwd);
        } else {
            removeField(top, SYNC);
            removeField(top, HOST);
            removeField(top, PORT);
            removeField(top, TLS);
            removeField(top, ACCOUNT);
            removeField(top, PASSWD);
        }

        ImAccount imAccount = new ImAccount(user);
        top.put(IM_ENABLED, imAccount.isEnabled());
        // The following settings used to be in contact-information.xml
        putOnlyIfNotNull(top, IM_ID, imAccount.getImId().toLowerCase());
        putOnlyIfNotNull(top, IM_DISPLAY_NAME, imAccount.getImDisplayName());
        putOnlyIfNotNull(top, CONF_ENTRY_IM, user.getSettingValue("im_notification/conferenceEntryIM").toString());
        putOnlyIfNotNull(top, CONF_EXIT_IM, user.getSettingValue("im_notification/conferenceExitIM").toString());
        putOnlyIfNotNull(top, LEAVE_MESSAGE_BEGIN_IM, user.getSettingValue("im_notification/leaveMsgBeginIM")
                .toString());
        putOnlyIfNotNull(top, LEAVE_MESSAGE_END_IM, user.getSettingValue("im_notification/leaveMsgEndIM").toString());
        putOnlyIfNotNull(top, CALL_IM, user.getSettingValue("im_notification/call").toString());
        putOnlyIfNotNull(top, CALL_FROM_ANY_IM, user.getSettingValue("im_notification/callFromAnyNumber").toString());
        // and this one in presencerouting-prefs.xml
        putOnlyIfNotNull(top, VMONDND, imAccount.isForwardOnDnd());
        // settings from xmpp-account-info.xml
        putOnlyIfNotNull(top, IM_ON_THE_PHONE_MESSAGE, imAccount.getOnThePhoneMessage());
        putOnlyIfNotNull(top, IM_ADVERTISE_ON_CALL_STATUS, imAccount.advertiseSipPresence());
        putOnlyIfNotNull(top, IM_SHOW_ON_CALL_DETAILS, imAccount.includeCallInfo());
        // personal attendant
        //if user chose to not have voicemail deposited, the default VM options prompt is
        //not played
        if (user.isDepositVoicemail()) {
            putOnlyIfNotNull(top, PLAY_DEFAULT_VM, user.getPlayVmDefaultOptions());
        } else {
            putOnlyIfNotNull(top, PLAY_DEFAULT_VM, false);
        }
        PersonalAttendant pa = m_mailboxManager.loadPersonalAttendantForUser(user);
        if (pa != null) {
            DBObject pao = new BasicDBObject();
            if (StringUtils.isNotEmpty(user.getOperator())) {
                pao.put(OPERATOR, SipUri.fix(user.getOperator(), getSipDomain()));
            }
            if (pa.getOverrideLanguage() && StringUtils.isNotEmpty(pa.getLanguage())) {
                pao.put(LANGUAGE, pa.getLanguage());
            } else {
                pao.put(LANGUAGE, m_localizationContext.getCurrentLanguage());
            }
            if (pa.getMenu() != null && !pa.getMenu().getMenuItems().isEmpty()) {
                List<DBObject> buttonsList = new ArrayList<DBObject>();
                for (DialPad dialPad : pa.getMenu().getMenuItems().keySet()) {
                    DBObject menuItem = new BasicDBObject();
                    menuItem.put(DIALPAD, dialPad.getName());
                    menuItem.put(ITEM,
                            SipUri.fix(pa.getMenu().getMenuItems().get(dialPad).getParameter(), getSipDomain()));
                    buttonsList.add(menuItem);
                }
                pao.put(BUTTONS, buttonsList);
            }
            top.put(PERSONAL_ATT, pao);
        }
        putOnlyIfNotNull(top, ACTIVEGREETING, user.getSettingValue(MailboxPreferences.ACTIVE_GREETING));
        // DL
        List<DBObject> dLists = new ArrayList<DBObject>();
        for (int i = 1; i < DistributionList.MAX_SIZE; i++) {
            String extensions = user.getSettingValue(new StringBuilder(SETTING_PATH_DISTRIBUTION_LIST).append(i)
                    .toString());
            if (extensions != null) {
                DBObject dlist = new BasicDBObject();
                dlist.put(DIALPAD, i);
                dlist.put(ITEM, extensions);
                dLists.add(dlist);
            }
        }
        if (dLists.size() > 0) {
            top.put(DISTRIB_LISTS, dLists);
        } else {
            removeField(top, DISTRIB_LISTS);
        }
        return true;
    }

    @Override
    protected DataSet getType() {
        return DataSet.MAILSTORE;
    }

    private String getMohSetting(User user) {
        String userMoh = user.getSettingValue("moh/audio-source");
        if (StringUtils.equals("SYSTEM_DEFAULT", userMoh)) {
            return m_mohManager.getSettings().getMusicOnHoldSource();
        }
        return userMoh;
    }

    public void setMailboxManager(MailboxManager mailboxManager) {
        m_mailboxManager = mailboxManager;
    }

    public void setMusicOnHoldManager(MusicOnHoldManager mohManager) {
        m_mohManager = mohManager;
    }

    public void setLocalizationContext(LocalizationContext localizationContext) {
        m_localizationContext = localizationContext;
    }
}
