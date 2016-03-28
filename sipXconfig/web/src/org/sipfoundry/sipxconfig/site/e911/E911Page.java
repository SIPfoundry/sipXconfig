package org.sipfoundry.sipxconfig.site.e911;

import java.util.Collection;

import org.apache.tapestry.IPage;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.callback.PageCallback;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.sipfoundry.sipxconfig.components.PageWithCallback;
import org.sipfoundry.sipxconfig.components.SelectMap;
import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;
import org.sipfoundry.sipxconfig.e911.E911Location;
import org.sipfoundry.sipxconfig.e911.E911Manager;

public abstract class E911Page extends PageWithCallback implements PageBeginRenderListener {

    @Bean
    public abstract SipxValidationDelegate getValidator();

    @Bean
    public abstract SelectMap getSelections();

    public abstract Collection<E911Location> getSelectedRows();

    public abstract void setCurrentRow(E911Location location);

    @InjectObject("spring:e911Manager")
    public abstract E911Manager getE911Manager();

    public abstract Collection<E911Location> getE911Locations();

    public abstract void setE911Locations(Collection<E911Location> location);

    public abstract E911Location getE911Location();

    @Override
    public void pageBeginRender(PageEvent event) {
        setE911Locations(getE911Manager().findLocations());
    }

    public IPage addLocation(IRequestCycle cycle) {
        EditE911LocationPage page = (EditE911LocationPage) cycle.getPage(EditE911LocationPage.PAGE);
        page.setE911LocationId(null);
        page.setCallback(new PageCallback(this));
        return page;
    }

    public IPage editLocation(IRequestCycle cycle, E911Location location) {
        EditE911LocationPage page = (EditE911LocationPage) cycle.getPage(EditE911LocationPage.PAGE);
        page.setE911LocationId(location.getId());
        page.setCallback(new PageCallback(this));
        return page;
    }

    public void deleteLocations() {
        @SuppressWarnings("unchecked")
		Collection<Integer> ids = getSelections().getAllSelected();
        
        if (!ids.isEmpty()) {
            getE911Manager().deleteLocations(ids);
        }
    }
}
