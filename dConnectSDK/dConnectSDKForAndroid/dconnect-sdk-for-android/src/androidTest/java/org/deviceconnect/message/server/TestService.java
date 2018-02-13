package org.deviceconnect.message.server;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

public class TestService extends Service {

    private final IBinder mBinder = new BindServiceBinder();

    private ServiceCallback mServiceCallback;

    @Override
    public IBinder onBind(final Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(final Intent intent) {
        return true;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (mServiceCallback != null) {
            mServiceCallback.onReceivedRequest(this, intent);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder builder = new Notification.Builder(this, "test_channel_id");
            startForeground(1, builder.build());
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void setServiceCallback(final ServiceCallback test) {
        mServiceCallback = test;
    }

    public class BindServiceBinder extends Binder {
        public TestService getService() {
            return TestService.this;
        }
    }

    public interface ServiceCallback {
        void onReceivedRequest(Context context, Intent intent);
    }
}
