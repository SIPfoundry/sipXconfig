package org.sipfoundry.sipxconfig.paging.config;

import java.util.List;

import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.paging.PagingContext;

public interface FSPagingProvider {

	public List<String> getRequiredModules(PagingContext feature, Location location);
}
