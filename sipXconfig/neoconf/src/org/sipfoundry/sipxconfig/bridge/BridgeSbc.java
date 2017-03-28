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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.sipfoundry.sipxconfig.address.AddressManager;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigManager;
import org.sipfoundry.sipxconfig.cfgmgt.DeployConfigOnEdit;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.commserver.LocationsManager;
import org.sipfoundry.sipxconfig.device.DeviceDefaults;
import org.sipfoundry.sipxconfig.device.FileSystemProfileLocation;
import org.sipfoundry.sipxconfig.device.Profile;
import org.sipfoundry.sipxconfig.device.ProfileContext;
import org.sipfoundry.sipxconfig.device.ProfileLocation;
import org.sipfoundry.sipxconfig.dialplan.DialPlanContext;
import org.sipfoundry.sipxconfig.feature.Feature;
import org.sipfoundry.sipxconfig.freeswitch.api.FreeswitchApi;
import org.sipfoundry.sipxconfig.freeswitch.api.FreeswitchApiConnectException;
import org.sipfoundry.sipxconfig.gateway.GatewayContext;
import org.sipfoundry.sipxconfig.gateway.SipTrunk;
import org.sipfoundry.sipxconfig.proxy.ProxyManager;
import org.sipfoundry.sipxconfig.sbc.SbcDevice;
import org.sipfoundry.sipxconfig.setting.Setting;
import org.sipfoundry.sipxconfig.setting.SettingEntry;
import org.sipfoundry.sipxconfig.xmlrpc.ApiProvider;
import org.sipfoundry.sipxconfig.xmlrpc.XmlRpcRemoteException;
import org.springframework.beans.factory.annotation.Required;

public class BridgeSbc extends SbcDevice implements DeployConfigOnEdit {
    public static final String LOG_SETTING = "bridge-configuration/log-level";
    public static final String LOCATION_ID_SETTING = "bridge-configuration/location-id";
    public static final String ITSP_PROXY_DOMAIN_SETTING = "itsp-account/itsp-proxy-domain";
    public static final String USER_NAME_SETTING = "itsp-account/user-name";
    public static final String SIXECS_LINEIDS_SETTING = "itsp-account/sipxecs-lineids";
    public static final String SIXECS_LINEID_START = "<sipxecs-lineid>";
    public static final String SIXECS_LINEID_END = "</sipxecs-lineid>";
    public static final String CONFIG_FORMAT_PREFIX = "    ";
    public static final String NEW_LINE_FEED = "\n";
    public static final String FREESWITCH_RESTART_COMMAND = "shutdown restart";
    public static final String XML_RPC_EXCEPTION = "&xml.rpc.error.operation";

    private GatewayContext m_gatewayContext;
    private LocationsManager m_locationsManager;
    private Location m_location;
    private ConfigManager m_configManager;
    private AddressManager m_addressManager;
    private ApiProvider<FreeswitchApi> m_freeswitchApiProvider;

    @Required
    public void setGatewayContext(GatewayContext gatewayContext) {
        m_gatewayContext = gatewayContext;
    }

    @Required
    public void setLocationsManager(LocationsManager locationsManager) {
        m_locationsManager = locationsManager;
    }
    
    @Required
    public void setFreeswitchApiProvider(ApiProvider<FreeswitchApi> freeswitchApiProvider) {
        m_freeswitchApiProvider = freeswitchApiProvider;
    }
    
    @Required
    public void setAddressManager(AddressManager addressManager) {
        m_addressManager = addressManager;
    }

    public void setLocation(Location location) {
        m_location = location;
    }

    @Override
    protected Setting loadSettings() {
        return getModelFilesContext().loadModelFile("bridge-sbc.xml", "sipxbridge");
    }
    
    @Override
    public Profile[] getProfileTypes() {
        String profileFilename = getProfileFilename();
        if (profileFilename == null) {
            return null;
        }
        return new Profile[] {
            new Profile(profileFilename)
        };
    }

    @Override
    protected ProfileContext createContext() {
        return new Context(this, "sipxbridge/sofia_external.xml.vm");
    }

    @Override
    public String getProfileFilename() {
        return "external.xml";
    }

    @Override
    public void initialize() {
        addDefaultBeanSettingHandler(new Defaults(getDefaults(), getLocation()));
    }

    @Override
    public ProfileLocation getProfileLocation() {
        String path = m_configManager.getLocationDataDirectory(getLocation()).getAbsolutePath();
        FileSystemProfileLocation profileLocation = new FileSystemProfileLocation();
        profileLocation.setParentDir(path + "/sipxbridge/sip_profiles");
        return profileLocation;
    }
    
    public void restartFreeswitch() {
    	if(isUseFreeswitch()) {
    		try {
    			api().fsctl(FREESWITCH_RESTART_COMMAND);
            } catch (XmlRpcRemoteException xrre) {
                throw new FreeswitchApiConnectException(getLocation(), xrre);
            }
    	}
    }

    boolean isRedundant(SipTrunk sipTrunk, List<SipTrunk> list) {
        for (SipTrunk t : list) {
            String domain = t.getAddress();
            String username = t.getSettingValue(USER_NAME_SETTING);
            if (domain == null) {
                continue;
            }

            if (sipTrunk.getAddress() != null && domain.compareToIgnoreCase(sipTrunk.getAddress()) != 0) {
                continue;
            }

            if (username == null && sipTrunk.getSettingValue(USER_NAME_SETTING) == null) {
                return true;
            }

            if (username == null && sipTrunk.getSettingValue(USER_NAME_SETTING) != null) {
                continue;
            }

            if (username.equals(sipTrunk.getSettingValue(USER_NAME_SETTING))) {
                return true;
            }
        }
        return false;
    }
    
    

    void addGWReference(SipTrunk sipTrunk, List< ? extends SipTrunk> list) {
        String lineID;
        String lineIDs = "";
        for (Object o : list) {
            if (o instanceof SipTrunk) {
                SipTrunk t = (SipTrunk) o;

                String domain = t.getSettingValue(ITSP_PROXY_DOMAIN_SETTING);
                String username = t.getSettingValue(USER_NAME_SETTING);
                if ((domain != null && domain.equals(sipTrunk.getSettingValue(ITSP_PROXY_DOMAIN_SETTING)))) {
                    if ((username != null && username.equals(sipTrunk.getSettingValue(USER_NAME_SETTING)))
                            || (username == null && sipTrunk.getSettingValue(USER_NAME_SETTING) == null)) {

                        lineID = Integer.toString(t.getId());
                        lineIDs = lineIDs.concat(NEW_LINE_FEED + CONFIG_FORMAT_PREFIX + SIXECS_LINEID_START + lineID
                                + SIXECS_LINEID_END);
                    }
                }
            }
        }
        lineIDs = lineIDs.concat(NEW_LINE_FEED + CONFIG_FORMAT_PREFIX);
        sipTrunk.setSettingValue(SIXECS_LINEIDS_SETTING, lineIDs);
    }

    public List<SipTrunk> getMySipItsps() {
        List<SipTrunk> itsps = new ArrayList<SipTrunk>();
        List< ? extends SipTrunk> list = m_gatewayContext.getGatewayByType(SipTrunk.class);
        for (SipTrunk t : list) {
            if (equals(t.getSbcDevice()) && t.isEnabled() && !isRedundant(t, itsps)) {
                addGWReference(t, list);
                itsps.add(t);
            }
        }
        return itsps;
    }

    public List<SipTrunk> getMySipTrunks() {
        List<SipTrunk> trunks = new ArrayList<SipTrunk>();
        for (SipTrunk t : m_gatewayContext.getGatewayByType(SipTrunk.class)) {
            if (equals(t.getSbcDevice()) && t.isEnabled()) {
                trunks.add(t);
            }
        }
        return trunks;
    }

    public Location getLocation() {
        if (m_location != null) {
            return m_location;
        }
        Integer id = (Integer) getSettings().getSetting(LOCATION_ID_SETTING).getTypedValue();
        if (id == null) {
            return m_locationsManager.getLocationByAddress(getAddress());
        }
        return m_locationsManager.getLocation(id);
    }
    
    private FreeswitchApi api() {
        String url = m_addressManager.getSingleAddress(BridgeSbcContext.XMLRPC_ADDRESS, getLocation())
                .toString();
        return m_freeswitchApiProvider.getApi(url);
    }

    public void updateBridgeLocationId() {
        Location location = m_locationsManager.getLocationByAddress(getAddress());
        setSettingTypedValue(LOCATION_ID_SETTING, location.getId());
    }

    public static class Context extends ProfileContext<BridgeSbc> {
        public Context(BridgeSbc device, String profileTemplate) {
            super(device, profileTemplate);
        }

        @Override
        public Map<String, Object> getContext() {
            Map<String, Object> context = super.getContext();
            BridgeSbc device = getDevice();
            context.put("itsps", device.getMySipItsps());
            context.put("externalPublicIp", device.getGlobalSipIp());
    		context.put("externalPublicPort", device.getGlobalSipPort());
            return context;
        }
    }
    
    public Set<String> getAclAllowedAddresses() {
        return getSettingTypeAsList("bridge-configuration/allow-addresses");
    }

	public Set<String> getAclDenyAddresses() {
        return getSettingTypeAsList("bridge-configuration/deny-addresses");
    }

    public String getAclDefaultAction() {
        return (String) getSettingTypedValue("bridge-configuration/default-action");
    }

    public Boolean isUseFreeswitch() {
        return (Boolean) getSettingTypedValue("bridge-configuration/use-freeswitch");
    }
    
    public class Defaults {
        private final DeviceDefaults m_defaults;
        private final Location m_location;

        Defaults(DeviceDefaults defaults, Location location) {
            m_defaults = defaults;
            m_location = location;
        }

        @SettingEntry(paths = {"bridge-configuration/local-address", "bridge-configuration/external-address"
                })
        public String getExternalAddress() {
            return m_location.getAddress();
        }

        @SettingEntry(path = "bridge-configuration/global-address")
        public String getGlobalAddress() {
            return m_location.getPublicAddress();
        }

        @SettingEntry(path = "bridge-configuration/sipx-proxy-domain")
        public String getDomainName() {
            return m_defaults.getDomainName();
        }

        @SettingEntry(path = "bridge-configuration/log-directory")
        public String getLogDirectory() {
            return m_defaults.getLogDirectory() + "/";
        }

        @SettingEntry(path = "bridge-configuration/stun-server-address")
        public String getStunServerAddress() {
            return m_location.getStunAddress();
        }

        @SettingEntry(path = "bridge-configuration/sipx-supervisor-host")
        public String getSipxSupervisorHost() {
            return m_location.getFqdn();
        }

        @SettingEntry(path = "bridge-configuration/sipx-supervisor-xml-rpc-port")
        public int getSipxSupervisorXmlRpcPort() {
            return Location.PROCESS_MONITOR_PORT;
        }
        
        @SettingEntry(path = "bridge-configuration/music-on-hold-supported-codecs")
        public List<String> getFreeswitchCodecs() {
            ArrayList<String> returnList = new ArrayList<String>();
            returnList.add("G722");
            returnList.add("PCMU@20i");
            returnList.add("PCMA@20i");
            returnList.add("speex");
            returnList.add("L16");
            return returnList;
        }        
    }
    
    public String getGlobalSipIp() {
    	return (String) getSettingTypedValue("bridge-configuration/global-address");
    }
    
    public int getGlobalSipPort() {
    	Integer port = (Integer) getSettingTypedValue("bridge-configuration/global-port");
    	return port != null ? port : 0;
    }

    public int getExternalSipPort() {
        return (Integer) getSettingTypedValue("bridge-configuration/external-port");
    }

    public int getXmlRpcPort() {
        return (Integer) getSettingTypedValue("bridge-configuration/xml-rpc-port");
    }

    @Override
    public Collection<Feature> getAffectedFeaturesOnChange() {
        return Arrays.asList((Feature) DialPlanContext.FEATURE, (Feature) BridgeSbcContext.FEATURE,
            (Feature) ProxyManager.FEATURE);
    }

    public void setConfigManager(ConfigManager configManager) {
        m_configManager = configManager;
    }
    
    private Set<String> getSettingTypeAsList(String settingName) {
        Set<String> list = new HashSet<String>();
        String listString = (String)getSettingTypedValue(settingName);
		if (StringUtils.isNotEmpty(listString)) {
		    String[] listTokens = StringUtils.split(listString, ',');
		    for (String token : listTokens) {
		        list.add(StringUtils.trim(token));
		    }
		}
		return list;
	}
}
