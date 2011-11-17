/**
 *
 *
 * Copyright (c) 2010 / 2011 eZuce, Inc. All rights reserved.
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
package org.sipfoundry.sipxconfig.common;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.sipfoundry.sipxconfig.admin.commserver.imdb.AliasMapping;
import org.sipfoundry.sipxconfig.admin.commserver.imdb.DataSet;
import org.sipfoundry.sipxconfig.service.SipxFreeswitchService;

/**
 * General contract for the entities that are inserted into Mongo.
 */
public interface Replicable extends NamedObject {

    /**
     * Set of {@link DataSet}s to be considered for this entity
     * @return
     */
    public Set<DataSet> getDataSets();
    /**
     * Identity of the Mongo entity. It will go in the "ident" field.
     * Not all entities require it.
     * @param domainName
     * @return
     */
    public String getIdentity(String domainName);
    /**
     * Returns a collection of aliases to go in the "als" field.
     * @param domainName
     * @return
     */
    public Collection<AliasMapping> getAliasMappings(String domainName);

    /**
     * Returns a collection of aliases to go in the "als" field.
     * @param domainName
     * @return
     */
    public Collection<AliasMapping> getAliasMappings(String domainName, SipxFreeswitchService fs);
    /**
     * Return true if this entity is to be considered by {link ValidUsers.getValidUsers()}
     * (required by IVR)
     * @return
     */
    public boolean isValidUser();
    /**
     * Returns a Map of properties to be inserted in Mongo as is.
     * Key is the name of the field, the value is the object to be inserted.
     * @param domain
     * @return
     */
    public Map<String, Object> getMongoProperties(String domain);
}
