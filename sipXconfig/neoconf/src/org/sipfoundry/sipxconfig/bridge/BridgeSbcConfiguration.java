/**
 *
 *
 * Copyright (c) 2012 eZuce, Inc. All rights reserved.
 * Contributed to SIPfoundry under a Contributor Agreement
 *
 * This software is free software; you can redistribute it and/or modify it under
 * the terms of the Affero General Public License (AGPL) as published by the
 * Free Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 */
package org.sipfoundry.sipxconfig.bridge;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.sipfoundry.sipxconfig.alarm.AlarmDefinition;
import org.sipfoundry.sipxconfig.alarm.AlarmProvider;
import org.sipfoundry.sipxconfig.alarm.AlarmServerManager;
import org.sipfoundry.sipxconfig.bridge.config.AbstractTrunkConfiguration;
import org.sipfoundry.sipxconfig.bridge.config.FSTrunkProvider;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigManager;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigProvider;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigRequest;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigUtils;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.device.ProfileLocation;
import org.sipfoundry.sipxconfig.feature.Bundle;
import org.sipfoundry.sipxconfig.feature.FeatureChangeRequest;
import org.sipfoundry.sipxconfig.feature.FeatureChangeValidator;
import org.sipfoundry.sipxconfig.feature.FeatureManager;
import org.sipfoundry.sipxconfig.feature.FeatureProvider;
import org.sipfoundry.sipxconfig.feature.GlobalFeature;
import org.sipfoundry.sipxconfig.feature.LocationFeature;
import org.sipfoundry.sipxconfig.nattraversal.NatTraversal;
import org.sipfoundry.sipxconfig.proxy.ProxyManager;
import org.sipfoundry.sipxconfig.sbc.SbcDeviceManager;
import org.sipfoundry.sipxconfig.setting.Setting;
import org.sipfoundry.sipxconfig.setting.SettingUtil;
import org.sipfoundry.sipxconfig.snmp.ProcessDefinition;
import org.sipfoundry.sipxconfig.snmp.ProcessProvider;
import org.sipfoundry.sipxconfig.snmp.SnmpManager;
import org.sipfoundry.sipxconfig.tls.TlsPeerManager;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;

public class BridgeSbcConfiguration implements ConfigProvider, ProcessProvider, FeatureProvider, AlarmProvider, BeanFactoryAware {
    // uses of this definition are not related, just defined in one place to avoid checkstyle err
    private static final String SIPXBRIDGE = "sipxbridge";
    
    private SbcDeviceManager m_sbcDeviceManager;
    private BridgeSbcContext m_sbcBridgeContext;
    private ListableBeanFactory m_beanFactory;
    
    /** Default sbc to use freeswitch instead of sipxbridge **/
    private boolean m_useFreeswitch = true;
    
    @Override
    public void replicate(ConfigManager manager, ConfigRequest request) throws IOException {
        if (!request.applies(ProxyManager.FEATURE, BridgeSbcContext.FEATURE, TlsPeerManager.FEATURE)) {
            return;
        }

        Set<Location> locations = request.locations(manager);
        List<BridgeSbc> bridges = m_sbcDeviceManager.getBridgeSbcs();
        Map<Integer, BridgeSbc> bridgesMap = new HashMap<Integer, BridgeSbc>();
        for (BridgeSbc bridge : bridges) {
            bridgesMap.put(bridge.getLocation().getId(), bridge);
        }
        for (Location location : locations) {
            BridgeSbc bridge = bridgesMap.get(location.getId());
            boolean bridgeHere = bridge != null ? true : false;
            File dir = manager.getLocationDataDirectory(location);
            ConfigUtils.enableCfengineClass(dir, "sipxbridge.cfdat", bridgeHere, SIPXBRIDGE);

            if (bridgeHere) {
            	if(m_useFreeswitch) {
            		replicateFreeswitchBridge(bridge, location, dir);
            	} else {
            		replicateInternalBridge(bridge, location, dir);
            	}
            }
        }
    }

    public void setSbcDeviceManager(SbcDeviceManager sbcDeviceManager) {
        m_sbcDeviceManager = sbcDeviceManager;
    }

    @Override
    public Collection<ProcessDefinition> getProcessDefinitions(SnmpManager manager, Location location) {
    	if (!manager.getFeatureManager().isFeatureEnabled(BridgeSbcContext.FEATURE, location)) {
            return null;
        }
                
        ProcessDefinition def = ProcessDefinition.sipxByRegex("sipxbridge", ".*\\s-conf\\s$(sipx.SIPX_CONFDIR)/sipxbridge/conf\\s.*");
        return Collections.singleton(def);
    }

    @Override
    public void featureChangePrecommit(FeatureManager manager, FeatureChangeValidator validator) {
        validator.requiresGlobalFeature(BridgeSbcContext.FEATURE, NatTraversal.FEATURE);
        validator.requiresAtLeastOne(BridgeSbcContext.FEATURE, ProxyManager.FEATURE);
    }

    @Override
    public void featureChangePostcommit(FeatureManager manager, FeatureChangeRequest request) {
        if (request.hasChanged(BridgeSbcContext.FEATURE)) {
            for (Location l : request.getLocationsForEnabledFeature(BridgeSbcContext.FEATURE)) {
                BridgeSbc bridgeSbc = m_sbcDeviceManager.getBridgeSbc(l);
                if (bridgeSbc == null) {
                    m_sbcDeviceManager.newBridgeSbc(l);
                }
            }
            for (Location l : request.getLocationsForDisabledFeature(BridgeSbcContext.FEATURE)) {
                BridgeSbc bridgeSbc = m_sbcDeviceManager.getBridgeSbc(l);
                if (bridgeSbc != null) {
                    m_sbcDeviceManager.deleteSbcDevice(bridgeSbc.getId());
                }
            }
        }
    }

    @Override
    public Collection<GlobalFeature> getAvailableGlobalFeatures(FeatureManager featureManager) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<LocationFeature> getAvailableLocationFeatures(FeatureManager featureManager, Location l) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void getBundleFeatures(FeatureManager featureManager, Bundle b) {
        if (b == Bundle.ADVANCED_TELEPHONY) {
            b.addFeature(BridgeSbcContext.FEATURE);
        }
    }

    @Override
    public Collection<AlarmDefinition> getAvailableAlarms(AlarmServerManager manager) {
        if (!manager.getFeatureManager().isFeatureEnabled(BridgeSbcContext.FEATURE)) {
            return null;
        }
        String[] ids = new String[] {
            "BRIDGE_STUN_FAILURE", "BRIDGE_STUN_RECOVERY", "BRIDGE_STUN_PUBLIC_ADDRESS_CHANGED",
            "BRIDGE_ACCOUNT_NOT_FOUND", "BRIDGE_ACCOUNT_CONFIGURATION_ERROR", "BRIDGE_OPERATION_TIMED_OUT",
            "BRIDGE_ITSP_SERVER_FAILURE", "BRIDGE_AUTHENTICATION_FAILED", "BRIDGE_ITSP_ACCOUNT_CONFIGURATION_ERROR",
            "BRIDGE_TLS_CERTIFICATE_MISMATCH", "BRIDGE_ACCOUNT_OK"
        };

        return AlarmDefinition.asArray(ids);
    }
    
    @Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		m_beanFactory = (ListableBeanFactory) beanFactory;
	}
    
    public void setSbcBridgeContext(BridgeSbcContext sbcBridgeContext) {
    	m_sbcBridgeContext = sbcBridgeContext;
    }
    
    public void setUseFreeswitch(boolean useFreeswitch) {
    	m_useFreeswitch = useFreeswitch;
    }
    
    private void replicateInternalBridge(BridgeSbc bridge, Location location, File configDir) 
    		throws IOException {
    	Setting settings = bridge.getSettings();
        Setting bridgeSettings = settings.getSetting("bridge-configuration");
        String log4jFileName = "log4j-bridge.properties.part";
        String[] logLevelKeys = {"log4j.logger.org.sipfoundry.sipxbridge",
                                 "log4j.logger.org.sipfoundry.commons" };
        SettingUtil.writeLog4jSetting(bridgeSettings, configDir, log4jFileName, logLevelKeys);

        // strange object for profile location to be compatible with device module
        ProfileLocation profileLocation = bridge.getProfileLocation();
        bridge.generateFiles(profileLocation);
    }
    
    private void replicateFreeswitchBridge(BridgeSbc bridge, Location location, File configDir) 
    		throws IOException {
    	Map<String, AbstractTrunkConfiguration> configs = m_beanFactory
                .getBeansOfType(AbstractTrunkConfiguration.class);
        for (AbstractTrunkConfiguration config : configs.values()) {
        	File f = new File(configDir, config.getFileName());
            f.getParentFile().mkdirs();
            FileWriter writer = new FileWriter(f);
            config.write(writer, location, bridge);
            IOUtils.closeQuietly(writer);
        }

        Map<String, FSTrunkProvider> providers = m_beanFactory.getBeansOfType(FSTrunkProvider.class);
        Writer modWriter = null;
        try {
            modWriter = new FileWriter(new File(configDir, "sipxbridge/modules.conf.xml.part"));
            writeModsParts(modWriter, providers.values(), location);
        } finally {
            IOUtils.closeQuietly(modWriter);
        }
    }
    
    private void writeModsParts(Writer w, Collection<FSTrunkProvider> providers, Location location) throws IOException {
        List<String> mods = new ArrayList<String>();
        for (FSTrunkProvider provider : providers) {
            mods.addAll(provider.getRequiredModules(m_sbcBridgeContext, location));
        }
        for (String mod : mods) {
            String entry = String.format("<load module=\"%s\"/>\n", mod);
            w.append(entry);
        }
    }
}
