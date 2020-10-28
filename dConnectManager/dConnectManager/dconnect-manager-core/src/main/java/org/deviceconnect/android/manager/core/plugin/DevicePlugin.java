/*
 DevicePlugin.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.core.plugin;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import org.deviceconnect.android.localoauth.DevicePluginXml;
import org.deviceconnect.android.localoauth.DevicePluginXmlProfile;
import org.deviceconnect.android.manager.core.BuildConfig;
import org.deviceconnect.android.manager.core.util.DConnectUtil;
import org.deviceconnect.android.manager.core.util.VersionName;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * デバイスプラグイン.
 *
 * @author NTT DOCOMO, INC.
 */
public class DevicePlugin extends DConnectService {
    /**
     * ロガー.
     */
    private final Logger mLogger = Logger.getLogger("dconnect.manager");

    /**
     * 接続リトライ回数.
     */
    private static final int MAX_CONNECTION_TRY = 5;

    /**
     * デバイスプラグイン情報.
     */
    private final Info mInfo;

    /**
     * デバイスプラグイン設定.
     */
    private final DevicePluginSetting mSetting;

    /**
     * デバイスプラグイン統計レポート.
     */
    private final CommunicationHistory mHistory;

    /**
     * 接続管理クラス.
     */
    private Connection mConnection;

    /**
     * コンストラクタ.
     *
     * @param info プラグインの情報
     * @param setting プラグインの設定
     * @param report レポート
     */
    private DevicePlugin(final Info info, final DevicePluginSetting setting,
                         final CommunicationHistory report) {
        super(info.mPluginId);
        mInfo = info;
        mSetting = setting;
        mHistory = report;
    }

    /**
     * リソースを破棄する.
     */
    synchronized void dispose() {
        mConnection.disconnect();
        mHistory.clear();
    }

    /**
     * デバイスプラグイン情報を取得する.
     *
     * @return デバイスプラグイン情報
     */
    public Info getInfo() {
        return mInfo;
    }

    /**
     * デバイスプラグイン統計データを取得する.
     *
     * @return デバイスプラグイン統計データ
     */
    public CommunicationHistory getHistory() {
        return mHistory;
    }

    /**
     * デバイスプラグインのパッケージ名を取得する.
     *
     * @return パッケージ名
     */
    public String getPackageName() {
        return mInfo.mPackageName;
    }

    /**
     * デバイスプラグインのクラス名を取得する.
     *
     * @return クラス名
     */
    public String getClassName() {
        return mInfo.mClassName;
    }

    /**
     * ComponentNameを取得する.
     *
     * @return ComponentNameのインスタンス
     */
    public ComponentName getComponentName() {
        return new ComponentName(getPackageName(), getClassName());
    }

    /**
     * デバイスプラグインのバージョン名を取得する.
     *
     * @return バージョン名
     */
    public String getVersionName() {
        return mInfo.mVersionName;
    }

    /**
     * デバイスプラグインIDを取得する.
     *
     * @return デバイスプラグインID
     */
    public String getPluginId() {
        return mInfo.mPluginId;
    }

    /**
     * デバイスプラグイン名を取得する.
     *
     * @return デバイスプラグイン名
     */
    public String getDeviceName() {
        return mInfo.mDeviceName;
    }

    /**
     * デバイスプラグインがサポートするプロファイルの一覧を取得する.
     *
     * @return サポートするプロファイルの一覧
     */
    public List<String> getSupportProfileNames() {
        List<String> result = new ArrayList<>();
        if (mInfo.getSupportedProfiles() != null) {
            result.addAll(mInfo.getSupportedProfiles().keySet());
        }
        return result;
    }

    /**
     * 指定されたリクエストのラウンドトリップ時間を記録する.
     * <p>
     * デバッグビルド時は、ラウンドトリップ時間についての統計とその永続化も行う.
     * </p>
     * @param request プラグインへ送信したリクエスト
     * @param start   リクエスト送信時刻
     * @param end     レスポンス受信時刻
     */
    public void reportRoundTrip(final Intent request, final long start, final long end) {
        String serviceId = getServiceId(request);
        String path = DConnectUtil.convertRequestToString(request);
        CommunicationHistory.Info info = new CommunicationHistory.Info(serviceId, path, start, end);
        mHistory.add(info);

        // 統計を取る.
        if (calculatesStats()) {
            if (BuildConfig.DEBUG) {
                mLogger.info("Plug-in PackageName: " + getPackageName());
                mLogger.info("Request: " + DConnectUtil.convertRequestToString(request));
                mLogger.info("ResponseTime: " + (end - start));
            }

            long baudRate = info.getRoundTripTime();
            long averageBaudRate = getAverageBaudRate();
            long worstBaudRate = getWorstBaudRate();
            if (averageBaudRate == 0) {
                setAverageBaudRate(baudRate);
            } else {
                setAverageBaudRate((baudRate + averageBaudRate) / 2);
            }
            if (worstBaudRate < baudRate) {
                setWorstBaudRate(baudRate);
                setWorstBaudRateRequest(path);
            }
        }
    }

    /**
     * レポートの計算を行うか確認します.
     *
     * @return レポートの計算を行う場合はtrue、それ以外はfalse
     */
    private boolean calculatesStats() {
        return BuildConfig.DEBUG;
    }

    /**
     * タイムアウトのレスポンスをレポートします.
     *
     * @param request リクエスト
     * @param start 開始時間
     */
    public void reportResponseTimeout(final Intent request, final long start) {
        String serviceId = getServiceId(request);
        String path = DConnectUtil.convertRequestToString(request);
        mHistory.add(new CommunicationHistory.Info(serviceId, path, start));
    }

    /**
     * サービスIDを取得します.
     * @param request リクエスト
     * @return サービスID
     */
    private static String getServiceId(final Intent request) {
        return request.getStringExtra(DConnectMessage.EXTRA_SERVICE_ID);
    }

    /**
     * 平均通信速度を取得します.
     *
     * @return 平均の通信速度
     */
    private long getAverageBaudRate() {
        return mHistory.getAverageBaudRate();
    }

    /**
     * 平均の通信速度を設定します.
     *
     * @param averageBaudRate 平均の通信速度
     */
    private void setAverageBaudRate(final long averageBaudRate) {
        mHistory.setAverageBaudRate(averageBaudRate);
    }

    /**
     * 最遅通信速度を取得します.
     *
     * @return 最遅通信速度
     */
    private long getWorstBaudRate() {
        return mHistory.getWorstBaudRate();
    }

    /**
     * 最遅通信速度を設定します.
     *
     * @param worstBaudRate 最遅通信速度
     */
    private void setWorstBaudRate(final long worstBaudRate) {
        mHistory.setWorstBaudRate(worstBaudRate);
    }

    /**
     * 最遅通信速度のリクエストを設定します.
     *
     * @param worstBaudRateRequest 最遅通信速度のリクエスト
     */
    private void setWorstBaudRateRequest(final String worstBaudRateRequest) {
        mHistory.setWorstBaudRateRequest(worstBaudRateRequest);
    }

    /**
     * 指定されたプロファイルをサポートするかどうかを確認する.
     *
     * @param profileName プロファイル名
     * @return サポートする場合は<code>true</code>、そうで無い場合は<code>false</code>
     */
    public boolean supportsProfile(final String profileName) {
        for (String support : getSupportProfileNames()) {
            if (support.equalsIgnoreCase(profileName)) { // MEMO パスの大文字小文字無視
                return true;
            }
        }
        return false;
    }

    /**
     * デバイスプラグインSDKのバージョンを取得する.
     *
     * @return デバイスプラグインSDKのバージョン
     */
    public VersionName getPluginSdkVersionName() {
        return mInfo.mPluginSdkVersionName;
    }

    /**
     * デバイスプラグインのアイコンデータのリソースIDを取得する.
     *
     * @return デバイスプラグインのアイコンデータのリソースID
     */
    public Integer getPluginIconId() {
        return mInfo.mPluginIconId;
    }

    /**
     * デバイスプラグインのアイコンデータを取得する.
     *
     * @param context コンテキスト
     * @return デバイスプラグインのアイコンデータ
     */
    public Drawable getPluginIcon(final Context context) {
        return DConnectUtil.loadPluginIcon(context, this);
    }

    public ConnectionError getCurrentConnectionError() {
        Connection connection = mConnection;
        if (connection == null) {
            return null;
        }
        return connection.getCurrentError();
    }

    /**
     * 連携タイプを取得する.
     *
     * @return 連携タイプ
     */
    public ConnectionType getConnectionType() {
        return mInfo.mConnectionType;
    }

    /**
     * プラグインとの接続を管理するオブジェクトを設定する.
     *
     * @param connection {@link Connection}オブジェクト
     */
    void setConnection(final Connection connection) {
        mConnection = connection;
    }

    /**
     * プラグインが有効であるかどうかを取得する.
     *
     * @return 有効である場合は<code>true</code>、そうでない場合は<code>false</code>
     */
    public boolean isEnabled() {
        return mSetting.isEnabled();
    }

    /**
     * プラグインと通信可能な状態かどうかを取得する.
     *
     * @return 通信可能である場合は<code>true</code>、そうでない場合は<code>false</code>
     */
    public boolean canCommunicate() {
        return isEnabled() && mConnection.getState() == ConnectionState.CONNECTED;
    }

    /**
     * プラグイン有効状態を設定する.
     *
     * @param isEnabled プラグイン有効状態
     */
    private void setEnabled(final boolean isEnabled) {
        mSetting.setEnabled(isEnabled);
    }

    /**
     * 指定された名前のコンテンツプロバイダーを公開しているかどうかを取得する.
     *
     * @param authority authority名
     * @return 公開している場合は <code>true</code>. そうでない場合は <code>false</code>
     */
    public boolean hasContentProvider(final String authority) {
        for (String a : mInfo.mProviderAuthorities) {
            if (a.equals(authority)) {
                return true;
            }
        }
        return false;
    }

    /**
     * プラグインを有効化する.
     * <p>
     * プラグインに有効化イベントを送信します。
     * </p>
     */
    public synchronized void enable() {
        setEnabled(true);
        apply();
        sendEnableState(true);
    }

    /**
     * プラグインを無効化する.
     * <p>
     * プラグインに無効化イベントを送信します。
     * </p>
     */
    public synchronized void disable() {
        setEnabled(false);
        apply();
        sendEnableState(false);
    }

    /**
     * プラグインの設定を適用します.
     */
    public synchronized void apply() {
        if (isEnabled()) {
            if (mConnection.getState() == ConnectionState.DISCONNECTED) {
                tryConnection();
            }
        } else {
            if (mConnection.getState() == ConnectionState.CONNECTED ||
                    mConnection.getState() == ConnectionState.SUSPENDED) {
                mConnection.disconnect();
            }
        }
    }

    /**
     * プラグインとマネージャ間の接続を確立を試みる.
     * <p>
     * 最大試行回数は、定数 MAX_CONNECTION_TRY で定める.
     *
     * @return 接続に成功した場合は<code>true</code>、そうでない場合は<code>false</code>
     */
    private boolean tryConnection() {
        for (int cnt = 0; cnt < MAX_CONNECTION_TRY; cnt++) {
            try {
                mConnection.connect();
                if (BuildConfig.DEBUG) {
                    mLogger.info("Connected to the plug-in: " + getPackageName());
                }
                return true;
            } catch (ConnectingException e) {
                mLogger.log(Level.WARNING, "Failed to connect to the plug-in: " + getPackageName(), e);
            }
        }
        return false;
    }

    /**
     * 接続変更通知リスナーを追加する.
     *
     * @param listener リスナー
     */
    public void addConnectionStateListener(final ConnectionStateListener listener) {
        mConnection.addConnectionStateListener(listener);
    }

    /**
     * 接続変更通知リスナーを解除する.
     *
     * @param listener リスナー
     */
    public void removeConnectionStateListener(final ConnectionStateListener listener) {
        mConnection.removeConnectionStateListener(listener);
    }

    /**
     * プラグインに対してメッセージを送信する.
     *
     * @param message メッセージ
     * @throws MessagingException メッセージ送信に失敗した場合
     */
    public synchronized void send(final Intent message) throws MessagingException {
        try {
            if (!isEnabled()) {
                throw new MessagingException(MessagingException.Reason.NOT_ENABLED);
            }
            switch (mConnection.getState()) {
                case SUSPENDED:
                    if (!tryConnection()) {
                        throw new MessagingException(MessagingException.Reason.CONNECTION_SUSPENDED);
                    }
                    break;
                default:
                    break;
            }
            mConnection.send(message);
        } catch (MessagingException e) {
            mLogger.warning("Failed to send message: plugin = " + mInfo.getPackageName() + "/" + mInfo.getClassName());
            throw e;
        }
    }

    /**
     * WebSocket の切断イベントを送信します.
     *
     * @param origin 切断されたWebSocketのオリジン
     */
    public void sendTransmitDisconnectEvent(final String origin) {
        Bundle extras = new Bundle();
        extras.putString(IntentDConnectMessage.EXTRA_ORIGIN, origin);
        sendManagerEvent(IntentDConnectMessage.ACTION_EVENT_TRANSMIT_DISCONNECT, extras);
    }

    /**
     * Device Connect Manager の起動通知を行う.
     */
    public void sendLaunchedEvent() {
        sendManagerEvent(IntentDConnectMessage.ACTION_MANAGER_LAUNCHED);
    }

    /**
     * Device Connect Manager の終了通知を行う.
     */
    public void sendTerminatedEvent() {
        sendManagerEvent(IntentDConnectMessage.ACTION_MANAGER_TERMINATED);
    }

    /**
     * プラグインに有効化・無効化イベント通知を行う.
     *
     * @param isEnabled trueの場合は有効化、falseの場合は無効化
     */
    private void sendEnableState(boolean isEnabled) {
        String action = isEnabled ? IntentDConnectMessage.ACTION_DEVICEPLUGIN_ENABLED
                : IntentDConnectMessage.ACTION_DEVICEPLUGIN_DISABLED;
        sendManagerEvent(action);
    }

    /**
     * プラグインにリセットイベント通知を行う.
     */
    public void sendResetDevicePlugin() {
        sendManagerEvent(IntentDConnectMessage.ACTION_DEVICEPLUGIN_RESET);
    }

    /**
     * Device Connect Manager のライフサイクルイベントなどをプラグインに送信します.
     *
     * @param action アクション
     */
    private void sendManagerEvent(final String action) {
        sendManagerEvent(action, null);
    }

    /**
     * デバイスプラグインに対して、Device Connect Managerのライフサイクルについての通知を行う.
     *
     * @param action アクション
     * @param extras 送信するデータ
     */
    private void sendManagerEvent(final String action, final Bundle extras) {
        if (mConnection.getState() != ConnectionState.CONNECTED) {
            mLogger.warning("Plugin not connected. " + getComponentName());
            return;
        }

        Intent request = new Intent(action);
        request.setComponent(getComponentName());
        if (extras != null) {
            request.putExtras(extras);
        }
        request.putExtra("pluginId", getPluginId());

        try {
            send(request);
        } catch (MessagingException e) {
            mLogger.warning("Failed to send enable-state to " + getComponentName());
        }
    }

    @Override
    public String toString() {
        return "{\n" +
                "    DeviceName: " + getDeviceName() + "\n" +
                "    ServiceId: " + getPluginId() + "\n" +
                "    Package: " + getPackageName() + "\n" +
                "    Class: " + getClassName() + "\n" +
                "    Version: " + getVersionName() + "\n" +
                "}";
    }

    /**
     * {@link DevicePlugin} インスタンスを生成するためのビルダー.
     */
    public static class Builder {

        /**
         * コンテキスト.
         */
        private final Context mContext;

        /**
         * デバイスプラグイン情報.
         */
        private Info mInfo = new Info();

        /**
         * コンストラクタ.
         *
         * @param context コンテキスト
         */
        public Builder(final Context context) {
            mContext = context;
        }

        public Builder setPackageName(final String packageName) {
            mInfo.mPackageName = packageName;
            return this;
        }

        public Builder setClassName(final String className) {
            mInfo.mClassName = className;
            return this;
        }

        public Builder setVersionName(final String versionName) {
            mInfo.mVersionName = versionName;
            return this;
        }

        public Builder setPluginSdkVersionName(final VersionName pluginSdkVersionName) {
            mInfo.mPluginSdkVersionName = pluginSdkVersionName;
            return this;
        }

        public Builder setPluginId(final String pluginId) {
            mInfo.mPluginId = pluginId;
            return this;
        }

        public Builder setDeviceName(final String deviceName) {
            mInfo.mDeviceName = deviceName;
            return this;
        }

        public Builder setPluginIconId(final Integer pluginIconId) {
            mInfo.mPluginIconId = pluginIconId;
            return this;
        }

        public Builder setPluginXml(final DevicePluginXml xml) {
            mInfo.mPluginXml = xml;
            return this;
        }

        public Builder setConnectionType(final ConnectionType connectionType) {
            mInfo.mConnectionType = connectionType;
            return this;
        }

        public Builder addProviderAuthority(final String authority) {
            mInfo.mProviderAuthorities.add(authority);
            return this;
        }

        /**
         * {@link DevicePlugin}オブジェクトを生成する.
         *
         * @return {@link DevicePlugin}オブジェクト
         */
        public DevicePlugin build() {
            String pluginId = mInfo.mPluginId;
            DevicePluginSetting setting = new DevicePluginSetting(mContext, pluginId);
            CommunicationHistory report = new CommunicationHistory(mContext, pluginId);
            return new DevicePlugin(mInfo, setting, report);
        }
    }

    /**
     * プラグインの静的な情報を提供するクラス.
     */
    public static class Info implements Parcelable {
        /**
         * プラグインの設定ファイル.
         */
        private DevicePluginXml mPluginXml;

        /**
         * プラグインのパッケージ名.
         */
        private String mPackageName;

        /**
         * マネージャからのメッセージを受信するJavaクラス名.
         */
        private String mClassName;

        /**
         * デバイスプラグインのバージョン名.
         */
        private String mVersionName;

        /**
         * プラグインSDKバージョン名.
         */
        private VersionName mPluginSdkVersionName;

        /**
         * プラグインID.
         */
        private String mPluginId;

        /**
         * デバイスプラグイン名.
         */
        private String mDeviceName;

        /**
         * プラグインアイコン.
         */
        private Integer mPluginIconId;

        /**
         * 接続タイプ.
         */
        private ConnectionType mConnectionType;

        /**
         * 公開コンテンツプロバイダーの名前のリスト.
         */
        private List<String> mProviderAuthorities = new ArrayList<>();

        public int getPluginXmlId() {
            return mPluginXml.getResourceId();
        }

        public String getPackageName() {
            return mPackageName;
        }

        public String getClassName() {
            return mClassName;
        }

        public String getVersionName() {
            return mVersionName;
        }

        public VersionName getPluginSdkVersionName() {
            return mPluginSdkVersionName;
        }

        public String getPluginId() {
            return mPluginId;
        }

        public String getDeviceName() {
            return mDeviceName;
        }

        public Integer getPluginIconId() {
            return mPluginIconId;
        }

        public DevicePluginXml getPluginXml() {
            return mPluginXml;
        }

        public Map<String, DevicePluginXmlProfile> getSupportedProfiles() {
            return mPluginXml.getSupportedProfiles();
        }

        public ConnectionType getConnectionType() {
            return mConnectionType;
        }

        public List<String> getProviderAuthorities() {
            return mProviderAuthorities;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(this.mPluginXml, flags);
            dest.writeString(this.mPackageName);
            dest.writeString(this.mClassName);
            dest.writeString(this.mVersionName);
            dest.writeParcelable(this.mPluginSdkVersionName, flags);
            dest.writeString(this.mPluginId);
            dest.writeString(this.mDeviceName);
            dest.writeValue(this.mPluginIconId);
            dest.writeInt(this.mConnectionType == null ? -1 : this.mConnectionType.ordinal());
            dest.writeStringList(this.mProviderAuthorities);
        }

        public Info() {
        }

        protected Info(Parcel in) {
            this.mPluginXml = in.readParcelable(DevicePluginXml.class.getClassLoader());
            this.mPackageName = in.readString();
            this.mClassName = in.readString();
            this.mVersionName = in.readString();
            this.mPluginSdkVersionName = in.readParcelable(VersionName.class.getClassLoader());
            this.mPluginId = in.readString();
            this.mDeviceName = in.readString();
            this.mPluginIconId = (Integer) in.readValue(Integer.class.getClassLoader());
            int tmpMConnectionType = in.readInt();
            this.mConnectionType = tmpMConnectionType == -1 ? null : ConnectionType.values()[tmpMConnectionType];
            this.mProviderAuthorities = in.createStringArrayList();
        }

        public static final Creator<Info> CREATOR = new Creator<Info>() {
            @Override
            public Info createFromParcel(Parcel source) {
                return new Info(source);
            }

            @Override
            public Info[] newArray(int size) {
                return new Info[size];
            }
        };
    }
}
