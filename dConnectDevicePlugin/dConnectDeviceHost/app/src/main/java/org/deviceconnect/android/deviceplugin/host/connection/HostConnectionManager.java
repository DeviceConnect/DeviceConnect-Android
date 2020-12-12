package org.deviceconnect.android.deviceplugin.host.connection;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.activity.BluetoothManageActivity;
import org.deviceconnect.android.message.DevicePluginContext;
import org.deviceconnect.android.util.NotificationUtils;

public class HostConnectionManager {
    /** Notification Id */
    private static final int NOTIFICATION_ID = 3527;

    private DevicePluginContext mPluginContext;

    private WifiManager mWifiManager;
    private BluetoothAdapter mBluetoothAdapter;

    private ConnectionEventListener mConnectionEventListener;

    private final BroadcastReceiver mHostConnectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)
                    || WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                postOnChangedWifiStatus();
            } else if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)
                    || BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                postOnChangedBluetoothStatus();
            }
        }
    };

    public HostConnectionManager(DevicePluginContext pluginContext) {
        mPluginContext = pluginContext;
        init();
    }

    private void init() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        mPluginContext.getContext().registerReceiver(mHostConnectionReceiver, filter);

        mWifiManager = (WifiManager) mPluginContext.getContext()
                .getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void destroy() {
        try {
            mPluginContext.getContext().unregisterReceiver(mHostConnectionReceiver);
        } catch (Exception e) {
            // ignore.
        }
    }

    public void setHostConnectionEventListener(ConnectionEventListener listener) {
        mConnectionEventListener = listener;
    }

    /**
     * WiFi接続の状態を取得する.
     *
     * @return Wi-Fi 接続が有効の場合はtrue、それ以外はfalse
     */
    public boolean isWifiEnabled() {
        if (mWifiManager == null) {
            return false;
        }
        return mWifiManager.isWifiEnabled();
    }

    /**
     * WiFi接続の状態を設定する.
     *
     * @param enabled WiFi接続状態
     */
    public boolean setWifiEnabled(final boolean enabled) {
        if (mWifiManager == null) {
            return false;
        }
        return mWifiManager.setWifiEnabled(enabled);
    }

    /**
     * Bluetooth接続の状態を取得する.
     */
    public boolean isBluetoothEnabled() {
        if (mBluetoothAdapter == null) {
            return false;
        }
        return mBluetoothAdapter.isEnabled();
    }

    /**
     * Bluetooth接続の状態を設定する.
     *
     * @param enabled Bluetooth接続状態
     */
    public boolean setBluetoothEnabled(final boolean enabled) {
        if (enabled) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent intent = new Intent();
                intent.setClass(mPluginContext.getContext(), BluetoothManageActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    mPluginContext.getContext().startActivity(intent);
                } else {
                    NotificationUtils.createNotificationChannel(mPluginContext.getContext());
                    NotificationUtils.notify(mPluginContext.getContext(), NOTIFICATION_ID, 0, intent,
                            mPluginContext.getContext().getString(R.string.host_notification_connection_warnning));
                }
            }
            return true;
        } else {
            return mBluetoothAdapter.disable();
        }
    }

    /**
     * Bluetooth Low Energy 接続の状態を取得する.
     */
    public boolean getEnabledOfBluetoothLowEnergy() {
        // Bluetoothが機能していないときはBluetooth LEも機能しない扱いに。
        return (mPluginContext.getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
                && mBluetoothAdapter.isEnabled());
    }

    private void postOnChangedWifiStatus() {
        if (mConnectionEventListener != null) {
            mConnectionEventListener.onChangedWifiStatus();
        }
    }

    private void postOnChangedBluetoothStatus() {
        if (mConnectionEventListener != null) {
            mConnectionEventListener.onChangedBluetoothStatus();
        }
    }

    public interface ConnectionEventListener {
        void onChangedWifiStatus();
        void onChangedBluetoothStatus();
    }
}
