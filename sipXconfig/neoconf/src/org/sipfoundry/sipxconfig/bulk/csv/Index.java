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
    USERNAME("userName", 0), PIN("pin", 1), VOICEMAIL_PIN("voicemailPin", 2), HOTELLING_PIN("hotellingPin", 3),
    SIP_PASSWORD("sipPassword", 4), FIRST_NAME("firstName", 5),
    LAST_NAME("lastName", 6), ALIAS("aliasesString", 7), EMAIL("emailAddress", 8), USER_GROUP("userGroupName", 9),

    // phone fields
    SERIAL_NUMBER("serialNumber", 10), MODEL_ID("modelId", 11), PHONE_GROUP("phoneGroupName", 12), PHONE_DESCRIPTION(
            "description", 13),
    // XMPP
    IM_ID("imId", 14),
    SALUTATION("userProfile.salutation", 15),
    MANAGER("userProfile.manager", 16),
    EMPLOYEE_ID("userProfile.employeeId", 17),
    JOB_TITLE("userProfile.jobTitle", 18),
    JOB_DEPT("userProfile.jobDept", 19),
    COMPANY_NAME("userProfile.companyName", 20),
    ASSISTANT_NAME("userProfile.assistantName", 21),
    CELL_PHONE_NUMBER("userProfile.cellPhoneNumber", 22),
    HOME_PHONE_NUMBER("userProfile.homePhoneNumber", 23),
    ASSISTANT_PHONE_NUMBER("userProfile.assistantPhoneNumber", 24),
    FAX_NUMBER("userProfile.faxNumber", 25),
    DID_NUMBER("userProfile.didNumber", 26),
    ALTERNATE_EMAIL("userProfile.alternateEmailAddress", 27),
    ALTERNATE_IM_ID("userProfile.alternateImId", 28),
    LOCATION("userProfile.location", 29),
    HOME_STREET("userProfile.homeAddress.street", 30),
    HOME_CITY("userProfile.homeAddress.city", 31),
    HOME_STATE("userProfile.homeAddress.state", 32),
    HOME_COUNTRY("userProfile.homeAddress.country", 33),
    HOME_ZIP("userProfile.homeAddress.zip", 34),
    OFFICE_STREET("userProfile.officeAddress.street", 35),
    OFFICE_CITY("userProfile.officeAddress.city", 36),
    OFFICE_STATE("userProfile.officeAddress.state", 37),
    OFFICE_COUNTRY("userProfile.officeAddress.country", 38),
    OFFICE_ZIP("userProfile.officeAddress.zip", 39),
    OFFICE_MAIL_STOP("userProfile.officeAddress.officeDesignation", 40),
    TWITTER_NAME("userProfile.twiterName", 41),
    LINKEDIN_NAME("userProfile.linkedinName", 42),
    FACEBOOK_NAME("userProfile.facebookName", 43),
    XING_NAME("userProfile.xingName", 44),
    // voice mail settings
    ACTIVE_GREETING("activeGreeting", 45),
    PRIMARY_EMAIL_NOTIFICATION("primaryEmailNotification", 46),
    PRIMARY_EMAIL_FORMAT("primaryEmailFormat", 47),
    PRIMARY_EMAIL_ATTACH_AUDIO("primaryEmailAttachAudio", 48),
    ALT_EMAIL_NOTIFICATION("alternateEmailNotification", 49),
    ALT_EMAIL_FORMAT("alternateEmailFormat", 50),
    ALT_EMAIL_ATTACH_AUDIO("alternateEmailAttachAudio", 51),
    VOICEMAIL_SERVER("voicemailServer", 52),
    // user caller alias
    EXTERNAL_NUMBER("externalNumber", 53),
    ANONYMOUS_CALLER_ALIAS("anonymousCallerAlias", 54),
    // additional phone settings
    ADDITIONAL_PHONE_SETTINGS("additionalPhoneSettings", 55),
    // additional line settings
    ADDITIONAL_LINE_SETTINGS("additionalLineSettings", 56),
    // authorization account name - can be used for authentication
    AUTH_ACCOUNT_NAME("userProfile.authAccountName", 57),
    // semi column separated email addresses - can be used for authentication
    EMAIL_ADDRESS_ALIASES("userProfile.emailAddressAliases", 58),
    //generic field 1
    CUSTOM_1("userProfile.custom1", 59),
    //generic field 2
    CUSTOM_2("userProfile.custom2", 60),
    //generic field 3
    CUSTOM_3("userProfile.custom3", 61);

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
            "User name", "PIN", "Voicemail PIN", "SIP password", "First name", "Last name", "User alias",
            "EMail address", "User group", "Phone serial number", "Phone model", "Phone group", "Phone description",
            "Im Id", "Salutation", "Manager", "EmployeeId", "Job Title", "Job department", "Company name",
            "Assistant name", "Cell phone number", "Home phone number", "Assistant phone number", "Fax number",
            "Did number", "Alternate email", "Alternate im", "Location", "Home street", "Home city", "Home state",
            "Home country", "Home zip", "Office street", "Office city", "Office state", "Office country",
            "Office zip", "Office mail stop", "Twitter", "Linkedin", "Facebook", "Xing", "Active greeting",
            "Email voicemail notification", "Email format", "Email attach audio",
            "Alternate email voicemail notification", "Alternate email format", "Alternate email attach audio",
            "Internal Voicemail Server", "Caller ID", "Block Caller ID", "Additional phone settings",
            "Additional line settings", "Auth Account Name", "EMail address aliases", "Custom 1", "Custom 2", "Custom 3"
        };
    }
}
