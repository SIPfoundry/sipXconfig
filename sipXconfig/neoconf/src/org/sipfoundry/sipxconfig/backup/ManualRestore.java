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

import java.io.File;
import java.util.Collection;

import org.sipfoundry.sipxconfig.common.WaitingListener;

/**
 * There's no other type other than manual restore, but I wanted name to match ManualBackup
 * because they are similar.
 *
 * Summary of steps to perform a backup: 1.) write custom restore plan in each CFDATA/$location
 * for each location 2.) write custom cluster restore plan in CFDATA/primary location 3.) stage
 * cluster restore files 4.) call restore on all nodes
 */
public class ManualRestore implements WaitingListener {
    private BackupManager m_backupManager;
    private BackupConfig m_backupConfig;

    BackupCommandRunner writePlan(Collection<String> defIds, BackupSettings settings) {
        // doesn't matter which plan, we already staged the files
        BackupPlan plan = getBackupManager().findOrCreateBackupPlan(BackupType.local);
        plan.getManualModeDefinitionIds().addAll(defIds);
        File planFile = getBackupConfig().writeManualBackupConfigs(plan, settings);
        BackupCommandRunner runner = new BackupCommandRunner(planFile, getBackupManager().getBackupScript());
        return runner;
    }

    /**
     * if defIds are null or empty, then files are already staged and we can skip right to node restore
     */
    public void restore(Collection<String> defIds, BackupSettings settings) {
        BackupCommandRunner runner = writePlan(defIds, settings);
        runner.setBackground(true);
        runner.restore(defIds);
    }

    /**
     * optionally restore in background, useful when sipxconfig is restoring sipxconfig
     */
    public void restore(Collection<String> defIds, BackupSettings settings, boolean backgroundProcess) {
        BackupCommandRunner runner = writePlan(defIds, settings);
        runner.setBackground(backgroundProcess);
        runner.restore(defIds);
    }

    public void setBackupManager(BackupManager backupManager) {
        m_backupManager = backupManager;
    }

    public void setBackupConfig(BackupConfig backupConfig) {
        m_backupConfig = backupConfig;
    }

    public BackupManager getBackupManager() {
        return m_backupManager;
    }

    public BackupConfig getBackupConfig() {
        return m_backupConfig;
    }

    @Override
    public void afterResponseSent() {
        // TODO Auto-generated method stub
    }
}
