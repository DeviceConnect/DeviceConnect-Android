/*
 LinkingManager.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.linking;

import java.util.List;

public interface LinkingManager {

    interface ConnectListener {
        void onConnect(LinkingDevice device);

        void onDisconnect(LinkingDevice device);
    }

    enum Range {
        IMMEDIATE, NEAR, FAR, UNKNOWN
    }

    interface RangeListener {
        void onChangeRange(LinkingDevice device, Range range);
    }

    interface SensorListener {
        void onChangeSensor(LinkingDevice device, LinkingSensorData sensor);
    }

    List<LinkingDevice> getDevices();

    void sendNotification(LinkingDevice device, LinkingNotification notification);

    void setConnectListener(ConnectListener listener);

    void setRangeListener(RangeListener listener);

    void setSensorListener(SensorListener listener);

    void sendLEDCommand(LinkingDevice device, boolean on);

    void sendVibrationCommand(LinkingDevice device, boolean on);

}
