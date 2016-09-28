package org.sipfoundry.sipxconfig.bridge.config;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.sipfoundry.sipxconfig.bridge.BridgeSbc;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.commserver.LocationsManager;

public class ModuleAclConfiguration extends AbstractModuleConfiguration {
	private static final String DEFAULT_ACL_NAME = "sipxecs-trunk-acl";
	private static final String DEFAULT_ACL_ACTION = "deny";
	
	private LocationsManager m_locationManager;
	private String m_aclName;
	
	@Override
	public void write(Writer writer, Location location, BridgeSbc bridgeSbc) throws IOException {		
		AclContextGenerator generator = new AclContextGenerator();
		generator.setAclId(getAclName())
			  .setAclDefaultAction(getAclAllowByDefault(bridgeSbc))
			  .setAclAllow(getAllowAddresses(bridgeSbc))
			  .setAclDeny(getDenyAddresses(bridgeSbc));
		write(writer, generator.getContext());
	}
	
	@Override
    public String getConfigName() {
        return "acl";
    }
	
	protected Set<String> getAllowAddresses(BridgeSbc settings) {
		Set<String> addresses = new HashSet<String>();
		
		//Add always the node as allowed addresses
		List<Location> locations = m_locationManager.getLocationsList();
		for(Location location : locations) {
			addresses.add(location.getAddress() + "/32");
		}
		
		//Add user provided allowed addresses (Must be in CIDR format)
		Set<String> acl = settings.getAclAllowedAddresses();
		if(settings != null && !CollectionUtils.isEmpty(acl)) {
			for(String location : settings.getAclAllowedAddresses()) {
				addresses.add(location);
			}
		}
		
		return addresses;
	}
	
	protected Set<String> getDenyAddresses(BridgeSbc settings) {
		Set<String> addresses = new HashSet<String>();
		
		//Add user provided deny addresses (Must be in CIDR format)
		if(settings != null && !CollectionUtils.isEmpty(settings.getAclDenyAddresses())) {
			for(String location : settings.getAclDenyAddresses()) {
				addresses.add(location);
			}
		}
		
		return addresses; 
	}
	
	protected String getAclAllowByDefault(BridgeSbc settings) {
		if(settings != null && !StringUtils.isBlank(settings.getAclDefaultAction())) {
			return settings.getAclDefaultAction();
		}
		
		return DEFAULT_ACL_ACTION;
	}
	
	public LocationsManager getLocationManager() {
		return m_locationManager;
	}

	public void setLocationManager(LocationsManager locationManager) {
		this.m_locationManager = locationManager;
	}
	
	public String getAclName() {
		return StringUtils.isNotBlank(m_aclName) 
				? m_aclName : DEFAULT_ACL_NAME;
	}

	public void setAclName(String aclName) {
		this.m_aclName = aclName;
	}
	
	/**
	 * class AclContextGenerator
	 */
    static class AclContextGenerator extends BasicContextGenerator {
    	
    	public AclContextGenerator() {
    		super();
    	}
    	
    	public AclContextGenerator setAclId(String id) {
    		put("acl_id", id);
    		return this;
    	}
    	
    	public AclContextGenerator setAclDefaultAction(String action) {
    		put("acl_default_action", action);
    		return this;
    	}
    	
    	public AclContextGenerator setAclAllow(Set<String> allow) {
    		put("acl_allow", allow);
    		return this;
    	}
    	
    	public AclContextGenerator setAclDeny(Set<String> deny) {
    		put("acl_deny", deny);
    		return this;
    	}
    }

}
