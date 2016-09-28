package org.sipfoundry.sipxconfig.bridge.config;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.sipfoundry.sipxconfig.bridge.BridgeSbc;
import org.sipfoundry.sipxconfig.bridge.BridgeSbcContext;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.sbc.SbcDeviceManager;

public abstract class AbstractTrunkConfiguration implements FSTrunkProvider {
	
	/** Default Freeswitch module name directory **/
	public static final String MODULE_NAME = "sipxbridge";
	
	private VelocityEngine m_velocityEngine;
	private SbcDeviceManager m_sbcDeviceManager;

    public VelocityEngine getVelocityEngine() {
        return m_velocityEngine;
    }

    public void setVelocityEngine(VelocityEngine velocityEngine) {
        m_velocityEngine = velocityEngine;
    }

    public abstract void write(Writer writer, Location location, BridgeSbc sbcDevice) throws IOException;

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
    public List<String> getRequiredModules(BridgeSbcContext feature, Location location) {
        return Collections.emptyList();
    }

    public SbcDeviceManager getSbcDeviceManager() {
        return m_sbcDeviceManager;
    }

    public void setSbcDeviceManager(SbcDeviceManager sbcDeviceManager) {
    	m_sbcDeviceManager = sbcDeviceManager;
    }
}
