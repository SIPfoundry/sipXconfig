/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */
package org.sipfoundry.sipxconfig.site.admin;

import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.sipfoundry.sipxconfig.components.SipxBasePage;
import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;
import org.sipfoundry.sipxconfig.components.TapestryUtils;
import org.sipfoundry.sipxconfig.moh.MusicOnHoldManager;

public abstract class MusicOnHold extends SipxBasePage implements PageBeginRenderListener {

    @Bean
    public abstract SipxValidationDelegate getValidator();

    @InjectObject("spring:musicOnHoldManager")
    public abstract MusicOnHoldManager getMusicOnHoldManager();

//    @InjectObject("spring:sipxServiceManager")
//    public abstract SipxServiceManager getSipxServiceManager();

    public abstract String getAsset();

    public abstract Boolean getAudioDirectoryEmpty();

    public abstract void setAudioDirectoryEmpty(Boolean isAudioDirectoryEmpty);

//    public abstract SipxService getSipxService();
//
//    public abstract void setSipxService(SipxService service);

    public void pageBeginRender(PageEvent event_) {
        boolean managerDirectoryEmpty = getMusicOnHoldManager().isAudioDirectoryEmpty();
        if (getAudioDirectoryEmpty() == null) {
            setAudioDirectoryEmpty(managerDirectoryEmpty);
        } else if (managerDirectoryEmpty != getAudioDirectoryEmpty()) {
            setAudioDirectoryEmpty(managerDirectoryEmpty);
        }

//        if (getSipxService() == null) {
//            SipxService sipxService = getSipxServiceManager().getServiceByBeanId(SipxFreeswitchService.BEAN_ID);
//            setSipxService(sipxService);
//        }
    }

//    public Setting getMohSetting() {
//        return getSipxService().getSettings().getSetting(SipxFreeswitchService.FREESWITCH_MOH_SOURCE);
//    }

    public void saveValid() {
        if (!TapestryUtils.isValid(this)) {
            return;
        }
//        SipxService service = getSipxService();
//        getSipxServiceManager().storeService(service);
    }
}
