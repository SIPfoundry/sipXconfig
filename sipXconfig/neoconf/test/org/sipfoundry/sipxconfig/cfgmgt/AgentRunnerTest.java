/*
 * Copyright (C) 2011 eZuce Inc., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the AGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.cfgmgt;


import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

import org.apache.commons.io.output.NullOutputStream;
import org.junit.Before;
import org.junit.Test;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.job.Job;
import org.sipfoundry.sipxconfig.job.JobContext;

public class AgentRunnerTest {
    private AgentRunner m_agent;
    
    @Before
    public void setUp() {
        m_agent = new AgentRunner();
    }
    
    @Test
    public void normal() {
        m_agent.runCommand("/bin/echo", new NullOutputStream());
    }

    @Test(expected=ConfigException.class)
    public void timeout() {
        m_agent.setTimeout(250); // in millis
        m_agent.runCommand("/bin/sleep 3", new NullOutputStream());
    }

    @Test
    public void err() {
        ByteArrayOutputStream actual = new ByteArrayOutputStream();
        try {
          m_agent.runCommand("/bin/ls /completely/bogus/dir", actual);
          fail("supposed to fail");
        } catch (ConfigException expected) {
            assertTrue(true);
        }
        assertTrue("Got " + actual.toString(), actual.toString().contains("/completely/bogus/dir"));
    }
    
    @Test
    public void runNormalJob() {
        Location l = new Location("one", "1.1.1.1");
        JobContext jobc = createMock(JobContext.class);
        Serializable job = new Job("x");
        jobc.schedule("test", l);
        expectLastCall().andReturn(job).once();
        jobc.start(job);
        expectLastCall().once();
        jobc.success(job);
        expectLastCall().once();
        replay(jobc);
        m_agent.setJobContext(jobc);
        m_agent.runJob(l, "test", "/bin/echo");
        verify(jobc);        
    }   

    @Test
    public void runBadJob() {
        Location l = new Location("one", "1.1.1.1");
        JobContext jobc = createMock(JobContext.class);
        
        // expect 2 errors:  exit code and stderr results 
        Serializable job1 = new Job("1");        
        jobc.schedule("test", l);
        expectLastCall().andReturn(job1).once();
        jobc.start(job1);
        expectLastCall().once();
        jobc.failure(eq(job1), eq("Agent run finshed but returned error code 2"), isA(RuntimeException.class));
        expectLastCall().once();

        Serializable job2 = new Job("1");        
        jobc.schedule("test", l);
        expectLastCall().andReturn(job2).once();
        jobc.start(job2);
        expectLastCall().once();
        jobc.failure(eq(job2), isA(String.class), isA(RuntimeException.class));
        expectLastCall().once();
        
        replay(jobc);
        
        m_agent.setJobContext(jobc);
        m_agent.runJob(l, "test", "/bin/ls /completely/bogus/dir");
        verify(jobc);        
    }   
}
