package org.sipfoundry.sipxconfig.bridge.config;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sipfoundry.sipxconfig.bridge.BridgeSbc;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.domain.Domain;
import org.sipfoundry.sipxconfig.domain.DomainManager;
import org.sipfoundry.sipxconfig.gateway.GatewayContext;
import org.sipfoundry.sipxconfig.gateway.SipTrunk;

public abstract class AbstractDialPlanConfiguration extends AbstractTrunkConfiguration {
	
	/** Default Dialplan configuration directory **/
	private static String DEFAULT_DIALPLAN_DIRECTORY = "dialplan";
	
	private String m_dialplanDirectory;
	private GatewayContext m_gatewayContext;
	private DomainManager m_domainManager;
	
	public abstract String getDialPlanName();
	
	@Override
    public void write(Writer writer, Location location, BridgeSbc bridgeSbc) 
    		throws IOException {
		List<SipTrunk> itsps = bridgeSbc.getMySipItsps();
		write(writer, new DialPlanContextGenerator()
				.setItsps(itsps)
				.setDomain(m_domainManager.getDomain())
				.getContext());
    }
	
	@Override
    public String getFileName() {
        return MODULE_NAME + "/" + getDialPlanDirectory() + "/" + getDialPlanName() + ".xml";
    }
	
	@Override
    protected String getTemplate() {
        return MODULE_NAME + "/dialplan_" + getDialPlanName() + ".xml.vm";
    }

	public String getDialPlanDirectory() {
		return StringUtils.isNotBlank(m_dialplanDirectory) 
				? m_dialplanDirectory : DEFAULT_DIALPLAN_DIRECTORY;
	}

	public void setDialPlanDirectory(String moduleDirectory) {
		this.m_dialplanDirectory = moduleDirectory;
	}
	
	public GatewayContext getGatewayContext() {
		return m_gatewayContext;
	}
	
	public void setGatewayContext(GatewayContext gatewayContext) {
		m_gatewayContext = gatewayContext;
	}
	
	public void setDomainManager(DomainManager domainManager) {
		m_domainManager = domainManager;
	}
	
	/**
	 * class DialPlanContextGenerator
	 */
    static class DialPlanContextGenerator extends BasicContextGenerator {
    	
    	public DialPlanContextGenerator() {
    		super();
    	}

		public DialPlanContextGenerator setItsps(List<SipTrunk> itsps) {
    		put("itsps", itsps);
    		return this;
    	}
    	
    	public DialPlanContextGenerator setDomain(Domain domain) {
    		put("domain", domain);
    		put("realm", domain.getSipRealm());
    		return this;
    	}
    	
    }
}
