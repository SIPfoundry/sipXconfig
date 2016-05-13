package org.sipfoundry.sipxconfig.phone.generic;

import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.device.DeviceDefaults;
import org.sipfoundry.sipxconfig.phone.Line;
import org.sipfoundry.sipxconfig.setting.SettingEntry;

import static org.sipfoundry.sipxconfig.phone.generic.GenericPhoneConstant.ACCOUNT_NAME;
import static org.sipfoundry.sipxconfig.phone.generic.GenericPhoneConstant.AUTHENTICATION_PASSWORD;
import static org.sipfoundry.sipxconfig.phone.generic.GenericPhoneConstant.AUTHENTICATION_USERNAME;
import static org.sipfoundry.sipxconfig.phone.generic.GenericPhoneConstant.DISPLAY_NAME;
import static org.sipfoundry.sipxconfig.phone.generic.GenericPhoneConstant.DOMAIN;
import static org.sipfoundry.sipxconfig.phone.generic.GenericPhoneConstant.PROXY;
import static org.sipfoundry.sipxconfig.phone.generic.GenericPhoneConstant.SIP_ID;
import static org.sipfoundry.sipxconfig.phone.generic.GenericPhoneConstant.VOICE_MAIL_ACCESS_CODE;

public class GenericPhoneLineDefaults {
    private Line m_line;
    private DeviceDefaults m_defaults;

    GenericPhoneLineDefaults(DeviceDefaults defaults, Line line) {
        m_line = line;
        m_defaults = defaults;
    }

    @SettingEntry(path = DOMAIN)
    public String getDomain() {
        String domain = null;
        User user = m_line.getUser();
        if (user != null) {
            domain = m_defaults.getDomainName();
        }

        return domain;
    }

    @SettingEntry(path = PROXY)
    public String getProxy() {
        String proxy = null;
        User user = m_line.getUser();
        if (user != null) {
            proxy = m_defaults.getDomainName();
        }

        return proxy;
    }

    @SettingEntry(path = AUTHENTICATION_USERNAME)
    public String getUserName() {
        String username = null;
        User user = m_line.getUser();
        if (user != null) {
            username = user.getUserName();
        }

        return username;
    }

    @SettingEntry(path = SIP_ID)
    public String getSipId() {
        String username = null;
        User user = m_line.getUser();
        if (user != null) {
            username = user.getUserName();
        }

        return username;
    }

    @SettingEntry(path = VOICE_MAIL_ACCESS_CODE)
    public String getVoiceMailAccessCode() {
        return "";
    }

    @SettingEntry(path = AUTHENTICATION_PASSWORD)
    public String getPassword() {
        String password = null;
        User user = m_line.getUser();
        if (user != null) {
            password = user.getSipPassword();
        }

        return password;
    }

    @SettingEntry(path = DISPLAY_NAME)
    public String getDisplayName() {

        String displayName = null;
        User user = m_line.getUser();
        if (user != null) {
            displayName = user.getDisplayName();
        }

        return displayName;
    }

    @SettingEntry(path = ACCOUNT_NAME)
    public String getName() {

        String displayName = null;
        User user = m_line.getUser();
        if (user != null) {
            displayName = user.getUserName();
        }

        return displayName;
    }
}
