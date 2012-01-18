/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.search;

import java.io.Serializable;

import org.hibernate.type.Type;
import org.sipfoundry.sipxconfig.common.SpringHibernateInstantiator;

public class IndexingInterceptor extends SpringHibernateInstantiator {
    private Indexer m_indexer;
    private BeanIndexHelper m_beanIndexHelper;

    public void setIndexer(Indexer indexer) {
        m_indexer = indexer;
    }

    public void setBeanIndexHelper(BeanIndexHelper beanIndexHelper) {
        m_beanIndexHelper = beanIndexHelper;
        beanIndexHelper.setSpringInstantiator(this);
    }

    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames,
            Type[] types) {
        BeanIndexProperties beanIndexProperties = new BeanIndexProperties(entity, id, state, propertyNames, types);
        m_beanIndexHelper.setupIndexProperties(beanIndexProperties);
        m_indexer.indexBean(entity, id, beanIndexProperties.getState(), beanIndexProperties.getPropertyNames(),
                beanIndexProperties.getTypes(), true);
        return super.onSave(entity, id, state, propertyNames, types);
    }

    public void onDelete(Object entity, Serializable id, Object[] state_,
            String[] propertyNames_, Type[] types_) {
        m_indexer.removeBean(entity, id);
        super.onDelete(entity, id, state_, propertyNames_, types_);
    }

    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState,
            Object[] previousState_, String[] propertyNames, Type[] types) {
        BeanIndexProperties beanIndexProperties = new BeanIndexProperties(entity, id, currentState,
                propertyNames, types);
        m_beanIndexHelper.setupIndexProperties(beanIndexProperties);
        m_indexer.indexBean(entity, id, beanIndexProperties.getState(), beanIndexProperties.getPropertyNames(),
                beanIndexProperties.getTypes(), false);
        return super.onFlushDirty(entity, id, currentState, previousState_, propertyNames, types);
    }
}
