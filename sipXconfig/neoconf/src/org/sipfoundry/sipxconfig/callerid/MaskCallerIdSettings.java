package org.sipfoundry.sipxconfig.callerid;

import java.util.Collection;
import java.util.Collections;

import org.sipfoundry.sipxconfig.cfgmgt.DeployConfigOnEdit;
import org.sipfoundry.sipxconfig.feature.Feature;
import org.sipfoundry.sipxconfig.proxy.ProxyManager;
import org.sipfoundry.sipxconfig.setting.PersistableSettings;
import org.sipfoundry.sipxconfig.setting.Setting;

public class MaskCallerIdSettings extends PersistableSettings implements DeployConfigOnEdit{
	
	@Override
	public Collection<Feature> getAffectedFeaturesOnChange() {
		return Collections.singleton((Feature) ProxyManager.FEATURE);
	}

	@Override
	public String getBeanId() {
		return "mcidSettings";
	}

	@Override
	protected Setting loadSettings() {
		return getModelFilesContext().loadModelFile("callerid/mcid.xml");
	}


}
