package org.sipfoundry.sipxconfig.oss;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.sipfoundry.sipxconfig.address.Address;
import org.sipfoundry.sipxconfig.address.AddressManager;
import org.sipfoundry.sipxconfig.address.AddressProvider;
import org.sipfoundry.sipxconfig.address.AddressType;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.feature.Bundle;
import org.sipfoundry.sipxconfig.feature.FeatureChangeRequest;
import org.sipfoundry.sipxconfig.feature.FeatureChangeValidator;
import org.sipfoundry.sipxconfig.feature.FeatureManager;
import org.sipfoundry.sipxconfig.feature.FeatureProvider;
import org.sipfoundry.sipxconfig.feature.GlobalFeature;
import org.sipfoundry.sipxconfig.feature.LocationFeature;
import org.sipfoundry.sipxconfig.firewall.DefaultFirewallRule;
import org.sipfoundry.sipxconfig.firewall.FirewallManager;
import org.sipfoundry.sipxconfig.firewall.FirewallProvider;
import org.sipfoundry.sipxconfig.firewall.FirewallRule;
import org.sipfoundry.sipxconfig.kamailio.KamailioManager;
import org.sipfoundry.sipxconfig.mysql.MySql;
import org.sipfoundry.sipxconfig.setting.BeanWithSettingsDao;
import org.sipfoundry.sipxconfig.snmp.ProcessDefinition;
import org.sipfoundry.sipxconfig.snmp.ProcessProvider;
import org.sipfoundry.sipxconfig.snmp.SnmpManager;

public class OSSCoreManagerImpl implements OSSCoreManager, FeatureProvider, AddressProvider
		, ProcessProvider, FirewallProvider {
	
	public static final Collection<AddressType> PUBLIC_ADDRESS_TYPES = Arrays.asList(
			PUBLIC_TCP_ADDRESS, PUBLIC_UDP_ADDRESS, PUBLIC_TLS_ADDRESS
		);
	
	public static final Collection<AddressType> INTERNAL_ADDRESS_TYPES = Arrays.asList(
			INTERNAL_TCP_ADDRESS, INTERNAL_UDP_ADDRESS
		);
	
	private FeatureManager m_featureManager;
	private BeanWithSettingsDao<OSSCoreSettings> m_settingsDao;
	
	@Override
	public void featureChangePrecommit(FeatureManager manager, FeatureChangeValidator validator) {
		validator.requiredOnSameHost(FEATURE, MySql.FEATURE);

		// Do not auto resolve to avoid circular dependency
		validator.requiredOnSameHost(FEATURE, KamailioManager.FEATURE_PROXY, false);
	}

	@Override
	public void featureChangePostcommit(FeatureManager manager, FeatureChangeRequest request) {
		//Do Nothing
	}

	@Override
	public Collection<DefaultFirewallRule> getFirewallRules(FirewallManager manager) {
		return DefaultFirewallRule.rules(PUBLIC_ADDRESS_TYPES, FirewallRule.SystemId.PUBLIC);
	}

	@Override
	public Collection<ProcessDefinition> getProcessDefinitions(SnmpManager manager, Location location) {
		if (!m_featureManager.isFeatureEnabled(FEATURE, location)) {
            return null;
        }
        ProcessDefinition def = ProcessDefinition.sysv("oss_core", true);
        return Collections.singleton(def);
	}

	@Override
	public Collection<Address> getAvailableAddresses(AddressManager manager, AddressType type, Location requester) {
		if (!PUBLIC_ADDRESS_TYPES.contains(type) && !INTERNAL_ADDRESS_TYPES.contains(type)) {
            return null;
        }
		
        Collection<Address> addresses = null;
        Collection<Location> locations = m_featureManager.getLocationsForEnabledFeature(FEATURE);
        addresses = new ArrayList<Address>(locations.size());
        for (Location location : locations) {
            Address address = null;
            if (type.equals(PUBLIC_TCP_ADDRESS)) {
                address = new Address(PUBLIC_TCP_ADDRESS, location.getAddress(), 5062);
            } else if (type.equals(PUBLIC_UDP_ADDRESS)) {
                address = new Address(PUBLIC_UDP_ADDRESS, location.getAddress(), 5062);
            } else if (type.equals(PUBLIC_TLS_ADDRESS)) {
                address = new Address(PUBLIC_TCP_ADDRESS, location.getAddress(), 5063);
            } else if (type.equals(INTERNAL_TCP_ADDRESS)) {
                address = new Address(INTERNAL_TCP_ADDRESS, location.getAddress(), 5050);
            } else if (type.equals(INTERNAL_UDP_ADDRESS)) {
                address = new Address(INTERNAL_UDP_ADDRESS, location.getAddress(), 5050);
            }
            addresses.add(address);
        }

        return addresses;
	}

	@Override
	public Collection<GlobalFeature> getAvailableGlobalFeatures(FeatureManager featureManager) {
		return null;
	}

	@Override
	public Collection<LocationFeature> getAvailableLocationFeatures(FeatureManager featureManager, Location l) {
		return Collections.singleton(FEATURE);
	}

	@Override
	public void getBundleFeatures(FeatureManager featureManager, Bundle b) {
		if (b == Bundle.CORE_TELEPHONY) {
            b.addFeature(FEATURE);
        }
	}

	@Override
	public OSSCoreSettings getSettings() {
		return m_settingsDao.findOrCreateOne();
	}

	@Override
	public void saveSettings(OSSCoreSettings settings) {
		m_settingsDao.upsert(settings);
	}

	public FeatureManager getFeatureManager() {
		return m_featureManager;
	}

	public void setFeatureManager(FeatureManager featureManager) {
		this.m_featureManager = featureManager;
	}

	public BeanWithSettingsDao<OSSCoreSettings> getSettingsDao() {
		return m_settingsDao;
	}

	public void setSettingsDao(BeanWithSettingsDao<OSSCoreSettings> settingsDao) {
		this.m_settingsDao = settingsDao;
	}

}
