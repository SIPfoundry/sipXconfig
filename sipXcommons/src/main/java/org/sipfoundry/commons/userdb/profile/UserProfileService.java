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

import java.io.InputStream;
import java.util.List;

import org.bson.types.ObjectId;

public interface UserProfileService {
    static final String DISABLED = "DISABLED";
    static final String ENABLED = "ENABLED";
    static final String LDAP = "LDAP";
    static final String PHANTOM = "PHANTOM";

    UserProfile getUserProfile(String userId);

    void saveUserProfile(UserProfile profile);

    void deleteUserProfile(UserProfile profile);

    void deleteUserProfile(String userName);

    Integer getUserIdByImId(String imId);

    boolean isAliasInUse(String alias, String username);

    boolean isImIdInUse(String imId, Integer userId);

    boolean isImIdInUse(String imId);

    String getUsernameByImId(String imId);

    UserProfile getUserProfileByImId(String imId);

    List<UserProfile> getUserProfileByAuthAccountName(String authAccountName);

    List<UserProfile> getUserProfileByEmail(String email);

    List<Integer> getUserIdsByAuthAccountName(String authAccountName);

    List<Integer> getUserIdsByEmail(String email);

    InputStream getAvatar(String userName);

    void saveAvatar(String userName, InputStream is) throws AvatarUploadException;

    void saveAvatar(String userName, InputStream is, boolean overwrite) throws AvatarUploadException;

    void deleteAvatar(String userName);

    List<UserProfile> getAllUserProfiles();

    List<UserProfile> getUserProfilesByEnabledProperty(String search, int firstRow, int pageSearch);

    void updateBranchAddress(String branch, Address address);

    void disableUsers(List<String> userNames);

    List<UserProfile> getUserProfilesToDisable(long age);

    List<UserProfile> getUserProfilesToDelete(long age);

    int getEnabledUsersCount();

    int getDisabledUsersCount();

    ObjectId getAvatarId(String userName);
}
