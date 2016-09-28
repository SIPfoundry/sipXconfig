/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.paging;
import static java.lang.String.format;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigManager;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigProvider;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigRequest;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigUtils;
import org.sipfoundry.sipxconfig.cfgmgt.LoggerKeyValueConfiguration;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.paging.config.AbstractPagingConfiguration;
import org.sipfoundry.sipxconfig.paging.config.FSPagingProvider;
import org.sipfoundry.sipxconfig.setting.Setting;
import org.sipfoundry.sipxconfig.setting.SettingUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;

public class PagingConfiguration implements ConfigProvider, BeanFactoryAware {
    private PagingContext m_pagingContext;
    private String m_audioDirectory;
    private ListableBeanFactory m_beanFactory;
    
    private boolean m_useFreeswitch = true;

    @Override
    public void replicate(ConfigManager manager, ConfigRequest request) throws IOException {
        if (!request.applies(PagingContext.FEATURE)) {
            return;
        }

        Set<Location> locations = request.locations(manager);
        PagingSettings settings = m_pagingContext.getSettings();
        String domainName = manager.getDomainManager().getDomainName();
        List<PagingGroup> groups = m_pagingContext.getPagingGroups();
        for (Location location : locations) {
            File dir = manager.getLocationDataDirectory(location);
            boolean enabled = manager.getFeatureManager().isFeatureEnabled(PagingContext.FEATURE, location);
            ConfigUtils.enableCfengineClass(dir, "sipxpage.cfdat", enabled, "sipxpage");
            if (!enabled) {
                continue;
            }

            if(m_useFreeswitch) {
            	writeFreeswitchPaging(settings, location, dir, groups, domainName);
            } else {
            	writeInternalPaging(settings, location, dir, groups, domainName);
            }
        }
    }
    
    void writeInternalPaging(PagingSettings settings, Location location, File dir, List<PagingGroup> groups, String domainName) 
    		throws IOException {
    	String log4jFileName = "log4j-page.properties.part";
        String[] logLevelKeys = {"log4j.logger.org.sipfoundry.sipxpage"};
        Setting pagingSettings = settings.getSettings().getSetting("page-config");
        SettingUtil.writeLog4jSetting(pagingSettings, dir, log4jFileName, logLevelKeys);

        FileWriter writer = new FileWriter(new File(dir, "sipxpage.properties.part"));
        try {
            write(writer, location, groups, settings, domainName);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }
    
    void writeFreeswitchPaging(PagingSettings settings, Location location, File dir, List<PagingGroup> groups, String domainName) 
    		throws IOException {
    	Map<String, AbstractPagingConfiguration> configs = m_beanFactory
                .getBeansOfType(AbstractPagingConfiguration.class);
        for (AbstractPagingConfiguration config : configs.values()) {
        	File f = new File(dir, config.getFileName());
            f.getParentFile().mkdirs();
            FileWriter writer = new FileWriter(f);
            config.write(writer, location, m_pagingContext);
            IOUtils.closeQuietly(writer);
        }

        Map<String, FSPagingProvider> providers = m_beanFactory.getBeansOfType(FSPagingProvider.class);
        Writer modWriter = null;
        try {
            modWriter = new FileWriter(new File(dir, "sipxpage/modules.conf.xml.part"));
            writeModsParts(modWriter, providers.values(), location);
        } finally {
            IOUtils.closeQuietly(modWriter);
        }
    }

    void write(Writer writer, Location location, List<PagingGroup> groups, PagingSettings settings, String domainName)
        throws IOException {
        LoggerKeyValueConfiguration config = LoggerKeyValueConfiguration.colonSeparated(writer);
        config.write("sip.address", location.getAddress());
        config.write("rtp.port", settings.getRtpPort());
        config.write("sip.tlsPort", settings.getSipTlsPort());
        config.write("sip.udpPort", settings.getSipUdpPort());
        config.write("sip.tcpPort", settings.getSipTcpPort());
        config.write("sip.trace", settings.getSipTraceLevel());
        for (int i = 0; i < groups.size(); i++) {
            PagingGroup g = groups.get(i);
            if (g.isEnabled()) {
                String prefix = "page.group." + (i + 1);
                config.write(prefix + ".user", g.getPageGroupNumber());
                config.write(prefix  + ".description", StringUtils.defaultString(g.getDescription()));
                config.write(prefix + ".urls", g.formatUserList(domainName));
                String beep = format("file://%s/%s", m_audioDirectory, g.getSound());
                config.write(prefix + ".beep", beep);
                long millis = ((long) g.getTimeout()) * 1000;
                config.write(prefix + ".timeout", millis);
            }
        }
    }
    
    private void writeModsParts(Writer w, Collection<FSPagingProvider> providers, Location location) throws IOException {
        List<String> mods = new ArrayList<String>();
        for (FSPagingProvider provider : providers) {
            mods.addAll(provider.getRequiredModules(m_pagingContext, location));
        }
        for (String mod : mods) {
            String entry = String.format("<load module=\"%s\"/>\n", mod);
            w.append(entry);
        }
    }
    
    @Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		m_beanFactory = (ListableBeanFactory) beanFactory;
	}

    public void setPagingContext(PagingContext pagingContext) {
        m_pagingContext = pagingContext;
    }

    public void setAudioDirectory(String audioDirectory) {
        m_audioDirectory = audioDirectory;
    }
    
    public void setUseFreeswitch(boolean useFreeswitch) {
    	m_useFreeswitch = useFreeswitch;
    }

    public String getAudioDirectory() {
        return m_audioDirectory;
    }
}
