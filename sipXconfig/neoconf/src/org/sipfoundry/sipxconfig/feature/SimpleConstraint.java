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
package org.sipfoundry.sipxconfig.feature;

import java.util.Collection;

import org.sipfoundry.sipxconfig.commserver.Location;

public class SimpleConstraint implements BundleConstraint {

    @Override
    public boolean isSingleLocation(FeatureManager manager, Feature feature) {
        return false;
    }

    @Override
    public boolean isLocationDependent(FeatureManager manager, Feature feature) {
        return feature instanceof GlobalFeature;
    }

    @Override
    public Collection<Location> getApplicableLocations(FeatureManager manager, Feature feature,
            Collection<Location> locations) {
        return locations;
    }
}
