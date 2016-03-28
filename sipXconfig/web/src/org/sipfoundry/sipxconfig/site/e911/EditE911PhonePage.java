package org.sipfoundry.sipxconfig.site.e911;

import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;
import org.sipfoundry.sipxconfig.e911.E911Manager;
import org.sipfoundry.sipxconfig.phone.Phone;
import org.sipfoundry.sipxconfig.site.phone.PhoneBasePage;

public abstract class EditE911PhonePage extends PhoneBasePage implements PageBeginRenderListener {
    public static final String PAGE = "e911/EditE911PhonePage";

    @InjectObject("spring:e911Manager")
    public abstract E911Manager getE911Manager();

    public abstract boolean isE911TabActive();

    public abstract void setE911TabActive(boolean b);
    
    @Bean
    public abstract SipxValidationDelegate getValidator();

    @Override
    public void pageBeginRender(PageEvent event) {
    	Phone phone = getPhoneContext().loadPhone(getPhoneId());
    	
        //FIXME
        // - Should we use phone component? or integrate it directly on Phone Page Navigation
        // - Investigate why pageBeginRender component is not triggered. For now manually call this
        E911PhoneComponent e911 = (E911PhoneComponent) getComponent("e911PhoneComp");
        e911.setPhone(phone);
        e911.pageBeginRender(event);

        setPhone(phone);
        setE911TabActive(true);
    }
}
