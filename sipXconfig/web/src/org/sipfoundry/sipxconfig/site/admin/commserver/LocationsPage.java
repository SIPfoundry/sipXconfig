/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */
package org.sipfoundry.sipxconfig.site.admin.commserver;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import org.apache.tapestry.IAsset;
import org.apache.tapestry.IPage;
import org.apache.tapestry.annotations.Asset;
import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.annotations.InjectPage;
import org.apache.tapestry.annotations.InjectState;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.sipfoundry.sipxconfig.admin.commserver.Location;
import org.sipfoundry.sipxconfig.admin.commserver.Location.State;
import org.sipfoundry.sipxconfig.admin.commserver.LocationsManager;
import org.sipfoundry.sipxconfig.admin.logging.AuditLogContext;
import org.sipfoundry.sipxconfig.common.CoreContext;
import org.sipfoundry.sipxconfig.common.UserException;
import org.sipfoundry.sipxconfig.components.SelectMap;
import org.sipfoundry.sipxconfig.components.SipxBasePage;
import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;
import org.sipfoundry.sipxconfig.domain.DomainManager;
import org.sipfoundry.sipxconfig.service.ServiceConfigurator;
import org.sipfoundry.sipxconfig.service.SipxServiceManager;
import org.sipfoundry.sipxconfig.site.UserSession;
import org.sipfoundry.sipxconfig.site.common.BreadCrumb;

public abstract class LocationsPage extends SipxBasePage implements PageBeginRenderListener {
    public static final String PAGE = "admin/commserver/LocationsPage";
    private static final String BLANK = "";

    @InjectState(value = "userSession")
    public abstract UserSession getUserSession();

    @InjectObject("spring:serviceConfigurator")
    public abstract ServiceConfigurator getServiceConfigurator();

    @InjectObject("spring:locationsManager")
    public abstract LocationsManager getLocationsManager();

    @InjectObject("spring:sipxServiceManager")
    public abstract SipxServiceManager getSipxServiceManager();

    @InjectObject("spring:domainManager")
    public abstract DomainManager getDomainManager();

    @InjectObject("spring:coreContext")
    public abstract CoreContext getCoreContext();

    @InjectObject("spring:auditLogContext")
    public abstract AuditLogContext getAuditLogContext();

    @InjectPage(EditLocationPage.PAGE)
    public abstract EditLocationPage getEditLocationPage();

    @Bean
    public abstract SipxValidationDelegate getValidator();

    @Bean
    public abstract SelectMap getSelections();

    @Asset("/images/breadcrumb_separator.png")
    public abstract IAsset getBreadcrumbSeparator();

    @Asset("/images/server.png")
    public abstract IAsset getServerIcon();

    public abstract Location getCurrentRow();

    public abstract Collection<Location> getLocations();

    public abstract void setLocations(Collection<Location> locations);

    public abstract Collection<Integer> getRowsToDelete();

    public abstract void setDetails(String details);

    public abstract String getDetails();

    @InjectPage(LocationStatePage.PAGE)
    public abstract LocationStatePage getLocationStatePage();

    public String getStatusLabel() {
        Location currentLocation = getCurrentRow();
        State state = currentLocation.getState();
        int percent = getAuditLogContext().getProgressPercent(currentLocation);
        String percentStr = BLANK;
        String key = state.getName();
        if (currentLocation.isInNotFinishedState()) {
            key = "ATTEMPT_NOT_FINISHED";
        } else if (currentLocation.isInProgressState()) {
            percentStr = (percent > 0) ? " " + percent + " % " : BLANK;
        }
        return getMessages().getMessage(key) + percentStr;
    }

    public Collection getAllSelected() {
        return getSelections().getAllSelected();
    }

    @Override
    public void pageBeginRender(PageEvent event) {
        if (getLocations() == null) {
            setLocations(Arrays.asList(getLocationsManager().getLocations()));
        }
    }

    public String getLastAttempt() {
        if (getCurrentRow().getLastAttempt() == null) {
            return BLANK;
        }
        long lastAttempt = getCurrentRow().getLastAttempt().getTime();
        Calendar c = Calendar.getInstance(getPage().getLocale());
        c.setTimeInMillis(lastAttempt);
        return new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z").format(c
                .getTime());
    }

    public IPage seeState(Location location) {
        LocationStatePage page = getLocationStatePage();
        page.setServer(location);
        page.setReturnPage(getPage());
        return page;
    }

    public IPage editLocation(int locationId) {
        EditLocationPage editLocationPage = getEditLocationPage();
        editLocationPage.setLocationId(locationId);
        editLocationPage.setReturnPage(this, getBreadCrumbs());
        return editLocationPage;
    }

    public IPage addLocation() {
        EditLocationPage editLocationPage = getEditLocationPage();
        editLocationPage.setLocationId(null);
        editLocationPage.setReturnPage(this, getBreadCrumbs());
        return editLocationPage;
    }

    public void deleteLocations() {
        Collection<Integer> selectedLocations = getSelections()
                .getAllSelected();
        for (Integer id : selectedLocations) {
            Location locationToDelete = getLocationsManager().getLocation(id);
            try {
                getLocationsManager().deleteLocation(locationToDelete);
            } catch (UserException e) {
                getValidator().record(e, getMessages());
            }
        }

        // update locations list
        setLocations(null);
    }

    public void generateProfiles() {
        Collection<Integer> selectedIds = getSelections().getAllSelected();
        Collection<Location> selectedLocations = new ArrayList<Location>();
        for (Location location : getLocations()) {
            if (selectedIds.contains(location.getId())) {
                selectedLocations.add(location);
            }
        }
        getServiceConfigurator().sendProfiles(selectedLocations);
    }

    public boolean isFailedState() {
        return !getCurrentRow().isUninitialized() && getCurrentRow().isInConfigurationErrorState();
    }

    public List<BreadCrumb> getBreadCrumbs() {
        List<BreadCrumb> breadCrumbs = new ArrayList<BreadCrumb>();
        breadCrumbs.add(new BreadCrumb(getPageName(), "&title", getMessages()));
        return breadCrumbs;
    }
}
