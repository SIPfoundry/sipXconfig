/*
 *
 *
 * Copyright (C) 2008 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */
package org.sipfoundry.sipxconfig.admin.alarm;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.sipfoundry.sipxconfig.admin.commserver.Location;
import org.sipfoundry.sipxconfig.admin.commserver.SipxReplicationContext;

public interface AlarmServerManager {
    AlarmServer getAlarmServer();

    List<AlarmGroup> getAlarmGroups();

    void removeAlarmGroups(Collection<Integer> groupsIds, List<Alarm> alarms);

    AlarmGroup loadAlarmGroup(Serializable id);

    AlarmGroup getAlarmGroupById(Integer alarmGroupId);

    AlarmGroup getAlarmGroupByName(String alarmGroupName);

    void deleteAlarmGroupsById(Collection<Integer> alarmsIds);

    void saveAlarmGroup(AlarmGroup group);

    void clear();

    void deployAlarmConfiguration(AlarmServer alarmServer, List<Alarm> alarms, List<AlarmGroup> alarmGroups);

    void replicateAlarmServer(SipxReplicationContext replicationContext, Location location);

    List<Alarm> getAlarmTypes();
}
