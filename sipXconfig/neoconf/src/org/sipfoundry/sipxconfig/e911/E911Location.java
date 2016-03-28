package org.sipfoundry.sipxconfig.e911;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.sipfoundry.commons.mongo.MongoConstants;
import org.sipfoundry.sipxconfig.common.BeanWithId;
import org.sipfoundry.sipxconfig.common.Replicable;
import org.sipfoundry.sipxconfig.commserver.imdb.AliasMapping;
import org.sipfoundry.sipxconfig.commserver.imdb.DataSet;
import org.sipfoundry.sipxconfig.systemaudit.SystemAuditable;

public class E911Location extends BeanWithId implements SystemAuditable, Replicable {

    private String m_elin;
    private String m_location;
    private String m_addressInfo;
    private String m_description;

    @Override
    public String getName() {
        return m_elin;
    }

	@Override
    public void setName(String name) {
		// Nothing to do
    }

    @Override
    public Set<DataSet> getDataSets() {
        return Collections.emptySet();
    }

    @Override
    public String getIdentity(String domainName) {
        return null;
    }

    @Override
    public Collection<AliasMapping> getAliasMappings(String domainName) {
        return Collections.emptyList();
    }

    @Override
    public boolean isValidUser() {
        return false;
    }

    @Override
    public Map<String, Object> getMongoProperties(String domain) {
        Map<String, Object> property = new HashMap<String, Object>();
        property.put(MongoConstants.ELIN, getElin());
        property.put(MongoConstants.LOCATION, getLocation());
        property.put(MongoConstants.ADDRESS_INFO, getAddressInfo());
        return property;
    }

    @Override
    public String getEntityName() {
        return getClass().getSimpleName();
    }

    @Override
    public boolean isReplicationEnabled() {
        return true;
    }

    @Override
    public String getEntityIdentifier() {
        return getLocation();
    }

    @Override
    public String getConfigChangeType() {
        return getClass().getSimpleName();
    }
    
	public String getElin() {
		return m_elin;
	}

	public void setElin(String elin) {
		this.m_elin = elin;
	}    
    
    public String getLocation() {
		return m_location;
	}

	public void setLocation(String location) {
		this.m_location = location;
	}

	public String getAddressInfo() {
		return m_addressInfo;
	}

	public void setAddressInfo(String addressInfo) {
		this.m_addressInfo = addressInfo;
	}

	public String getDescription() {
		return m_description;
	}

	public void setDescription(String description) {
		this.m_description = description;
	}
}
