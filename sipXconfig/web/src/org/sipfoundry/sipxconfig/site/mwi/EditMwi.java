/**
 *
 *
 * Copyright (c) 2012 eZuce, Inc. All rights reserved.
 * Contributed to SIPfoundry under a Contributor Agreement
 *
 * This software is free software; you can redistribute it and/or modify it under
 * the terms of the Affero General Public License (AGPL) as published by the
 * Free Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 */
package org.sipfoundry.sipxconfig.site.mwi;

import org.apache.commons.lang.StringUtils;
import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.sipfoundry.sipxconfig.components.PageWithCallback;
import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;
import org.sipfoundry.sipxconfig.components.TapestryUtils;
import org.sipfoundry.sipxconfig.feature.FeatureManager;
import org.sipfoundry.sipxconfig.mwi.Mwi;
import org.sipfoundry.sipxconfig.mwi.MwiSettings;
import org.sipfoundry.sipxconfig.setting.AbstractSetting;
import org.sipfoundry.sipxconfig.setting.Setting;

public abstract class EditMwi extends PageWithCallback implements PageBeginRenderListener {
    public static final String PAGE = "mwi/EditMwi";

    @Bean
    public abstract SipxValidationDelegate getValidator();

    @InjectObject("spring:mwi")
    public abstract Mwi getMwi();
    
    @InjectObject("spring:featureManager")
    public abstract FeatureManager getFeatureManager();

    public abstract MwiSettings getSettings();

    public abstract void setSettings(MwiSettings settings);

    @Override
    public void pageBeginRender(PageEvent arg0) {
    	MwiSettings settings = getMwi().getSettings();
    	if(!getFeatureManager().isFeatureEnabled(Mwi.FEATURE)) {
    		//Hide all settings except Phone MWI Subscription behavior
    		Setting statusConfig = settings.getSettings().getSetting("status-config");
        	for(Setting setting : statusConfig.getValues()) {
        		if(!StringUtils.equals(setting.getProfileName(), "MWI_SUBSCRIBE_BEHAVIOR")) {
        			((AbstractSetting) setting).setHidden(true);
        		}
        	}
        	
        	//Hide resource limit as it isn't needed here
        	Setting resourceConfig = settings.getSettings().getSetting("resource-limits");
        	((AbstractSetting) resourceConfig).setHidden(true);
    	}
    	
        if (getSettings() == null) {
            setSettings(settings);
        }
    }

    public void apply() {
        if (!TapestryUtils.validateFDSoftAndHardLimits(this, getSettings(), "resource-limits")) {
            return;
        }
        getMwi().saveSettings(getSettings());
    }
}
