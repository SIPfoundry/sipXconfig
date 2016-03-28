package org.sipfoundry.sipxconfig.site.e911;

import java.util.List;

import org.apache.tapestry.BaseComponent;
import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.apache.tapestry.form.IPropertySelectionModel;
import org.sipfoundry.sipxconfig.common.CoreContext;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;
import org.sipfoundry.sipxconfig.e911.E911Location;
import org.sipfoundry.sipxconfig.e911.E911Manager;
import org.sipfoundry.sipxconfig.setting.SettingDao;

public abstract class E911UserComponent extends BaseComponent implements PageBeginRenderListener {

    @Parameter(required = true)
    public abstract User getUser();

    public abstract void setUser(User user);

    @InjectObject("spring:coreContext")
    public abstract CoreContext getCoreContext();

    @InjectObject("spring:e911Manager")
    public abstract E911Manager getE911Manager();
    
    @InjectObject(value = "spring:settingDao")
    public abstract SettingDao getSettingDao();

    public abstract IPropertySelectionModel getE911LocationTypeModel();

    public abstract void setE911LocationTypeModel(IPropertySelectionModel model);

    public abstract Integer getE911LocationId();

    public abstract void setE911LocationId(Integer locationId);

    @Bean
    public abstract SipxValidationDelegate getValidator();

    @Override
    public void pageBeginRender(PageEvent event) {
        // Init. the e911 location dropdown menu.
    	List<E911Location> locationList = getE911Manager().findLocations();

        // Sort list alphanumerically.
    	E911LocationSelectionModel model = new E911LocationSelectionModel(locationList);
        setE911LocationTypeModel(model);
        if (!event.getRequestCycle().isRewinding()) {
        	setE911LocationId(getUser().getE911LocationId());
        }
    }

    public void onApply() {
    	Integer id = getE911LocationId();
    	//BeanId should be negative for the default value
        if (id.intValue() < 0) { 
            id = null;
        }
        
        User user = getUser();
        user.setSettingTypedValue(E911LocationSelectionModel.E911_LOCATION_SETTINGS, id);
        getCoreContext().saveUser(user);
    }	
}
