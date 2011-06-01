/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.admin.callgroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.sipfoundry.sipxconfig.acd.AcdContext;
import org.sipfoundry.sipxconfig.acd.AcdQueue;
import org.sipfoundry.sipxconfig.admin.ExtensionInUseException;
import org.sipfoundry.sipxconfig.admin.NameInUseException;
import org.sipfoundry.sipxconfig.admin.commserver.SipxReplicationContext;
import org.sipfoundry.sipxconfig.admin.commserver.imdb.AliasMapping;
import org.sipfoundry.sipxconfig.alias.AliasManager;
import org.sipfoundry.sipxconfig.common.BeanId;
import org.sipfoundry.sipxconfig.common.CoreContext;
import org.sipfoundry.sipxconfig.common.Replicable;
import org.sipfoundry.sipxconfig.common.ReplicableProvider;
import org.sipfoundry.sipxconfig.common.SipxCollectionUtils;
import org.sipfoundry.sipxconfig.common.SipxHibernateDaoSupport;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.common.event.UserDeleteListener;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 * Hibernate implementation of the call group context
 */
public class CallGroupContextImpl extends SipxHibernateDaoSupport implements CallGroupContext, ReplicableProvider {
    private static final String VALUE = "value";

    private static final String QUERY_CALL_GROUP_IDS_WITH_NAME = "callGroupIdsWithName";
    private static final String QUERY_CALL_GROUP_IDS_WITH_ALIAS = "callGroupIdsWithAlias";

    private CoreContext m_coreContext;
    private SipxReplicationContext m_replicationContext;
    private AliasManager m_aliasManager;
    private AcdContext m_acdContext;

    // trivial setters
    public void setCoreContext(CoreContext coreContext) {
        m_coreContext = coreContext;
    }

    public void setReplicationContext(SipxReplicationContext replicationContext) {
        m_replicationContext = replicationContext;
    }

    public void setAliasManager(AliasManager aliasManager) {
        m_aliasManager = aliasManager;
    }

    public void setAcdContext(AcdContext acdContext) {
        m_acdContext = acdContext;
    }

    @Override
    public CallGroup loadCallGroup(Integer id) {
        return getHibernateTemplate().load(CallGroup.class, id);
    }

    @Override
    public void storeCallGroup(CallGroup callGroup) {
        // Check for duplicate names or extensions before saving the call group
        String name = callGroup.getName();
        String extension = callGroup.getExtension();
        final String huntGroupTypeName = "hunt group";
        if (!m_aliasManager.canObjectUseAlias(callGroup, name)) {
            throw new NameInUseException(huntGroupTypeName, name);
        }
        if (!m_aliasManager.canObjectUseAlias(callGroup, extension)) {
            throw new ExtensionInUseException(huntGroupTypeName, extension);
        }
        if (!m_aliasManager.canObjectUseAlias(callGroup, callGroup.getDid())) {
            throw new ExtensionInUseException(huntGroupTypeName, callGroup.getDid());
        }
        getHibernateTemplate().saveOrUpdate(callGroup);
        // activate call groups every time the call group is saved
        m_replicationContext.generate(callGroup);
    }

    @Override
    public void removeCallGroups(Collection<Integer> ids) {
        if (ids.isEmpty()) {
            return;
        }
        Collection<CallGroup> cgs = new ArrayList<CallGroup>();
        for (Integer id : ids) {
            CallGroup cg = (CallGroup) load(CallGroup.class, id);
            cgs.add(cg);
        }
        m_acdContext.removeOverflowSettings(ids, AcdQueue.HUNTGROUP_TYPE);
        removeAll(CallGroup.class, ids);

        // activate call groups every time the call group is removed
        deactivateCallGroups(cgs);
    }

    public UserDeleteListener createUserDeleteListener() {
        return new OnUserDelete();
    }

    private class OnUserDelete extends UserDeleteListener {
        @Override
        protected void onUserDelete(User user) {
            getHibernateTemplate().update(user);
            removeUser(user.getId());
        }
    }

    /**
     * Removes all rings associated with a removed user. This is temporary. In the long term we
     * will introduce hibernate dependency between the users and rings table.
     *
     * Note: we cannot just blindly removed rings from database, there is a cascade relationship
     * between call groups and rings, hibernate will resave the ring if it's removed from database
     * but not from the call group
     *
     * This function is called from legacy notification service, there is no need to activate call
     * groups - they will be activated anyway because alias generation follows user deletion.
     *
     * @param userId id of the user that us being deleted
     *
     */
    @Override
    public void removeUser(Integer userId) {
        final HibernateTemplate hibernate = getHibernateTemplate();
        Collection rings = hibernate.findByNamedQueryAndNamedParam("userRingsForUserId", "userId", userId);
        for (Iterator i = rings.iterator(); i.hasNext();) {
            UserRing ring = (UserRing) i.next();
            CallGroup callGroup = ring.getCallGroup();
            callGroup.removeRing(ring);
            hibernate.save(callGroup);
        }
    }

    @Override
    public List<CallGroup> getCallGroups() {
        return getHibernateTemplate().loadAll(CallGroup.class);
    }

    @Override
    public void duplicateCallGroups(Collection ids) {
        for (Iterator i = ids.iterator(); i.hasNext();) {
            CallGroup group = loadCallGroup((Integer) i.next());

            // Create a copy of the call group with a unique name
            CallGroup groupDup = (CallGroup) duplicateBean(group, QUERY_CALL_GROUP_IDS_WITH_NAME);

            // Extensions should be unique, so don't copy the extension from the
            // source call group. The admin must fill it in explicitly.
            groupDup.setExtension(null);
            groupDup.setDid(null);

            groupDup.setEnabled(false);
            storeCallGroup(groupDup);
        }
    }

    /**
     * Remove all call groups - mostly used for testing
     */
    @Override
    public void clear() {
        HibernateTemplate template = getHibernateTemplate();
        Collection<CallGroup> callGroups = template.loadAll(CallGroup.class);
        template.deleteAll(callGroups);
        for (CallGroup cg : callGroups) {
            m_replicationContext.generate(cg);
        }
    }

    /**
     * Sends notification to profile generator to trigger alias generation
     */
    public void deactivateCallGroups(Collection<CallGroup> cgs) {
        for (CallGroup cg : cgs) {
            m_replicationContext.remove(cg);
        }
    }

    /**
     * Generate aliases for all call groups
     */
    @Override
    public Collection<AliasMapping> getAliasMappings() {
        Collection<AliasMapping> aliases = new ArrayList<AliasMapping>();
        Collection<CallGroup> callGroups = getCallGroups();
        for (CallGroup callGroup : callGroups) {
            aliases.addAll(callGroup.getAliasMappings(m_coreContext.getDomainName()));
        }

        return aliases;
    }

    @Override
    public boolean isAliasInUse(String alias) {
        // Look for the ID of a call group with the specified alias as its name or extension.
        // If there is one, then the alias is in use.
        List objs = getHibernateTemplate().findByNamedQueryAndNamedParam(QUERY_CALL_GROUP_IDS_WITH_ALIAS, VALUE,
                alias);
        return SipxCollectionUtils.safeSize(objs) > 0;
    }

    @Override
    public Collection getBeanIdsOfObjectsWithAlias(String alias) {
        List ids = getHibernateTemplate().findByNamedQueryAndNamedParam(QUERY_CALL_GROUP_IDS_WITH_ALIAS, VALUE,
                alias);
        Collection bids = BeanId.createBeanIdCollection(ids, CallGroup.class);
        return bids;
    }

    @Override
    public void addUsersToCallGroup(Integer callGroupId, Collection ids) {
        CallGroup callGroup = loadCallGroup(callGroupId);
        for (Iterator i = ids.iterator(); i.hasNext();) {
            Integer userId = (Integer) i.next();
            User user = m_coreContext.loadUser(userId);
            callGroup.insertRingForUser(user);
        }
        storeCallGroup(callGroup);
    }

    @Override
    public void generateSipPasswords() {
        List<CallGroup> callGroups = getCallGroups();
        List<CallGroup> changed = new ArrayList<CallGroup>();
        for (CallGroup callGroup : callGroups) {
            if (callGroup.generateSipPassword()) {
                changed.add(callGroup);
            }
        }
        // no need to trigger replication - do not use storeCallGroup
        getHibernateTemplate().saveOrUpdateAll(changed);
    }

    @Override
    public List<Replicable> getReplicables() {
        List<Replicable> replicables = new ArrayList<Replicable>();
        for (CallGroup cg : getCallGroups()) {
            replicables.add(cg);
        }
        return replicables;
    }
}
