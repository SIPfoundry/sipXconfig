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
package org.sipfoundry.sipxconfig.site.openacd;

import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.InitialValue;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.commserver.LocationsManager;
import org.sipfoundry.sipxconfig.components.PageWithCallback;
import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;
import org.sipfoundry.sipxconfig.components.TapestryUtils;

public abstract class OpenAcdServerPage extends PageWithCallback implements PageBeginRenderListener {
    public static final String PAGE = "openacd/OpenAcdServerPage";

    @InjectObject("spring:locationsManager")
    public abstract LocationsManager getLocationsManager();

    @InjectObject("spring:sipxServiceManager")
    public abstract SipxServiceManager getSipxServiceManager();

    @InjectObject("spring:serviceConfigurator")
    public abstract ServiceConfigurator getServiceConfigurator();

    public abstract Location getSipxLocation();

    public abstract void setSipxLocation(Location location);

    public abstract SipxService getService();

    public abstract void setService(SipxService service);

    @Bean
    public abstract SipxValidationDelegate getValidator();

    @Persist
    @InitialValue("literal:clients")
    public abstract String getTab();

    public abstract void setTab(String tab);

    public void pageBeginRender(PageEvent event) {
        if (getSipxLocation() == null) {
            setSipxLocation(getLocationsManager().getPrimaryLocation());
        }
        SipxService sipxService = getSipxServiceManager().getServiceByBeanId(SipxOpenAcdService.BEAN_ID);
        setService(sipxService);
    }

    public void saveService() {
        if (!TapestryUtils.isValid(this)) {
            return;
        }
        SipxService service = getService();
        getSipxServiceManager().storeService(service);
    }

}
