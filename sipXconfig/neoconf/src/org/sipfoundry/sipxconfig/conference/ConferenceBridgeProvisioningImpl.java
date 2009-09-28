/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */
package org.sipfoundry.sipxconfig.conference;

import org.sipfoundry.sipxconfig.admin.commserver.Location;
import org.sipfoundry.sipxconfig.admin.commserver.SipxReplicationContext;
import org.sipfoundry.sipxconfig.admin.commserver.imdb.DataSet;
import org.sipfoundry.sipxconfig.service.ServiceConfigurator;
import org.sipfoundry.sipxconfig.service.SipxFreeswitchService;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class ConferenceBridgeProvisioningImpl extends HibernateDaoSupport implements
        ConferenceBridgeProvisioning {
    private SipxReplicationContext m_replicationContext;
    private ServiceConfigurator m_serviceConfigurator;

    public void deploy(int bridgeId) {
        Bridge bridge = (Bridge) getHibernateTemplate().load(Bridge.class, bridgeId);
        Location location = bridge.getLocation();

        m_replicationContext.generate(DataSet.ALIAS);

        // only need to replicate files that do not require restart
        SipxFreeswitchService service = bridge.getFreeswitchService();
        m_serviceConfigurator.replicateServiceConfig(location, service, true);
        service.reloadXml(location);
    }

    @Required
    public void setReplicationContext(SipxReplicationContext replicationContext) {
        m_replicationContext = replicationContext;
    }

    @Required
    public void setServiceConfigurator(ServiceConfigurator serviceConfigurator) {
        m_serviceConfigurator = serviceConfigurator;
    }
}
