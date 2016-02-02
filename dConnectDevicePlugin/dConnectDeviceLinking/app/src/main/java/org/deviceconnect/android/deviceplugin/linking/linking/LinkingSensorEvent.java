/*
 LinkingSensorEvent.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.linking;


import android.content.Context;
import android.os.Bundle;

public class LinkingSensorEvent extends LinkingEvent {

    public static final String EXTRA_SENSOR = "sensor";
    private LinkingManager mManager;

    LinkingManager.SensorListener mListener = new LinkingManager.SensorListener() {
        @Override
        public void onChangeSensor(LinkingDevice device, LinkingSensorData sensor) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(EXTRA_SENSOR, sensor);
            sendEvent(device, bundle);
        }
    };

    public LinkingSensorEvent(Context context, LinkingDevice device) {
        super(context, device);
        mManager = LinkingManagerFactory.createManager(context);
    }

    @Override
    public void listen() {
        mManager.setSensorListener(mListener);
    }

    @Override
    public void invalidate() {
        mManager.setSensorListener(null);
    }

}
