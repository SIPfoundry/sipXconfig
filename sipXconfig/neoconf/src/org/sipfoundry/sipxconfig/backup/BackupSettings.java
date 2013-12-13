/**
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
package org.sipfoundry.sipxconfig.backup;

import java.util.Collection;
import java.util.Collections;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.sipfoundry.sipxconfig.cfgmgt.DeployConfigOnEdit;
import org.sipfoundry.sipxconfig.feature.Feature;
import org.sipfoundry.sipxconfig.setting.PersistableSettings;
import org.sipfoundry.sipxconfig.setting.Setting;

public class BackupSettings extends PersistableSettings implements DeployConfigOnEdit {

    @Override
    public String getBeanId() {
        return "backupSettings";
    }

    @Override
    protected Setting loadSettings() {
        return getModelFilesContext().loadModelFile("backup/backup.xml");
    }

    @Override
    @JsonIgnore
    public Collection<Feature> getAffectedFeaturesOnChange() {
        return Collections.singleton((Feature) BackupManager.FEATURE);
    }

    @JsonIgnore
    public boolean isKeepDeviceFiles() {
        return (Boolean) getSettingTypedValue("backup/device");
    }

    @JsonIgnore
    public boolean isKeepDomain() {
        return (Boolean) getSettingTypedValue("restore/keepDomain");
    }

    @JsonIgnore
    public boolean isKeepFqdn() {
        return (Boolean) getSettingTypedValue("restore/keepFqdn");
    }

    @JsonIgnore
    public boolean isDecodePins() {
        return (Boolean) getSettingTypedValue("restore/decodePins");
    }

    @JsonIgnore
    public int getDecodePinLen() {
        return (Integer) getSettingTypedValue("restore/decodePinMaxLen");
    }

    @JsonIgnore
    public String getResetPin() {
        return (String) getSettingTypedValue("restore/resetPin");
    }

    @JsonIgnore
    public String getResetPassword() {
        return (String) getSettingTypedValue("restore/resetPassword");
    }
}
