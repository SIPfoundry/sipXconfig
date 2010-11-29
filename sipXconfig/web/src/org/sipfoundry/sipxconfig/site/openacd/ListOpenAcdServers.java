/*
 *
 *
 * Copyright (C) 2010 eZuce, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package org.sipfoundry.sipxconfig.site.openacd;

import java.util.Collection;

import org.apache.tapestry.IPage;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.annotations.InjectPage;
import org.sipfoundry.sipxconfig.admin.commserver.Location;
import org.sipfoundry.sipxconfig.admin.commserver.LocationsManager;
import org.sipfoundry.sipxconfig.components.SipxBasePage;
import org.sipfoundry.sipxconfig.service.SipxOpenAcdService;
import org.sipfoundry.sipxconfig.service.SipxServiceManager;

public abstract class ListOpenAcdServers extends SipxBasePage {
    @InjectObject("spring:locationsManager")
    public abstract LocationsManager getLocationsManager();

    @InjectObject("spring:sipxServiceManager")
    public abstract SipxServiceManager getSipxServiceManager();

    @InjectPage(OpenAcdServerPage.PAGE)
    public abstract IPage getOpenAcdServerPage();

    public abstract void setLocations(Collection<Location> locations);

    public abstract Location getCurrentRow();

    public abstract void setCurrentRow(Location location);

    public IPage editOpenAcdServer(Location l) {
        OpenAcdServerPage page = (OpenAcdServerPage) getOpenAcdServerPage();
        page.setSipxLocation(l);
        return page;
    }

    public boolean onFormSubmit(IRequestCycle cycle) {
        // TODO: implement activating ACD server
        return true;
    }

    public Collection<Location> getOpenAcdLocations() {
        return getLocationsManager().getLocationsForService(
                getSipxServiceManager().getServiceByBeanId(SipxOpenAcdService.BEAN_ID));
    }

}
