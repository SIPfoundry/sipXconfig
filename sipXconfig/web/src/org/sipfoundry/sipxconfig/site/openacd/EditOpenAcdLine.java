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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.sipfoundry.sipxconfig.admin.commserver.Location;
import org.sipfoundry.sipxconfig.admin.commserver.LocationsManager;
import org.sipfoundry.sipxconfig.common.UserException;
import org.sipfoundry.sipxconfig.components.PageWithCallback;
import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;
import org.sipfoundry.sipxconfig.components.TapestryUtils;
import org.sipfoundry.sipxconfig.freeswitch.FreeswitchAction;
import org.sipfoundry.sipxconfig.openacd.OpenAcdClient;
import org.sipfoundry.sipxconfig.openacd.OpenAcdContext;
import org.sipfoundry.sipxconfig.openacd.OpenAcdLine;
import org.sipfoundry.sipxconfig.openacd.OpenAcdQueue;
import org.sipfoundry.sipxconfig.service.SipxOpenAcdService;

public abstract class EditOpenAcdLine extends PageWithCallback implements PageBeginRenderListener {
    public static final String PAGE = "openacd/EditOpenAcdLine";
    private static final String SLASH = "/";

    @InjectObject("spring:openAcdContext")
    public abstract OpenAcdContext getOpenAcdContext();

    @InjectObject("spring:locationsManager")
    public abstract LocationsManager getLocationsManager();

    @InjectObject("spring:sipxOpenAcdService")
    public abstract SipxOpenAcdService getSipxOpenAcdService();

    public abstract String getName();

    public abstract void setName(String name);

    public abstract String getDescription();

    public abstract void setDescription(String description);

    public abstract String getLineNumber();

    public abstract void setLineNumber(String number);

    public abstract OpenAcdQueue getSelectedQueue();

    public abstract void setSelectedQueue(OpenAcdQueue queue);

    public abstract OpenAcdClient getSelectedClient();

    public abstract void setSelectedClient(OpenAcdClient client);

    @Persist
    public abstract String getWelcomeMessage();

    public abstract void setWelcomeMessage(String path);

    public abstract boolean isAllowVoicemail();

    public abstract void setAllowVoicemail(boolean allow);

    public abstract boolean isAnswerSupervision();

    public abstract void setAnswerSupervision(boolean answer);

    public abstract ActionBean getActionBean();

    public abstract void setActionBean(ActionBean a);

    public abstract int getIndex();

    public abstract void setIndex(int i);

    @Persist
    public abstract Location getSipxLocation();

    public abstract void setSipxLocation(Location locationId);

    @Persist
    public abstract Integer getOpenAcdLineId();

    public abstract void setOpenAcdLineId(Integer id);

    public abstract String getFilter();

    public abstract void setFilter(String filter);

    @Bean
    public abstract SipxValidationDelegate getValidator();

    public abstract ActionBean getRemoveAction();

    public abstract void setRemoveAction(ActionBean bean);

    @Persist
    public abstract Collection<ActionBean> getActions();

    public abstract void setActions(Collection<ActionBean> actions);

    public abstract void setAlias(String alias);
    public abstract void setDid(String did);
    public abstract String getAlias();
    public abstract String getDid();

    @Override
    public void pageBeginRender(PageEvent event) {
        super.pageEndRender(event);
        List<FreeswitchAction> actions = null;

        if (getOpenAcdLineId() == null) {
            actions = OpenAcdLine.getDefaultActions(getSipxLocation());
        } else {
            OpenAcdLine line = (OpenAcdLine) getOpenAcdContext().getExtensionById(getOpenAcdLineId());
            actions = line.getLineActions();
            setName(line.getName());
            setDescription(line.getDescription());
            setLineNumber(line.getNumberCondition().getExtension());
            setAlias(line.getAlias());
            setDid(line.getDid());
        }

        List<ActionBean> actionBeans = new LinkedList<ActionBean>();
        for (FreeswitchAction action : actions) {
            String application = action.getApplication();
            String data = action.getData();
            if (StringUtils.equals(application, FreeswitchAction.PredefinedAction.answer.toString())) {
                setAnswerSupervision(true);
            } else if (StringUtils.contains(data, OpenAcdLine.Q)) {
                if (getSelectedQueue() == null) {
                    String queueName = StringUtils.removeStart(data, OpenAcdLine.Q);
                    OpenAcdQueue queue = getOpenAcdContext().getQueueByName(queueName);
                    setSelectedQueue(queue);
                }
            } else if (StringUtils.contains(data, OpenAcdLine.BRAND)) {
                if (getSelectedClient() == null) {
                    String clientIdentity = StringUtils.removeStart(data, OpenAcdLine.BRAND);
                    OpenAcdClient client = getOpenAcdContext().getClientByIdentity(clientIdentity);
                    setSelectedClient(client);
                }
            } else if (StringUtils.contains(data, OpenAcdLine.ALLOW_VOICEMAIL)) {
                setAllowVoicemail(BooleanUtils.toBoolean(StringUtils.removeStart(data, OpenAcdLine.ALLOW_VOICEMAIL)));
            } else if (StringUtils.equals(application, FreeswitchAction.PredefinedAction.playback.toString())) {
                if (getWelcomeMessage() == null) {
                    setWelcomeMessage(StringUtils.removeStart(data, getSipxOpenAcdService().getAudioDir() + SLASH));
                }
            } else {
                actionBeans.add(new ActionBean(action));
            }
        }
        if (getActions() == null) {
            setActions(actionBeans);
        }
        if (getRemoveAction() != null) {
            getActions().remove(getRemoveAction());
        }
    }

    public void addAction() {
        FreeswitchAction action = new FreeswitchAction();
        ActionBean bean = new ActionBean(action);
        getActions().add(bean);
    }

    public String[] getOpenAcdApplicationNames() {
        String filter = getFilter();

        if (filter == null || filter.length() < 1) {
            return getOpenAcdContext().getOpenAcdApplicationNames();
        }
        List<String> temp = new ArrayList<String>();
        for (String app : getOpenAcdContext().getOpenAcdApplicationNames()) {
            if (app.startsWith(filter)) {
                temp.add(app);
            }
        }
        return temp.toArray(new String[0]);
    }

    public void filterList(String filter) {
        setFilter(filter);
    }

    public void saveLine() {
        // save the line and reload
        if (TapestryUtils.isValid(this)) {
            OpenAcdLine line = null;
            if (getOpenAcdLineId() != null) {
                line = (OpenAcdLine) getOpenAcdContext().getExtensionById(getOpenAcdLineId());
            } else {
                line = getOpenAcdContext().newOpenAcdLine();
                line.addCondition(OpenAcdLine.createLineCondition());
            }

            line.setName(getName());
            line.setDescription(getDescription());
            line.setLocation(getSipxLocation());
            line.setAlias(getAlias());
            line.setDid(getDid());

            // add common actions
            line.getNumberCondition().getActions().clear();
            line.getNumberCondition().addAction(OpenAcdLine.createAnswerAction(isAnswerSupervision()));
            line.getNumberCondition().addAction(OpenAcdLine.createVoicemailAction(isAllowVoicemail()));
            if (getSelectedQueue() == null) {
                throw new UserException(getMessages().getMessage("error.requiredQueue"));
            } else {
                line.getNumberCondition().addAction(OpenAcdLine.createQueueAction(getSelectedQueue().getName()));
            }
            if (getSelectedClient() == null) {
                throw new UserException(getMessages().getMessage("error.requiredClient"));
            } else {
                line.getNumberCondition().addAction(
                        OpenAcdLine.createClientAction(getSelectedClient().getIdentity()));
            }
            if (StringUtils.isNotEmpty(getWelcomeMessage())) {
                line.getNumberCondition().addAction(
                        OpenAcdLine.createPlaybackAction(getSipxOpenAcdService().getAudioDir() + SLASH
                                + getWelcomeMessage()));
            }

            for (ActionBean actionBean : getActions()) {
                line.getNumberCondition().addAction((FreeswitchAction) actionBean.getAction().duplicate());
            }

            line.getNumberCondition().setExpression(
                    String.format(OpenAcdLine.DESTINATION_NUMBER_PATTERN, getLineNumber()));
            getOpenAcdContext().saveExtension(line);
            setOpenAcdLineId(line.getId());
            setActions(null);
            setWelcomeMessage(null);
        }
    }

}
