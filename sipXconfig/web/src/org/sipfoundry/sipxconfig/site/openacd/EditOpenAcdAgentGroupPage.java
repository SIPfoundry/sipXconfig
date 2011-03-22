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
package org.sipfoundry.sipxconfig.site.openacd;

import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.sipfoundry.sipxconfig.common.CoreContext;
import org.sipfoundry.sipxconfig.components.PageWithCallback;
import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;
import org.sipfoundry.sipxconfig.components.TapestryUtils;
import org.sipfoundry.sipxconfig.openacd.OpenAcdAgentGroup;
import org.sipfoundry.sipxconfig.openacd.OpenAcdContext;

public abstract class EditOpenAcdAgentGroupPage extends PageWithCallback implements PageBeginRenderListener {
    public static final String PAGE = "openacd/EditOpenAcdAgentGroupPage";

    @InjectObject("spring:coreContext")
    public abstract CoreContext getCoreContext();

    @InjectObject("spring:openAcdContext")
    public abstract OpenAcdContext getOpenAcdContext();

    @Bean
    public abstract SipxValidationDelegate getValidator();

    @Persist
    public abstract Integer getGroupId();

    public abstract void setGroupId(Integer groupId);

    public abstract OpenAcdAgentGroup getAgentGroup();

    public abstract void setAgentGroup(OpenAcdAgentGroup agentGroup);

    public void addAgentGroup(String returnPage) {
        setGroupId(null);
        setAgentGroup(null);
        setReturnPage(returnPage);
    }

    public void editAgentGroup(Integer groupId, String returnPage) {
        setGroupId(groupId);
        setReturnPage(returnPage);
    }

    @Override
    public void pageBeginRender(PageEvent event) {
        if (!TapestryUtils.isValid(this)) {
            return;
        }

        if (getGroupId() == null) {
            setAgentGroup(new OpenAcdAgentGroup());
        } else {
            setAgentGroup(getOpenAcdContext().getAgentGroupById(getGroupId()));
        }

        OpenAcdAgentGroup group = getAgentGroup();
        if (getAddedAgents() != null && getAddedAgents().size() > 0) {
            List<OpenAcdAgent> existingAgents = new ArrayList<OpenAcdAgent>();
            try {
                existingAgents = getOpenAcdContext().addAgentsToGroup(group,
                        getAgentsBySelectedIds(getAddedAgents()));
            } catch (UserException uex) {
                IValidationDelegate validator = TapestryUtils.getValidator(getPage());
                validator.record(new ValidatorException(getMessages().getMessage(uex.getMessage())));
            }
            if (!existingAgents.isEmpty()) {
                List<String> existingAgentNames = new ArrayList<String>(existingAgents.size());
                for (OpenAcdAgent agent : existingAgents) {
                    existingAgentNames.add(agent.getUser().getUserName());
                }
                String msg = getMessages().format("duplicate.agents.error",
                        StringUtils.join(existingAgentNames, ", "));
                getValidator().record(new ValidatorException(msg));
            }
            // force a reload
            setAgentGroup(getOpenAcdContext().getAgentGroupById(group.getId()));
        }
    }

    private List<OpenAcdAgent> getAgentsBySelectedIds(Collection selectedIds) {
        List<OpenAcdAgent> agents = new ArrayList<OpenAcdAgent>();
        for (Object obj : selectedIds) {
            OpenAcdAgent agent = new OpenAcdAgent();
            agent.setUser(getCoreContext().loadUser((Integer) obj));
            agents.add(agent);
        }
        return agents;
    }

    public IPage addAgent(IRequestCycle cycle) {
        OpenAcdAgentGroup group = getAgentGroup();
        if (StringUtils.isBlank(group.getName())) {
            getValidator().record(new ValidatorException(getMessages().getMessage("blank.agentGroupName.error")));
            return this;
        }
        getOpenAcdContext().saveAgentGroup(group);
        setGroupId(group.getId());

        SelectUsers addAgents = (SelectUsers) cycle.getPage(SelectUsers.PAGE);
        SelectUsersCallback callback = new SelectUsersCallback(this.getPage());
        callback.setIdsPropertyName("addedAgents");
        addAgents.setCallback(callback);
        addAgents.setTitle(getMessages().getMessage("title.selectAgents"));
        addAgents.setPrompt(getMessages().getMessage("prompt.selectAgents"));
        return addAgents;
    }

    public IPage editAgent(IRequestCycle cycle, Integer agentId) {
        EditOpenAcdAgentPage page = (EditOpenAcdAgentPage) cycle.getPage(EditOpenAcdAgentPage.PAGE);
        page.editAgent(agentId, getPage().getPageName());
        return page;
    }

    public void delete() {
        Collection ids = getSelections().getAllSelected();
        if (ids.isEmpty()) {
            return;
        }
        OpenAcdAgentGroup group = getAgentGroup();
        getOpenAcdContext().deleteAgents(group.getId(), ids);
    }

    public void commit() {
        if (!TapestryUtils.isValid(this)) {
            return;
        }

        getOpenAcdContext().saveAgentGroup(getAgentGroup());
        setGroupId(getAgentGroup().getId());
    }
}
