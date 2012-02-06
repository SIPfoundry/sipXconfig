/*
 * Copyright (C) 2011 eZuce Inc., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the AGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.dns;

import org.sipfoundry.sipxconfig.address.AddressType;
import org.sipfoundry.sipxconfig.feature.LocationFeature;

public interface DnsManager {
    public static final LocationFeature FEATURE = new LocationFeature("sipxdns");
    public static final AddressType DNS_ADDRESS = new AddressType("dnsAddress");

    public DnsSettings getSettings();

    public void saveSettings(DnsSettings settings);
}
