/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.search;

import java.util.Collection;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import org.sipfoundry.sipxconfig.common.CoreContext;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.phone.Phone;
import org.sipfoundry.sipxconfig.search.BeanAdaptor.Identity;
import org.sipfoundry.sipxconfig.test.IntegrationTestCase;
import org.sipfoundry.sipxconfig.test.TestHelper;

public class SearchManagerImplTestIntegration extends IntegrationTestCase {
    private SearchManager m_searchManager;
    private CoreContext m_coreContext;
    private IndexManager m_indexManager;
    private IdentityToBean m_identityToBean;

    @Override
    protected void onSetUpBeforeTransaction() throws Exception {
        super.onSetUpBeforeTransaction();
        clear();
    }

    protected void onSetUpInTransaction() throws Exception {
        super.onSetUpInTransaction();
        sql("commserver/SeedLocations.sql");
        sql("domain/DomainSeed.sql");
        m_identityToBean = new IdentityToBean(m_coreContext);
        m_indexManager.indexAll();
    }

    public void testSearch() throws Exception {
        User user = m_coreContext.newUser();
        user.setFirstName("fIrst");
        user.setLastName("last");
        user.setUserName("boNGor");
        user.setPintoken("e3e367205de83ab477cdf3449f000000");
        user.setSipPassword("54321");

        m_coreContext.saveUser(user);
        commit();

        Collection collection = m_searchManager.search("bon*", m_identityToBean);
        assertEquals(1, collection.size());
        assertTrue(collection.remove(user));

        collection = m_searchManager.search("userName:bongo", m_identityToBean);
        assertEquals(1, collection.size());
        assertTrue(collection.remove(user));

        collection = m_searchManager.search("firstName:bongo", m_identityToBean);
        assertEquals(0, collection.size());

        // do not seach by PIN token or SIP password
        collection = m_searchManager.search("54321", m_identityToBean);
        assertEquals(0, collection.size());

        collection = m_searchManager.search("e3e367*", m_identityToBean);
        assertEquals(0, collection.size());


        user.setUserName("kuku");
        // when changing username, must change PIN too
        user.setPin("1234");
        m_coreContext.saveUser(user);

        collection = m_searchManager.search("first", m_identityToBean);
        assertEquals(1, collection.size());
        assertTrue(collection.remove(user));

        collection = m_searchManager.search("userName:kuku", m_identityToBean);
        assertEquals(1, collection.size());
        assertTrue(collection.remove(user));

        collection = m_searchManager.search("bongo", m_identityToBean);
        assertEquals(0, collection.size());

        user.setAliasesString("aaa, bcd");
        // user.setFirstName("zzzfirstname");
        m_coreContext.saveUser(user);

        collection = m_searchManager.search("zzzfirstname", m_identityToBean);

        // assertEquals(1, collection.size());
        // assertTrue(collection.remove(user));

        /*
         * we have to disable tests
         * Due to a bug in Hibernate, when the dirty field is a collection,
         * onFlushDirty interceptor will not be called.
         * Make sure to enable this test when problem is fixed
        User user2 = m_coreContext.loadUser(user.getId());
        System.err.println(user2.getAliasesString());

        collection = m_searchManager.search("aaa", m_identityToBean);

        assertEquals(1, collection.size());
        assertTrue(collection.remove(user));

        collection = m_searchManager.search("alias:bcd", m_identityToBean);
        assertEquals(1, collection.size());
        assertTrue(collection.remove(user));*/
    }

    public void testSearchByClass() throws Exception {
        User user =  m_coreContext.newUser();
        user.setFirstName("first");
        user.setLastName("last");
        user.setUserName("bongo3");

        m_coreContext.saveUser(user);

        Collection collection = m_searchManager.search(User.class, "bon*", m_identityToBean);
        assertEquals(1, collection.size());
        assertTrue(collection.remove(user));

        collection = m_searchManager.search(User.class, "userName:bongo", m_identityToBean);
        assertEquals(1, collection.size());
        assertTrue(collection.remove(user));

        collection = m_searchManager.search(Phone.class, "userName:bongo", null);
        assertEquals(0, collection.size());

        collection = m_searchManager.search(User.class, "firstName:bongo", null);
        assertEquals(0, collection.size());
    }

    public void testSearchIdent() throws Exception {
        User user =  m_coreContext.newUser();
        user.setFirstName("first");
        user.setLastName("last");
        //username "boNGo" might already created and it has "bongo" as default im id
        //if we pick "bongo" as userName will fail, because will try to save im id "bongo"
        //and this collides with "bongo" in case is already saved
        
        user.setUserName("bongo12");

        m_coreContext.saveUser(user);

        Collection collection = m_searchManager.search("bon*", null);
        assertEquals(1, collection.size());
        Identity ident = (Identity) collection.iterator().next();
        assertEquals("last, first, bongo12", ident.getName());
    }

    public void testSorting() throws Exception {
        final String[] names = {
            "aa", "bb", "ee", "zz"
        };

        for (int i = 0; i < names.length; i++) {
            User user =  m_coreContext.newUser();
            user.setFirstName("first");
            user.setLastName("last");
            user.setUserName(names[i]);

            m_coreContext.saveUser(user);
        }

        // ascending
        String[] orderBy = new String[] {
            "userName"
        };
        List collection = m_searchManager.search(User.class, "first*", 0, -1, orderBy, true,
                m_identityToBean);
        assertEquals(names.length, collection.size());
        User user = (User) collection.get(0);
        assertEquals(names[0], user.getUserName());
        user = (User) collection.get(names.length - 1);
        assertEquals(names[names.length - 1], user.getUserName());

        // descending
        collection = m_searchManager.search(User.class, "first*", 0, -1, orderBy, false,
                m_identityToBean);
        assertEquals(names.length, collection.size());
        user = (User) collection.get(names.length - 1);
        assertEquals(names[0], user.getUserName());
        user = (User) collection.get(0);
        assertEquals(names[names.length - 1], user.getUserName());

        // do not return first item - descending order
        collection = m_searchManager.search(User.class, "first*", 1, -1, orderBy, false,
                m_identityToBean);
        int size = collection.size();
        assertEquals(names.length - 1, size);
        user = (User) collection.get(0);
        assertEquals(names[names.length - 2], user.getUserName());
        user = (User) collection.get(size - 1);
        assertEquals(names[0], user.getUserName());

        // only return 2 items starting from first - ascending order
        int pageSize = 2;
        collection = m_searchManager.search(User.class, "first*", 1, pageSize, orderBy, true,
                m_identityToBean);
        assertEquals(pageSize, collection.size());
        user = (User) collection.get(0);
        assertEquals(names[1], user.getUserName());
        user = (User) collection.get(1);
        assertEquals(names[2], user.getUserName());
    }

    public void testSortingByMultipleFields() {
        final String[] names = {
            "xaa", "xee", "xbb", "xzz"
        };

        final String[] firstNames = {
            "44", "33", "22", "11"
        };

        final String[] lastNames = {
            "11", "22", "22", "33"
        };

        for (int i = 0; i < names.length; i++) {
            User user = m_coreContext.newUser();
            user.setFirstName(firstNames[i]);
            user.setLastName(lastNames[i]);
            user.setUserName(names[i]);

            m_coreContext.saveUser(user);
        }
        String[] orderBy = new String[] {
            "lastName", "firstName"
        };
        List users = m_searchManager.search(User.class, "x*", 0, -1, orderBy, false,
                m_identityToBean);

        assertEquals("xzz", ((User) users.get(0)).getUserName());
        assertEquals("xee", ((User) users.get(1)).getUserName());
        assertEquals("xbb", ((User) users.get(2)).getUserName());
        assertEquals("xaa", ((User) users.get(3)).getUserName());
    }

    public void testSortingIllegalFieldName() throws Exception {
        List collection = m_searchManager.search(User.class, "first*", 0, -1, new String[] {
            "bongo"
        }, true, m_identityToBean);
        assertEquals(0, collection.size());
    }

    public void testParseQuery() throws Exception {
        SearchManagerImpl impl = new SearchManagerImpl();
        impl.setAnalyzer(new StandardAnalyzer(Version.LUCENE_30));

        Query query = impl.parseUserQuery("kuku");
        assertTrue(query instanceof PrefixQuery);

        query = impl.parseUserQuery("-kuku");
        assertFalse(query instanceof PrefixQuery);

        query = impl.parseUserQuery("name:kuku");
        assertTrue(query instanceof PrefixQuery);

        query = impl.parseUserQuery("name:kuku AND bongo");
        assertFalse(query instanceof PrefixQuery);
    }

    public void testOutdatedIndex() throws Exception {
        User user =  m_coreContext.newUser();
        user.setFirstName("first");
        user.setLastName("last");
        user.setUserName("bongo");

        m_coreContext.saveUser(user);
        commit();

        Collection collection = m_searchManager.search(User.class, "bon*", m_identityToBean);
        assertEquals(1, collection.size());
        assertTrue(collection.contains(user));

        // remove user from database only
        TestHelper.cleanInsert("ClearDb.xml");

        // cannot find user but no exception
        collection = m_searchManager.search(User.class, "bon*", m_identityToBean);
        assertEquals(0, collection.size());
    }

    public void setSearchManager(SearchManager searchManager) {
        m_searchManager = searchManager;
    }

    public void setCoreContext(CoreContext coreContext) {
        m_coreContext = coreContext;
    }

    public void setIndexManager(IndexManager indexManager) {
        m_indexManager = indexManager;
    }
}
