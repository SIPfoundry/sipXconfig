/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.phone.polycom;

import org.sipfoundry.sipxconfig.device.DeviceVersion;
import org.sipfoundry.sipxconfig.phone.PhoneModel;

/**
 * Static differences in polycom phone models
 */
public final class PolycomModel extends PhoneModel {
    /** Firmware 2.0 or beyond */
    public static final DeviceVersion VER_2_0 = new DeviceVersion(PolycomPhone.BEAN_ID, "2.0");
    public static final DeviceVersion VER_3_1_X = new DeviceVersion(PolycomPhone.BEAN_ID, "3.1.X");
    public static final DeviceVersion VER_3_2_X = new DeviceVersion(PolycomPhone.BEAN_ID, "3.2.X");
    public static final DeviceVersion VER_4_0_X = new DeviceVersion(PolycomPhone.BEAN_ID, "4.0.X");
    public static final DeviceVersion VER_4_1_X = new DeviceVersion(PolycomPhone.BEAN_ID, "4.1.X");
    public static final DeviceVersion VER_4_1_0 = new DeviceVersion(PolycomPhone.BEAN_ID, "4.1.0");
    public static final DeviceVersion VER_4_1_2 = new DeviceVersion(PolycomPhone.BEAN_ID, "4.1.2");
    public static final DeviceVersion VER_4_1_3 = new DeviceVersion(PolycomPhone.BEAN_ID, "4.1.3");
    public static final DeviceVersion VER_4_1_4 = new DeviceVersion(PolycomPhone.BEAN_ID, "4.1.4");
    public static final DeviceVersion VER_4_1_5 = new DeviceVersion(PolycomPhone.BEAN_ID, "4.1.5");
    public static final DeviceVersion[] SUPPORTED_VERSIONS = new DeviceVersion[] {
        VER_3_1_X, VER_3_2_X, VER_4_0_X, VER_4_1_X, VER_4_1_0, VER_4_1_2, VER_4_1_3, VER_4_1_4, VER_4_1_5
    };
    private DeviceVersion m_deviceVersion;

    public PolycomModel() {
        super(PolycomPhone.BEAN_ID);
        setEmergencyConfigurable(true);
    }

    /**
     * checks if this is one of the 4.1.X versions
     * @return
     */
    protected static boolean is41(DeviceVersion v) {
        return v.getVersionId().startsWith("4.1");
    }
    
    public static DeviceVersion getPhoneDeviceVersion(String version) {
        for (DeviceVersion deviceVersion : SUPPORTED_VERSIONS) {
            if (deviceVersion.getName().contains(version)) {
                return deviceVersion;
            }
        }
        return VER_2_0;
    }

    public void setDefaultVersion(DeviceVersion ver) {
        m_deviceVersion = ver;
    }

    public DeviceVersion getDefaultVersion() {
        return m_deviceVersion;
    }
}
