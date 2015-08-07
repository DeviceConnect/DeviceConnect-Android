/*
 ThetaPhotoEventListener
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta;

/**
 * Listener which subscribes events to notify that a photo is taken by THETA.
 *
 * @author NTT DOCOMO, INC.
 */
public interface ThetaPhotoEventListener {
    void onPhoto(ThetaPhoto photo);
    void onError();
}
