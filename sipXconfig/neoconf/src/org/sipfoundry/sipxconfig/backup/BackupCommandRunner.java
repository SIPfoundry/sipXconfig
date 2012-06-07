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
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Wraps functionality of cluster backup script and makes it available.
 */
public class BackupCommandRunner {
    private String m_backupScript;
    private File m_plan;

    public BackupCommandRunner(File plan, String backupScript) {
        m_backupScript = backupScript;
        m_plan = plan;
    }

    public String lastBackup() {
        List<String> backups = list();
        return backups.isEmpty() ? null : (String) backups.get(backups.size() - 1);
    }

    public void restoreFromStage() {
        runCommand(m_backupScript, "--restore-from-stage", m_plan.getAbsolutePath());
    }

    public void restore(String backupPath) {
        // Stages then restores
        runCommand("--restore", m_plan.getAbsolutePath(), "--path", backupPath);
    }

    public void backup() {
        runCommand("--backup", m_plan.getAbsolutePath());
    }

    public String getBackupLink() {
        return StringUtils.chomp(runCommand("--link", m_plan.getAbsolutePath()));
    }

    String runCommand(String... command) {
        File listFile = null;
        Reader rdr = null;
        String commandLine = StringUtils.EMPTY;
        try {
            listFile = File.createTempFile("archive-command", ".tmp");
            String[] commandOut = new String[command.length + 5];
            System.arraycopy(command, 0, commandOut, 1, command.length);
            commandOut[0] = m_backupScript;
            commandOut[command.length + 1] = "--out";
            commandOut[command.length + 2] = listFile.getAbsolutePath();
            commandOut[command.length + 3] = "--mode"; // Relevant to few cmds, but harmless otherwise
            commandOut[command.length + 4] = "manual";
            ProcessBuilder pb = new ProcessBuilder(commandOut);
            commandLine = StringUtils.join(pb.command(), ' ');
            Process process = pb.start();
            int code = process.waitFor();
            if (code != 0) {
                String errorMsg = String.format("Archive command %s failed. Exit code: %d", commandLine, code);
                throw new RuntimeException(errorMsg);
            }
            rdr = new FileReader(listFile);
            return IOUtils.toString(rdr);
        } catch (IOException e) {
            String errorMsg = String.format("Error running archive command %s.", commandLine);
            throw new RuntimeException(errorMsg);
        } catch (InterruptedException e) {
            String errorMsg = String.format("Timed out running archive command %s.", commandLine);
            throw new RuntimeException(errorMsg);
        } finally {
            IOUtils.closeQuietly(rdr);
            if (listFile != null) {
                listFile.delete();
            }
        }
    }

    public List<String> list() {
        if (!m_plan.exists()) {
            return Collections.emptyList();
        }
        String lines = StringUtils.chomp(runCommand("--list", m_plan.getAbsolutePath()));
        return Arrays.asList(StringUtils.splitByWholeSeparator(lines, "\n"));
    }
}
