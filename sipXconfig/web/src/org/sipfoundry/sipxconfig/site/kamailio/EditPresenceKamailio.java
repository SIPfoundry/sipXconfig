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
package org.sipfoundry.sipxconfig.site.kamailio;

import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.sipfoundry.sipxconfig.components.PageWithCallback;
import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;
import org.sipfoundry.sipxconfig.kamailio.KamailioManager;
import org.sipfoundry.sipxconfig.kamailio.KamailioSettings;

public abstract class EditPresenceKamailio extends PageWithCallback implements PageBeginRenderListener {
    public static final String PAGE = "kamailio/EditPresenceKamailio";

    @Bean
    public abstract SipxValidationDelegate getValidator();

    @InjectObject("spring:kamailioManager")
    public abstract KamailioManager getKamailioManager();

    public abstract KamailioSettings getSettings();

    public abstract void setSettings(KamailioSettings settings);

    @Override
    public void pageBeginRender(PageEvent arg0) {
        if (getSettings() == null) {
            setSettings(getKamailioManager().getSettings());
        }
    }

    public void apply() {
        getKamailioManager().saveSettings(getSettings());
    }
}
