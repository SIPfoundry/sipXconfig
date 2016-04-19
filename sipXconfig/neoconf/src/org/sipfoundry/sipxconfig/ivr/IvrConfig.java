/**
 *
 *
 * Copyright (c) 2010 / 2011 eZuce, Inc. All rights reserved.
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
package org.sipfoundry.sipxconfig.ivr;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.sipfoundry.sipxconfig.address.Address;
import org.sipfoundry.sipxconfig.admin.AdminContext;
import org.sipfoundry.sipxconfig.alarm.AlarmDefinition;
import org.sipfoundry.sipxconfig.alarm.AlarmProvider;
import org.sipfoundry.sipxconfig.alarm.AlarmServerManager;
import org.sipfoundry.sipxconfig.apache.ApacheManager;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigException;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigManager;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigProvider;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigRequest;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigUtils;
import org.sipfoundry.sipxconfig.cfgmgt.LoggerKeyValueConfiguration;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.dialplan.AutoAttendantManager;
import org.sipfoundry.sipxconfig.dialplan.DialPlanContext;
import org.sipfoundry.sipxconfig.dialplan.attendant.AutoAttendantSettings;
import org.sipfoundry.sipxconfig.domain.Domain;
import org.sipfoundry.sipxconfig.feature.FeatureManager;
import org.sipfoundry.sipxconfig.freeswitch.FreeswitchFeature;
import org.sipfoundry.sipxconfig.im.ImManager;
import org.sipfoundry.sipxconfig.imbot.ImBot;
import org.sipfoundry.sipxconfig.mwi.Mwi;
import org.sipfoundry.sipxconfig.restserver.RestServer;
import org.sipfoundry.sipxconfig.setting.Setting;
import org.sipfoundry.sipxconfig.setting.SettingUtil;
import org.springframework.beans.factory.annotation.Required;

public class IvrConfig implements ConfigProvider, AlarmProvider {
    private Ivr m_ivr;
    private Mwi m_mwi;
    private AutoAttendantManager m_aaManager;
    private AdminContext m_adminContext;

    @Override
    public void replicate(ConfigManager manager, ConfigRequest request) throws IOException {
        if (!request.applies(DialPlanContext.FEATURE, Ivr.FEATURE, Mwi.FEATURE, RestServer.FEATURE, ImBot.FEATURE,
                FreeswitchFeature.FEATURE, AdminContext.FEATURE, ApacheManager.FEATURE, ImManager.FEATURE)) {
            return;
        }
        Set<Location> locations = request.locations(manager);
        FeatureManager featureManager = manager.getFeatureManager();
        Address adminApi = manager.getAddressManager().getSingleAddress(AdminContext.HTTP_ADDRESS_AUTH);
        Address apacheApi = manager.getAddressManager().getSingleAddress(ApacheManager.HTTPS_ADDRESS);
        Address restApi = manager.getAddressManager().getSingleAddress(RestServer.HTTP_API);
        Address imApi = manager.getAddressManager().getSingleAddress(ImManager.XMLRPC_ADDRESS);
        Address fsEvent = manager.getAddressManager().getSingleAddress(FreeswitchFeature.EVENT_ADDRESS);
        IvrSettings settings = m_ivr.getSettings();
        Domain domain = manager.getDomainManager().getDomain();
        List<Location> mwiLocations = manager.getFeatureManager().getLocationsForEnabledFeature(Mwi.FEATURE);
        int mwiPort = m_mwi.getSettings().getHttpApiPort();
        Setting ivrSettings = settings.getSettings().getSetting("ivr");
        AutoAttendantSettings aaSettings = m_aaManager.getSettings();
        for (Location location : locations) {
            File dir = manager.getLocationDataDirectory(location);
            boolean enabled = featureManager.isFeatureEnabled(Ivr.FEATURE, location);

            ConfigUtils.enableCfengineClass(dir, "sipxivr.cfdat", enabled, "sipxivr");
            if (!enabled) {
                continue;
            }

            String log4jFileName = "log4j-ivr.properties.part";
            String[] logLevelKeys = {
                "log4j.logger.org.sipfoundry.attendant", "log4j.logger.org.sipfoundry.bridge",
                "log4j.logger.org.sipfoundry.conference", "log4j.logger.org.sipfoundry.faxrx",
                "log4j.logger.org.sipfoundry.moh", "log4j.logger.org.sipfoundry.sipxivr",
                "log4j.logger.org.sipfoundry.voicemail"
            };
            SettingUtil.writeLog4jSetting(ivrSettings, dir, log4jFileName, logLevelKeys);

            File f = new File(dir, "sipxivr.properties.part");
            Writer wtr = new FileWriter(f);
            try {
                write(wtr, settings, domain, location, getMwiLocations(mwiLocations, location), mwiPort, restApi,
                        adminApi, apacheApi, imApi, fsEvent, aaSettings, m_adminContext.isHazelcastEnabled());
            } finally {
                IOUtils.closeQuietly(wtr);
            }
        }
    }

    public String getMwiLocations(List<Location> mwiLocations, Location currentLocation) {
        Set<String> mwiAddresses = new LinkedHashSet<String>();
        // always append current location first
        if (mwiLocations.contains(currentLocation)) {
            mwiAddresses.add(getMwiAddress(currentLocation));
        }
        for (Location mwiLocation : mwiLocations) {
            if (mwiLocation != currentLocation) {
                mwiAddresses.add(getMwiAddress(mwiLocation));
            }
        }
        return StringUtils.join(mwiAddresses, ",");
    }

    private String getMwiAddress(Location location) {
        StringBuilder address = new StringBuilder();
        address.append(location.getAddress());
        if (location.getRegionId() != null) {
            address.append("@");
            address.append(location.getRegionId());
        }
        return address.toString();
    }

    void write(Writer wtr, IvrSettings settings, Domain domain, Location location, String mwiAddresses, int mwiPort,
            Address restApi, Address adminApi, Address apacheApi, Address imApi, Address fsEvent,
            AutoAttendantSettings aaSettings, boolean hzEnabled)
        throws IOException {
        LoggerKeyValueConfiguration config = LoggerKeyValueConfiguration.equalsSeparated(wtr);
        config.writeSettings(settings.getSettings());
        config.write("freeswitch.eventSocketPort", fsEvent.getPort());

        // potential bug: name "operator" could be changed by admin. this should be configurable
        // and linked with vm dialing rule
        config.write("ivr.operatorAddr", "sip:operator@" + domain.getName());

        // required services
        if (mwiAddresses == null) {
            throw new ConfigException("MWI feature needs to be enabled. No addresses found.");
        }
        config.write("ivr.mwiAddresses", mwiAddresses);
        config.write("ivr.mwiPort", mwiPort);
        if (adminApi == null) {
            throw new ConfigException("Admin feature needs to be enabled. No addresses found.");
        }
        config.write("ivr.configUrl", adminApi.toString());
        if (apacheApi != null) {
            config.write("ivr.emailAddressUrl", apacheApi.toString());
        }

        // optional services
        if (restApi != null) {
            config.write("ivr.3pccSecureUrl", restApi.toString());
            config.write("ivr.callHistoryUrl", restApi.toString() + "/cdr/");
        }
        if (imApi != null) {
            config.write("ivr.openfireHost", imApi.getAddress());
            config.write("ivr.openfireXmlRpcPort", imApi.getPort());
        }
        config.write("aa.liveAaEnablePrefix", aaSettings.getEnablePrefix());
        config.write("aa.liveAaDisablePrefix", aaSettings.getDisablePrefix());
        config.write("aa.liveAaDid", aaSettings.getLiveDid());
        config.write("aa.dtmf.maxDigits", aaSettings.getMaxDigits());
        config.write("aa.dtmf.firstDigitTimeout", aaSettings.getFirstDigit());
        config.write("aa.dtmf.interDigitTimeout", aaSettings.getInterDigit());
        config.write("aa.dtmf.extraDigitTimeout", aaSettings.getExtraDigit());
        config.write("ivr.hzEnabled", hzEnabled);
    }

    @Override
    public Collection<AlarmDefinition> getAvailableAlarms(AlarmServerManager manager) {
        if (!manager.getFeatureManager().isFeatureEnabled(Ivr.FEATURE)) {
            return null;
        }
        String[] ids = new String[] {
            "SIPXIVR_FAILED_LOGIN"
        };

        return AlarmDefinition.asArray(ids);
    }

    @Required
    public void setIvr(Ivr ivr) {
        m_ivr = ivr;
    }

    @Required
    public void setMwi(Mwi mwi) {
        m_mwi = mwi;
    }

    @Required
    public void setAutoAttendantManager(AutoAttendantManager aaManager) {
        m_aaManager = aaManager;
    }

    public void setAdminContext(AdminContext adminContext) {
        m_adminContext = adminContext;
    }
}
