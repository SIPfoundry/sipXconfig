package org.sipfoundry.sipxconfig.site.e911;

import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.sipfoundry.sipxconfig.components.PageWithCallback;
import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;
import org.sipfoundry.sipxconfig.e911.E911Location;
import org.sipfoundry.sipxconfig.e911.E911Manager;

public abstract class EditE911LocationPage extends PageWithCallback implements PageBeginRenderListener {
    public static final String PAGE = "e911/EditE911LocationPage";

    @Persist
    public abstract Integer getE911LocationId();
    
    public abstract void setE911LocationId(Integer id);
    
    public abstract E911Location getE911Location();

    public abstract void setE911Location(E911Location location);
    
    @Bean
    public abstract SipxValidationDelegate getValidator();

    @InjectObject("spring:e911Manager")
    public abstract E911Manager getE911Manager();

    @Override
    public void pageBeginRender(PageEvent event) {
        if (getE911LocationId() != null) {
            setE911Location(getE911Manager().findLocationById(getE911LocationId()));
        } 
        
        if (getE911Location() == null) {
            setE911Location(new E911Location());
        }

    }

    public void save() {
        getE911Manager().saveLocation(getE911Location());
        setE911LocationId(getE911Location().getId());
    }
}
