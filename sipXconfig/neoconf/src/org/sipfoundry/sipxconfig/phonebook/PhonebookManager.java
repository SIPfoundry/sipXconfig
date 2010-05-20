/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */
package org.sipfoundry.sipxconfig.phonebook;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;

import org.sipfoundry.sipxconfig.common.DataObjectSource;
import org.sipfoundry.sipxconfig.common.User;

public interface PhonebookManager extends DataObjectSource<Phonebook> {
    public static final String CONTEXT_BEAN_NAME = "phonebookManager";
    public enum PhonebookFormat {
        VCARD("vcf"), CSV("csv");
        private final String m_name;

        PhonebookFormat(String name) {
            m_name = name;
        }
        public String getName() {
            return m_name;
        }
    }

    /**
     * Gets whether or not phonebook management is enabled.
     *
     * If phonebook management is disabled, no phonebook files will be generated for any phones
     * that use a separate phonebook file.
     */
    boolean getPhonebookManagementEnabled();

    Collection<Phonebook> getPhonebooks();

    void deletePhonebooks(Collection<Integer> ids);

    /**
     * Gets phonebooks created by the administrator for the provided user
     */
    Collection<Phonebook> getPublicPhonebooksByUser(User user);

    Phonebook getPhonebook(Integer phonebookId);

    Phonebook getPhonebookByName(String name);

    void savePhonebook(Phonebook phonebook);

    String getExternalUsersDirectory();

    Collection<PhonebookEntry> getEntries(Collection<Phonebook> phonebook, User user);

    PagedPhonebook getPagedPhonebook(Collection<Phonebook> phonebook, User user, String startRow, String endRow,
            String queryString);

    Collection<PhonebookEntry> getEntries(Phonebook phonebook);

    Collection<PhonebookEntry> search(Collection<Phonebook> phonebooks, String searchTerm, User user);

    void reset();

    void exportPhonebook(Collection<PhonebookEntry> entries, OutputStream out, PhonebookFormat format)
        throws IOException;

    int addEntriesFromFile(Integer phonebookId, InputStream in);

    int addEntriesFromGoogleAccount(Integer phonebookId, String account, String password);

    Phonebook getPrivatePhonebook(User user);

    Phonebook getPrivatePhonebookCreateIfRequired(User user);

    PhonebookEntry getPhonebookEntry(Integer phonebookFileEntryId);

    void savePhonebookEntry(PhonebookEntry entry);

    void updatePhonebookEntry(PhonebookEntry entry);

    void deletePhonebookEntry(PhonebookEntry entry);

    /**
     * Gets all phonebooks for the provided user
     */
    Collection<Phonebook> getAllPhonebooksByUser(User consumer);

    void updateFilePhonebookEntryInternalIds();

    // --> this methods will be removed after version update
    void removeTableColumns();

    Map<Integer, String[]> getPhonebookFilesName();

    // <--

    GoogleDomain getGoogleDomain();

    void saveGoogleDomain(GoogleDomain gd);

    void saveGeneralPhonebookSettings(GeneralPhonebookSettings generalPhonebookSettings);

    GeneralPhonebookSettings getGeneralPhonebookSettings();

    PhonebookEntry getDuplicatePhonebookEntry(PhonebookEntry newEntry, User user);

    /**
     * Gets all the entries of a phonebook and adds "everyone" if "everyone" is enabled
     */
    public Collection<PhonebookEntry> getAllEntries(int phonebookId);

    public Collection<PhonebookEntry> getEntries(int phonebookId);

    public void deletePrivatePhonebook(User user);

}
