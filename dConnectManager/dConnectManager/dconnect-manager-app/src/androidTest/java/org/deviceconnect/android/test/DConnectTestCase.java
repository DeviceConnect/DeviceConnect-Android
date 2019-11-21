/*
 DConnectTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.test;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;

import org.deviceconnect.android.profile.AuthorizationProfile;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectResponseMessage;
import org.deviceconnect.message.DConnectSDK;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;
import org.deviceconnect.profile.ServiceInformationProfileConstants;
import org.deviceconnect.profile.SystemProfileConstants;
import org.junit.After;
import org.junit.Before;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertNotNull;

/**
 * DeviceConnectのTestCaseのスーパークラス.
 * @author NTT DOCOMO, INC.
 */
public abstract class DConnectTestCase {

    /**
     * DeviceConnectManagerへのURI.
     */
    protected static final String MANAGER_URI = "http://localhost:4035/gotapi";

    /**
     * テスト用デバイス名.
     */
    private static final String DEVICE_NAME = "Test Success Device";

    /**
     * DeviceConnectManagerへアクセスするインターフェース.
     */
    protected DConnectSDK mDConnectSDK;

    /**
     * プロファイル一覧.
     * <p>
     * OAuth処理でのスコープの指定に使用する.
     * </p>
     */
    private static final String[] PROFILES = {
            ServiceDiscoveryProfileConstants.PROFILE_NAME,
            ServiceInformationProfileConstants.PROFILE_NAME,
            SystemProfileConstants.PROFILE_NAME,
            "deviceOrientation",
            "files",
            "unique",
            "jsonTest",
            "dataTest",
            "allGetControl",
            "abc" // 実際には実装しないプロファイル
    };

    /** デバイス一覧. */
    private List<ServiceInfo> mServiceInfoList;

    /** アクセストークン. */
    private static String sAccessToken;

    private Context mContext;

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    @Before
    public void setUp() throws Exception {
        setContext(InstrumentationRegistry.getInstrumentation().getTargetContext());
        mDConnectSDK.setOrigin(getOrigin());
        waitForManager();
        if (isLocalOAuth()) {
            // アクセストークン取得
            if (sAccessToken == null) {
                sAccessToken = requestAccessToken(PROFILES);
                assertNotNull(sAccessToken);
            }
            if (mDConnectSDK != null) {
                mDConnectSDK.setAccessToken(sAccessToken);
            }
            waitForFoundTestService();
        }
    }

    @After
    public void tearDown() throws Exception {}

    /**
     * Manager が起動するまでブロックします.
     *
     * @throws InterruptedException スレッドが割り込まれた場合
     */
    private void waitForManager() throws InterruptedException {
        mDConnectSDK.startManager(getContext());

        long timeout = 30 * 1000;
        final long interval = 1000;
        while (!isManagerAvailable()) {
            Thread.sleep(interval);
            timeout -= interval;
            if (timeout <= 0) {
                fail("Manager launching timeout.");
            }
        }
    }

    /**
     * テスト用のサービスを発見するまでブロックします.
     * <p>
     *     30秒検索して見つからない場合にはインストールされていないと判断します。
     * </p>
     * @throws InterruptedException スレッドが割り込まれた場合
     */
    private void waitForFoundTestService() throws InterruptedException {
        long timeout = 30 * 1000;
        final long interval = 1000;
        while (true) {
            // サービスの検索
            mServiceInfoList = searchServices();

            String serviceId = getServiceIdByName(DEVICE_NAME);
            if (serviceId != null) {
                // テストプラグインを発見
                return;
            }

            Thread.sleep(interval);
            timeout -= interval;
            if (timeout <= 0) {
                fail("The test plugin is not installed.");
            }
        }
    }

    /**
     * Device Connect Managerに対してアクセストークン取得リクエストを送信する.
     * <p>
     * レスポンスとしてアクセストークンを受信できなかった場合はnullを返す。
     * </p>
     * @param scopes スコープ指定
     * @return アクセストークン
     */
    private String requestAccessToken(final String[] scopes) {
        DConnectResponseMessage response = mDConnectSDK.authorization("JUnitTest", scopes);
        if (response.getResult() == DConnectMessage.RESULT_OK) {
            return response.getString(AuthorizationProfile.PARAM_ACCESS_TOKEN);
        } else {
            return null;
        }
    }

    /**
     * デバイス一覧をRestfulAPIで取得する.
     * @return デバイス一覧
     */
    private List<ServiceInfo> searchServices() {
        List<ServiceInfo> services = new ArrayList<>();
        DConnectResponseMessage response = mDConnectSDK.serviceDiscovery();
        if (response.getResult() == DConnectMessage.RESULT_ERROR) {
            return null;
        }

        List<Object> list = response.getList(ServiceDiscoveryProfile.PARAM_SERVICES);
        for (Object value : list) {
            DConnectMessage service = (DConnectMessage) value;
            String serviceId = service.getString(ServiceDiscoveryProfileConstants.PARAM_ID);
            String deviceName = service.getString(ServiceDiscoveryProfileConstants.PARAM_NAME);
            services.add(new ServiceInfo(serviceId, deviceName));
        }
        return services;
    }

    /**
     * Device Connect Managerが起動しているかどうかを確認する.
     * @return Device Connect Managerが起動している場合はtrue、そうでない場合はfalse
     */
    private boolean isManagerAvailable() {
        DConnectResponseMessage response = mDConnectSDK.availability();
        return response.getResult() == DConnectMessage.RESULT_OK;
    }

    /**
     * Originを取得する.
     *
     * @return Origin
     */
    protected String getOrigin() {
        return InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageName();
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
     * スコープを連結する.
     * @param scopes 連結するスコープ
     * @return 連結した文字列
     */
    protected String combineStr(final String[] scopes) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < scopes.length; i++) {
            if (i > 0) {
                builder.append(",");
            }
            builder.append(scopes[i].trim());
        }
        return builder.toString();
    }

    /**
     * 各テストメソッド実行前に、LocalOAuth認証を行うかどうかの設定を取得する.
     * @return テスト実行前にLocalOAuth認証を行う場合はtrue、そうでない場合はfalse
     */
    protected boolean isLocalOAuth() {
        return true;
    }

    /**
     * サービスIDを取得する.
     * <p>
     * サービスが見つからない場合は fail を発生させます。
     * </p>
     * @return サービスID
     */
    protected String getServiceId() {
        if (mServiceInfoList == null) {
            mServiceInfoList = searchServices();
            if (mServiceInfoList == null) {
                fail("Not found the test plugin.");
            }
        }
        String serviceId = getServiceIdByName(DEVICE_NAME);
        if (serviceId == null) {
            fail("Not found the test plugin.");
        }
        return serviceId;
    }

    /**
     * 指定したデバイス名をもつデバイスのIDを取得する.
     * <p>
     *     取得できない場合は null を返却します。
     * </p>
     * @param deviceName デバイス名
     * @return サービスID
     */
    private String getServiceIdByName(final String deviceName) {
        if (mServiceInfoList == null) {
            return null;
        }

        for (int i = 0; i < mServiceInfoList.size(); i++) {
            ServiceInfo obj = mServiceInfoList.get(i);
            if (deviceName.equals(obj.getDeviceName())) {
                return obj.getServiceId();
            }
        }

        return null;
    }

    /**
     * サービス情報.
     */
    private static class ServiceInfo {

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
        ServiceInfo(final String serviceId, final String deviceName) {
            mServiceId = serviceId;
            mDeviceName = deviceName;
        }

        /**
         * サービスIDを取得する.
         * 
         * @return サービスID
         */
        private String getServiceId() {
            return mServiceId;
        }

        /**
         * デバイス名を取得する.
         * 
         * @return デバイス名
         */
        private String getDeviceName() {
            return mDeviceName;
        }
    }
}
