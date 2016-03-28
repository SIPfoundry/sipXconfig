package org.sipfoundry.sipxconfig.e911;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sipfoundry.commons.mongo.MongoConstants;
import org.sipfoundry.sipxconfig.common.Replicable;
import org.sipfoundry.sipxconfig.common.UserException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;

public class E911ManagerImpl extends HibernateDaoSupport implements E911Manager {
    private JdbcTemplate m_jdbcTemplate;
    private MongoTemplate m_imdbTemplate;

    @Override
    public List<E911Location> findLocations() {
        return getHibernateTemplate().loadAll(E911Location.class);
    }

    @Override
    public E911Location findLocationById(Integer id) {
        return getHibernateTemplate().load(E911Location.class, id);
    }
    
    @Override
    public E911Location findLocationByElin(String elin) {
    	@SuppressWarnings("unchecked")
        Collection<E911Location> locations = 
        	getHibernateTemplate().findByNamedQueryAndNamedParam(
        			"findByElin", "elin", elin
                );
    	if (!locations.isEmpty()) {
            return locations.iterator().next();
        }
    	
        return null;
    }

    @Override
    public void saveLocation(E911Location location) {
    	String elin = StringUtils.trim(location != null ? location.getElin() : "");
    	if (StringUtils.isBlank(StringUtils.trim(elin))) {
            throw new UserException("&blank.elin.error");
        }
    	location.setElin(elin);
    	
    	String locationAddress = StringUtils.trim(location != null ? location.getLocation() : "");
    	if (StringUtils.isEmpty(StringUtils.trim(locationAddress))) {
            throw new UserException("&blank.location.error");
        }
    	location.setLocation(locationAddress);

        // Check for duplicate codes before saving the Location
        if (location.isNew() || (!location.isNew() && isElinChanged(location))) {
            checkForDuplicateCode(location);
        }
        
        if (location.isNew()) {
            getHibernateTemplate().save(location);
        } else {
            getHibernateTemplate().merge(location);
            updateAffectedEntities(location);
        }
    }
    
    @Override
    public void deleteLocations(Collection<Integer> ids) {
        for (Integer id : ids) {
        	//Delete ids from mongo
        	E911Location location = findLocationById(id);
        	if(location != null) {
        		getHibernateTemplate().delete(location);
        	}
        	
        	//Delete ids from database
            m_jdbcTemplate.update(String.format(
                    	"delete from setting_value where path='e911/location' and value='%d'", id
                    ));
            
            //Update affected entities
            removeAffectedEntities(id);
            
            //Finally remove e911 entity
            DBObject e911Entity = getEntityCollection().findOne(
            			new BasicDBObject(MongoConstants.ID, id)
            		);
            if (e911Entity != null) {
                getEntityCollection().remove(e911Entity);
            }
        }
    }
    
    @Override
    public List<Replicable> getReplicables() {
        List<Replicable> replicables = new ArrayList<Replicable>();
        replicables.addAll(findLocations());
        return replicables;
    }
    
    private boolean isElinChanged(E911Location location) {
        // lookup via id what the old location is and compare with the input
        // parameter
    	E911Location currentLocation = findLocationByElin(location.getElin());
    	if(currentLocation != null)
    	{
    		return !currentLocation.getElin().equals(location.getElin());
    	}
        
    	return false;
    }
    
    private void checkForDuplicateCode(E911Location location) {
        String elin = location.getElin();
        E911Location existingElin = findLocationByElin(elin);
        if (existingElin != null) {
            throw new UserException("&duplicate.elin.error", elin);
        }
    }
    

    private void updateAffectedEntities(E911Location location) {
        BasicDBObject query = new BasicDBObject(MongoConstants.ELIN_ID, location.getId())
										.append(MongoConstants.ELIN, location.getElin());
        
        //Update user elin id
        List<DBObject> objects = findUserByElinId(location.getId());
        for (DBObject dbObject : objects) {
            DBObject set = new BasicDBObject("$set", query);
            getEntityCollection().update(dbObject, set);
        }
        
        //Update phone elin id
        objects = findPhoneByElinId(location.getId());
        for (DBObject dbObject : objects) {
        	DBObject set = new BasicDBObject("$set", query);
    		getEntityCollection().update(dbObject, set);
        }
    }

    private void removeAffectedEntities(Integer id) {
    	//Remove user elid ids
        List<DBObject> objects = findUserByElinId(id);
        for (DBObject object : objects) {
            object.removeField(MongoConstants.ELIN_ID);
            object.removeField(MongoConstants.ELIN);
            getEntityCollection().save(object);
        }
        
        //Remove phone elid ids
        objects = findPhoneByElinId(id);
        for (DBObject object : objects) {
            object.removeField(MongoConstants.ELIN_ID);
            object.removeField(MongoConstants.ELIN);
            getEntityCollection().save(object);
        }
    }

    private List<DBObject> findUserByElinId(int id) {
        DBObject query = QueryBuilder.start(MongoConstants.ENTITY_NAME).is("user")
        							 .and(new BasicDBObject(MongoConstants.ELIN_ID, id))
        							 .get();
        DBCursor cursor = getEntityCollection().find(query);
        List<DBObject> result = cursor.toArray();
        cursor.close();
        return result;
    }

    private List<DBObject> findPhoneByElinId(int id) {
        DBObject query = QueryBuilder.start(MongoConstants.ENTITY_NAME).is("phone")
        							 .and(new BasicDBObject(MongoConstants.ELIN_ID, id))
        							 .get();
        DBCursor cursor = getEntityCollection().find(query);
        List<DBObject> result = cursor.toArray();
        cursor.close();
        return result;
    }
    
    public DBCollection getEntityCollection() {
        return m_imdbTemplate.getDb().getCollection("entity");
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        m_jdbcTemplate = jdbcTemplate;
    }

    public void setImdbTemplate(MongoTemplate imdbTemplate) {
        m_imdbTemplate = imdbTemplate;
    }

}
