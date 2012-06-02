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


import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.test.TestHelper;

public class BackupConfigTest {

    @Test
    public void config() throws IOException {
        BackupConfig config = new BackupConfig();
        ArchiveDefinition d1 = new ArchiveDefinition("d1", "b1", "r1");
        ArchiveDefinition d2 = new ArchiveDefinition("d2", "b2", "r2");
        StringWriter actual = new StringWriter();
        config.writeBackupConfig(actual, Arrays.asList(d1, d2));
        String expected = IOUtils.toString(getClass().getResourceAsStream("expected-backup.yaml"));
        assertEquals(expected, actual.toString());
    }
    
    @Test
    public void cluster() throws IOException {
        BackupConfig config = new BackupConfig();
        BackupSettings settings = new BackupSettings();
        settings.setModelFilesContext(TestHelper.getModelFilesContext());
        settings.setSettingTypedValue("ftp/user", "joe");
        settings.setSettingTypedValue("ftp/host", "ftp.example.org");
        settings.setSettingTypedValue("ftp/password", "xxx");
        Collection<Location> hosts = Collections.singleton(new Location("one", "1.1.1.1"));
        BackupPlan ftpPlan = new BackupPlan(BackupType.ftp);
        ftpPlan.setLimitedCount(20);
        StringWriter actual = new StringWriter();
        config.writeClusterBackupConfig(actual, ftpPlan, hosts, settings);
        String expected = IOUtils.toString(getClass().getResourceAsStream("expected-cluster-backup.yaml"));        
        assertEquals(expected, actual.toString());        
    }
    
    @Test
    public void schedules() throws IOException {
        BackupConfig config = new BackupConfig();
        DailyBackupSchedule s1 = new DailyBackupSchedule();
        DailyBackupSchedule s2 = new DailyBackupSchedule();
        BackupPlan localPlan = new BackupPlan(BackupType.local);
        localPlan.addSchedule(s1);
        localPlan.addSchedule(s2);
        BackupPlan ftpPlan = new BackupPlan(BackupType.ftp);
        ftpPlan.addSchedule(s1);
        StringWriter actual = new StringWriter();
        config.writeBackupSchedules(actual, Arrays.asList(localPlan, ftpPlan));
        String expected = IOUtils.toString(getClass().getResourceAsStream("expected-cluster-backup.yaml"));        
        assertEquals(expected, actual.toString());        
    }
}
