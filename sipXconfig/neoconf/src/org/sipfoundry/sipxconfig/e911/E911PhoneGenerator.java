package org.sipfoundry.sipxconfig.e911;

import org.apache.commons.lang.StringUtils;
import org.sipfoundry.commons.mongo.MongoConstants;
import org.sipfoundry.sipxconfig.common.Replicable;
import org.sipfoundry.sipxconfig.commserver.imdb.AbstractDataSetGenerator;
import org.sipfoundry.sipxconfig.commserver.imdb.DataSet;
import org.sipfoundry.sipxconfig.phone.Phone;

import com.mongodb.DBObject;

public class E911PhoneGenerator extends AbstractDataSetGenerator {
    private E911Manager m_e911Manager;

    @Override
    public void generate(Replicable entity, DBObject top) {
        if (!(entity instanceof Phone)) {
        	//Only process Phone entity
        	return;
        }
        
        Phone phone = (Phone) entity;
        
        E911Location location = null;
        if (phone.getE911LocationId() != null) {
            location = m_e911Manager.findLocationById(phone.getE911LocationId());
        } 
        
        String elin = "";
        if(location != null && StringUtils.isNotEmpty(location.getElin()))
        {
        	elin = location.getElin();
        } else {
        	top.removeField(MongoConstants.ELIN);
        }
        
        top.put(MongoConstants.ELIN, elin);
        top.put(MongoConstants.ELIN_ID, phone.getE911LocationId());
    }

    @Override
    protected DataSet getType() {
        return null;
    }

    public void setE911Manager(E911Manager e911Manager) {
        m_e911Manager = e911Manager;
    }

}
