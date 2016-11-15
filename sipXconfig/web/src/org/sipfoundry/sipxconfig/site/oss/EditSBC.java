package org.sipfoundry.sipxconfig.site.oss;

import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.sipfoundry.sipxconfig.common.UserException;
import org.sipfoundry.sipxconfig.components.PageWithCallback;
import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;
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
        int lowestPort = getSettings().getRtpLowestPort();
        int highestPort = getSettings().getRtpHighestPort();
        if(lowestPort > highestPort) {
            throw new UserException(getMessages().getMessage("error.invalid.rtp.port"));
        }

    	getOSSCoreManager().saveSettings(getSettings());
    }
    
    
}
