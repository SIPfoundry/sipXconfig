package org.sipfoundry.sipxconfig.mail;

public interface EmailNotifier {
    public void sendMail(String username, String prefixResource, Object args[]);
}
