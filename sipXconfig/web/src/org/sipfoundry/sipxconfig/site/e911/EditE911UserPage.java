package org.sipfoundry.sipxconfig.site.e911;

import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;
import org.sipfoundry.sipxconfig.e911.E911Manager;
import org.sipfoundry.sipxconfig.site.user_portal.UserBasePage;

public abstract class EditE911UserPage extends UserBasePage implements PageBeginRenderListener {
    public static final String PAGE = "e911/EditE911UserPage";

    public abstract User getEditedUser();

    public abstract void setEditedUser(User user);

    @InjectObject("spring:e911Manager")
    public abstract E911Manager getE911Manager();

    public abstract boolean isE911TabActive();

    public abstract void setE911TabActive(boolean b);

    @Override
    @Bean
    public abstract SipxValidationDelegate getValidator();

    @Override
    public void pageBeginRender(PageEvent event_) {
        if (getEditedUser() == null) {
            setEditedUser(getUser());
        }
        setE911TabActive(true);
    }
}
