/*
 *
 *
 * Copyright (C) 2011 eZuce, Inc. All rights reserved.
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
package org.sipfoundry.sipxconfig.site.openacd;

import org.apache.tapestry.IActionListener;
import org.sipfoundry.sipxconfig.components.selection.OptionAdapter;
import org.sipfoundry.sipxconfig.openacd.OpenAcdSkillGroup;

public abstract class OpenAcdSkillGroupAction implements OptionAdapter, IActionListener {
    private Integer m_id;

    private OpenAcdSkillGroup m_skillGroup;

    public OpenAcdSkillGroupAction(OpenAcdSkillGroup skillGroup) {
        m_skillGroup = skillGroup;
    }

    public Integer getId() {
        return m_id;
    }

    public void setId(Integer id) {
        m_id = id;
    }

    public OpenAcdSkillGroup getSkillGroup() {
        return m_skillGroup;
    }

    public void setSkillGroup(OpenAcdSkillGroup skillGroup) {
        m_skillGroup = skillGroup;
    }

    public Object getValue(Object option, int index) {
        return this;
    }

    public String getLabel(Object option, int index) {
        return m_skillGroup.getName();
    }

    public String squeezeOption(Object option, int index) {
        return getClass().getName() + m_skillGroup.getId();
    }

    public String getMethodName() {
        return null;
    }
}
