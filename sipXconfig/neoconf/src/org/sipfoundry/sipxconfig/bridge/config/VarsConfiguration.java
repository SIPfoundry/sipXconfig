package org.sipfoundry.sipxconfig.bridge.config;

import java.io.IOException;
import java.io.Writer;

import org.sipfoundry.sipxconfig.bridge.BridgeSbc;
import org.sipfoundry.sipxconfig.bridge.BridgeSbcContext;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.feature.FeatureManager;
import org.springframework.beans.factory.annotation.Required;

public class VarsConfiguration extends AbstractTrunkConfiguration {
    private FeatureManager m_featureManager;

    @Override
    public void write(Writer writer, Location location, BridgeSbc settings) throws IOException {
        write(writer, new ContextGenerator()
        		.setTrunkDomain(getTrunkDomain(location))
        		.setInternalPort(settings.getPort())
        		.setExternalPort(settings.getExternalSipPort())
        		.getContext());
    }

    protected String getTrunkDomain(Location location) {
        if (m_featureManager.isFeatureEnabled(BridgeSbcContext.FEATURE, location)) {
            return location.getFqdn();
        }
        return "$${local_ip_v4}";
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

    @Required
    public FeatureManager getFeatureManager() {
        return m_featureManager;
    }
    
    /**
	 * class ContextGenerator
	 */
    static class ContextGenerator extends BasicContextGenerator {
    	
    	public ContextGenerator() {
    		super();
    	}

    	public ContextGenerator setTrunkDomain(String domain) {
    		put("sipxtrunkDomain", domain);
    		return this;
    	}
    	
    	public ContextGenerator setExternalPort(int port) {
    		put("externalSipPort", port);
    		put("externalTlsPort", port + 1);
    		return this;
    	}
    	
    	public ContextGenerator setInternalPort(int port) {
    		put("internalSipPort", port);
    		put("internalTlsPort", port + 1);
    		return this;
    	}
    	
    }    
}
