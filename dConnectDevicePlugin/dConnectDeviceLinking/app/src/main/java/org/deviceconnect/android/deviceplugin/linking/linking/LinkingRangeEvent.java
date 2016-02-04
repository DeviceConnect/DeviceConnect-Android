/*
 LinkingRangeEvent.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.linking;

import android.content.Context;
import android.os.Bundle;

public class LinkingRangeEvent extends LinkingEvent {

    public final static String EXTRA_RANGE = "range";
    private LinkingManager mManager;

    LinkingManager.RangeListener mListener = new LinkingManager.RangeListener() {
        @Override
        public void onChangeRange(LinkingDevice device, LinkingManager.Range range) {
            Bundle parameters = new Bundle();
            parameters.putInt(EXTRA_RANGE, range.ordinal());
            sendEvent(device, parameters);
        }
    };

    public LinkingRangeEvent(Context context, LinkingDevice device) {
        super(context, device);
        mManager = LinkingManagerFactory.createManager(context);
    }

    @Override
    public void listen() {
        mManager.setRangeListener(mListener);
    }

    @Override
    public void invalidate() {
        mManager.setRangeListener(null);
    }

}
