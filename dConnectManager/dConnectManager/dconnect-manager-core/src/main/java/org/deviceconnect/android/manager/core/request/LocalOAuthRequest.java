/*
 LocalOAuthRequest.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.core.request;

import android.content.Intent;

import org.deviceconnect.android.manager.core.DConnectConst;
import org.deviceconnect.android.manager.core.DConnectLocalOAuth;
import org.deviceconnect.android.manager.core.DConnectLocalOAuth.OAuthData;
import org.deviceconnect.android.manager.core.R;
import org.deviceconnect.android.manager.core.plugin.DevicePlugin;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.deviceconnect.profile.AuthorizationProfileConstants;
import org.deviceconnect.profile.DConnectProfileConstants;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

/**
 * LocalOAuthを行うためのリクエスト.
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class LocalOAuthRequest extends DConnectPluginRequest {
    /**
     * プラグイン側のAuthorizationのアトリビュート名: {@value}.
     */
    private static final String ATTRIBUTE_CREATE_CLIENT = "createClient";

    /**
     * プラグイン側のAuthorizationのアトリビュート名: {@value}.
     */
    private static final String ATTRIBUTE_REQUEST_ACCESS_TOKEN = "requestAccessToken";

    /**
     * リトライ回数の最大値を定義.
     */
    protected static final int MAX_RETRY_COUNT = 3;

    /**
     * Local OAuthを使用するクラス.
     */
    protected DConnectLocalOAuth mLocalOAuth;

    /**
     * アクセストークンの使用フラグ.
     */
    protected boolean mUseAccessToken;

    /**
     * オリジン有効フラグ.
     */
    protected boolean mRequireOrigin;

    /**
     * リトライ回数.
     */
    protected int mRetryCount;

    /**
     * Local OAuth管理クラスを設定する.
     *
     * @param auth Local OAuth管理クラス
     */
    public void setLocalOAuth(final DConnectLocalOAuth auth) {
        mLocalOAuth = auth;
    }

    /**
     * アクセストークンの使用フラグを設定する.
     *
     * @param useAccessToken 使用する場合はtrue、それ以外はfalse
     */
    public void setUseAccessToken(final boolean useAccessToken) {
        mUseAccessToken = useAccessToken;
    }

    /**
     * オリジン有効フラグを設定する.
     *
     * @param requireOrigin 有効にする場合はtrue、それ以外はfalse
     */
    public void setRequireOrigin(final boolean requireOrigin) {
        mRequireOrigin = requireOrigin;
    }

    /**
     * アクセストークンの使用フラグを取得する.
     *
     * @return アクセストークンを使用する場合はtrue、それ以外はfalse
     */
    public boolean isUseAccessToken() {
        return mUseAccessToken;
    }

    @Override
    public void run() {
        if (getRequest() == null) {
            throw new RuntimeException("mRequest is null.");
        }

        if (mDevicePlugin == null) {
            throw new RuntimeException("mDevicePlugin is null.");
        }

        // リトライ回数を初期化
        mRetryCount = 0;

        // リクエストコードを作成
        mRequestCode = UUID.randomUUID().hashCode();

        // リクエストを実行
        executeRequest();
    }

    /**
     * 実際の命令を行う.
     *
     * @param accessToken アクセストークン
     */
    protected abstract void executeRequest(final String accessToken);

    /**
     * プラグイン側のアクセストークンを更新したときに呼び出されるコールバック.
     *
     * @param plugin         プラグイン
     * @param newAccessToken 新しいアクセストークン
     */
    protected void onAccessTokenUpdated(final DevicePlugin plugin, final String newAccessToken) {
    }

    /**
     * Local OAuthの有効期限切れの場合にリトライを行う.
     */
    void executeRequest() {
        String profile = mRequest.getStringExtra(DConnectMessage.EXTRA_PROFILE);
        String serviceId = mRequest.getStringExtra(DConnectMessage.EXTRA_SERVICE_ID);
        String origin = getRequestOrigin(mRequest);

        if (mUseAccessToken && !isIgnoredPluginProfile(profile)) {
            String accessToken = getAccessTokenForPlugin(origin, serviceId);
            if (accessToken != null) {
                executeRequest(accessToken);
            } else {
                OAuthData oauth = mLocalOAuth.getOAuthData(origin, serviceId);
                if (oauth == null) {
                    // OAuthData が存在しない場合には、プラグインに生成要求を行う
                    ClientData clientData = executeClient(serviceId, origin);
                    if (clientData == null) {
                        sendResponse(mResponse);
                        return;
                    } else if (clientData.mClientId == null) {
                        // プラグイン側で、アクセストークン不要のレスポンスが返ってきた場合の処理
                        executeRequest(null);
                        return;
                    } else {
                        // クライアントデータを保存
                        mLocalOAuth.setOAuthData(origin, serviceId, clientData.mClientId);
                        oauth = mLocalOAuth.getOAuthData(origin, serviceId);
                    }
                }

                accessToken = mLocalOAuth.getAccessToken(oauth.getId());
                if (accessToken == null) {
                    // 再度アクセストークンを取得してから再度実行
                    accessToken = executeGetAccessToken(serviceId, origin, oauth.getClientId());
                    if (accessToken == null) {
                        sendResponse(mResponse);
                    } else {
                        // アクセストークンを保存
                        mLocalOAuth.setAccessToken(oauth.getId(), accessToken);
                        onAccessTokenUpdated(mDevicePlugin, accessToken);
                        executeRequest(accessToken);
                    }
                } else {
                    executeRequest(accessToken);
                }
            }
        } else {
            executeRequest(null);
        }
    }

    /**
     * ClientData を作成するためのリクエストを実行します.
     *
     * @param serviceId サービスID
     * @param origin オリジン
     * @return ClientDataのインスタンス、エラーの場合は null
     */
    private ClientData executeClient(final String serviceId, final String origin) {
        CountDownLatch latch = new CountDownLatch(1);

        CreateClientRequest request = new CreateClientRequest();
        request.setDestination(mDevicePlugin);
        request.setDevicePluginManager(mPluginMgr);
        request.setRequest(mRequest);
        request.setContext(getContext());
        request.setServiceId(serviceId);
        request.setOrigin(origin);
        request.setLocalOAuth(mLocalOAuth);
        request.setDConnectInterface(mInterface);
        request.setReportedRoundTrip(false);
        request.setOnResponseCallback((response) -> {
            mResponse = response;
            latch.countDown();
        });
        mRequestManager.addRequest(request);

        try {
            latch.await();
        } catch (InterruptedException e) {
            // ignore.
        }

        return request.getClientData();
    }

    /**
     * アクセストークンを作成するためのリクエストを実行します.
     *
     * @param serviceId サービスID
     * @param origin オリジン
     * @param clientId クライアントID
     * @return アクセストークン、エラーの場合には null
     */
    private String executeGetAccessToken(String serviceId, String origin, String clientId) {
        CountDownLatch latch = new CountDownLatch(1);

        GetAccessTokenRequest request = new GetAccessTokenRequest();
        request.setDestination(mDevicePlugin);
        request.setDevicePluginManager(mPluginMgr);
        request.setRequest(mRequest);
        request.setContext(getContext());
        request.setServiceId(serviceId);
        request.setOrigin(origin);
        request.setClientId(clientId);
        request.setLocalOAuth(mLocalOAuth);
        request.setDConnectInterface(mInterface);
        request.setReportedRoundTrip(false);
        request.setOnResponseCallback((response) -> {
            mResponse = response;
            latch.countDown();
        });
        mRequestManager.addRequest(request);

        try {
            latch.await();
        } catch (InterruptedException e) {
            // ignore.
        }

        return request.getAccessToken();
    }

    /**
     * resultの値をレスポンスのIntentから取得する.
     *
     * @param response レスポンスのIntent
     * @return resultの値
     */
    static int getResult(final Intent response) {
        return response.getIntExtra(DConnectMessage.EXTRA_RESULT,
                DConnectMessage.RESULT_ERROR);
    }

    /**
     * errorCodeの値をレスポンスのIntentから取得する.
     *
     * @param response レスポンスのIntent
     * @return errorCodeの値
     */
    static int getErrorCode(final Intent response) {
        return response.getIntExtra(DConnectMessage.EXTRA_ERROR_CODE,
                DConnectMessage.ErrorCode.UNKNOWN.getCode());
    }

    /**
     * プラグインからアクセストークンを求められないプロファイルであるかどうかを判定する.
     *
     * @param profile プロファイル名
     * @return アクセストークンを求めない場合は<code>true</code>、そうでなければ<code>false</code>
     */
    private boolean isIgnoredPluginProfile(final String profile) {
        for (String ignored : DConnectLocalOAuth.IGNORE_PLUGIN_PROFILES) {
            if (ignored.equals(profile)) {
                return true;
            }
        }
        return false;
    }

    /**
     * リクエストからOriginを取得します.
     *
     * @param request リクエスト
     * @return オリジン
     */
    private String getRequestOrigin(final Intent request) {
        String origin = request.getStringExtra(IntentDConnectMessage.EXTRA_ORIGIN);
        if (!mRequireOrigin && origin == null) {
            origin = DConnectConst.ANONYMOUS_ORIGIN;
        }
        return origin;
    }

    /**
     * 指定されたサービスIDに対応するアクセストークンを取得する.
     * アクセストークンが存在しない場合にはnullを返却する。
     *
     * @param origin    リクエスト元のオリジン
     * @param serviceId サービスID
     * @return アクセストークン
     */
    private String getAccessTokenForPlugin(final String origin, final String serviceId) {
        OAuthData oauth = mLocalOAuth.getOAuthData(origin, serviceId);
        if (oauth != null) {
            return mLocalOAuth.getAccessToken(oauth.getId());
        }
        return null;
    }

    /**
     * スコープを一つの文字列に連結する.
     *
     * @param scopes スコープ一覧
     * @return 連結された文字列
     */
    private static String combineStr(final String[] scopes) {
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
     * クライアントデータ.
     */
    private static class ClientData {
        /**
         * クライアントID.
         */
        String mClientId;

        /**
         * クライアントシークレット.
         */
        String mClientSecret;

        @Override
        public String toString() {
            return "ClientData{" +
                    "mClientId='" + mClientId + '\'' +
                    ", mClientSecret='" + mClientSecret + '\'' +
                    '}';
        }
    }

    /**
     * Local OAuth を行うための基底リクエスト.
     *
     */
    private static abstract class OAuthRequest extends DConnectPluginRequest {
        /**
         * 送信元のオリジン.
         */
        private String mOrigin;

        /**
         * 送信先のサービスID.
         */
        private String mServiceId;

        /**
         * LocalOAuth管理クラス.
         */
        private DConnectLocalOAuth mLocalOAuth;

        /**
         * オリジンを設定する.
         *
         * @param origin オリジン
         */
        void setOrigin(final String origin) {
            mOrigin = origin;
        }

        /**
         * サービスIDを設定する.
         *
         * @param id サービスID
         */
        void setServiceId(final String id) {
            mServiceId = id;
        }

        /**
         * LocalOAuth 管理オブジェクトを設定する.
         *
         * @param localOAuth LocalOAuth管理オブジェクト
         */
        void setLocalOAuth(final DConnectLocalOAuth localOAuth) {
            mLocalOAuth = localOAuth;
        }

        /**
         * オリジンを取得します.
         *
         * @return オリジン
         */
        String getOrigin() {
            return mOrigin;
        }

        /**
         * サービスIDを取得します.
         *
         * @return サービスID
         */
        String getServiceId() {
            return mServiceId;
        }

        /**
         * LocalOAuth管理オブジェクトを取得します.
         *
         * @return LocalOAuth管理オブジェクト
         */
        DConnectLocalOAuth getLocalOAuth() {
            return mLocalOAuth;
        }
    }


    /**
     * プラグインに対して ClientData の作成要求を行うクラス.
     */
    private static class CreateClientRequest extends OAuthRequest {
        /**
         * プラグインの返答から作成する ClientData.
         * <p>
         * プラグインからの返答がエラーの場合は null.
         * </p>
         */
        private ClientData mClientData;

        @Override
        public void run() {
            if (getRequest() == null) {
                throw new RuntimeException("mRequest is null.");
            }

            if (mDevicePlugin == null) {
                throw new RuntimeException("mDevicePlugin is null.");
            }

            // リクエストコードを作成
            mRequestCode = UUID.randomUUID().hashCode();

            // リクエストを実行
            executeRequest();
        }

        /**
         * プラグインからの ClientData を取得します.
         *
         * @return ClientData
         */
        ClientData getClientData() {
            return mClientData;
        }

        /**
         * Client を作成するリクエストを実行します.
         */
        private void executeRequest() {
            String serviceId = getServiceId();
            String origin = getOrigin();

            Intent request = createRequestMessage(mRequest, mDevicePlugin);
            request.setAction(IntentDConnectMessage.ACTION_GET);
            request.setComponent(mDevicePlugin.getComponentName());
            request.putExtra(IntentDConnectMessage.EXTRA_REQUEST_CODE, mRequestCode);
            request.putExtra(DConnectMessage.EXTRA_API, "gotapi");
            request.putExtra(DConnectMessage.EXTRA_PROFILE, AuthorizationProfileConstants.PROFILE_NAME);
            request.putExtra(DConnectMessage.EXTRA_INTERFACE, (String) null);
            request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, ATTRIBUTE_CREATE_CLIENT);
            request.putExtra(DConnectProfileConstants.PARAM_SERVICE_ID, serviceId);
            request.putExtra(AuthorizationProfileConstants.PARAM_PACKAGE, origin);

            if (sendRequest(request)) {
                int result = getResult(mResponse);
                if (result == DConnectMessage.RESULT_OK) {
                    String clientId = mResponse.getStringExtra(AuthorizationProfileConstants.PARAM_CLIENT_ID);
                    if (clientId == null) {
                        // クライアントの作成エラー
                        mResponse = new Intent(IntentDConnectMessage.ACTION_RESPONSE);
                        MessageUtils.setAuthorizationError(mResponse, "Cannot create client data.");
                    } else {
                        // クライアントデータを作成
                        mClientData = new ClientData();
                        mClientData.mClientId = clientId;
                        mClientData.mClientSecret = null;
                    }
                } else {
                    int errorCode = getErrorCode(mResponse);
                    if (errorCode == DConnectMessage.ErrorCode.NOT_SUPPORT_PROFILE.getCode()) {
                        // authorizationプロファイルに対応していないのでアクセストークンはいらない。
                        mClientData = new ClientData();
                    }
                }
            } else {
                mResponse = new Intent(IntentDConnectMessage.ACTION_RESPONSE);
                MessageUtils.setPluginDisabledError(mResponse, "Failed to access a plugin.");
            }

            sendResponse(mResponse);
        }
    }

    /**
     * プラグインに対してアクセストークンの作成要求を行うクラス.
     */
    private static class GetAccessTokenRequest extends OAuthRequest {
        /**
         * プラグインの返答から取得したアクセストークン.
         */
        private String mAccessToken;

        /**
         * Client ID.
         */
        private String mClientId;

        @Override
        public void run() {
            if (getRequest() == null) {
                throw new RuntimeException("mRequest is null.");
            }

            if (mDevicePlugin == null) {
                throw new RuntimeException("mDevicePlugin is null.");
            }

            if (mClientId == null) {
                throw new RuntimeException("mClient Id is not set.");
            }

            // リクエストコードを作成
            mRequestCode = UUID.randomUUID().hashCode();

            // リクエストを実行
            executeRequest();
        }

        /**
         * アクセストークンの生成に必要な Client ID を設定します.
         *
         * @param clientId Client ID
         */
        void setClientId(final String clientId) {
            mClientId = clientId;
        }

        /**
         * プラグインから取得したアクセストークンを取得します.
         * <p>
         * プラグインからエラーが返ってきた場合には null.
         * </p>
         * @return アクセストークン
         */
        String getAccessToken() {
            return mAccessToken;
        }

        /**
         * アクセストークンの取得を実行します.
         */
        private void executeRequest() {
            Intent request = createRequestMessage(mRequest, mDevicePlugin);
            request.setAction(IntentDConnectMessage.ACTION_GET);
            request.setComponent(mDevicePlugin.getComponentName());
            request.putExtra(IntentDConnectMessage.EXTRA_REQUEST_CODE, mRequestCode);
            request.putExtra(DConnectMessage.EXTRA_API, "gotapi");
            request.putExtra(DConnectMessage.EXTRA_PROFILE, AuthorizationProfileConstants.PROFILE_NAME);
            request.putExtra(DConnectMessage.EXTRA_INTERFACE, (String) null);
            request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, ATTRIBUTE_REQUEST_ACCESS_TOKEN);
            request.putExtra(AuthorizationProfileConstants.PARAM_CLIENT_ID, mClientId);
            request.putExtra(AuthorizationProfileConstants.PARAM_APPLICATION_NAME, mContext.getString(R.string.app_name));
            request.putExtra(AuthorizationProfileConstants.PARAM_SCOPE, combineStr(getScope()));

            if (sendRequest(request)) {
                int result = getResult(mResponse);
                if (result == DConnectMessage.RESULT_OK) {
                    String accessToken = mResponse.getStringExtra(DConnectMessage.EXTRA_ACCESS_TOKEN);
                    if (accessToken == null) {
                        // アクセストークン作成失敗
                        mResponse = new Intent(IntentDConnectMessage.ACTION_RESPONSE);
                        MessageUtils.setAuthorizationError(mResponse, "Cannot create access token.");
                    } else {
                        mAccessToken = accessToken;
                    }
                } else {
                    int errorCode = getErrorCode(mResponse);
                    if (errorCode == DConnectMessage.ErrorCode.NOT_FOUND_CLIENT_ID.getCode()
                            || errorCode == DConnectMessage.ErrorCode.AUTHORIZATION.getCode()) {
                        // 認証エラーで、有効期限切れ・スコープ範囲外以外は ClientId を作り直す処理を入れる
                        getLocalOAuth().deleteOAuthData(getOrigin(), getServiceId());
                    }
                }
            } else {
                mResponse = new Intent(IntentDConnectMessage.ACTION_RESPONSE);
                MessageUtils.setPluginDisabledError(mResponse, "Failed to access a plugin.");
            }

            sendResponse(mResponse);
        }

        /**
         * デバイスプラグインでサポートするプロファイルの一覧を取得する.
         *
         * @return プロファイルの一覧
         */
        private String[] getScope() {
            List<String> list = mDevicePlugin.getSupportProfileNames();
            return list.toArray(new String[0]);
        }
    }
}
