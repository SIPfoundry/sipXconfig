package org.sipfoundry.sipxconfig.site.setting;

import org.sipfoundry.sipxconfig.setting.Group;

public abstract class BulkVMAction extends BulkGroupAction {

    private String m_label;
    
    public BulkVMAction(Group group, String label) {
        super(group);
        m_label = label;
    }
    
    @Override
    public String getLabel(Object option_, int index_) {
        return m_label;
    }

    @Override
    public String squeezeOption(Object option_, int index_) {
        return m_label;
    }
}
