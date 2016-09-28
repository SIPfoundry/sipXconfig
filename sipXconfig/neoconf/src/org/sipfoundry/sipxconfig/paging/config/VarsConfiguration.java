package org.sipfoundry.sipxconfig.paging.config;

import java.io.IOException;
import java.io.Writer;

import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.feature.FeatureManager;
import org.sipfoundry.sipxconfig.paging.PagingContext;
import org.sipfoundry.sipxconfig.paging.PagingSettings;
import org.springframework.beans.factory.annotation.Required;

public class VarsConfiguration extends AbstractPagingConfiguration {
    
	private FeatureManager m_featureManager;

    @Override
    public void write(Writer writer, Location location, PagingContext pagingContext) throws IOException {
    	PagingSettings settings = pagingContext.getSettings();
        write(writer, new VarContextGenerator()
        		.setDomain(getDomain(location))
        		.setTcpPort(settings.getSipTcpPort())
        		.setUdpPort(settings.getSipUdpPort())
        		.setTlsPort(settings.getSipTlsPort())
        		.getContext());
    }

    protected String getDomain(Location location) {
        if (m_featureManager.isFeatureEnabled(PagingContext.FEATURE, location)) {
            return location.getFqdn();
        }
        return "${dollar}${dollar}{local_ip_v4}";
    }
    
    @Override
    public String getFileName() {
        return MODULE_NAME + "/" + "vars.xml";
    }

    @Override
    protected String getTemplate() {
        return MODULE_NAME + "/" + "vars.xml.vm";
    }

    @Required
    public void setFeatureManager(FeatureManager featureManager) {
        m_featureManager = featureManager;
    }
    
    public FeatureManager getFeatureManager() {
        return m_featureManager;
    }
    
    /**
	 * class ContextGenerator
	 */
    static class VarContextGenerator extends BasicContextGenerator {
    	
    	public VarContextGenerator() {
    		super();
    	}

    	public VarContextGenerator setDomain(String domain) {
    		put("sipxpageDomain", domain);
    		return this;
    	}
    	
    	public VarContextGenerator setTcpPort(int port) {
    		put("tcpPort", port);
    		return this;
    	}
    	
    	public VarContextGenerator setUdpPort(int port) {
    		put("udpPort", port);
    		return this;
    	}
    	
    	public VarContextGenerator setTlsPort(int port) {
    		put("tlsPort", port);
    		return this;
    	}
    	
    }    
}
