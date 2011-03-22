/**
 *
 *
 * Copyright (c) 2010 / 2011 eZuce, Inc. All rights reserved.
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
package org.sipfoundry.sipxconfig.phone.nortel;

import org.sipfoundry.sipxconfig.device.DeviceVersion;
import org.sipfoundry.sipxconfig.phone.PhoneModel;

public class NortelPhoneModel extends PhoneModel {
    public static final String VENDOR = "nortelPhone";
    public static final DeviceVersion FIRM_2_2 = new DeviceVersion(VENDOR, "2.2");
    public static final DeviceVersion FIRM_3_2 = new DeviceVersion(VENDOR, "3.2");
    public static final int FIRMWARE32ORLATER_MAX_LINES = 24;
    public static final int FIRMWARE22_MAX_LINES = 1;

    public NortelPhoneModel() {
        super();
        setVersions(getDeviceVersions());
    }

    public static DeviceVersion[] getDeviceVersions() {
        return new DeviceVersion[] {
            FIRM_2_2, FIRM_3_2
        };
    }
}
