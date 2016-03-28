package org.sipfoundry.sipxconfig.e911;

import java.util.Collection;
import java.util.List;

import org.sipfoundry.sipxconfig.common.ReplicableProvider;

public interface E911Manager extends ReplicableProvider {

    List<E911Location> findLocations();
    
    E911Location findLocationByElin(String elin);
    
    E911Location findLocationById(Integer id);

    void saveLocation(E911Location location);

    void deleteLocations(Collection<Integer> ids);
}
