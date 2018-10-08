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
package org.sipfoundry.sipxconfig.rest;

import static org.sipfoundry.sipxconfig.rest.JacksonConvert.toRepresentation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.vm.MailboxPreferences;
import org.sipfoundry.sipxconfig.vm.MailboxPreferences.ActiveGreeting;
import org.sipfoundry.sipxconfig.vm.MailboxPreferences.AttachType;
import org.sipfoundry.sipxconfig.vm.MailboxPreferences.MailFormat;

public class UserVoicemailPreferencesResource extends UserResource {
    private static final Log LOG = LogFactory.getLog(UserVoicemailPreferencesResource.class);

    @Override
    public boolean allowDelete() {
        return false;
    }

    @Override
    public boolean allowPost() {
        return false;
    }
    
    public User getUserToQuery() {
        return getUser();
    }

    // GET
    @Override
    public Representation represent(Variant variant) throws ResourceException {
        MailboxPreferences prefs = new MailboxPreferences(getUserToQuery());
        VMPreferencesBean bean = new VMPreferencesBean();

        bean.setVoicemailPermission(getUser().hasVoicemailPermission());
        bean.setGreeting(prefs.getActiveGreeting());
        bean.setEnableMwi(prefs.isEnableMwi());
        bean.setTranscribeVoicemail(prefs.isTranscribeVoicemail());
        bean.setTranscribeLanguage(prefs.getTranscribeLanguage());
        bean.setNotifyMissCalls(prefs.isNotifyMissCalls());
        bean.setForwardDeleteVoicemail(prefs.isForwardDeleteVoicemail());
        if (prefs.isEmailNotificationEnabled()) {
            bean.setEmail(prefs.getEmailAddress());
            bean.setEmailAttachType(prefs.getAttachVoicemailToEmail());
            bean.setEmailFormat(prefs.getEmailFormat());
            bean.setEmailIncludeAudioAttachment(prefs.isIncludeAudioAttachment());
        }
        if (prefs.isEmailNotificationAlternateEnabled()) {
            bean.setAltEmail(prefs.getAlternateEmailAddress());
            bean.setAltEmailAttachType(prefs.getVoicemailToAlternateEmailNotification());
            bean.setAltEmailFormat(prefs.getAlternateEmailFormat());
            bean.setAltEmailIncludeAudioAttachment(prefs.isIncludeAudioAttachmentAlternateEmail());
        }

        LOG.debug("Returning VM settings:\t" + bean);

        return toRepresentation(bean);
    }

    // PUT
    @Override
    public void storeRepresentation(Representation entity) throws ResourceException {
        User user = getUserToQuery();
        if (Boolean.TRUE == user.hasVoicemailPermission()) {
            VMPreferencesBean bean = JacksonConvert.fromRepresentation(entity, VMPreferencesBean.class);
            MailboxPreferences prefs = new MailboxPreferences(user);

            LOG.debug("Saving VM settings bean:\t" + bean);

            if (bean.getGreeting() != null) {
                prefs.setActiveGreeting(bean.getGreeting());
            }
            if (bean.getEnableMwi() != null) {
                prefs.setEnableMwi(bean.getEnableMwi());
            }
            if (bean.getTranscribeVoicemail() != null) {
                prefs.setTranscribeVoicemail(bean.getTranscribeVoicemail());
            }
            if (bean.getTranscribeLanguage() != null) {
                prefs.setTranscribeLanguage(bean.getTranscribeLanguage());
            }
            if (bean.getNotifyMissCalls() != null) {
                prefs.setNotifyMissCalls(bean.getNotifyMissCalls());
            }
            if (bean.getForwardDeleteVoicemail() != null) {
                prefs.setForwardDeleteVoicemail(bean.getForwardDeleteVoicemail());
            }
            if (bean.getEmail() != null) {
                prefs.setEmailAddress(bean.getEmail());
            }
            if (bean.getEmailAttachType() != null) {
                prefs.setAttachVoicemailToEmail(bean.getEmailAttachType());
            }
            if (bean.getEmailFormat() != null) {
                prefs.setEmailFormat(bean.getEmailFormat());
            }
            if (bean.getEmailIncludeAudioAttachment() != null) {
                prefs.setIncludeAudioAttachment(bean.getEmailIncludeAudioAttachment());
            }
            if (bean.getAltEmail() != null) {
                prefs.setAlternateEmailAddress(bean.getAltEmail());
            }
            if (bean.getAltEmailAttachType() != null) {
                prefs.setVoicemailToAlternateEmailNotification(bean.getAltEmailAttachType());
            }
            if (bean.getAltEmailFormat() != null) {
                prefs.setAlternateEmailFormat(bean.getAltEmailFormat());
            }
            if (bean.getAltEmailIncludeAudioAttachment() != null) {
                prefs.setIncludeAudioAttachmentAlternateEmail(bean.getAltEmailIncludeAudioAttachment());
            }

            prefs.updateUser(user);
            getCoreContext().saveUser(user);
        }
    }

    private static class VMPreferencesBean {
        private ActiveGreeting m_greeting;
        private Boolean m_enableMwi;
        private Boolean m_forwardDeleteVoicemail;
        private Boolean m_transcribeVoicemail;
        private String m_transcribeLanguage;
        private Boolean m_notifyMissCalls;
        private String m_email;
        private AttachType m_emailAttachType;
        private MailFormat m_emailFormat;
        private Boolean m_emailIncludeAudioAttachment;
        private String m_altEmail;
        private AttachType m_altEmailAttachType;
        private MailFormat m_altEmailFormat;
        private Boolean m_altEmailIncludeAudioAttachment;
        private Boolean m_voicemailPermission;

        public ActiveGreeting getGreeting() {
            return m_greeting;
        }

        public void setGreeting(ActiveGreeting greeting) {
            m_greeting = greeting;
        }
        
        public Boolean getForwardDeleteVoicemail() {
            return m_forwardDeleteVoicemail;
        }
        
        public void setForwardDeleteVoicemail(Boolean forwardDeleteVoicemail) {
            m_forwardDeleteVoicemail = forwardDeleteVoicemail;
        }
        
        public Boolean getEnableMwi() {
            return m_enableMwi;
        }
        
        public void setEnableMwi(Boolean enableMwi) {
            m_enableMwi = enableMwi;
        }

        public Boolean getTranscribeVoicemail() {
            return m_transcribeVoicemail;
        }

        public void setTranscribeVoicemail(Boolean transcribeVoicemail) {
            m_transcribeVoicemail = transcribeVoicemail;
        }
        
        public String getTranscribeLanguage() {
            return m_transcribeLanguage;
        }
        
        public void setTranscribeLanguage(String language) {
            m_transcribeLanguage = language;
        }
        
        public Boolean getNotifyMissCalls() {
            return m_notifyMissCalls;
        }

        public void setNotifyMissCalls(Boolean notifyMissCalls) {
            m_notifyMissCalls = notifyMissCalls;
        }

        public String getEmail() {
            return m_email;
        }

        public void setEmail(String email) {
            m_email = email;
        }

        public AttachType getEmailAttachType() {
            return m_emailAttachType;
        }

        public void setEmailAttachType(AttachType attachType) {
            m_emailAttachType = attachType;
        }

        public MailFormat getEmailFormat() {
            return m_emailFormat;
        }

        public void setEmailFormat(MailFormat emailFormat) {
            m_emailFormat = emailFormat;
        }

        public Boolean getEmailIncludeAudioAttachment() {
            return m_emailIncludeAudioAttachment;
        }

        public void setEmailIncludeAudioAttachment(Boolean emailIncludeAudioAttachment) {
            m_emailIncludeAudioAttachment = emailIncludeAudioAttachment;
        }

        public String getAltEmail() {
            return m_altEmail;
        }

        public void setAltEmail(String altEmail) {
            m_altEmail = altEmail;
        }

        public AttachType getAltEmailAttachType() {
            return m_altEmailAttachType;
        }

        public void setAltEmailAttachType(AttachType altEmailAttachType) {
            m_altEmailAttachType = altEmailAttachType;
        }

        public MailFormat getAltEmailFormat() {
            return m_altEmailFormat;
        }

        public void setAltEmailFormat(MailFormat altEmailFormat) {
            m_altEmailFormat = altEmailFormat;
        }

        public Boolean getAltEmailIncludeAudioAttachment() {
            return m_altEmailIncludeAudioAttachment;
        }

        public void setAltEmailIncludeAudioAttachment(Boolean altEmailIncludeAudioAttachment) {
            m_altEmailIncludeAudioAttachment = altEmailIncludeAudioAttachment;
        }

        @SuppressWarnings("unused")
        public Boolean getVoicemailPermission() {
            return m_voicemailPermission;
        }

        public void setVoicemailPermission(Boolean hasVoicemailPermission) {
            m_voicemailPermission = hasVoicemailPermission;
        }

        @Override
        public String toString() {
            return "VMPreferencesBean [m_greeting=" + m_greeting + ", m_transcribeVoicemail=" + m_transcribeVoicemail
                + ", m_transcribeLanguage=" + m_transcribeLanguage + ", m_notifyMissCalls=" + m_notifyMissCalls
                + ", m_forwardDeleteVoicemail=" + m_forwardDeleteVoicemail + ", m_email=" + m_email + ", m_emailAttachType="
                + m_emailAttachType + ", m_emailFormat=" + m_emailFormat + ", m_emailIncludeAudioAttachment="
                + m_emailIncludeAudioAttachment + ", m_altEmail=" + m_altEmail + ", m_altEmailAttachType="
                + m_altEmailAttachType + ", m_altEmailFormat=" + m_altEmailFormat
                + ", m_altEmailIncludeAudioAttachment=" + m_altEmailIncludeAudioAttachment
                + ", m_voicemailPermission=" + m_voicemailPermission + "]";
        }
    }
}
