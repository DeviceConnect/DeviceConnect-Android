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

    /** Touch profile onTouch cache time. */
    long mOnTouchCacheTime = 0;

    /** Touch profile onTouchStart cache. */
    Bundle mOnTouchStartCache = null;

    /** Touch profile onTouchStart cache time. */
    long mOnTouchStartCacheTime = 0;

    /** Touch profile onTouchEnd cache. */
    Bundle mOnTouchEndCache = null;

    /** Touch profile onTouchEnd cache time. */
    long mOnTouchEndCacheTime = 0;

    /** Touch profile onDoubleTap cache. */
    Bundle mOnDoubleTapCache = null;

    /** Touch profile onDoubleTap cache time. */
    long mOnDoubleTapCacheTime = 0;

    /** Touch profile onTouchMove cache. */
    Bundle mOnTouchMoveCache = null;

    /** Touch profile onTouchMove cache time. */
    long mOnTouchMoveCacheTime = 0;

    /** Touch profile onTouchCancel cache. */
    Bundle mOnTouchCancelCache = null;

    /** Touch profile onTouchCancel cache time. */
    long mOnTouchCancelCacheTime = 0;

    /** Touch profile cache retention time (mSec). */
    static final long CACHE_RETENTION_TIME = 10000;

    /**
     * Get Touch cache data.
     * 
     * @param attr Attribute.
     * @return Touch cache data.
     */
    public Bundle getTouchCache(final String attr) {
        long lCurrentTime = System.currentTimeMillis();
        if (attr.equals(TouchProfile.ATTRIBUTE_ON_TOUCH)) {
            if (lCurrentTime - mOnTouchCacheTime <= CACHE_RETENTION_TIME) {
                return mOnTouchCache;
            } else {
                return null;
            }
        } else if (attr.equals(TouchProfile.ATTRIBUTE_ON_TOUCH_START)) {
            if (lCurrentTime - mOnTouchStartCacheTime <= CACHE_RETENTION_TIME) {
                return mOnTouchStartCache;
            } else {
                return null;
            }
        } else if (attr.equals(TouchProfile.ATTRIBUTE_ON_TOUCH_END)) {
            if (lCurrentTime - mOnTouchEndCacheTime <= CACHE_RETENTION_TIME) {
                return mOnTouchEndCache;
            } else {
                return null;
            }
        } else if (attr.equals(TouchProfile.ATTRIBUTE_ON_DOUBLE_TAP)) {
            if (lCurrentTime - mOnDoubleTapCacheTime <= CACHE_RETENTION_TIME) {
                return mOnDoubleTapCache;
            } else {
                return null;
            }
        } else if (attr.equals(TouchProfile.ATTRIBUTE_ON_TOUCH_MOVE)) {
            if (lCurrentTime - mOnTouchMoveCacheTime <= CACHE_RETENTION_TIME) {
                return mOnTouchMoveCache;
            } else {
                return null;
            }
        } else if (attr.equals(TouchProfile.ATTRIBUTE_ON_TOUCH_CANCEL)) {
            if (lCurrentTime - mOnTouchCancelCacheTime <= CACHE_RETENTION_TIME) {
                return mOnTouchCancelCache;
            } else {
                return null;
            }
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
        long lCurrentTime = System.currentTimeMillis();
        if (attr.equals(TouchProfile.ATTRIBUTE_ON_TOUCH)) {
            mOnTouchCache = touchData;
            mOnTouchCacheTime = lCurrentTime;
        } else if (attr.equals(TouchProfile.ATTRIBUTE_ON_TOUCH_START)) {
            mOnTouchStartCache = touchData;
            mOnTouchStartCacheTime = lCurrentTime;
        } else if (attr.equals(TouchProfile.ATTRIBUTE_ON_TOUCH_END)) {
            mOnTouchEndCache = touchData;
            mOnTouchEndCacheTime = lCurrentTime;
        } else if (attr.equals(TouchProfile.ATTRIBUTE_ON_DOUBLE_TAP)) {
            mOnDoubleTapCache = touchData;
            mOnDoubleTapCacheTime = lCurrentTime;
        } else if (attr.equals(TouchProfile.ATTRIBUTE_ON_TOUCH_MOVE)) {
            mOnTouchMoveCache = touchData;
            mOnTouchMoveCacheTime = lCurrentTime;
        } else if (attr.equals(TouchProfile.ATTRIBUTE_ON_TOUCH_CANCEL)) {
            mOnTouchCancelCache = touchData;
            mOnTouchCancelCacheTime = lCurrentTime;
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
