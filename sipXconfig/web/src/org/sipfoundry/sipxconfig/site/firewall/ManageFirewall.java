/**
 * Copyright (c) 2012 eZuce, Inc. All rights reserved.
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
package org.sipfoundry.sipxconfig.site.firewall;

import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.InitialValue;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.sipfoundry.sipxconfig.components.SipxBasePage;
import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;
import org.sipfoundry.sipxconfig.firewall.FirewallManager;
import org.sipfoundry.sipxconfig.firewall.FirewallSettings;

public abstract class ManageFirewall extends SipxBasePage implements PageBeginRenderListener {

    @Bean
    public abstract SipxValidationDelegate getValidator();

    @InjectObject("spring:firewallManager")
    public abstract FirewallManager getFirewallManager();

    public abstract FirewallSettings getSettings();

    public abstract void setSettings(FirewallSettings settings);

    @Persist
    @InitialValue(value = "literal:rules")
    public abstract String getTab();

    public abstract void setTab(String tab);

    @Override
    public void pageBeginRender(PageEvent arg0) {
        if (getSettings() == null) {
            setSettings(getFirewallManager().getSettings());
        }
    }

    public void saveSettings() {
        getFirewallManager().saveSettings(getSettings());
    }
}
