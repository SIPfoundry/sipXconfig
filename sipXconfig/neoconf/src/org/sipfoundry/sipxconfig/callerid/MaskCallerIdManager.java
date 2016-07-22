package org.sipfoundry.sipxconfig.callerid;

import java.util.Collection;
import java.util.List;

import org.sipfoundry.sipxconfig.common.ReplicableProvider;
import org.sipfoundry.sipxconfig.feature.GlobalFeature;

public interface MaskCallerIdManager extends ReplicableProvider {
	public static final GlobalFeature FEATURE = new GlobalFeature("maskCallerId");

    List<MaskCallerIdBean> getMaskCallerIds();

    MaskCallerIdBean getMaskCallerIdById(Integer id);

    MaskCallerIdSettings getSettings();
    
    void saveMaskCallerId(MaskCallerIdBean id);
    
    void saveSettings(MaskCallerIdSettings settings);

    void deleteMaskCallerIds(Collection<Integer> ids);
    
}
