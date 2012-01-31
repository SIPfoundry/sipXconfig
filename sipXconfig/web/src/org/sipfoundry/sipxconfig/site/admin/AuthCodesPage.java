/*
 *
 *
 * Copyright (C) 2010 Avaya, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.site.admin;

import static org.apache.commons.lang.StringUtils.join;
import static org.apache.commons.lang.StringUtils.split;
import static org.apache.commons.lang.StringUtils.trim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tapestry.IPage;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.InitialValue;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.annotations.InjectPage;
import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.contrib.table.model.IBasicTableModel;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.sipfoundry.sipxconfig.acccode.AuthCode;
import org.sipfoundry.sipxconfig.acccode.AuthCodeManager;
import org.sipfoundry.sipxconfig.acccode.AuthCodeSettings;
import org.sipfoundry.sipxconfig.acccode.AuthCodes;
import org.sipfoundry.sipxconfig.alias.AliasManager;
import org.sipfoundry.sipxconfig.components.SelectMap;
import org.sipfoundry.sipxconfig.components.SipxBasePage;
import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;
import org.sipfoundry.sipxconfig.components.TapestryUtils;
import org.sipfoundry.sipxconfig.search.SearchManager;


public abstract class AuthCodesPage extends SipxBasePage implements PageBeginRenderListener {

    public static final Log LOG = LogFactory.getLog(AuthCodesPage.class);

    public static final String PAGE = "admin/AuthCodes";

    private Set<String> m_aliases = new LinkedHashSet<String>(0);

    @InjectObject("spring:authCodes")
    public abstract AuthCodes getAuthCodes();

    @InjectObject("spring:aliasManager")
    public abstract AliasManager getAliasManager();

    @Bean
    public abstract SipxValidationDelegate getValidator();

    @InjectPage(EditAuthCode.PAGE)
    public abstract EditAuthCode getEditAuthCodePage();

    @InjectObject(value = "spring:authCodeManager")
    public abstract AuthCodeManager getAuthCodeManager();

    public abstract AuthCodeSettings getSettings();

    public abstract void setSettings(AuthCodeSettings settings);

    @Bean
    public abstract SelectMap getSelections();

    public abstract AuthCode getCurrentRow();

    @InjectObject(value = "spring:searchManager")
    public abstract SearchManager getSearchManager();

    @Persist
    public abstract String getQueryText();

    @InitialValue("false")
    @Persist
    public abstract boolean getSearchMode();

    @Persist
    public abstract Integer getGroupId();

    public abstract void setGroupId(Integer groupId);


    @Override
    public void pageBeginRender(PageEvent event) {
        LOG.info("ENTERED AuthCodesPage::pageBeginRender()");
        if (getSettings() == null) {
            setSettings(getAuthCodes().getSettings());
        }
    }

    public IPage addAuthCode(IRequestCycle cycle) {
        return getEditAuthCodePage(null);
    }

    public IPage editAuthCode(int codeId) {
        return getEditAuthCodePage(codeId);
    }

    private IPage getEditAuthCodePage(Integer codeId) {
        EditAuthCode page = getEditAuthCodePage();
        page.setAuthCodeId(codeId);
        page.setReturnPage(PAGE);
        return page;
    }

    public IBasicTableModel getTableModel() {
        String queryText = getQueryText();
        if (!getSearchMode() || StringUtils.isBlank(queryText)) {
            AuthCodesTableModel tableModel = new AuthCodesTableModel();
            List<AuthCode> list = getAuthCodeManager().getAuthCodes();
            tableModel.setAuthCodes(list);
            return tableModel;
        } else {
            //Need to do searching
            SearchAuthCodesTableModel tableModel =
                        new SearchAuthCodesTableModel(getSearchManager(), queryText, getAuthCodeManager());
            return tableModel;
        }
    }

    public void deleteAuthCodes() {
        Collection<Integer> selectedLocations = getSelections().getAllSelected();
        getAuthCodeManager().deleteAuthCodes(selectedLocations);
    }

    /*
     * apply method to manually replicate the dialplan so that the new Authentication Code
     * Prefix is set in the mapping rules.
     */
    public void apply() {
        if (!TapestryUtils.isValid(this)) {
            return;
        }
        getAuthCodes().saveSettings(getSettings());
        TapestryUtils.recordSuccess(this, getMessages().getMessage("starPrefix.updated"));
    }

    //Alias Handling Section
    public Set<String> getAliases() {
        LOG.info(String.format("AuthCodesPage::getAliases(): %s ", m_aliases));
        return m_aliases;
    }

    public void setAliases(Set<String> aliases) {
        m_aliases = aliases;
        LOG.info(String.format("AuthCodesPage::setAliases(): %s ", aliases));
    }

    /**
     * Add the alias to the set of aliases. Return true if the alias was added, false if the alias
     * was already in the set.
     */
    public boolean addAlias(String alias) {
        return getAliases().add(trim(alias));
    }

    public void addAliases(String[] aliases) {
        for (String alias : aliases) {
            addAlias(alias);
        }
    }

    /** Return the aliases as a space-delimited string */
    public String getAliasesString() {
        List<String> aliases = new ArrayList<String>(getAliases());
        Collections.sort(aliases);
        String aliasesString = join(aliases.iterator(), " ");
        LOG.info(String.format("AuthCodesPage::getAliasesString(): returning:%s:", aliasesString));
        return aliasesString;
    }

    /** Set the aliases from a space-delimited string */
    public void setAliasesString(String aliasesString) {
        LOG.info(String.format("AuthCodesPage::setAliasesString(): input:%s:", aliasesString));
        getAliases().clear();
        if (aliasesString != null) {
            String[] aliases = split(aliasesString);
            addAliases(aliases);
        }
        LOG.info(String.format("AuthCodesPage::setAliasesString(): new aliases :%s:", m_aliases));
    }

}
