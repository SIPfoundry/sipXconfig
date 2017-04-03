package org.sipfoundry.sipxconfig.freeswitch.api;

import org.apache.commons.lang.StringUtils;

public class FreeswitchSofiaStatus {

    private Type m_type;
    private String m_name;
    private String m_data;
    private Status m_status;
    
    public Type getType() {
        return m_type;
    }

    public void setType(Type m_type) {
        this.m_type = m_type;
    }

    public String getName() {
        return m_name;
    }

    public void setName(String m_name) {
        this.m_name = m_name;
    }

    public String getData() {
        return m_data;
    }

    public void setData(String m_data) {
        this.m_data = m_data;
    }

    public Status getStatus() {
        return m_status;
    }

    public void setStatus(Status m_status) {
        this.m_status = m_status;
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
    
    public enum Status
    {
        STATUS_TRYING("TRYING", "Trying"),
        STATUS_REGISTER("REGISTER", "Registering"),
        STATUS_REGISTERED("REGED", "Registered"),
        STATUS_UNREGISTER("UNREGISTER", "Unregistering"),
        STATUS_UNREGISTERED("UNREGED", "Unregistered"),
        STATUS_FAILED("FAILED", "Failed"),
        STATUS_FAILED_WAIT("FAIL_WAIT", "Fail (Retrying)"),
        STATUS_EXPIRED("EXPIRED", "Expired"),
        STATUS_NO_REGISTRATION("NOREG", "No Registration");
        
        final String m_key;
        final String m_message;
        
        private Status(final String key, final String message) {
            m_key = key;
            m_message = message;
        }
        
        public String getKey() {
            return m_key;
        }
        
        public String getMessage() {
            return m_message;
        }
        
        public static Status fromString(String key) {
            for(Status status : values()) {
                if(StringUtils.equalsIgnoreCase(status.getKey(), key)) {
                    return status;
                }
            }
            
            throw new IllegalArgumentException(key + " is invalid/unsupported");
        }
    }
    
}
