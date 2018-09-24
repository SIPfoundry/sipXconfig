/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */
package org.sipfoundry.sipxconfig.vm;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sipfoundry.sipxconfig.common.User;

public class MailboxPreferences {
    public static final String EMPTY = "";

    public static final String ACTIVE_GREETING = "voicemail/mailbox/active-greeting";
    public static final String UNIFIED_MESSAGING_LANGUAGE = "voicemail/mailbox/language";
    public static final String BUSY_PROMPT = "voicemail/mailbox/user-busy-prompt";
    public static final String VOICEMAIL_TUI = "voicemail/mailbox/voicemail-tui";
    public static final String EXTERNAL_MWI = "voicemail/mailbox/external-mwi";
    public static final String FORWARD_DELETE_VOICEMAIL = "voicemail/mailbox/forward-delete-voicemail";
    public static final String TRANSCRIBE_VOICEMAIL = "voicemail/mailbox/transcribe-voicemail";
    public static final String TRANSCRIBE_LANGUAGE = "voicemail/mailbox/transcribe-language";
    public static final String NOTIFY_MISS_CALLS = "voicemail/mailbox/notify-miss-calls";

    public static final String PRIMARY_EMAIL_NOTIFICATION = "voicemail/mailbox/primary-email-voicemail-notification";
    public static final String PRIMARY_EMAIL_FORMAT = "voicemail/mailbox/primary-email-format";
    public static final String PRIMARY_EMAIL_ATTACH_AUDIO = "voicemail/mailbox/primary-email-attach-audio";
    public static final String ALT_EMAIL_NOTIFICATION = "voicemail/mailbox/alternate-email-voicemail-notification";
    public static final String ALT_EMAIL_FORMAT = "voicemail/mailbox/alternate-email-format";
    public static final String ALT_EMAIL_ATTACH_AUDIO = "voicemail/mailbox/alternate-email-attach-audio";

    public static final String IMAP_ACCOUNT = "voicemail/imap/account";
    public static final String IMAP_PASSWORD = "voicemail/imap/password";
    public static final String IMAP_TLS = "voicemail/imap/tls";
    public static final String IMAP_PORT = "voicemail/imap/port";
    public static final String IMAP_HOST = "voicemail/imap/host";

    public enum ActiveGreeting {
        NONE("none"), STANDARD("standard"), OUT_OF_OFFICE("outofoffice"), EXTENDED_ABSENCE("extendedabsence");

        private static final Set<String> IDS = new HashSet<String>();

        private final String m_id;

        ActiveGreeting(String id) {
            m_id = id;
        }

        public String getId() {
            return m_id;
        }

        static {
            for (ActiveGreeting e : ActiveGreeting.values()) {
                IDS.add(e.getId());
            }
            IDS.add(EMPTY);
        }

        public static ActiveGreeting fromId(String id) {
            for (ActiveGreeting greeting : ActiveGreeting.values()) {
                if (greeting.getId().equals(id)) {
                    return greeting;
                }
            }
            return NONE;
        }

        public static boolean isValid(String s) {
            return IDS.contains(s);
        }
    }

    public enum AttachType {
        NO("0"), YES("1")/* , IMAP("2") */;

        private static final Set<String> VALUES = new HashSet<String>();

        private String m_value;

        AttachType(String value) {
            m_value = value;
        }

        public String getValue() {
            return m_value;
        }

        static {
            for (AttachType e : AttachType.values()) {
                VALUES.add(e.getValue());
            }
            VALUES.add(EMPTY);
        }

        public static AttachType fromValue(String value) {
            for (AttachType e : values()) {
                if (e.m_value.equals(value)) {
                    return e;
                }
            }
            return NO;
        }

        public static boolean isValid(String s) {
            return VALUES.contains(s);
        }
    }

    public enum VoicemailTuiType {
        STANDARD("stdui"), CALLPILOT("cpui");

        private String m_value;

        VoicemailTuiType(String value) {
            m_value = value;
        }

        public String getValue() {
            return m_value;
        }

        public static VoicemailTuiType fromValue(String value) {
            for (VoicemailTuiType e : values()) {
                if (e.m_value.equals(value)) {
                    return e;
                }
            }
            return STANDARD;
        }
    }

    public enum MailFormat {
        FULL, MEDIUM, BRIEF;

        private static final Set<String> VALUES = new HashSet<String>();

        static {
            for (MailFormat e : MailFormat.values()) {
                VALUES.add(e.toString());
            }
            VALUES.add(EMPTY);
        }

        public static boolean isValid(String s) {
            return VALUES.contains(s);
        }
    }
    
    public enum LanguageCode {
        AF_ZA("af-ZA"), AM_ET("am-ET"), HY_AM("hy-AM"), AZ_AZ("az-AZ"), ID_ID("id-ID"),
        MS_MY("ms-MY"), BN_BD("bn-BD"), BN_IN("bn-IN"), CA_ES("ca-ES"), CS_CZ("cs-CZ"),
        DA_DK("da-DK"), DE_DE("de-DE"), EN_AU("en-AU"), EN_CA("en-CA"), EN_GH("en-GH"),
        EN_GB("en-GB"), EN_IN("en-IN"), EN_IE("en-IE"), EN_KE("en-KE"), EN_NZ("en-NZ"),
        EN_NG("en-NG"), EN_PH("en-PH"), EN_ZA("en-ZA"), EN_TZ("en-TZ"), EN_US("en-US"),
        ES_AR("es-AR"), ES_BO("es-BO"), ES_CL("es-CL"), ES_CO("es-CO"), ES_CR("es-CR"),
        ES_EC("es-EC"), ES_SV("es-SV"), ES_ES("es-ES"), ES_US("es-US"), ES_GT("es-GT"),
        ES_HN("es-HN"), ES_MX("es-MX"), ES_NI("es-NI"), ES_PA("es-PA"), ES_PY("es-PY"),
        ES_PE("es-PE"), ES_PR("es-PR"), ES_DO("es-DO"), ES_UY("es-UY"), ES_VE("es-VE"),
        EU_ES("eu-ES"), FIL_PH("fil-PH"), FR_CA("fr-CA"), FR_FR("fr-FR"), GL_ES("gl-ES"),
        KA_GE("ka-GE"), GU_IN("gu-IN"), HR_HR("hr-HR"), ZU_ZA("zu-ZA"), IS_IS("is-IS"),
        IT_IT("it-IT"), JV_ID("jv-ID"), KN_IN("kn-IN"), KM_KH("km-KH"), LO_LA("lo-LA"),
        LV_LV("lv-LV"), LT_LT("lt-LT"), HU_HU("hu-HU"), ML_IN("ml-IN"), MR_IN("mr-IN"),
        NL_NL("nl-NL"), NE_NP("ne-NP"), NB_NO("nb-NO"), PL_PL("pl-PL"), PT_BR("pt-BR"),
        PT_PT("pt-PT"), RO_RO("ro-RO"), SI_LK("si-LK"), SK_SK("sk-SK"), SL_SI("sl-SI"),
        SU_ID("su-ID"), SW_TZ("sw-TZ"), SW_KE("sw-KE"), FI_FI("fi-FI"), SV_SE("sv-SE"),
        TA_IN("ta-IN"), TA_SG("ta-SG"), TA_LK("ta-LK"), TA_MY("ta-MY"), TE_IN("te-IN"),
        VI_VN("vi-VN"), TR_TR("tr-TR"), UR_PK("ur-PK"), UR_IN("ur-IN"), EL_GR("el-GR"),
        BG_BG("bg-BG"), RU_RU("ru-RU"), SR_RS("sr-RS"), UK_UA("uk-UA"), HE_IL("he-IL"),
        AR_IL("ar-IL"), AR_JO("ar-JO"), AR_AE("ar-AE"), AR_BH("ar-BH"), AR_DZ("ar-DZ"),
        AR_SA("ar-SA"), AR_IQ("ar-IQ"), AR_KW("ar-KW"), AR_MA("ar-MA"), AR_TN("ar-TN"),
        AR_OM("ar-OM"), AR_PS("ar-PS"), AR_QA("ar-QA"), AR_LB("ar-LB"), AR_EG("ar-EG"),
        FA_IR("fa-IR"), HI_IN("hi-IN"), TH_TH("th-TH"), KO_KR("ko-KR"), JA_JP("ja-JP"),
        CMN_HANT_TW("cmn-Hant-TW"), YUE_HANT_HK("yue-Hant-HK"), 
        CMN_HANS_HK("cmn-Hans-HK"), CMN_HANS_CN("cmn-Hans-CN");
        
        private String code;
        
        private LanguageCode(String code) {
            this.code = code;
        }
        
        public String getCode() {
            return code;
        }
        
        public static LanguageCode fromString(String code) {
            for(LanguageCode lc : LanguageCode.values()) {
                if (lc.getCode() == code) {
                    return lc;
                }
            }
            
            throw new IllegalArgumentException("Invalid/Unsupported language code");
        }
        
        public static LanguageCode tryFromString(String code) {
            try {
                return LanguageCode.fromString(code);
            } catch(IllegalArgumentException ex) {
                return null;
            }
        }

        public static String[] getCodes() {
            List<String> codes = new ArrayList<String>();
            for(LanguageCode lc : LanguageCode.values()) {
                codes.add(lc.getCode());
            }

            return codes.toArray(new String[codes.size()]);
        }
    }

    private ActiveGreeting m_activeGreeting = ActiveGreeting.NONE;
    private String m_language;
    private String m_busyPrompt;
    private VoicemailTuiType m_voicemailTui = VoicemailTuiType.STANDARD;
    private String m_externalMwi;
    private boolean m_forwardDeleteVoicemail;
    private boolean m_transcribeVoicemail;
    private boolean m_notifyMissCalls;
    private String m_transcribeLanguage;

    private String m_emailAddress;
    private MailFormat m_emailFormat = MailFormat.FULL;
    private AttachType m_attachVoicemailToEmail = AttachType.NO;
    private boolean m_includeAudioAttachment;

    private String m_alternateEmailAddress;
    private MailFormat m_alternateEmailFormat = MailFormat.FULL;
    private AttachType m_voicemailToAlternateEmailNotification = AttachType.NO;
    private boolean m_includeAudioAttachmentAlternateEmail;

    private String m_imapHost;
    private String m_imapPort;
    private boolean m_imapTLS;
    private boolean m_enableMwi;
    private String m_imapAccount;
    private String m_imapPassword;

    public MailboxPreferences() {
        // empty
    }

    public MailboxPreferences(User user) {
        m_emailAddress = user.getEmailAddress();
        m_enableMwi = user.getIsMWI();
        m_alternateEmailAddress = user.getAlternateEmailAddress();
        m_activeGreeting = ActiveGreeting.fromId(user.getActiveGreeting());
        m_language = user.getSettingValue(UNIFIED_MESSAGING_LANGUAGE);
        m_busyPrompt = user.getSettingValue(BUSY_PROMPT);
        m_voicemailTui = VoicemailTuiType.fromValue(user.getSettingValue(VOICEMAIL_TUI));
        m_externalMwi = user.getSettingValue(EXTERNAL_MWI);
        m_forwardDeleteVoicemail = (Boolean) user.getSettingTypedValue(FORWARD_DELETE_VOICEMAIL);
        m_transcribeVoicemail = (Boolean) user.getSettingTypedValue(TRANSCRIBE_VOICEMAIL);
        m_transcribeLanguage = user.getSettingValue(TRANSCRIBE_LANGUAGE);
        m_notifyMissCalls = (Boolean) user.getSettingTypedValue(NOTIFY_MISS_CALLS);
        m_attachVoicemailToEmail = AttachType.fromValue(user.getPrimaryEmailNotification());
        m_emailFormat = MailFormat.valueOf(user.getPrimaryEmailFormat());
        m_includeAudioAttachment = (Boolean) user.isPrimaryEmailAttachAudio();
        m_voicemailToAlternateEmailNotification = AttachType.fromValue(user.getAlternateEmailNotification());
        m_alternateEmailFormat = MailFormat.valueOf(user.getAlternateEmailFormat());
        m_includeAudioAttachmentAlternateEmail = (Boolean) user.isAlternateEmailAttachAudio();
        m_imapHost = user.getSettingValue(IMAP_HOST);
        m_imapPort = user.getSettingValue(IMAP_PORT);
        m_imapTLS = (Boolean) user.getSettingTypedValue(IMAP_TLS);
        m_imapPassword = user.getSettingValue(IMAP_PASSWORD);
        m_imapAccount = user.getSettingValue(IMAP_ACCOUNT);
    }

    public void updateUser(User user) {
        user.setEmailAddress(m_emailAddress);
        user.setIsMWI(m_enableMwi);
        user.setAlternateEmailAddress(m_alternateEmailAddress);
        user.setActiveGreeting(m_activeGreeting.getId());
        user.setSettingValue(UNIFIED_MESSAGING_LANGUAGE, m_language);
        user.setSettingValue(BUSY_PROMPT, m_busyPrompt);
        user.setSettingValue(VOICEMAIL_TUI, m_voicemailTui.getValue());
        user.setSettingValue(EXTERNAL_MWI, m_externalMwi);
        user.setSettingTypedValue(FORWARD_DELETE_VOICEMAIL, m_forwardDeleteVoicemail);
        user.setSettingTypedValue(TRANSCRIBE_VOICEMAIL, m_transcribeVoicemail);
        user.setSettingValue(TRANSCRIBE_LANGUAGE, m_transcribeLanguage);
        user.setSettingTypedValue(NOTIFY_MISS_CALLS, m_notifyMissCalls);
        user.setPrimaryEmailNotification(m_attachVoicemailToEmail.getValue());
        user.setPrimaryEmailFormat(m_emailFormat.name());
        user.setPrimaryEmailAttachAudio(m_includeAudioAttachment);
        user.setAlternateEmailNotification(m_voicemailToAlternateEmailNotification.getValue());
        user.setAlternateEmailFormat(m_alternateEmailFormat.name());
        user.setAlternateEmailAttachAudio(m_includeAudioAttachmentAlternateEmail);
        user.setSettingValue(IMAP_HOST, m_imapHost);
        user.setSettingValue(IMAP_PORT, m_imapPort);
        user.setSettingTypedValue(IMAP_TLS, m_imapTLS);
        user.setSettingValue(IMAP_PASSWORD, m_imapPassword);
        user.setSettingValue(IMAP_ACCOUNT, m_imapAccount);
    }

    public ActiveGreeting getActiveGreeting() {
        return m_activeGreeting;
    }

    public void setActiveGreeting(ActiveGreeting activeGreeting) {
        m_activeGreeting = activeGreeting;
    }

    public String getLanguage() {
        return m_language;
    }

    public void setLanguage(String language) {
        m_language = language;
    }

    public String getBusyPrompt() {
        return m_busyPrompt;
    }

    public void setBusyPrompt(String busyPrompt) {
        m_busyPrompt = busyPrompt;
    }

    public VoicemailTuiType getVoicemailTui() {
        return m_voicemailTui;
    }

    public void setVoicemailTui(VoicemailTuiType voicemailTui) {
        m_voicemailTui = voicemailTui;
    }

    public String getExternalMwi() {
        return m_externalMwi;
    }

    public void setExternalMwi(String externalMwi) {
        m_externalMwi = externalMwi;
    }
    
    public boolean isEnableMwi() {
        return m_enableMwi;
    }
    
    public void setEnableMwi(boolean enable) {
        m_enableMwi = enable;
    }

    public AttachType getAttachVoicemailToEmail() {
        return m_attachVoicemailToEmail;
    }

    public void setAttachVoicemailToEmail(AttachType attachVoicemailToEmail) {
        m_attachVoicemailToEmail = attachVoicemailToEmail;
    }

    public MailFormat getEmailFormat() {
        return m_emailFormat;
    }

    public void setEmailFormat(MailFormat emailFormat) {
        m_emailFormat = emailFormat;
    }

    public String getEmailAddress() {
        return m_emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        m_emailAddress = emailAddress;
    }

    public String getAlternateEmailAddress() {
        return m_alternateEmailAddress;
    }

    public void setAlternateEmailAddress(String alternateEmailAddress) {
        m_alternateEmailAddress = alternateEmailAddress;
    }

    public AttachType getVoicemailToAlternateEmailNotification() {
        return m_voicemailToAlternateEmailNotification;
    }

    public void setVoicemailToAlternateEmailNotification(AttachType voicemailToAlternateEmailNotification) {
        m_voicemailToAlternateEmailNotification = voicemailToAlternateEmailNotification;
    }

    public boolean isSynchronizeWithImapServer() {
        // return m_attachVoicemailToEmail == AttachType.IMAP;
        return false;
    }

    public boolean isEmailNotificationEnabled() {
        return m_attachVoicemailToEmail == AttachType.YES;
    }

    public boolean isEmailNotificationAlternateEnabled() {
        return m_voicemailToAlternateEmailNotification == AttachType.YES;
    }

    public boolean isIncludeAudioAttachment() {
        return m_includeAudioAttachment;
    }

    public void setIncludeAudioAttachment(boolean includeAudioAttachment) {
        m_includeAudioAttachment = includeAudioAttachment;
    }

    public MailFormat getAlternateEmailFormat() {
        return m_alternateEmailFormat;
    }

    public void setAlternateEmailFormat(MailFormat emailFormat) {
        m_alternateEmailFormat = emailFormat;
    }

    public boolean isIncludeAudioAttachmentAlternateEmail() {
        return m_includeAudioAttachmentAlternateEmail;
    }

    public void setIncludeAudioAttachmentAlternateEmail(boolean audioAttachmentAlternateEmail) {
        m_includeAudioAttachmentAlternateEmail = audioAttachmentAlternateEmail;
    }

    public String getImapHost() {
        return m_imapHost;
    }

    public void setImapHost(String imapHost) {
        m_imapHost = imapHost;
    }

    public String getImapPort() {
        return m_imapPort;
    }

    public void setImapPort(String imapPort) {
        m_imapPort = imapPort;
    }

    public boolean getImapTLS() {
        return m_imapTLS;
    }

    public void setImapTLS(boolean imapTls) {
        m_imapTLS = imapTls;
    }

    public String getImapPassword() {
        return m_imapPassword;
    }

    public void setImapPassword(String emailPassword) {
        m_imapPassword = emailPassword;
    }

    public String getImapAccount() {
        return m_imapAccount;
    }

    public void setImapAccount(String imapAccount) {
        m_imapAccount = imapAccount;
    }

    public boolean isForwardDeleteVoicemail() {
        return m_forwardDeleteVoicemail;
    }

    public void setForwardDeleteVoicemail(boolean forwardDeleteVoicemail) {
        m_forwardDeleteVoicemail = forwardDeleteVoicemail;
    }
    
    public boolean isTranscribeVoicemail() {
        return m_transcribeVoicemail;
    }

    public void setTranscribeVoicemail(boolean transcribeVoicemail) {
        m_transcribeVoicemail = transcribeVoicemail;
    }
    
    public String getTranscribeLanguage() {
        return m_transcribeLanguage;
    }
    
    public void setTranscribeLanguage(String transcribeLanguage) {
        m_transcribeLanguage = transcribeLanguage;
    }
    
    public boolean isNotifyMissCalls() {
        return m_notifyMissCalls;
    }

    public void setNotifyMissCalls(boolean notifyMissCalls) {
        m_notifyMissCalls = notifyMissCalls;
    }

    public boolean isImapServerConfigured() {
        // return StringUtils.isNotEmpty(getImapHost()) && getImapPort() != null;
        return false;
    }

    public ActiveGreeting[] getOptionsForActiveGreeting(boolean isStandardTui) {
        List list = new ArrayList();
        if (isStandardTui) {
            list.add(ActiveGreeting.NONE);
            list.add(ActiveGreeting.STANDARD);
            list.add(ActiveGreeting.OUT_OF_OFFICE);
            list.add(ActiveGreeting.EXTENDED_ABSENCE);
        } else {
            list.add(ActiveGreeting.STANDARD);
            list.add(ActiveGreeting.OUT_OF_OFFICE);
        }
        return (ActiveGreeting[]) list.toArray(new ActiveGreeting[0]);
    }

    public AttachType[] getAttachOptions(boolean isAdmin) {
        if (isImapServerConfigured() || isAdmin) {
            return AttachType.values();
        }
        return new AttachType[] {
            AttachType.NO, AttachType.YES
        };

    }

    public AttachType[] getAttachOptionsForAlternateEmail() {
        return new AttachType[] {
            AttachType.NO, AttachType.YES
        };
    }

    public VoicemailTuiType[] getOptionsForVoicemailTui(String promptDir) {
        List list = new ArrayList();
        list.add(VoicemailTuiType.STANDARD);
        // Check that the voicemail stdprompts directory is available
        if (promptDir != null) {
            // Check if the optional scs-callpilot-prompts package is installed
            String cpPromptDir = promptDir + "/cpui";
            if ((new File(cpPromptDir)).exists()) {
                list.add(VoicemailTuiType.CALLPILOT);
            }
        }
        // Add any additional voicemail prompts packages here
        return (VoicemailTuiType[]) list.toArray(new VoicemailTuiType[0]);
    }
    
    public String[] getOptionsForTranscribeLanguage() {
        return LanguageCode.getCodes();
    }
}
