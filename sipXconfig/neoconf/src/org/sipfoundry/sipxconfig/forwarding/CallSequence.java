/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */
package org.sipfoundry.sipxconfig.forwarding;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.sipfoundry.sipxconfig.callgroup.AbstractCallSequence;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.commserver.imdb.AliasMapping;

/**
 * CallSequence
 */
public class CallSequence extends AbstractCallSequence {
    public static final String CALL_FWD_TIMER_SETTING = "callfwd/timer";

    private User m_user;
    private boolean m_withVoicemail;

    public CallSequence() {
        // empty default constructor
    }

    /**
     * @param calls list of ring objects
     * @param user original phone number
     * @param withVoicemail
     */
    CallSequence(List calls, User user, boolean withVoicemail) {
        m_user = user;
        m_withVoicemail = withVoicemail;
        setRings(calls);
    }

    public Ring insertRing() {
        Ring ring = new Ring();
        ring.setCallSequence(this);
        insertRing(ring);
        return ring;
    }

    public User getUser() {
        return m_user;
    }

    public void setUser(User user) {
        m_user = user;
    }

    public boolean isWithVoicemail() {
        return m_withVoicemail;
    }

    public void setWithVoicemail(boolean withVoicemail) {
        m_withVoicemail = withVoicemail;
    }

    public int getCfwdTime() {
        return (Integer) m_user.getSettingTypedValue(CALL_FWD_TIMER_SETTING);
    }

    public void setCfwdTime(int cfwdtime) {
        m_user.setSettingTypedValue(CALL_FWD_TIMER_SETTING, cfwdtime);
    }

    @Override
    public void insertRings(Collection rings) {
        super.insertRings(rings);
        for (Iterator iter = rings.iterator(); iter.hasNext();) {
            Ring ring = (Ring) iter.next();
            ring.setCallSequence(this);
        }
    }

    public Collection<AliasMapping> getAliasMappings(String domain) {
        String identity = m_user.getUserName();
        // pass true to never route this to voicemail
        Collection<AliasMapping> mappings = generateAliases(identity, domain, true);
        return mappings;
    }

    @Override
    public String toString() {
        return "CallSequence [m_user=" + m_user + ", m_withVoicemail=" + m_withVoicemail + ", getCfwdTime()="
            + getCfwdTime() + ", getRings()=" + getRings() + "]";
    }

}
