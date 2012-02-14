/*
 * Copyright (C) 2011 eZuce Inc., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the AGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.cfgmgt;

import static java.lang.String.format;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.commserver.LocationsManager;
import org.sipfoundry.sipxconfig.job.JobContext;

public class AgentRunner {
    private static final Log LOG = LogFactory.getLog(ConfigAgent.class);
    private String m_command;
    private volatile boolean m_inProgress;
    private LocationsManager m_locationsManager;
    private int m_timeout = 300000;
    private JobContext m_jobContext;

    protected synchronized void run(Collection<Location> locations, String label, String command) {
        try {
            m_inProgress = true;
            List<Location> ok = new ArrayList<Location>(locations.size());
            for (Location l : locations) {
                run(l, label, command);
            }
        } finally {
            m_inProgress = false;
        }
    }

    void run(Location location, String label, String subCommand) {
        String address = location.isPrimary() ? "127.0.0.1" : location.getAddress();
        String command = format("%s --host %s %s", getCommand(), address, subCommand);
        OutputStream log = null;
        Serializable job = m_jobContext.schedule(label, location);
        try {
            m_jobContext.start(job);
            PipedInputStream in = new PipedInputStream();
            log = new PipedOutputStream(in);
            AgentResults results = new AgentResults();
            results.parse(in);
            run(command, log);
            List<String> errs = results.getResults();
            for (String err : errs) {
                // No sense showing job unless there was a problem
                m_jobContext.start(job);
                m_jobContext.failure(job, err, new RuntimeException());
            }
            m_jobContext.success(job);
        } catch (ConfigException e) {
            m_jobContext.failure(job, e.getMessage(), new RuntimeException());
        } catch (Exception e) {
            m_jobContext.failure(job, "Internal error", e);
        } finally {
            IOUtils.closeQuietly(log);
        }
    }

    void run(String command, OutputStream log) {
        Process exec = null;
        try {
            LOG.info("Starting agent run " + command);
            exec = Runtime.getRuntime().exec(command);
            // nothing goes to stderr, so just eat it
            StreamGobbler errGobbler = new StreamGobbler(exec.getErrorStream());
            StreamGobbler outGobbler = new StreamGobbler(exec.getInputStream(), log);
            Worker worker = new Worker(exec);
            new Thread(errGobbler).start();
            new Thread(outGobbler).start();
            Thread work = new Thread(worker);
            work.start();
            work.join(m_timeout);
            int code = worker.getExitCode();
            if (outGobbler.m_error != null) {
                LOG.error("Error logging output stream from agent run", outGobbler.m_error);
            }
            if (code == 0) {
                LOG.info("Finished agent run successfully");
            } else {
                throw new ConfigException("Agent run finshed but returned error code " + code);
            }
        } catch (InterruptedException e) {
            throw new ConfigException(format("Interrupted error. Could not complete agent command in %d ms.",
                    m_timeout));
        } catch (IOException e) {
            throw new ConfigException("IO error. Could not complete agent command " + e.getMessage());
        } finally {
            if (exec != null) {
                exec.destroy();
            }
        }
    }

    public void setLocationsManager(LocationsManager locationsManager) {
        m_locationsManager = locationsManager;
    }

    public boolean isInProgress() {
        return m_inProgress;
    }

    public int getTimeout() {
        return m_timeout;
    }

    public void setTimeout(int timeout) {
        m_timeout = timeout;
    }

    class Worker implements Runnable {
        private Process m_process;
        private Integer m_exitCode;
        private InterruptedException m_error;
        Worker(Process process) {
            m_process = process;
        }
        public void run() {
            try {
                m_exitCode = m_process.waitFor();
            } catch (InterruptedException e) {
                m_error = e;
            }
        }

        int getExitCode() throws InterruptedException {
            if (m_error != null) {
                throw m_error;
            }
            if (m_exitCode == null) {
                throw new InterruptedException("Proccess still running");
            }
            return m_exitCode;
        }
    }

    // cfagent script will block unless streams are read
    // http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html
    class StreamGobbler implements Runnable {
        private InputStream m_in;
        private OutputStream m_out;
        private IOException m_error;
        StreamGobbler(InputStream in) {
            m_in = in;
            m_out = new NullOutputStream();
        }

        StreamGobbler(InputStream in, OutputStream out) {
            m_in = in;
            m_out = out;
        }

        @Override
        public void run() {
            try {
                IOUtils.copy(m_in, m_out);
            } catch (IOException e) {
                m_error = e;
            }
        }
    }

    public LocationsManager getLocationsManager() {
        return m_locationsManager;
    }

    public void setCommand(String command) {
        m_command = command;
    }

    public String getCommand() {
        return m_command;
    }

    public void setJobContext(JobContext jobContext) {
        m_jobContext = jobContext;
    }
}
