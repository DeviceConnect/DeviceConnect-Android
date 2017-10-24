/*
 HostDeviceApplication.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.linking.LinkingApplication;
import org.deviceconnect.android.logger.AndroidHandler;
import org.deviceconnect.android.profile.BatteryProfile;
import org.deviceconnect.android.profile.KeyEventProfile;
import org.deviceconnect.android.profile.TouchProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static org.deviceconnect.android.deviceplugin.host.profile.HostKeyEventProfile.ATTRIBUTE_ON_KEY_CHANGE;
import static org.deviceconnect.android.deviceplugin.host.profile.HostTouchProfile.ATTRIBUTE_ON_TOUCH_CHANGE;

/**
 * Host Device Plugin Application.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HostDeviceApplication extends LinkingApplication {

    /** Cache retention time (mSec). */
    static final long CACHE_RETENTION_TIME = 10000;

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
    /** Touch profile onTouchChange cache. */
    Bundle mOnTouchChangeCache = null;
    /** Touch profile onTouchChange cache time. */
    long mOnTouchChangeCacheTime = 0;
    /** Touch State start. */
    public static final String STATE_START = "start";
    /** Touch State end. */
    public static final String STATE_END = "end";
    /** Touch State double tap. */
    public static final String STATE_DOUBLE_TAP = "doubletap";
    /** Touch State move. */
    public static final String STATE_MOVE = "move";
    /** Touch State cancel. */
    public static final String STATE_CANCEL = "cancel";

    /**
     * Get Touch cache data.
     * 
     * @param attr Attribute.
     * @return Touch cache data.
     */
    public Bundle getTouchCache(final String attr) {
        long lCurrentTime = System.currentTimeMillis();
        if (attr.equalsIgnoreCase(TouchProfile.ATTRIBUTE_ON_TOUCH)) {
            if (lCurrentTime - mOnTouchCacheTime <= CACHE_RETENTION_TIME) {
                return mOnTouchCache;
            } else {
                return null;
            }
        } else if (attr.equalsIgnoreCase(TouchProfile.ATTRIBUTE_ON_TOUCH_START)) {
            if (lCurrentTime - mOnTouchStartCacheTime <= CACHE_RETENTION_TIME) {
                return mOnTouchStartCache;
            } else {
                return null;
            }
        } else if (attr.equalsIgnoreCase(TouchProfile.ATTRIBUTE_ON_TOUCH_END)) {
            if (lCurrentTime - mOnTouchEndCacheTime <= CACHE_RETENTION_TIME) {
                return mOnTouchEndCache;
            } else {
                return null;
            }
        } else if (attr.equalsIgnoreCase(TouchProfile.ATTRIBUTE_ON_DOUBLE_TAP)) {
            if (lCurrentTime - mOnDoubleTapCacheTime <= CACHE_RETENTION_TIME) {
                return mOnDoubleTapCache;
            } else {
                return null;
            }
        } else if (attr.equalsIgnoreCase(TouchProfile.ATTRIBUTE_ON_TOUCH_MOVE)) {
            if (lCurrentTime - mOnTouchMoveCacheTime <= CACHE_RETENTION_TIME) {
                return mOnTouchMoveCache;
            } else {
                return null;
            }
        } else if (attr.equalsIgnoreCase(TouchProfile.ATTRIBUTE_ON_TOUCH_CANCEL)) {
            if (lCurrentTime - mOnTouchCancelCacheTime <= CACHE_RETENTION_TIME) {
                return mOnTouchCancelCache;
            } else {
                return null;
            }
        } else if (attr.equalsIgnoreCase(ATTRIBUTE_ON_TOUCH_CHANGE)) {
            if (lCurrentTime - mOnTouchChangeCacheTime <= CACHE_RETENTION_TIME) {
                return mOnTouchChangeCache;
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
        if (attr.equalsIgnoreCase(TouchProfile.ATTRIBUTE_ON_TOUCH)) {
            mOnTouchCache = touchData;
            mOnTouchCacheTime = lCurrentTime;
        } else if (attr.equalsIgnoreCase(TouchProfile.ATTRIBUTE_ON_TOUCH_START)) {
            mOnTouchStartCache = touchData;
            mOnTouchStartCacheTime = lCurrentTime;
        } else if (attr.equalsIgnoreCase(TouchProfile.ATTRIBUTE_ON_TOUCH_END)) {
            mOnTouchEndCache = touchData;
            mOnTouchEndCacheTime = lCurrentTime;
        } else if (attr.equalsIgnoreCase(TouchProfile.ATTRIBUTE_ON_DOUBLE_TAP)) {
            mOnDoubleTapCache = touchData;
            mOnDoubleTapCacheTime = lCurrentTime;
        } else if (attr.equalsIgnoreCase(TouchProfile.ATTRIBUTE_ON_TOUCH_MOVE)) {
            mOnTouchMoveCache = touchData;
            mOnTouchMoveCacheTime = lCurrentTime;
        } else if (attr.equalsIgnoreCase(TouchProfile.ATTRIBUTE_ON_TOUCH_CANCEL)) {
            mOnTouchCancelCache = touchData;
            mOnTouchCancelCacheTime = lCurrentTime;
        } else if (attr.equalsIgnoreCase(ATTRIBUTE_ON_TOUCH_CHANGE)) {
            mOnTouchChangeCache = touchData;
            mOnTouchChangeCacheTime = lCurrentTime;
        }
    }

    /** KeyEvent profile onDown cache. */
    Bundle mOnDownCache = null;

    /** KeyEvent profile onDown cache time. */
    static long sOnDownCacheTime = 0;

    /** KeyEvent profile onUp cache. */
    Bundle mOnUpCache = null;

    /** KeyEvent profile onUp cache time. */
    static long sOnUpCacheTime = 0;
    /** KeyEvent profile onKeyChange cache. */
    Bundle mOnKeyChangeCache = null;
    /** KeyEvent profile onKeyChange cache time. */
    static long sOnKeyChangeCacheTime = 0;
    /** KeyEvent State move. */
    public static final String STATE_UP = "up";
    /** KeyEvent State cancel. */
    public static final String STATE_DOWN = "down";
    /**
     * Get KeyEvent cache data.
     * 
     * @param attr Attribute.
     * @return KeyEvent cache data.
     */
    public Bundle getKeyEventCache(final String attr) {
        long lCurrentTime = System.currentTimeMillis();
        if (attr.equalsIgnoreCase(KeyEventProfile.ATTRIBUTE_ON_DOWN)) {
            if (lCurrentTime - sOnDownCacheTime <= CACHE_RETENTION_TIME) {
                return mOnDownCache;
            } else {
                return null;
            }
        } else if (attr.equalsIgnoreCase(KeyEventProfile.ATTRIBUTE_ON_UP)) {
            if (lCurrentTime - sOnUpCacheTime <= CACHE_RETENTION_TIME) {
                return mOnUpCache;
            } else {
                return null;
            }
        } else if (attr.equalsIgnoreCase(ATTRIBUTE_ON_KEY_CHANGE)) {
            if (lCurrentTime - sOnKeyChangeCacheTime <= CACHE_RETENTION_TIME) {
                return mOnKeyChangeCache;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Set KeyEvent data to cache.
     * 
     * @param attr Attribute.
     * @param keyEventData Touch data.
     */
    public void setKeyEventCache(final String attr, final Bundle keyEventData) {
        long lCurrentTime = System.currentTimeMillis();
        if (attr.equalsIgnoreCase(KeyEventProfile.ATTRIBUTE_ON_DOWN)) {
            mOnDownCache = keyEventData;
            sOnDownCacheTime = lCurrentTime;
        } else if (attr.equalsIgnoreCase(KeyEventProfile.ATTRIBUTE_ON_UP)) {
            mOnUpCache = keyEventData;
            sOnUpCacheTime = lCurrentTime;
        } else if (attr.equalsIgnoreCase(ATTRIBUTE_ON_KEY_CHANGE)) {
            mOnKeyChangeCache = keyEventData;
            sOnKeyChangeCacheTime = lCurrentTime;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Logger logger = Logger.getLogger("host.dplugin");
        if (BuildConfig.DEBUG) {
            AndroidHandler handler = new AndroidHandler(logger.getName());
            handler.setFormatter(new SimpleFormatter());
            handler.setLevel(Level.ALL);
            logger.addHandler(handler);
            logger.setLevel(Level.ALL);
        } else {
            logger.setLevel(Level.OFF);
        }
    }
}
