/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.  
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 * 
 */
package org.sipfoundry.sipxconfig.gateway;

import org.sipfoundry.sipxconfig.setting.BeanWithSettings;
import org.sipfoundry.sipxconfig.setting.Setting;

/**
 * Object representing FXO port or a digital trunk (T1/E1) in an FXO/PSTN gateway
 */
public class FxoPort extends BeanWithSettings {
    private Gateway m_gateway;

    private boolean m_initialized;

    public Gateway getGateway() {
        return m_gateway;
    }

    public void setGateway(Gateway gateway) {
        m_gateway = gateway;
    }

    @Override
    protected Setting loadSettings() {
        Gateway gateway = getGateway();
        Setting settings = gateway.loadPortSettings();
        if (settings != null) {
            // kludge - not obvious place to initialize, but latest place
            initialize();
        }

        return settings;
    }

    @Override
    public synchronized void initialize() {
        if (m_initialized) {
            return;
        }
        Gateway gateway = getGateway();
        if (m_gateway == null) {
            return;
        }
        gateway.initializePort(this);
        m_initialized = true;
    }

    public String getLabel() {
        return "Temporary user visible name";
    }
}
