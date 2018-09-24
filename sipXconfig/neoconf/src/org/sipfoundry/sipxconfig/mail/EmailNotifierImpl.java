package org.sipfoundry.sipxconfig.mail;

import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.sipfoundry.commons.userdb.User;
import org.sipfoundry.commons.userdb.ValidUsers;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.MimeMessageHelper;

public class EmailNotifierImpl implements EmailNotifier {
    private static final Logger LOG = Logger.getLogger("org.sipfoundry.sipxconfig.emailer");
    private ExecutorService m_es;
    private Session m_session;
    private MessageSource m_messages;
    private ValidUsers m_validUsers;

    public void init() {
        m_es = Executors.newCachedThreadPool();
        // Setup mail server
        Properties props = System.getProperties();
        props.put("mail.smtp.host", "localhost");
        props.put("mail.smtp.user", "postmaster"); // TODO get from ivrConfig
        m_session = Session.getDefaultInstance(props, null);
    }
    
    public void sendMail(String username, String prefixResource, Object args[]) {
        User user = m_validUsers.getUser(username);
        if (user != null) {
            String to = user.getEmailAddress();
            String alt = user.getAltEmailAddress();
            if (!StringUtils.isBlank(to) || !StringUtils.isBlank(alt)) {
                LOG.info("EmailerNotifier::sendMail queuing e-mail for " + user.getIdentity());
                try {
                    BackgroundMailer bm = new BackgroundMailer();
                    bm.init(user, prefixResource, args);
                    submit(bm);    
                } catch(Exception ex) {
                    LOG.error("EmailerNotifier::sendMail failed to queue email for " + user.getIdentity(), ex);
                }
                       
            }
        }
    }
    
    public void submit(BackgroundMailer bm) {
        m_es.submit(bm);
    }
    
    /**
     * The Runnable class that builds and sends the e-mail
     */
    class BackgroundMailer implements Runnable {
        private User m_user;
        private String m_prefixResource;
        private Object[] m_args;

        public BackgroundMailer() {

        }
        
        public void init(User user, String prefixResource, Object args[]) {
            m_user = user;
            m_prefixResource = prefixResource;
            
            Object[] tempArgs =new Object[8];
            if (args != null && args.length > 0) {
                tempArgs = ArrayUtils.addAll(tempArgs, args);
            }
            
             // Build original set of args

            tempArgs[0] = new Date();
            tempArgs[1] = fmt(prefixResource + ".SenderName", tempArgs);
            tempArgs[2] = fmt(prefixResource + ".SenderMailto", tempArgs);
            tempArgs[3] = fmt(prefixResource + ".HtmlTitle", tempArgs);
            tempArgs[4] = fmt(prefixResource + ".Sender", tempArgs);
            tempArgs[5] = fmt(prefixResource + ".Subject", tempArgs);
            tempArgs[6] = fmt(prefixResource + ".HtmlBody", tempArgs);
            tempArgs[7] = fmt(prefixResource + ".TextBody", tempArgs);
            m_args = tempArgs;
        }
        
        javax.mail.Message buildMessage() throws AddressException,
                MessagingException, IOException {
            MimeMessage message = new MimeMessage(m_session);
            message.setFrom(new InternetAddress(getSender()));
        
            message.setSubject(getSubject(), "UTF-8");
       
            message.addHeader("X-SIPX-MBXID", m_user.getUserName());
        
            String htmlBody = getHtmlBody();
            String textBody = getTextBody();
            
            // Use multipart
            if (htmlBody != null && htmlBody.length() > 0) {
                MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED);
        
                if (textBody != null && textBody.length() > 0) {
                    // Add the text part of the message first
                    helper.setText(textBody); // UTF-8 in case there's Unicode in there
                }
                
                if (htmlBody != null && htmlBody.length() > 0) {
                    // Add the HTML part of the message
                    helper.setText(htmlBody, true);
                }
                
            } else {
                if (textBody != null && textBody.length() > 0) {
                    // Just a text part, use a simple message
                    message.setText(textBody, "UTF-8");
                }
            }
            return message;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            String to = m_user.getEmailAddress();
            String alt = m_user.getAltEmailAddress();

            LOG.debug("EmailerNotifier::run started (" + m_prefixResource + ")");
            
            // Send to the main e-mail address
            if (!StringUtils.isBlank(to)) {
                try {
                    LOG.info(String.format("EmailerNotifier::run sending notification email to %s", to));
                    javax.mail.Message message = buildMessage();
                    message.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress(to));
                    Transport.send(message);
                } catch (Exception e) {
                    LOG.error("Emailer::run problem sending email.", e);
                }
            }

            // Send to the alternate e-mail address
            if (!StringUtils.isBlank(alt)) {
                try {
                    LOG.info(String.format("EmailerNotifier::run sending notification email to %s", alt));
                    javax.mail.Message message = buildMessage();
                    message.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress(alt));
                    Transport.send(message);
                } catch (Exception e) {
                    LOG.error("Emailer::run problem sending alternate email.", e);
                }
            }
            
            LOG.debug("EmailerNotifier::run finished (" + m_prefixResource + ")");
        }
        
        private String fmt(String text, Object[] args) {
            String value = "";
            if (text == null) {
                return value;
            }
            return m_messages.getMessage(text, args, "", Locale.getDefault());
        }
        
        private String getSender() {
            return fmt(m_prefixResource + ".Sender", m_args);
        }
        
        private String getSubject() {
            return fmt(m_prefixResource + ".Subject", m_args);
        }
        
        private String getHtmlBody() {
            return fmt(m_prefixResource + ".HtmlBody", m_args);
        }
        
        private String getTextBody() {
            return fmt(m_prefixResource + ".TextBody", m_args);
        }
    }
    
    public void setValidUsers(ValidUsers validUsers) {
        m_validUsers = validUsers;
    }
    
    public void setMessages(MessageSource messages) {
        m_messages = messages;
    }
}
