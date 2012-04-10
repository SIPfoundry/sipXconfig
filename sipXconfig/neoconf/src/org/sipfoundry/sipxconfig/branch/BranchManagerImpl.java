/*
 *
 *
 * Copyright (C) 2009 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 */

package org.sipfoundry.sipxconfig.branch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.sipfoundry.sipxconfig.common.DaoUtils;
import org.sipfoundry.sipxconfig.common.SipxHibernateDaoSupport;
import org.sipfoundry.sipxconfig.common.UserException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.HibernateCallback;

public class BranchManagerImpl extends SipxHibernateDaoSupport<Branch> implements BranchManager {

    private static final String NAME_PROP_NAME = "name";
    private JdbcTemplate m_jdbcTemplate;

    @Override
    public Branch getBranch(Integer branchId) {
        return load(Branch.class, branchId);
    }

    public Branch getBranch(String branchName) {
        return loadBranchByUniqueProperty(NAME_PROP_NAME, branchName);
    }

    public void saveBranch(Branch branch) {
        // Check for duplicate names before saving the branch
        if (branch.isNew() || (!branch.isNew() && isNameChanged(branch))) {
            checkForDuplicateName(branch);
        }
        if (!branch.isNew()) {
            getHibernateTemplate().merge(branch);
        } else {
            getHibernateTemplate().save(branch);
        }
    }

    private boolean isNameChanged(Branch branch) {
        return !getBranch(branch.getId()).getName().equals(branch.getName());
    }

    private void checkForDuplicateName(Branch branch) {
        String branchName = branch.getName();
        Branch existingBranch = getBranch(branchName);
        if (existingBranch != null) {
            throw new UserException("&duplicate.branchName.error", branchName);
        }
    }

    /*
     * (non-Javadoc)
     * Use plain sql for increased efficiency when deleting user groups.
     * A thing to note is that this method breaks the convention established by DaoEventDispatcher,
     * namely publish delete event first, then proceed with the actual delete. It will actually
     * manually delete from DB the group associations and the group and then the delete event is published.
     * In the case of groups now, only ReplicationTrigger will trigger the delete sequence, all other
     * event listeners that listened to group deletes were removed, and control moved in this method.
     * This was the price to pay for increased efficiency in saving large groups.
     */
    public void deleteBranches(Collection<Integer> branchIds) {
        List<String> sqlUpdates = new ArrayList<String>();
        Collection<Branch> branches = new ArrayList<Branch>(branchIds.size());
        for (Integer id : branchIds) {
            Branch branch = getBranch(id);
            branches.add(branch);
            sqlUpdates.add("update users set branch_id=null where branch_id=" + id);
            sqlUpdates.add("update group_storage set branch_id=null where branch_id=" + id);
            sqlUpdates.add("delete from branch where branch_id=" + id);
            getHibernateTemplate().evict(branch);
        }
        if (!sqlUpdates.isEmpty()) {
            m_jdbcTemplate.batchUpdate(sqlUpdates.toArray(new String[sqlUpdates.size()]));
            for (Branch branch : branches) {
                getDaoEventPublisher().publishDelete(branch);
            }
        }
    }

    public List<Branch> getBranches() {
        List<Branch> branches = getHibernateTemplate().loadAll(Branch.class);
        return branches;
    }

    private Branch loadBranchByUniqueProperty(String propName, String propValue) {
        final Criterion expression = Restrictions.eq(propName, propValue);

        HibernateCallback callback = new HibernateCallback() {
            public Object doInHibernate(Session session) {
                Criteria criteria = session.createCriteria(Branch.class).add(expression);
                return criteria.list();
            }
        };

        List branches = getHibernateTemplate().executeFind(callback);
        Branch branch = (Branch) DaoUtils.requireOneOrZero(branches, expression.toString());

        return branch;
    }

    public List<Branch> loadBranchesByPage(final int firstRow, final int pageSize, final String[] orderBy,
            final boolean orderAscending) {
        return loadBeansByPage(Branch.class, null, null, firstRow, pageSize, orderBy, orderAscending);
    }

    @Override
    public void clear() {
        removeAll(Branch.class);
    }

    public void setConfigJdbcTemplate(JdbcTemplate jdbcTemplate) {
        m_jdbcTemplate = jdbcTemplate;
    }
}
