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
package org.sipfoundry.sipxconfig.openacd;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class OpenAcdQueue extends OpenAcdQueueWithSkills {
    private String m_name;
    private String m_description;
    private OpenAcdQueueGroup m_group;
    private int m_weight = 1;
    private String m_oldName;

    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    public String getDescription() {
        return m_description;
    }

    public void setDescription(String description) {
        m_description = description;
    }

    public OpenAcdQueueGroup getGroup() {
        return m_group;
    }

    public void setGroup(OpenAcdQueueGroup group) {
        m_group = group;
    }

    public int getWeight() {
        return m_weight;
    }

    public void setWeight(int weight) {
        m_weight = weight;
    }

    public String getQueueGroup() {
        return m_group.getName();
    }

    public String getOldName() {
        return m_oldName;
    }

    public void setOldName(String oldName) {
        m_oldName = oldName;
    }

    public int hashCode() {
        return new HashCodeBuilder().append(m_name).toHashCode();
    }

    public boolean equals(Object other) {
        if (!(other instanceof OpenAcdQueue)) {
            return false;
        }
        if (this == other) {
            return true;
        }
        OpenAcdQueue bean = (OpenAcdQueue) other;
        return new EqualsBuilder().append(m_name, bean.getName()).isEquals();
    }

    @Override
    public List<String> getProperties() {
        List<String> props = new LinkedList<String>();
        props.add("name");
        props.add("queueGroup");
        props.add("skillsAtoms");
        props.add("profiles");
        props.add("weight");
        props.add("oldName");
        return props;
    }

    @Override
    public String getType() {
        return "queue";
    }
}
