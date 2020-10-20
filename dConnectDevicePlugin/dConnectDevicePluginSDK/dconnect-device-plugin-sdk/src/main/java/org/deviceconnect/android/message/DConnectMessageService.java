/*
 DConnectMessageService.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.message;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

import org.deviceconnect.android.BuildConfig;
import org.deviceconnect.android.IDConnectCallback;
import org.deviceconnect.android.IDConnectPlugin;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.cache.EventCacheController;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.logger.AndroidHandler;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.service.DConnectServiceProvider;
import org.deviceconnect.android.ssl.KeyStoreCallback;
import org.deviceconnect.android.ssl.KeyStoreError;

import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.net.ssl.SSLContext;

/**
 * Device Connectメッセージサービス.
 * 
 * <p>
 * Device Connectリクエストメッセージを受信し、Device Connectレスポンスメッセージを送信するサービスである。<br>
 * {@link DConnectMessageServiceProvider}から呼び出されるサービスとし、UIレイヤーから明示的な呼び出しは行わない。
 * </p>
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class DConnectMessageService extends Service implements DConnectProfileProvider {
    /**
     * ロガー.
     */
    private Logger mLogger = Logger.getLogger("org.deviceconnect.dplugin");

    /**
     * プラグインのコンテキスト.
     */
    private DevicePluginContext mPluginContext;

    /**
     * プラグインにリクエストを配送するスレッド.
     */
    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();

    /**
     * 同じ apk 内からバインドされた場合のバインダー.
     */
    private final IBinder mLocalBinder = new LocalBinder();

    /**
     * 外部からバインドされた場合のバインダー.
     */
    private final IBinder mRemoteBinder = new PluginBinder();

    /**
     * Device Connect Manager がアンインストールされた場合の通知を受け取るレシーバ.
     */
    private final BroadcastReceiver mUninstallReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (mPluginContext != null) {
                mPluginContext.handleMessage(intent);
            }
        }
    };

    @Override
    public IBinder onBind(final Intent intent) {
        if (BuildConfig.DEBUG) {
            mLogger.info("onBind: " + getClass().getName());
        }

        // 同じパッケージから呼び出されている場合
        if (isCalledFromLocal()) {
            if (BuildConfig.DEBUG) {
                mLogger.info("onBind: Local binder");
            }
            return mLocalBinder;
        }

        if (BuildConfig.DEBUG) {
            mLogger.info("onBind: Remote binder");
        }
        return mRemoteBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setLogLevel();

        mPluginContext = createPluginContext();

        if (mPluginContext == null) {
            // プラグインコンテキストが作成できなかったので終了
            mLogger.severe("Failed to create a plugin context.");
            stopSelf();
            return;
        }

        registerReceiver();
    }

    @Override
    public void onDestroy() {
        if (mExecutorService != null) {
            mExecutorService.shutdown();
            mExecutorService = null;
        }

        if (mPluginContext != null) {
            mPluginContext.release();
            mPluginContext = null;
        }

        unregisterReceiver();

        super.onDestroy();
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        super.onStartCommand(intent, flags, startId);
        if (intent == null) {
            mLogger.warning("request intent is null.");
            return START_STICKY;
        }

        String action = intent.getAction();
        if (action == null) {
            mLogger.warning("request action is null.");
            return START_STICKY;
        }

        handleMessage(intent);

        return START_STICKY;
    }

    /**
     * プラグインコンテキストを作成します.
     * <p>
     * オーバーライドされなければ、{@link DefaultPluginContext} を作成します。
     * </p>
     * @return プラグインコンテキスト
     */
    public DevicePluginContext createPluginContext() {
        return new DefaultPluginContext(this);
    }

    /**
     * プラグインコンテキストを取得します.
     *
     * @return プラグインコンテキスト
     */
    public DevicePluginContext getPluginContext() {
        return mPluginContext;
    }

    /**
     * EventCacheControllerのインスタンスを返す.
     *
     * <p>
     * デフォルトではMemoryCacheControllerを使用する.<br>
     * 変更したい場合は本メソッドをオーバーライドすること.
     * </p>
     *
     * @return EventCacheControllerのインスタンス
     */
    protected EventCacheController getEventCacheController() {
        return new MemoryCacheController();
    }

    /**
     * SystemProfileを取得する.
     * <p>
     * SystemProfileは必須実装となるため、本メソッドでSystemProfileのインスタンスを渡すこと。<br>
     * このメソッドで返却したSystemProfileは自動で登録される。
     * </p>
     * @return SystemProfileのインスタンス
     */
    protected abstract SystemProfile getSystemProfile();

    /**
     * 証明書を使用するか確認します.
     * <p>
     * 使用する場合には、このメソッドをオーバーライドして、trueを返却します。
     * </p>
     * @return 使用する場合にはtrue、それ以外はfalse
     */
    protected boolean usesAutoCertificateRequest() {
        return false;
    }

    /**
     * 証明書で使用するキーストアのファイル名を取得します.
     * <p>
     * デフォルトでは、keystore.p12 を使用します。
     * </p>
     * <p>
     * キーストアのファイル名を変更したい場合には、このメソッドをオーバーライドします。
     * </p>
     * @return キーストアのファイル名
     */
    protected String getKeyStoreFileName() {
        return "keystore.p12";
    }

    /**
     * 証明書で使用するエイリアス名を取得します.
     * <p>
     * デフォルトでは、パッケージ名を返却します。
     * </p>
     * <p>
     * エイリアス名を変更したい場合には、このメソッドをオーバーライドします。
     * </p>
     * @return エイリアス名
     */
    protected String getCertificateAlias() {
        return getPackageName();
    }

    /**
     * SSLContext のインスタンスを作成します.
     * <p>
     * プラグイン内で Web サーバを立ち上げて、Managerと同じ証明書を使いたい場合にはこのSSLContext を使用します。
     * </p>
     * @param keyStore キーストア
     * @param password パスワード
     * @return SSLContextのインスタンス
     * @throws GeneralSecurityException SSLContextの作成に失敗した場合に発生
     */
    protected SSLContext createSSLContext(final KeyStore keyStore, final String password) throws GeneralSecurityException {
        return mPluginContext.createSSLContext(keyStore, password);
    }

    /**
     * キーストア作成要求を行います.
     *
     * @param ipAddress IPアドレス
     * @param callback 結果通知用コールバック
     */
    protected void requestKeyStore(final String ipAddress, final KeyStoreCallback callback) {
        mPluginContext.requestKeyStore(ipAddress, callback);
    }

    /// Public Method

    /**
     * サービスを管理するクラスを取得する.
     *
     * @return サービス管理クラス
     */
    public final DConnectServiceProvider getServiceProvider() {
        return mPluginContext.getServiceProvider();
    }

    /**
     * Device Connect Managerにレスポンスを返却するためのメソッド.
     *
     * @param response レスポンス
     * @return 送信成功の場合true、それ以外はfalse
     */
    public final boolean sendResponse(final Intent response) {
        return mPluginContext.sendResponse(response);
    }

    /**
     * Device Connectにイベントを送信する.
     *
     * @param event イベントパラメータ
     * @param accessToken 送り先のアクセストークン
     * @return 送信成功の場合true、アクセストークンエラーの場合はfalseを返す。
     */
    public final boolean sendEvent(final Intent event, final String accessToken) {
        return mPluginContext.sendEvent(event, accessToken);
    }

    /**
     * Device Connectにイベントを送信する.
     *
     * @param event イベントパラメータ
     * @param bundle パラメータ
     * @return 送信成功の場合true、アクセストークンエラーの場合はfalseを返す。
     */
    public final boolean sendEvent(final Event event, final Bundle bundle) {
        return mPluginContext.sendEvent(event, bundle);
    }

    /**
     * Local OAuth使用フラグを設定する.
     * <p>
     * このフラグをfalseに設定することで、LocalOAuthの機能をOFFにすることができる。<br>
     * デフォルトでは、trueになっているので、LocalOAuthが有効になっている。
     * </p>
     * @param use フラグ
     */
    protected void setUseLocalOAuth(final boolean use) {
        mPluginContext.setUseLocalOAuth(use);
    }

    /**
     * Local OAuth使用フラグを取得する.
     *
     * @return 使用する場合にはtrue、それ以外はfalse
     */
    public boolean isUseLocalOAuth() {
        return mPluginContext.isUseLocalOAuth();
    }

    /**
     * Device Connect Manager側で本プラグインが有効になっているかどうかを取得する.
     *
     * @return 有効になっている場合は<code>true</code>, そうでない場合は<code>false</code>
     */
    public boolean isEnabled() {
        return mPluginContext.isEnabled();
    }

    /// DConnectProfileProvider Method

    @Override
    public List<DConnectProfile> getProfileList() {
        return mPluginContext.getProfileList();
    }

    @Override
    public DConnectProfile getProfile(final String name) {
        return mPluginContext.getProfile(name);
    }

    @Override
    public void addProfile(final DConnectProfile profile) {
        mPluginContext.addProfile(profile);
    }

    @Override
    public void removeProfile(final DConnectProfile profile) {
        mPluginContext.removeProfile(profile);
    }

    /// Device Connect Manager からのイベント

    /**
     * Device Connect Managerがアンインストールされた時に呼ばれる処理部.
     * <p>
     * Device Connect Managerがアンインストールされた場合に処理を行いたい場合には、
     * このメソッドをオーバーライドして実装を行うこと。
     * </p>
     */
    protected void onManagerUninstalled() {
        if (BuildConfig.DEBUG) {
            mLogger.info("SDK : onManagerUninstalled");
        }
    }

    /**
     * Device Connect Managerの起動通知を受信した時に呼ばれる処理部.
     * <p>
     * Device Connect Managerが起動された場合に処理を行い場合には、このメソッドをオーバーライドして実装を行うこと。
     * </p>
     */
    protected void onManagerLaunched() {
        if (BuildConfig.DEBUG) {
            mLogger.info("SDK : onManagerLaunched");
        }
    }

    /**
     * Device Connect Managerの正常終了通知を受信した時に呼ばれる処理部.
     * <p>
     * Device Connect Managerが終了された場合に処理を行い場合には、このメソッドをオーバーライドして実装を行うこと。
     * </p>
     */
    protected void onManagerTerminated() {
        if (BuildConfig.DEBUG) {
            mLogger.info("SDK : onManagerTerminated");
        }
    }

    /**
     * Device Connect ManagerのEvent送信経路切断通知を受信した時に呼ばれる処理部.
     * <p>
     * Device Connect ManagerでWebSocketなどが切断され、イベント停止要求が送られてきた場合には、
     * このメソッドをオーバーライドして、イベントの停止処理や後始末の処理を行うこと。
     * </p>
     * @param origin イベント停止が要求されたオリジン
     */
    protected void onManagerEventTransmitDisconnected(final String origin) {
        if (BuildConfig.DEBUG) {
            mLogger.info("SDK : onManagerEventTransmitDisconnected: " + origin);
        }
    }

    /**
     * Device Plug-inへのReset要求を受信した時に呼ばれる処理部.
     * <p>
     * Device Connect Managerからデバイスプラグインのリセット要求が送られてきた場合には、
     * このメソッドをオーバーライドして、再起動処理を行うこと。
     * </p>
     */
    protected void onDevicePluginReset() {
        if (BuildConfig.DEBUG) {
            mLogger.info("SDK : onDevicePluginReset");
        }
    }

    /**
     * Device Connect Managerからプラグイン有効通知を受信した時に呼ばれる処理部.
     */
    protected void onDevicePluginEnabled() {
        if (BuildConfig.DEBUG) {
            mLogger.info("SDK : onEnabled");
        }
    }

    /**
     * Device Connect Managerからプラグイン無効通知を受信した時に呼ばれる処理部.
     */
    protected void onDevicePluginDisabled() {
        if (BuildConfig.DEBUG) {
            mLogger.info("SDK : onDisabled");
        }
    }

    /// 証明書用イベント

    /**
     * キーストアが更新された場合に呼び出されます.
     * <p>
     * キーストアが更新された場合に処理を行いたい場合には、このメソッドをオーバーライドします。
     * </p>
     * @param keyStore キーストア
     * @param cert 証明書
     * @param rootCert ルート証明書
     */
    protected void onKeyStoreUpdated(final KeyStore keyStore, final Certificate cert, final Certificate rootCert) {
    }

    /**
     * キーストアの更新に失敗した場合に呼び出されます.
     * <p>
     * キーストアの更新に失敗した場合に処理を行いたい場合には、このメソッドをオーバーライドします。
     * </p>
     * @param error エラー
     */
    protected void onKeyStoreUpdateError(final KeyStoreError error) {
    }

    /// Private Method

    /**
     * デバッグログの出力レベルを設定する.
     * <p>
     * デバッグフラグがfalseの場合には、ログを出力しないようにすること。
     * </p>
     */
    private void setLogLevel() {
        if (BuildConfig.DEBUG) {
            AndroidHandler handler = new AndroidHandler(mLogger.getName());
            handler.setFormatter(new SimpleFormatter());
            handler.setLevel(Level.ALL);
            mLogger.addHandler(handler);
            mLogger.setLevel(Level.ALL);
            mLogger.setUseParentHandlers(false);
        } else {
            mLogger.setLevel(Level.OFF);
        }
    }

    /**
     * 現在のスレッドがメインスレッドか確認します.
     *
     * @return メインスレッドの場合はtrue、それ以外はfalse
     */
    private boolean isCurrentMainThread() {
        return Thread.currentThread().equals(getMainLooper().getThread());
    }

    /**
     * 送られてきたメッセージを配送します.
     * <p>
     * メインスレッドから呼び出された場合には、別スレッドで実行します。<br>
     * メインスレッド以外から呼び出された場合には、そのままのスレッドで実行します。
     * </p>
     * @param message 送られてきたメッセージ
     */
    private void handleMessage(final Intent message) {
        if (isCurrentMainThread()) {
            mExecutorService.execute(() -> {
                if (mPluginContext != null) {
                    mPluginContext.handleMessage(message);
                }
            });
        } else {
            if (mPluginContext != null) {
                mPluginContext.handleMessage(message);
            }
        }
    }

    /**
     * アンインストールの通知を受け取るレシーバを登録します.
     */
    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED);
        filter.addDataScheme("package");
        registerReceiver(mUninstallReceiver, filter);
    }

    /**
     * アンインストールの通知を受け取るレシーバを解除します.
     */
    private void unregisterReceiver() {
        try {
            unregisterReceiver(mUninstallReceiver);
        } catch (Exception e) {
            // ignore.
        }
    }

    /**
     * 呼び出し元のパッケージ名が同じか確認します.
     *
     * @return 同じ場合にはtrue、それ以外はfalse
     */
    private boolean isCalledFromLocal() {
        return getPackageName().equals(getCallingPackage());
    }

    /**
     * 呼び出し元のパッケージ名を取得します.
     *
     * @return 呼び出し元のパッケージ名
     */
    private String getCallingPackage() {
        return getPackageManager().getNameForUid(Binder.getCallingUid());
    }

    /**
     * DConnectMessageService で使用するプラグインコンテキスト.
     * <p>
     * DevicePluginContext と DConnectMessageService を連携させるための実装になっています。
     * </p>
     */
    private class DefaultPluginContext extends DevicePluginContext {
        /**
         * コンストラクタ.
         * @param context コンテキスト
         */
        DefaultPluginContext(final Context context) {
            super(context);
        }

        @Override
        public EventCacheController getEventCacheController() {
            return DConnectMessageService.this.getEventCacheController();
        }

        @Override
        protected boolean usesAutoCertificateRequest() {
            return DConnectMessageService.this.usesAutoCertificateRequest();
        }

        @Override
        protected String getKeyStoreFileName() {
            return DConnectMessageService.this.getKeyStoreFileName();
        }

        @Override
        protected String getCertificateAlias() {
            return DConnectMessageService.this.getCertificateAlias();
        }

        @Override
        protected SystemProfile getSystemProfile() {
            return DConnectMessageService.this.getSystemProfile();
        }

        @Override
        protected void onManagerUninstalled() {
            DConnectMessageService.this.onManagerUninstalled();
        }

        @Override
        protected void onManagerLaunched() {
            DConnectMessageService.this.onManagerLaunched();
        }

        @Override
        protected void onManagerTerminated() {
            DConnectMessageService.this.onManagerTerminated();
        }

        @Override
        protected void onManagerEventTransmitDisconnected(final String origin) {
            DConnectMessageService.this.onManagerEventTransmitDisconnected(origin);
        }

        @Override
        protected void onDevicePluginReset() {
            DConnectMessageService.this.onDevicePluginReset();
        }

        @Override
        protected void onDevicePluginEnabled() {
            DConnectMessageService.this.onDevicePluginEnabled();
        }

        @Override
        protected void onDevicePluginDisabled() {
            DConnectMessageService.this.onDevicePluginDisabled();
        }

        @Override
        protected void onKeyStoreUpdated(final KeyStore keyStore, final Certificate cert, final Certificate rootCert) {
            DConnectMessageService.this.onKeyStoreUpdated(keyStore, cert, rootCert);
        }

        @Override
        protected void onKeyStoreUpdateError(final KeyStoreError error) {
            DConnectMessageService.this.onKeyStoreUpdateError(error);
        }
    }

    /**
     * Service をバインドするためのクラス.
     * <p>
     * {@link org.deviceconnect.android.ui.activity.DConnectServiceListActivity} で、
     * サービス一覧をを取得するためにバインドされる。
     * </p>
     */
    public class LocalBinder extends IDConnectPlugin.Stub {

        private final IDConnectPlugin.Stub mDelegate = new PluginBinder();

        /**
         * DConnectMessageServiceのインスタンスを取得する.
         * <p>
         * 本メソッドはDevice Connect Manager内部のみで使用される.
         * </p>
         * @return DConnectMessageServiceのインスタンス
         */
        public DConnectMessageService getMessageService() {
            return DConnectMessageService.this;
        }

        @Override
        public void registerCallback(final IDConnectCallback callback) throws RemoteException {
            mDelegate.registerCallback(callback);
        }

        @Override
        public void sendMessage(final Intent message) throws RemoteException {
            mDelegate.sendMessage(message);
        }

        @Override
        public ParcelFileDescriptor readFileDescriptor(final String fileId) throws RemoteException {
            return mDelegate.readFileDescriptor(fileId);
        }
    }

    /**
     * Device Connect Manager と接続するためのバインダー.
     */
    private class PluginBinder extends IDConnectPlugin.Stub {
        @Override
        public void registerCallback(final IDConnectCallback callback) throws RemoteException {
            if (mPluginContext != null) {
                mPluginContext.setIDConnectCallback(callback);
            }
        }

        @Override
        public void sendMessage(final Intent message) throws RemoteException {
            handleMessage(message);
        }

        @Override
        public ParcelFileDescriptor readFileDescriptor(final String fileId) throws RemoteException {
            return null; // 将来的に必要になった場合に実装.
        }
    }
}
