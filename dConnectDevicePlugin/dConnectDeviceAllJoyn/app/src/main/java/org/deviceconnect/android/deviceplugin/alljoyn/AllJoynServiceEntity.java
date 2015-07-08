package org.deviceconnect.android.deviceplugin.alljoyn;

import org.alljoyn.bus.Variant;
import org.alljoyn.services.common.BusObjectDescription;

import java.util.Map;

/**
 * AllJoynServiceEntitiy represents an AllJoyn service.
 *
 * @author NTT DOCOMO, INC.
 */
public class AllJoynServiceEntity {
    /**
     * Human-friendly service name.
     */
    public String serviceName;
    public String busName;
    public short port;
    public Map<String, Variant> aboutData;

    //    public String objPath;
    public BusObjectDescription[] proxyObjects;

    public Integer sessionId;
//    public List<Class<?>> proxyObjects = new LinkedList<>();
}
