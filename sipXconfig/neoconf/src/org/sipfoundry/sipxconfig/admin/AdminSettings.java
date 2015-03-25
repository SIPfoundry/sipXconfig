/**
 * Copyright (c) 2012 eZuce, Inc. All rights reserved.
 * Contributed to SIPfoundry under a Contributor Agreement
 *
 * This software is free software; you can redistribute it and/or modify it under
 * the terms of the Affero General Public License (AGPL) as published by the
 * Free Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 */
package org.sipfoundry.sipxconfig.admin;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sipfoundry.sipxconfig.cfgmgt.DeployConfigOnEdit;
import org.sipfoundry.sipxconfig.feature.Feature;
import org.sipfoundry.sipxconfig.setting.PersistableSettings;
import org.sipfoundry.sipxconfig.setting.Setting;
import org.sipfoundry.sipxconfig.setting.SettingEntry;
import org.springframework.beans.factory.annotation.Required;

/**
 * Does not implement DeployOnEdit because we don't need to replicate to other servers
 * and we don't want to restart config server
 */
public class AdminSettings extends PersistableSettings implements DeployConfigOnEdit {

    public static final String SYSTEM_AUDIT = "configserver-config/systemAudit";
    private static final Log LOG = LogFactory.getLog(AdminSettings.class);

    private static final String LDAP_MANAGEMENT_DISABLE = "ldap-management/disable";
    private static final String LDAP_MANAGEMENT_DELETE = "ldap-management/delete";
    private static final String LDAP_MANAGEMENT_AGE = "ldap-management/age";
    private static final String LDAP_MANAGEMENT_PAGE_SIZE = "ldap-management/pageImportSize";
    private static final String AUTHENTICATION_AUTH_ACC_NAME = "configserver-config/account-name";
    private static final String AUTHENTICATION_EMAIL_ADDRESS = "configserver-config/email-address";
    private static final String CORS_DOMAIN_SETTING = "configserver-config/corsDomains";
    private static final String NEW_LDAP_USERS_GROUP_PREFIX = "ldap-management/newUserGroupPrefix";

    private PasswordPolicy m_passwordPolicy;
    private String[] m_logLevelKeys;

    @Override
    public String getBeanId() {
        return "adminSettings";
    }

    @Override
    public void initialize() {
        addDefaultBeanSettingHandler(new AdminSettingsDefaults());
    }

    @Override
    protected Setting loadSettings() {
        Setting adminSetting = getModelFilesContext().loadModelFile("sipxconfig/admin.xml");
        adminSetting.acceptVisitor(m_passwordPolicy);
        return adminSetting;
    }

    public String getSelectedPolicy() {
        return getSettingValue("configserver-config/password-policy");
    }

    public String getDefaultPassword() {
        return getSettingValue("configserver-config/password-default");
    }

    public String getDefaultVmPin() {
        return getSettingValue("configserver-config/vmpin-default");
    }

    public int getAge() {
        return (Integer) getSettingTypedValue(LDAP_MANAGEMENT_AGE);
    }

    public int getPageImportSize() {
        return (Integer) getSettingTypedValue(LDAP_MANAGEMENT_PAGE_SIZE);
    }

    public boolean isDisable() {
        return (Boolean) getSettingTypedValue(LDAP_MANAGEMENT_DISABLE);
    }

    public boolean isDelete() {
        return (Boolean) getSettingTypedValue(LDAP_MANAGEMENT_DELETE);
    }

    public String getNewLdapUserGroupNamePrefix() {
        return (String) getSettingTypedValue(NEW_LDAP_USERS_GROUP_PREFIX);
    }

    public void setDisable(boolean disable) {
        setSettingTypedValue(LDAP_MANAGEMENT_DISABLE, disable);
    }

    public void setDelete(boolean delete) {
        setSettingTypedValue(LDAP_MANAGEMENT_DELETE, delete);
    }

    public boolean isAuthAccName() {
        return (Boolean) getSettingTypedValue(AUTHENTICATION_AUTH_ACC_NAME);
    }

    public void setAuthAccName(boolean authAccName) {
        setSettingTypedValue(AUTHENTICATION_AUTH_ACC_NAME, authAccName);
    }

    public boolean isAuthEmailAddress() {
        return (Boolean) getSettingTypedValue(AUTHENTICATION_EMAIL_ADDRESS);
    }

    public void setEmailAddress(boolean authEmailAddress) {
        setSettingTypedValue(AUTHENTICATION_EMAIL_ADDRESS, authEmailAddress);
    }

    public String getCorsDomains() {
        return getSettingValue(CORS_DOMAIN_SETTING);
    }

    public void setCorsDomains(String corsDomains) {
        String noSpaces = validateDomainList(corsDomains);
        LOG.warn("Setting CORS domains " + corsDomains + " " + noSpaces);
        setSettingValue(CORS_DOMAIN_SETTING, noSpaces);
    }

    public boolean isSystemAuditEnabled() {
        return (Boolean) getSettingTypedValue(SYSTEM_AUDIT);
    }

    public void setSystemAuditEnabled(boolean systemAuditEnabled) {
        setSettingTypedValue(SYSTEM_AUDIT, systemAuditEnabled);
    }

    protected static String validateDomainList(String corsDomains) {
        if (corsDomains == null) {
            return StringUtils.EMPTY;
        }
        String noSpaces = corsDomains.replaceAll("\\s", StringUtils.EMPTY);
        String validDomainRegex = "\\w[\\w\\.\\-]*";
        String validDomainListRegex = String.format("%s[%s,]*", validDomainRegex, validDomainRegex);
        if (!noSpaces.matches(validDomainListRegex)) {
            throw new IllegalArgumentException("Invalid domain list. List must match " + validDomainListRegex);
        }

        return noSpaces;
    }

    @Required
    public void setPasswordPolicy(PasswordPolicy passwordPolicy) {
        m_passwordPolicy = passwordPolicy;
    }

    @Required
    public void setLogLevelKeys(String[] logLevelKeys) {
        m_logLevelKeys = logLevelKeys;
    }

    public class AdminSettingsDefaults {
        @SettingEntry(path = "configserver-config/password-policy")
        public String getDefaultPolicy() {
            return m_passwordPolicy.getDefaultPolicy();
        }
    }

    @Override
    public Collection<Feature> getAffectedFeaturesOnChange() {
        return Collections.singleton((Feature) AdminContext.FEATURE);
    }

    public PasswordPolicy getPasswordPolicy() {
        return m_passwordPolicy;
    }

    public String[] getLogLevelKeys() {
        return m_logLevelKeys;
    }
}
