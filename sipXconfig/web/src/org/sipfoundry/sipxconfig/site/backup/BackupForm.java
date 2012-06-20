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
package org.sipfoundry.sipxconfig.site.backup;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.tapestry.BaseComponent;
import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.InitialValue;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.apache.tapestry.form.IPropertySelectionModel;
import org.sipfoundry.sipxconfig.backup.BackupManager;
import org.sipfoundry.sipxconfig.backup.BackupPlan;
import org.sipfoundry.sipxconfig.backup.BackupSettings;
import org.sipfoundry.sipxconfig.backup.DailyBackupSchedule;
import org.sipfoundry.sipxconfig.backup.ManualBackup;
import org.sipfoundry.sipxconfig.common.UserException;
import org.sipfoundry.sipxconfig.components.ExtraOptionModelDecorator;
import org.sipfoundry.sipxconfig.components.ObjectSelectionModel;
import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;
import org.sipfoundry.sipxconfig.components.TapestryUtils;
import org.sipfoundry.sipxconfig.setting.Setting;
import org.sipfoundry.sipxconfig.setting.SettingsValidator;

public abstract class BackupForm extends BaseComponent implements PageBeginRenderListener {
    /**
     * Conceivable, available backup limits. Otherwise arbitrary. NOTE : Spring 1.1 couldn't
     * define Integers in lists see DefaultXmlBeanDefinitionParser.java:parsePropertySubelement()
     */
    public static final List<Integer> BACKUP_LIMIT_MODEL = Arrays.asList(1, 2, 3, 4, 5, 10, 20, 30, 40, 50);

    @Bean
    public abstract SipxValidationDelegate getValidator();

    public abstract Collection<String> getDefinitionIds();

    public abstract void setDefinitionIds(Collection<String> ids);

    public abstract String getDefinitionId();

    public abstract BackupPlan getBackupPlan();

    public boolean isSelectedDefinition() {
        return getBackupPlan() != null && getBackupPlan().getAutoModeDefinitionIds().contains(getDefinitionId());
    }

    public void setSelectedDefinition(boolean selected) {
        Set<String> ids = getBackupPlan().getAutoModeDefinitionIds();
        if (selected) {
            ids.add(getDefinitionId());
        } else {
            ids.remove(getDefinitionId());
        }
    }

    @InitialValue("literal:auto")
    public abstract void setMode(String mode);

    public abstract String getMode();

    @Parameter(required = true)
    public abstract void setBackupPlan(BackupPlan plan);

    @Parameter
    public abstract String getSettingsPath();

    @InjectObject("spring:manualBackup")
    public abstract ManualBackup getManualBackup();

    @InjectObject("spring:backupManager")
    public abstract BackupManager getBackupManager();

    public abstract BackupSettings getSettings();

    public abstract void setSettings(BackupSettings settings);

    public abstract IPropertySelectionModel getBackupLimitSelectionModelCached();

    public abstract void setBackupLimitSelectionModelCached(IPropertySelectionModel model);

    public abstract DailyBackupSchedule getSchedule();

    @Override
    public void pageBeginRender(PageEvent event) {

        if (getDefinitionIds() == null) {
            setDefinitionIds(getBackupManager().getArchiveDefinitionIds());
        }

        // every plan has exactly 1 schedule
        BackupPlan plan = getBackupPlan();
        if (plan.getSchedules().isEmpty()) {
            DailyBackupSchedule schedule = new DailyBackupSchedule();
            plan.addSchedule(schedule);
        }

        // NOTE: it's possible to have old IDs in there as features were disabled. one could argue
        // these ids should be cleared up on feature enable/disable but it's rather convenient to keep ids
        // in there as features are re-enabled, they are automatically part of backup plan. Unless they
        // come to the backup page and save before they re-enabled features, we clear them here. However,
        // WYSIWYG overrules this convenience.
        Collection<?> invalidOrOff = CollectionUtils.disjunction(getDefinitionIds(), plan.getAutoModeDefinitionIds());
        plan.getAutoModeDefinitionIds().removeAll(invalidOrOff);

        if (getSettingsPath() != null) {
            if (getSettings() == null) {
                setSettings(getBackupManager().getSettings());
            }
        }
    }

    public Setting getPlanSettings() {
        return getSettings() != null ? getSettings().getSettings().getSetting(getSettingsPath()) : null;
    }

    public IPropertySelectionModel getBackupLimitSelectionModel() {
        IPropertySelectionModel modelCached = getBackupLimitSelectionModelCached();
        if (modelCached != null) {
            return modelCached;
        }

        ObjectSelectionModel numbersOnly = new ObjectSelectionModel();
        numbersOnly.setCollection(BACKUP_LIMIT_MODEL);
        numbersOnly.setLabelExpression("toString()");

        ExtraOptionModelDecorator backupLimitModel = new ExtraOptionModelDecorator();
        backupLimitModel.setModel(numbersOnly);
        backupLimitModel.setExtraLabel(getMessages().getMessage("select.unlimited"));
        backupLimitModel.setExtraOption(null);
        setBackupLimitSelectionModelCached(backupLimitModel);
        return backupLimitModel;
    }

    public void backup() {
        if (!validatePlan()) {
            return;
        }
        setMode("backup");
        getManualBackup().backup(getBackupPlan(), getSettings());
        BackupTable table = (BackupTable) getComponent("backups");
        table.setBackups(null);
        getValidator().recordSuccess(getMessages().getMessage("message.backupCompleted"));
    }

    private boolean validatePlan() {
        if (!TapestryUtils.isValid(this)) {
            return false;
        }
        validateSettings();
        BackupPlan plan = getBackupPlan();
        if (plan.getAutoModeDefinitionIds().isEmpty()) {
            getValidator().record(new UserException("&message.emptySelection"), getMessages());
            return false;
        }
        return true;
    }

    public void ok() {
        if (!validatePlan()) {
            return;
        }
        setMode("auto");
        getBackupManager().saveBackupPlan(getBackupPlan());
        if (getPlanSettings() != null) {
            getBackupManager().saveSettings(getSettings());
        }
    }

    private void validateSettings() {
        Setting settings = getPlanSettings();
        if (settings != null) {
            SettingsValidator validator = TapestryUtils.requiredSettingsValidator();
            validator.validate(getPlanSettings());
        }
    }

    public boolean isValidSettings() {
        try {
            validateSettings();
            return true;
        } catch (UserException ignore) {
            return false;
        }
    }
}
