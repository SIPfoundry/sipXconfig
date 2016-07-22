package org.sipfoundry.sipxconfig.site.callerid;

import java.util.Collection;
import java.util.List;

import org.apache.tapestry.IPage;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.callback.PageCallback;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.sipfoundry.sipxconfig.callerid.MaskCallerIdBean;
import org.sipfoundry.sipxconfig.callerid.MaskCallerIdManager;
import org.sipfoundry.sipxconfig.components.SelectMap;
import org.sipfoundry.sipxconfig.components.SipxBasePage;
import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;

public abstract class MaskCallerIdPage extends SipxBasePage implements PageBeginRenderListener {

    @Bean
    public abstract SipxValidationDelegate getValidator();

    @InjectObject("spring:maskCallerIdManager")
    public abstract MaskCallerIdManager getMaskCallerIdManager();

    @Bean
    public abstract SelectMap getSelections();
    
    public abstract Collection<MaskCallerIdBean> getSelectedRows();
    
    public abstract List<MaskCallerIdBean> getMaskCallerIds();

    public abstract void setMaskCallerIds(List<MaskCallerIdBean> ids);
    
    public abstract MaskCallerIdBean getCurrentRow();

    public abstract void setCurrentRow(MaskCallerIdBean ccid);

    @Override
    public void pageBeginRender(PageEvent arg0) {
        setMaskCallerIds(getMaskCallerIdManager().getMaskCallerIds());
    }

    public IPage addMaskCallerId(IRequestCycle cycle) {
        EditMaskCallerIdPage page = (EditMaskCallerIdPage) cycle.getPage(EditMaskCallerIdPage.PAGE);
        page.setMaskId(null);
        page.setCallback(new PageCallback(this));
        return page;
    }

    public IPage editMaskCallerId(IRequestCycle cycle, MaskCallerIdBean id) {
    	EditMaskCallerIdPage page = (EditMaskCallerIdPage) cycle.getPage(EditMaskCallerIdPage.PAGE);
        page.setMaskId(id.getId());
        page.setCallback(new PageCallback(this));
        return page;
    }

    public void delete() {
        @SuppressWarnings("unchecked")
		Collection<Integer> ids = getSelections().getAllSelected();
        if (!ids.isEmpty()) {
        	getMaskCallerIdManager().deleteMaskCallerIds(ids);
        }
    }

}
