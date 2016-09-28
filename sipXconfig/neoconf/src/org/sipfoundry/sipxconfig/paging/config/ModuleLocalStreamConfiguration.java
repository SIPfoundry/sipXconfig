package org.sipfoundry.sipxconfig.paging.config;

import java.io.IOException;
import java.io.Writer;

import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.moh.MusicOnHoldManager;
import org.sipfoundry.sipxconfig.paging.PagingContext;
import org.springframework.beans.factory.annotation.Required;

public class ModuleLocalStreamConfiguration extends AbstractModuleConfiguration {
	
	private MusicOnHoldManager m_musicOnHoldManager;
    private String m_docDir;

	@Override
	public void write(Writer writer, Location location, PagingContext pagingContext) throws IOException {
		String audioDir = null;
        if (!m_musicOnHoldManager.isAudioDirectoryEmpty()) {
            audioDir =  m_musicOnHoldManager.getAudioDirectoryPath();
        }
        write(writer, audioDir);
	}

	@Override
    public String getConfigName() {
        return "local_stream";
    }
	
	private void write(Writer writer, String audioDir) throws IOException {
		write(writer, new ContextGenerator()
        		.setAudioDir(audioDir)
        		.setDocDir(m_docDir)
        		.getContext());
	}

	public MusicOnHoldManager getM_musicOnHoldManager() {
		return m_musicOnHoldManager;
	}

	@Required
	public void setMusicOnHoldManager(MusicOnHoldManager musicOnHoldManager) {
		this.m_musicOnHoldManager = musicOnHoldManager;
	}

	public String getDocDir() {
		return m_docDir;
	}

	@Required
	public void setDocDir(String docDir) {
		this.m_docDir = docDir;
	}
	
	/**
	 * class ContextGenerator
	 */
    static class ContextGenerator extends BasicContextGenerator {
    	
    	public ContextGenerator() {
    		super();
    	}
    	
    	public ContextGenerator setDocDir(String dir) {
    		put("docDir", dir);
    		return this;
    	}
    	
    	public ContextGenerator setAudioDir(String dir) {
    		put("audioDir", dir);
    		return this;
    	}
    	
    }
	
}
