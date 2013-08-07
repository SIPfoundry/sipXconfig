/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.site.vm;

import java.io.Serializable;

import org.apache.tapestry.engine.IEngineService;
import org.apache.tapestry.engine.ILink;
import org.sipfoundry.sipxconfig.common.UserException;
import org.sipfoundry.sipxconfig.vm.MailboxManager;
import org.sipfoundry.sipxconfig.vm.Voicemail;

public abstract class MailboxOperation implements Serializable {
    private String m_userId;
    private String m_folderId;
    private String m_messageId;

    MailboxOperation(String userId, String folderId, String messageId) {
        m_userId = userId;
        m_folderId = folderId;
        m_messageId = messageId;
    }

    public static MailboxOperation createMailboxOperationFromServletPath(String pathInfo) {
        String[] tokens = pathInfo.substring(1).split("/");
        if (tokens.length < 2) {
            throw new MalformedMailboxUrlException("Too few parameters for mailbox operation " + pathInfo);
        }
        if (tokens.length == 2) {
            return new ManageVoiceMail(tokens[0], tokens[1]);
        }
        if (tokens.length == 3 || "play".equals(tokens[3])) {
            return new PlayVoiceMail(tokens[0], tokens[1], tokens[2]);
        }
        if ("delete".equals(tokens[3])) {
            return new MoveVoiceMail(tokens[0], tokens[1], tokens[2], "deleted");
        }
        if ("save".equals(tokens[3])) {
            return new MoveVoiceMail(tokens[0], tokens[1], tokens[2], "saved");
        }

        throw new MalformedMailboxUrlException("Unknown mailbox operation " + tokens[3]);
    }

    public static class MalformedMailboxUrlException extends UserException {
        public MalformedMailboxUrlException(String message) {
            super(message);
        }
    }

    public abstract void operate(ManageVoicemail manageVoicemail);

    public String getMessageId() {
        return m_messageId;
    }

    public String getFolderId() {
        return m_folderId;
    }

    public String getUserId() {
        return m_userId;
    }

    static class ManageVoiceMail extends MailboxOperation {
        ManageVoiceMail(String userId, String folderId) {
            super(userId, folderId, null);
        }

        @Override
        public void operate(ManageVoicemail page) {
            // let ManageVoicemail page show folder
        }
    }

    static class MoveVoiceMail extends MailboxOperation {
        private String m_destinationFolderId;

        MoveVoiceMail(String userId, String folderId, String messageId, String destinationFolderId) {
            super(userId, folderId, messageId);
            m_destinationFolderId = destinationFolderId;
        }

        @Override
        public void operate(ManageVoicemail page) {
            MailboxManager mgr = page.getMailboxManager();
            Voicemail vm = mgr.getVoicemail(getUserId(), getFolderId(), getMessageId());
            mgr.move(getUserId(), vm, m_destinationFolderId);
        }
    }

    static class PlayVoiceMail extends MailboxOperation {
        PlayVoiceMail(String userId, String folderId, String messageId) {
            super(userId, folderId, messageId);
        }

        @Override
        public void operate(ManageVoicemail page) {
            // this works perfectly, but tells client to redirect. if service encoder
            // directed tapestry to the playvm service (or derivative) then we could
            // serve the file w/o redirect. I didn't want to couple MailboxOperation with
            // MailboxPageEncoder, but it certainly could be done.
            IEngineService playService = page.getPlayVoicemailService();
            MailboxManager mgr = page.getMailboxManager();
            Voicemail vm = mgr.getVoicemail(getUserId(), getFolderId(), getMessageId());
            PlayVoicemailService.Info info = new PlayVoicemailService.Info(vm.getFolderId(), vm.getMessageId(),
                    vm.getUserId(), vm.getAudioFormat());
            ILink link = playService.getLink(false, info);
            page.getRequestCycle().sendRedirect(link.getURL());
        }
    }
}
