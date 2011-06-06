/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 */
package org.sipfoundry.sipxconfig.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.restlet.Client;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sipfoundry.sipxconfig.branch.Branch;
import org.sipfoundry.sipxconfig.domain.DomainManager;
import org.sipfoundry.sipxconfig.moh.MusicOnHoldManager;
import org.sipfoundry.sipxconfig.permission.Permission;
import org.sipfoundry.sipxconfig.permission.PermissionManager;
import org.sipfoundry.sipxconfig.permission.PermissionName;
import org.sipfoundry.sipxconfig.phonebook.Address;
import org.sipfoundry.sipxconfig.phonebook.AddressBookEntry;
import org.sipfoundry.sipxconfig.service.SipxImbotService;
import org.sipfoundry.sipxconfig.setting.BeanWithGroups;
import org.sipfoundry.sipxconfig.setting.Group;
import org.sipfoundry.sipxconfig.setting.Setting;
import org.sipfoundry.sipxconfig.setting.SettingEntry;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.defaultString;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.join;
import static org.apache.commons.lang.StringUtils.split;
import static org.apache.commons.lang.StringUtils.trim;
import static org.apache.commons.lang.StringUtils.trimToNull;

/**
 * Can be user that logs in, can be superadmin, can be user for phone line
 */
public abstract class AbstractUser extends BeanWithGroups {
    public static final String GROUP_RESOURCE_ID = "user";

    // property names
    public static final String ALIASES_PROP = "aliases";
    public static final String FIRST_NAME_PROP = "firstName";
    public static final String LAST_NAME_PROP = "lastName";
    public static final String USER_NAME_PROP = "userName";

    public static final String MOH_SETTING = "moh";
    public static final String MOH_AUDIO_SOURCE_SETTING = "moh/audio-source";
    public static final String FAX_EXTENSION_PREFIX = "~~ff~";
    public static final String EMPTY_STRING = "";
    public static final String FAX_EXTENSION_SETTING = "voicemail/fax/extension";
    public static final String DID_SETTING = "voicemail/fax/did";
    public static final String CALLFWD_TIMER = "callfwd/timer";


    public static enum MohAudioSource {
        FILES_SRC, PERSONAL_FILES_SRC, SOUNDCARD_SRC, SYSTEM_DEFAULT, NONE;

        public static MohAudioSource parseSetting(String mohSetting) {
            try {
                return valueOf(mohSetting);
            } catch (IllegalArgumentException e) {
                return SYSTEM_DEFAULT;
            }
        }
    }

    // Reserved name for the special superadmin user. In principle, this name could now be
    // anything, it's just "superadmin" by current convention.
    public static final String SUPERADMIN = "superadmin";

    // "0" cannot be used as valid extension - it's hardcoded in login.vxml
    private static final Pattern PATTERN_NUMERIC = Pattern.compile("([1-9]\\d*)|(0\\d+)");

    private PermissionManager m_permissionManager;
    private DomainManager m_domainManager;

    private SipxImbotService m_sipxImbotService;

    private String m_firstName;

    private String m_sipPassword;

    private String m_pintoken;

    private String m_lastName;

    private String m_userName;

    private Set<String> m_aliases = new LinkedHashSet<String>(0);

    private Set m_supervisorForGroups;

    private AddressBookEntry m_addressBookEntry;

    private boolean m_isShared;

    private Branch m_branch;

    private MusicOnHoldManager m_musicOnHoldManager;

    private final Client m_restClient = new Client(Protocol.HTTP);

    /**
     * Return the pintoken, which is the hash of the user's PIN. The PIN itself is private to the
     * user. To keep the PIN secure, we don't store it.
     */
    public String getPintoken() {
        return defaultString(m_pintoken, EMPTY);
    }

    /**
     * Set the pintoken, which is the hash of the user's PIN. This method is only for prevent the
     * pintoken from being nulluse by Hibernate. Call setPin to change the PIN.
     */
    public void setPintoken(String pintoken) {
        m_pintoken = pintoken;
    }

    /**
     * Set the PIN, protecting it under a security realm. The PIN is private to the user. To keep
     * the PIN secure, we don't store it. Instead we store the "pintoken", which is a hash of the
     * PIN.
     *
     * @param pin PIN
     * @param realm security realm
     */
    public void setPin(String pin, String realm) {
        String pin2 = defaultString(pin, EMPTY); // handle null
        // pin
        setPintoken(Md5Encoder.digestPassword(m_userName, realm, pin2));
    }

    public String getFirstName() {
        return m_firstName;
    }

    public void setFirstName(String firstName) {
        m_firstName = trim(firstName);
    }

    public String getSipPassword() {
        return m_sipPassword;
    }

    public String getSipPasswordHash(String realm) {
        String password = defaultString(m_sipPassword, EMPTY);
        return Md5Encoder.digestPassword(m_userName, realm, password);
    }

    public void setSipPassword(String password) {
        m_sipPassword = password;
    }

    public String getLastName() {
        return m_lastName;
    }

    public void setLastName(String lastName) {
        m_lastName = trim(lastName);
    }

    public String getUserName() {
        return defaultString(m_userName, EMPTY);
    }

    public void setUserName(String userName) {
        m_userName = trim(userName);
    }

    /**
     * Builds displayName based on the first and last names.
     *
     * Should be only used to retrieve displayName part of SIP URI: it will return null if both
     * last names and first name are empty. Use getLabel as safer alternative.
     */
    public String getDisplayName() {
        Object[] names = {
            m_firstName, m_lastName
        };
        String s = join(names, ' ');
        return trimToNull(s);
    }

    public String getLabel() {
        return defaultString(getDisplayName(), getUserName());
    }

    public Set<String> getAliases() {
        return m_aliases;
    }

    public void setAliases(Set<String> aliases) {
        m_aliases = aliases;
    }

    public String getDomain() {
        return m_domainManager.getDomainName();
    }

    public Branch getBranch() {
        return m_branch;
    }

    public void setBranch(Branch branch) {
        m_branch = branch;
    }

    public void setMusicOnHoldManager(MusicOnHoldManager musicOnHoldManager) {
        m_musicOnHoldManager = musicOnHoldManager;
    }

    private List<String> getNumericAliases() {
        Set<String> aliases = getAliases();
        List<String> numeric = new ArrayList<String>(aliases.size());
        for (String alias : aliases) {
            if (isNumeric(alias)) {
                numeric.add(alias);
            }
        }
        return numeric;
    }

    /**
     * Finds the shorted numeric alias for this user.
     *
     * @return null if no numeric aliases, shortest numeric alias (if there more than one that
     *         have equal lenght we can return any of them)
     */
    private String getShortestNumericAlias() {
        List<String> numericAliases = getNumericAliases();
        String shortestAlias = null;
        int len = 0;
        for (String alias : numericAliases) {
            if (shortestAlias == null || alias.length() < len) {
                shortestAlias = alias;
                len = alias.length();
            }
        }
        return shortestAlias;
    }

    public boolean hasNumericUsername() {
        return isNumeric(m_userName);
    }

    public static boolean isNumeric(String s) {
        Matcher m = PATTERN_NUMERIC.matcher(s);
        return m.matches();
    }

    /**
     * Get numeric extension for this user. Since we are trying to support many possible options
     * we are going to try user name and then list of aliases. If user has more than a single
     * numeric alias it's not going to work reliably.
     *
     * Note: since "0" is hardcoded in login.vxml it cannot be used as user extension
     *
     * @return String representing numeric extension for this user
     */
    public String getExtension(boolean considerUserName) {
        if (considerUserName && hasNumericUsername()) {
            return m_userName;
        }
        return getShortestNumericAlias();
    }

    /**
     * Copy the input aliases to become the aliases of this user, without replacing the Set
     * object. For a user read from the DB, Hibernate creates the Set and we don't want to mess
     * with it. Also, by copying the aliases, subsequent changes to the input Set won't affect the
     * user's Set, since it is a separate object.
     */
    public void copyAliases(Collection<String> aliases) {
        getAliases().clear();
        getAliases().addAll(aliases);
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
        return join(aliases.iterator(), " ");
    }

    /** Set the aliases from a space-delimited string */
    public void setAliasesString(String aliasesString) {
        getAliases().clear();
        if (aliasesString != null) {
            String[] aliases = split(aliasesString);
            addAliases(aliases);
        }
    }

    /**
     * Creates long version of user SIP URI (with display name)
     */
    public String getUri(String domainName) {
        return SipUri.format(this, domainName);
    }

    /**
     * Creates short version of user SIP URI (without display name)
     *
     * sip:user@example.com
     */
    public String getAddrSpec(String domainName) {
        return SipUri.format(m_userName, domainName, false);
    }

    @Override
    public void initialize() {
        addDefaultBeanSettingHandler(new AbstractUserDefaults(m_permissionManager));
    }

    @Override
    protected Setting loadSettings() {
        if (m_permissionManager != null) {
            return m_permissionManager.getPermissionModel();
        }
        return null;
    }



    /**
     * Returns the names of all permissions assigned to the user
     */
    public Collection<String> getUserPermissionNames() {
        Collection<Permission> perms = m_permissionManager.getPermissions();
        Collection<String> names = new ArrayList<String>(perms.size());
        for (Permission perm : perms) {
            if (hasPermission(perm)) {
                names.add(perm.getName());
            }
        }
        return names;
    }

    /**
     * Check if a user has a specific permission
     */
    public boolean hasPermission(PermissionName permissionName) {
        Setting setting = retrieveSettingForPermission(permissionName);
        return Permission.isEnabled(setting.getValue());
    }

    /**
     * Check if a user has a specific permission
     */
    public boolean hasPermission(Permission permission) {
        Setting setting = retrieveSettingForPermission(permission);
        return Permission.isEnabled(setting.getValue());
    }

    /**
     * Set specific permission for the user
     *
     * @param permissionName - permission to set
     * @param enabled - true for enabled, false for disabled
     */
    public void setPermission(PermissionName permissionName, boolean enabled) {
        Setting setting = retrieveSettingForPermission(permissionName);
        setting.setTypedValue(enabled);
    }

    /**
     * Set specific permission for the user
     *
     * @param permission - permission to set
     * @param enabled - true for enabled, false for disabled
     */
    public void setPermission(Permission permission, boolean enabled) {
        Setting setting = retrieveSettingForPermission(permission);
        setting.setTypedValue(enabled);
    }

    public boolean isAdmin() {
        return hasPermission(PermissionName.SUPERADMIN);
    }

    private Setting retrieveSettingForSettingPath(String path, String name) {
        Setting setting = getSettings().getSetting(path);
        if (setting == null) {
            throw new IllegalArgumentException("Setting " + name + " does not exist in user setting model");
        }
        return setting;
    }

    private Setting retrieveSettingForPermission(PermissionName permissionName) {
        return retrieveSettingForSettingPath(permissionName.getPath(), permissionName.getName());
    }

    private Setting retrieveSettingForPermission(Permission permission) {
        return retrieveSettingForSettingPath(permission.getSettingPath(), permission.getName());
    }

    public String getFaxExtension() {
        Setting setting = null == getSettings() ? null : getSettings().getSetting(FAX_EXTENSION_SETTING);
        return null == setting ? EMPTY_STRING : (setting.getTypedValue() == null ? EMPTY_STRING : (String) setting
                .getTypedValue());
    }

    public void setFaxExtension(String faxExtension) {
        setSettingValue(FAX_EXTENSION_SETTING, faxExtension);
    }

    public String getFaxDid() {
        Setting setting = null == getSettings() ? null : getSettings().getSetting(DID_SETTING);
        return null == setting ? EMPTY_STRING : (setting.getTypedValue() == null ? EMPTY_STRING : (String) setting
                .getTypedValue());
    }

    public void setFaxDid(String did) {
        setSettingValue(DID_SETTING, did);
    }

    public boolean isSupervisor() {
        return m_supervisorForGroups != null && m_supervisorForGroups.size() > 0;
    }

    public Set getSupervisorForGroups() {
        return m_supervisorForGroups;
    }

    public void setSupervisorForGroups(Set<Group> supervisorForGroups) {
        m_supervisorForGroups = supervisorForGroups;
    }

    public void clearSupervisorForGroups() {
        if (m_supervisorForGroups != null) {
            m_supervisorForGroups.clear();
        }
    }

    public Branch getSite() {
        Branch branch = getBranch();
        if (branch != null) {
            return branch;
        }
        return getInheritedBranch();
    }

    public void addSupervisorForGroup(Group group) {
        if (group.isNew()) {
            throw new RuntimeException("Group needs to be saved before it can be added to the set.");
        }
        if (m_supervisorForGroups == null) {
            m_supervisorForGroups = new HashSet();
        }
        m_supervisorForGroups.add(group);
    }

    /**
     * Check if the branch in question is referenced by this user
     *
     * @param Branch
     * @return true if any references have been found false otherwise
     */
    public boolean checkBranch(Branch branch) {
        return m_branch.equals(branch);
    }

    public String getName() {
        return getUserName();
    }

    public void setName(String name) {
        setUserName(name);
    }

    public String getImId() {
        if (m_addressBookEntry == null) {
            return null;
        }
        return m_addressBookEntry.getImId();
    }

    public void setImId(String id) {
        useAddressBookEntry().setImId(trim(id));
    }

    public String getImDisplayName() {
        if (m_addressBookEntry == null) {
            return null;
        }
        return m_addressBookEntry.getImDisplayName();
    }

    public void setImDisplayName(String imDisplayName) {
        useAddressBookEntry().setImDisplayName(trim(imDisplayName));
    }

    public String getImPassword() {
        if (m_addressBookEntry == null) {
            return null;
        }
        return m_addressBookEntry.getImPassword();
    }

    public void setImPassword(String imPassword) {
        useAddressBookEntry().setImPassword(imPassword);
    }

    /**
     * Creates address book entry if it does not exist
     */
    private AddressBookEntry useAddressBookEntry() {
        if (m_addressBookEntry == null) {
            m_addressBookEntry = new AddressBookEntry();
        }
        return m_addressBookEntry;
    }

    public void setPermissionManager(PermissionManager permissionManager) {
        m_permissionManager = permissionManager;
    }

    public void setSipxImbotService(SipxImbotService sipxImbotService) {
        m_sipxImbotService = sipxImbotService;
    }

    public AddressBookEntry getAddressBookEntry() {
        return m_addressBookEntry;
    }

    /**
     * Need a getter method to return created address book entry in order to dinamically pass
     * address book entry attributes
     *
     * @return
     */
    public AddressBookEntry getCreatedAddressBookEntry() {
        useAddressBookEntry();
        if (m_addressBookEntry.getHomeAddress() == null) {
            m_addressBookEntry.setHomeAddress(new Address());
        }
        if (m_addressBookEntry.getOfficeAddress() == null) {
            m_addressBookEntry.setOfficeAddress(new Address());
        }
        return m_addressBookEntry;
    }

    public void setAddressBookEntry(AddressBookEntry addressBook) {
        m_addressBookEntry = addressBook;
    }

    public boolean getIsShared() {
        return m_isShared;
    }

    public void setIsShared(boolean isShared) {
        m_isShared = isShared;
    }

    public boolean hasImAccount() {
        return isNotEmpty(getImId());
    }

    public String getMusicOnHoldUri() {
        String mohAudioSource = getSettings().getSetting(MOH_AUDIO_SOURCE_SETTING).getValue();

        switch (MohAudioSource.parseSetting(mohAudioSource)) {
        case FILES_SRC:
            return m_musicOnHoldManager.getLocalFilesMohUri();
        case PERSONAL_FILES_SRC:
            return m_musicOnHoldManager.getPersonalMohFilesUri(getName());
        case SOUNDCARD_SRC:
            return m_musicOnHoldManager.getPortAudioMohUri();
        case SYSTEM_DEFAULT:
            return m_musicOnHoldManager.getDefaultMohUri();
        case NONE:
            return m_musicOnHoldManager.getNoneMohUri();
        default:
            return m_musicOnHoldManager.getDefaultMohUri();
        }
    }

    public void setEmailAddress(String emailAddress) {
        useAddressBookEntry().setEmailAddress(emailAddress);
    }

    public String getEmailAddress() {
        if (m_addressBookEntry == null) {
            return null;
        }
        return m_addressBookEntry.getEmailAddress();
    }

    public void setAlternateEmailAddress(String emailAddress) {
        useAddressBookEntry().setAlternateEmailAddress(emailAddress);
    }

    public String getAlternateEmailAddress() {
        if (m_addressBookEntry == null) {
            return null;
        }
        return m_addressBookEntry.getAlternateEmailAddress();
    }

    public boolean requestToAddMyAssistantToRoster() {
        boolean result;
        String paAddress = m_sipxImbotService.getAddress();
        String httpPort = m_sipxImbotService.getHttpPort();

        String uri = String.format("http://%s:%s/IM/%s/addToRoster", paAddress, httpPort, m_userName);

        Request request = new Request(Method.PUT, uri);

        Response response = m_restClient.handle(request);

        if (!response.getStatus().equals(Status.SUCCESS_OK)) {
            result = false;
        } else {
            result = true;
        }

        return result;
    }

    /**
     * Determines if the passed group is available
     */
    @Override
    public boolean isGroupAvailable(Group group) {
        if (getBranch() != null && group.getBranch() != null
                && !StringUtils.equals(getBranch().getName(), group.getBranch().getName())) {
            return false;
        } else {
            return super.isGroupAvailable(group);
        }
    }

    public static class AbstractUserDefaults {
        private final PermissionManager m_permissionManager;

        public AbstractUserDefaults(PermissionManager permissionManager) {
            m_permissionManager = permissionManager;
        }

        @SettingEntry(path = CALLFWD_TIMER)
        public String getDefaultInitDelay() {
            return m_permissionManager.getDefaultInitDelay();
        }
    }

    public void setDomainManager(DomainManager domainManager) {
        m_domainManager = domainManager;
    }

}
