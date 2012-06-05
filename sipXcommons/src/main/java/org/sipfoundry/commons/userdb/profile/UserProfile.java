/**
 *
 *
 * Copyright (c) 2012 eZuce, Inc. All rights reserved.
 * Contributed to SIPfoundry under a Contributor Agreement
 *
 * This software is free software; you can redistribute it and/or modify it under
 * the terms of the Affero General Public License (AGPL) as published by the
 * Free Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 */
package org.sipfoundry.commons.userdb.profile;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class UserProfile {

    @Id
    private String m_userid;

    private String m_userName;
    private String m_firstName;
    private String m_lastName;

    private String m_jobTitle;
    private String m_jobDept;
    private String m_companyName;
    private String m_assistantName;
    private String m_location;

    private Address m_homeAddress = new Address();
    private Address m_officeAddress = new Address();
    private Address m_branchAddress = new Address();
    private String m_cellPhoneNumber;
    private String m_homePhoneNumber;
    private String m_assistantPhoneNumber;
    private String m_faxNumber;
    private String m_didNumber;
    private String m_imId;
    private String m_imDisplayName;
    private String m_alternateImId;
    private String m_emailAddress;
    private String m_alternateEmailAddress;
    private boolean m_useBranchAddress;

    private String m_avatar;
    private String m_extAvatar;
    private boolean m_useExtAvatar;

    public String getUserId() {
        return m_userid;
    }
 
    public void setUserId(final String userid) {
        m_userid = userid;
    }

    public String getUserName() {
        return m_userName;
    }

    public void setUserName(String userName) {
        m_userName = userName;
    }

    public String getFirstName() {
        return m_firstName;
    }

    public void setFirstName(String firstName) {
        m_firstName = firstName;
    }

    public String getLastName() {
        return m_lastName;
    }

    public void setLastName(String lastName) {
        m_lastName = lastName;
    }

    public String getJobTitle() {
        return m_jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        m_jobTitle = jobTitle;
    }

    public String getJobDept() {
        return m_jobDept;
    }

    public void setJobDept(String jobDept) {
        m_jobDept = jobDept;
    }

    public String getCompanyName() {
        return m_companyName;
    }

    public void setCompanyName(String companyName) {
        m_companyName = companyName;
    }

    public String getAssistantName() {
        return m_assistantName;
    }

    public void setAssistantName(String assistantName) {
        m_assistantName = assistantName;
    }

    public Address getHomeAddress() {
        return m_homeAddress;
    }

    public void setHomeAddress(Address homeAddress) {
        m_homeAddress = homeAddress;
    }

    public Address getOfficeAddress() {
        return m_officeAddress;
    }

    public Address getBranchAddress() {
        return m_branchAddress;
    }

    public void setOfficeAddress(Address officeAddress) {
        m_officeAddress = officeAddress;
    }

    public void setBranchAddress(Address branchAddress) {
        m_branchAddress = branchAddress;
    }

    public String getCellPhoneNumber() {
        return m_cellPhoneNumber;
    }

    public void setCellPhoneNumber(String cellPhoneNumber) {
        m_cellPhoneNumber = cellPhoneNumber;
    }

    public String getHomePhoneNumber() {
        return m_homePhoneNumber;
    }

    public void setHomePhoneNumber(String homePhoneNumber) {
        m_homePhoneNumber = homePhoneNumber;
    }

    public String getAssistantPhoneNumber() {
        return m_assistantPhoneNumber;
    }

    public void setAssistantPhoneNumber(String assistantPhoneNumber) {
        m_assistantPhoneNumber = assistantPhoneNumber;
    }

    public String getFaxNumber() {
        return m_faxNumber;
    }

    public void setFaxNumber(String faxNumber) {
        m_faxNumber = faxNumber;
    }

    public String getImId() {
        return m_imId;
    }

    public void setImId(String imId) {
        m_imId = imId;
    }

    public String getImDisplayName() {
        return m_imDisplayName;
    }

    public void setImDisplayName(String imDisplayName) {
        m_imDisplayName = imDisplayName;
    }

    public String getAlternateImId() {
        return m_alternateImId;
    }

    public void setAlternateImId(String alternateImId) {
        m_alternateImId = alternateImId;
    }

    public String getLocation() {
        return m_location;
    }

    public void setLocation(String location) {
        m_location = location;
    }

    public boolean getUseBranchAddress() {
        return m_useBranchAddress;
    }

    public void setUseBranchAddress(boolean useBranchAddress) {
        m_useBranchAddress = useBranchAddress;
    }

    public String getEmailAddress() {
        return m_emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        m_emailAddress = emailAddress;
    }

    public String getAvatar() {
        if (m_useExtAvatar) {
            return m_extAvatar;
        }
        return m_avatar;
    }

    public void setAvatar(String url) {
        m_avatar = url;
    }

    public void setExtAvatar(String url) {
        m_extAvatar = url;
    }

    public String getExtAvatar() {
        return m_extAvatar;
    }

    public void setUseExtAvatar(boolean useExternal) {
        m_useExtAvatar = useExternal;
    }

    public boolean getUseExtAvatar() {
        return m_useExtAvatar;
    }

    public String getAlternateEmailAddress() {
        return m_alternateEmailAddress;
    }

    public void setAlternateEmailAddress(String alternateEmailAddress) {
        m_alternateEmailAddress = alternateEmailAddress;
    }

    public String getDidNumber() {
        return m_didNumber;
    }

    public void setDidNumber(String didNumber) {
        m_didNumber = didNumber;
    }

    public void update(UserProfile object) {
        try {
            BeanUtils.copyProperties(this, object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
