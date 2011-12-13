/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.admin.commserver;

import static java.util.Arrays.asList;
import static org.sipfoundry.sipxconfig.admin.commserver.imdb.MongoTestCaseHelper.assertCollectionCount;
import static org.sipfoundry.sipxconfig.admin.commserver.imdb.MongoTestCaseHelper.assertObjectWithIdFieldValuePresent;
import static org.sipfoundry.sipxconfig.admin.commserver.imdb.MongoTestCaseHelper.assertObjectWithIdNotPresent;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.easymock.EasyMock;
import org.sipfoundry.commons.mongo.MongoConstants;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.sipfoundry.sipxconfig.acd.AcdContext;
import org.sipfoundry.sipxconfig.admin.commserver.imdb.ImdbTestCase;
import org.sipfoundry.sipxconfig.common.UserException;
import org.sipfoundry.sipxconfig.common.event.DaoEventPublisher;
import org.sipfoundry.sipxconfig.conference.ConferenceBridgeContext;
import org.sipfoundry.sipxconfig.nattraversal.NatLocation;
import org.sipfoundry.sipxconfig.service.LocationSpecificService;
import org.sipfoundry.sipxconfig.service.SipxAcdService;
import org.sipfoundry.sipxconfig.service.SipxConfigService;
import org.sipfoundry.sipxconfig.service.SipxFreeswitchService;
import org.sipfoundry.sipxconfig.service.SipxProxyService;
import org.sipfoundry.sipxconfig.service.SipxServiceBundle;
import org.sipfoundry.sipxconfig.service.SipxServiceManager;

import com.mongodb.DBCollection;

public class LocationsManagerImplTestIntegration extends ImdbTestCase {
    private LocationsManager m_out;
    private AcdContext m_acdContext;
    private ConferenceBridgeContext m_conferenceBridgeContext;
    private LocationsManagerImpl m_locationsManagerImpl;
    private DaoEventPublisher m_originalDaoEventPublisher;
    private SipxServiceManager m_serviceManager;
    private SipxServiceBundle m_managementBundle;
    private SipxServiceBundle m_primarySipRouterBundle;
    private SipxServiceBundle m_redundantSipRouterBundle;
    private SipxFreeswitchService m_sipxFreeswitchService;
    private MongoTemplate m_nodedb;

    public void testGetLocations() throws Exception {
        loadDataSetXml("admin/commserver/clearLocations.xml");
        Location[] emptyLocations = m_out.getLocations();
        assertEquals(0, emptyLocations.length);

        loadDataSetXml("admin/commserver/seedLocationsAndServices.xml");
        Location[] locations = m_out.getLocations();
        assertEquals(3, locations.length);
        assertEquals("https://localhost:8092/RPC2", locations[0].getProcessMonitorUrl());
        assertEquals("https://secondary.example.org:8092/RPC2", locations[1].getProcessMonitorUrl());
    }

    public void testGetLocationByFqdn() throws Exception {
        loadDataSetXml("admin/commserver/clearLocations.xml");
        loadDataSetXml("admin/commserver/seedLocationsAndServices.xml");

        Location l = m_out.getLocationByFqdn("remotehost.example.org");
        assertEquals(new Integer(102), l.getId());
    }

    public void testGetLocationByAddress() throws Exception {
        loadDataSetXml("admin/commserver/clearLocations.xml");
        loadDataSetXml("admin/commserver/seedLocationsAndServices.xml");

        Location l = m_out.getLocationByAddress("10.1.1.1");
        assertEquals(new Integer(101), l.getId());
    }

    public void testFindById() throws Exception {
        loadDataSetXml("admin/commserver/seedLocationsAndServices.xml");
        Location[] locations = m_out.getLocations();

        Location secondLocation = locations[1];
        int locationId = secondLocation.getId();

        Location locationById = m_out.getLocation(locationId);
        assertNotNull(locationById);
        assertEquals(secondLocation.getName(), locationById.getName());

        Collection<LocationSpecificService> services = locationById.getServices();
        assertNotNull(services);
        assertEquals(3, services.size());
    }

    public void testGetPrimaryLocation() throws Exception {
        loadDataSetXml("admin/commserver/seedLocations.xml");
        Location location = m_out.getPrimaryLocation();
        assertNotNull(location);
        assertEquals(101, (int) location.getId());
    }

    public void testStore() throws Exception {
        loadDataSetXml("admin/commserver/clearLocations.xml");
        Location location = new Location();
        location.setName("test location");
        location.setPrimary(true);
        location.setFqdn("location.example.org");
        location.setAddress("192.168.1.2");
        location.setFqdn("localhost");
        location.setRegistered(true);

        Location location2 = new Location();
        location2.setName("test location");
        location2.setFqdn("location2.example.org");
        location2.setAddress("192.168.1.3");
        location2.setFqdn("localhost1");
        location2.setRegistered(false);

        m_out.saveLocation(location);
        m_out.saveLocation(location2);

        Location[] dbLocations = m_out.getLocations();
        assertEquals(2, dbLocations.length);
        assertEquals("test location", dbLocations[0].getName());
        assertEquals("192.168.1.2", dbLocations[0].getAddress());
        assertEquals("localhost", dbLocations[0].getFqdn());
        assertTrue(dbLocations[0].isCallTraffic());
        assertTrue(dbLocations[0].isReplicateConfig());
        assertTrue(dbLocations[1].isReplicateConfig());
        assertTrue(dbLocations[1].isReplicateConfig());

        // test conditions on offline options
        try {
            dbLocations[1].setCallTraffic(true);
            dbLocations[1].setReplicateConfig(false);
            m_out.saveLocation(dbLocations[1]);
            fail();
        } catch (UserException e) {
        }
        try {
            dbLocations[0].setReplicateConfig(false);
            m_out.saveLocation(dbLocations[0]);
            fail();
        } catch (UserException e) {
        }
        dbLocations[1].setCallTraffic(false);
        dbLocations[1].setReplicateConfig(false);
        m_out.saveLocation(dbLocations[1]);

        assertObjectWithIdFieldValuePresent(getNodeCollection(), dbLocations[0].getId(), "ip", "192.168.1.2");
        assertObjectWithIdNotPresent(getEntityCollection(), dbLocations[1].getId());
        
        dbLocations[1].setCallTraffic(true);
        dbLocations[1].setReplicateConfig(true);
        m_out.saveLocation(dbLocations[1]);
    }
    
    private DBCollection getNodeCollection() {
        return getImdb().getDb().getCollection(MongoConstants.NODE_COLLECTION);
    }

    public void testsaveLocationWithDuplicateFqdnOrIp() throws Exception {
        loadDataSetXml("admin/commserver/clearLocations.xml");
        Location location = new Location();
        location.setName("test location");
        location.setAddress("10.1.1.1");
        location.setFqdn("localhost");
        m_out.saveLocation(location);

        Location[] dbLocations = m_out.getLocations();
        assertEquals(1, dbLocations.length);
        assertEquals("test location", dbLocations[0].getName());
        assertEquals("10.1.1.1", dbLocations[0].getAddress());
        assertEquals("localhost", dbLocations[0].getFqdn());

        location = new Location();
        location.setName("test location");
        location.setAddress("10.1.1.2");
        // Same FQDN
        location.setFqdn("localhost");
        try {
            m_out.saveLocation(location);
            assertTrue(false);
        } catch (UserException ex) {
            assertTrue(true);
        }

        location = new Location();
        location.setName("test location");
        // Same ip address
        location.setAddress("10.1.1.1");
        location.setFqdn("localhost.localdomain");
        try {
            m_out.saveLocation(location);
            assertTrue(false);
        } catch (UserException ex) {
            assertTrue(true);
        }
    }

    public void DISABLED_XX_9963_testDeleteWithServices() throws Exception {
        loadDataSetXml("admin/commserver/seedLocationsAndServices.xml");
        Location[] locationsBeforeDelete = m_out.getLocations();
        assertEquals(3, locationsBeforeDelete.length);

        Location locationToDelete = m_out.getLocation(101);

        DaoEventPublisher daoEventPublisher = EasyMock.createMock(DaoEventPublisher.class);
        daoEventPublisher.publishDelete(locationToDelete);
        EasyMock.expectLastCall();
        EasyMock.replay(daoEventPublisher);
        modifyContext(m_locationsManagerImpl, "daoEventPublisher", m_originalDaoEventPublisher, daoEventPublisher);

        m_out.deleteLocation(locationToDelete);

        Location[] locationsAfterDelete = m_out.getLocations();
        assertEquals(2, locationsAfterDelete.length);
        assertEquals("remotehost.example.org", locationsAfterDelete[1].getFqdn());
    }

    public void testAcdBridgePublishSaveDelete() throws Exception {
        loadDataSetXml("admin/commserver/clearLocations.xml");
        Location[] emptyLocations = m_out.getLocations();
        assertEquals(0, emptyLocations.length);
        assertEquals(0, m_acdContext.getServers().size());

        Location location = new Location();
        location.setName("test location");
        location.setAddress("192.168.1.2");
        location.setFqdn("location1");
        location.setInstalledBundles(asList("acdBundle"));
        m_out.saveLocation(location);
        assertEquals(1, m_acdContext.getServers().size());
        assertEquals(0, m_conferenceBridgeContext.getBridges().size());

        LocationSpecificService service = new LocationSpecificService();
        service.setSipxService(m_sipxFreeswitchService);
        service.setLocation(location);
        location.addService(m_sipxFreeswitchService);
        location.setInstalledBundles(asList("acdBundle", "conferenceBundle"));
        m_out.saveLocation(location);
        assertEquals(1, m_acdContext.getServers().size());
        assertEquals(1, m_conferenceBridgeContext.getBridges().size());

        location.setInstalledBundles(asList("acdBundle"));
        m_out.saveLocation(location);
        assertEquals(1, m_acdContext.getServers().size());
        assertEquals(0, m_conferenceBridgeContext.getBridges().size());

        location.setInstalledBundles(Collections.<String> emptyList());
        m_out.saveLocation(location);
        assertEquals(0, m_acdContext.getServers().size());
        assertEquals(0, m_conferenceBridgeContext.getBridges().size());
    }

    public void testStoreNatLocation() throws Exception {
        loadDataSetXml("admin/commserver/seedLocations.xml");

        Location location = m_out.getPrimaryLocation();
        NatLocation natDB = location.getNat();
        assertNotNull(natDB);

        NatLocation nat = new NatLocation();
        nat.setStunAddress("stun.com");
        nat.setStunInterval(30);
        nat.setPublicPort(5160);
        nat.setPublicTlsPort(5161);
        nat.setStartRtpPort(30000);
        nat.setStopRtpPort(30100);

        location.setNat(nat);
        m_out.saveLocation(location);

        natDB = m_out.getLocation(location.getId()).getNat();
        assertNotNull(natDB);
        assertEquals("stun.com", natDB.getStunAddress());
        assertEquals(30, natDB.getStunInterval());
        assertEquals(5160, natDB.getPublicPort());
        assertEquals(5161, natDB.getPublicTlsPort());
        assertEquals(30000, natDB.getStartRtpPort());
        assertEquals(30100, natDB.getStopRtpPort());
    }

    public void testGetNatLocation() throws Exception {
        loadDataSet("nattraversal/nat_location.db.xml");

        NatLocation natLocation = m_out.getLocation(111).getNat();
        assertEquals("stun.ezuce.com", natLocation.getStunAddress());
        assertEquals(60, natLocation.getStunInterval());
        assertEquals(5060, natLocation.getPublicPort());
        assertEquals(5061, natLocation.getPublicTlsPort());
        assertEquals(30000, natLocation.getStartRtpPort());
        assertEquals(31000, natLocation.getStopRtpPort());
    }

    public void testGetLocationByBundle() throws Exception {
        loadDataSetXml("admin/commserver/clearLocations.xml");
        loadDataSetXml("service/clear-service.xml");
        loadDataSetXml("admin/commserver/seedLocationsAndBundles.xml");

        Location primaryLocation = m_out.getLocation(1001);
        m_serviceManager.setBundlesForLocation(primaryLocation,
                Arrays.asList(m_managementBundle, m_primarySipRouterBundle));
        Location secondaryLocation = m_out.getLocation(1002);
        m_serviceManager.setBundlesForLocation(secondaryLocation, Arrays.asList(m_redundantSipRouterBundle));

        assertTrue(m_serviceManager.isServiceInstalled(primaryLocation.getId(), SipxConfigService.BEAN_ID));
        assertTrue(m_serviceManager.isServiceInstalled(primaryLocation.getId(), SipxProxyService.BEAN_ID));
        assertFalse(m_serviceManager.isServiceInstalled(primaryLocation.getId(), SipxAcdService.BEAN_ID));
        assertTrue(m_serviceManager.isServiceInstalled(secondaryLocation.getId(), SipxProxyService.BEAN_ID));
        assertFalse(m_serviceManager.isServiceInstalled(secondaryLocation.getId(), SipxConfigService.BEAN_ID));

        Location location = m_out.getLocationByBundle("managementBundle");
        assertEquals("primary.exampl.org", location.getFqdn());
        assertNull(m_out.getLocationByBundle("voicemailBundle"));
        location = m_out.getLocationByBundle("redundantSipRouterBundle");
        assertEquals("secondary.example.org", location.getFqdn());
    }

    public void setLocationsManager(LocationsManager locationsManager) {
        m_out = locationsManager;
    }

    /*
     * Allows access to setters not exposed in interface. Used for setting mock objects for
     * testing purposes
     */
    public void setLocationsManagerImpl(LocationsManagerImpl locationsManagerImpl) {
        m_locationsManagerImpl = locationsManagerImpl;
    }

    public void setDaoEventPublisher(DaoEventPublisher daoEventPublisher) {
        m_originalDaoEventPublisher = daoEventPublisher;
    }

    public void setAcdContext(AcdContext acdContext) {
        m_acdContext = acdContext;
    }

    public void setConferenceBridgeContext(ConferenceBridgeContext conferenceBridgeContext) {
        m_conferenceBridgeContext = conferenceBridgeContext;
    }

    public void setSipxServiceManager(SipxServiceManager serviceManager) {
        m_serviceManager = serviceManager;
    }

    public void setManagementBundle(SipxServiceBundle managementBundle) {
        m_managementBundle = managementBundle;
    }

    public void setPrimarySipRouterBundle(SipxServiceBundle primarySipRouterBundle) {
        m_primarySipRouterBundle = primarySipRouterBundle;
    }

    public void setRedundantSipRouterBundle(SipxServiceBundle redundantSipRouterBundle) {
        m_redundantSipRouterBundle = redundantSipRouterBundle;
    }

    public void testStoreServerRoleLocation() throws Exception {
        loadDataSetXml("admin/commserver/clearLocations.xml");
        Location[] emptyLocations = m_out.getLocations();
        assertEquals(0, emptyLocations.length);
        assertEquals(0, m_conferenceBridgeContext.getBridges().size());

        Location location = new Location();
        location.setName("test location");
        location.setAddress("192.168.1.2");
        location.setFqdn("location1");
        location.setInstalledBundles(asList("conferenceBundle"));
        ServerRoleLocation serverRole = new ServerRoleLocation();
        serverRole.setModifiedBundles(m_serviceManager.getBundlesForLocation(location));
        m_out.storeServerRoleLocation(location, serverRole);
        assertEquals(1, m_conferenceBridgeContext.getBridges().size());
    }

    public void setSipxFreeswitchService(SipxFreeswitchService sipxFreeswitchService) {
        m_sipxFreeswitchService = sipxFreeswitchService;
    }
}
