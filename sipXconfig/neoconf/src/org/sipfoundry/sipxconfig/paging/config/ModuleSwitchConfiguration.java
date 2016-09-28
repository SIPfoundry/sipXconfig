package org.sipfoundry.sipxconfig.paging.config;

import java.io.IOException;
import java.io.Writer;

import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.paging.PagingContext;
import org.sipfoundry.sipxconfig.paging.PagingSettings;

public class ModuleSwitchConfiguration extends AbstractModuleConfiguration {

	@Override
	public void write(Writer writer, Location location, PagingContext pagingContext) throws IOException {
		PagingSettings settings = pagingContext.getSettings();
        write(writer, new SwitchContextGenerator()
        		.setMaxSession(settings.getMaxSessions())
        		.getContext());
	}
	
	@Override
	public String getConfigName() {
		return "switch";
	}

	/**
	 * class SwitchContextGenerator
	 */
    static class SwitchContextGenerator extends BasicContextGenerator {
    	
    	public SwitchContextGenerator() {
    		super();
    	}
    	
    	public SwitchContextGenerator setMaxSession(int maxSession) {
    		put("maxSession", maxSession);
    		return this;
    	}
    }
}
