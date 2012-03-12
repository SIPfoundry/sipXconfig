/*
 * Copyright (C) 2011 eZuce Inc., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the AGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.feature;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sipfoundry.sipxconfig.cfgmgt.DeployConfigOnEdit;
import org.sipfoundry.sipxconfig.common.SipxHibernateDaoSupport;
import org.sipfoundry.sipxconfig.common.event.DaoEventListener;
import org.sipfoundry.sipxconfig.common.event.DaoEventPublisher;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

/**
 * NOTE: This implementation pays no attention to efficiency.  Should work in caching or optimize queries
 * accordingly if found to be inefficient during testing.
 */
public class FeatureManagerImpl extends SipxHibernateDaoSupport implements BeanFactoryAware, FeatureManager,
    DaoEventListener {
    private ListableBeanFactory m_beanFactory;
    private Collection<FeatureProvider> m_providers;
    private Collection<FeatureListener> m_listeners;
    private JdbcTemplate m_jdbcTemplate;
    private DaoEventPublisher m_daoEventPublisher;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        m_beanFactory = (ListableBeanFactory) beanFactory;
    }

    Collection<FeatureProvider> getFeatureProviders() {
        if (m_providers == null) {
            Map<String, FeatureProvider> beanMap = safeGetListableBeanFactory().getBeansOfType(
                    FeatureProvider.class, false, false);
            m_providers = new ArrayList<FeatureProvider>(beanMap.values());
        }

        return m_providers;
    }

    private ListableBeanFactory safeGetListableBeanFactory() {
        if (m_beanFactory == null) {
            throw new BeanInitializationException(getClass().getName() + " not initialized");
        }
        return m_beanFactory;
    }

    Collection<FeatureListener> getFeatureListeners() {
        if (m_listeners == null) {
            Map<String, FeatureListener> beanMap = safeGetListableBeanFactory().getBeansOfType(
                    FeatureListener.class, false, false);
            m_listeners = new ArrayList<FeatureListener>(beanMap.values());
        }

        return m_listeners;
    }

    @Override
    public Set<GlobalFeature> getAvailableGlobalFeatures() {
        Set<GlobalFeature> features = new HashSet<GlobalFeature>();
        for (FeatureProvider p : getFeatureProviders()) {
            Collection<GlobalFeature> gfeatures = p.getAvailableGlobalFeatures();
            if (gfeatures != null) {
                features.addAll(gfeatures);
            }
        }
        return features;
    }

    public void setConfigJdbcTemplate(JdbcTemplate jdbcTemplate) {
        m_jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean isFeatureEnabled(LocationFeature feature, Location location) {
        SqlRowSet queryForRowSet = m_jdbcTemplate.queryForRowSet(
                "select 1 from feature_local where feature_id = ? and location_id = ?", feature.getId(),
                location.getId());
        return queryForRowSet.first();
    }

    @Override
    public boolean isFeatureEnabled(GlobalFeature feature) {
        SqlRowSet queryForRowSet = m_jdbcTemplate.queryForRowSet(
                "select 1 from feature_global where feature_id = ?", feature.getId());
        return queryForRowSet.first();
    }


    @Override
    public boolean isFeatureEnabled(LocationFeature feature) {
        SqlRowSet queryForRowSet = m_jdbcTemplate.queryForRowSet(
                "select 1 from feature_local where feature_id = ?", feature.getId());
        return queryForRowSet.first();
    }

    @Override
    public Set<LocationFeature> getAvailableLocationFeatures(Location location) {
        Set<LocationFeature> features = new HashSet<LocationFeature>();
        for (FeatureProvider p : getFeatureProviders()) {
            Collection<LocationFeature> lfeatures = p.getAvailableLocationFeatures(location);
            if (lfeatures != null) {
                features.addAll(lfeatures);
            }
        }
        return features;
    }

    @Override
    public void enableGlobalFeatures(Set<GlobalFeature> features) {
        DeltaSet<GlobalFeature> delta = new DeltaSet<GlobalFeature>(features, getEnabledGlobalFeatures());
        sendGlobalFeatureEvent(FeatureListener.FeatureEvent.PRE_ENABLE, FeatureListener.FeatureEvent.PRE_DISABLE,
                delta.m_newlyAdded, delta.m_newlyRemoved);
        String remove = "delete from feature_global";
        StringBuilder update = new StringBuilder();
        for (GlobalFeature f : features) {
            if (update.length() == 0) {
                update.append("insert into feature_global values");
            } else {
                update.append(',');
            }
            update.append("('").append(f.getId()).append("')");
        }
        m_jdbcTemplate.batchUpdate(new String[] {
            remove, update.toString()
        });
        sendGlobalFeatureEvent(FeatureListener.FeatureEvent.POST_ENABLE, FeatureListener.FeatureEvent.POST_DISABLE,
                delta.m_newlyAdded, delta.m_newlyRemoved);
        m_daoEventPublisher.publishSave(delta);
    }

    static class DeltaSet<T extends Feature> implements DeployConfigOnEdit {
        private Set<T> m_newlyAdded;
        private Set<T> m_newlyRemoved;
        private Set<Feature> m_allChanges;

        DeltaSet(Set<T> newSet, Set<T> oldSet) {
            m_newlyAdded = new HashSet<T>(newSet);
            m_newlyAdded.removeAll(oldSet);

            m_newlyRemoved = new HashSet<T>(oldSet);
            m_newlyRemoved.removeAll(newSet);

            m_allChanges = new HashSet<Feature>(m_newlyAdded);
            m_allChanges.addAll(m_newlyRemoved);
        }

        @Override
        public Collection<Feature> getAffectedFeaturesOnChange() {
            return m_allChanges;
        }
    }

    void sendLocationFeatureEvent(FeatureListener.FeatureEvent enable, FeatureListener.FeatureEvent disable,
            Set<LocationFeature> enabled, Set<LocationFeature> disabled, Location location) {
        for (FeatureListener listener : getFeatureListeners()) {
            for (LocationFeature feature : enabled) {
                listener.enableLocationFeature(this, enable, feature, location);
            }
            for (LocationFeature feature : disabled) {
                listener.enableLocationFeature(this, disable, feature, location);
            }
        }
    }

    void sendGlobalFeatureEvent(FeatureListener.FeatureEvent enable, FeatureListener.FeatureEvent disable,
            Set<GlobalFeature> enabled, Set<GlobalFeature> disabled) {
        for (FeatureListener listener : getFeatureListeners()) {
            for (GlobalFeature feature : enabled) {
                listener.enableGlobalFeature(this, enable, feature);
            }
            for (GlobalFeature feature : disabled) {
                listener.enableGlobalFeature(this, disable, feature);
            }
        }
    }

    @Override
    public Set<GlobalFeature> getEnabledGlobalFeatures() {
        List<String> queryForList = m_jdbcTemplate.queryForList("select feature_id from feature_global", String.class);
        Set<GlobalFeature> features = new HashSet<GlobalFeature>(queryForList.size());
        for (String id : queryForList) {
            features.add(new GlobalFeature(id));
        }
        return features;
    }

    public Set<LocationFeature> getEnabledLocationFeatures() {
        List<String> queryForList = m_jdbcTemplate.queryForList("select feature_id from feature_local", String.class);
        return locationFeatures(queryForList);
    }

    @Override
    public Set<LocationFeature> getEnabledLocationFeatures(Location location) {
        List<String> queryForList = m_jdbcTemplate.queryForList(
                "select feature_id from feature_local where location_id = ? ", String.class, location.getId());
        return locationFeatures(queryForList);
    }

    Set<LocationFeature> locationFeatures(List<String> ids) {
        Set<LocationFeature> features = new HashSet<LocationFeature>(ids.size());
        for (String id : ids) {
            features.add(new LocationFeature(id));
        }
        return features;
    }

    @Override
    public void enableLocationFeatures(Set<LocationFeature> features, Location location) {

        // Only send sends for changes. newly enabled and newly disabled.
        DeltaSet<LocationFeature> delta = new DeltaSet<LocationFeature>(features, getEnabledLocationFeatures(location));

        sendLocationFeatureEvent(FeatureListener.FeatureEvent.PRE_ENABLE, FeatureListener.FeatureEvent.PRE_DISABLE,
                delta.m_newlyAdded, delta.m_newlyRemoved, location);
        String remove = "delete from feature_local where location_id = " + location.getId();
        StringBuilder update = new StringBuilder();
        for (LocationFeature f : features) {
            if (update.length() == 0) {
                update.append("insert into feature_local values");
            } else {
                update.append(',');
            }
            update.append('(');
            update.append('\'').append(f.getId()).append('\'');
            update.append(',');
            update.append(location.getId());
            update.append(')');
        }
        m_jdbcTemplate.batchUpdate(new String[] {
            remove, update.toString()
        });

        sendLocationFeatureEvent(FeatureListener.FeatureEvent.POST_ENABLE, FeatureListener.FeatureEvent.POST_DISABLE,
                delta.m_newlyAdded, delta.m_newlyRemoved, location);

        m_daoEventPublisher.publishSave(delta);
    }

    public void enableLocationFeature(LocationFeature feature, Location location, boolean enable) {
        Set<LocationFeature> features = getEnabledLocationFeatures(location);
        if (update(features, feature, enable)) {
            enableLocationFeatures(features, location);
        }
    }

    @Override
    public List<Location> getLocationsForEnabledFeature(LocationFeature feature) {
        List<Location> locations = (List<Location>) getHibernateTemplate().findByNamedQueryAndNamedParam(
                "locationsForEnabledFeature", "featureId", feature.getId());
        return locations;
    }

    @Override
    public void enableGlobalFeature(GlobalFeature feature, boolean enable) {
        Set<GlobalFeature> features = getEnabledGlobalFeatures();
        if (update(features, feature, enable)) {
            enableGlobalFeatures(features);
        }
    }

    private <T extends Feature> boolean update(Collection<T> features, T feature, boolean enable) {
        if (features.contains(feature)) {
            if (!enable) {
                features.remove(feature);
            } else {
                return false;
            }
        } else {
            if (enable) {
                features.add(feature);
            } else {
                return false;
            }
        }
        return true;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        m_jdbcTemplate = jdbcTemplate;
    }

    public void setDaoEventPublisher(DaoEventPublisher daoEventPublisher) {
        m_daoEventPublisher = daoEventPublisher;
    }

    @Override
    public void onDelete(Object entity) {
        if (entity instanceof Location) {
            // When deleting a location, treat this as if someone disabled all features at this location.
            Set<LocationFeature> none = Collections.emptySet();
            enableLocationFeatures(none, (Location) entity);
        }
    }

    @Override
    public void onSave(Object entity) {
    }
}
