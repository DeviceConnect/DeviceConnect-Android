/*
 DeveloperToolGuard.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.protection;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings;

/**
 * 開発者ツールの使用を防止する.
 *
 * @author NTT DOCOMO, INC.
 */
class DeveloperToolGuard extends CopyGuardSetting {

    private static final int FLAG_ADB_ENABLED = 1;

    private static final int FLAG_ADB_DISABLED = 0;

    private final ContentResolver mContentResolver;

    private final ContentObserver mContentObserver;

    private final HandlerThread mHandlerThread;

    private final boolean mIsAvailable;

    private final Context mContext;

    DeveloperToolGuard(final Context context) {
        mContext = context;
        mHandlerThread = new HandlerThread("DeveloperToolGuardThread");
        mHandlerThread.start();
        mContentObserver = new ContentObserver(new Handler(mHandlerThread.getLooper())) {
            @Override
            public void onChange(final boolean selfChange) {
                super.onChange(selfChange);
                notifyOnSettingChange(isEnabled());
            }
        };

        Uri uri = Settings.Global.getUriFor(Settings.Global.ADB_ENABLED);
        mContentResolver = context.getContentResolver();
        mContentResolver.registerContentObserver(uri, false, mContentObserver);
        boolean isAvailable = false;
        try {
            getAdbFlag();
            isAvailable = true;
        } catch (Settings.SettingNotFoundException ignored) {}
        mIsAvailable = isAvailable;
    }

    @Override
    public boolean isAvailable() {
        return mIsAvailable;
    }

    @Override
    public boolean isEnabled() {
        try {
            // ADB機能がOFFになっていれば、開発者ツールによるWebアプリの変更は不可.
            return getAdbFlag() == FLAG_ADB_DISABLED;
        } catch (Settings.SettingNotFoundException e) {
            return false;
        }
    }

    @Override
    public void enable() {
        if (!isEnabled()) {
            Intent intent = new Intent(mContext, DeveloperToolDialogActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mContext, intent);
        }
    }

    @Override
    public void disable() {
        // 何もしない
    }

    private int getAdbFlag() throws Settings.SettingNotFoundException {
        return Settings.Global.getInt(mContentResolver, Settings.Global.ADB_ENABLED);
    }

    private void putAdbFlag(final int flag) {
        Settings.Global.putInt(mContentResolver, Settings.Global.ADB_ENABLED, flag);
    }
}
