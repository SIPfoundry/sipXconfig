/*
 * Copyright (C) 2011 eZuce Inc., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the AGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.dns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sipfoundry.sipxconfig.address.Address;
import org.sipfoundry.sipxconfig.address.AddressManager;
import org.sipfoundry.sipxconfig.address.AddressProvider;
import org.sipfoundry.sipxconfig.address.AddressType;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.feature.Bundle;
import org.sipfoundry.sipxconfig.feature.FeatureProvider;
import org.sipfoundry.sipxconfig.feature.GlobalFeature;
import org.sipfoundry.sipxconfig.feature.LocationFeature;
import org.sipfoundry.sipxconfig.setting.BeanWithSettingsDao;
import org.sipfoundry.sipxconfig.snmp.ProcessDefinition;
import org.sipfoundry.sipxconfig.snmp.ProcessProvider;
import org.sipfoundry.sipxconfig.snmp.SnmpManager;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;

public class DnsManagerImpl implements DnsManager, AddressProvider, FeatureProvider, BeanFactoryAware, ProcessProvider {
    private BeanWithSettingsDao<DnsSettings> m_settingsDao;
    private List<DnsProvider> m_providers;
    private ListableBeanFactory m_beanFactory;
    private AddressManager m_addressManager;

    @Override
    public Address getSingleAddress(AddressType t, Collection<Address> addresses, Location whoIsAsking) {
        if (addresses == null || addresses.size() == 0) {
            return null;
        }

        Iterator<Address> i = addresses.iterator();
        Address first = i.next();
        if (addresses.size() == 1 || whoIsAsking == null) {
            return first;
        }

        for (DnsProvider p : getProviders()) {
            Address rewrite = p.getAddress(this, t, addresses, whoIsAsking);
            if (rewrite != null) {
                return rewrite;
            }
        }

        // return the address local to who is asking if available
        Address a = first;
        while (a != null) {
            if (a.getAddress().equals(whoIsAsking.getAddress())) {
                return a;
            }
            a = (i.hasNext() ? i.next() : null);
        }

        // first is as good as any other
        return first;
    }

    @Override
    public List<ResourceRecords> getResourceRecords(Location whoIsAsking) {
        List<ResourceRecords> rrs = new ArrayList<ResourceRecords>();
        for (DnsProvider p : getProviders()) {
            List<ResourceRecords> rRecords = p.getResourceRecords(this, whoIsAsking);
            if (rRecords != null) {
                rrs.addAll(rRecords);
            }
        }
        return rrs;
    }

    @Override
    public Collection<AddressType> getSupportedAddressTypes(AddressManager manager) {
        return Collections.singleton(DNS_ADDRESS);
    }

    @Override
    public Collection<Address> getAvailableAddresses(AddressManager manager, AddressType type, Object requester) {
        if (!type.equals(DNS_ADDRESS)) {
            return null;
        }
        List<Location> locations = manager.getFeatureManager().getLocationsForEnabledFeature(FEATURE);
        return Location.toAddresses(DNS_ADDRESS, locations);
    }

    @Override
    public Collection<GlobalFeature> getAvailableGlobalFeatures() {
        return null;
    }

    @Override
    public Collection<LocationFeature> getAvailableLocationFeatures(Location l) {
        return Collections.singleton(FEATURE);
    }

    @Override
    public DnsSettings getSettings() {
        return m_settingsDao.findOrCreateOne();
    }

    @Override
    public void saveSettings(DnsSettings settings) {
        m_settingsDao.upsert(settings);
    }

    public void setSettingsDao(BeanWithSettingsDao<DnsSettings> settingsDao) {
        m_settingsDao = settingsDao;
    }

    List<DnsProvider> getProviders() {
        if (m_providers == null) {
            Map<String, DnsProvider> beanMap = m_beanFactory.getBeansOfType(DnsProvider.class, false, false);
            m_providers = new ArrayList<DnsProvider>(beanMap.values());
        }
        return m_providers;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        m_beanFactory = (ListableBeanFactory) beanFactory;
    }

    public AddressManager getAddressManager() {
        return m_addressManager;
    }

    public void setAddressManager(AddressManager addressManager) {
        m_addressManager = addressManager;
    }

    void setProviders(List<DnsProvider> providers) {
        m_providers = providers;
    }

    @Override
    public Collection<ProcessDefinition> getProcessDefinitions(SnmpManager manager, Location location) {
        boolean enabled = manager.getFeatureManager().isFeatureEnabled(FEATURE, location);
        return (enabled ? Collections.singleton(new ProcessDefinition("named")) : null);
    }

    @Override
    public void getBundleFeatures(Bundle b) {
        if (b.isRouter()) {
            b.addFeature(FEATURE);
        }
    }
}
