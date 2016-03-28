package org.sipfoundry.sipxconfig.site.e911;

import java.util.List;

import org.apache.tapestry.BaseComponent;
import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.apache.tapestry.form.IPropertySelectionModel;
import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;
import org.sipfoundry.sipxconfig.e911.E911Location;
import org.sipfoundry.sipxconfig.e911.E911Manager;
import org.sipfoundry.sipxconfig.phone.Phone;
import org.sipfoundry.sipxconfig.phone.PhoneContext;
import org.sipfoundry.sipxconfig.setting.BeanWithGroups;

public abstract class E911PhoneComponent extends BaseComponent implements PageBeginRenderListener {

	@Parameter
    public abstract Phone getPhone();

    public abstract void setPhone(Phone phone);
    
    @Persist
    public abstract BeanWithGroups getBean();

    public abstract void setBean(BeanWithGroups bean);

    @InjectObject("spring:e911Manager")
    public abstract E911Manager getE911Manager();
    
    @InjectObject("spring:phoneContext")
    public abstract PhoneContext getPhoneContext();

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
        	setE911LocationId((getPhone().getE911LocationId()));
        }
    }

    public void onApply() {
    	//Dont set id if e911 location is negative
    	Integer id = getE911LocationId();
        if (id.intValue() < 0) { 
            id = null;
        }
        
        Phone phone = getPhone();
        phone.setSettingTypedValue(E911LocationSelectionModel.E911_LOCATION_SETTINGS, id);
        getPhoneContext().storePhone(phone);
    }	
}