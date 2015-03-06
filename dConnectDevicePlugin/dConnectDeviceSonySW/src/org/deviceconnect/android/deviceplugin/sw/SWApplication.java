/*
 SWApplication.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sw;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.deviceconnect.android.logger.AndroidHandler;
import org.deviceconnect.android.profile.TouchProfile;

import android.app.Application;
import android.os.Bundle;

/**
 * SonyWatchDevicePlugin_LoggerLevelSetting.
 */
public class SWApplication extends Application {

    /** Touch profile onTouch cache. */
    static Bundle sOnTouchCache = null;

    /** Touch profile onTouchStart cache. */
    static Bundle sOnTouchStartCache = null;

    /** Touch profile onTouchEnd cache. */
    static Bundle sOnTouchEndCache = null;

    /** Touch profile onDoubleTap cache. */
    static Bundle sOnDoubleTapCache = null;

    /** Touch profile onTouchMove cache. */
    static Bundle sOnTouchMoveCache = null;

    /** Touch profile onTouchCancel cache. */
    static Bundle sOnTouchCancelCache = null;

    /**
     * Get Touch cache data.
     * 
     * @param attr Attribute.
     * @return Touch cache data.
     */
    public static Bundle getTouchCache(final String attr) {
        if (attr.equals(TouchProfile.ATTRIBUTE_ON_TOUCH)) {
            return sOnTouchCache;
        } else if (attr.equals(TouchProfile.ATTRIBUTE_ON_TOUCH_START)) {
            return sOnTouchStartCache;
        } else if (attr.equals(TouchProfile.ATTRIBUTE_ON_TOUCH_END)) {
            return sOnTouchEndCache;
        } else if (attr.equals(TouchProfile.ATTRIBUTE_ON_DOUBLE_TAP)) {
            return sOnDoubleTapCache;
        } else if (attr.equals(TouchProfile.ATTRIBUTE_ON_TOUCH_MOVE)) {
            return sOnTouchMoveCache;
        } else if (attr.equals(TouchProfile.ATTRIBUTE_ON_TOUCH_CANCEL)) {
            return sOnTouchCancelCache;
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
    public static void setTouchCache(final String attr, final Bundle touchData) {
        if (attr.equals(TouchProfile.ATTRIBUTE_ON_TOUCH)) {
            sOnTouchCache = touchData;
        } else if (attr.equals(TouchProfile.ATTRIBUTE_ON_TOUCH_START)) {
            sOnTouchStartCache = touchData;
        } else if (attr.equals(TouchProfile.ATTRIBUTE_ON_TOUCH_END)) {
            sOnTouchEndCache = touchData;
        } else if (attr.equals(TouchProfile.ATTRIBUTE_ON_DOUBLE_TAP)) {
            sOnDoubleTapCache = touchData;
        } else if (attr.equals(TouchProfile.ATTRIBUTE_ON_TOUCH_MOVE)) {
            sOnTouchMoveCache = touchData;
        } else if (attr.equals(TouchProfile.ATTRIBUTE_ON_TOUCH_CANCEL)) {
            sOnTouchCancelCache = touchData;
        }
    }

    /** ロガー. */
    private Logger mLogger = Logger.getLogger(SWConstants.LOGGER_NAME);

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            AndroidHandler handler = new AndroidHandler(SWConstants.LOGGER_NAME);
            handler.setFormatter(new SimpleFormatter());
            handler.setLevel(Level.ALL);
            mLogger.addHandler(handler);
            mLogger.setLevel(Level.ALL);
        } else {
            mLogger.setLevel(Level.OFF);
        }
    }

}
