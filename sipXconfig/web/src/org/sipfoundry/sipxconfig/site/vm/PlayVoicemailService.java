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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.asset.AssetService;
import org.apache.tapestry.engine.ILink;
import org.apache.tapestry.engine.state.ApplicationStateManager;
import org.apache.tapestry.services.LinkFactory;
import org.apache.tapestry.util.ContentType;
import org.apache.tapestry.web.WebResponse;
import org.sipfoundry.sipxconfig.address.Address;
import org.sipfoundry.sipxconfig.common.CoreContext;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.site.UserSession;
import org.sipfoundry.sipxconfig.vm.MailboxManager;

/**
 * Send voicemail file to user and mark it as read
 */
public class PlayVoicemailService extends AssetService {

    public static final String SERVICE_NAME = "playvoicemail";

    private static final Log LOG = LogFactory.getLog(PlayVoicemailService.class);

    private static final String MESSAGE_ID = "message_id";

    private static final String FOLDER = "folder";

    private MailboxManager m_mailboxManager;

    private LinkFactory m_linkFactory;

    private WebResponse m_response;

    private CoreContext m_coreContext;

    private ApplicationStateManager m_stateManager;

    public String getName() {
        return SERVICE_NAME;
    }

    /**
     * The only parameter is the service parameters[dirName, fileName]
     */
    public void service(IRequestCycle cycle) throws IOException {
        Address addressCache = m_mailboxManager.getLastGoodIvrNode();
        OutputStream responseOutputStream = m_response.getOutputStream(new ContentType("audio/x-wav"));
        if (addressCache != null) {
            try {
                copyVoicemail(responseOutputStream,
                        m_mailboxManager.getMediaFileURL(
                                addressCache,
                                getUserName(),
                                cycle.getParameter(FOLDER),
                                cycle.getParameter(MESSAGE_ID)), cycle);
                IOUtils.closeQuietly(responseOutputStream);
                return;
            } catch (Exception ex) {
                //do not throw exception as we have to iterate again through all urls
                LOG.warn("Cannot play voicemail on: "
                        + addressCache + " for reason: " + ex.getMessage());
            }
        }
        List<String> strUrls = m_mailboxManager.getMediaFileURLs(getUserName(), cycle.getParameter(FOLDER),
                cycle.getParameter(MESSAGE_ID));
        for (String url : strUrls) {
            try {
                copyVoicemail(responseOutputStream, url, cycle);
                break;
            } catch (Exception ex) {
                //if exception is thrown, continue to iterate...
                LOG.warn("Cannot play voicemail on node: "
                        + url + " for following reason: " + ex.getMessage());
            }
        }
        IOUtils.closeQuietly(responseOutputStream);
    }

    private void copyVoicemail(OutputStream responseOutputStream, String url, IRequestCycle cycle) throws Exception {
        InputStream stream = null;
        try {
            URL voicemailUrl = new URL(url);
            stream = voicemailUrl.openStream();
            IOUtils.copy(stream, responseOutputStream);
        } finally {
            IOUtils.closeQuietly(stream);
        }
        m_mailboxManager.markRead(getUserName(), cycle.getParameter(MESSAGE_ID));
    }

    public static class Info {
        private String m_messageId;
        private String m_folderId;
        private String m_userId;

        public Info(Object[] serviceParameters) {
            m_messageId = (String) serviceParameters[1];
        }

        public Info(String folderId, String messageId, String userId) {
            m_folderId = folderId;
            m_messageId = messageId;
            m_userId = userId;
        }

        public String getFolderId() {
            return m_folderId;
        }

        public String getMessageId() {
            return m_messageId;
        }

        public String getUserId() {
            return m_userId;
        }

    }

    public ILink getLink(boolean post, Object parameter) {
        requireUserId();
        Info info = (Info) parameter;
        Map<String, String> params = new HashMap<String, String>();
        params.put(MESSAGE_ID, ((Info) info).getMessageId());
        params.put(FOLDER, ((Info) info).getFolderId());
        return m_linkFactory.constructLink(this, post, params, false);
    }

    public void setMailboxManager(MailboxManager mailboxManager) {
        m_mailboxManager = mailboxManager;
    }

    public void setResponse(WebResponse response) {
        m_response = response;
    }

    public void setLinkFactory(LinkFactory linkFactory) {
        m_linkFactory = linkFactory;
    }

    public void setStateManager(ApplicationStateManager stateManager) {
        m_stateManager = stateManager;
    }

    public void setCoreContext(CoreContext coreContext) {
        m_coreContext = coreContext;
    }

    private String getUserName() {
        Integer userId = getUserId();
        if (userId == null) {
            return null;
        }

        User user = m_coreContext.loadUser(userId);
        return user.getUserName();
    }

    private Integer getUserId() {
        if (m_stateManager.exists(UserSession.SESSION_NAME)) {
            UserSession userSession = (UserSession) m_stateManager.get(UserSession.SESSION_NAME);
            return userSession.getUserId();
        }
        return null;
    }

    private void requireUserId() {
        if (getUserId() == null) {
            throw new RuntimeException("You have to be logged in to generate download links.");
        }
    }

}
