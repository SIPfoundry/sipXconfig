/*
 *
 *
 * Copyright (C) 2009 Nortel, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */
package org.sipfoundry.sipxconfig.moh;

import java.io.File;

import org.sipfoundry.sipxconfig.admin.dialplan.DialingRuleProvider;
import org.sipfoundry.sipxconfig.alias.AliasOwner;
import org.sipfoundry.sipxconfig.common.User;

public interface MusicOnHoldManager extends DialingRuleProvider, AliasOwner {

    String getAudioDirectoryPath();

    boolean isAudioDirectoryEmpty();

    void replicateMohConfiguration();

    String getDefaultMohUri();

    String getLocalFilesMohUri();

    String getPortAudioMohUri();

    String getNoneMohUri();

    String getPersonalMohFilesUri(String userName);

    File getUserAudioDirectory(User user);

    String getPortAudioMohUriMapping();

    String getLocalFilesMohUriMapping();

    String getNoneMohUriMapping();
}
