/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.backup;

import java.util.Map;

import org.sipfoundry.sipxconfig.test.IntegrationTestCase;

public class BackupPlanTestDb extends IntegrationTestCase {
    private BackupManager m_backupManager;

    protected void onSetUpBeforeTransaction() throws Exception {
        super.onSetUpBeforeTransaction();
        clear();
    }

    public void testStoreJob() throws Exception {
        BackupPlan plan = new LocalBackupPlan();
        m_backupManager.storeBackupPlan(plan);
        commit();
        
        Map<String, Object> actual = db().queryForMap("select * from backup_plan");
        assertEquals(plan.getId(), actual.get("backup_plan_id"));
        assertEquals(50, actual.get("limited_count"));
        assertEquals(true, actual.get("configs"));
        assertEquals(true, actual.get("voicemail"));
    }

    public void setBackupManager(BackupManager backupManager) {
        m_backupManager = backupManager;
    }
}
