package org.sipfoundry.sipxconfig.kamailio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.sipfoundry.sipxconfig.address.Address;
import org.sipfoundry.sipxconfig.address.AddressManager;
import org.sipfoundry.sipxconfig.address.AddressProvider;
import org.sipfoundry.sipxconfig.address.AddressType;
import org.sipfoundry.sipxconfig.alarm.AlarmDefinition;
import org.sipfoundry.sipxconfig.alarm.AlarmProvider;
import org.sipfoundry.sipxconfig.alarm.AlarmServerManager;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigManager;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.dialplan.DialPlanContext;
import org.sipfoundry.sipxconfig.dns.DnsManager;
import org.sipfoundry.sipxconfig.dns.DnsProvider;
import org.sipfoundry.sipxconfig.dns.ResourceRecord;
import org.sipfoundry.sipxconfig.dns.ResourceRecords;
import org.sipfoundry.sipxconfig.feature.Bundle;
import org.sipfoundry.sipxconfig.feature.FeatureChangeRequest;
import org.sipfoundry.sipxconfig.feature.FeatureChangeValidator;
import org.sipfoundry.sipxconfig.feature.FeatureManager;
import org.sipfoundry.sipxconfig.feature.FeatureProvider;
import org.sipfoundry.sipxconfig.feature.GlobalFeature;
import org.sipfoundry.sipxconfig.feature.InvalidChange;
import org.sipfoundry.sipxconfig.feature.LocationFeature;
import org.sipfoundry.sipxconfig.firewall.DefaultFirewallRule;
import org.sipfoundry.sipxconfig.firewall.FirewallManager;
import org.sipfoundry.sipxconfig.firewall.FirewallProvider;
import org.sipfoundry.sipxconfig.firewall.FirewallRule.SystemId;
import org.sipfoundry.sipxconfig.mysql.MySql;
import org.sipfoundry.sipxconfig.oss.OSSCoreManager;
import org.sipfoundry.sipxconfig.proxy.ProxyManager;
import org.sipfoundry.sipxconfig.redis.Redis;
import org.sipfoundry.sipxconfig.setting.BeanWithSettingsDao;
import org.sipfoundry.sipxconfig.snmp.ProcessDefinition;
import org.sipfoundry.sipxconfig.snmp.ProcessProvider;
import org.sipfoundry.sipxconfig.snmp.SnmpManager;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class KamailioManagerImpl implements KamailioManager, FeatureProvider, AddressProvider
    , ProcessProvider, AlarmProvider, FirewallProvider, DnsProvider {
    
    public static final Collection<AddressType> PROXY_ADDRESS_TYPES = Arrays.asList(
            TCP_PROXY_ADDRESS, UDP_PROXY_ADDRESS, TLS_PROXY_ADDRESS
        );
    
    public static final Collection<AddressType> PRESENCE_ADDRESS_TYPES = Arrays.asList(
            TCP_PRESENCE_ADDRESS, UDP_PRESENCE_ADDRESS, TLS_PRESENCE_ADDRESS
        );
    
    private FeatureManager m_featureManager;
    private BeanWithSettingsDao<KamailioSettings> m_settingsDao;
    private ConfigManager m_configManager;

    @Override
    public void featureChangePrecommit(FeatureManager manager, FeatureChangeValidator validator) {
        boolean ingressOn = validator.isEnabledSomewhere(FEATURE_PROXY);
        boolean presenceOn = validator.isEnabledSomewhere(FEATURE_PRESENCE);
        
        /* Proxy Feature Precommit */
        if (ingressOn && !validator.isEnabledSomewhere(ProxyManager.FEATURE)) {
            validator.requiresAtLeastOne(FEATURE_PROXY, ProxyManager.FEATURE);
        }
        
        if (ingressOn && !presenceOn) {
            InvalidChange requires = InvalidChange.requires(FEATURE_PROXY, FEATURE_PRESENCE);
            requires.setAllowAutoResolve(false);
            validator.getInvalidChanges().add(requires);
        }
        
        validator.requiredOnSameHost(FEATURE_PROXY, MySql.FEATURE);
        validator.requiredOnSameHost(FEATURE_PROXY, OSSCoreManager.FEATURE);
                
        /* Presence Feature Precommit */
        validator.singleLocationOnly(FEATURE_PRESENCE); // Only one presence at a time
        if (presenceOn && !ingressOn) {
            validator.requiresAtLeastOne(FEATURE_PRESENCE, FEATURE_PROXY);
        }
        
        validator.singleLocationOnly(FEATURE_PRESENCE);
        validator.requiredOnSameHost(FEATURE_PRESENCE, Redis.FEATURE);
        validator.requiredOnSameHost(FEATURE_PRESENCE, MySql.FEATURE);
    }

    @Override
    public void featureChangePostcommit(FeatureManager manager, FeatureChangeRequest request) {
        if (request.hasChanged(FEATURE_PROXY)) {
            m_configManager.configureEverywhere(DnsManager.FEATURE, DialPlanContext.FEATURE);
        }
        
        if (request.hasChanged(FEATURE_PRESENCE)) {
            m_configManager.configureEverywhere(DnsManager.FEATURE, DialPlanContext.FEATURE);
        }
    }

    @Override
    public Address getAddress(DnsManager manager, AddressType t, Collection<Address> addresses, Location whoIsAsking) {
        return null;
    }

    @Override
    public Collection<ResourceRecords> getResourceRecords(DnsManager manager) {
        Collection<ResourceRecords> proxyRecords = getProxyResourceRecords(manager);
        Collection<ResourceRecords> presenceRecords = getPresenceResourceRecords(manager);
        
        final Iterable<ResourceRecords> allRecords =
                Iterables.unmodifiableIterable(
                    Iterables.concat(proxyRecords, presenceRecords));
        
        return Lists.newArrayList(allRecords);
    }

    @Override
    public Collection<DefaultFirewallRule> getFirewallRules(FirewallManager manager) {
        final Iterable<AddressType> allAddresses =
                Iterables.unmodifiableIterable(
                    Iterables.concat(PROXY_ADDRESS_TYPES, PRESENCE_ADDRESS_TYPES));
        
        return DefaultFirewallRule.rules(Lists.newArrayList(allAddresses), SystemId.PUBLIC);
    }

    @Override
    public Collection<AlarmDefinition> getAvailableAlarms(AlarmServerManager manager) {
        return null;
    }

    @Override
    public Collection<ProcessDefinition> getProcessDefinitions(SnmpManager manager, Location location) {
        Collection<ProcessDefinition> procs = new ArrayList<ProcessDefinition>(2);
        FeatureManager featureManager = manager.getFeatureManager();
        
        if (featureManager.isFeatureEnabled(FEATURE_PROXY, location)) {
            ProcessDefinition def = ProcessDefinition.sysvByRegex("kamailio-proxy",
                    ".*\\s-f\\s.*kamailio-proxy\\.cfg\\s.*");
            def.setRestartClass("restart_kamailioproxy");
            procs.add(def);
        }
        
        if (featureManager.isFeatureEnabled(FEATURE_PRESENCE, location)) {
            ProcessDefinition def = ProcessDefinition.sysvByRegex("kamailio-presence",
                    ".*\\s-f\\s.*kamailio-presence\\.cfg\\s.*");
            def.setRestartClass("restart_kamailiopresence");
            procs.add(def);
        }

        return procs;
    }

    @Override
    public Collection<Address> getAvailableAddresses(AddressManager manager, AddressType type, Location requester) {
        if (!PROXY_ADDRESS_TYPES.contains(type) && !PRESENCE_ADDRESS_TYPES.contains(type)) {
            return null;
        }
        
        Collection<Address> addresses = new ArrayList<Address>();
        Collection<Location> locations = m_featureManager.getLocationsForEnabledFeature(FEATURE_PROXY);
        if(!locations.isEmpty()) {
            for (Location location : locations) {
                if (type.equals(TCP_PROXY_ADDRESS)) {
                    addresses.add(new Address(TCP_PROXY_ADDRESS, location.getAddress(), 5060));
                } else if (type.equals(UDP_PROXY_ADDRESS)) {
                    addresses.add(new Address(UDP_PROXY_ADDRESS, location.getAddress(), 5060));
                } else if (type.equals(TLS_PROXY_ADDRESS)) {
                    addresses.add(new Address(TCP_PROXY_ADDRESS, location.getAddress(), 5061));
                }
            }
        }
        
        locations = m_featureManager.getLocationsForEnabledFeature(FEATURE_PRESENCE);
        if(!locations.isEmpty()) {
            for (Location location : locations) {
                if (type.equals(TCP_PRESENCE_ADDRESS)) {
                    addresses.add(new Address(TCP_PRESENCE_ADDRESS, location.getAddress(), 5065));
                } else if (type.equals(UDP_PRESENCE_ADDRESS)) {
                    addresses.add(new Address(UDP_PRESENCE_ADDRESS, location.getAddress(), 5065));
                }
            }
        }
        
        return addresses;
    }

    @Override
    public Collection<GlobalFeature> getAvailableGlobalFeatures(FeatureManager featureManager) {
        return null;
    }

    @Override
    public Collection<LocationFeature> getAvailableLocationFeatures(FeatureManager featureManager, Location l) {
        return Arrays.asList(FEATURE_PROXY, FEATURE_PRESENCE);
    }

    @Override
    public void getBundleFeatures(FeatureManager featureManager, Bundle b) {
        if (b == Bundle.CORE_TELEPHONY) {
            b.addFeature(FEATURE_PROXY);
            b.addFeature(FEATURE_PRESENCE);
        }
    }

    @Override
    public KamailioSettings getSettings() {
        return m_settingsDao.findOrCreateOne();
    }

    @Override
    public void saveSettings(KamailioSettings settings) {
        m_settingsDao.upsert(settings);
    }
    
    private Collection<ResourceRecords> getProxyResourceRecords(DnsManager manager) {
        FeatureManager fm = manager.getAddressManager().getFeatureManager();
        List<Location> locations = fm.getLocationsForEnabledFeature(FEATURE_PROXY);
        if (locations == null || locations.isEmpty()) {
            return Collections.emptyList();
        }

        String root = ""; // THE sip root resource and no resource name
        ResourceRecords[] records = new ResourceRecords[] {
            new ResourceRecords("_sip._tcp", root, false),
            new ResourceRecords("_sip._udp", root, false),
            new ResourceRecords("_sips._tcp", root, false),
            new ResourceRecords("_sip._tls", root, false)
        };
        
        KamailioSettings settings = getSettings();
        int[] ports = new int[] {
                settings.getProxySipTcpPort(),
                settings.getProxySipUdpPort(),
                settings.getProxySipTlsPort(),
                settings.getProxySipTlsPort()
        };
        
        for (int i = 0; i < records.length; i++) {
            for (Location l : locations) {
                records[i].addRecord(new ResourceRecord(l.getHostname(), ports[i], l.getRegionId()));
            }
        }
        
        return Arrays.asList(records);
    }
    
    private Collection<ResourceRecords> getPresenceResourceRecords(DnsManager manager) {
        FeatureManager fm = manager.getAddressManager().getFeatureManager();
        List<Location> locations = fm.getLocationsForEnabledFeature(FEATURE_PRESENCE);
        if (locations == null || locations.isEmpty()) {
            return Collections.emptyList();
        }

        String root = "pm"; // THE sip root resource and no resource name
        ResourceRecords[] records = new ResourceRecords[] {
            new ResourceRecords("_sip._tcp", root, false),
            new ResourceRecords("_sip._udp", root, false),
            new ResourceRecords("_sips._tcp", root, false),
            new ResourceRecords("_sip._tls", root, false)
        };
        
        KamailioSettings settings = getSettings();
        int[] ports = new int[] {
                settings.getPresenceSipTcpPort(),
                settings.getPresenceSipUdpPort(),
                settings.getPresenceSipTlsPort(),
                settings.getPresenceSipTlsPort()
        };
        
        for (int i = 0; i < records.length; i++) {
            for (Location l : locations) {
                records[i].addRecord(new ResourceRecord(l.getHostname(), ports[i], l.getRegionId()));
            }
        }
        
        return Arrays.asList(records);
    }

    public FeatureManager getFeatureManager() {
        return m_featureManager;
    }

    public void setFeatureManager(FeatureManager featureManager) {
        this.m_featureManager = featureManager;
    }

    public ConfigManager getConfigManager() {
        return m_configManager;
    }

    public void setConfigManager(ConfigManager configManager) {
        this.m_configManager = configManager;
    }

    public BeanWithSettingsDao<KamailioSettings> getSettingsDao() {
        return m_settingsDao;
    }

    public void setSettingsDao(BeanWithSettingsDao<KamailioSettings> settingsDao) {
        this.m_settingsDao = settingsDao;
    }    
}
