/*
 HostDeviceApplication.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.deviceconnect.android.logger.AndroidHandler;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Host Device Plugin Application.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HostDeviceApplication extends Application implements Application.ActivityLifecycleCallbacks {

    /** 現在表示されているActivity名. */
    private String mNowTopActivityClassName = "";

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
            logger.setFilter((record) -> false);
        }
        registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onTerminate() {
        unregisterActivityLifecycleCallbacks(this);
        super.onTerminate();
    }

    // Implements Application.ActivityLifecycleCallbacks.

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        mNowTopActivityClassName = activity.getLocalClassName();
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        mNowTopActivityClassName = "";
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }

    /**
     * Host プラグインがサポートしている Activity(Manager含む) の名前を返す.
     *
     * @return 現在表示されている Activity のクラス名
     */
    public String getClassnameOfTopActivity() {
        return mNowTopActivityClassName;
    }

    /**
     * 指定されたクラスが画面に表示されているか確認します.
     *
     * @param clazz クラス
     * @return 画面に表示されている場合はtrue、それ以外はfalse
     */
    public boolean isClassnameOfTopActivity(Class<?> clazz) {
        return clazz != null && isClassnameOfTopActivity(clazz.getName());
    }

    /**
     * 指定されたクラス名が画面に表示されているか確認します.
     *
     * @param className クラス名
     * @return 画面に表示されている場合はtrue、それ以外はfalse
     */
    public boolean isClassnameOfTopActivity(String className) {
        return className != null && className.equals(mNowTopActivityClassName);
    }

    /**
     * Host プラグイン関連の Activity が画面に表示されているか確認します.
     *
     * @return トップにいる場合はtrue、それ以外はfalse
     */
    public boolean isDeviceConnectClassOfTopActivity() {
        return !mNowTopActivityClassName.isEmpty();
    }
}
