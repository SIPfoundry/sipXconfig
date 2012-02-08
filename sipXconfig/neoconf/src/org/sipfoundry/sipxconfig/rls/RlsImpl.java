/*
 * Copyright (C) 2011 eZuce Inc., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the AGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.rls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.sipfoundry.sipxconfig.address.Address;
import org.sipfoundry.sipxconfig.address.AddressManager;
import org.sipfoundry.sipxconfig.address.AddressProvider;
import org.sipfoundry.sipxconfig.address.AddressType;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.feature.FeatureProvider;
import org.sipfoundry.sipxconfig.feature.GlobalFeature;
import org.sipfoundry.sipxconfig.feature.LocationFeature;
import org.sipfoundry.sipxconfig.setting.BeanWithSettingsDao;

public class RlsImpl implements AddressProvider, FeatureProvider, Rls {
    private static final Collection<AddressType> ADDRESSES = Arrays.asList(UDP_SIP, TCP_SIP);
    private BeanWithSettingsDao<RlsSettings> m_settingsDao;

    @Override
    public RlsSettings getSettings() {
        return m_settingsDao.findOrCreateOne();
    }

    @Override
    public void saveSettings(RlsSettings settings) {
        m_settingsDao.upsert(settings);
    }

    @Override
    public Collection<GlobalFeature> getAvailableGlobalFeatures() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<LocationFeature> getAvailableLocationFeatures(Location l) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<AddressType> getSupportedAddressTypes(AddressManager manager) {
        return ADDRESSES;
    }

    @Override
    public Collection<Address> getAvailableAddresses(AddressManager manager, AddressType type,
            Object requester) {
        if (!ADDRESSES.contains(type)) {
            return null;
        }

        List<Location> locations = manager.getFeatureManager().getLocationsForEnabledFeature(FEATURE);
        if (locations.isEmpty()) {
            return null;
        }

        RlsSettings settings = getSettings();
        List<Address> addresses = new ArrayList<Address>(locations.size());
        for (Location location : locations) {
            Address address;
            if (type.equals(UDP_SIP)) {
                address = new Address(UDP_SIP, location.getAddress(), settings.getUdpPort());
            } else {
                address = new Address(TCP_SIP, location.getAddress(), settings.getTcpPort());
            }
            addresses.add(address);
        }
        return addresses;
    }

    public void setSettingsDao(BeanWithSettingsDao<RlsSettings> settingsDao) {
        m_settingsDao = settingsDao;
    }
}
