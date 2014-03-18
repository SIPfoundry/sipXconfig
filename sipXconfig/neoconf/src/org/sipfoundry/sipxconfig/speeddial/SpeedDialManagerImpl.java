/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.speeddial;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.sipfoundry.commons.mongo.MongoConstants;
import org.sipfoundry.commons.userdb.ValidUsers;
import org.sipfoundry.sipxconfig.alias.AliasManager;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigManager;
import org.sipfoundry.sipxconfig.common.CoreContext;
import org.sipfoundry.sipxconfig.common.SipxHibernateDaoSupport;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.common.UserException;
import org.sipfoundry.sipxconfig.common.UserValidationUtils;
import org.sipfoundry.sipxconfig.feature.FeatureManager;
import org.sipfoundry.sipxconfig.rls.Rls;
import org.sipfoundry.sipxconfig.setting.Group;
import org.springframework.beans.factory.annotation.Required;

public class SpeedDialManagerImpl extends SipxHibernateDaoSupport<SpeedDial> implements SpeedDialManager {
    private static final int MAX_BUTTONS = 136;
    private CoreContext m_coreContext;
    private FeatureManager m_featureManager;
    private ConfigManager m_configManager;
    private ValidUsers m_validUsers;

    private AliasManager m_aliasManager;
    private String m_feature = "rls";

    @Override
    public SpeedDial getSpeedDialForUserId(Integer userId, boolean create) {
        List<SpeedDial> speeddials = findSpeedDialForUserId(userId);
        if (!speeddials.isEmpty()) {
            return speeddials.get(0);
        }

        User user = m_coreContext.loadUser(userId);
        return getGroupSpeedDialForUser(user, create);
    }

    @Override
    public SpeedDial getSpeedDialForUser(User user, boolean create) {
        List<SpeedDial> speeddials = findSpeedDialForUserId(user.getId());
        if (!speeddials.isEmpty()) {
            return speeddials.get(0);
        }

        return getGroupSpeedDialForUser(user, create);
    }

    @Override
    public SpeedDial getGroupSpeedDialForUser(User user, boolean create) {
        Set<Group> groups = user.getGroups();
        if (groups.isEmpty() && !create) {
            return null;
        }
        List<SpeedDialGroup> speeddialGroups = new ArrayList<SpeedDialGroup>();
        for (Group group : groups) {
            speeddialGroups.addAll(findSpeedDialForGroupId(group.getId()));
        }
        if (!speeddialGroups.isEmpty()) {
            /*
             * If there are more than 1 group, choose the last group on the list that has a
             * non-zero number of speeddial buttons defined.
             */
            for (int i = speeddialGroups.size() - 1; i >= 0; i--) {
                if (0 < speeddialGroups.get(i).getButtons().size()) {
                    return speeddialGroups.get(i).getSpeedDial(user);
                }
            }
        }

        if (!create) {
            return null;
        }
        SpeedDial speedDial = new SpeedDial();
        speedDial.setUser(m_coreContext.loadUser(user.getId()));
        return speedDial;
    }

    @Override
    public SpeedDialGroup getSpeedDialForGroupId(Integer groupId) {
        List<SpeedDialGroup> speedDialGroups = findSpeedDialForGroupId(groupId);
        if (!speedDialGroups.isEmpty()) {
            return speedDialGroups.get(0);
        }

        SpeedDialGroup speedDialGroup = new SpeedDialGroup();
        Group userGroup = m_coreContext.getGroupById(groupId);
        speedDialGroup.setUserGroup(userGroup);
        return speedDialGroup;
    }

    @Override
    public boolean isSpeedDialDefinedForUserId(Integer userId) {
        List<SpeedDial> speeddials = findSpeedDialForUserId(userId);
        if (!speeddials.isEmpty()) {
            return true;
        }
        return false;
    }

    @Override
    public List<SpeedDial> findSpeedDialForUserId(Integer userId) {
        List<SpeedDial> speeddials = getHibernateTemplate().findByNamedQueryAndNamedParam("speedDialForUserId",
                "userId", userId);
        return speeddials;
    }

    private List<SpeedDialGroup> findSpeedDialForGroupId(Integer groupId) {
        List<SpeedDialGroup> speeddialGroups = getHibernateTemplate().findByNamedQueryAndNamedParam(
                "speedDialForGroupId", "userGroupId", groupId);
        return speeddialGroups;
    }

    @Override
    public void saveSpeedDial(SpeedDial speedDial) {
        verifyBlfs(speedDial.getButtons());
        getHibernateTemplate().saveOrUpdate(speedDial);
        getHibernateTemplate().flush();
        User user = m_coreContext.loadUser(speedDial.getUser().getId());
        getDaoEventPublisher().publishSave(user);
    }

    private void verifyBlfs(List<Button> buttons) {
        int blfCount = 0;
        for (Button button : buttons) {
            if (button.isBlf()) {
                blfCount++;
                String number = button.getNumber();
                if (!UserValidationUtils.isValidEmail(number) && !m_aliasManager.isAliasInUse(number)) {
                    button.setBlf(false);
                    throw new UserException("&error.notValidBlf", number);
                }
            }
        }
        if (blfCount > MAX_BUTTONS) {
            throw new UserException("&error.blfExceedsMaxNumber", MAX_BUTTONS);
        }
    }

    /**
     * This method starts with "save" because we want to trigger speed dial replication (see
     * ReplicationTrigger.java)
     */
    @Override
    public void speedDialSynchToGroup(SpeedDial speedDial) {
        User user = m_coreContext.loadUser(speedDial.getUser().getId());
        deleteSpeedDialsForUser(speedDial.getUser().getId());
        getHibernateTemplate().flush();
        getDaoEventPublisher().publishSave(user);
    }

    @Override
    public void saveSpeedDialGroup(SpeedDialGroup speedDialGroup) {
        verifyBlfs(speedDialGroup.getButtons());
        getHibernateTemplate().saveOrUpdate(speedDialGroup);
        getDaoEventPublisher().publishSave(speedDialGroup.getUserGroup());
    }

    @Override
    public void deleteSpeedDialsForGroup(int groupId) {
        List<SpeedDialGroup> groups = findSpeedDialForGroupId(groupId);
        getDaoEventPublisher().publishDeleteCollection(groups);
        getHibernateTemplate().deleteAll(groups);
    }

    @Override
    public void deleteSpeedDialsForUser(int userId) {
        List<SpeedDial> speedDials = findSpeedDialForUserId(userId);
        if (!speedDials.isEmpty()) {
            getDaoEventPublisher().publishDeleteCollection(speedDials);
            getHibernateTemplate().deleteAll(speedDials);
        }
    }

    @Required
    public void setCoreContext(CoreContext coreContext) {
        m_coreContext = coreContext;
    }

    @Required
    public void setFeatureManager(FeatureManager featureManager) {
        m_featureManager = featureManager;
    }

    @Override
    public void clear() {
        removeAll(SpeedDial.class);

        // A little convoluted, but only way i could keep mongo and postgres in sync
        // which is critical for resource-lists.xml.
        m_validUsers.removeFieldFromUsers(MongoConstants.SPEEDDIAL);
        m_configManager.configureEverywhere(Rls.FEATURE);
    }

    @Required
    public void setConfigManager(ConfigManager configManager) {
        m_configManager = configManager;
    }

    public ValidUsers getValidUsers() {
        return m_validUsers;
    }

    public void setValidUsers(ValidUsers validUsers) {
        m_validUsers = validUsers;
    }

    public void setAliasManager(AliasManager aliasMgr) {
        m_aliasManager = aliasMgr;
    }

    public void setFeatureId(String feature) {
        m_feature = feature;
    }
}
