package org.sipfoundry.sipxconfig.e911;

import org.apache.commons.lang.StringUtils;
import org.sipfoundry.commons.mongo.MongoConstants;
import org.sipfoundry.sipxconfig.common.Replicable;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.commserver.imdb.AbstractDataSetGenerator;
import org.sipfoundry.sipxconfig.commserver.imdb.DataSet;

import com.mongodb.DBObject;

public class E911UserGenerator extends AbstractDataSetGenerator {
    private E911Manager m_e911Manager;

    @Override
    public void generate(Replicable entity, DBObject top) {
        if (! (entity instanceof User)) {
        	//We only process User entity
        	return;
        }
            
        User user = (User) entity;
        E911Location location = null;
        if (user.getE911LocationId() != null && user.getE911LocationId() >= 0) {
        	location = m_e911Manager.findLocationById(user.getE911LocationId());
        } 
        
        String elin = "";
        if(location != null && StringUtils.isNotEmpty(location.getElin()))
        {
        	elin = location.getElin();
        } else {
        	if (StringUtils.isNotEmpty(user.getDid())) {
                elin = user.getDid();
            } else {
                top.removeField(MongoConstants.ELIN);
            }
        }
        
        top.put(MongoConstants.ELIN, elin);
        top.put(MongoConstants.ELIN_ID, user.getE911LocationId());
    }

    @Override
    protected DataSet getType() {
        return null;
    }

    public void setE911Manager(E911Manager e911Manager) {
        m_e911Manager = e911Manager;
    }

}
