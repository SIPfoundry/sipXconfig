package org.sipfoundry.sipxconfig.bridge.config;

import java.io.IOException;
import java.io.Writer;

import org.sipfoundry.sipxconfig.bridge.BridgeSbc;
import org.sipfoundry.sipxconfig.commserver.Location;

public class GatewayConfiguration extends AbstractTrunkConfiguration {

	@Override
	public void write(Writer writer, Location location, BridgeSbc bridgeSbc) throws IOException {
		
	}
	
	@Override
	public String getFileName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getTemplate() {
		// TODO Auto-generated method stub
		return null;
	}

}
