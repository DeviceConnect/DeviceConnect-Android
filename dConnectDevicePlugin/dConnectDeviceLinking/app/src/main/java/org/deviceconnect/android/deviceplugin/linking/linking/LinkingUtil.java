/*
 LinkingUtil.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.linking;

import android.content.Context;

import java.util.List;

public final class LinkingUtil {

    private LinkingUtil() {
    }

    public static LinkingDevice getLinkingDevice(Context context, String serviceId) {
        LinkingManager manager = LinkingManagerFactory.createManager(context);
        List<LinkingDevice> list = manager.getDevices();
        for (LinkingDevice device : list) {
            if (device.getBdAddress().equals(serviceId)) {
                return device;
            }
        }
        return null;
    }

    public static boolean hasSensor(LinkingDevice device) {
        return device.getSensor() != null;
    }

    public static boolean hasLED(LinkingDevice device) {
        return device.getIllumination() != null;
    }

    public static boolean hasVibration(LinkingDevice device) {
        return device.getVibration() != null;
    }

}
