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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sipfoundry.sipxconfig.admin.commserver.imdb.AliasMapping;
import org.sipfoundry.sipxconfig.admin.commserver.imdb.DataSet;
import org.sipfoundry.sipxconfig.common.Replicable;
import org.sipfoundry.sipxconfig.common.SipUri;
import org.sipfoundry.sipxconfig.freeswitch.FreeswitchAction;
import org.sipfoundry.sipxconfig.freeswitch.FreeswitchCondition;
import org.sipfoundry.sipxconfig.freeswitch.FreeswitchExtension;
import org.sipfoundry.sipxconfig.service.SipxFreeswitchService;
import org.sipfoundry.sipxconfig.service.SipxServiceManager;

public class OpenAcdExtension extends FreeswitchExtension implements Replicable {
    public static final String DESTINATION_NUMBER = "destination_number";
    public static final String DESTINATION_NUMBER_PATTERN = "^%s$";
    public static final String EMPTY_STRING = "";
    static final String ALIAS_RELATION = "openacd";
    private SipxServiceManager m_serviceManager;

    /**
     * We call this condition the (first, because they can be many) condition that has
     * destination_number as a field
     */
    public FreeswitchCondition getNumberCondition() {
        if (getConditions() == null) {
            return null;
        }
        for (FreeswitchCondition condition : getConditions()) {
            if (condition.getField().equals(DESTINATION_NUMBER)) {
                return condition;
            }
        }
        return null;
    }

    public String getExtension() {
        if (getNumberCondition() != null) {
            return getNumberCondition().getExtension();
        }
        return null;
    }

    public List<FreeswitchAction> getLineActions() {
        List<FreeswitchAction> actions = new LinkedList<FreeswitchAction>();
        for (FreeswitchCondition condition : getConditions()) {
            if (condition.getField().equals(DESTINATION_NUMBER)) {
                for (FreeswitchAction action : condition.getActions()) {
                    actions.add(action);
                }
            }
        }
        return actions;
    }

    protected static FreeswitchAction createAction(String application, String data) {
        FreeswitchAction action = new FreeswitchAction();
        action.setApplication(application);
        action.setData(data);
        return action;
    }

    public static FreeswitchCondition createLineCondition() {
        FreeswitchCondition condition = new FreeswitchCondition();
        condition.setField(OpenAcdLine.DESTINATION_NUMBER);
        condition.setExpression(EMPTY_STRING);
        return condition;
    }

    public static FreeswitchAction createAnswerAction(boolean answer) {
        if (!answer) {
            return null;
        }
        return createAction(FreeswitchAction.PredefinedAction.answer.toString(), null);
    }

    @Override
    public Collection<AliasMapping> getAliasMappings(String domainName) {
        List<AliasMapping> mappings = new ArrayList<AliasMapping>();
        SipxFreeswitchService freeswitchService = (SipxFreeswitchService) m_serviceManager
                .getServiceByBeanId(SipxFreeswitchService.BEAN_ID);

        AliasMapping nameMapping = new AliasMapping(getName(), SipUri.format(getExtension(),
                freeswitchService.getAddress(), freeswitchService.getFreeswitchSipPort(), false), ALIAS_RELATION);
        AliasMapping lineMapping = new AliasMapping(getExtension(), SipUri.format(getExtension(),
                freeswitchService.getAddress(), freeswitchService.getFreeswitchSipPort()), ALIAS_RELATION);
        mappings.addAll(Arrays.asList(nameMapping, lineMapping));
        if (getAlias() != null) {
            AliasMapping aliasMapping = new AliasMapping(getAlias(), SipUri.format(getExtension(),
                    freeswitchService.getAddress(), freeswitchService.getFreeswitchSipPort()), ALIAS_RELATION);
            mappings.add(aliasMapping);
        }
        if (getDid() != null) {
            AliasMapping didMapping = new AliasMapping(getDid(), SipUri.format(getExtension(),
                    freeswitchService.getAddress(), freeswitchService.getFreeswitchSipPort()), ALIAS_RELATION);
            mappings.add(didMapping);
        }

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

    @Override
    public boolean isValidUser() {
        return false;
    }

    @Override
    public Map<String, Object> getMongoProperties(String domain) {
        return Collections.EMPTY_MAP;
    }
}
