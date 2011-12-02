/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */
package org.sipfoundry.sipxconfig.admin.commserver;

import java.io.Serializable;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sipfoundry.sipxconfig.admin.commserver.imdb.DataSet;
import org.sipfoundry.sipxconfig.admin.commserver.imdb.ReplicationManager;
import org.sipfoundry.sipxconfig.admin.forwarding.CallSequence;
import org.sipfoundry.sipxconfig.common.Replicable;
import org.sipfoundry.sipxconfig.job.JobContext;
import org.sipfoundry.sipxconfig.service.ServiceConfigurator;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

public abstract class SipxReplicationContextImpl implements ApplicationEventPublisherAware, SipxReplicationContext {
    private static final Log LOG = LogFactory.getLog(SipxReplicationContextImpl.class);
    private ApplicationEventPublisher m_applicationEventPublisher;
    private ReplicationManager m_replicationManager;
    private JobContext m_jobContext;
    private LocationsManager m_locationsManager;

    protected abstract ServiceConfigurator getServiceConfigurator();

    @Override
    public void generate(final Replicable entity) {
        m_replicationManager.replicateEntity(entity);
    }

    @Override
    public void generateAll() {
        ReplicateWork work = new ReplicateWork() {
            @Override
            public void replicate() {
                m_replicationManager.replicateAllData();
            }

        };
        doWithJob(IMDB_REGENERATION, m_locationsManager.getPrimaryLocation(), work);
    }

    @Override
    public void generateAll(DataSet ds) {
        m_replicationManager.replicateAllData(ds);
    }

    @Override
    public void replicateLocation(final Location location) {
        ReplicateWork work = new ReplicateWork() {
            @Override
            public void replicate() {
                m_replicationManager.replicateLocation(location);
            }
        };
        doWithJob(SipxReplicationContext.MONGO_LOCATION_REGISTRATION, m_locationsManager.getPrimaryLocation(), work);
    }

    @Override
    public void remove(final Replicable entity) {
        m_replicationManager.removeEntity(entity);
    }

    @Override
    public void resyncSlave(Location location) {
        m_replicationManager.resyncSlave(location);
    }

    public void regenerateCallSequences(final Collection<CallSequence> callSequences) {
        ReplicateWork work = new ReplicateWork() {
            @Override
            public void replicate() {
                for (CallSequence callSequence : callSequences) {
                    m_replicationManager.replicateEntity(callSequence);
                }
            }
        };
        doWithJob("DST change: regeneration of call sequences.",
                m_locationsManager.getPrimaryLocation(), work);
    }

    private void doWithJob(final String jobName, final Location location, final ReplicateWork work) {
        Serializable jobId = m_jobContext.schedule(jobName, location);
        try {
            LOG.info("Start replication: " + jobName);
            m_jobContext.start(jobId);
            work.replicate();
            m_jobContext.success(jobId);
        } catch (RuntimeException e) {
            LOG.warn("Replication failed: " + jobName, e);
            // there is not really a good info here - advise user to consult log?
            m_jobContext.failure(jobId, null, null);
        }
    }

    interface ReplicateWork {
        void replicate();
    }

    @Required
    public void setReplicationManager(ReplicationManager replicationManager) {
        m_replicationManager = replicationManager;
    }

    @Required
    public void setJobContext(JobContext jobContext) {
        m_jobContext = jobContext;
    }

    @Required
    public void setLocationsManager(LocationsManager locationsManager) {
        m_locationsManager = locationsManager;
    }

    @Override
    @Required
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        m_applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publishEvent(ApplicationEvent event) {
        m_applicationEventPublisher.publishEvent(event);
    }

}
