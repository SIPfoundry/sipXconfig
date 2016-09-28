package org.sipfoundry.sipxconfig.bridge.config;

import org.apache.velocity.VelocityContext;
import org.sipfoundry.sipxconfig.common.VersionInfo;

public class BasicContextGenerator {

	private VelocityContext m_context;
	
	public BasicContextGenerator() {
		this("$");
	}
	
	public BasicContextGenerator(String dollarKeyword) {
		m_context = new VelocityContext();
		m_context.put("dollar", dollarKeyword);
		m_context.put("version", new VersionInfo().getVersion());
	}
	
	public BasicContextGenerator put(String key, Object value) {
		m_context.put(key, value);
		return this;
	}
	
	public VelocityContext getContext() {
		return m_context;
	}
	
}
