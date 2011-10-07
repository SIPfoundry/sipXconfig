/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */
package org.sipfoundry.sipxconfig.admin.commserver.imdb;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sipfoundry.sipxconfig.admin.commserver.Location;
import org.sipfoundry.sipxconfig.admin.commserver.LocationsManager;
import org.sipfoundry.sipxconfig.admin.commserver.SipxProcessContext;
import org.sipfoundry.sipxconfig.admin.commserver.SipxReplicationContext;
import org.sipfoundry.sipxconfig.admin.commserver.Location.State;
import org.sipfoundry.sipxconfig.admin.logging.AuditLogContext;
import org.sipfoundry.sipxconfig.branch.Branch;
import org.sipfoundry.sipxconfig.common.ApplicationInitializedEvent;
import org.sipfoundry.sipxconfig.common.Replicable;
import org.sipfoundry.sipxconfig.common.ReplicationsFinishedEvent;
import org.sipfoundry.sipxconfig.common.SipxHibernateDaoSupport;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.common.event.DaoEventListener;
import org.sipfoundry.sipxconfig.gateway.Gateway;
import org.sipfoundry.sipxconfig.openacd.OpenAcdContext;
import org.sipfoundry.sipxconfig.openacd.OpenAcdExtension;
import org.sipfoundry.sipxconfig.permission.Permission;
import org.sipfoundry.sipxconfig.service.ConfigFileActivationManager;
import org.sipfoundry.sipxconfig.service.ServiceConfigurator;
import org.sipfoundry.sipxconfig.service.SipxService;
import org.sipfoundry.sipxconfig.service.SipxServiceManager;
import org.sipfoundry.sipxconfig.setting.Group;
import org.sipfoundry.sipxconfig.speeddial.SpeedDial;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * This class aggregates some of the conditions that will trigger a replication. Initially it was meant to
 * aggregate conditions for Mongo replication but others were added.
 * The main methods are the event listeners methods onSave() and onDelete().
 * See {@link DaoEventDispatcher} to understand sipXecs event system, namely the order in which events
 * are triggered and methods processed.
 * The replication methods will throw a {@link UserException} which will veto the save/delete operation.
 * Stuff other than the delegation of replication to managers should be kept to a bare minimum.
 */
public class ReplicationTrigger extends SipxHibernateDaoSupport implements ApplicationListener, DaoEventListener {
    protected static final Log LOG = LogFactory.getLog(ReplicationTrigger.class);
    private static final String OPENFIRE_SERVICE_BEANID = "sipxOpenfireService";
    private static final String USER_GROUP_RESOURCE = "user";

    private ReplicationManager m_replicationManager;
    private OpenAcdContext m_openAcdContext;
    private LocationsManager m_locationsManager;
    private AuditLogContext m_auditLogContext;
    private SipxReplicationContext m_lazySipxReplicationContext;
    private SipxReplicationContext m_eagerSipxReplicationContext;
    private ConfigFileActivationManager m_configFileManager;
    private SipxServiceManager m_serviceManager;
    private ServiceConfigurator m_serviceConfigurator;
    private SipxProcessContext m_sipxProcessContext;
    private ExecutorService m_executorService;

    /** no replication at start-up by default */
    private boolean m_replicateOnStartup;

    public boolean isReplicateOnStartup() {
        return m_replicateOnStartup;
    }

    public void setReplicateOnStartup(boolean replicateOnStartup) {
        m_replicateOnStartup = replicateOnStartup;
    }

    @Required
    public void setLocationsManager(LocationsManager locationsManager) {
        m_locationsManager = locationsManager;
    }

    @Required
    public void setAuditLogContext(AuditLogContext auditLogContext) {
        m_auditLogContext = auditLogContext;
    }

    @Override
    public void onSave(final Object entity) {
        if (entity instanceof Replicable) {
            m_replicationManager.replicateEntity((Replicable) entity);
            if (entity instanceof OpenAcdExtension) {
                //unfortunately we need to flush here, otherwise we get inconsistent data
                //in sipX_context.xml
                getHibernateTemplate().flush();
                m_openAcdContext.replicateConfig();
            }
        } else if (entity instanceof Group) {
            //flush is necessary here in order to get consistent data
            getHibernateTemplate().flush();
            //It is important to replicate asynch since large groups might take a while to replicate
            //and we want to return control to the page immadiately.
            replicateEntityGroup(new GroupWorker(entity));
        } else if (entity instanceof Branch) {
            getHibernateTemplate().flush();
            //there is no file replication needed so we can trigger the branch replication directly
            replicateEntityGroup(new BranchWorker(entity));
        } else if (entity instanceof Location) {
            //use eager replication context rather than ReplicationManager directly.
            //It uses Job to present info in Job status page
            m_eagerSipxReplicationContext.replicateLocation((Location) entity);
        } else if (entity instanceof Permission) {
            generatePermission((Permission) entity);
        } else if (entity instanceof SpeedDial) {
            User u = ((SpeedDial) entity).getUser();
            m_replicationManager.replicateEntity(u, DataSet.SPEED_DIAL);
        }
    }

    @Override
    public void onDelete(final Object entity) {
        if (entity instanceof Replicable) {
            m_replicationManager.removeEntity((Replicable) entity);
        } else if (entity instanceof Group) {
            //It is important to replicate asynch since large groups might take a while to replicate
            //and we want to return control to the page immadiately.
            replicateEntityGroup(new GroupDeleteWorker(entity));
        } else if (entity instanceof Branch) {
            replicateEntityGroup(new BranchDeleteWorker(entity));
        } else if (entity instanceof Location) {
            m_replicationManager.removeLocation((Location) entity);
        } else if (entity instanceof ArrayList< ? >) {
            ArrayList< ? > col = (ArrayList< ? >) entity;
            if (col.get(0) instanceof Gateway) {
                for (Object object : col) {
                    Gateway gw = (Gateway) object;
                    m_replicationManager.removeEntity(gw);
                }
            }
        } else if (entity instanceof Permission) {
            removePermission((Permission) entity);
        }
    }

    /*
     * Runnables that call the actual replication to be submited to the ExecutorService
     */
    private class BranchWorker implements Runnable {
        private Object m_entity;
        public BranchWorker(Object entity) {
            m_entity = entity;
        }
        @Override
        public void run() {
            m_replicationManager.replicateBranch((Branch) m_entity);
        }
    }

    //public for use in tests
    public class BranchDeleteWorker implements Runnable {
        private Object m_entity;
        public BranchDeleteWorker(Object entity) {
            m_entity = entity;
        }
        @Override
        public void run() {
            m_replicationManager.deleteBranch((Branch) m_entity);
        }
    }

    private class GroupWorker implements Runnable {
        private Object m_entity;
        public GroupWorker(Object entity) {
            m_entity = entity;
        }
        @Override
        public void run() {
            generateGroup((Group) m_entity);
        }
    }

    private class GroupDeleteWorker implements Runnable {
        private Object m_entity;
        public GroupDeleteWorker(Object entity) {
            m_entity = entity;
        }
        @Override
        public void run() {
            deleteGroup((Group) m_entity);
        }
    }

    //ensure async replication of groups of entities
    //can be used for groups, as well as branches (don't let the name fool you)
    private void replicateEntityGroup(Runnable worker) {
        if (m_executorService == null) {
            m_executorService = Executors.newSingleThreadExecutor(Executors.defaultThreadFactory());
        }
        m_executorService.submit(worker);
        m_executorService.shutdown();
        m_executorService = null;
    }

    /**
     * Sequence of replication actions that need to be performed when a group is saved.
     * Order of the sequence is important - files must be replicated after group members.
     * @param group
     */
    private void generateGroup(Group group) {
        if (USER_GROUP_RESOURCE.equals(group.getResource())) {
            m_replicationManager.replicateGroup(group);
            activateGroup();
        }
    }

    /**
     * Sequence of replication actions that need to be performed when a group is deleted.
     * Order of the sequence is important - files must be replicated after group members.
     * @param group
     */
    private void deleteGroup(Group group) {
        if (USER_GROUP_RESOURCE.equals(group.getResource())) {
            m_replicationManager.deleteGroup(group);
            activateGroup();
        }
    }

    /**
     * Helper method to replicate files when group is saved/removed.
     */
    private void activateGroup() {
        m_configFileManager.activateConfigFiles();
        //We need to replicate 2 OF configs (moved control from plugin to config to control the order)
        //We can do that only by replicating the service. We unmark the service for restart as none of
        //the config that changes requires restart
        if (m_serviceManager.isServiceInstalled(OPENFIRE_SERVICE_BEANID)) {
            SipxService instantMessagingService = m_serviceManager.getServiceByBeanId(OPENFIRE_SERVICE_BEANID);
            m_serviceConfigurator.replicateServiceConfig(instantMessagingService);
            m_sipxProcessContext.unmarkServicesToRestart(m_sipxProcessContext.getRestartNeededServices());
        }
    }

    private void generatePermission(Permission permission) {
        Object originalDefaultValue = getOriginalValue(permission, "defaultValue");
        if (originalDefaultValue == null) {
            if (!permission.getDefaultValue()) {
                return;
            } else {
                // We do not need lazy/async here. The operation uses mongo commands and does not
                // hit PG db.
                // It will take a matter of seconds and the control is taken safely to the page.
                // (i.e. we do not need to worry about timeout.)
                m_replicationManager.addPermission(permission);
                return;
            }
        }

        if ((Boolean) originalDefaultValue == permission.getDefaultValue()) {
            return;
        }
        m_lazySipxReplicationContext.generateAll(DataSet.PERMISSION);
    }

    private void removePermission(Permission permission) {
        // We do not need lazy/async here. The operation uses mongo commands and does not hit PG
        // db. It will take a matter of seconds and the control is taken safely to the page.
        // (i.e. we do not need to worry about timeout.)
        m_replicationManager.removePermission(permission);
    }

    /**
     * Override ApplicationListener.onApplicationEvent so we can handle events.
     */
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationInitializedEvent && isReplicateOnStartup()) {
            LOG.info("Replicating all data sets after application has initialized");
            m_lazySipxReplicationContext.generateAll();
        } else if (event instanceof ReplicationsFinishedEvent) {
            updateLocations();
        }
    }
    //TODO: This has to be moved in some manager
    private void updateLocations() {
        Location[] locations = m_locationsManager.getLocations();
        for (Location location : locations) {
            // location is updated when SendProfiles finished execution and also
            // if/when any files get replicated in other scenarios based on
            // AuditLogContext worker's reports
            // such as when firstRun task is executed or occasional replications
            // take place when system is up and running
            Set<String> failedList = m_auditLogContext.getReplicationFailedList(location.getFqdn());
            if (failedList != null && !failedList.isEmpty()) {
                // when something failed, we have configuration error
                Set<String> prevFailedList = location.getFailedReplications();
                prevFailedList.addAll(failedList);
                location.setLastAttempt(new Timestamp(Calendar.getInstance().getTimeInMillis()));
                location.setState(State.CONFIGURATION_ERROR);
                location.setFailedReplications(prevFailedList);
                // location is configured only when sendProfiles successfully
                // finished and nothing failed
            } else if (!m_auditLogContext.isSendProfilesInProgress(location)) {
                location.setLastAttempt(new Timestamp(Calendar.getInstance().getTimeInMillis()));
                location.setState(State.CONFIGURED);
                location.setFailedReplications(new TreeSet<String>());
            } else {
                // in this case means that nothing failed at this point and we are
                // in send profiles progress...
                // we don't have to do anything because we have to wait until send
                // profiles finishes
                continue;
            }
            getHibernateTemplate().update(location);
        }
    }

    public void setReplicationManager(ReplicationManager replicationManager) {
        m_replicationManager = replicationManager;
    }

    public void setOpenAcdContext(OpenAcdContext openAcdContext) {
        m_openAcdContext = openAcdContext;
    }

    public void setSipxReplicationContext(SipxReplicationContext sipxReplicationContext) {
        m_lazySipxReplicationContext = sipxReplicationContext;
    }
    @Required
    public void setRlsConfigFilesActivator(ConfigFileActivationManager configFileManager) {
        m_configFileManager = configFileManager;
    }

    public void setSipxServiceManager(SipxServiceManager serviceManager) {
        m_serviceManager = serviceManager;
    }

    public void setServiceConfigurator(ServiceConfigurator serviceConfigurator) {
        m_serviceConfigurator = serviceConfigurator;
    }

    public void setSipxProcessContext(SipxProcessContext sipxProcessContext) {
        m_sipxProcessContext = sipxProcessContext;
    }

    /**
     * use only in tests
     * @param executorService
     */
    public void setExecutorService(ExecutorService executorService) {
        m_executorService = executorService;
    }

    public void setEagerSipxReplicationContext(SipxReplicationContext eagerSipxReplicationContext) {
        m_eagerSipxReplicationContext = eagerSipxReplicationContext;
    }
}
