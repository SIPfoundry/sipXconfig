package org.sipfoundry.sipxconfig.site.oss;

import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.sipfoundry.sipxconfig.components.PageWithCallback;
import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;
import org.sipfoundry.sipxconfig.kamailio.KamailioManager;
import org.sipfoundry.sipxconfig.kamailio.KamailioSettings;
import org.sipfoundry.sipxconfig.oss.OSSCoreManager;
import org.sipfoundry.sipxconfig.oss.OSSCoreSettings;

public abstract class EditSBC extends PageWithCallback implements PageBeginRenderListener {
    public static final String PAGE = "oss/EditSBC";

    @Bean
    public abstract SipxValidationDelegate getValidator();

    @InjectObject("spring:osscoreManager")
    public abstract OSSCoreManager getOSSCoreManager();

    public abstract OSSCoreSettings getSettings();

    public abstract void setSettings(OSSCoreSettings settings);

    @Override
    public void pageBeginRender(PageEvent arg0) {
        if (getSettings() == null) {
            setSettings(getOSSCoreManager().getSettings());
        }
    }

    public void apply() {
    	getOSSCoreManager().saveSettings(getSettings());
    }
    
    
}