package org.sipfoundry.sipxconfig.callerid;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.sipfoundry.sipxconfig.common.BeanWithId;
import org.sipfoundry.sipxconfig.common.Replicable;
import org.sipfoundry.sipxconfig.commserver.imdb.AliasMapping;
import org.sipfoundry.sipxconfig.commserver.imdb.DataSet;
import org.sipfoundry.sipxconfig.systemaudit.SystemAuditable;

public class MaskCallerIdBean extends BeanWithId implements Replicable, SystemAuditable {
    public static final String MCID_FROM = "mcid_from";
    public static final String MCID_EXT = "mcid_ext";
    public static final String MCID_NAME = "mcid_name";
    
    private String m_from;
    private String m_maskExtension;
    private String m_maskName;

	@Override
	public String getName() {
		return m_from;
	}

	@Override
	public void setName(String name) {
		// Do nothing
	}

	@Override
	public String getEntityIdentifier() {
		return m_from;
	}

	@Override
	public String getConfigChangeType() {
		return getClass().getSimpleName();
	}

	@Override
	public Map<String, Object> getMongoProperties(String domain) {
		Map<String, Object> props = new HashMap<String, Object>();
        props.put(MCID_FROM, getFrom());
        props.put(MCID_EXT, getMaskExtension());
        props.put(MCID_NAME, getMaskName());
        return props;
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
	public Set<DataSet> getDataSets() {
		return Collections.emptySet(); // Default. Do Nothing
	}

	@Override
	public String getIdentity(String domainName) {
		return null; // Default. Do Nothing
	}

	@Override
	public Collection<AliasMapping> getAliasMappings(String domainName) {
		return Collections.emptyList(); // Default. Do Nothing
	}

	@Override
	public boolean isValidUser() {
		return false; // Default. Do Nothing
	}
	
	public String getFrom() {
		return m_from;
	}
	
	public void setFrom(String from) {
		this.m_from = from;
	}
	
	public String getMaskExtension() {
		return m_maskExtension;
	}
	
	public void setMaskExtension(String maskExtension) {
		this.m_maskExtension = maskExtension;
	}
	
	public String getMaskName() {
		return m_maskName;
	}
	
	public void setMaskName(String maskName) {
		this.m_maskName = maskName;
	}
}
