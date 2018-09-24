/*
 *
 *
 * Copyright (C) 2010 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */
package org.sipfoundry.sipxconfig.site.vm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry.BaseComponent;
import org.apache.tapestry.IMarkupWriter;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.form.IPropertySelectionModel;
import org.apache.tapestry.form.StringPropertySelectionModel;
import org.sipfoundry.sipxconfig.components.TapestryUtils;
import org.sipfoundry.sipxconfig.setting.Setting;
import org.sipfoundry.sipxconfig.vm.MailboxManager;
import org.sipfoundry.sipxconfig.vm.MailboxPreferences.LanguageCode;

public abstract class VoicemailEmailComponent extends BaseComponent {
    private static final String VOICEMAIL_TRANSCRIBE_SETTING = "voicemail/mailbox/transcribe-voicemail";
    private static final String VOICEMAIL_NOTIFY_MISS_CALLS_SETTING = "voicemail/mailbox/notify-miss-calls";
    private static final String VOICEMAIL_TRANSCRIBE_LANGUAGE = "voicemail/mailbox/transcribe-language";
    private static final String VOICEMAIL_FORWARD_DELETE = "voicemail/mailbox/forward-delete-voicemail";

    @InjectObject(value = "spring:mailboxManager")
    public abstract MailboxManager getMailboxManager();

    @Parameter(required = true)
    public abstract Setting getSettings();

    public abstract Boolean getTranscribeVoicemail();

    public abstract void setTranscribeVoicemail(Boolean enabled);
    
    public abstract Boolean getNotifyMissCalls();

    public abstract void setNotifyMissCalls(Boolean enabled);
    
    public abstract String getTranscribeLanguage();

    public abstract void setTranscribeLanguage(String language);
    
    public abstract Boolean getForwardDeleteVoicemail();

    public abstract void setForwardDeleteVoicemail(Boolean enabled);
    
    public IPropertySelectionModel getModel() {
        return new StringPropertySelectionModel(LanguageCode.getCodes());
    }

    @Override
    protected void renderComponent(IMarkupWriter writer, IRequestCycle cycle) {
        if (!TapestryUtils.isRewinding(cycle, this)) {
            Setting enabledTranscribe = getSettings().getSetting(VOICEMAIL_TRANSCRIBE_SETTING);
            setTranscribeVoicemail((Boolean)enabledTranscribe.getTypedValue());
            
            Setting enabledMissCall = getSettings().getSetting(VOICEMAIL_NOTIFY_MISS_CALLS_SETTING);
            setNotifyMissCalls((Boolean)enabledMissCall.getTypedValue());
            
            Setting forwardDeleteVoicemail = getSettings().getSetting(VOICEMAIL_FORWARD_DELETE);
            setForwardDeleteVoicemail((Boolean)forwardDeleteVoicemail.getTypedValue());
            
            Setting transcribeLanguage = getSettings().getSetting(VOICEMAIL_TRANSCRIBE_LANGUAGE);
            setTranscribeLanguage(transcribeLanguage.getValue());
        }
        super.renderComponent(writer, cycle);
        if (TapestryUtils.isRewinding(cycle, this)) {
            getSettings().getSetting(VOICEMAIL_TRANSCRIBE_SETTING).setTypedValue(getTranscribeVoicemail());
            getSettings().getSetting(VOICEMAIL_NOTIFY_MISS_CALLS_SETTING).setTypedValue(getNotifyMissCalls());
            getSettings().getSetting(VOICEMAIL_FORWARD_DELETE).setTypedValue(getForwardDeleteVoicemail());
            getSettings().getSetting(VOICEMAIL_TRANSCRIBE_LANGUAGE).setValue(getTranscribeLanguage());
        }
    }

}
