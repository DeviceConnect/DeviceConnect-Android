/*
 DevicePlugin.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.plugin;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateFormat;

import org.deviceconnect.android.localoauth.DevicePluginXml;
import org.deviceconnect.android.localoauth.DevicePluginXmlProfile;
import org.deviceconnect.android.manager.util.DConnectUtil;
import org.deviceconnect.android.manager.util.VersionName;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * デバイスプラグイン.
 * @author NTT DOCOMO, INC.
 */
public class DevicePlugin {

    /** 接続リトライ回数. */
    private static final int MAX_CONNECTION_TRY = 5;

    /** デバイスプラグイン情報. */
    private final Info mInfo;
    /** デバイスプラグイン設定 */
    private final DevicePluginSetting mSetting;
    /** 接続管理クラス. */
    private Connection mConnection;
    /** ロガー. */
    private final Logger mLogger = Logger.getLogger("dconnect.manager");

    private DevicePlugin(final Info info,
                         final DevicePluginSetting setting) {
        mInfo = info;
        mSetting = setting;

        mInfo.mAverageBaudRate = mSetting.getAverageBaudRate();
        mInfo.mWorstBaudRate = mSetting.getWorstBaudRate();
        mInfo.mWorstBaudRateRequest = mSetting.getWorstRequest();
    }

    /**
     * リソースを破棄する.
     */
    synchronized void dispose() {
        mConnection.disconnect();
        mSetting.clear();
    }

    /**
     * デバイスプラグイン情報を取得する.
     * @return デバイスプラグイン情報
     */
    public Info getInfo() {
        return mInfo;
    }

    /**
     * デバイスプラグインのパッケージ名を取得する.
     * @return パッケージ名
     */
    public String getPackageName() {
        return mInfo.mPackageName;
    }

    /**
     * デバイスプラグインのクラス名を取得する.
     * @return クラス名
     */
    public String getClassName() {
        return mInfo.mClassName;
    }

    /**
     * ComponentNameを取得する.
     * @return ComponentNameのインスタンス
     */
    public ComponentName getComponentName() {
        return new ComponentName(getPackageName(), getClassName());
    }

    /**
     * デバイスプラグインのバージョン名を取得する.
     * @return バージョン名
     */
    public String getVersionName() {
        return mInfo.mVersionName;
    }

    /**
     * デバイスプラグインIDを取得する.
     * @return デバイスプラグインID
     */
    public String getPluginId() {
        return mInfo.mPluginId;
    }

    /**
     * デバイスプラグイン名を取得する.
     * @return デバイスプラグイン名
     */
    public String getDeviceName() {
        return mInfo.mDeviceName;
    }

    /**
     * デバイスプラグインがサポートするプロファイルの一覧を取得する.
     * @return サポートするプロファイルの一覧
     */
    public List<String> getSupportProfileNames() {
        List<String> result = new ArrayList<>();
        if (mInfo.getSupportedProfiles() != null) {
            for (String profileName : mInfo.getSupportedProfiles().keySet()) {
                result.add(profileName);
            }
        }
        return result;
    }

    /**
     * 通信速度を保持します.
     * @param request 通信を行ったリクエスト
     * @param baudRate 通信時間
     */
    public void addBaudRate(final Intent request, final long baudRate) {
        long averageBaudRate = getAverageBaudRate();
        long worstBaudRate = getWorstBaudRate();
        String path = DConnectUtil.convertRequestToString(request);
        if (averageBaudRate == 0) {
            setAverageBaudRate(baudRate);
        } else {
            setAverageBaudRate((baudRate + averageBaudRate) / 2);
        }
        if (worstBaudRate < baudRate) {
            setWorstBaudRate(baudRate);
            setWorstBaudRateRequest(path);
        }

        mInfo.mBaudRates.add(new BaudRate(path, baudRate, System.currentTimeMillis()));
        if (mInfo.mBaudRates.size() > 10) {
            mInfo.mBaudRates.remove(0);
        }
    }

    /**
     * 平均通信速度を取得します.
     * @return 平均の通信速度
     */
    private long getAverageBaudRate() {
        return mInfo.mAverageBaudRate;
    }

    /**
     * 平均の通信速度を設定します.
     * @param averageBaudRate 平均の通信速度
     */
    private void setAverageBaudRate(final long averageBaudRate) {
        mInfo.mAverageBaudRate = averageBaudRate;
        mSetting.setAverageBaudRate(averageBaudRate);
    }

    /**
     * 最遅通信速度を取得します.
     * @return 最遅通信速度
     */
    private long getWorstBaudRate() {
        return mInfo.mWorstBaudRate;
    }

    /**
     * 最遅通信速度を設定します.
     * @param worstBaudRate 最遅通信速度
     */
    private void setWorstBaudRate(final long worstBaudRate) {
        mInfo.mWorstBaudRate = worstBaudRate;
        mSetting.setWorstBaudRate(worstBaudRate);
    }

    /**
     * 最遅通信速度のリクエストを設定します.
     * @param worstBaudRateRequest 最遅通信速度のリクエスト
     */
    private void setWorstBaudRateRequest(final String worstBaudRateRequest) {
        mInfo.mWorstBaudRateRequest = worstBaudRateRequest;
        mSetting.setWorstRequest(worstBaudRateRequest);
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
     * @return デバイスプラグインSDKのバージョン
     */
    public VersionName getPluginSdkVersionName() {
        return mInfo.mPluginSdkVersionName;
    }

    /**
     * デバイスプラグインのアイコンデータのリソースIDを取得する.
     * @return デバイスプラグインのアイコンデータのリソースID
     */
    public Integer getPluginIconId() {
        return mInfo.mPluginIconId;
    }

    /**
     * デバイスプラグインのアイコンデータを取得する.
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
     * @return 連携タイプ
     */
    public ConnectionType getConnectionType() {
        return mInfo.mConnectionType;
    }

    /**
     * プラグインとの接続を管理するオブジェクトを設定する.
     * @param connection {@link Connection}オブジェクト
     */
    void setConnection(final Connection connection) {
        mConnection = connection;
    }

    /**
     * プラグインが有効であるかどうかを取得する.
     * @return 有効である場合は<code>true</code>、そうでない場合は<code>false</code>
     */
    public boolean isEnabled() {
        return mSetting.isEnabled();
    }

    /**
     * プラグインと通信可能な状態かどうかを取得する.
     * @return 通信可能である場合は<code>true</code>、そうでない場合は<code>false</code>
     */
    public boolean canCommunicate() {
        return isEnabled() && mConnection.getState() == ConnectionState.CONNECTED;
    }

    /**
     * プラグイン有効状態を設定する.
     * @param isEnabled プラグイン有効状態
     */
    public void setEnabled(final boolean isEnabled) {
        mSetting.setEnabled(isEnabled);
    }

    /**
     * プラグインを有効化する.
     */
    public synchronized void enable() {
        setEnabled(true);
        apply();
        sendEnableState(true);
    }

    /**
     * プラグインを無効化する.
     */
    public synchronized void disable() {
        sendEnableState(false);
        setEnabled(false);
        apply();
    }

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
     *
     * 最大試行回数は、定数 MAX_CONNECTION_TRY で定める.
     *
     * @return 接続に成功した場合は<code>true</code>、そうでない場合は<code>false</code>
     */
    private boolean tryConnection() {
        for (int cnt = 0; cnt < MAX_CONNECTION_TRY; cnt++) {
            try {
                mConnection.connect();
                mLogger.info("Connected to the plug-in: " + getPackageName());
                return true;
            } catch (ConnectingException e) {
                mLogger.warning("Failed to connect to the plug-in: " + getPackageName());
            }
        }
        return false;
    }

    /**
     * 接続変更通知リスナーを追加する.
     * @param listener リスナー
     */
    public void addConnectionStateListener(final ConnectionStateListener listener) {
        mConnection.addConnectionStateListener(listener);
    }

    /**
     * 接続変更通知リスナーを解除する.
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

    private void sendEnableState(boolean isEnabled) {
        if (mConnection.getState() != ConnectionState.CONNECTED) {
            return;
        }
        Intent notification = createNotificationIntent(isEnabled);
        try {
            send(notification);
        } catch (MessagingException e) {
            mLogger.warning("Failed to send enable-state to " + getComponentName());
        }
    }

    private Intent createNotificationIntent(final boolean isEnabled) {
        String action = isEnabled ? IntentDConnectMessage.ACTION_DEVICEPLUGIN_ENABLED
                                    : IntentDConnectMessage.ACTION_DEVICEPLUGIN_DISABLED;
        Intent notification = new Intent(action);
        notification.setComponent(getComponentName());
        return notification;
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
     * {@link DevicePlugin}オブジェクトを生成するためのビルダー.
     */
    static class Builder {

        /** コンテキスト. */
        private final Context mContext;
        /** デバイスプラグイン情報. */
        private Info mInfo = new Info();

        /**
         * コンストラクタ.
         *
         * @param context コンテキスト
         */
        public Builder(final Context context) {
            mContext = context;
        }

        Builder setPackageName(final String packageName) {
            mInfo.mPackageName = packageName;
            return this;
        }

        Builder setClassName(final String className) {
            mInfo.mClassName = className;
            return this;
        }

        Builder setVersionName(final String versionName) {
            mInfo.mVersionName = versionName;
            return this;
        }

        Builder setPluginSdkVersionName(final VersionName pluginSdkVersionName) {
            mInfo.mPluginSdkVersionName = pluginSdkVersionName;
            return this;
        }

        Builder setPluginId(final String pluginId) {
            mInfo.mPluginId = pluginId;
            return this;
        }

        Builder setDeviceName(final String deviceName) {
            mInfo.mDeviceName = deviceName;
            return this;
        }

        Builder setPluginIconId(final Integer pluginIconId) {
            mInfo.mPluginIconId = pluginIconId;
            return this;
        }

        Builder setPluginXml(final DevicePluginXml xml) {
            mInfo.mPluginXml = xml;
            return this;
        }

        Builder setConnectionType(final ConnectionType connectionType) {
            mInfo.mConnectionType = connectionType;
            return this;
        }

        /**
         * {@link DevicePlugin}オブジェクトを生成する.
         * @return {@link DevicePlugin}オブジェクト
         */
        DevicePlugin build() {
            DevicePluginSetting setting = new DevicePluginSetting(mContext, mInfo.mPluginId);
            DevicePlugin plugin = new DevicePlugin(mInfo, setting);
            return plugin;
        }
    }

    /**
     * プラグインの静的な情報を提供するクラス.
     */
    public static class Info implements Parcelable {

        /** プラグインの設定ファイル. */
        private DevicePluginXml mPluginXml;
        /** プラグインのパッケージ名. */
        private String mPackageName;
        /** マネージャからのメッセージを受信するJavaクラス名. */
        private String mClassName;
        /** デバイスプラグインのバージョン名. */
        private String mVersionName;
        /** プラグインSDKバージョン名. */
        private VersionName mPluginSdkVersionName;
        /** プラグインID. */
        private String mPluginId;
        /** デバイスプラグイン名. */
        private String mDeviceName;
        /** プラグインアイコン. */
        private Integer mPluginIconId;
        /** 接続タイプ. */
        private ConnectionType mConnectionType;

        /**
         * 平均の通信速度.
         */
        private long mAverageBaudRate;

        /**
         * 最遅の通信速度.
         */
        private long mWorstBaudRate;

        /**
         * 最遅通信のリクエスト.
         */
        private String mWorstBaudRateRequest = "None";

        /**
         * 通信履歴.
         */
        private List<BaudRate> mBaudRates = new LinkedList<>();

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

        /**
         * 平均通信速度を取得します.
         * @return 平均通信速度
         */
        public long getAverageBaudRate() {
            return mAverageBaudRate;
        }

        /**
         * 最遅通信速度を取得します.
         * @return 最遅通信速度
         */
        public long getWorstBaudRate() {
            return mWorstBaudRate;
        }

        /**
         * 最遅通信のリクエストを取得します.
         * @return 最遅通信のリクエスト
         */
        public String getWorstBaudRateRequest() {
            return mWorstBaudRateRequest;
        }

        /**
         * 通信履歴を取得します.
         * @return 通信履歴
         */
        public List<BaudRate> getBaudRates() {
            return mBaudRates;
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
            dest.writeLong(this.mAverageBaudRate);
            dest.writeString(this.mWorstBaudRateRequest);
            dest.writeLong(this.mWorstBaudRate);
            dest.writeInt(this.mBaudRates.size());
            for (int i = 0; i < mBaudRates.size(); i++) {
                dest.writeString(mBaudRates.get(i).getRequest());
                dest.writeLong(mBaudRates.get(i).getBaudRate());
                dest.writeLong(mBaudRates.get(i).getDate());
            }
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
            this.mAverageBaudRate = in.readLong();
            this.mWorstBaudRateRequest = in.readString();
            this.mWorstBaudRate = in.readLong();
            int size = in.readInt();
            for (int i = 0; i < size; i++) {
                String request = in.readString();
                long baudRate = in.readLong();
                long date = in.readLong();
                mBaudRates.add(new BaudRate(request, baudRate, date));
            }
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

    /**
     * 通信履歴.
     */
    public static class BaudRate {
        /**
         * 通信するリクエストのパス.
         */
        String mRequest;

        /**
         * 通信時間.
         */
        long mBaudRate;

        /**
         * 通信日付.
         */
        long mDate;

        /**
         * 通信履歴.
         * @param request リクエストのパス
         * @param baudRate 通信時間
         */
        BaudRate(final String request, final long baudRate, final long date) {
            mRequest = request;
            mBaudRate = baudRate;
            mDate = date;
        }

        /**
         * リクエストのパスを取得します.
         * @return リクエストのパス
         */
        public String getRequest() {
            return mRequest;
        }

        /**
         * 通信時間を取得します.
         * @return 通信時間
         */
        public long getBaudRate() {
            return mBaudRate;
        }

        /**
         * 日付を取得します.
         * @return 日付
         */
        public long getDate() {
            return mDate;
        }

        /**
         * 日付の文字列を取得します.
         * @return 日付
         */
        public String getDateString() {
            return DateFormat.format("yyyy/MM/dd kk:mm:ss", mDate).toString();
        }
    }
}
