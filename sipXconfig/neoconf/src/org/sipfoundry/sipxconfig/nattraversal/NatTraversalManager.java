/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.nattraversal;

import org.sipfoundry.sipxconfig.commserver.Location;

public interface NatTraversalManager {
    String CONTEXT_BEAN_NAME = "natTraversalManager";

    void store(NatTraversal natTraversal);
    NatTraversal getNatTraversal();
    void activateNatLocation(Location location);
}
