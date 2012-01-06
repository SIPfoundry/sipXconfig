/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.acd.stats.historical;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.feature.LocationFeature;

public interface AcdHistoricalStats {
    public static final LocationFeature FEATURE = new LocationFeature("acdHistory");

    public static final String BEAN_NAME = "acdHistoricalStats";

    public List<String> getReports();

    public List<String> getReportFields(String reportName);

    public List<Map<String, Object>> getReport(String name, Date startTime, Date endTime, Location location);

    public void dumpReport(Writer writer, List<Map<String, Object>> reportData, Locale locale) throws IOException;

    public AcdHistoricalSettings getSettings();

    public void saveSettings(AcdHistoricalSettings settings);
}
