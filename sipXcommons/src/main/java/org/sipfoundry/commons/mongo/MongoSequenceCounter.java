package org.sipfoundry.commons.mongo;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

public class MongoSequenceCounter {
	
	private static final String COUNTER_COLLECTION_NAME = "counter";
	private static final String COUNTER_KEY_NAME = "sequence";
	
	private MongoSpringTemplate m_dbTemplate;
	private String m_counterCollectionName;
	
	public MongoSequenceCounter() {
		
	}
	
	public MongoSequenceCounter(MongoSpringTemplate dbTemplate) {
		this(dbTemplate, COUNTER_COLLECTION_NAME);
	}
	
	public MongoSequenceCounter(MongoSpringTemplate dbTemplate, String collectionName) {
		this.m_dbTemplate = dbTemplate;
		this.m_counterCollectionName = collectionName;
	}
	
	public long getCurrentSequence(String key) {
		Assert.notNull(m_dbTemplate, "MongoTemplate must not be null");
		
		BasicDBObject result = (BasicDBObject)getCounterCollection().findOne(
					new BasicDBObject(MongoConstants.ID, key));
		if(result != null) {
			return result.getLong(COUNTER_KEY_NAME);
		}
		
		return 0;
	}
	
	public long getNextSequence(String key) {
		Assert.notNull(m_dbTemplate, "MongoTemplate must not be null");
		
		//Use automic increment operation to prevent race condition
		//during multiple calls
		BasicDBObject updateQuery = new BasicDBObject("$inc"
				, new BasicDBObject(COUNTER_KEY_NAME, 1));
		
		//Return the modified/incremented object.
		BasicDBObject result = (BasicDBObject)getCounterCollection().findAndModify(
					new BasicDBObject(MongoConstants.ID, key),
					null, null, false, updateQuery, true, true
				);
		return result.getLong(COUNTER_KEY_NAME);
	}
	
	
	private DBCollection getCounterCollection() {
		return m_dbTemplate.getDb().getCollection(getCounterCollectionName());
	}
	
	public MongoSpringTemplate getDbTemplate() {
		return m_dbTemplate;
	}

	public void setDbTemplate(MongoSpringTemplate dbTemplate) {
		this.m_dbTemplate = dbTemplate;
	}

	public String getCounterCollectionName() {
		if(StringUtils.isBlank(m_counterCollectionName)) {
			m_counterCollectionName = COUNTER_COLLECTION_NAME;
		}
		
		return m_counterCollectionName;
	}

	public void setCounterCollectionName(String counterCollectionName) {
		this.m_counterCollectionName = counterCollectionName;
	}
	
}
