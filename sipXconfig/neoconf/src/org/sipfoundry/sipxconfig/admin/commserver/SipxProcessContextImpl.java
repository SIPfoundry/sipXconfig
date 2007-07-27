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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.collections.Factory;
import org.apache.commons.collections.MapUtils;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.secure.SecureXmlRpcClient;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

public class SipxProcessContextImpl extends SipxReplicationContextImpl implements
        BeanFactoryAware, SipxProcessContext, ApplicationListener {

    private EventsToServices m_eventsToServices = new EventsToServices();
    private String m_host;

    @Required
    public void setHost(String host) {
        m_host = host;
    }

    /**
     * Invokes the specified 'ProcMgmtRpc' method on the watchdog XML-RPC server of the specified
     * location, using the specified parameters. It adds this machine's hostname as the first
     * parameter.
     */
    protected Object invokeXmlRpcRequest(Location location, String methodName, Vector params) {
        try {
            // Add this machine's hostname as the first parameter.
            params.add(0, m_host);

            // The execute() method may throw OR return an XmlRpcException.
            XmlRpcClient client = new SecureXmlRpcClient(location.getProcessMonitorUrl());
            Object result = client.execute("ProcMgmtRpc." + methodName, params);
            if (result instanceof XmlRpcException) {
                throw (XmlRpcException) result;
            }

            // The result was not an XmlRpcException, so return it.
            return result;
        } catch (XmlRpcException e) {
            LOG.error("Error XMLRPC, fault: " + e.code + " message: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (MalformedURLException e) {
            LOG.error("Error URL: " + location.getProcessMonitorUrl());
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Read service status values from the process monitor and return them in an array.
     * ClassCastException or NoSuchElementException (both RuntimeException subclasses) could be
     * thrown from this method, but only if things have gone horribly wrong.
     */
    public ServiceStatus[] getStatus(Location location) {

        // Break the result into the keys and values.
        Map<String, String> result = (Map<String, String>) invokeXmlRpcRequest(location,
                "getStateAll", new Vector());

        // Loop through the key-value pairs and construct the ServiceStatus.
        List<ServiceStatus> serviceStatusList = new ArrayList(result.size());

        for (Map.Entry<String, String> entry : result.entrySet()) {

            String status = entry.getValue();
            ServiceStatus.Status st = ServiceStatus.Status.getEnum(status);
            if (st == null) {
                st = ServiceStatus.Status.UNKNOWN;
            }

            String name = entry.getKey();
            Process process = Process.getEnum(name);
            if (process == null) {
                // Ignore unknown processes
                LOG.warn("Unknown process name" + name + "received from: "
                        + location.getProcessMonitorUrl());
            } else {
                serviceStatusList.add(new ServiceStatus(process, st));
            }
        }

        return serviceStatusList.toArray(new ServiceStatus[serviceStatusList.size()]);
    }

    public void manageServices(Collection processes, Command command) {
        Location[] locations = getLocations();
        for (int i = 0; i < locations.length; i++) {
            Location location = locations[i];
            manageServices(location, processes, command);
        }
    }

    public void manageServices(Location location, Collection processes, Command command) {
        for (Iterator i = processes.iterator(); i.hasNext();) {
            Process process = (Process) i.next();
            manageService(location, process, command);
        }
    }

    public void manageService(Location location, Process process, Command command) {
        try {
            String alias = URLEncoder.encode(process.getName(), "UTF-8");

            Vector<Object> params = new Vector<Object>();
            params.add(alias);
            params.add(Boolean.TRUE); // Yes, block for the state change.
            boolean result = (Boolean) invokeXmlRpcRequest(location, command.getName(), params);
            if (!result) {
                LOG.warn("Failed to '" + command.getName() + "' process '" + alias + "'.");
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public void restartOnEvent(Collection services, Class eventClass) {
        m_eventsToServices.addServices(services, eventClass);
    }

    public void onApplicationEvent(ApplicationEvent event) {
        Collection services = m_eventsToServices.getServices(event.getClass());
        if (!services.isEmpty()) {
            // do not call if set is empty - it's harmless but it triggers topology.xml parsing
            manageServices(services, Command.RESTART);
        }
    }

    static final class EventsToServices {
        private Map m_map;

        public EventsToServices() {
            Factory setFactory = new Factory() {
                public Object create() {
                    return new HashSet();
                }
            };
            m_map = MapUtils.lazyMap(new HashMap(), setFactory);
        }

        public void addServices(Collection services, Class eventClass) {
            Set serviceSet = (Set) m_map.get(eventClass);
            serviceSet.addAll(services);
        }

        public Collection getServices(Class eventClass) {
            Set services = new HashSet();
            for (Iterator i = m_map.entrySet().iterator(); i.hasNext();) {
                Map.Entry entry = (Map.Entry) i.next();
                Class klass = (Class) entry.getKey();
                Collection servicesForKlass = (Collection) entry.getValue();
                if (klass.isAssignableFrom(eventClass)) {
                    services.addAll(servicesForKlass);
                }
            }
            // do that again this time removing collected services...
            for (Iterator i = m_map.values().iterator(); i.hasNext();) {
                Collection servicesForKlass = (Collection) i.next();
                servicesForKlass.removeAll(services);
            }
            return services;
        }
    }
}
