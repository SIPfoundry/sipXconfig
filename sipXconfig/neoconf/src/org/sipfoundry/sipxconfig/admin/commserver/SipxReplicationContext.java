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

import org.sipfoundry.sipxconfig.admin.ConfigurationFile;
import org.sipfoundry.sipxconfig.admin.commserver.imdb.DataSet;
import org.sipfoundry.sipxconfig.common.Replicable;
import org.springframework.context.ApplicationEvent;

public interface SipxReplicationContext {
    void generateAll();
    void generateAll(DataSet ds);

    /**
     * Replicates configuration to all locations.
     *
     * @param conf configuration to be replicated
     */
    void replicate(ConfigurationFile conf);

    /**
     * Replicates configuration to a specific location only...
     *
     * @param location server to which location needs to be pushed
     * @param conf configuration to be replicated
     */
    void replicate(Location location, ConfigurationFile conf);

    /**
     * This function will publish application event - in case the application is done lazily it
     * will publish the even only after everything has been replicated
     *
     * @param event event to be published
     */
    void publishEvent(ApplicationEvent event);

    void generate(Replicable entity);
    void remove(Replicable entity);
    void replicateWork(Replicable entity);
    void resyncSlave(Location location);
    void replicateLocation(Location location);
}
