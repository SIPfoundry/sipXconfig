/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 */
package org.sipfoundry.sipxconfig.admin;


import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang.StringUtils;
import org.sipfoundry.sipxconfig.address.Address;
import org.sipfoundry.sipxconfig.address.AddressManager;
import org.sipfoundry.sipxconfig.address.AddressProvider;
import org.sipfoundry.sipxconfig.address.AddressType;
import org.sipfoundry.sipxconfig.alarm.AlarmDefinition;
import org.sipfoundry.sipxconfig.alarm.AlarmProvider;
import org.sipfoundry.sipxconfig.alarm.AlarmServerManager;
import org.sipfoundry.sipxconfig.backup.ArchiveDefinition;
import org.sipfoundry.sipxconfig.backup.ArchiveProvider;
import org.sipfoundry.sipxconfig.backup.BackupManager;
import org.sipfoundry.sipxconfig.backup.BackupSettings;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.commserver.LocationsManager;
import org.sipfoundry.sipxconfig.firewall.DefaultFirewallRule;
import org.sipfoundry.sipxconfig.firewall.FirewallManager;
import org.sipfoundry.sipxconfig.firewall.FirewallProvider;
import org.sipfoundry.sipxconfig.snmp.ProcessDefinition;
import org.sipfoundry.sipxconfig.snmp.ProcessProvider;
import org.sipfoundry.sipxconfig.snmp.SnmpManager;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Backup provides Java interface to backup scripts
 */
public class AdminContextImpl extends HibernateDaoSupport implements AdminContext, AddressProvider, ProcessProvider,
    AlarmProvider, FirewallProvider, ArchiveProvider {
    private LocationsManager m_locationsManager;
    private int m_internalPort;

    @Override
    public Collection<Address> getAvailableAddresses(AddressManager manager, AddressType type, Location requester) {
        if (!type.equals(HTTP_ADDRESS)) {
            return null;
        }

        Location location = m_locationsManager.getPrimaryLocation();
        Address address = new Address(HTTP_ADDRESS, location.getAddress(), m_internalPort);
        return Collections.singleton(address);
    }

    public void setLocationsManager(LocationsManager locationsManager) {
        m_locationsManager = locationsManager;
    }

    @Override
    public Collection<ProcessDefinition> getProcessDefinitions(SnmpManager manager, Location location) {
        return (location.isPrimary() ? Collections.singleton(ProcessDefinition.sipxDefault("sipxconfig",
                ".*-Dprocname=sipxconfig.*")) : null);
    }

    @Override
    public Collection<AlarmDefinition> getAvailableAlarms(AlarmServerManager manager) {
        return Collections.singleton(ALARM_LOGIN_FAILED);
    }

    @Required
    public void setInternalPort(int internalPort) {
        m_internalPort = internalPort;
    }

    @Override
    public Collection<DefaultFirewallRule> getFirewallRules(FirewallManager manager) {
        return Collections.singleton(new DefaultFirewallRule(HTTP_ADDRESS));
    }

    @Override
    public void avoidCheckstyleError() {
    }

    @Override
    public Collection<ArchiveDefinition> getArchiveDefinitions(BackupManager manager, Location location,
            BackupSettings settings) {
        if (!location.isPrimary()) {
            return null;
        }
        StringBuilder restore = new StringBuilder(
                "$(sipx.SIPX_BINDIR)/sipxconfig-archive --restore %s --ipaddress $(sipx.bind_ip)");
        if (settings != null) {
            if (settings.isKeepDomain()) {
                restore.append(" --domain $(sipx.domain)");
            }
            if (settings.isKeepFqdn()) {
                restore.append(" --fqdn $(sipx.host).$(sipx.net_domain)");
            }
            String resetPin = settings.getResetPin();
            if (settings.isDecodePins()) {
                restore.append(" --crack-pin ").append(resetPin);
                restore.append(" --crack-pin-len ").append(settings.getDecodePinLen());
            } else if (StringUtils.isNotBlank(resetPin)) {
                restore.append(" --reset-pin ").append(resetPin);
            }
        }
        ArchiveDefinition def = new ArchiveDefinition(ARCHIVE,
                "$(sipx.SIPX_BINDIR)/sipxconfig-archive --backup %s", restore.toString());
        return Collections.singleton(def);
    }
}
