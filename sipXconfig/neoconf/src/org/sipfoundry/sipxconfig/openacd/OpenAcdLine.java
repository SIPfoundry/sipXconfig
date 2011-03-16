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
package org.sipfoundry.sipxconfig.openacd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.sipfoundry.sipxconfig.admin.commserver.Location;
import org.sipfoundry.sipxconfig.admin.commserver.imdb.AliasMapping;
import org.sipfoundry.sipxconfig.admin.commserver.imdb.DataSet;
import org.sipfoundry.sipxconfig.common.Replicable;
import org.sipfoundry.sipxconfig.common.SipUri;
import org.sipfoundry.sipxconfig.freeswitch.FreeswitchAction;
import org.sipfoundry.sipxconfig.service.SipxFreeswitchService;
import org.sipfoundry.sipxconfig.service.SipxServiceManager;

public class OpenAcdLine extends OpenAcdExtension implements Replicable {
    public static final String Q = "queue=";
    public static final String BRAND = "brand=";
    public static final String DESTINATION_NUMBER = "destination_number";
    public static final String ALLOW_VOICEMAIL = "allow_voicemail=";
    public static final String EMPTY_STRING = "";
    private SipxServiceManager m_serviceManager;

    public static List<FreeswitchAction> getDefaultActions(Location location) {
        List<FreeswitchAction> actions = new LinkedList<FreeswitchAction>();
        actions.add(createAction(FreeswitchAction.PredefinedAction.answer.toString(), null));
        actions.add(createAction(FreeswitchAction.PredefinedAction.set.toString(), "domain_name=$${domain}"));
        actions.add(createAction(FreeswitchAction.PredefinedAction.set.toString(), Q));
        actions.add(createAction(FreeswitchAction.PredefinedAction.set.toString(), "allow_voicemail=true"));
        actions.add(createAction(FreeswitchAction.PredefinedAction.erlang_sendmsg.toString(),
                "freeswitch_media_manager  " + location.getHostname() + "@127.0.0.1 inivr ${uuid}"));
        actions.add(createAction(FreeswitchAction.PredefinedAction.playback.toString(), EMPTY_STRING));
        actions.add(createAction(FreeswitchAction.PredefinedAction.erlang.toString(),
                "freeswitch_media_manager:!  " + location.getHostname() + "@127.0.0.1"));
        return actions;
    }

    public static FreeswitchAction createVoicemailAction(boolean allow) {
        return createAction(FreeswitchAction.PredefinedAction.set.toString(), ALLOW_VOICEMAIL + allow);
    }

    public static FreeswitchAction createQueueAction(String queue) {
        return createAction(FreeswitchAction.PredefinedAction.set.toString(), Q + queue);
    }

    public static FreeswitchAction createClientAction(String client) {
        return createAction(FreeswitchAction.PredefinedAction.set.toString(), BRAND + client);
    }

    public static FreeswitchAction createPlaybackAction(String path) {
        return createAction(FreeswitchAction.PredefinedAction.playback.toString(), path);
    }

    @Override
    public Collection<AliasMapping> getAliasMappings(String domainName) {
        List<AliasMapping> mappings = new ArrayList<AliasMapping>();
        SipxFreeswitchService freeswitchService = (SipxFreeswitchService) m_serviceManager
                .getServiceByBeanId(SipxFreeswitchService.BEAN_ID);

        AliasMapping nameMapping = new AliasMapping(getName(), SipUri.format(
                getExtension(), freeswitchService.getAddress(), false), ALIAS_RELATION);
        mappings.add(nameMapping);
        AliasMapping lineMapping = new AliasMapping(getExtension(),
                SipUri.format(getExtension(), freeswitchService.getAddress(),
                        freeswitchService.getFreeswitchSipPort()), ALIAS_RELATION);
        mappings.add(lineMapping);
        return mappings;
    }

    @Override
    public Set<DataSet> getDataSets() {
        Set<DataSet> ds = new HashSet<DataSet>();
        ds.add(DataSet.ALIAS);
        return ds;
    }

    @Override
    public String getIdentity(String domain) {
        // TODO Auto-generated method stub
        return null;
    }

    public void setSipxServiceManager(SipxServiceManager manager) {
        m_serviceManager = manager;
    }
}
