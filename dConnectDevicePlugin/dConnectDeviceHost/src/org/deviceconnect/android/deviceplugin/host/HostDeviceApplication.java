/*
 HostDeviceApplication.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host;

import org.deviceconnect.android.profile.BatteryProfile;
import org.deviceconnect.android.profile.TouchProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;

/**
 * Host Device Plugin Application.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HostDeviceApplication extends Application {

    /** Touch profile onTouch cache. */
    Bundle mOnTouchCache = null;
    
    /** Touch profile onTouchStart cache. */
    Bundle mOnTouchStartCache = null;
    
    /** Touch profile onTouchEnd cache. */
    Bundle mOnTouchEndCache = null;
    
    /** Touch profile onDoubleTap cache. */
    Bundle mOnDoubleTapCache = null;
    
    /** Touch profile onTouchMove cache. */
    Bundle mOnTouchMoveCache = null;
    
    /** Touch profile onTouchCancel cache. */
    Bundle mOnTouchCancelCache = null;
    
    /**
     * Get Touch cache data.
     * 
     * @param attr Attribute.
     * @return Touch cache data.
     */
    public Bundle getTouchCache(final String attr) {
        if (attr.equals(TouchProfile.ATTRIBUTE_ON_TOUCH)) {
            return mOnTouchCache;
        } else if (attr.equals(TouchProfile.ATTRIBUTE_ON_TOUCH_START)) {
            return mOnTouchStartCache;
        } else if (attr.equals(TouchProfile.ATTRIBUTE_ON_TOUCH_END)) {
            return mOnTouchEndCache;
        } else if (attr.equals(TouchProfile.ATTRIBUTE_ON_DOUBLE_TAP)) {
            return mOnDoubleTapCache;
        } else if (attr.equals(TouchProfile.ATTRIBUTE_ON_TOUCH_MOVE)) {
            return mOnTouchMoveCache;
        } else if (attr.equals(TouchProfile.ATTRIBUTE_ON_TOUCH_CANCEL)) {
            return mOnTouchCancelCache;
        } else {
            return null;
        }
    }

    /**
     * Set Touch data to cache.
     * 
     * @param attr Attribute.
     * @param touchData Touch data.
     */
    public void setTouchCache(final String attr, final Bundle touchData) {
        if (attr.equals(TouchProfile.ATTRIBUTE_ON_TOUCH)) {
            mOnTouchCache = touchData;
        } else if (attr.equals(TouchProfile.ATTRIBUTE_ON_TOUCH_START)) {
            mOnTouchStartCache = touchData;
        } else if (attr.equals(TouchProfile.ATTRIBUTE_ON_TOUCH_END)) {
            mOnTouchEndCache = touchData;
        } else if (attr.equals(TouchProfile.ATTRIBUTE_ON_DOUBLE_TAP)) {
            mOnDoubleTapCache = touchData;
        } else if (attr.equals(TouchProfile.ATTRIBUTE_ON_TOUCH_MOVE)) {
            mOnTouchMoveCache = touchData;
        } else if (attr.equals(TouchProfile.ATTRIBUTE_ON_TOUCH_CANCEL)) {
            mOnTouchCancelCache = touchData;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // start accept service
        Intent request = new Intent(IntentDConnectMessage.ACTION_PUT);
        request.setClass(this, HostDeviceProvider.class);
        request.putExtra(DConnectMessage.EXTRA_PROFILE, BatteryProfile.PROFILE_NAME);
        sendBroadcast(request);
    }

}
