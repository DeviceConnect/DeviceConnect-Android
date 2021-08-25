package org.deviceconnect.android.deviceplugin.host.connection;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppOpsManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.telephony.CellSignalStrength;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyDisplayInfo;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.host.HostDeviceApplication;
import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.activity.BluetoothManageActivity;
import org.deviceconnect.android.libmedia.streaming.util.WeakReferenceList;
import org.deviceconnect.android.message.DevicePluginContext;
import org.deviceconnect.android.util.NotificationUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class HostConnectionManager {
    private static final int NOTIFICATION_ID = 3527;

    private final DevicePluginContext mPluginContext;
    private final WifiManager mWifiManager;
    private final BluetoothAdapter mBluetoothAdapter;
    private final ConnectivityManager mConnectivityManager;
    private  TelephonyManager mTelephonyManager;
    private NetworkType mMobileNetworkType = NetworkType.TYPE_NONE;
    private int mStrengthLevel = 0;
    private HostTrafficMonitor mTrafficMonitor;
    private final Handler mCallbackHandler = new Handler(Looper.getMainLooper());
    private final WeakReferenceList<ConnectionEventListener> mConnectionEventListeners = new WeakReferenceList<>();
    private final WeakReferenceList<TrafficEventListener> mTrafficEventListeners = new WeakReferenceList<>();

    private AppOpsManager.OnOpChangedListener mOnOpChangedListener;
    private AppOpsManager mAppOps;

    /**
     * Wi-Fi、Bluetooth の状態遷移のイベントを受け取るための BroadcastReceiver.
     */
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
        public void onAvailable(@NonNull Network network) {
            postOnChangeNetwork();
        }

        @Override
        public void onLost(@NonNull Network network) {
            postOnChangeNetwork();
        }
    };

    private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @TargetApi(Build.VERSION_CODES.R)
        @Override
        public void onDisplayInfoChanged(@NonNull TelephonyDisplayInfo info) {
            switch (info.getOverrideNetworkType()) {
                case TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_LTE_CA:
                    mMobileNetworkType = NetworkType.TYPE_LTE_CA;
                    break;
                case TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_LTE_ADVANCED_PRO:
                    mMobileNetworkType = NetworkType.TYPE_LTE_ADVANCED_PRO;
                    break;
                case TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA:
                    mMobileNetworkType = NetworkType.TYPE_NR_NSA;
                    break;
                case TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA_MMWAVE:
                    mMobileNetworkType = NetworkType.TYPE_NR_NSA_MMWAV;
                    break;
                case TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NONE:
                default:
                    mMobileNetworkType = NetworkType.TYPE_NONE;
                    break;
            }
            postOnChangeNetwork();
        }

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                for (CellSignalStrength strength : signalStrength.getCellSignalStrengths()) {
                    mStrengthLevel = strength.getLevel();
                    switch (mStrengthLevel) {
                        case CellSignalStrength.SIGNAL_STRENGTH_NONE_OR_UNKNOWN:
                        case CellSignalStrength.SIGNAL_STRENGTH_POOR:
                        case CellSignalStrength.SIGNAL_STRENGTH_MODERATE:
                        case CellSignalStrength.SIGNAL_STRENGTH_GOOD:
                        case CellSignalStrength.SIGNAL_STRENGTH_GREAT:
                            break;
                    }
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mStrengthLevel = signalStrength.getLevel();
            }
            postOnChangeNetwork();
        }
    };

    public HostConnectionManager(DevicePluginContext pluginContext) {
        mPluginContext = pluginContext;

        mWifiManager = (WifiManager) mPluginContext.getContext()
                .getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mConnectivityManager = (ConnectivityManager) mPluginContext.getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        registerNetworkCallback();
        registerTelephony();

        startWatchingUsageStats(pluginContext.getContext());
    }

    public void destroy() {
        stopWatchingUsageStats();
        stopTrafficMonitor();
        unregisterNetworkCallback();
        unregisterTelephony();
        mConnectionEventListeners.clear();
        mTrafficEventListeners.clear();
    }

    /**
     * 接続イベントをリスナーを追加します.
     *
     * @param listener 追加するリスナー
     */
    public void addConnectionEventListener(ConnectionEventListener listener) {
        if (listener != null) {
            mConnectionEventListeners.add(listener);
        }
    }

    /**
     * 接続イベントをリスナーを削除します.
     *
     * @param listener 削除するリスナー
     */
    public void removeConnectionEventListener(ConnectionEventListener listener) {
        if (listener != null) {
            mConnectionEventListeners.remove(listener);
        }
    }

    /**
     * 現在有効になっているネットワークを取得します.
     *
     * @return 有効になっているネットワーク名
     */
    public String getActivityNetworkString(NetworkType networkType) {
        Context context = mPluginContext.getContext();
        switch (networkType) {
            case TYPE_MOBILE:
                return context.getString(R.string.host_connection_network_type_mobile);
            case TYPE_WIFI:
                return context.getString(R.string.host_connection_network_type_wifi);
            case TYPE_ETHERNET:
                return context.getString(R.string.host_connection_network_type_ethernet);
            case TYPE_BLUETOOTH:
                return context.getString(R.string.host_connection_network_type_bluetooth);
            case TYPE_LTE_CA:
                return context.getString(R.string.host_connection_network_type_lte);
            case TYPE_LTE_ADVANCED_PRO:
                return context.getString(R.string.host_connection_network_type_lte_advanced);
            case TYPE_NR_NSA:
                return context.getString(R.string.host_connection_network_type_nsa);
            case TYPE_NR_NSA_MMWAV:
                return context.getString(R.string.host_connection_network_type_nsa_mmwav);
            case TYPE_NONE:
            default:
                return context.getString(R.string.host_connection_network_type_no_connect);
        }
    }

    /**
     * 有効になっているネットワークタイプを取得します.
     *
     * @return ネットワークの情報
     */
    public NetworkCaps getNetworkCaps() {
        NetworkCaps caps = new NetworkCaps();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network n = mConnectivityManager.getActiveNetwork();
            NetworkCapabilities capabilities = mConnectivityManager.getNetworkCapabilities(n);
            if (capabilities != null) {
                boolean isWifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
                boolean isMobile = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
                boolean isBluetooth = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH);
                boolean isEthernet = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);
                if (isWifi) {
                    caps.mStrengthLevel = getWifiStrengthLevel();
                    caps.mType = NetworkType.TYPE_WIFI;
                } else if (isEthernet) {
                    caps.mType = NetworkType.TYPE_ETHERNET;
                } else if (isBluetooth) {
                    caps.mType = NetworkType.TYPE_BLUETOOTH;
                } else if (isMobile) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        caps.mStrengthLevel = capabilities.getSignalStrength();
                    } else {
                        caps.mStrengthLevel = mStrengthLevel;
                    }
                    caps.mType = NetworkType.TYPE_MOBILE;
                    if (mMobileNetworkType != null && mMobileNetworkType != NetworkType.TYPE_NONE) {
                        caps.mType = mMobileNetworkType;
                    }
                } else {
                    caps.mType = NetworkType.TYPE_NONE;
                    if (mMobileNetworkType != null && mMobileNetworkType != NetworkType.TYPE_NONE) {
                        caps.mType = mMobileNetworkType;
                    }
                }
                caps.mDownstreamBW = capabilities.getLinkDownstreamBandwidthKbps();
                caps.mUpstreamBW = capabilities.getLinkUpstreamBandwidthKbps();
            }
        } else {
            NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (networkInfo != null) {
                switch (networkInfo.getType()) {
                    case ConnectivityManager.TYPE_MOBILE:
                        caps.mStrengthLevel = mStrengthLevel;
                        caps.mType = NetworkType.TYPE_MOBILE;
                        break;
                    case ConnectivityManager.TYPE_WIFI:
                        caps.mStrengthLevel = getWifiStrengthLevel();
                        caps.mType = NetworkType.TYPE_WIFI;
                        break;
                    case ConnectivityManager.TYPE_ETHERNET:
                        caps.mType = NetworkType.TYPE_ETHERNET;
                        break;
                    case ConnectivityManager.TYPE_BLUETOOTH:
                        caps.mType = NetworkType.TYPE_BLUETOOTH;
                        break;
                }
            }
        }
        return caps;
    }

    /**
     * ネットワークの情報を格納するクラス.
     */
    public class NetworkCaps {
        private NetworkType mType;
        private int mUpstreamBW;
        private int mDownstreamBW;
        private int mStrengthLevel;

        /**
         * ネットワークタイプを取得します.
         *
         * @return ネットワークタイプ
         */
        public NetworkType getType() {
            return mType;
        }

        /**
         * ネットワークタイプの文字列を取得します.
         *
         * @return ネットワークタイプ
         */
        public String getTypeString() {
            return getActivityNetworkString(mType);
        }

        /**
         * upstream の帯域幅を取得します.
         *
         * @return upstream の帯域幅
         */
        public int getUpstreamBW() {
            return mUpstreamBW;
        }

        /**
         * downstream の帯域幅を取得します.
         *
         * @return downstream の帯域幅
         */
        public int getDownstreamBW() {
            return mDownstreamBW;
        }

        /**
         * 電波強度を取得します.
         *
         * @return 電波強度
         */
        public int getStrengthLevel() {
            return mStrengthLevel;
        }

        @NotNull
        @Override
        public String toString() {
            return "NetworkCaps{" +
                    "mType=" + mType +
                    ", mUpstreamBW=" + mUpstreamBW +
                    ", mDownstreamBW=" + mDownstreamBW +
                    ", mStrengthLevel=" + mStrengthLevel +
                    '}';
        }
    }

    public enum NetworkType {
        TYPE_NONE,
        TYPE_MOBILE,
        TYPE_WIFI,
        TYPE_ETHERNET,
        TYPE_BLUETOOTH,
        TYPE_LTE_CA,
        TYPE_LTE_ADVANCED_PRO,
        TYPE_NR_NSA,
        TYPE_NR_NSA_MMWAV
    }

    private boolean mRegisterTelephonyManager;

    private synchronized void registerTelephony() {
        if (mRegisterTelephonyManager) {
            return;
        }

        try {
            mTelephonyManager = (TelephonyManager) mPluginContext.getContext()
                    .getSystemService(Context.TELEPHONY_SERVICE);
            if (mTelephonyManager != null) {
                int events = PhoneStateListener.LISTEN_SIGNAL_STRENGTHS;
                events |= PhoneStateListener.LISTEN_DISPLAY_INFO_CHANGED;
                mTelephonyManager.listen(mPhoneStateListener, events);
                mRegisterTelephonyManager = true;
            }
        } catch (Exception e) {
            // ignore.
        }
    }

    private synchronized void unregisterTelephony() {
        if (mTelephonyManager != null) {
            try {
                mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
            } catch (Exception e) {
                // ignore.
            }
            mRegisterTelephonyManager = false;
        }
    }

    /**
     * ネットワークの接続イベントを受信するための Receiver を登録します.
     */
    private void registerNetworkCallback() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            mPluginContext.getContext().registerReceiver(mHostConnectionReceiver, filter);
        } else {
            NetworkRequest request = new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                    .build();
            mConnectivityManager.registerNetworkCallback(request, mNetworkCallback);
        }
    }

    /**
     * ネットワークの接続イベントを受信するための Receiver を解除します.
     */
    private void unregisterNetworkCallback() {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                mPluginContext.getContext().unregisterReceiver(mHostConnectionReceiver);
            } else {
                mConnectivityManager.unregisterNetworkCallback(mNetworkCallback);
            }
        } catch (Exception e) {
            // ignore.
        }
    }

    /**
     * WiFi接続の状態を取得する.
     *
     * @return Wi-Fi 接続が有効の場合はtrue、それ以外はfalse
     */
    public boolean isWifiEnabled() {
        return mWifiManager != null && mWifiManager.isWifiEnabled();
    }

    /**
     * 接続している WiFi の電波強度を取得します.
     *
     * WiFi の RSSI から下記の範囲で値を返却します。
     * 優れた> -50 dBm
     * 良好-50〜-60 dBm
     * 普通-60〜-70 dBm
     * 弱い<-70dBm
     *
     * @return WiFi の電波強度
     */
    public int getWifiStrengthLevel() {
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            int rssi = wifiInfo.getRssi();
            if (rssi > -50) {
                return 4;
            } else if (rssi > -60) {
                return 3;
            } else if (rssi > -70) {
                return 2;
            } else {
                return 1;
            }
        }
        return 0;
    }

    /**
     * WiFi接続の状態を設定する.
     *
     * @param enabled WiFi接続状態
     */
    public void setWifiEnabled(boolean enabled, Callback callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            callback.onFailure();
        } else {
            boolean result = mWifiManager != null && mWifiManager.setWifiEnabled(enabled);
            if (result) {
                callback.onSuccess();
            } else {
                callback.onFailure();
            }
        }
    }

    /**
     * Bluetooth接続の状態を取得する.
     *
     * @return 有効の場合はtrue、それ以外はfalse
     */
    public boolean isBluetoothEnabled() {
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }

    /**
     * Bluetooth 接続状態を設定する.
     *
     * @param enabled Bluetooth 接続状態
     * @param callback Bluetooth 設定結果を通知するリスナー
     */
    public void setBluetoothEnabled(boolean enabled, Callback callback) {
        if (enabled) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent intent = new Intent();
                intent.setClass(mPluginContext.getContext(), BluetoothManageActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(BluetoothManageActivity.EXTRA_CALLBACK, new ResultReceiver(mCallbackHandler) {
                    @Override
                    protected void onReceiveResult(final int resultCode, final Bundle resultData) {
                        if (resultCode == Activity.RESULT_OK) {
                            callback.onSuccess();
                        } else {
                            callback.onFailure();
                        }
                    }
                });

                if (getApp().isDeviceConnectClassOfTopActivity() || Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    mPluginContext.getContext().startActivity(intent);
                } else {
                    NotificationUtils.createNotificationChannel(mPluginContext.getContext());
                    NotificationUtils.notify(mPluginContext.getContext(), NOTIFICATION_ID, 0, intent,
                            mPluginContext.getContext().getString(R.string.host_notification_connection_warnning));
                }
            } else {
                callback.onSuccess();
            }
        } else {
            boolean result = mBluetoothAdapter != null && mBluetoothAdapter.disable();
            if (result) {
                callback.onSuccess();
            } else {
                callback.onFailure();
            }
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

    /**
     * ネットワーク通信量の計測を開始します.
     *
     * すでに計測を開始している場合には、何も処理を行いません。
     */
    public synchronized void startTrafficMonitor() {
        if (mTrafficMonitor != null) {
            return;
        }
        mTrafficMonitor = new HostTrafficMonitor(mPluginContext.getContext());
        mTrafficMonitor.setOnTrafficListener((trafficList) -> {
            for (TrafficEventListener l : mTrafficEventListeners) {
                l.onTraffic(trafficList);
            }
        });
        mTrafficMonitor.startTimer();
    }

    /**
     * ネットワーク通信量の計測を終了します.
     */
    public synchronized void stopTrafficMonitor() {
        if (mTrafficMonitor == null) {
            return;
        }
        mTrafficMonitor.stopTimer();
        mTrafficMonitor = null;
    }

    /**
     * ネットワーク通信量のリストを取得します.
     *
     * リストには、各ネットワークの通信量が格納されています。
     *
     * {@link #startTrafficMonitor()} が行われていない場合には、空のリストを返却します。
     *
     * @return ネットワーク通信量のリスト
     */
    public synchronized List<HostTraffic> getTrafficList() {
        return mTrafficMonitor != null ? mTrafficMonitor.getTrafficList() : new ArrayList<>();
    }

    /**
     * ネットワーク通信量の計測イベントを受け取るリスナーを追加します.
     *
     * @param listener 追加するリスナー
     */
    public void addTrafficEventListener(TrafficEventListener listener) {
        if (listener != null) {
            mTrafficEventListeners.add(listener);
        }
    }

    /**
     * ネットワーク通信量の計測イベントを受け取るリスナーを削除します.
     *
     * @param listener 削除するリスナー
     */
    public void removeTrafficEventListener(TrafficEventListener listener) {
        if (listener != null) {
            mTrafficEventListeners.remove(listener);
        }
    }

    /**
     * 使用履歴許可の設定を監視を開始します.
     *
     * 使用許可が降りた時に計測を開始します。
     *
     * @param context コンテキスト
     */
    private void startWatchingUsageStats(Context context) {
        if (checkUsageAccessSettings(context)) {
            startTrafficMonitor();
        } else {
            mOnOpChangedListener = (op, packageName) -> {
                if (checkUsageAccessSettings(context)) {
                    startTrafficMonitor();
                }
            };
            mAppOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            if (mAppOps != null) {
                mAppOps.startWatchingMode(AppOpsManager.OPSTR_GET_USAGE_STATS,
                        context.getPackageName(), mOnOpChangedListener);
            }
        }
    }

    /**
     *  使用履歴許可の設定を監視を停止します.
     */
    private void stopWatchingUsageStats() {
        if (mAppOps != null && mOnOpChangedListener != null) {
            mAppOps.stopWatchingMode(mOnOpChangedListener);
        }
    }

    /**
     * 端末の使用履歴を使用するための許可が降りているか確認します.
     *
     * ネットワークの通信量を取得するために端末の使用履歴利用許可が必要になります。
     *
     * @param context コンテキスト
     * @return 許可が降りている場合はtrue、それ以外はfalse
     */
    public static boolean checkUsageAccessSettings(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    /**
     * 端末の使用履歴許可の設定を行う画面を開きます.
     *
     * 端末の使用履歴許可を設定する API がないので、設定画面を開きます。
     *
     * @param context コンテキスト
     */
    public static void openUsageAccessSettings(Context context) {
        HostDeviceApplication app = (HostDeviceApplication) context.getApplicationContext();
        if (app.isDeviceConnectClassOfTopActivity() || Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS,
                    Uri.parse("package:" + context.getPackageName()));
            try {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (Exception e) {
                // アプリのパッケージを指定して、使用履歴許可が開けない場合には
                // 使用履歴許可のアプリ一覧画面へ遷移させる。
                intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    context.startActivity(intent);
                } catch (Exception ignore) {
                    // ignore.
                }
            }
        } else {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            NotificationUtils.createNotificationChannel(context);
            NotificationUtils.notify(context, NOTIFICATION_ID, 0, intent,
                    context.getString(R.string.host_notification_connection_warnning));
        }
    }

    /**
     * READ_PHONE_STATE パーミッションの確認をします.
     *
     * モバイルネットワークの通信量を取得するためには、READ_PHONE_STATE のパーミッションが必要になります。
     *
     * @param context コンテキスト
     * @return パーミッションがある場合にはtrue、それ以外はfalse
     */
    public static boolean hasPermissionToReadPhoneStats(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_DENIED;
    }

    /**
     * READ_PHONE_STATE パーミッションの要求を行います.
     *
     * @param callback コールバック
     */
    public void requestPermission(final PermissionCallback callback) {
        PermissionUtility.requestPermissions(mPluginContext.getContext(), new Handler(Looper.getMainLooper()),
                new String[]{
                        Manifest.permission.READ_PHONE_STATE,
                },
                new PermissionUtility.PermissionRequestCallback() {
                    @Override
                    public void onSuccess() {
                        registerTelephony();
                        callback.onAllowed();
                    }

                    @Override
                    public void onFail(@NonNull String deniedPermission) {
                        callback.onDisallowed();
                    }
                });
    }

    private HostDeviceApplication getApp() {
        return (HostDeviceApplication) mPluginContext.getContext().getApplicationContext();
    }

    private void postOnChangeNetwork() {
        for (ConnectionEventListener l : mConnectionEventListeners) {
            l.onChangedNetwork();
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


    /**
     * パーミッション結果通知用コールバック.
     */
    public interface PermissionCallback {
        /**
         * 許可された場合に呼び出されます.
         */
        void onAllowed();

        /**
         * 拒否された場合に呼び出されます.
         */
        void onDisallowed();
    }

    /**
     * 処理結果を通知するコールバック.
     */
    public interface Callback {
        /**
         * 処理に成功した場合に呼び出します.
         */
        void onSuccess();

        /**
         * 処理に失敗した場合に呼び出します.
         */
        void onFailure();
    }

    /**
     * ネットワーク状況を通知するリスナー.
     */
    public interface ConnectionEventListener {
        /**
         * ネットワークが切り替わったことを通知します.
         */
        void onChangedNetwork();

        /**
         * Wi-Fi の状態が切り替わったことを通知します.
         *
         * Wi-Fi の ON/OFF のイベント。
         */
        void onChangedWifiStatus();

        /**
         * Bluetooth の状態が切り替わったことを通知します.
         *
         * Bluetooth の ON/OFF のイベント。
         */
        void onChangedBluetoothStatus();
    }

    /**
     * 通信量の計測イベントを通知するリスナー.
     */
    public interface TrafficEventListener extends HostTrafficMonitor.OnTrafficListener {
    }
}
