package org.deviceconnect.android.deviceplugin.alljoyn;

import java.util.Arrays;
import java.util.List;

/**
 * A container class for interface information of a certain AllJoyn service.
 *
 * @author NTT DOCOMO, INC.
 */
public class AllJoynServiceInterfaceSet {
    public final String serviceName;
    public final List<String> interfaces;

    public AllJoynServiceInterfaceSet(String serviceName, String... interfaces) {
        this.serviceName = serviceName;
        this.interfaces = Arrays.asList(interfaces);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
