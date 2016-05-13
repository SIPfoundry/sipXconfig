package org.sipfoundry.sipxconfig.phone.generic;

import org.sipfoundry.sipxconfig.device.DeviceDefaults;
import org.sipfoundry.sipxconfig.setting.SettingEntry;

import static org.sipfoundry.sipxconfig.phone.generic.GenericPhoneConstant.CONFIG_URL;

public class GenericPhoneDefaults {
    private final DeviceDefaults m_defaults;
    private final GenericPhone m_phone;

    GenericPhoneDefaults(DeviceDefaults defaults, GenericPhone phone) {
        m_defaults = defaults;
        m_phone = phone;
    }

    @SettingEntry(path = CONFIG_URL)
    public String getConfigUrl() {
        String configUrl = m_defaults.getProfileRootUrl() + '/' + m_phone.getProfileFilename();
        return configUrl;
    }
}
