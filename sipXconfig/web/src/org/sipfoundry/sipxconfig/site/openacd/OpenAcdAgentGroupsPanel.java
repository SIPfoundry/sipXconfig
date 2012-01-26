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
package org.sipfoundry.sipxconfig.site.openacd;

import java.util.Collection;
import java.util.List;

import org.apache.tapestry.BaseComponent;
import org.apache.tapestry.IPage;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.ComponentClass;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.apache.tapestry.valid.IValidationDelegate;
import org.apache.tapestry.valid.ValidatorException;
import org.sipfoundry.sipxconfig.common.UserException;
import org.sipfoundry.sipxconfig.components.SelectMap;
import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;
import org.sipfoundry.sipxconfig.components.TapestryUtils;
import org.sipfoundry.sipxconfig.openacd.OpenAcdAgentGroup;
import org.sipfoundry.sipxconfig.openacd.OpenAcdContext;
import org.sipfoundry.sipxconfig.openacd.OpenAcdContextImpl.DefaultAgentGroupDeleteException;

@ComponentClass(allowBody = false, allowInformalParameters = false)
public abstract class OpenAcdAgentGroupsPanel extends BaseComponent implements PageBeginRenderListener {
    private static final String GROUP_NAME_DEFAULT = "Default";
    @InjectObject("spring:openAcdContext")
    public abstract OpenAcdContext getOpenAcdContext();

    @Bean
    public abstract SelectMap getSelections();

    @Bean
    public abstract SipxValidationDelegate getValidator();

    public abstract void setAgentGroups(List<OpenAcdAgentGroup> agentGroups);

    public abstract List<OpenAcdAgentGroup> getAgentGroups();

    public abstract void setCurrentRow(OpenAcdAgentGroup agentGroup);

    public abstract Collection getSelectedRows();

    public IPage addAgentGroup(IRequestCycle cycle) {
        EditOpenAcdAgentGroupPage page = (EditOpenAcdAgentGroupPage) cycle.getPage(EditOpenAcdAgentGroupPage.PAGE);
        page.addAgentGroup(getPage().getPageName());
        return page;
    }

    public IPage editAgentGroup(IRequestCycle cycle, Integer agentGroupId) {
        EditOpenAcdAgentGroupPage page = (EditOpenAcdAgentGroupPage) cycle.getPage(EditOpenAcdAgentGroupPage.PAGE);
        page.editAgentGroup(agentGroupId, getPage().getPageName());
        return page;
    }

    @Override
    public void pageBeginRender(PageEvent event) {
        List<OpenAcdAgentGroup> groups = getOpenAcdContext().getAgentGroups();
        setAgentGroups(groups);
    }

    public void delete() {
        Collection<Integer> ids = getSelections().getAllSelected();
        if (ids.isEmpty()) {
            return;
        }
        try {
            for (Integer id : ids) {
                OpenAcdAgentGroup group = getOpenAcdContext().getAgentGroupById(id);
                try {
                    getOpenAcdContext().deleteAgentGroup(group);
                } catch (DefaultAgentGroupDeleteException e) {
                    IValidationDelegate validator = TapestryUtils.getValidator(getPage());
                    validator.record(new ValidatorException(getMessages()
                            .getMessage("msg.err.defalutAgentGroupDeletion")));
                }
            }
        } catch (UserException ex) {
            IValidationDelegate validator = TapestryUtils.getValidator(getPage());
            validator.record(new ValidatorException(getMessages().getMessage("msg.cannot.connect")));
        }
    }
}
