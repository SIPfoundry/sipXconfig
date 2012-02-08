/*
 *
 *
 * Copyright (C) 2008 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */
package org.sipfoundry.sipxconfig.bridge;

import java.util.Map;
import java.util.Set;

import org.sipfoundry.sipxconfig.address.Address;
import org.sipfoundry.sipxconfig.address.AddressManager;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigManager;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigManager.ConfigStatus;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.xmlrpc.ApiProvider;
import org.springframework.beans.factory.annotation.Required;

public class BridgeSbcStatistics {
    private ApiProvider<BridgeSbcXmlRpcApi> m_bridgeSbcApiProvider;
    private ConfigManager m_configManager;
    private AddressManager m_addressManager;

    /**
     * Get a count of the number of ongoing calls.
     *
     * @return the number of ongoing calls.
     * @throws Exception
     */
    public int getCallCount(BridgeSbc bridgeSbc) throws Exception {
        BridgeSbcXmlRpcApi api = getApi(bridgeSbc);
        return (api == null ? 0 : api.getCallCount());
    }

    boolean isOk(BridgeSbc bridgeSbc) {
        Location location = bridgeSbc.getLocation();
        ConfigStatus status = m_configManager.getStatus(location, BridgeSbcContext.FEATURE.toString());
        return status == ConfigStatus.OK;
    }

    BridgeSbcXmlRpcApi getApi(BridgeSbc bridgeSbc) {
        if (!isOk(bridgeSbc)) {
            return null;
        }
        Address address = m_addressManager.getSingleAddress(BridgeSbcContext.XMLRPC_ADDRESS);
        BridgeSbcXmlRpcApi api = m_bridgeSbcApiProvider.getApi(address.toString());
        return api;
    }

    /**
     * Gets an array of Registration records - one record for each account that requires
     * registration.
     *
     * @return an array of registration records.
     * @throws Exception
     */
    public BridgeSbcRegistrationRecord[] getRegistrationRecords(BridgeSbc bridgeSbc) throws Exception {
        BridgeSbcXmlRpcApi api = getApi(bridgeSbc);
        if (api == null) {
            return null;
        }

        Map<String, String> registrationRecordMap = null;
        registrationRecordMap = api.getRegistrationStatus();
        if (registrationRecordMap == null) {
            return null;
        }

        BridgeSbcRegistrationRecord[] registrationRecords = new BridgeSbcRegistrationRecord[registrationRecordMap
                .size()];
        int i = 0;
        Set<String> keys = registrationRecordMap.keySet();
        for (String key : keys) {
            registrationRecords[i++] = new BridgeSbcRegistrationRecord(key, registrationRecordMap.get(key));
        }

        return registrationRecords;
    }

    @Required
    public void setConfigManager(ConfigManager configManager) {
        m_configManager = configManager;
    }

    @Required
    public void setBridgeSbcApiProvider(ApiProvider bridgeSbcApiProvider) {
        m_bridgeSbcApiProvider = bridgeSbcApiProvider;
    }

    public void setAddressManager(AddressManager addressManager) {
        m_addressManager = addressManager;
    }

}
