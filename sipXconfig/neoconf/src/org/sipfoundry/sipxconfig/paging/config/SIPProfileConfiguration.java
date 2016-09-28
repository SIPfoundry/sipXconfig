package org.sipfoundry.sipxconfig.paging.config;

import java.io.IOException;
import java.io.Writer;

import org.apache.commons.lang.StringUtils;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.domain.Domain;
import org.sipfoundry.sipxconfig.domain.DomainManager;
import org.sipfoundry.sipxconfig.paging.PagingContext;

public class SIPProfileConfiguration extends AbstractPagingConfiguration {

	/** Default Freeswitch module configuration directory **/
	private static final String DEFAULT_CONFIG_DIRECTORY = "sip_profiles";
	
	private String m_configDirectory;
	private DomainManager m_domainManager;
	
	@Override
    public void write(Writer writer, Location location, PagingContext pagingContext) 
    		throws IOException {
		write(writer, new SIPProfileContextGenerator()
				.setDomain(m_domainManager.getDomain())
				.getContext());
    }
	
	@Override
    public String getFileName() {
        return MODULE_NAME + "/" + getConfigDirectory() + "/sipxpage.xml";
    }
	
	@Override
    protected String getTemplate() {
        return MODULE_NAME + "/sofia_sipxpage.xml.vm";
    }

	public String getConfigDirectory() {
		return StringUtils.isNotBlank(m_configDirectory) 
				? m_configDirectory : DEFAULT_CONFIG_DIRECTORY;
	}

	public void setConfigDirectory(String configDirectory) {
		this.m_configDirectory = configDirectory;
	}
	
	public void setDomainManager(DomainManager domainManager) {
		this.m_domainManager = domainManager;
	}
	
	/**
	 * class SIPProfileContextGenerator
	 */
    static class SIPProfileContextGenerator extends BasicContextGenerator {
    	
    	public SIPProfileContextGenerator() {
    		super();
    	}
    	
    	public SIPProfileContextGenerator setDomain(Domain domain) {
			put("domain", domain);
			put("realm", domain.getSipRealm());
			return this;
		}
    }
	
}
