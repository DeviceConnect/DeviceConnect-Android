package org.deviceconnect.android.deviceplugin.host.connection;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.CellSignalStrength;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyDisplayInfo;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.activity.BluetoothManageActivity;
import org.deviceconnect.android.libmedia.streaming.util.WeakReferenceList;
import org.deviceconnect.android.message.DevicePluginContext;
import org.deviceconnect.android.util.NotificationUtils;

public class HostConnectionManager {
    /** Notification Id */
    private static final int NOTIFICATION_ID = 3527;

    private DevicePluginContext mPluginContext;
    private WifiManager mWifiManager;
    private BluetoothAdapter mBluetoothAdapter;
    private TelephonyManager mTelephonyManager;
    private ConnectivityManager mConnectivityManager;

    private HostNetworkStatsManager mHostNetworkStatsManager;

    private final WeakReferenceList<ConnectionEventListener> mConnectionEventListeners = new WeakReferenceList<>();

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

    private final ConnectivityManager.NetworkCallback mNetworkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
        }

        @Override
        public void onLost(Network network) {
        }

        @Override
        public void onUnavailable() {
        }
    };

    private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @TargetApi(Build.VERSION_CODES.R)
        @Override
        public void onDisplayInfoChanged(@NonNull TelephonyDisplayInfo info) {
            int type = info.getOverrideNetworkType();
            switch (type) {
                case TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_LTE_ADVANCED_PRO:
                    // LTE Advanced Pro（5Ge)
                    postOnChangeMobileNetwork(type);
                    break;
                case TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA:
                    // 5G Sub-6
                    postOnChangeMobileNetwork(type);
                    break;
                case TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA_MMWAVE:
                    // 5G ミリ波
                    postOnChangeMobileNetwork(type);
                    break;
                default:
                    // 5G 以外
                    break;
            }
        }

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                for (CellSignalStrength strength : signalStrength.getCellSignalStrengths()) {
                    int level = strength.getLevel();
                    switch (level) {
                        case CellSignalStrength.SIGNAL_STRENGTH_NONE_OR_UNKNOWN:
                            break;
                        case CellSignalStrength.SIGNAL_STRENGTH_POOR:
                        case CellSignalStrength.SIGNAL_STRENGTH_MODERATE:
                        case CellSignalStrength.SIGNAL_STRENGTH_GOOD:
                        case CellSignalStrength.SIGNAL_STRENGTH_GREAT:
                            break;
                    }
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int level = signalStrength.getLevel();
            }
        }
    };

    public HostConnectionManager(DevicePluginContext pluginContext) {
        mPluginContext = pluginContext;
        init();
    }

    private void init() {
        mWifiManager = (WifiManager) mPluginContext.getContext()
                .getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mConnectivityManager = (ConnectivityManager) mPluginContext.getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        mPluginContext.getContext().registerReceiver(mHostConnectionReceiver, filter);

        mTelephonyManager = (TelephonyManager) mPluginContext.getContext()
                .getSystemService(Context.TELEPHONY_SERVICE);

        if (mTelephonyManager != null) {
            int events = PhoneStateListener.LISTEN_SIGNAL_STRENGTHS;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                events |= PhoneStateListener.LISTEN_DISPLAY_INFO_CHANGED;
            }
            mTelephonyManager.listen(mPhoneStateListener, events);
        }

        mHostNetworkStatsManager = new HostNetworkStatsManager(mPluginContext.getContext());
    }

    public void destroy() {
        try {
            mPluginContext.getContext().unregisterReceiver(mHostConnectionReceiver);
        } catch (Exception e) {
            // ignore.
        }

        if (mTelephonyManager != null) {
            try {
                mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
            } catch (Exception e) {
                // ignore.
            }
        }

        mConnectionEventListeners.clear();
    }

    public void addHostConnectionEventListener(ConnectionEventListener listener) {
        if (listener != null) {
            mConnectionEventListeners.add(listener);
        }
    }

    public void removeHostConnectionEventListener(ConnectionEventListener listener) {
        if (listener != null) {
            mConnectionEventListeners.remove(listener);
        }
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
     *
     * @return 有効の場合はtrue、それ以外はfalse
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
                // Bluetooth の
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
     *
     * @return BLE が有効の場合はtrue、それ以外はfalse
     */
    public boolean getEnabledOfBluetoothLowEnergy() {
        // Bluetoothが機能していないときはBluetooth LEも機能しない扱いに。
        return (mPluginContext.getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
                && mBluetoothAdapter.isEnabled());
    }

    private void postOnChangeMobileNetwork(int type) {
        for (ConnectionEventListener l : mConnectionEventListeners) {
            l.onChangedMobileNetwork(type);
        }
    }

    private void postOnChangedWifiStatus() {
        for (ConnectionEventListener l : mConnectionEventListeners) {
            l.onChangedWifiStatus();
        }
    }

    private void postOnChangedBluetoothStatus() {
        for (ConnectionEventListener l : mConnectionEventListeners) {
            l.onChangedBluetoothStatus();
        }
    }

    public interface ConnectionEventListener {
        void onChangedMobileNetwork(int type);
        void onChangedWifiStatus();
        void onChangedBluetoothStatus();
    }
}
