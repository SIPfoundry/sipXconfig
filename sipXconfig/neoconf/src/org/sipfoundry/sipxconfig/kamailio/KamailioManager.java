package org.sipfoundry.sipxconfig.kamailio;

import org.sipfoundry.sipxconfig.address.AddressType;
import org.sipfoundry.sipxconfig.feature.LocationFeature;

public interface KamailioManager {
    public static final LocationFeature FEATURE_PROXY = new LocationFeature("kamailioproxy");
    public static final AddressType TCP_PROXY_ADDRESS = AddressType.externalSipTcp("ingressTcp");
    public static final AddressType UDP_PROXY_ADDRESS = AddressType.externalSipUdp("ingressUdp");
    public static final AddressType TLS_PROXY_ADDRESS = AddressType.sipTls("ingressTls");
    
    public static final LocationFeature FEATURE_PRESENCE = new LocationFeature("kamailiopresence");
    public static final AddressType TCP_PRESENCE_ADDRESS = AddressType.externalSipTcp("presenceTcp");
    public static final AddressType UDP_PRESENCE_ADDRESS = AddressType.externalSipUdp("presenceUdp");
    public static final AddressType TLS_PRESENCE_ADDRESS = AddressType.sipTls("presenceTls");

    public KamailioSettings getSettings();

    public void saveSettings(KamailioSettings settings);
    
}
