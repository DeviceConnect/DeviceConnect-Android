/*
 DConnectTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.test;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.test.AndroidTestCase;

import org.deviceconnect.android.observer.util.AndroidSocket;
import org.deviceconnect.android.observer.util.SockStatUtil;
import org.deviceconnect.android.observer.util.SocketState;
import org.deviceconnect.android.test.plugin.profile.TestServiceDiscoveryProfileConstants;
import org.deviceconnect.android.test.plugin.profile.TestSystemProfileConstants;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.deviceconnect.profile.BatteryProfileConstants;
import org.deviceconnect.profile.ConnectProfileConstants;
import org.deviceconnect.profile.DeviceOrientationProfileConstants;
import org.deviceconnect.profile.FileDescriptorProfileConstants;
import org.deviceconnect.profile.FileProfileConstants;
import org.deviceconnect.profile.LightProfileConstants;
import org.deviceconnect.profile.MediaPlayerProfileConstants;
import org.deviceconnect.profile.MediaStreamRecordingProfileConstants;
import org.deviceconnect.profile.NotificationProfileConstants;
import org.deviceconnect.profile.PhoneProfileConstants;
import org.deviceconnect.profile.ProximityProfileConstants;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;
import org.deviceconnect.profile.ServiceInformationProfileConstants;
import org.deviceconnect.profile.SettingsProfileConstants;
import org.deviceconnect.profile.SystemProfileConstants;
import org.deviceconnect.profile.VibrationProfileConstants;
import org.junit.Before;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;

/**
 * DeviceConnectのTestCaseのスーパークラス.
 * @author NTT DOCOMO, INC.
 */
public abstract class DConnectTestCase extends AndroidTestCase {

    /** DeviceConnectManagerへのURI. */
    protected static final String DCONNECT_MANAGER_URI = "http://localhost:4035/gotapi";

    /** DeviceConnectManagerのアプリケーション名. */
    protected static final String DCONNECT_MANAGER_APP_NAME = "Device Connect Manager";

    /** DeviceConnectManagerのバージョン名. */
    protected static final String DCONNECT_MANAGER_VERSION_NAME = TestSystemProfileConstants.VERSION;

    /** HMACアルゴリズム. */
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    /** 乱数生成用オブジェクト. */
    private static final Random RANDOM;

    /** HMACの生成キー. */
    private static final SecretKey HMAC_KEY;

    static {
        RANDOM = new Random();
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(HMAC_ALGORITHM);
            HMAC_KEY = keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("The JDK does not support " + HMAC_ALGORITHM
                    + ". Try these testcases on other JDK.");
        }
    }

    /**
     * プロファイル一覧.
     * <p>
     * OAuth処理でのスコープの指定に使用する.
     * </p>
     */
    protected static final String[] PROFILES = {
            BatteryProfileConstants.PROFILE_NAME,
            ConnectProfileConstants.PROFILE_NAME,
            DeviceOrientationProfileConstants.PROFILE_NAME,
            FileDescriptorProfileConstants.PROFILE_NAME,
            FileProfileConstants.PROFILE_NAME,
            LightProfileConstants.PROFILE_NAME,
            MediaStreamRecordingProfileConstants.PROFILE_NAME,
            MediaPlayerProfileConstants.PROFILE_NAME,
            NotificationProfileConstants.PROFILE_NAME,
            PhoneProfileConstants.PROFILE_NAME,
            ProximityProfileConstants.PROFILE_NAME,
            ServiceDiscoveryProfileConstants.PROFILE_NAME,
            ServiceInformationProfileConstants.PROFILE_NAME,
            SettingsProfileConstants.PROFILE_NAME,
            SystemProfileConstants.PROFILE_NAME,
            VibrationProfileConstants.PROFILE_NAME,
            "files",
            "unique",
            "json_test",
            "abc" // 実際には実装しないプロファイル
    };

    /** テスト用Action: RESPONSE. */
    public static final String TEST_ACTION_RESPONSE
            = "org.deviceconnect.android.test.intent.action.RESPONSE";

    /** テスト用Action: EVENT. */
    public static final String TEST_ACTION_EVENT
            = "org.deviceconnect.android.test.intent.action.EVENT";

    /** テスト用Action: MANAGER_LAUNCHED. */
    public static final String TEST_ACTION_MANAGER_LAUNCHED
            = "org.deviceconnect.android.test.intent.action.MANAGER_LAUNCHED";

    /** テスト用セッションID. */
    public static final String TEST_SESSION_KEY = "test_session_key";

    /** バッファサイズ. */
    private static final int BUF_SIZE = 8192;

    /** デバイス一覧. */
    private List<DeviceInfo> mDevices;

    /** プラグイン一覧. */
    private List<PluginInfo> mPlugins;

    /** ロガー. */
    protected final Logger mLogger = Logger.getLogger("dconnect.manager");

    /** クライアントID. */
    protected static String sClientId;

    /** アクセストークン. */
    protected static String sAccessToken;

    /**
     * ApplicationContext を取得します.
     * @return コンテキスト
     */
    protected Context getApplicationContext() {
        return InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    /**
     * バイト配列のアサート文.
     * @param expected 期待するバイト配列
     * @param actual 実際のバイト倍列
     */
    protected static void assertEquals(final byte[] expected, final byte[] actual) {
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], actual[i]);
        }
    }

    /**
     * dConnectManagerに対してクライアント作成リクエストを送信する.
     * <p>
     * レスポンスとしてクライアントIDを受信できなかった場合はnullを返すこと.
     * </p>
     * 
     * @return クライアントID
     */
    protected abstract String createClient();

    /**
     * dConnectManagerに対してアクセストークン取得リクエストを送信する.
     * <p>
     * レスポンスとしてアクセストークンを受信できなかった場合はnullを返すこと.
     * </p>
     * 
     * @param clientId クライアントID
     * @param scopes スコープ指定
     * @return アクセストークン
     */
    protected abstract String requestAccessToken(String clientId, String[] scopes);

    /**
     * dConnectManagerから最新のデバイス一覧を取得する.
     * @return dConnectManagerから取得した最新のデバイス一覧
     */
    protected abstract List<DeviceInfo> searchDevices();

    /**
     * dConnectManagerから最新のプラグイン一覧を取得する.
     * @return dConnectManagerから取得した最新のプラグイン一覧
     */
    protected abstract List<PluginInfo> searchPlugins();

    /**
     * Device Connect Managerが起動しているかどうかを確認する.
     * @return Device Connect Managerが起動している場合はtrue、そうでない場合はfalse
     */
    protected abstract boolean isManagerAvailable();

    /**
     * Managerが起動するまでブロックする.
     * @throws InterruptedException スレッドが割り込まれた場合
     */
    protected void waitForManager() throws InterruptedException {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClassName("org.deviceconnect.android.manager",
                "org.deviceconnect.android.manager.DConnectLaunchActivity");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.putExtra(IntentDConnectMessage.EXTRA_ORIGIN, getOrigin());
        intent.putExtra(IntentDConnectMessage.EXTRA_KEY, getHMACString());
        intent.setData(Uri.parse("dconnect://start"));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        getContext().startActivity(intent);

        if (!isDConnectServiceRunning()) {
            long timeout = 30 * 1000;
            final long interval = 400;
            while (!isDConnectServiceRunning()) {
                timeout -= interval;
                if (timeout <= 0) {
                    fail("Manager launching timeout.");
                }
                Thread.sleep(interval);
            }
            Thread.sleep(1000);
        }
    }

    /**
     * DConnectServiceが動作しているか確認する.
     * @return 起動中の場合はtrue、それ以外はfalse
     */
    private boolean isDConnectServiceRunning() {
        return isServiceRunning(getContext(), "org.deviceconnect.android.manager.DConnectService") && isDConnectServerRunning();
    }

    /**
     * RESTfulサーバが動作しているか確認する.
     * @return 動作している場合はtrue、それ以外はfalse
     */
    private boolean isDConnectServerRunning() {
        ArrayList<AndroidSocket> sockets = SockStatUtil.getSocketList(getApplicationContext());
        String packageName = getApplicationContext().getPackageName();
        for (AndroidSocket socket : sockets) {
            if (socket.getAppName().equals(packageName)
                    && socket.getLocalPort() == 4035
                    && socket.getState() == SocketState.TCP_LISTEN) {
                return true;
            }
        }
        return false;
    }

    /**
     * サービスに起動確認を行う.
     * @param c コンテキスト
     * @param className クラス名
     * @return 起動中の場合はtrue、それ以外はfalse
     */
    private boolean isServiceRunning(final Context c, final String className) {
        ActivityManager am = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningServiceInfo> runningService = am.getRunningServices(Integer.MAX_VALUE);
        for (RunningServiceInfo i : runningService) {
            if (className.equals(i.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 指定されたNONCEからHMACを生成する.
     * 
     * @param nonce リクエスト時に送信したNONCE
     * @return HMACのバイト配列
     * @throws NoSuchAlgorithmException 使用するアルゴリズムがサポートされていない場合
     * @throws InvalidKeyException キーが不正な場合
     */
    protected byte[] calculateHMAC(final byte[] nonce) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(HMAC_KEY);
        return mac.doFinal(nonce);
    }

    /**
     * Originを取得する.
     * 
     * @return Origin
     */
    protected String getOrigin() {
        return getContext().getPackageName();
    }

    /**
     * クライアントIDのオンメモリ上のキャッシュを取得する.
     * 
     * @return クライアントIDのオンメモリ上のキャッシュ
     */
    protected String getClientId() {
        return sClientId;
    }

    /**
     * アクセストークンのオンメモリ上のキャッシュを取得する.
     * 
     * @return アクセストークンのオンメモリ上のキャッシュ
     */
    protected String getAccessToken() {
        return sAccessToken;
    }

    /**
     * テストの前に実行される.
     * @exception Exception 設定に失敗した場合に発生
     */
    @Before
    public void setUp() throws Exception {
        setContext(InstrumentationRegistry.getContext());
        waitForManager();
        if (isLocalOAuth()) {
            // クライアントID取得
            if (sClientId == null) {
                String clientId = createClient();
                assertNotNull(clientId);
                sClientId = clientId;
            }
            // アクセストークン取得
            if (sAccessToken == null) {
                sAccessToken = requestAccessToken(sClientId, PROFILES);
                assertNotNull(sAccessToken);
            }
            Thread.sleep(2000);
        }
        if (isSearchDevices()) {
            // テストデバイスプラグインを探す
            setDevices(searchDevices());
            setPlugins(searchPlugins());
        }
    }

    /**
     * 各テストメソッド実行前に、LocalOAuth認証を行うかどうかの設定を取得する.
     * @return テスト実行前にLocalOAuth認証を行う場合はtrue、そうでない場合はfalse
     */
    protected boolean isLocalOAuth() {
        return true;
    }

    /**
     * 各テストメソッド実行前に、デバイス一覧取得を行うかどうかの設定を取得する.
     * @return テスト実行前にデバイス一覧取得を行う場合はtrue、そうでない場合はfalse
     */
    protected boolean isSearchDevices() {
        return true;
    }

    /**
     * サービスIDを取得する.
     * @return サービスID
     */
    protected String getServiceId() {
        return getServiceIdByName(TestServiceDiscoveryProfileConstants.DEVICE_NAME);
    }

    /**
     * 指定したデバイス名をもつデバイスのIDを取得する.
     * @param deviceName デバイス名
     * @return サービスID
     */
    protected String getServiceIdByName(final String deviceName) {
        for (int i = 0; i < mDevices.size(); i++) {
            DeviceInfo obj = mDevices.get(i);
            if (deviceName.equals(obj.getDeviceName())) {
                return obj.getServiceId();
            }
        }
        return null;
    }

    /**
     * 指定したプラグイン名をもつプラグインIDを取得する.
     * @param pluginName プラグイン名
     * @return プラグインID
     */
    private String getPluginIdByName(final String pluginName) {
        for (int i = 0; i < mPlugins.size(); i++) {
            PluginInfo obj = mPlugins.get(i);
            if (pluginName.equals(obj.getName())) {
                return obj.getId();
            }
        }
        return null;
    }

    /**
     * テスト用プラグインIDを取得する.
     * @return テスト用プラグインID
     */
    protected String getTestPluginId() {
        return getPluginIdByName("Device Connect Device Plugin for Test");
    }

    /**
     * デバイス一覧をキャッシュする.
     * @param services デバイス一覧
     */
    protected void setDevices(final List<DeviceInfo> services) {
        this.mDevices = services;
    }

    /**
     * プラグイン一覧をキャッシュする.
     * @param plugins プラグイン一覧
     */
    protected void setPlugins(final List<PluginInfo> plugins) {
        this.mPlugins = plugins;
    }

    /**
     * assetsにあるファイルデータを取得する.
     * @param name ファイル名
     * @return ファイルデータ
     */
    protected byte[] getBytesFromAssets(final String name) {
        AssetManager mgr = getApplicationContext().getResources().getAssets();
        InputStream in = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[BUF_SIZE];
        int len;
        try {
            in = mgr.open(name);
            while ((len = in.read(buf)) > 0) {
                baos.write(buf, 0, len);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    mLogger.warning("Exception occured in close method.");
                }
            }
        }
    }

    /**
     * 指定したサイズ(単位はバイト)の乱数を生成し、その16進文字列を返す. 
     * @param size バイト数
     * @return 乱数の16進文字列
     */
    protected byte[] generateRandom(final int size) {
        byte[] key = new byte[size];
        RANDOM.nextBytes(key);
        return key;
    }

    /**
     * 指定したバイト配列の16進文字列を返す.
     * @param data バイト配列
     * @return 16進文字列
     */
    protected static String toHexString(final byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException();
        }
        StringBuilder str = new StringBuilder();
        for (byte b : data) {
            str.append(String.format("%02x", b));
        }
        return str.toString();
    }

    /**
     * バイト配列の16進文字列を解析する.
     * @param b バイト配列の16進文字列
     * @return 解析したバイト配列
     */
    protected static byte[] toByteArray(final String b) {
        byte[] array = new byte[b.length() / 2];
        for (int i = 0; i < b.length() / 2; i++) {
            String hex = b.substring(2 * i, 2 * i + 2);
            array[i] = (byte) Integer.parseInt(hex, 16);
        }
        return array;
    }

    /**
     * HMAC生成キーの16進文字列表現を返す. 
     * @return HMAC生成キーの16進文字列表現
     */
    protected String getHMACString() {
        return toHexString(HMAC_KEY.getEncoded());
    }

    /**
     * デバイス情報.
     */
    protected static class DeviceInfo {

        /**
         * サービスID.
         */
        private final String mServiceId;

        /**
         * デバイス名.
         */
        private final String mDeviceName;

        /**
         * コンストラクタ.
         * @param serviceId サービスID
         * @param deviceName デバイス名
         */
        public DeviceInfo(final String serviceId, final String deviceName) {
            this.mServiceId = serviceId;
            this.mDeviceName = deviceName;
        }

        /**
         * サービスIDを取得する.
         * 
         * @return サービスID
         */
        public String getServiceId() {
            return mServiceId;
        }

        /**
         * デバイス名を取得する.
         * 
         * @return デバイス名
         */
        public String getDeviceName() {
            return mDeviceName;
        }
    }

    /**
     * プラグイン情報.
     */
    protected static class PluginInfo {

        /**
         * プラグインID.
         */
        private final String mId;

        /**
         * プラグイン名.
         */
        private final String mName;

        /**
         * コンストラクタ.
         * @param id プラグインID
         * @param name プラグイン名
         */
        public PluginInfo(final String id, final String name) {
            this.mId = id;
            this.mName = name;
        }

        /**
         * プラグインIDを取得する.
         * 
         * @return プラグインID
         */
        public String getId() {
            return mId;
        }

        /**
         * プラグイン名を取得する.
         * 
         * @return プラグイン名
         */
        public String getName() {
            return mName;
        }
    }
}
