/*
 *
 *
 * Copyright (C) 2009 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.site.admin.commserver;

import org.apache.tapestry.IAsset;
import org.apache.tapestry.annotations.Asset;
import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.annotations.InjectPage;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.sipfoundry.sipxconfig.commserver.LocationsManager;
import org.sipfoundry.sipxconfig.components.SelectMap;
import org.sipfoundry.sipxconfig.components.SipxBasePage;
import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;
import org.sipfoundry.sipxconfig.site.admin.WaitingPage;

public abstract class RestartNeededServicesPage extends SipxBasePage implements PageBeginRenderListener {
    public static final Object PAGE = "admin/commserver/RestartNeededServicesPage";

//    @InjectObject("spring:sipxProcessContext")
//    public abstract SipxProcessContext getSipxProcessContext();

    @InjectObject("spring:locationsManager")
    public abstract LocationsManager getLocationsManager();

//    @InjectObject("spring:sipxServiceManager")
//    public abstract SipxServiceManager getSipxServiceManager();
//
//    @InjectObject("spring:restartListener")
//    public abstract RestartListener getRestartListener();

    @Bean
    public abstract SipxValidationDelegate getValidator();

    @Bean
    public abstract SelectMap getSelections();

    @Asset("/images/server.png")
    public abstract IAsset getServerIcon();

    @Asset("/images/service_restart.png")
    public abstract IAsset getRestartIcon();

//    public abstract RestartNeededService getCurrentRow();
//
//    public abstract Collection<RestartNeededService> getRestartNeededServices();
//
//    public abstract void setRestartNeededServices(Collection<RestartNeededService> restartNeededServices);

    @InjectPage(value = WaitingPage.PAGE)
    public abstract WaitingPage getWaitingPage();

    public void pageBeginRender(PageEvent event) {
//        if (getRestartNeededServices() == null) {
//            setRestartNeededServices(getSipxProcessContext().getRestartNeededServices());
//        }
    }

//    public String getServiceLabel() {
//        String serviceBeanId = getCurrentRow().getServiceBeanId();
//        String key = "label." + serviceBeanId;
//        return getMessage(getMessages(), key, serviceBeanId);
//    }

//    public IPage restart() {
//        Collection<RestartNeededService> beans = getSelections().getAllSelected();
//        if (beans == null) {
//            return null;
//        }
//
//        // Restart needed services on each affected location
//        RestartListener restartListener = getRestartListener();
//        restartListener.setServicesMap(createLocationToServiceMap(beans));
//        if (listenerNeeded(beans)) {
//            WaitingPage waitingPage = getWaitingPage();
//            waitingPage.setWaitingListener(restartListener);
//            return waitingPage;
//        } else {
//            restartListener.restart();
//            // Forces a page refresh
//            setRestartNeededServices(null);
//            return null;
//        }
//
//    }

//    private boolean listenerNeeded(Collection<RestartNeededService> beans) {
//        for (RestartNeededService bean : beans) {
//            if (bean.isConfigurationRestartNeeded()) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private Map<Location, List<SipxService>> createLocationToServiceMap(Collection<RestartNeededService> beans) {
//        Map<Location, List<SipxService>> map = new HashMap<Location, List<SipxService>>();
//        for (RestartNeededService bean : beans) {
//            Location location = getLocationsManager().getLocationByFqdn(bean.getLocation());
//            if (location == null) {
//                continue;
//            }
//            List<SipxService> services = map.get(location);
//            if (services == null) {
//                services = new ArrayList<SipxService>();
//                map.put(location, services);
//            }
//            SipxService service = getSipxServiceManager().getServiceByBeanId(bean.getServiceBeanId());
//            services.add(service);
//        }
//        return map;
//    }
//
//    public void ignore() {
//        Collection<RestartNeededService> beans = getSelections().getAllSelected();
//        if (beans == null) {
//            return;
//        }
//
//        getSipxProcessContext().unmarkServicesToRestart(beans);
//
//        // Forces a page refresh
//        setRestartNeededServices(null);
//    }
}
