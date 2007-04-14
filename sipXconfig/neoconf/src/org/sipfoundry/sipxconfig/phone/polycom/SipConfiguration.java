/*
 * 
 * 
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.  
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 * 
 * $
 */
package org.sipfoundry.sipxconfig.phone.polycom;

import java.util.Collection;
import java.util.Map;

import org.sipfoundry.sipxconfig.device.ProfileContext;
import org.sipfoundry.sipxconfig.setting.PatternSettingFilter;
import org.sipfoundry.sipxconfig.setting.Setting;
import org.sipfoundry.sipxconfig.setting.SettingUtil;

/**
 * Responsible for generating ipmid.cfg
 */
public class SipConfiguration extends ProfileContext {
    private static final String SIP_TEMPLATE = PolycomPhone.TEMPLATE_DIR + "/sip-%s.cfg.vm";
    private static PatternSettingFilter s_callSettings = new PatternSettingFilter();

    static {
        s_callSettings.addExcludes("call/donotdisturb.*$");
        s_callSettings.addExcludes("call/shared.*$");
    }

    public SipConfiguration(PolycomPhone device) {
        super(device, getSipTemplate(device));
    }

    public static String getSipTemplate(PolycomPhone device) {
        return String.format(SIP_TEMPLATE, device.getDeviceVersion().getVersionId());
    }

    @Override
    public Map<String, Object> getContext() {
        Map<String, Object> context = super.getContext();
        Setting endpointSettings = getDevice().getSettings();
        Setting call = endpointSettings.getSetting(PolycomPhone.CALL);
        Collection items = SettingUtil.filter(s_callSettings, call);
        context.put(PolycomPhone.CALL, items);
        return context;
    }
}
