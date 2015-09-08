package org.sipfoundry.commons.mongo;

import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.MongoDbFactory;

import com.mongodb.DB;

public class MongoDelegateFactory implements MongoDbFactory {

    private MongoDbFactory m_delegateFactory;
    private String m_dbName;
    
    public MongoDelegateFactory(MongoDbFactory delegateFactory, String dbName) {
        this.m_delegateFactory = delegateFactory;
        this.m_dbName = dbName;
    }

    @Override
    public DB getDb() throws DataAccessException {
        return getDb(m_dbName);
    }

    @Override
    public DB getDb(String dbName) throws DataAccessException {
        return m_delegateFactory.getDb(dbName);
    }
    
}
