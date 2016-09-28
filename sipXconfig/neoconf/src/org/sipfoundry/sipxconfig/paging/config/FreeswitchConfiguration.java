package org.sipfoundry.sipxconfig.paging.config;

import java.io.IOException;
import java.io.Writer;

import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.domain.Domain;
import org.sipfoundry.sipxconfig.domain.DomainManager;
import org.sipfoundry.sipxconfig.paging.PagingContext;

public class FreeswitchConfiguration extends AbstractPagingConfiguration {
	
	private static final String DEFAULT_FREESWITCH_CONF_NAME = "freeswitch";
	
    private DomainManager m_domainManager;

    @Override
    public void write(Writer writer, Location location, PagingContext bridgeSbc) throws IOException {
        write(writer, new ContextGenerator()
        		.setDomain(m_domainManager.getDomain())
        		.getContext());
    }
    
    @Override
    public String getFileName() {
        return MODULE_NAME + "/" + DEFAULT_FREESWITCH_CONF_NAME + ".xml";
    }

    @Override
    protected String getTemplate() {
        return MODULE_NAME + "/" + DEFAULT_FREESWITCH_CONF_NAME + ".xml.vm";
    }

    public void setDomainManager(DomainManager domainManager) {
        m_domainManager = domainManager;
    }
    
    /**
	 * class ContextGenerator
	 */
    static class ContextGenerator extends BasicContextGenerator {
    	
    	public ContextGenerator() {
    		super();
    	}
    	
    	public ContextGenerator setDomain(Domain domain) {
    		put("domain", domain);
    		return this;
    	}
    	
    }
}
