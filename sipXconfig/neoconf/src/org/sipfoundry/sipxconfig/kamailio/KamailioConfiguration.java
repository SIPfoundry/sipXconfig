/**
 *
 * Copyright (C) 2015 SIPFoundry., certain elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 */
package org.sipfoundry.sipxconfig.kamailio;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.sipfoundry.sipxconfig.cfgmgt.CfengineModuleConfiguration;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigManager;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigProvider;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigRequest;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigUtils;
import org.sipfoundry.sipxconfig.cfgmgt.KeyValueConfiguration;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.dialplan.config.XmlFile;
import org.sipfoundry.sipxconfig.domain.Domain;
import org.sipfoundry.sipxconfig.mysql.MySql;
import org.sipfoundry.sipxconfig.setting.Setting;
import org.sipfoundry.sipxconfig.tls.TlsPeer;

public class KamailioConfiguration implements ConfigProvider {
    private static final String NAMESPACE = "http://www.sipfoundry.org/sipX/schema/xml/peeridentities-00-00";
    private KamailioManager m_kamailioManager;
    private Map<String, String> m_kamailioLogMap;

    @Override
    public void replicate(ConfigManager manager, ConfigRequest request) throws IOException {
        if (!request.applies(KamailioManager.FEATURE)) {
            return;
        }
        
        KamailioSettings settings = m_kamailioManager.getSettings();
        Domain domain = manager.getDomainManager().getDomain();

        Set<Location> locations = request.locations(manager);
        for (Location location : locations) {
            File dir = manager.getLocationDataDirectory(location);
            boolean enabled = manager.getFeatureManager().isFeatureEnabled(KamailioManager.FEATURE, location);
            ConfigUtils.enableCfengineClass(dir, "kamailio.cfdat", enabled
                    , MySql.FEATURE.getId()
                    , KamailioManager.FEATURE.getId());
            
            if (enabled) {
                String password = settings.getMysqlPassword();
                Writer pwd = new FileWriter(new File(dir, "mysql-pwd.properties"));
                Writer pwdCfdat = new FileWriter(new File(dir, "mysql-pwd.cfdat"));
                try {
                    KeyValueConfiguration cfg = KeyValueConfiguration.equalsSeparated(pwd);
                    CfengineModuleConfiguration cfgCfdat = new CfengineModuleConfiguration(pwdCfdat);
                    cfg.write("password", password);
                    cfgCfdat.write("NEW_MYSQL_PASSWORD", password);
                } finally {
                    IOUtils.closeQuietly(pwd);
                    IOUtils.closeQuietly(pwdCfdat);
                }
                
                Writer proxy = new FileWriter(new File(dir, "sipXingress-config.part"));
                try {
                    writeIngress(proxy, settings, location, domain);
                } finally {
                    IOUtils.closeQuietly(proxy);
                }
                
                Writer kamailioCfg = new FileWriter(new File(dir, "kamailio.cfg.part"));
                try {
                    writeKamailio(kamailioCfg, settings, location, domain);
                } finally {
                    IOUtils.closeQuietly(kamailioCfg);
                }
            }
        }
    }

    void writeIngress(Writer wtr, KamailioSettings settings, Location location, Domain domain)
        throws IOException {
        KeyValueConfiguration config = KeyValueConfiguration.colonSeparated(wtr);
        Setting root = settings.getSettings();
        config.writeSettings(root.getSetting("ingress-configuration"));
    }
    
    void writeKamailio(Writer wtr, KamailioSettings settings, Location location, Domain domain)
            throws IOException {
        KeyValueConfiguration config = KeyValueConfiguration.equalsSeparated(wtr);
        Setting root = settings.getSettings();
        Setting ingressCfg = root.getSetting("ingress-configuration");
        
        //Configure Kamailio logging
        String logLevel = ingressCfg.getSetting("SIPX_INGRESS_LOG_LEVEL").getValue();
        config.write("debug", translateLogToKamailioLogLevel(logLevel));
        config.write("log_stderror", "no");
        
        //Configure Kamailio ports
        int port = settings.getSipTcpPort();
        config.write("listen", "udp:" + location.getAddress() + ':' + port);
        config.write("listen", "tcp:" + location.getAddress() + ':' + port);
    }

    public Document getDocument(Collection<TlsPeer> peers) {
        Document document = XmlFile.FACTORY.createDocument();
        final Element peerIdentities = document.addElement("peeridentities", NAMESPACE);
        for (TlsPeer peer : peers) {
            Element peerElement = peerIdentities.addElement("peer");
            peerElement.addElement("trusteddomain").setText(peer.getName());
            peerElement.addElement("internaluser").setText(peer.getInternalUser().getUserName());
        }

        return document;
    }
    
    private String translateLogToKamailioLogLevel(String logLevel) {
        if(m_kamailioLogMap.containsKey(logLevel)) {
            return m_kamailioLogMap.get(logLevel);
        }
        
        throw new IllegalArgumentException("Unable to translate sipx log level to kamailio log: " + logLevel);
    }

    public void setKamailioManager(KamailioManager kamailioManager) {
        this.m_kamailioManager = kamailioManager;
    }
    
    public void setKamailioLogMap(Map<String, String> kamailioLogMap) {
        this.m_kamailioLogMap = kamailioLogMap;
    }

}
