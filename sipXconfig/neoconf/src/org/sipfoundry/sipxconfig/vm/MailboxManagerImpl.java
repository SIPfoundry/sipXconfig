/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */
package org.sipfoundry.sipxconfig.vm;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.sipfoundry.sipxconfig.common.CoreContext;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.common.UserException;
import org.sipfoundry.sipxconfig.common.event.UserDeleteListener;
import org.sipfoundry.sipxconfig.permission.PermissionName;
import org.sipfoundry.sipxconfig.vm.attendant.PersonalAttendant;
import org.sipfoundry.sipxconfig.vm.attendant.PersonalAttendantWriter;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class MailboxManagerImpl extends HibernateDaoSupport implements MailboxManager {
    private static final String MESSAGE_SUFFIX = "-00.xml";
    private static final FilenameFilter MESSAGE_FILES = new SuffixFileFilter(MESSAGE_SUFFIX);
    private File m_mailstoreDirectory;
    private MailboxPreferencesWriter m_mailboxPreferencesWriter;
    private DistributionListsReader m_distributionListsReader;
    private DistributionListsWriter m_distributionListsWriter;
    private PersonalAttendantWriter m_personalAttendantWriter;
    private CoreContext m_coreContext;

    public boolean isEnabled() {
        return m_mailstoreDirectory != null && m_mailstoreDirectory.exists();
    }

    public DistributionList[] loadDistributionLists(Mailbox mailbox) {
        File file = mailbox.getDistributionListsFile();
        DistributionList[] lists = m_distributionListsReader.readObject(file);
        if (lists == null) {
            lists = DistributionList.createBlankList();
        }
        return lists;
    }

    public void saveDistributionLists(Mailbox mailbox, DistributionList[] lists) {
        Collection<String> aliases = DistributionList.getUniqueExtensions(lists);
        m_coreContext.checkForValidExtensions(aliases, PermissionName.VOICEMAIL);
        File file = mailbox.getDistributionListsFile();
        m_distributionListsWriter.writeObject(lists, file);
    }

    public List<Voicemail> getVoicemail(Mailbox mbox, String folder) {
        checkMailstoreDirectory();
        File vmdir = new File(mbox.getUserDirectory(), folder);
        String[] wavs = vmdir.list(MESSAGE_FILES);
        if (wavs == null) {
            return Collections.emptyList();
        }
        Arrays.sort(wavs);
        List<Voicemail> vms = new ArrayList(wavs.length);
        for (String wav : wavs) {
            String basename = basename(wav);
            vms.add(new Voicemail(m_mailstoreDirectory, mbox.getUserId(), folder, basename));
        }
        return vms;
    }

    /**
     * Mark voicemail as read
     */
    public void markRead(Mailbox mailbox, Voicemail voicemail) {
        // FIXME: need to have a way of marking voicemail as read
    }

    public void move(Mailbox mailbox, Voicemail voicemail, String destinationFolderId) {
        File destination = new File(mailbox.getUserDirectory(), destinationFolderId);
        for (File f : voicemail.getAllFiles()) {
            f.renameTo(new File(destination, f.getName()));
        }
    }

    public void delete(Mailbox mailbox, Voicemail voicemail) {
        for (File f : voicemail.getAllFiles()) {
            f.delete();
        }
    }

    /**
     * Because in HA systems, admin may change mailstore directory, validate it
     */
    void checkMailstoreDirectory() {
        if (m_mailstoreDirectory == null) {
            throw new MailstoreMisconfigured(null);
        }
        if (!m_mailstoreDirectory.exists()) {
            throw new MailstoreMisconfigured(m_mailstoreDirectory.getAbsolutePath());
        }
    }

    static class MailstoreMisconfigured extends UserException {
        MailstoreMisconfigured() {
            super("Mailstore directory configuration setting is missing.");
        }

        MailstoreMisconfigured(String dir) {
            super(String.format("Mailstore directory does not exist '%s'", dir));
        }

        MailstoreMisconfigured(String message, IOException cause) {
            super(message, cause);
        }
    }

    /**
     * extract file name w/o ext.
     */
    static String basename(String filename) {
        int suffix = filename.lastIndexOf(MESSAGE_SUFFIX);
        return suffix >= 0 ? filename.substring(0, suffix) : filename;
    }

    public String getMailstoreDirectory() {
        return m_mailstoreDirectory.getPath();
    }

    public void setMailstoreDirectory(String mailstoreDirectory) {
        m_mailstoreDirectory = new File(mailstoreDirectory);
    }

    public Mailbox getMailbox(String userId) {
        return new Mailbox(m_mailstoreDirectory, userId);
    }

    public void deleteMailbox(String userId) {
        Mailbox mailbox = getMailbox(userId);
        mailbox.deleteUserDirectory();
    }

    public void saveMailboxPreferences(User user) {
        Mailbox mailbox = getMailbox(user.getName());
        File file = mailbox.getVoicemailPreferencesFile();
        m_mailboxPreferencesWriter.writeObject(new MailboxPreferences(user), file);
    }

    public static class YesNo {
        public String encode(Object o) {
            return Boolean.TRUE.equals(o) ? "yes" : "no";
        }
    }

    public void setMailboxPreferencesWriter(MailboxPreferencesWriter mailboxWriter) {
        m_mailboxPreferencesWriter = mailboxWriter;
    }

    public void setDistributionListsReader(DistributionListsReader distributionListsReader) {
        m_distributionListsReader = distributionListsReader;
    }

    public void setDistributionListsWriter(DistributionListsWriter distributionListsWriter) {
        m_distributionListsWriter = distributionListsWriter;
    }

    public void setCoreContext(CoreContext coreContext) {
        m_coreContext = coreContext;
    }

    public void setPersonalAttendantWriter(PersonalAttendantWriter personalAttendantWriter) {
        m_personalAttendantWriter = personalAttendantWriter;
    }

    public PersonalAttendant loadPersonalAttendantForUser(User user) {
        PersonalAttendant pa = findPersonalAttendant(user);
        if (pa == null) {
            pa = new PersonalAttendant();
            pa.setUser(user);
            getHibernateTemplate().save(pa);
        }
        return pa;
    }

    public void removePersonalAttendantForUser(User user) {
        PersonalAttendant pa = findPersonalAttendant(user);
        if (pa != null) {
            getHibernateTemplate().delete(pa);
        }
    }

    public void storePersonalAttendant(PersonalAttendant pa) {
        getHibernateTemplate().saveOrUpdate(pa);
        writePersonalAttendant(pa);
    }

    public void clearPersonalAttendants() {
        List<PersonalAttendant> allPersonalAttendants = getHibernateTemplate().loadAll(PersonalAttendant.class);
        getHibernateTemplate().deleteAll(allPersonalAttendants);
    }

    public void writeAllPersonalAttendants() {
        List<PersonalAttendant> all = getHibernateTemplate().loadAll(PersonalAttendant.class);
        for (PersonalAttendant pa : all) {
            writePersonalAttendant(pa);
        }
    }

    private void writePersonalAttendant(PersonalAttendant pa) {
        Mailbox mailbox = getMailbox(pa.getUser().getUserName());
        m_personalAttendantWriter.write(mailbox, pa);
    }

    private PersonalAttendant findPersonalAttendant(User user) {
        Collection pas = getHibernateTemplate().findByNamedQueryAndNamedParam("personalAttendantForUser", "user",
                user);
        return (PersonalAttendant) DataAccessUtils.singleResult(pas);
    }

    public UserDeleteListener createUserDeleteListener() {
        return new OnUserDelete();
    }

    private class OnUserDelete extends UserDeleteListener {
        @Override
        protected void onUserDelete(User user) {
            removePersonalAttendantForUser(user);
        }
    }
}
