/*
 DConnectTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;

import org.deviceconnect.android.cipher.signature.AuthSignature;
import org.deviceconnect.android.test.plugin.profile.TestServiceDiscoveryProfileConstants;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.BatteryProfileConstants;
import org.deviceconnect.profile.ConnectProfileConstants;
import org.deviceconnect.profile.DeviceOrientationProfileConstants;
import org.deviceconnect.profile.FileDescriptorProfileConstants;
import org.deviceconnect.profile.FileProfileConstants;
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

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.Uri;
import android.test.InstrumentationTestCase;


/**
 * DeviceConnectのTestCaseのスーパークラス.
 * @author NTT DOCOMO, INC.
 */
public abstract class DConnectTestCase extends InstrumentationTestCase {

    /** DeviceConnectManagerへのURI. */
    protected static final String DCONNECT_MANAGER_URI = "http://localhost:4035/gotapi";

    /** DeviceConnectManagerのアプリケーション名. */
    protected static final String DCONNECT_MANAGER_APP_NAME = "Device Connect Manager";

    /** DeviceConnectManagerのバージョン名. */
    protected static final String DCONNECT_MANAGER_VERSION_NAME = "1.0";

    /** 起動用インテントを受信するクラスのコンポーネント名. */
    private static final String LAUNCH_RECEIVER
        = "org.deviceconnect.android.manager/.setting.SettingActivity";

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

    /** テスト用プラグインID. */
    public static final String TEST_PLUGIN_ID = "test_plugin_id";

    /** テスト用セッションID. */
    public static final String TEST_SESSION_KEY = "test_session_key";

    /** OAUTH認証情報を保存するファイル名. */
    private static final String FILE_NAME_OAUTH = "oauth.db";

    /** クライアントIDのキー. */
    private static final String KEY_CLIENT_ID = "clientId";

    /** クライアントシークレットのキー. */
    private static final String KEY_CLIENT_SECRET = "clientSecret";

    /** アクセストークンのキー. */
    private static final String KEY_ACCESS_TOKEN = "accessToken";

//    /** dConnectManagerの起動を待つ時間を定義. */
//    private static final int TIME_WAIT_FOR_DCONNECT = 1000;

    /** バッファサイズ. */
    private static final int BUF_SIZE = 8192;

    /** デバイス一覧. */
    private List<DeviceInfo> mDevices;

    /** プラグイン一覧. */
    private List<PluginInfo> mPlugins;

    /** ロガー. */
    protected final Logger mLogger = Logger.getLogger("dconnect.manager");

    /** クライアントID. */
    protected String mClientId;

    /** クライアントシークレット. */
    protected String mClientSecret;

    /** アクセストークン. */
    protected String mAccessToken;

    /**
     * コンストラクタ.
     * @param tag テストタグ
     */
    public DConnectTestCase(final String tag) {
        setName(tag);
    }

    /**
     * ApplicationContext を取得します.
     * @return コンテキスト
     */
    protected Context getApplicationContext() {
        return getInstrumentation()
                .getTargetContext().getApplicationContext();
    }

    /**
     * JUnitプロジェクトのコンテキストを取得する.
     * @return コンテキスト
     */
    protected Context getContext() {
        return getInstrumentation().getContext();
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
     * レスポンスとしてクライアントIDまたはクライアントシークレットのいずれかを受信できなかった場合はnullを返すこと.
     * </p>
     * 
     * @return クライアントIDおよびクライアントシークレットを格納した配列
     */
    protected abstract String[] createClient();

    /**
     * dConnectManagerに対してアクセストークン取得リクエストを送信する.
     * <p>
     * レスポンスとしてアクセストークンを受信できなかった場合はnullを返すこと.
     * </p>
     * 
     * @param clientId クライアントID
     * @param clientSecret クライアントシークレット
     * @param scopes スコープ指定
     * @return アクセストークン
     */
    protected abstract String requestAccessToken(String clientId, String clientSecret,
            String[] scopes);

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
        final CountDownLatch lockObj = new CountDownLatch(1);
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                lockObj.countDown();
            }
        };
        getContext().registerReceiver(receiver, new IntentFilter(TEST_ACTION_MANAGER_LAUNCHED));

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setComponent(ComponentName.unflattenFromString(LAUNCH_RECEIVER));
        intent.putExtra(IntentDConnectMessage.EXTRA_ORIGIN, getOrigin());
        intent.putExtra(IntentDConnectMessage.EXTRA_KEY, getHMACString());
        intent.putExtra(IntentDConnectMessage.EXTRA_RECEIVER,
                new ComponentName(getContext(), TestCaseBroadcastReceiver.class));
        intent.setData(Uri.parse("dconnect://start"));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(intent);

        // 起動通知を受信するまでプロック
        if (!lockObj.await(20, TimeUnit.SECONDS)) {
            fail("Manager launching timeout.");
        }
        getContext().unregisterReceiver(receiver);
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
        return mClientId;
    }

    /**
     * クライアントシークレットのオンメモリ上のキャッシュを取得する.
     * 
     * @return クライアントシークレットのオンメモリ上のキャッシュ
     */
    protected String getClientSecret() {
        return mClientSecret;
    }

    /**
     * アクセストークンのオンメモリ上のキャッシュを取得する.
     * 
     * @return アクセストークンのオンメモリ上のキャッシュ
     */
    protected String getAccessToken() {
        return mAccessToken;
    }

    /**
     * クライアントIDのキャッシュを取得する.
     * 
     * @return クライアントIDのキャッシュ
     */
    protected String getClientIdCache() {
        SharedPreferences pref = getContext().getSharedPreferences(FILE_NAME_OAUTH, Context.MODE_PRIVATE);
        return pref.getString(KEY_CLIENT_ID, null);
    }

    /**
     * クライアントシークレットのキャッシュを取得する.
     * 
     * @return クライアントシークレットのキャッシュ
     */
    protected String getClientSecretCache() {
        SharedPreferences pref = getContext().getSharedPreferences(FILE_NAME_OAUTH, Context.MODE_PRIVATE);
        return pref.getString(KEY_CLIENT_SECRET, null);
    }

    /**
     * アクセストークンのキャッシュを取得する.
     * 
     * @return アクセストークンのキャッシュ
     */
    protected String getAccessTokenCache() {
        SharedPreferences pref = getContext().getSharedPreferences(FILE_NAME_OAUTH, Context.MODE_PRIVATE);
        return pref.getString(KEY_ACCESS_TOKEN, null);
    }

    /**
     * 認証情報をキャッシュする.
     * 
     * @param clientId クライアントID
     * @param clientSecret クライアントシークレット
     * @param accessToken アクセストークン
     */
    protected void storeOAuthInfo(final String clientId, final String clientSecret, final String accessToken) {
        SharedPreferences pref = getContext().getSharedPreferences(FILE_NAME_OAUTH, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(KEY_CLIENT_ID, clientId);
        editor.putString(KEY_CLIENT_SECRET, clientSecret);
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        boolean edited = editor.commit();
        if (!edited) {
            fail("Failed to store oauth info: clientId, cliendSecret, accessToken");
        }
    }

    /**
     * テストの前に実行される.
     * @exception Exception 設定に失敗した場合に発生
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        waitForManager();
        if (isLocalOAuth()) {
            mClientId = getClientIdCache();
            mClientSecret = getClientSecretCache();
            mAccessToken = getAccessTokenCache();
            // クライアントID、クライアントシークレット取得
            if (mClientId == null || mClientSecret == null) {
                String[] client = createClient();
                assertNotNull(client);
                assertNotNull(client[0]);
                assertNotNull(client[1]);
                mClientId = client[0];
                mClientSecret = client[1];
            }
            // アクセストークン取得
            if (mAccessToken == null) {
                mAccessToken = requestAccessToken(mClientId, mClientSecret, PROFILES);
                assertNotNull(mAccessToken);
            }
            // 認証情報をキャッシュする
            storeOAuthInfo(mClientId, mClientSecret, mAccessToken);
        }
        if (isSearchDevices()) {
            // テストデバイスプラグインを探す
            setDevices(searchDevices());
            setPlugins(searchPlugins());
        }
    }

    /**
     * accessTokenをリクエストするためのシグネイチャを作成する.
     * @param clientId クライアントID
     * @param scopes スコープ
     * @param clientSecret クライアントシークレット
     * @return シグネイチャ
     */
    protected String createSignature(final String clientId, final String[] scopes, final String clientSecret) {
        String signature = null;
        try {
            signature = AuthSignature.generateSignature(clientId,
                    AuthorizationProfileConstants.GrantType.AUTHORIZATION_CODE.getValue(), 
                    null, scopes, clientSecret);
            signature = URLEncoder.encode(signature, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
        return signature;
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
     * @param b バイト配列
     * @return 16進文字列
     */
    protected static String toHexString(final byte[] b) {
        if (b == null) {
            throw new IllegalArgumentException();
        }
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            String substr = Integer.toHexString(b[i] & 0xff);
            if (substr.length() < 2) {
                str.append("0");
            }
            str.append(substr);
        }
        return str.toString();
    }

    /**
     * バイト配列の16進文字列を解析する.
     * @param b バイト配列の16進文字列
     * @return 解析したバイト配列
     */
    protected static byte[] toByteArray(final String b) {
        String c = b;
        if (c.length() % 2 != 0) {
            c = "0" + c;
        }
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
