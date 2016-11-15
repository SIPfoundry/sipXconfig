package org.sipfoundry.sipxconfig.oss;

import org.sipfoundry.sipxconfig.address.AddressType;
import org.sipfoundry.sipxconfig.feature.LocationFeature;

public interface OSSCoreManager {
	public static final LocationFeature FEATURE = new LocationFeature("sbc");
    public static final AddressType PUBLIC_TCP_ADDRESS = AddressType.externalSipTcp("sbcPublicTCP");
    public static final AddressType PUBLIC_UDP_ADDRESS = AddressType.externalSipUdp("sbcPublicUDP");
    public static final AddressType PUBLIC_TLS_ADDRESS = AddressType.sipTls("sbcPublicTLS");
    
    public static final AddressType INTERNAL_TCP_ADDRESS = AddressType.sipTcp("sbcInternalTCP");
    public static final AddressType INTERNAL_UDP_ADDRESS = AddressType.sipUdp("sbcInternalUDP");
    
    public static final AddressType PUBLIC_RTP_ADDRESS = new AddressType("natRtp", "rtp:%s:%d", 30000,
            AddressType.Protocol.udp);

    public OSSCoreSettings getSettings();

    public void saveSettings(OSSCoreSettings settings);
}
