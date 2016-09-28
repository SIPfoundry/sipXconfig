package org.sipfoundry.sipxconfig.bridge.config;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sipfoundry.sipxconfig.bridge.BridgeSbc;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.domain.Domain;
import org.sipfoundry.sipxconfig.domain.DomainManager;
import org.sipfoundry.sipxconfig.gateway.SipTrunk;

public abstract class AbstractSIPProfileConfiguration extends AbstractTrunkConfiguration {

	/** Default Freeswitch module configuration directory **/
	private static final String DEFAULT_CONFIG_DIRECTORY = "sip_profiles";
	
	private String m_configDirectory;
	private DomainManager m_domainManager;
	
	public abstract String getProfileName();
	
	@Override
    public void write(Writer writer, Location location, BridgeSbc bridgeSbc) 
    		throws IOException {
		List<SipTrunk> itsps = bridgeSbc.getMySipItsps();
		
		write(writer, new SIPProfileContextGenerator()
				.setSbc(bridgeSbc)
				.setItsps(itsps)
				.setPublicIp(bridgeSbc.getGlobalSipIp())
				.setPublicPort(bridgeSbc.getGlobalSipPort())
				.setDomain(m_domainManager.getDomain())
				.getContext());
    }
	
	@Override
    public String getFileName() {
        return MODULE_NAME + "/" + getConfigDirectory() + "/" + getProfileName() + ".xml";
    }
	
	@Override
    protected String getTemplate() {
        return MODULE_NAME + "/sofia_" + getProfileName() + ".xml.vm";
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
    	
    	public SIPProfileContextGenerator setSbc(BridgeSbc bridgeSbc) {
			put("sbc", bridgeSbc);
			return this;
		}

		public SIPProfileContextGenerator setItsps(List<SipTrunk> itsps) {
			put("itsps", itsps);
			return this;
		}

		public SIPProfileContextGenerator setDomain(Domain domain) {
			put("domain", domain);
			put("realm", domain.getSipRealm());
			return this;
		}
		
    	public SIPProfileContextGenerator setPublicIp(String publicIp) {
    		put("externalPublicIp", publicIp);
    		return this;
    	}
    	
    	public SIPProfileContextGenerator setPublicPort(int publicPort) {
			put("externalPublicPort", publicPort);
			return this;
		}
    }
	
}
