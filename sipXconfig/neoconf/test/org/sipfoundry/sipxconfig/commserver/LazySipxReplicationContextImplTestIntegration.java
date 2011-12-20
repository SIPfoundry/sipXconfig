/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.admin.commserver;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import junit.framework.TestCase;

import org.sipfoundry.sipxconfig.admin.ConfigurationFile;
import org.sipfoundry.sipxconfig.admin.commserver.LazySipxReplicationContextImpl.ConfTask;
import org.sipfoundry.sipxconfig.admin.dialplan.DialPlanActivatedEvent;
import org.sipfoundry.sipxconfig.admin.dialplan.config.MappingRules;
import org.sipfoundry.sipxconfig.admin.dialplan.config.Orbits;
import org.sipfoundry.sipxconfig.admin.dialplan.config.XmlFile;
import org.sipfoundry.sipxconfig.admin.forwarding.CallSequence;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.device.InMemoryConfiguration;
import org.springframework.context.ApplicationEvent;

/**
 * It's marked as Integration test due to time sensitivity - it is known to fail on our PPC built
 * system
 */
public class LazySipxReplicationContextImplTestIntegration extends TestCase {

    public void testGenerateAll() throws Exception {
        int lazyIterations = 20;
        XmlFile mr = new MappingRules();
        mr.setName("mappingrules.xml");
        XmlFile orbits = new ParkOrbitConfigurationTest();
        orbits.setName("orbits.xml");

        ApplicationEvent event = new DialPlanActivatedEvent(this, false);

        LazySipxReplicationContextImpl lazy = new LazySipxReplicationContextImpl();

        SipxReplicationContext replication = createMock(SipxReplicationContext.class);
        replication.replicate(mr);
        replication.generate(new User());
        replication.generate(new User());
        replication.generate(new CallSequence());
        replication.generateAll();
        replication.publishEvent(event);
        replication.replicate(orbits);
        replay(replication);

        int interval = 50;
        lazy.setSleepInterval(interval);
        lazy.setTarget(replication);

        lazy.init();

        lazy.replicate(mr);
        lazy.generate(new User());
        lazy.generate(new User());
        lazy.generate(new CallSequence());
        lazy.generateAll();
        lazy.publishEvent(event);

        Thread.sleep(400);

        for (int i = 0; i < lazyIterations; i++) {
            lazy.replicate(orbits);
        }

        Thread.sleep(800);

        verify(replication);
    }

    public void testConfTaskUpdate() {
        ConfigurationFile conf1 = new InMemoryConfiguration("dir", "name", "content");
        ConfigurationFile conf2 = new InMemoryConfiguration("dir", "name", "different");

        ConfTask task1 = new ConfTask(conf1);
        ConfTask task2 = new ConfTask(conf1);
        ConfTask task3 = new ConfTask(conf2);

        assertTrue(task1.update(task2));
        assertFalse(task1.update(task3));

        Location location = new Location();
        ConfTask task4 = new ConfTask(location, conf1);

        SipxReplicationContext rc = createMock(SipxReplicationContext.class);
        rc.replicate(location, conf1);
        rc.replicate(conf1);
        replay(rc);

        // replicate to specific location
        task4.replicate(rc);
        assertTrue(task4.update(task1));
        // now replicate to all locations
        task4.replicate(rc);

        verify(rc);
    }

}
