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
package org.sipfoundry.sipxconfig.openacd;

import java.util.LinkedList;
import java.util.List;

import org.sipfoundry.sipxconfig.admin.commserver.Location;
import org.sipfoundry.sipxconfig.freeswitch.FreeswitchAction;

public class OpenAcdLine extends OpenAcdExtension {
    public static final String Q = "queue=";
    public static final String BRAND = "brand=";
    public static final String DESTINATION_NUMBER = "destination_number";
    public static final String ALLOW_VOICEMAIL = "allow_voicemail=";
    public static final String EMPTY_STRING = "";
    public static final String OPEN_ACD = "openacd@";

    public static List<FreeswitchAction> getDefaultActions(Location location) {
        List<FreeswitchAction> actions = new LinkedList<FreeswitchAction>();
        actions.add(createAction(FreeswitchAction.PredefinedAction.answer.toString(), null));
        actions.add(createAction(FreeswitchAction.PredefinedAction.set.toString(), "domain_name=$${domain}"));
        actions.add(createAction(FreeswitchAction.PredefinedAction.set.toString(), Q));
        actions.add(createAction(FreeswitchAction.PredefinedAction.set.toString(), "allow_voicemail=true"));
        actions.add(createAction(FreeswitchAction.PredefinedAction.erlang_sendmsg.toString(),
                "freeswitch_media_manager  " + OPEN_ACD + location.getFqdn() + " inivr ${uuid}"));
        actions.add(createAction(FreeswitchAction.PredefinedAction.playback.toString(), EMPTY_STRING));
        actions.add(createAction(FreeswitchAction.PredefinedAction.erlang.toString(),
                "freeswitch_media_manager:!  " + OPEN_ACD + location.getFqdn()));
        return actions;
    }

    public static FreeswitchAction createVoicemailAction(boolean allow) {
        return createAction(FreeswitchAction.PredefinedAction.set.toString(), ALLOW_VOICEMAIL + allow);
    }

    public static FreeswitchAction createQueueAction(String queue) {
        return createAction(FreeswitchAction.PredefinedAction.set.toString(), Q + queue);
    }

    public static FreeswitchAction createClientAction(String client) {
        return createAction(FreeswitchAction.PredefinedAction.set.toString(), BRAND + client);
    }

    public static FreeswitchAction createPlaybackAction(String path) {
        return createAction(FreeswitchAction.PredefinedAction.playback.toString(), path);
    }
}
