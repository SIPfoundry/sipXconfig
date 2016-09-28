package org.sipfoundry.sipxconfig.paging.config;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.paging.PagingContext;

public abstract class AbstractPagingConfiguration implements FSPagingProvider {
	
	/** Default Freeswitch module name directory **/
	public static final String MODULE_NAME = "sipxpage";
	
	private VelocityEngine m_velocityEngine;
	private PagingContext m_pagingContext;

    public VelocityEngine getVelocityEngine() {
        return m_velocityEngine;
    }

    public void setVelocityEngine(VelocityEngine velocityEngine) {
        m_velocityEngine = velocityEngine;
    }

    public abstract void write(Writer writer, Location location, PagingContext pagingContext) throws IOException;

    @SuppressWarnings("deprecation")
	protected void write(Writer writer, VelocityContext context) throws IOException {
        try {
            m_velocityEngine.mergeTemplate(getTemplate(), context, writer);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
    
    public abstract String getFileName();

    protected abstract String getTemplate();

    /**
     * Override to add modules to modules.conf.xml. Note you don't have to have a subclass
     * of this class to add modules. Any bean that implements FreeswitchProvider can
     * also submit modules to be enabled.
     *
     * @return list of module names.
     */
    @Override
    public List<String> getRequiredModules(PagingContext feature, Location location) {
        return Collections.emptyList();
    }

    public PagingContext getPagingContext() {
        return m_pagingContext;
    }

    public void setPagingContext(PagingContext pagingContext) {
    	m_pagingContext = pagingContext;
    }
}
