package org.sipfoundry.sipxconfig.site.callerid;

import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.apache.tapestry.form.IPropertySelectionModel;
import org.sipfoundry.sipxconfig.callerid.MaskCallerIdBean;
import org.sipfoundry.sipxconfig.callerid.MaskCallerIdManager;
import org.sipfoundry.sipxconfig.components.PageWithCallback;
import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;
import org.sipfoundry.sipxconfig.components.TapestryUtils;

public abstract class EditMaskCallerIdPage extends PageWithCallback implements PageBeginRenderListener {
	public static final String PAGE = "callerid/EditMaskCallerIdPage";

    @Bean
    public abstract SipxValidationDelegate getValidator();

    @Persist
    public abstract Integer getMaskId();
    
    public abstract void setMaskId(Integer id);
    
    public abstract void setTypeModel(IPropertySelectionModel model);

    public abstract void setMaskCallerId(MaskCallerIdBean ccid);

    public abstract MaskCallerIdBean getMaskCallerId();

    @InjectObject("spring:maskCallerIdManager")
    public abstract MaskCallerIdManager getMaskCallerIdManager();

    @Override
    public void pageBeginRender(PageEvent event) {
        if (!TapestryUtils.isValid(this)) {
            return;
        }

        if (getMaskId() != null) {
            setMaskCallerId(getMaskCallerIdManager().getMaskCallerIdById(getMaskId()));
        } else {
            setMaskCallerId(new MaskCallerIdBean());
        }
    }

    public void save() {
        if (!TapestryUtils.isValid(this)) {
            return;
        }

        MaskCallerIdBean customCallerId = getMaskCallerId();
        getMaskCallerIdManager().saveMaskCallerId(customCallerId);
        setMaskId(customCallerId.getId());
    }
}
