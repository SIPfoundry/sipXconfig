/*
 *
 *
 * Copyright (C) 2009 Nortel, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 */
package org.sipfoundry.sipxprovision.auto;

import junit.framework.TestCase;

// TODO import org.easymock.EasyMock;
// TODO import org.easymock.IMocksControl;

/**
 * Tests for the Servlet class.
 *
 * @see Servlet
 *
 * @author Paul Mossman
 */
public class ServletTest extends TestCase {


    public void testUniqueishId() {

        String seed1 = "lower case seed";

        // The unique-ish ID is a constant length.
        assert(Servlet.getUniqueId(seed1).length() == Servlet.UNIQUE_ID_LENGTH);

        // The same seed must result in the same unique-ish ID.
        assertEquals(Servlet.getUniqueId(seed1),
                Servlet.getUniqueId(seed1));

        // The seed is case insensitive.
        assertEquals(Servlet.getUniqueId(seed1),
                Servlet.getUniqueId(seed1.toUpperCase()));

        // No need to choke.
        assert(Servlet.getUniqueId(null).length() == Servlet.UNIQUE_ID_LENGTH);
        assert(Servlet.getUniqueId("").length() == Servlet.UNIQUE_ID_LENGTH);
    }

    public void testTmp() {

        Servlet servlet = new Servlet();
        Servlet.m_config = new Configuration();
    }

    // TODO: Parse Polycom UAs with AND without Serial #s!!  (Version is the tricky part...)

    // TODO: Parse Polycom UA with "UA/ "  and "UA/", which may do interesting things to Version
}
