/**
 *
 *
 * Copyright (c) 2010 / 2011 eZuce, Inc. All rights reserved.
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
package org.sipfoundry.sipxconfig.admin.commserver.imdb;

import org.sipfoundry.sipxconfig.common.SipUri;

import static org.sipfoundry.commons.mongo.MongoConstants.*;

public class UserStaticMapping extends DataSetRecord {

    public UserStaticMapping(String domain, String username, String mwi) {
        String identity = username + "@" + domain;
        put(IDENTITY, identity);
        put(EVENT, "message-summary");
        put(CONTACT, SipUri.format(mwi, domain, false));
        put(FROM_URI, SipUri.format("IVR", domain, false));
        put(TO_URI, SipUri.format(username, domain, false));
        put(CALLID, "static-mwi-" + identity);
    }

}
