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
package org.sipfoundry.sipxconfig.dns;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.sipfoundry.commons.util.IPAddressUtil;
import org.sipfoundry.sipxconfig.address.Address;
import org.sipfoundry.sipxconfig.cfgmgt.DeployConfigOnEdit;
import org.sipfoundry.sipxconfig.common.UserException;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.commserver.LocationsManager;
import org.sipfoundry.sipxconfig.feature.Feature;
import org.sipfoundry.sipxconfig.sbc.SbcManager;
import org.sipfoundry.sipxconfig.sbc.SbcRoutes;
import org.sipfoundry.sipxconfig.setting.PersistableSettings;
import org.sipfoundry.sipxconfig.setting.Setting;
import org.sipfoundry.sipxconfig.setting.SettingEntry;
import org.sipfoundry.sipxconfig.setting.SettingUtil;

public class DnsSettings extends PersistableSettings implements DeployConfigOnEdit {
    private static final String FORWARDERS = "named-config/forwarders";
    private static final String FORWARDER_N = FORWARDERS + "/forwarder_";
    private static final String UNMANAGED_SETTING = "sys/unmanaged";
    private static final String DNS_UNMANAGED_SERVER_0 = "sys/unmanaged_servers/unmanaged_0";
    private static final String DNS_UNMANAGED_SERVER_1 = "sys/unmanaged_servers/unmanaged_1";
    private static final String DNS_UNMANAGED_SERVER_2 = "sys/unmanaged_servers/unmanaged_2";
    private static final String DNS_UNMANAGED_SERVER_3 = "sys/unmanaged_servers/unmanaged_3";
    private static final String ACL_IPS = "acl/ips";
    private static final String SEMICOLON = ";";
    private boolean m_unmanagedDefault;
    private LocationsManager m_locationsManager;
    private SbcManager m_sbcManager;

    public DnsSettings() {
        addDefaultBeanSettingHandler(new Defaults());
    }

    public void validate() {
        validateAclList(getAcl());
    }

    public class Defaults {
        @SettingEntry(path = "sys/unmanaged")
        public boolean unmanaged() {
            return m_unmanagedDefault;
        }

        @SettingEntry(path = ACL_IPS)
        public String acl() {
            List<String> ips = new LinkedList<String>();
            List<Location> locations = m_locationsManager.getLocationsList();
            for (Location location : locations) {
                ips.add(location.getAddress());
            }
            SbcRoutes sbcRoutes = m_sbcManager.getRoutes();
            for (String subnet : sbcRoutes.getSubnets()) {
                ips.add(subnet);
            }
            ips.add("127.0.0.0/8");
            return StringUtils.join(ips, ",");
        }
    }

    @Override
    public String getBeanId() {
        return "dnsSettings";
    }

    @Override
    protected Setting loadSettings() {
        return getModelFilesContext().loadModelFile("sipxdns/sipxdns.xml");
    }

    public List<Address> getDnsForwarders() {
        return SettingUtil.getAddresses(DnsManager.DNS_ADDRESS, getSettings(), FORWARDERS);
    }

    public String getAcl() {
        return (String) getSettingTypedValue(ACL_IPS);
    }

    public String getFormattedAcl() {
        Set<String> ips = IPAddressUtil.getIpsSet(getAcl());
        if (ips.size() > 0) {
            return StringUtils.join(ips, SEMICOLON) + SEMICOLON;
        }
        return StringUtils.EMPTY;
    }

    public void setDnsForwarder(String ip, int index) {
        getSettings().getSetting(FORWARDER_N + index).setValue(ip);
    }

    public boolean isServiceUnmanaged() {
        return (Boolean) getSettingTypedValue(UNMANAGED_SETTING);
    }

    public List<String> getUnmanagedDnsServers() {
        List<String> servers = new LinkedList<String>();
        if (isServiceUnmanaged()) {
            addDnsServer(DNS_UNMANAGED_SERVER_0, servers);
            addDnsServer(DNS_UNMANAGED_SERVER_1, servers);
            addDnsServer(DNS_UNMANAGED_SERVER_2, servers);
            addDnsServer(DNS_UNMANAGED_SERVER_3, servers);
        }
        return servers;
    }

    private void validateAclList(String aclList) {
        if (!IPAddressUtil.validateIpList(aclList)) {
            throw new UserException("&msg.invalidcidr", aclList);
        }
    }

    private void addDnsServer(String setting, List<String> servers) {
        String server = getSettingValue(setting);
        if (StringUtils.isNotBlank(server)) {
            servers.add(server);
        }
    }

    @Override
    public Collection<Feature> getAffectedFeaturesOnChange() {
        return Collections.singleton((Feature) DnsManager.FEATURE);
    }

    public void setUnmanagedDefault(boolean unmanagedDefault) {
        m_unmanagedDefault = unmanagedDefault;
    }

    public void setLocationsManager(LocationsManager locationsManager) {
        m_locationsManager = locationsManager;
    }

    public void setSbcManager(SbcManager sbcManager) {
        m_sbcManager = sbcManager;
    }
}
