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
import org.sipfoundry.sipxconfig.setting.BeanWithGroups;
import org.sipfoundry.sipxconfig.setting.Group;
import org.sipfoundry.sipxconfig.setting.SettingDao;

public abstract class E911GroupComponent extends BaseComponent implements PageBeginRenderListener {

    @Persist
    public abstract void setGroupId(Integer id);

    public abstract Integer getGroupId();
    
    @Parameter
    public abstract Group getGroup();

    public abstract void setGroup(Group group);
    
    @Persist
    public abstract BeanWithGroups getBean();

    public abstract void setBean(BeanWithGroups bean);

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
        	setE911LocationId((Integer) (
        			getGroup().inherhitSettingsForEditing(getBean()).getSetting(
        					E911LocationSelectionModel.E911_LOCATION_SETTINGS).getTypedValue())
        			);
        }
    }

    public void onApply() {
    	//Dont set id if e911 location is negative
    	Integer id = getE911LocationId();
        if (id.intValue() < 0) { 
            id = null;
        }
        
        getGroup().inherhitSettingsForEditing(getBean()).getSetting(
        		E911LocationSelectionModel.E911_LOCATION_SETTINGS).setTypedValue(id);
    	getSettingDao().saveGroup(getGroup());
    }	
}

