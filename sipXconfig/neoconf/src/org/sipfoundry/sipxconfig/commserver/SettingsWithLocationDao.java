/*
 * Copyright (C) 2011 eZuce Inc., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the AGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.commserver;

import java.util.List;

public interface SettingsWithLocationDao<T extends SettingsWithLocation> {

    public List<T> findAll();

    public List<T> findAll(Location location);

    public T findOrCreate(Location location);

    public void upsert(T bean);
}
