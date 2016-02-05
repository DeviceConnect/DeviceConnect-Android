/*
 LinkingEventListener.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.linking;

import android.os.Bundle;

import org.deviceconnect.android.event.Event;

public interface LinkingEventListener {
    void onReceiveEvent(Event event, Bundle parameters);
}
