package org.sipfoundry.sipxconfig.phone.generic;

import java.util.Map;

import org.sipfoundry.sipxconfig.device.ProfileContext;

public class GenericPhoneProfileContext extends ProfileContext<GenericPhone> {

    public GenericPhoneProfileContext(GenericPhone device, String profileTemplate) {
        super(device, profileTemplate);
    }

    @Override
    public Map<String, Object> getContext() {
        Map<String, Object> context = super.getContext();
        return context;
    }
}
