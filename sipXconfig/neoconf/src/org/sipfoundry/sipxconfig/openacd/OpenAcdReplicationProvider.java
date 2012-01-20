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
package org.sipfoundry.sipxconfig.openacd;

import java.util.ArrayList;
import java.util.List;

import org.sipfoundry.sipxconfig.common.Replicable;
import org.sipfoundry.sipxconfig.common.ReplicableProvider;
import org.sipfoundry.sipxconfig.feature.FeatureManager;
import org.sipfoundry.sipxconfig.freeswitch.FreeswitchFeature;

public class OpenAcdReplicationProvider implements ReplicableProvider {
    private OpenAcdContext m_openAcdContext;
    private FeatureManager m_featureManager;

    @Override
    public List<Replicable> getReplicables() {
        List<Replicable> replicables = new ArrayList<Replicable>();
        if (!m_featureManager.isFeatureEnabled(FreeswitchFeature.FEATURE)) {
            return replicables;
        }
        for (OpenAcdExtension ext : m_openAcdContext.getFreeswitchExtensions()) {
            if (ext.isEnabled()) {
                replicables.add(ext);
            }
        }
        return replicables;
    }

    public void setOpenAcdContext(OpenAcdContext openAcdContext) {
        m_openAcdContext = openAcdContext;
    }

    public void setFeatureManager(FeatureManager featureManager) {
        m_featureManager = featureManager;
    }

}
