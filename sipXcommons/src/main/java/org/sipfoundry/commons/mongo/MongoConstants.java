/**
 *
 *
 * Copyright (c) 2012 eZuce, Inc. All rights reserved.
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
package org.sipfoundry.commons.mongo;


public interface MongoConstants {
    static final String NODE_COLLECTION = "node";
    static final String STUNNEL_COLLECTION = "registrarnode";
    static final String ENTITY_COLLECTION = "entity";

    static final String ALIASES = "als";
    static final String ALIAS = "alias";
    static final String ID = "_id";
    static final String ALIAS_ID = "id";
    static final String CONTACT = "cnt";
    static final String RELATION = "rln";
    static final String USERBUSYPROMPT = "bsyprmpt";
    static final String VOICEMAILTUI = "vcmltui";
    static final String EMAIL = "email";
    static final String NOTIFICATION = "notif";
    static final String ATTACH_AUDIO = "attaudio";
    static final String ALT_EMAIL = "altemail";
    static final String ALT_NOTIFICATION = "altnotif";
    static final String ALT_ATTACH_AUDIO = "altattaudio";
    static final String SYNC = "synch";
    static final String HOST = "host";
    static final String PORT = "port";
    static final String TLS = "tls";
    static final String ACCOUNT = "acnt";
    static final String PASSWD = "pswd";
    static final String DISPLAY_NAME = "dspl";
    static final String HASHED_PASSTOKEN = "hshpstk";
    static final String IM_ENABLED = "imenbld";
    static final String IM_ID = "imid";
    static final String IM_DISPLAY_NAME = "imdn";
    static final String ALT_IM_ID = "altimid";
    static final String IM_PASSWORD = "impwd";
    static final String IM_ON_THE_PHONE_MESSAGE = "onphnmsg";
    static final String IM_ADVERTISE_ON_CALL_STATUS = "advcllsts";
    static final String IM_SHOW_ON_CALL_DETAILS = "clldttls";
    static final String JOB_TITLE = "jbttl";
    static final String JOB_DEPT = "jbdpt";
    static final String COMPANY_NAME = "cmpnm";
    static final String ASSISTANT_NAME = "astnm";
    static final String ASSISTANT_PHONE = "astph";
    static final String FAX_NUMBER = "fax";
    static final String LOCATION = "loctn"; // this is different than loc field in user
                                                    // location db
    static final String HOME_PHONE_NUMBER = "hmph";
    static final String CELL_PHONE_NUMBER = "cell";
    static final String AVATAR = "avt";
    static final String HOME_STREET = "hstr";
    static final String HOME_CITY = "hcty";
    static final String HOME_COUNTRY = "hcntry";
    static final String HOME_STATE = "hstate";
    static final String HOME_ZIP = "hzip";
    static final String OFFICE_STREET = "ostr";
    static final String OFFICE_CITY = "octy";
    static final String OFFICE_COUNTRY = "ocntry";
    static final String OFFICE_STATE = "ostate";
    static final String OFFICE_ZIP = "ozip";
    static final String OFFICE_DESIGNATION = "odsgn";
    static final String CONF_ENTRY_IM = "cnfentry";
    static final String CONF_EXIT_IM = "cnfexit";
    static final String LEAVE_MESSAGE_BEGIN_IM = "lvmsgbeg";
    static final String LEAVE_MESSAGE_END_IM = "lvmsgend";
    static final String CALLERALIAS = "clrid";
    static final String IGNORE_USER_CID = "ignorecid";
    static final String CID_PREFIX = "pfix";
    static final String KEEP_DIGITS = "kpdgts";
    static final String TRANSFORM_EXT = "trnsfrmext";
    static final String ANONYMOUS = "blkcid";
    static final String URL_PARAMS = "url";
    static final String REALM = "rlm";
    static final String PASSTOKEN = "pstk";
    static final String PINTOKEN = "pntk";
    static final String AUTHTYPE = "authtp";
    static final String IDENTITY = "ident";
    static final String UID = "uid";
    static final String GROUPS = "gr";
    static final String VALID_USER = "vld";
    static final String CONF_EXT = "cnfext";
    static final String CONF_NAME = "cnfnm";
    static final String CONF_OWNER = "cnfown";
    static final String CONF_PIN = "cnfpin";
    static final String PERMISSIONS = "prm";
    static final String CFWDTIME = "cfwdtm";
    static final String USER_LOCATION = "loc";
    static final String STATIC = "stc";
    static final String EVENT = "evt";
    static final String FROM_URI = "from";
    static final String TO_URI = "to";
    static final String CALLID = "cid";
    static final String VMONDND = "vmondnd";
    //speed dials
    static final String SPEEDDIAL = "spdl";
    static final String USER = "usr";
    static final String USER_CONS = "usrcns";
    static final String URI = "uri";
    static final String NAME = "name";
    static final String BUTTONS = "btn";
    //node registrar
    static final String SERVER = "server";
    static final String INTERNAL_ADDRESS = "internalAddress";
    static final String ENABLED = "active";
    static final String TIMESTAMP = "lastUpdated";
    static final String NEXT_NODE = "next";
    //MAILSTORE
    static final String OPERATOR = "opr";
    static final String LANGUAGE = "lng";
    static final String DIALPAD = "dlpd";
    static final String PERSONAL_ATT = "pa";
    static final String ACTIVEGREETING = "actvgr";
    static final String ITEM = "itm";
    static final String DISTRIB_LISTS = "dlst";
    //AUTH CODE
    static final String AUTH_CODE = "authc";
    //OPENACD
    static final String QUEUE_GROUP = "qgrp";
    static final String SKILLS = "skl";
    static final String PROFILES = "prfl";
    static final String WEIGHT = "wht";
    static final String OLD_NAME = "oldnm";
    static final String ACTION = "actn";
    static final String ACTION_VALUE = "actionValue";
    static final String CONDITION = "cndt";
    static final String FREQUENCY = "frq";
    static final String STEP_NAME = "stpnm";
    static final String RECIPES = "rcps";
    
    static final String PIN = "pin";
    static final String AGENT_GROUP = "aggrp";
    static final String SKILLS_ATOM = "sklatm";
    static final String QUEUES = "qs";
    static final String CLIENTS = "clns";
    static final String FIRST_NAME = "fnm";
    static final String LAST_NAME = "lnm";
    static final String SECURITY = "scrty";
    
    static final String VALUE = "vlu";
    static final String NODE = "nd";
    static final String DIAL_STRING = "dlst";
    static final String LISTENER_ENABLED = "lstenbl";
    static final String LOG_LEVEL = "lglvl";
    static final String LOG_FILE = "lgfile";
    static final String ATOM = "atom";
    static final String GROUP_NAME = "grpnm";
    static final String DESCRIPTION = "dscr";
    static final String UUID = "uuid";
    static final String TYPE = "type";
    static final String LABEL = "lbl";
    static final String BIAS = "bias";
}
