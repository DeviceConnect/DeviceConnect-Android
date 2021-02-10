/*
 DConnectService.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.security.KeyChain;
import androidx.annotation.Nullable;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.host.HostDevicePlugin;
import org.deviceconnect.android.localoauth.DevicePluginXmlUtil;
import org.deviceconnect.android.manager.core.DConnectManager;
import org.deviceconnect.android.manager.core.DConnectSettings;
import org.deviceconnect.android.manager.core.WebSocketInfoManager;
import org.deviceconnect.android.manager.core.event.KeepAlive;
import org.deviceconnect.android.manager.core.plugin.ConnectionType;
import org.deviceconnect.android.manager.core.plugin.DevicePlugin;
import org.deviceconnect.android.manager.core.plugin.DevicePluginManager;
import org.deviceconnect.android.manager.core.plugin.MessagingException;
import org.deviceconnect.android.manager.core.util.DConnectUtil;
import org.deviceconnect.android.manager.core.util.VersionName;
import org.deviceconnect.android.manager.profile.DConnectSettingProfile;
import org.deviceconnect.android.manager.setting.KeywordDialogActivity;
import org.deviceconnect.android.manager.setting.SettingActivity;
import org.deviceconnect.android.manager.util.NotificationUtil;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.ssl.KeyStoreCallback;
import org.deviceconnect.android.ssl.KeyStoreError;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.deviceconnect.android.manager.core.DConnectConst.EXTRA_EVENT_RECEIVER_ID;

/**
 * {@link DConnectManager} を動作させるためのサービス.
 *
 * @author NTT DOCOMO, INC.
 */
public class DConnectService extends Service {
    /**
     * デバッグフラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * デバッグ用タグ.
     */
    private static final String TAG = "DConnectService";

    /**
     * Notification Id.
     */
    private static final int ONGOING_NOTIFICATION_ID = 4035;

    /**
     * WakeLockのタグを定義する.
     */
    private static final String TAG_WAKE_LOCK = "dconnect:DeviceConnectManager";

    /**
     * ロガー.
     */
    private final Logger mLogger = Logger.getLogger("dconnect.manager");

    /**
     * Device Connect Manager の設定を保持するクラス.
     */
    private DConnectSettings mSettings;

    /**
     * Device Connect Manager 本体.
     */
    private DConnectManager mManager;

    /**
     * Device Connect Manager 本体に設定する Setting プロファイル.
     */
    private DConnectSettingProfile mSettingProfile;

    /**
     * WakeLockのインスタンス.
     */
    private PowerManager.WakeLock mWakeLock;

    /**
     * バインドするためのクラス.
     */
    private final IBinder mLocalBinder = new LocalBinder();

    /**
     * スレッドプール.
     */
    private final ExecutorService mExecutor = Executors.newFixedThreadPool(10);

    /**
     * DConnectServiceとバインドするためのクラス.
     */
    public class LocalBinder extends Binder {
        /**
         * DConnectServiceのインスタンスを取得する.
         *
         * @return DConnectServiceのインスタンス
         */
        public DConnectService getDConnectService() {
            return DConnectService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mLocalBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        DConnectApplication app = (DConnectApplication) getApplication();
        mSettings = app.getSettings();
        mSettingProfile = new DConnectSettingProfile();
        mSettingProfile.start(this, R.drawable.on_icon);
        mManager = new DConnectManager(this, mSettings, app.getPluginManager()) {
            @Override
            public Class<? extends BroadcastReceiver> getDConnectBroadcastReceiverClass() {
                return DConnectBroadcastReceiver.class;
            }

            @Override
            public Class<? extends Activity> getSettingActivityClass() {
                return KeywordDialogActivity.class;
            }

            @Override
            public Class<? extends Activity> getKeywordActivityClass() {
                return SettingActivity.class;
            }
        };
        mManager.addProfile(mSettingProfile);

        // Webサーバの起動フラグがONになっている場合には起動を行う
        if (mSettings.isManagerStartFlag()) {
            startInternal();
        } else {
            NotificationUtil.fakeStartForeground(this,
                    getString(R.string.dconnect_service_on_channel_id),
                    getString(R.string.dconnect_service_on_channel_title),
                    getString(R.string.dconnect_service_on_channel_desc),
                    ONGOING_NOTIFICATION_ID);
        }
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (intent == null) {
            mLogger.warning("intent is null.");
            return START_STICKY;
        }

        String action = intent.getAction();
        if (action == null) {
            mLogger.warning("action is null.");
            return START_STICKY;
        }

        if (!isRunning()) {
            return START_NOT_STICKY;
        }

        if (IntentDConnectMessage.ACTION_KEEPALIVE.equals(action)) {
            onKeepAliveCommand(intent);
            return START_STICKY;
        } else if (checkDConnectMessage(action)) {
            onReceivedMessage(intent);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        stopInternal();
        mSettingProfile.stop();
        mManager = null;
        super.onDestroy();
    }

    private boolean checkDConnectMessage(String action) {
        return (IntentDConnectMessage.ACTION_GET.equalsIgnoreCase(action)
                || IntentDConnectMessage.ACTION_PUT.equalsIgnoreCase(action)
                || IntentDConnectMessage.ACTION_POST.equalsIgnoreCase(action)
                || IntentDConnectMessage.ACTION_DELETE.equalsIgnoreCase(action)
                || IntentDConnectMessage.ACTION_RESPONSE.equals(action)
                || IntentDConnectMessage.ACTION_EVENT.equals(action));
    }

    /**
     * DConnectManagerを起動します.
     */
    private void startManager() {
        mManager.setOnEventListener(new DConnectManager.OnEventListener() {
            @Override
            public void onFinishSearchPlugin() {
                if (DEBUG) {
                    Log.i(TAG, "Finish search plugin.");
                }
//                try {
//                    addDevicePlugin();
//                } catch (Exception e) {
//                    if (DEBUG) {
//                        Log.e(TAG, "search plugin error.", e);
//                    }
//                }
            }

            @Override
            public void onStarted() {
                if (DEBUG) {
                    Log.i(TAG, "DConnectManager is started.");
                }
            }

            @Override
            public void onStopped() {
                if (DEBUG) {
                    Log.i(TAG, "DConnectManager is stopped.");
                }
            }

            @Override
            public void onChangedNetwork() {
                showNotification();
            }

            @Override
            public void onError(final Exception e) {
                if (DEBUG) {
                    Log.e(TAG, "An error occurred in DConnectManager.", e);
                }
                // DConnectManager でエラーが発生したので、開始フラグをOFFにして停止する
                mSettings.setManagerStartFlag(false);
                stopManager();
                stopSelf();
            }
        });

        showNotification();

        try {
            mManager.startDConnect();
        } catch (Exception e) {
            // DConnectManager でエラーが発生したので、開始フラグをOFFにして停止する
            mSettings.setManagerStartFlag(false);
            stopManager();
            stopSelf();

            if (DEBUG) {
                mLogger.warning("Failed to start a DConnectManager." + e.getMessage());
            }
        }
    }
    /**
     * Hostプラグインを追加します.
     */
    private void addDevicePlugin() {
        String packageName = getPackageName();
        String className = HostDevicePlugin.class.getName();

        DevicePlugin plugin;
        try {
            plugin = new DevicePlugin.Builder(this)
                    .setClassName(className)
                    .setPackageName(packageName)
                    .setConnectionType(ConnectionType.DIRECT)
                    .setDeviceName(getString(R.string.app_name_host))
                    .setPluginIconId(R.drawable.dconnect_icon)
                    .setVersionName(org.deviceconnect.android.deviceplugin.host.BuildConfig.VERSION_NAME)
                    .setPluginXml(DevicePluginXmlUtil.getXml(getApplicationContext(),
                            R.xml.org_deviceconnect_android_deviceplugin_host))
                    .setPluginId(DConnectUtil.toMD5(packageName + className))
                    .setPluginSdkVersionName(VersionName.parse("2.0.0"))
                    .addProviderAuthority("org.deviceconnect.android.deviceplugin.host.provider.included")
                    .build();
            mManager.getPluginManager().addDevicePlugin(plugin);
        } catch (UnsupportedEncodingException e) {
            if (DEBUG) {
                Log.e(TAG, "add plugin error.", e);
            }
        } catch (NoSuchAlgorithmException e) {
            if (DEBUG) {
                Log.e(TAG, "add plugin error.", e);
            }
        }
    }
    /**
     * DConnectManagerを停止します.
     */
    private void stopManager() {
        try {
            NotificationUtil.hideNotification(this);
            mManager.stopDConnect();
        } catch (Exception e) {
            // ignore.
        }
    }

    /**
     * 通知バーに IP アドレスを表示します.
     */
    private void showNotification() {
        NotificationUtil.showNotification(DConnectService.this,
                createIPAddress(),
                getString(R.string.dconnect_service_on_channel_id),
                getString(R.string.app_name),
                getString(R.string.dconnect_service_on_channel_title),
                getString(R.string.dconnect_service_on_channel_desc),
                ONGOING_NOTIFICATION_ID);
    }

    /**
     * DConnectManagerにアクセスするためのIPアドレスを作成します.
     *
     * @return IPアドレス
     */
    private String createIPAddress() {
        return DConnectUtil.getIPAddress(this) + ":" + mSettings.getPort();
    }

    /**
     * Keep Alive のメッセージ受信した時の処理を行います.
     *
     * @param intent Keep Alive のメッセージ
     */
    private void onKeepAliveCommand(final Intent intent) {
        String status = intent.getStringExtra(IntentDConnectMessage.EXTRA_KEEPALIVE_STATUS);
        if (status.equals("RESPONSE")) {
            String serviceId = intent.getStringExtra("serviceId");
            if (serviceId != null) {
                KeepAlive keepAlive = mManager.getKeepAliveManager().getKeepAlive(serviceId);
                if (keepAlive != null) {
                    keepAlive.setResponseFlag();
                }
            }
        } else if (status.equals("DISCONNECT")) {
            String receiverId = intent.getStringExtra(EXTRA_EVENT_RECEIVER_ID);
            if (receiverId != null) {
                mManager.disconnectWebSocketWithReceiverId(receiverId);
            }
        }
    }

    /**
     * BroadcastReceiver からのレスポンスを受信した時の処理を行います.
     *
     * @param message Intent
     */
    private void onReceivedMessage(final Intent message) {
        mManager.onReceivedMessage(message);
    }

    /// IBinderを通じて実行されるメソッド

    /**
     * Keep Alive の有効・無効を設定します.
     *
     * @param enable 有効の場合はtrue、無効の場合はfalse
     */
    public void setEnableKeepAlive(final boolean enable) {
        if (enable) {
            mManager.getKeepAliveManager().enableKeepAlive();
        } else {
            mManager.getKeepAliveManager().disableKeepAlive();
        }
    }

    /**
     * プラグインの設定画面を開きます.
     *
     * @param pluginId プラグインID
     */
    public void openPluginSettings(final String pluginId) {
        DevicePlugin plugin = mManager.getPluginManager().getDevicePlugin(pluginId);
        if (plugin == null) {
            return;
        }

        Intent request = new Intent();
        request.setComponent(plugin.getComponentName());
        request.setAction(IntentDConnectMessage.ACTION_PUT);
        SystemProfile.setApi(request, "gotapi");
        SystemProfile.setProfile(request, SystemProfile.PROFILE_NAME);
        SystemProfile.setInterface(request, SystemProfile.INTERFACE_DEVICE);
        SystemProfile.setAttribute(request, SystemProfile.ATTRIBUTE_WAKEUP);
        request.putExtra("pluginId", plugin.getPluginId());
        mExecutor.execute(() -> {
            try {
                plugin.send(request);
            } catch (MessagingException e) {
                if (DEBUG) {
                    mLogger.warning("Failed to send event: action = " + request.getAction()
                            + ", destination = " + plugin.getComponentName());
                }
            }
        });
    }

    /**
     * プラグインの有効・無効を設定します.
     *
     * @param pluginId プラグインID
     * @param enable 有効の場合はtrue、無効の場合はfalse
     */
    public void setEnablePlugin(final String pluginId, final boolean enable) {
        final DevicePlugin plugin = mManager.getPluginManager().getDevicePlugin(pluginId);
        if (plugin == null) {
            return;
        }

        mExecutor.execute(() -> {
            if (enable) {
                plugin.enable();
            } else {
                plugin.disable();
            }
        });
    }

    /**
     * DConnectManagerを起動する.
     */
    public synchronized void startInternal() {
        if (!isRunning()) {
            if (mSettings.enableWakLock()) {
                acquireWakeLock();
            }
            startManager();
        }
    }

    /**
     * DConnectManagerを停止する.
     */
    public synchronized void stopInternal() {
        if (isRunning()) {
            releaseWakeLock();
            stopManager();
        }
    }

    /**
     * ルート証明書を「信頼できる証明書」としてインストールする.
     *
     * <p>
     * インストール前にユーザーに対して、認可ダイアログが表示される.
     * 認可されない場合は、インストールされない.
     * </p>
     */
    public void installRootCertificate() {
        String ipAddress = DConnectUtil.getIPAddress(getApplicationContext());
        mManager.requestKeyStore(ipAddress, new KeyStoreCallback() {
            @Override
            public void onSuccess(final KeyStore keyStore, final Certificate cert, final Certificate rootCert) {
                try {
                    Intent installIntent = KeyChain.createInstallIntent();
                    installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    installIntent.putExtra(KeyChain.EXTRA_NAME, "Device Connect Root CA");
                    installIntent.putExtra(KeyChain.EXTRA_CERTIFICATE, rootCert.getEncoded());
                    startActivity(installIntent);
                } catch (Exception e) {
                    mLogger.log(Level.SEVERE, "Failed to encode server certificate.", e);
                }
            }

            @Override
            public void onError(final KeyStoreError error) {
                mLogger.severe("Failed to encode server certificate: " + error.name());
            }
        });
    }

    /**
     * キーストアをSDカード上のファイルとして出力する.
     *
     * @param dirPath 出力先のディレクトリへのパス
     * @throws IOException 出力に失敗した場合
     */
    public void exportKeyStore(final String dirPath) throws IOException {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("Failed to create dir for keystore export: path = " + dirPath);
            }
        }
        mManager.exportKeyStore(new File(dir, "keystore.p12"));
    }

    /**
     * WakeLockを登録にする.
     * <p>
     * {@link DConnectSettings#enableWakLock()}が{@code false}で、
     * {@link #mWakeLock}が{@code null}の場合のみ新しいWakeLocをします。
     * </p>
     */
    public void acquireWakeLock() {
        if (mWakeLock == null) {
            if (BuildConfig.DEBUG) {
                mLogger.info("DConnectService acquire WakeLock.");
            }

            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (pm != null) {
                mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG_WAKE_LOCK);
                mWakeLock.acquire();
            }
        }
    }

    /**
     * WakeLockを解除する.
     */
    public void releaseWakeLock() {
        if (mWakeLock != null) {
            if (BuildConfig.DEBUG) {
                mLogger.info("DConnectServiceOld release WakeLock.");
            }
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    /**
     * WebSocketを切断する.
     * <p>
     * NOTE: Android 7以降ではメインスレッド上で切断すると例外が発生する場合があるため、
     * 別スレッド上で実行している.
     * </p>
     * @param webSocketId 内部的に発行したWebSocket ID
     */
    public void disconnectWebSocket(final String webSocketId) {
        mExecutor.execute(() -> {
            if (webSocketId != null) {
                mManager.disconnectWebSocket(webSocketId);
            }
        });
    }

    /**
     * RESTfulサーバが動作しているかを確認する.
     *
     * @return 動作している場合にはtrue、それ以外はfalse
     */
    public boolean isRunning() {
        return mManager != null && mManager.isRunning();
    }

    /**
     * WebSocket 管理クラスを取得します.
     * <p>
     * DConnectManager が動作していない場合は null を返却します.
     * </p>
     * @return WebSocketInfoManager
     */
    public WebSocketInfoManager getWebSocketInfoManager() {
        return  mManager.getWebSocketInfoManager();
    }

    /**
     * プラグイン管理クラスを取得します.
     * <p>
     * DConnectManager が動作していない場合は null を返却します.
     * </p>
     * @return プラグイン管理クラス
     */
    public DevicePluginManager getPluginManager() {
        return mManager.getPluginManager();
    }
}
