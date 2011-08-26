/*
 *
 *
 * Copyright (C) 2010 Avaya, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipfoundry.sipxconfig.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sipfoundry.sipxconfig.admin.ExtensionInUseException;
import org.sipfoundry.sipxconfig.admin.commserver.SipxReplicationContext;
import org.sipfoundry.sipxconfig.admin.commserver.imdb.AliasMapping;
import org.sipfoundry.sipxconfig.admin.commserver.imdb.DataSet;
import org.sipfoundry.sipxconfig.alias.AliasManager;
import org.sipfoundry.sipxconfig.common.CoreContext;
import org.sipfoundry.sipxconfig.common.Replicable;
import org.sipfoundry.sipxconfig.common.SipUri;
import org.sipfoundry.sipxconfig.common.UserException;
import org.springframework.beans.factory.annotation.Required;

import static org.apache.commons.lang.StringUtils.split;
import static org.apache.commons.lang.StringUtils.trim;

public class SipxAccCodeService extends SipxService implements LoggingEntity, Replicable {
    public static final String BEAN_ID = "sipxAccCodeService";
    public static final String LOG_SETTING = "acccode/log.level";
    public static final String AUTH_CODE_PREFIX = "authcode/SIP_AUTH_CODE_PREFIX";
    public static final String AUTH_CODE_ALIASES = "authcode/SIP_AUTH_CODE_ALIASES";
    private static final Log LOG = LogFactory.getLog(SipxAccCodeService.class);
    private static final String ALIAS_RELATION = "alias";
    private static final String ERROR_ALIAS_IN_USE = "&error.aliasinuse";

    private AliasManager m_aliasManager;
    private CoreContext m_coreContext;

    private SipxReplicationContext m_replicationContext;

    private String m_promptsDir;
    private String m_docDir;
    private String m_authcodeprefix;

    private String m_aliases;

    @Required
    public void setReplicationContext(SipxReplicationContext replicationContext) {
        m_replicationContext = replicationContext;
    }

    @Required
    public void setPromptsDir(String promptsDirectory) {
        m_promptsDir = promptsDirectory;
    }

    public String getPromptsDir() {
        return m_promptsDir;
    }

    @Required
    public void setDocDir(String docDirectory) {
        m_docDir = docDirectory;
    }

    public String getDocDir() {
        return m_docDir;
    }

    @Required
    public void setAliasManager(AliasManager aliasManager) {
        m_aliasManager = aliasManager;
    }

    /**
     * * Validates the data in this service and throws a UserException if there is a problem
     **/
    @Override
    public void validate() {
        String extension = getSettingValue(SipxAccCodeService.AUTH_CODE_PREFIX);
        if (!m_aliasManager.canObjectUseAlias(this, extension)) {
            getSipxServiceManager().resetServicesFromDb();
            throw new ExtensionInUseException("Auth code", extension);
        }

        String aliases = getSettingValue(SipxAccCodeService.AUTH_CODE_ALIASES);
        for (String alias : getAliasesSet(aliases)) {
            if (!m_aliasManager.canObjectUseAlias(this, alias)) {
                getSipxServiceManager().resetServicesFromDb();
                throw new UserException(ERROR_ALIAS_IN_USE, alias);
            }
        }
    }

    /** get the aliases from a space-delimited string */
    public Set<String> getAliasesSet(String aliasesString) {
        LOG.info(String.format("SipxAccCodeService::getAliasesString(): input:%s:", aliasesString));

        Set<String> aliasesSet = new LinkedHashSet<String>(0);

        if (aliasesString != null) {
            String[] aliases = split(aliasesString);
            for (String alias : aliases) {
                aliasesSet.add(trim(alias));
            }
        }
        LOG.info(String.format("SipxAccCodeService::getAliasesString(): retun set :%s:", aliasesSet));
        return aliasesSet;
    }

    @Override
    public String getLogSetting() {
        return LOG_SETTING;
    }

    @Override
    public void setLogLevel(String logLevel) {
        super.setLogLevel(logLevel);
    }

    @Override
    public String getLogLevel() {
        return super.getLogLevel();
    }

    @Override
    public String getLabelKey() {
        return super.getLabelKey();
    }

    @Override
    public void onConfigChange() {
        m_authcodeprefix = getSettingValue(AUTH_CODE_PREFIX);
        m_aliases = getSettingValue(AUTH_CODE_ALIASES);
        LOG.info(String.format("SipxAccCodeService::onConfigChange(): set prefix", m_authcodeprefix));
        LOG.info(String.format("SipxAccCodeService::onConfigChange(): set aliases", m_aliases));
        LOG.info(String.format("SipxAccCodeService::onConfigChange(): replicate ", DataSet.ALIAS));
        m_replicationContext.generate(this);

    }

    public String getAuthCodePrefix() {
        String prefix = getSettingValue(SipxAccCodeService.AUTH_CODE_PREFIX);
        return prefix;
    }

    public void setAuthCodePrefix(String authcodeprefix) {
        m_authcodeprefix = authcodeprefix;
        setSettingValue(SipxAccCodeService.AUTH_CODE_PREFIX, authcodeprefix);
    }

    public String getAuthCodeAliases() {
        String aliases = getSettingValue(SipxAccCodeService.AUTH_CODE_PREFIX);
        return aliases;
    }

    public void setAuthCodeAliases(String aliases) {
        m_aliases = aliases;
        setSettingValue(SipxAccCodeService.AUTH_CODE_ALIASES, aliases);
    }

    /** get the aliases from a space-delimited string */
    public Set<String> getAliasesAsSet() {

        String aliasesString = this.getSettingValue(SipxAccCodeService.AUTH_CODE_ALIASES);
        Set<String> aliasesSet = new LinkedHashSet<String>(0);

        if (aliasesString != null) {
            String[] aliases = split(aliasesString);
            for (String alias : aliases) {
                aliasesSet.add(trim(alias));
            }
        }
        return aliasesSet;
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setName(String name) {
        // TODO Auto-generated method stub
    }

    @Override
    public Collection<AliasMapping> getAliasMappings(String domain) {
        Set<String> aliasesSet = getAliasesAsSet();
        Collection<AliasMapping> mappings = new ArrayList<AliasMapping>(aliasesSet.size());
        // Add alias entry for each extension alias
        // all entries points to the same auth code url
        // sip:AUTH@47.135.162.72:15060;command=auth;
        // see mappingrules.xml
        for (String alias : aliasesSet) {
            // simple alias@bcm2072.com type of identity
            String contact = SipUri.format(getAuthCodePrefix(), getDomainName(), false);
            // direct mapping is for testing only
            // contact = getDirectContactUri();
            mappings.add(new AliasMapping(alias, contact, ALIAS_RELATION));
        }
        return mappings;
    }

    public Collection<AliasMapping> getAliasMappings() {
        return getAliasMappings(m_coreContext.getDomainName());
    }

    @Override
    public Set<DataSet> getDataSets() {
        Set<DataSet> dataSets = new HashSet<DataSet>();
        dataSets.add(DataSet.ALIAS);
        return dataSets;
    }

    @Override
    public String getIdentity(String domain) {
        return getAuthCodePrefix() + "@" + domain;
    }

    public void setCoreContext(CoreContext coreContext) {
        m_coreContext = coreContext;
    }

    @Override
    public boolean isValidUser() {
        return true;
    }

    @Override
    public Map<String, Object> getMongoProperties(String domain) {
        return Collections.EMPTY_MAP;
    }

}
