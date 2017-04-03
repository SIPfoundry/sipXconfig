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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sipfoundry.sipxconfig.address.AddressManager;
import org.sipfoundry.sipxconfig.commserver.ServiceStatus;
import org.sipfoundry.sipxconfig.freeswitch.api.FreeswitchApi;
import org.sipfoundry.sipxconfig.freeswitch.api.FreeswitchApiResultParser;
import org.sipfoundry.sipxconfig.freeswitch.api.FreeswitchApiResultParserImpl;
import org.sipfoundry.sipxconfig.freeswitch.api.FreeswitchSofiaStatus;
import org.sipfoundry.sipxconfig.freeswitch.api.FreeswitchSofiaStatus.Type;
import org.sipfoundry.sipxconfig.snmp.SnmpManager;
import org.sipfoundry.sipxconfig.xmlrpc.ApiProvider;
import org.sipfoundry.sipxconfig.xmlrpc.XmlRpcRemoteException;
import org.springframework.beans.factory.annotation.Required;

public class BridgeSbcStatistics {
	private static final Log LOG = LogFactory.getLog(BridgeSbcStatistics.class);
	
	private final FreeswitchApiResultParser m_freeswitchApiParser = new FreeswitchApiResultParserImpl();
	private final String SHOW_ACTIVE_CALL_PARAM = "calls count";
	private final String SHOW_SOFIA_PROFILE_STATUS_PARAM = "xmlstatus";
	
	private ApiProvider<FreeswitchApi> m_freeswitchApiProvider;
	private AddressManager m_addressManager;
    private SnmpManager m_snmpManager;

    /**
     * Get a count of the number of ongoing calls.
     *
     * @return the number of ongoing calls.
     * @throws Exception
     */
    public int getCallCount(BridgeSbc bridgeSbc) throws Exception {
        FreeswitchApi api = api(bridgeSbc);
        if(api == null) {
        	return 0;
        }
        
        String result = "";
        try {
			result = api.show(SHOW_ACTIVE_CALL_PARAM);
        } catch (XmlRpcRemoteException xrre) {
            LOG.warn("Unable to retrieve active calls from sipxbridge", xrre);
        }
        
        int count = m_freeswitchApiParser.getCallCount(result);
        return count;
    }

    boolean isOk(BridgeSbc bridgeSbc) {
        // Not sure what to check? process configed? running? ---Douglas
        List<ServiceStatus> stats = m_snmpManager.getServicesStatuses(bridgeSbc.getLocation());
        for (ServiceStatus status : stats) {
            if (status.getServiceBeanId().equals("sipxbridge")
                    && status.getStatus().equals(ServiceStatus.Status.Running)) {
                return true;
            }
        }
        return false;
    }

    private FreeswitchApi api(BridgeSbc bridgeSbc) {
        String url = m_addressManager.getSingleAddress(BridgeSbcContext.XMLRPC_ADDRESS, bridgeSbc.getLocation())
                .toString();
        return m_freeswitchApiProvider.getApi(url);
    }

    /**
     * Gets an array of Registration records - one record for each account that requires
     * registration.
     *
     * @return an array of registration records.
     * @throws Exception
     */
    public BridgeSbcRegistrationRecord[] getRegistrationRecords(BridgeSbc bridgeSbc) throws Exception {
    	FreeswitchApi api = api(bridgeSbc);
        if (api == null) {
            return null;
        }

        String result = api.sofia(SHOW_SOFIA_PROFILE_STATUS_PARAM);
        if (StringUtils.isBlank(result)) {
            return null;
        }
        
        List<FreeswitchSofiaStatus> sofiaStatuses = m_freeswitchApiParser.getSofiaStatuses(result);
        if(sofiaStatuses.isEmpty()) {
        	return null;
        }
        
        List<BridgeSbcRegistrationRecord> registrationRecords = new ArrayList<>(); 
        for(FreeswitchSofiaStatus sofiaStatus : sofiaStatuses) {
        	if(sofiaStatus.getType() == Type.SOFIA_GATEWAY) {
        		registrationRecords.add(new BridgeSbcRegistrationRecord(sofiaStatus.getData()
        				, sofiaStatus.getStatus().getMessage()));
        	}
        }
        
        return !registrationRecords.isEmpty() ?
        		registrationRecords.toArray(new BridgeSbcRegistrationRecord[registrationRecords.size()])
        		: null;
    }

    @Required
    public void setFreeswitchApiProvider(ApiProvider<FreeswitchApi> freeswitchApiProvider) {
        m_freeswitchApiProvider = freeswitchApiProvider;
    }
    
    @Required
    public void setAddressManager(AddressManager addressManager) {
    	m_addressManager = addressManager;
    }

    @Required
    public void setSnmpManager(SnmpManager snmpManager) {
        m_snmpManager = snmpManager;
    }

}
