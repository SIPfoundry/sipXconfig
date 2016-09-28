package org.sipfoundry.sipxconfig.paging.config;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.domain.Domain;
import org.sipfoundry.sipxconfig.domain.DomainManager;
import org.sipfoundry.sipxconfig.paging.PagingContext;
import org.sipfoundry.sipxconfig.paging.PagingGroup;

public class DialPlanConfiguration extends AbstractPagingConfiguration {
	
	/** Default Dialplan configuration directory **/
	private static String DEFAULT_DIALPLAN_DIRECTORY = "dialplan";
	
	private String m_dialplanDirectory;
	private String m_audioDirectory;
	private DomainManager m_domainManager;
	
	@Override
    public void write(Writer writer, Location location, PagingContext pagingContext) 
    		throws IOException {
		List<PagingGroup> pagingGroup = pagingContext.getPagingGroups();
		write(writer, new DialPlanContextGenerator()
        		.setDomain(m_domainManager.getDomain())
        		.setAudioDirectory(m_audioDirectory)
        		.setPagingGroup(pagingGroup != null ? pagingGroup : new ArrayList<PagingGroup>())
        		.getContext());
    }
	
	@Override
    public String getFileName() {
        return MODULE_NAME + "/" + getDialPlanDirectory() + "/default.xml";
    }
	
	@Override
    protected String getTemplate() {
        return MODULE_NAME + "/dialplan_default.xml.vm";
    }

	public String getDialPlanDirectory() {
		return StringUtils.isNotBlank(m_dialplanDirectory) 
				? m_dialplanDirectory : DEFAULT_DIALPLAN_DIRECTORY;
	}

	public void setDialPlanDirectory(String moduleDirectory) {
		this.m_dialplanDirectory = moduleDirectory;
	}
	
	public void setAudioDirectory(String audioDirectory) {
		this.m_audioDirectory = audioDirectory;
	}
	
	public void setDomainManager(DomainManager domainManager) {
		m_domainManager = domainManager;
	}
	
	/**
	 * class DialPlanContextGenerator
	 */
    static class DialPlanContextGenerator extends BasicContextGenerator {
    	
    	public DialPlanContextGenerator() {
    		super();
    	}

		public DialPlanContextGenerator setPagingGroup(List<PagingGroup> pagingGroups) {
    		put("pagingGroups", pagingGroups);
    		return this;
    	}
    	
    	public DialPlanContextGenerator setDomain(Domain domain) {
    		put("domain", domain);
    		put("realm", domain.getSipRealm());
    		return this;
    	}
    	
    	public DialPlanContextGenerator setAlertCode(String code) {
    		put("alertCode", code);
    		return this;
    	}
    	
		public DialPlanContextGenerator setAudioDirectory(String audioDirectory) {
			put("audioDirectory", audioDirectory);
    		return this;
		}

    }
}
