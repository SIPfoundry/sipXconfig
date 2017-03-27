package org.sipfoundry.sipxconfig.bridge.config;

import java.io.IOException;
import java.io.Writer;

import org.sipfoundry.sipxconfig.bridge.BridgeSbc;
import org.sipfoundry.sipxconfig.commserver.Location;

public class ModuleXmlRpcConfiguration extends AbstractModuleConfiguration {

    @Override
    public void write(Writer writer, Location location, BridgeSbc settings) throws IOException {
        write(writer, new ContextGenerator()
        		.setXmlRpcPort(settings.getXmlRpcPort())
        		.getContext());
    }
    
    @Override
    public String getConfigName() {
        return "xml_rpc";
    }
    
    /**
	 * class ContextGenerator
	 */
    static class ContextGenerator extends BasicContextGenerator {
    	
    	public ContextGenerator() {
    		super();
    	}
    	
    	public ContextGenerator setXmlRpcPort(int port) {
    		put("xmlRpcPort", port);
    		return this;
    	}    	
    }
}
