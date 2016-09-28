package org.sipfoundry.sipxconfig.bridge.config;

import org.apache.commons.lang.StringUtils;

public abstract class AbstractModuleConfiguration extends AbstractTrunkConfiguration {
	
	/** Default Freeswitch module configuration directory **/
	private static final String DEFAULT_CONFIG_DIRECTORY = "autoload_configs";
	
	private String m_configDirectory;
	
	public abstract String getConfigName();
	
	@Override
    public String getFileName() {
        return MODULE_NAME + "/" + getConfigDirectory() + "/" + getConfigName() + ".conf.xml";
    }
	
	@Override
    protected String getTemplate() {
        return MODULE_NAME + "/module_" + getConfigName() + ".conf.xml.vm";
    }

	public String getConfigDirectory() {
		return StringUtils.isNotBlank(m_configDirectory) 
				? m_configDirectory : DEFAULT_CONFIG_DIRECTORY;
	}

	public void setConfigDirectory(String configDirectory) {
		this.m_configDirectory = configDirectory;
	}
	
	
}
