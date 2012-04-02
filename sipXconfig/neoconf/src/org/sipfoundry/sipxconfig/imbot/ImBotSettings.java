/**
 *
 *
 * Copyright (c) 2012 eZuce, Inc. All rights reserved.
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
package org.sipfoundry.sipxconfig.imbot;

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang.StringUtils.replace;

import java.util.Collection;
import java.util.Collections;

import org.sipfoundry.sipxconfig.cfgmgt.DeployConfigOnEdit;
import org.sipfoundry.sipxconfig.feature.Feature;
import org.sipfoundry.sipxconfig.setting.PersistableSettings;
import org.sipfoundry.sipxconfig.setting.Setting;

public class ImBotSettings extends PersistableSettings implements DeployConfigOnEdit {
    private static final String PA_USER_NAME_SETTING = "imbot/_imbot.paUserName";
    private static final String PA_PASSWORD_SETTING = "imbot/imbot.paPassword";
    private static final String HTTP_PORT = "imbot/imbot.httpport";
    private static final String LOCALE_SETTING = "imbot/imbot.locale";
    private static final String LOG_LEVEL_SETTING = "imbot/log.level";

    @Override
    protected Setting loadSettings() {
        return getModelFilesContext().loadModelFile("sipximbot/sipximbot.xml");
    }

    public String getPersonalAssistantImId() {
        return getSettingValue(PA_USER_NAME_SETTING);
    }

    public void setPersonalAssistantImPassword(String password) {
        setSettingValue(PA_PASSWORD_SETTING, password);
    }

    public String getPersonalAssistantImPassword() {
        return defaultIfEmpty(replace(getSettingValue(PA_PASSWORD_SETTING), "\\", "\\\\"), getPersonalAssistantImId());
    }

    public void setPaPassword(String password) {
        setSettingValue(PA_PASSWORD_SETTING, password);
    }

    public int getHttpPort() {
        return (Integer) getSettingTypedValue(HTTP_PORT);
    }

    public void setLocale(String localeString) {
        getSettings().getSetting(LOCALE_SETTING).setValue(localeString);
    }

    public String getLocale() {
        return getSettingValue(LOCALE_SETTING);
    }

    public String getLogLevel() {
        return getSettingValue(LOG_LEVEL_SETTING);
    }

    @Override
    public String getBeanId() {
        return "imBotSettings";
    }

    @Override
    public Collection<Feature> getAffectedFeaturesOnChange() {
        return Collections.singleton((Feature) ImBot.FEATURE);
    }
}
