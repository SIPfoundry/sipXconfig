/*
 *
 *
 * Copyright (C) 2010 eZuce, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package org.sipfoundry.sipxconfig.freeswitch;

import java.util.LinkedHashSet;
import java.util.Set;

import org.sipfoundry.sipxconfig.admin.commserver.Location;
import org.sipfoundry.sipxconfig.common.BeanWithId;

public abstract class FreeswitchExtension extends BeanWithId {
    private String m_name;
    private String m_description;
    private Set<FreeswitchCondition> m_conditions;
    private Location m_location;

    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    public Set<FreeswitchCondition> getConditions() {
        return m_conditions;
    }

    public void setConditions(Set<FreeswitchCondition> conditions) {
        m_conditions = conditions;
    }

    public Location getLocation() {
        return m_location;
    }

    public void setLocation(Location location) {
        m_location = location;
    }

    public String getDescription() {
        return m_description;
    }

    public void setDescription(String description) {
        m_description = description;
    }

    public void addCondition(FreeswitchCondition condition) {
        if (m_conditions == null) {
            m_conditions = new LinkedHashSet<FreeswitchCondition>();
        }
        if (condition != null) {
            m_conditions.add(condition);            
        }
    }

}
