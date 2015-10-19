/**
 *
 * Copyright (C) 2015 SIPFoundry., certain elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 */
package org.sipfoundry.sipxconfig.kamailio;

import java.util.Collection;
import java.util.Collections;

import org.sipfoundry.sipxconfig.cfgmgt.DeployConfigOnEdit;
import org.sipfoundry.sipxconfig.feature.Feature;
import org.sipfoundry.sipxconfig.setting.PersistableSettings;
import org.sipfoundry.sipxconfig.setting.Setting;

public class KamailioSettings extends PersistableSettings implements DeployConfigOnEdit {
    public static final String LOG_SETTING = "ingress-configuration/SIPX_INGRESS_LOG_LEVEL";
    public static final String SIP_PORT_SETTING = "ingress-configuration/SIPX_INGRESS_TCP_PORT";
    public static final String SIP_UDP_PORT_SETTING = "ingress-configuration/SIPX_INGRESS_UDP_PORT";
    public static final String SIP_SECURE_PORT_SETTING = "ingress-configuration/SIPX_INGRESS_TLS_PORT";
    public static final String TABLE_DIALOG_VERSION = "ingress-configuration/SIPX_INGRESS_TABLE_DIALOG_VERSION";
    public static final String TABLE_DIALOG_VARS_VERSION = "ingress-configuration/SIPX_INGRESS_TABLE_DIALOG_VARS_VERSION";
    public static final String MYSQL_PASSWORD = "ingress-configuration/MYSQL_PASSWORD";

    public int getSipTcpPort() {
        return (Integer) getSettingTypedValue(SIP_PORT_SETTING);
    }

    public int getSipUdpPort() {
        return (Integer) getSettingTypedValue(SIP_UDP_PORT_SETTING);
    }

    public int getSecureSipPort() {
        return (Integer) getSettingTypedValue(SIP_SECURE_PORT_SETTING);
    }
    
    public int getDialogVersion() {
        return (Integer) getSettingTypedValue(TABLE_DIALOG_VERSION);
    }
    
    public int getDialogVarsVersion() {
        return (Integer) getSettingTypedValue(TABLE_DIALOG_VARS_VERSION);
    }

    public String getMysqlPassword() {
        return getSettingValue(MYSQL_PASSWORD);
    }    

    @Override
    protected Setting loadSettings() {
        return getModelFilesContext().loadModelFile("sipxingress/sipxingress.xml");
    }

    @Override
    public Collection<Feature> getAffectedFeaturesOnChange() {
        return Collections.singleton((Feature) KamailioManager.FEATURE);
    }

    @Override
    public String getBeanId() {
        return "kamailioSettings";
    }
}
