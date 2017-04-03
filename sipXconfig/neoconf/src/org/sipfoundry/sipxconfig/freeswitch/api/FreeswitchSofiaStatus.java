package org.sipfoundry.sipxconfig.freeswitch.api;

import org.apache.commons.lang.StringUtils;

public class FreeswitchSofiaStatus {

    private Type m_type;
    private String m_name;
    private String m_data;
    private String m_state;
    
    public Type getType() {
        return m_type;
    }

    public void setType(Type type) {
        this.m_type = type;
    }

    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        this.m_name = name;
    }

    public String getData() {
        return m_data;
    }

    public void setData(String data) {
        this.m_data = data;
    }

    public String getState() {
        return m_state;
    }

    public void setState(String state) {
        this.m_state = state;
    }

    public enum Type
    {
        SOFIA_PROFILE("profile"),
        SOFIA_GATEWAY("gateway");
        
        private final String m_key;
        
        private Type(final String key) {
            m_key = key;
        }
        
        public String getKey() {
            return m_key;
        }
        
        public static Type fromString(String key) {
            for(Type type : values()) {
                if(StringUtils.equalsIgnoreCase(type.getKey(), key)) {
                    return type;
                }
            }
            
            throw new IllegalArgumentException(key + " is invalid/unsupported");
        }
    }
    
    public enum State
    {
        STATE_TRYING("TRYING", "Trying"),
        STATE_REGISTER("REGISTER", "Registering"),
        STATE_REGISTERED("REGED", "Registered"),
        STATE_UNREGISTER("UNREGISTER", "Unregistering"),
        STATE_UNREGISTERED("UNREGED", "Unregistered"),
        STATE_FAILED("FAILED", "Failed"),
        STATE_FAILED_WAIT("FAIL_WAIT", "Fail (Retrying)"),
        STATE_EXPIRED("EXPIRED", "Expired"),
        STATE_NO_REGISTRATION("NOREG", "No Registration");
        
        final String m_key;
        final String m_message;
        
        private State(final String key, final String message) {
            m_key = key;
            m_message = message;
        }
        
        public String getKey() {
            return m_key;
        }
        
        public String getMessage() {
            return m_message;
        }
        
        public static State fromString(String key) {
            for(State state : values()) {
                if(StringUtils.equalsIgnoreCase(state.getKey(), key)) {
                    return state;
                }
            }
            
            throw new IllegalArgumentException(key + " is invalid/unsupported");
        }
    }
    
}
