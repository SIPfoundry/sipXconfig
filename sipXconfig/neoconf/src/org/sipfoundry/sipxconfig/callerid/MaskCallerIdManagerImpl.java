package org.sipfoundry.sipxconfig.callerid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sipfoundry.commons.mongo.MongoConstants;
import org.sipfoundry.sipxconfig.common.Replicable;
import org.sipfoundry.sipxconfig.common.UserException;
import org.sipfoundry.sipxconfig.common.event.DaoEventPublisher;
import org.sipfoundry.sipxconfig.setting.BeanWithSettingsDao;
import org.sipfoundry.sipxconfig.setup.SetupListener;
import org.sipfoundry.sipxconfig.setup.SetupManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class MaskCallerIdManagerImpl extends HibernateDaoSupport implements MaskCallerIdManager, SetupListener {
	
	private static final Log LOG = LogFactory.getLog(MaskCallerIdManagerImpl.class);
	
	private BeanWithSettingsDao<MaskCallerIdSettings> m_settingsDao;
	private DaoEventPublisher m_daoEventPublisher;
	private MongoTemplate m_imdb;

	@Override
	public boolean setup(SetupManager manager) {
		if (manager.isFalse(FEATURE.getId())) {
			createIndexes();
            manager.setTrue(FEATURE.getId());
        }
        return true;
	}

	@Override
	public List<MaskCallerIdBean> getMaskCallerIds() {
		return getHibernateTemplate().loadAll(MaskCallerIdBean.class);
	}

	@Override
	public MaskCallerIdBean getMaskCallerIdById(Integer id) {
		return getHibernateTemplate().load(MaskCallerIdBean.class, id);
	}

	@Override
	public MaskCallerIdSettings getSettings() {
		return m_settingsDao.findOrCreateOne();
	}

	@Override
	public void saveMaskCallerId(MaskCallerIdBean id) {
		validate(id);
        if (id.isNew()) {
            getHibernateTemplate().save(id);
        } else {
            getHibernateTemplate().merge(id);
        }
	}

	@Override
	public void saveSettings(MaskCallerIdSettings settings) {
		m_settingsDao.upsert(settings);		
	}

	@Override
	public void deleteMaskCallerIds(Collection<Integer> ids) {
		for (Integer id : ids) {
            MaskCallerIdBean bean = getMaskCallerIdById(id);
            if(bean != null) {
                getHibernateTemplate().delete(bean);
                m_daoEventPublisher.publishDelete(bean);
            }
        }
	}
	
	@Override
    public List<Replicable> getReplicables() {
		createIndexes();
        List<Replicable> replicables = new ArrayList<Replicable>();
        for (MaskCallerIdBean ids : getMaskCallerIds()) {
            replicables.add(ids);
        }
        return replicables;
    }
	
    private void validate(MaskCallerIdBean id) {
    	String from = id.getFrom();
    	String maskExtension = id.getMaskExtension();
    	String maskName = id.getMaskName();
    	
        if (StringUtils.isBlank(from)) {
            throw new UserException("&error.from.empty");
        }

        if (StringUtils.isBlank(maskExtension) && StringUtils.isBlank(maskName)) {
            throw new UserException("&error.no.mask.value");
        }
        
        //Extension should not have any spaces
        if (!StringUtils.isBlank(maskExtension) && StringUtils.containsAny(maskExtension, " ")) {
        	throw new UserException("&error.mask.extension.spaces");
        }
        
        //Name should not have any quotes
        if (!StringUtils.isBlank(maskName) && StringUtils.containsAny(maskName, "\'\"")) {
        	throw new UserException("&error.mask.name.quotes");
        }

        @SuppressWarnings("unchecked")
		List<MaskCallerIdBean> ids = getHibernateTemplate().findByNamedQueryAndNamedParam("mcidByFrom",
                new String[] { "from" }, new Object[] { from });
        
        if (id.isNew() && ids.size() >= 1) {
        	throw new UserException("&error.duplicate.error");
        }
    }
    
	@SuppressWarnings("deprecation")
	private void createIndexes() {
		DBCollection entity = m_imdb.getDb().getCollection(MongoConstants.ENTITY_COLLECTION);
        DBObject index = new BasicDBObject();
        index.put(MaskCallerIdBean.MCID_FROM, 1);

        try {
            //entity.createIndex(index);
            entity.ensureIndex(index);
        } catch(Exception ex) {
        	LOG.warn("Unable to create index for MaskCallerId. " 
        			+ "Perfomance might be affected for querying Caller Id", ex);
        }
	}	
	
	public MongoTemplate getImdb() {
		return m_imdb;
	}

	public void setImdb(MongoTemplate imdb) {
		this.m_imdb = imdb;
	}
	
    public void setSettingsDao(BeanWithSettingsDao<MaskCallerIdSettings> settingsDao) {
        m_settingsDao = settingsDao;
    }
    
    public void setDaoEventPublisher(DaoEventPublisher daoEventPublisher) {
        m_daoEventPublisher = daoEventPublisher;
    }
	
}
