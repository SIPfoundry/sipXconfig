/*
 * Copyright (C) 2011 eZuce Inc., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the AGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.nattraversal;

import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.feature.FeatureListener;
import org.sipfoundry.sipxconfig.feature.FeatureManager;
import org.sipfoundry.sipxconfig.feature.GlobalFeature;
import org.sipfoundry.sipxconfig.feature.LocationFeature;
import org.sipfoundry.sipxconfig.feature.FeatureListener.FeatureEvent;
import org.sipfoundry.sipxconfig.setting.BeanWithSettingsDao;

public class NatTraversalImpl implements FeatureListener, NatTraversal {
    private BeanWithSettingsDao<NatSettings> m_settingsDao;

    public NatSettings getSettings() {
        return m_settingsDao.findOrCreateOne();
    }

    public void saveSettings(NatSettings settings) {
        m_settingsDao.upsert(settings);
    }

    @Override
    public void enableLocationFeature(FeatureManager manager, FeatureEvent event, LocationFeature feature,
            Location location) {
    }

    @Override
    public void enableGlobalFeature(FeatureManager manager, FeatureEvent event, GlobalFeature feature) {
        if (feature.equals(FEATURE)) {
            if (event == FeatureEvent.PRE_ENABLE) {
                NatSettings settings = getSettings();
                if (!settings.isBehindNat()) {
                    settings.setBehindNat(true);
                    saveSettings(settings);
                }
            } else if (event == FeatureEvent.PRE_DISABLE) {
                NatSettings settings = getSettings();
                if (settings.isBehindNat()) {
                    settings.setBehindNat(false);
                    saveSettings(settings);
                }
            }
        }
    }

    public void setSettingsDao(BeanWithSettingsDao<NatSettings> settingsDao) {
        m_settingsDao = settingsDao;
    }

}
