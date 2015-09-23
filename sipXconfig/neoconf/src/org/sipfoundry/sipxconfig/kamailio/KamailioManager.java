/**
 *
 * Copyright (C) 2015 SIPFoundry., certain elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 */
package org.sipfoundry.sipxconfig.kamailio;

import org.sipfoundry.sipxconfig.address.AddressType;
import org.sipfoundry.sipxconfig.feature.LocationFeature;

public interface KamailioManager {
    public static final LocationFeature FEATURE = new LocationFeature("kamailio");
    public static final AddressType TCP_ADDRESS = AddressType.externalSipTcp("ingressTcp");
    public static final AddressType UDP_ADDRESS = AddressType.externalSipUdp("ingressUdp");
    public static final AddressType TLS_ADDRESS = AddressType.sipTls("ingressTls");

    public KamailioSettings getSettings();

    public void saveSettings(KamailioSettings settings);

}
