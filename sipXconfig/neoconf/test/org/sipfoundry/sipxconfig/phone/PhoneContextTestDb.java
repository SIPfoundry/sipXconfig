/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */
package org.sipfoundry.sipxconfig.phone;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import org.sipfoundry.sipxconfig.common.UserException;
import org.sipfoundry.sipxconfig.phonebook.PhonebookManager;
import org.sipfoundry.sipxconfig.setting.Group;
import org.sipfoundry.sipxconfig.setting.SettingDao;
import org.sipfoundry.sipxconfig.test.IntegrationTestCase;
import org.sipfoundry.sipxconfig.test.TestHelper;

public class PhoneContextTestDb extends IntegrationTestCase {
    private PhoneContext m_context;
    private PhonebookManager m_phonebookManager;
    private SettingDao m_settingContext;
    
    @Override
    protected void onSetUpBeforeTransaction() throws Exception {
        super.onSetUpBeforeTransaction();
        db().execute("select truncate_all()");
    }
    
    @Override
    protected void onSetUpInTransaction() throws Exception {
        super.onSetUpInTransaction();
    }

    public void testClear() throws Exception {
        m_context.clear();
        TestHelper.cleanInsertFlat("phone/EndpointSeed.xml");
        m_context.clear();
    }

    public void testCheckForDuplicateFieldsOnNew() throws Exception {
        TestHelper.cleanInsertFlat("phone/EndpointSeed.xml");

        Phone p = m_context.newPhone(new TestPhoneModel());
        p.setSerialNumber("999123456789");

        try {
            m_context.storePhone(p);
            fail("should have thrown Duplicate*Exception");
        } catch (UserException e) {
            // ok
        }
    }

    public void testCheckForDuplicateFieldsOnSave() throws Exception {
        sql("phone/DuplicateSerialNumberSeed.sql");
        flush();
        Phone p = m_context.newPhone(new TestPhoneModel());
        p.setSerialNumber("000000000002");
        try {
            m_context.storePhone(p);
            fail("should have thrown DuplicateFieldException");
        } catch (UserException e) {
            // ok
        }
    }

    public void testCheckForInvalidSerialNumberOnSave() throws Exception {
        Phone p = m_context.newPhone(new TestPhoneModel());
        p.setSerialNumber("0000000z");

        // The pattern is quite flexible, but does not accept 'z'.
        try {
            m_context.storePhone(p);
            fail("should have thrown InvalidSerialNumberException");
        } catch (UserException e) {
            // ok
        }
    }

    public void testGetGroupMemberCountIndexedByGroupId() throws Exception {
        TestHelper.cleanInsertFlat("phone/GroupMemberCountSeed.xml");

        Map<Integer, Long> counts = m_settingContext.getGroupMemberCountIndexedByGroupId(Phone.class);
        assertEquals(2, counts.size());
        assertEquals(2, counts.get(1001).intValue());
        assertEquals(1, counts.get(1002).intValue());
        assertNull(counts.get(1003));
    }

    /**
     * this test is really for PhoneTableModel in web context
     */
    public void testGetPhonesByPageSortedByModel() throws Exception {
        TestHelper.cleanInsertFlat("phone/SamplePhoneSeed.xml");

        List<Phone> page1 = m_context.loadPhonesByPage(null, 0, 4, new String[] {
            "serialNumber"
        }, true);
        assertEquals("00001", page1.get(0).getSerialNumber());
        assertEquals("00002", page1.get(1).getSerialNumber());
        assertEquals("00003", page1.get(2).getSerialNumber());
        assertEquals("aa00004", page1.get(3).getSerialNumber());
    }

    public void testLoadPhones() throws Exception {
        TestHelper.cleanInsertFlat("phone/SamplePhoneSeed.xml");

        List<Phone> page1 = m_context.loadPhones();
        assertEquals(4, page1.size());
        assertEquals("00001", page1.get(0).getSerialNumber());
        assertEquals("00002", page1.get(1).getSerialNumber());
        assertEquals("00003", page1.get(2).getSerialNumber());
        assertEquals("aa00004", page1.get(3).getSerialNumber());
    }

    public void testGetAllPhoneIds() throws Exception {
        TestHelper.cleanInsertFlat("phone/SamplePhoneSeed.xml");

        List<Integer> result = m_context.getAllPhoneIds();
        assertEquals(4, result.size());
        for (int i = 0; i < result.size(); i++) {
            assertEquals(1000 + i, result.get(i).intValue());
        }
    }

    public void testGetPhoneIdBySerialNumber() throws Exception {
        TestHelper.cleanInsertFlat("phone/SamplePhoneSeed.xml");
        assertEquals(new Integer(1002), m_context.getPhoneIdBySerialNumber("00003"));
        assertEquals(new Integer(1003), m_context.getPhoneIdBySerialNumber("aa00004"));
        assertEquals(null, m_context.getPhoneIdBySerialNumber("won't find this guy"));
    }

    public void testCountPhones() throws Exception {
        TestHelper.cleanInsertFlat("phone/SamplePhoneSeed.xml");
        assertEquals(4, m_context.getPhonesCount());
    }

    public void testGetGroupByName() throws Exception {
        TestHelper.cleanInsertFlat("phone/SamplePhoneSeed.xml");

        Group g1 = m_context.getGroupByName("phone group 1", false);
        assertNotNull(g1);
        assertEquals("phone group 1", g1.getName());

        Group g2 = m_context.getGroupByName("bongo", false);
        assertNull(g2);
        assertEquals(2, getConnection().getRowCount("group_storage"));

        g2 = m_context.getGroupByName("bongo", true);
        assertNotNull(g2);
        assertEquals("bongo", g2.getName());

        assertEquals(2, getConnection().getRowCount("group_storage"));
    }

    public void testCountPhonesInGroup() throws Exception {
        TestHelper.cleanInsertFlat("phone/SamplePhoneSeed.xml");
        assertEquals(1, m_context.getPhonesInGroupCount(new Integer(1001)));
        assertEquals(2, m_context.getPhonesInGroupCount(new Integer(1002)));
    }

    public void testAddToGroup() throws Exception {
        TestHelper.cleanInsertFlat("phone/GroupMemberCountSeed.xml");

        assertEquals(0, TestHelper.getConnection().getRowCount("phone_group",
                "where phone_id = 1001 AND group_id = 1002"));

        m_context.addToGroup(1002, Collections.singleton(1001));
        flush();
        db().queryForInt("select 1 from phone_group where phone_id = 1001 AND group_id = 1002");
    }

    public void testRemoveFromGroup() throws Exception {
        TestHelper.cleanInsertFlat("phone/GroupMemberCountSeed.xml");

        Integer[] ids = {
            1001, 1002
        };

        assertEquals(2, TestHelper.getConnection().getRowCount("phone_group", "where phone_group.group_id = 1001"));

        m_context.removeFromGroup(1001, Arrays.asList(ids));
        flush();
        assertEquals(0, db().queryForInt("select count(*) from phone_group where group_id = 1001"));
    }

    public void testLoadPhonesWithNoLinesByPage() throws Exception {
        TestHelper.cleanInsertFlat("phone/SamplePhoneWithLineSeed.xml");

        List<Phone> page1 = m_context.loadPhonesWithNoLinesByPage(0, 4, new String[] {
            "serialNumber"}, true);
        assertEquals(2, page1.size());
        assertEquals("00002", page1.get(0).getSerialNumber());
        assertEquals("00004", page1.get(1).getSerialNumber());
    }

    public void testGetPhonebookEntries() throws Exception {
        TestHelper.cleanInsertFlat("phone/PhoneWithPhonebookSeed.xml");

        Phone phone = m_context.loadPhone(1001);
        assertEquals(6, m_context.getPhonebookEntries(phone).size());

        Phone phone2 = m_context.loadPhone(2001);
        assertEquals(1, m_context.getPhonebookEntries(phone2).size());

        //Test everyone disabled
        m_phonebookManager.getGeneralPhonebookSettings().setEveryoneEnabled(false);
        phone = m_context.loadPhone(1001);
        assertEquals(5, m_context.getPhonebookEntries(phone).size());

        phone2 = m_context.loadPhone(2001);
        assertEquals(0, m_context.getPhonebookEntries(phone2).size());

        //Reset everyone to default value(true)
        m_phonebookManager.getGeneralPhonebookSettings().setEveryoneEnabled(true);
    }

    public void setPhoneContext(PhoneContext context) {
        m_context = context;
    }

    public void setPhonebookManager(PhonebookManager phonebookManager) {
        m_phonebookManager = phonebookManager;
    }

    public void setSettingDao(SettingDao settingContext) {
        m_settingContext = settingContext;
    }
}
