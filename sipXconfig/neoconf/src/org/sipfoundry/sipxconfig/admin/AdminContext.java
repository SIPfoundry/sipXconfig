/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.admin;

import org.sipfoundry.sipxconfig.address.AddressProvider;
import org.sipfoundry.sipxconfig.address.AddressType;
import org.sipfoundry.sipxconfig.feature.LocationFeature;

public interface AdminContext extends AddressProvider {
    public static final LocationFeature FEATURE = new LocationFeature("admin");
    public static final AddressType HTTP_ADDRESS = new AddressType("adminApi");
    public static final AddressType HTTPS_ADDRESS = new AddressType("secureAdminApi");
    public static final AddressType TFTP_ADDRESS = new AddressType("tftp");
    public static final AddressType FTP_ADDRESS = new AddressType("ftp");

    final String CONTEXT_BEAN_NAME = "adminContext";

    /**
     * After successfully sending event to application to perform a database related task, remove
     * task from initialization task table.
     */
    void deleteInitializationTask(String task);

    String[] getInitializationTasks();

    /**
     * @return true if this is an upgrade/data init run, and *not* a real sipXconfig run
     */
    boolean inInitializationPhase();

}
