package org.sipfoundry.sipxconfig.bridge.config;

import java.util.List;

import org.sipfoundry.sipxconfig.bridge.BridgeSbcContext;
import org.sipfoundry.sipxconfig.commserver.Location;

public interface FSTrunkProvider {
	
	/**
     * return a list of modules in modules.conf.xml you want loaded.
     */
    public List<String> getRequiredModules(BridgeSbcContext feature, Location location);
    
}
