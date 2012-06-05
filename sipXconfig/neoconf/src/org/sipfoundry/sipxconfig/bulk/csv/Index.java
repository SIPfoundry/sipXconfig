/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.bulk.csv;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Values of the enums below determine the exact format of CSV file
 *
 * "Username", "Pintoken", "Voicemail PIN", "Sip Password", "FirstName", "LastName", "Alias", "UserGroup",
 * "SerialNumber", "Manufacturer", "Model", "Phone Group", "Phone Description"
 */
public enum Index {
    // user fields
    USERNAME("userName", 0), PIN("pin", 1), VOICEMAIL_PIN("voicemailPin", 2),
    SIP_PASSWORD("sipPassword", 3), FIRST_NAME("firstName", 4),
    LAST_NAME("lastName", 5), ALIAS("aliasesString", 6), EMAIL("emailAddress", 7), USER_GROUP("userGroupName", 8),

    // phone fields
    SERIAL_NUMBER("serialNumber", 9), MODEL_ID("modelId", 10), PHONE_GROUP("phoneGroupName", 11), PHONE_DESCRIPTION(
            "description", 12),
    // XMPP
    IM_ID("imId", 13),
    JOB_TITLE("userProfile.jobTitle", 14),
    JOB_DEPT("userProfile.jobDept", 15),
    COMPANY_NAME("userProfile.companyName", 16),
    ASSISTANT_NAME("userProfile.assistantName", 17),
    CELL_PHONE_NUMBER("userProfile.cellPhoneNumber", 18),
    HOME_PHONE_NUMBER("userProfile.homePhoneNumber", 19),
    ASSISTANT_PHONE_NUMBER("userProfile.assistantPhoneNumber", 20),
    FAX_NUMBER("userProfile.faxNumber", 21),
    ALTERNATE_EMAIL("userProfile.alternateEmailAddress", 22),
    ALTERNATE_IM_ID("userProfile.alternateImId", 23),
    LOCATION("userProfile.location", 24),
    HOME_STREET("userProfile.homeAddress.street", 25),
    HOME_CITY("userProfile.homeAddress.city", 26),
    HOME_STATE("userProfile.homeAddress.state", 27),
    HOME_COUNTRY("userProfile.homeAddress.country", 28),
    HOME_ZIP("userProfile.homeAddress.zip", 29),
    OFFICE_STREET("userProfile.officeAddress.street", 30),
    OFFICE_CITY("userProfile.officeAddress.city", 31),
    OFFICE_STATE("userProfile.officeAddress.state", 32),
    OFFICE_COUNTRY("userProfile.officeAddress.country", 33),
    OFFICE_ZIP("userProfile.officeAddress.zip", 34);

    private final String m_name;
    private final int m_value;

    Index(String name, int value) {
        m_name = name;
        m_value = value;
    }

    public String getName() {
        return m_name;
    }

    public int getValue() {
        return m_value;
    }

    public String get(String[] row) {
        return (m_value < row.length ? row[m_value] : StringUtils.EMPTY);
    }

    public void set(String[] row, String value) {
        row[m_value] = value;
    }

    public void setProperty(Object bean, String[] row) {
        String value = get(row);
        if (value.length() == 0) {
            return;
        }
        try {
            BeanUtils.setProperty(bean, m_name, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    public String getProperty(Object bean) {
        try {
            return BeanUtils.getProperty(bean, m_name);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static String[] getAllNames() {
        Index[] values = values();
        String[] names = new String[values.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = values[i].getName();
        }
        return names;
    }

    public static String[] newRow() {
        String[] row = new String[values().length];
        Arrays.fill(row, StringUtils.EMPTY);
        return row;
    }

    public static String[] labels() {
        return new String[] {
            "User name", "PIN", "Voicemail PIN", "SIP password", "First name", "Last name",
            "User alias", "EMail address", "User group", "Phone serial number", "Phone model",
            "Phone group", "Phone description", "Im Id", "Job Title", "Job department",
            "Company name", "Assistant name", "Cell phone number", "Home phone number",
            "Assistant phone number", "Fax number", "Alternate email", "Alternate im",
            "Location", "Home street", "Home city", "Home state", "Home country", "Home zip",
            "Office street", "Office city", "Office state", "Office country", "Office zip"
        };
    }
}
