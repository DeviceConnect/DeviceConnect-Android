/*
 NotifyKeyData.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.linking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.nttdocomo.android.sdaiflib.NotifyNotification;
import com.nttdocomo.android.sdaiflib.Utils;

import org.deviceconnect.android.deviceplugin.linking.BuildConfig;

public class NotifyKeyData {

    private Context mContext;
    private ReceiveNotification2 mReceiver;

    public NotifyKeyData(Context context, NotifyNotification.NotificationInterface observer) {
        mContext = context;
        mReceiver = new ReceiveNotification2(observer);
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.nttdocomo.android.smartdeviceagent.action.NOTIFICATION");
        mContext.registerReceiver(mReceiver, filter);
    }

    public void release() {
        if (mContext != null && mReceiver != null) {
            try {
                mContext.unregisterReceiver(mReceiver);
                mReceiver = null;
            } catch (IllegalArgumentException var2) {
                if (BuildConfig.DEBUG) {
                    Log.w("LinkingPlugIn", "release", var2);
                }
            }
        }
    }

    class ReceiveNotification2 extends BroadcastReceiver {
        private static final String TAG = "Receiver_notify";
        private NotifyNotification.NotificationInterface mObserver;

        public ReceiveNotification2(NotifyNotification.NotificationInterface observer) {
            this.mObserver = observer;
            if (observer == null) {
                Log.d("Receiver_notify", "observer null");
            }

        }

        public ReceiveNotification2() {
            this.mObserver = null;
        }

        public void onReceive(Context context, Intent intent) {
            Log.d("ABC", intent.toString());
            Bundle extra = intent.getExtras();
            if (extra != null) {
                Log.d("Receiver_notify", intent.getExtras().toString());
            }

            String action = intent.getAction();
            if (action != null) {
                if (this.mObserver == null) {
                    Log.d("Receiver_notify", "ERROR:observer null");
                }

                if ("com.nttdocomo.android.smartdeviceagent.action.NOTIFICATION".equals(action)) {
                    Log.d("Receiver_notify", "write!");
                    SharedPreferences preference = context.getSharedPreferences("NotificationInformation", 0);
                    SharedPreferences.Editor editor = preference.edit();
                    editor.putLong("RECEIVE_TIME", Utils.getCurrentTimeLong());
                    editor.putString("APP_NAME", intent.getStringExtra("com.nttdocomo.android.smartdeviceagent.extra.APP_NAME"));
                    editor.putString("CONTENT_URI_1", intent.getStringExtra("com.nttdocomo.android.smartdeviceagent.extra.CONTENT_URI_1"));
                    editor.putString("CONTENT_URI_2", intent.getStringExtra("com.nttdocomo.android.smartdeviceagent.extra.CONTENT_URI_2"));
                    editor.putString("CONTENT_URI_3", intent.getStringExtra("com.nttdocomo.android.smartdeviceagent.extra.CONTENT_URI_3"));
                    editor.putString("CONTENT_URI_4", intent.getStringExtra("com.nttdocomo.android.smartdeviceagent.extra.CONTENT_URI_4"));
                    editor.putString("CONTENT_URI_5", intent.getStringExtra("com.nttdocomo.android.smartdeviceagent.extra.CONTENT_URI_5"));
                    editor.putString("CONTENT_URI_6", intent.getStringExtra("com.nttdocomo.android.smartdeviceagent.extra.CONTENT_URI_6"));
                    editor.putString("CONTENT_URI_7", intent.getStringExtra("com.nttdocomo.android.smartdeviceagent.extra.CONTENT_URI_7"));
                    editor.putString("IMAGE_URI", intent.getStringExtra("com.nttdocomo.android.smartdeviceagent.extra.IMAGE_URI"));
                    editor.putString("IMAGE_TYPE", intent.getStringExtra("com.nttdocomo.android.smartdeviceagent.extra.IMAGE_TYPE"));
                    editor.putString("MEDIA_URI", intent.getStringExtra("com.nttdocomo.android.smartdeviceagent.extra.MEDIA_URI"));
                    editor.putString("MEDIA_TYPE", intent.getStringExtra("com.nttdocomo.android.smartdeviceagent.extra.MEDIA_TYPE"));
                    editor.putInt("NOTIFICATION_ID", intent.getIntExtra("com.nttdocomo.android.smartdeviceagent.extra.NOTIFICATION_ID", -1));
                    editor.putInt("NOTIFICATION_CATEGORY_ID", intent.getIntExtra("com.nttdocomo.android.smartdeviceagent.extra.NOTIFICATION_CATEGORY_ID", -1));
                    editor.putInt("DEVICE_ID", intent.getIntExtra("com.nttdocomo.android.smartdeviceagent.extra.DEVICE_ID", -1));
                    editor.putInt("DEVICE_UID", intent.getIntExtra("com.nttdocomo.android.smartdeviceagent.extra.DEVICE_UID", -1));
                    editor.putInt("DEVICE_BUTTON_ID", intent.getIntExtra("com.nttdocomo.android.smartdeviceagent.extra.NOTIFICATION_DEVICE_BUTTON_ID", -1));
                    editor.commit();
                    if (this.mObserver != null) {
                        this.mObserver.onNotify();
                    }
                }

            }
        }
    }
}
