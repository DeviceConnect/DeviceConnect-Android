/*
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet
 */
package org.deviceconnect.android.localoauth;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;

import org.deviceconnect.android.BuildConfig;
import org.deviceconnect.android.localoauth.activity.ConfirmAuthActivity;
import org.deviceconnect.android.localoauth.exception.AuthorizationException;
import org.deviceconnect.android.localoauth.oauthserver.LoginPageServerResource;
import org.deviceconnect.android.localoauth.oauthserver.SampleUser;
import org.deviceconnect.android.localoauth.oauthserver.SampleUserManager;
import org.deviceconnect.android.localoauth.oauthserver.db.LocalOAuthOpenHelper;
import org.deviceconnect.android.localoauth.oauthserver.db.SQLiteClient;
import org.deviceconnect.android.localoauth.oauthserver.db.SQLiteClientManager;
import org.deviceconnect.android.localoauth.oauthserver.db.SQLiteToken;
import org.deviceconnect.android.localoauth.oauthserver.db.SQLiteTokenManager;
import org.deviceconnect.android.localoauth.temp.RedirectRepresentation;
import org.deviceconnect.android.localoauth.temp.ResultRepresentation;
import org.deviceconnect.android.logger.AndroidHandler;
import org.json.JSONException;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ClientInfo;
import org.restlet.data.Cookie;
import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.ext.oauth.AccessTokenServerResource;
import org.restlet.ext.oauth.AuthPageServerResource;
import org.restlet.ext.oauth.AuthorizationBaseServerResource;
import org.restlet.ext.oauth.AuthorizationServerResource;
import org.restlet.ext.oauth.OAuthException;
import org.restlet.ext.oauth.PackageInfoOAuth;
import org.restlet.ext.oauth.internal.Client;
import org.restlet.ext.oauth.internal.Client.ClientType;
import org.restlet.ext.oauth.internal.ClientManager;
import org.restlet.ext.oauth.internal.Scope;
import org.restlet.ext.oauth.internal.Token;
import org.restlet.ext.oauth.internal.TokenManager;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.util.Series;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Local OAuth API.
 */
public class LocalOAuth2Main {

    /**
     * 認可の結果を通知するアクションを定義します.
     */
    public static final String ACTION_TOKEN_APPROVAL = "org.deviceconnect.android.localoauth.TOKEN_APPROVAL";

    /**
     * 認可の結果に格納されるThread IDのエクストラキーを定義します.
     */
    public static final String EXTRA_THREAD_ID = "org.deviceconnect.android.localoauth.THREAD_ID";

    /**
     * 認可の結果に格納される認可フラグのエクストラキーを定義します.
     */
    public static final String EXTRA_APPROVAL = "org.deviceconnect.android.localoauth.APPROVAL";

    /** ダミー値(RedirectURI). */
    public static final String DUMMY_REDIRECT_URI = "dummyRedirectURI";

    /** ダミー値(OriginalRef). */
    private static final String DUMMY_ORIGINAL_REF = "dummyOriginalRef";

    /** ダミー値(Reference). */
    private static final String DUMMY_REFERENCE = "dummyReference";

    /** ダミー値(Scope). */
    private static final String DUMMY_SCOPE1 = "scope1";

    /** UserManager. */
    private SampleUserManager mUserManager;

    /** ClientManager. */
    private ClientManager mClientManager;

    /** TokenManager. */
    private TokenManager mTokenManager;

    /** DBHelper. */
    private LocalOAuthOpenHelper mDbHelper;

    /** Database. */
    private SQLiteDatabase mDb;

    /** ロガー. */
    private Logger sLogger = Logger.getLogger("org.deviceconnect.localoauth");

    /** DBアクセス用Lockオブジェクト. */
    private final Object mLockForDbAccess = new Object();

    /** 承認確認画面リクエストキュー(アクセスする際はsynchronizedが必要). */
    private List<ConfirmAuthRequest> mRequestQueue = new ArrayList<>();

    /** 承認確認画面リクエストキュー用Lockオブジェクト. */
    private final Object mLockForRequestQueue = new Object();

    private android.content.Context mContext;
    /**
     * コンストラクタ.
     */
    public LocalOAuth2Main(final android.content.Context context) {
        this(context, "localoauth.db");
    }

    /**
     * コンストラクタ.
     */
    public LocalOAuth2Main(final android.content.Context context, final String dbName) {
        // ログレベル設定
        Logger logger = sLogger;
        if (BuildConfig.DEBUG) {
            AndroidHandler handler = new AndroidHandler(logger.getName());
            handler.setFormatter(new SimpleFormatter());
            handler.setLevel(Level.ALL);
            logger.addHandler(handler);
            logger.setLevel(Level.ALL);
        } else {
            logger.setLevel(Level.OFF);
        }

        mContext = context;

        // DB初期化処理
        mDbHelper = new LocalOAuthOpenHelper(context, dbName);
        mDb = mDbHelper.getWritableDatabase();

        // 初期化処理
        mUserManager = new SampleUserManager();
        mClientManager = new SQLiteClientManager(mDb);
        mTokenManager = new SQLiteTokenManager(mDb);

        // ユーザー追加
        addUserData(SampleUser.LOCALOAUTH_USER, SampleUser.LOCALOAUTH_PASS);

        register(context);
    }

    /**
     * (1)-2.LocalOAuth終了処理.
     */
    public void destroy() {
        unregister(mContext);

        // DBをまとめてクローズ
        if (mDbHelper != null) {
            mDbHelper.close();
        }

        mDb = null;
        mUserManager = null;
        mClientManager = null;
        mTokenManager = null;
        mDbHelper = null;
        mContext = null;
    }

    /**
     * SampleUserManagerを返す.
     * @return SampleUserManagerのインスタンス
     */
    public SampleUserManager getSampleUserManager() {
        return mUserManager;
    }

    /**
     * (2)クライアントを登録する.
     * <p>
     * アプリやデバイスプラグインがインストールされるときに実行する.
     * </p>
     * @param packageInfo アプリ(Android)の場合は、パッケージ名を入れる。<br>
     *            アプリ(Web)の場合は、パッケージ名にURLを入れる。<br>
     *            デバイスプラグインの場合は、パッケージ名とサービスIDを入れる。<br>
     * @return 登録したクライアント情報(クライアントID, クライアントシークレット)を返す。<br>
     *         nullは返らない。
     * @throws AuthorizationException Authorization例外.
     */
    public ClientData createClient(final PackageInfoOAuth packageInfo) throws AuthorizationException {
        if (packageInfo == null) {
            throw new IllegalArgumentException("packageInfo is null.");
        } else if (packageInfo.getPackageName() == null) {
            throw new IllegalArgumentException("packageInfo.getPackageName() is null.");
        } else if (packageInfo.getPackageName().length() <= 0) {
            throw new IllegalArgumentException("packageInfo is empty.");
        } else if (!mDb.isOpen()) {
            throw new RuntimeException("Database is not opened.");
        }

        // 長時間使用されていなかったclientIdをクリーンアップする(DBアクセスしたついでに有効クライアント数も取得する)
        int clientCount = cleanupClient();
        
        // クライアント数が上限まで使用されていれば例外発生
        if (clientCount >= LocalOAuth2Settings.CLIENT_MAX) {
            throw new AuthorizationException(AuthorizationException.CLIENT_COUNTS_IS_FULL);
        }
        
        // クライアント追加
        ClientData clientData;

        synchronized (mLockForDbAccess) {
            try {
                mDb.beginTransaction();

                // パッケージ情報に対応するクライアントIDがすでに登録済なら破棄する
                Client client = mClientManager.findByPackageInfo(packageInfo);
                if (client != null) {
                    String clientId = client.getClientId();
                    removeTokenData(clientId);
                    removeClientData(clientId);
                }
                
                // クライアントデータを新規生成して返す
                client = addClientData(packageInfo);
                clientData = new ClientData(client.getClientId(), String.copyValueOf(client.getClientSecret()));
                
                mDb.setTransactionSuccessful();
            } catch (SQLiteException e) {
                throw new RuntimeException(e);
            } finally {
                mDb.endTransaction();
            }
        }
        
        return clientData;
    }

    /**
     * (5)アクセストークン発行承認確認画面表示.
     * <p>
     * - Activityはprocess指定を行わないのでDevice Connect Managerのスレッドとは別プロセスとなる。<br>
     * - Device Connect Managerのサービスととプロセス間通信が行えるように(0)onBind()でBindする。<br>
     * - Messenger, Handler はLocalOAuth内部に持つ。<br>
     * 
     * - 状況別で動作が変わる。<br>
     * 
     * - (a)有効なアクセストークンが存在しない。(失効中も含む) =>
     * 承認確認画面を表示する。承認/拒否はBindされたServiceへMessageで通知される。<br>
     * 
     * - (b)アクセストークンは存在するがスコープが不足している。 =>
     * 承認確認画面を表示する。承認/拒否はBindされたServiceへMessageで通知される。<br>
     * 
     * - (c)アクセストークンは存在しスコープも満たされている。 => 承認確認画面は表示しない。(Message通知されない)<br>
     * </p>
     * @param params パラメータ
     * @param listener アクセストークン発行リスナー(承認確認画面で承認／拒否ボタンが押されたら実行される)
     * @throws AuthorizationException Authorization例外.
     */
    public void confirmPublishAccessToken(final ConfirmAuthParams params,
            final PublishAccessTokenListener listener) throws AuthorizationException {
        if (params == null) {
            throw new IllegalArgumentException("confirmAuthParams is null.");
        } else if (listener == null) {
            throw new IllegalArgumentException("publishAccessTokenListener is null.");
        } else if (params.getContext() == null) {
            throw new IllegalArgumentException("Context is null.");
        } else if (params.getApplicationName() == null || params.getApplicationName().isEmpty()) {
            throw new IllegalArgumentException("ApplicationName is null.");
        } else if (params.getClientId() == null || params.getClientId().isEmpty()) {
            throw new IllegalArgumentException("ClientId is null.");
        } else if (params.getScopes() == null || params.getScopes().length <= 0) {
            throw new IllegalArgumentException("Scope is null.");
        } else if (!mDb.isOpen()) {
            throw new RuntimeException("Database is not opened.");
        }

        synchronized (mLockForDbAccess) {
            // カレントスレッドID取得
            Thread thread = Thread.currentThread();
            final long threadId = thread.getId();

            // ロケール取得(アンダーバーがついている場合("ja_JP"等)は、アンダーバーから後の文字列は削除する)
            String locale = Locale.getDefault().getLanguage();
            String[] splitLocales = locale.split("_");
            if (splitLocales.length > 1) {
                locale = splitLocales[0];
            }

            // デバイスプラグインの場合はdevicePlugin.xmlからロケールが一致する表示スコープ名を取得する
            Map<String, DevicePluginXmlProfile> supportProfiles = null;
            if (params.isForDevicePlugin()) {
                supportProfiles =
                        DevicePluginXmlUtil.getSupportProfiles(
                                params.getContext(),
                                params.getContext().getPackageName());
            }
            String[] scopes = params.getScopes();
            String[] displayScopes = new String[scopes.length];
            for (int i = 0; i < scopes.length; i++) {
                // ローカライズされたプロファイル名取得する
                displayScopes[i] = ScopeUtil.getDisplayScope(params.getContext(),
                        scopes[i], locale, supportProfiles);
            }

            // リクエストデータを作成する
            ConfirmAuthRequest request = new ConfirmAuthRequest(threadId, params,
                    listener, displayScopes, params.isAutoFlag());

            // キューにリクエストを追加
            enqueueRequest(request);

            // ActivityがサービスがBindされていない場合には、
            // Activityを起動する。
            if (mRequestQueue.size() <= 1) {
                startConfirmAuthActivity(pickupRequest());
            }
        }
    }

    /**
     * 1分以内かチェックします.
     * @param t チェックする経過時間
     * @return trueの場合は1分以内、falseの場合はそれ以外
     */
    private static boolean checkTime(final long t) {
        return 0 <= t && t <= (LocalOAuth2Settings.ACCESS_TOKEN_GRACE_TIME * LocalOAuth2Settings.MSEC);
    }

    /**
     * (7)アクセストークンを確認する.
     * 
     * @param accessToken 確認するアクセストークン
     * @param scope このスコープがアクセストークンに含まれるかチェックする
     * @param specialScopes 承認許可されていなくてもアクセス可能なscopes(nullなら指定無し)
     * @return チェック結果
     */
    public CheckAccessTokenResult checkAccessToken(final String accessToken, final String scope,
            final String[] specialScopes) {
        if (scope == null) {
            throw new IllegalArgumentException("scope is null.");
        }

        // true: アクセストークンを発行したクライアントIDあり
        // false: アクセストークンを発行したクライアントIDなし
        boolean isExistClientId = false;
        // true: アクセストークンあり / false: アクセストークンなし
        boolean isExistAccessToken = false;
        // true: スコープあり / false: スコープなし
        boolean isExistScope = false;
        // true: 有効期限内 / false: 有効期限切れ
        boolean isNotExpired = false;

        // 無視するスコープが指定されていた場合
        if (specialScopes != null && Arrays.asList(specialScopes).contains(scope)) {
            return new CheckAccessTokenResult(true, true, true, true);
        }

        // アクセストークンが設定されていない場合
        if (accessToken == null) {
            return new CheckAccessTokenResult(false, false, false, false);
        }

        synchronized (mLockForDbAccess) {
            if (!mDb.isOpen()) {
                throw new RuntimeException("Database is not opened.");
            }

            try {
                mDb.beginTransaction();

                // アクセストークンを元にトークンを検索する
                SQLiteToken token = (SQLiteToken) mTokenManager.findTokenByAccessToken(accessToken);
                if (token != null) {
                    // アクセストークンあり
                    isExistAccessToken = true;
                    Scope[] scopes = token.getScope();
                    for (Scope s : scopes) {
                        // token.scopeに"*"が含まれていたら、どんなスコープにもアクセスできる
                        if (BuildConfig.DEBUG && s.getScope().equals("*")) {
                            isExistScope = true; // スコープあり
                            isNotExpired = true; // 有効期限
                            break;
                        }

                        if (s.getScope().equals(scope)) {
                            isExistScope = true; // スコープあり
                            
                            if (s.getExpirePeriod() == 0) {
                                // 有効期限0の場合は、トークン発行から1分以内の初回アクセスなら有効期限内とする
                                long t = System.currentTimeMillis() - token.getRegistrationDate();
                                if (checkTime(t) && token.isFirstAccess()) {
                                    isNotExpired = true;
                                }
                            } else if (s.getExpirePeriod() > 0) {
                                // 有効期限1以上の場合は、トークン発行からの経過時間が有効期限内かを判定して返す
                                isNotExpired = !s.isExpired();
                            } else {
                                // 有効期限にマイナス値が設定されていたら、有効期限切れとみなす
                                isNotExpired = false;
                            }
                            break;
                        }
                    }
    
                    // このトークンを発行したクライアントIDが存在するかチェック
                    if (mClientManager.findById(token.getClientId()) != null) {
                        isExistClientId = true;
                    }
    
                    // トークンのアクセス時間更新
                    if (token.isFirstAccess()) {
                        token.dbUpdateTokenAccessTime(mDb);
                    }
                }
                
                mDb.setTransactionSuccessful();
            } catch (SQLiteException e) {
                throw new RuntimeException(e);
            } finally {
                mDb.endTransaction();
            }
        }
        
        CheckAccessTokenResult result = new CheckAccessTokenResult(isExistClientId,
                isExistAccessToken, isExistScope, isNotExpired);
        if (!result.checkResult()) {
            sLogger.warning("checkAccessToken() - error.");
            sLogger.warning(" - isExistClientId: " + isExistClientId);
            sLogger.warning(" - isExistAccessToken: " + isExistAccessToken);
            sLogger.warning(" - isExistScope:" + isExistScope);
            sLogger.warning(" - isNotExpired:" + isNotExpired);
            sLogger.warning(" - accessToken:" + accessToken);
            sLogger.warning(" - scope:" + scope);
        }
        return result;
    }

    /**
     * (10)アクセストークンからクライアントパッケージ情報を取得する.
     * 
     * @param accessToken アクセストークン
     * @return not null: クライアントパッケージ情報 / null:アクセストークンがないのでクライアントパッケージ情報が取得できない。
     */
    public ClientPackageInfo findClientPackageInfoByAccessToken(final String accessToken) {
        if (accessToken == null) {
            throw new IllegalArgumentException("accessToken is null.");
        } else if (!mDb.isOpen()) {
            throw new RuntimeException("Database is not opened.");
        }

        ClientPackageInfo clientPackageInfo = null;
        synchronized (mLockForDbAccess) {
            try {
                SQLiteToken token = (SQLiteToken) mTokenManager.findTokenByAccessToken(accessToken);
                if (token != null) {
                    String clientId = token.getClientId();
                    if (clientId != null) {
                        Client client = mClientManager.findById(clientId);
                        if (client != null) {
                            clientPackageInfo = new ClientPackageInfo(client.getPackageInfo(), clientId);
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return clientPackageInfo;
    }
    
    /**
     * (13)-1.アクセストークン一覧を取得する(startAccessTokenListActivity()用).
     * @return not null: アクセストークンの配列 / null: アクセストークンなし
     */
    public SQLiteToken[] getAccessTokens() {
        synchronized (mLockForDbAccess) {
            if (!mDb.isOpen()) {
                throw new RuntimeException("Database is not opened.");
            }

            try {
                // LocalOAuthが保持しているクライアントシークレットを取得
                return (SQLiteToken[]) mTokenManager.findTokens(SampleUser.USERNAME);
            } catch (SQLiteException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * クライアントに対応するトークンデータを取得する.
     * <p>
     * トークンが存在しない場合にはnullを返却する。
     * </p>
     * @param client クライアントデータ
     * @return トークンデータ
     */
    public SQLiteToken getAccessToken(final Client client) {
        synchronized (mLockForDbAccess) {
            if (!mDb.isOpen()) {
                throw new RuntimeException("Database is not opened.");
            }

            try {
                // LocalOAuthが保持しているクライアントシークレットを取得
                return (SQLiteToken) mTokenManager.findToken(client, SampleUser.USERNAME);
            } catch (SQLiteException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    /**
     * (13)-2.アクセストークンを破棄して利用できないようにする(startAccessTokenListActivity()用.
     * 
     * @param tokenId トークンID
     */
    public void destroyAccessToken(final long tokenId) {
        synchronized (mLockForDbAccess) {
            if (!mDb.isOpen()) {
                throw new RuntimeException("Database is not opened.");
            }

            try {
                mDb.beginTransaction();

                ((SQLiteTokenManager) mTokenManager).revokeToken(tokenId);

                mDb.setTransactionSuccessful();
            } catch (SQLiteException e) {
                throw new RuntimeException(e);
            } finally {
                mDb.endTransaction();
            }
        }
    }

    /**
     * (13)-3.DBのトークンデータを削除(startAccessTokenListActivity()用.
     * 
     */
    public void destroyAllAccessToken() {
        synchronized (mLockForDbAccess) {
            if (!mDb.isOpen()) {
                throw new RuntimeException("Database is not opened.");
            }

            try {
                mDb.beginTransaction();
                
                mTokenManager.revokeAllTokens(SampleUser.USERNAME);
                
                mDb.setTransactionSuccessful();
            } catch (SQLiteException e) {
                throw new RuntimeException(e);
            } finally {
                mDb.endTransaction();
            }
        }
    }

    /**
     * (13)-4.クライアントIDが一致するクライアントデータを取得する(startAccessTokenListActivity()用).
     * @param clientId クライアントID
     * @return not null: クライアント / null: クライアントなし
     */
    public SQLiteClient findClientByClientId(final String clientId) {
        synchronized (mLockForDbAccess) {
            if (!mDb.isOpen()) {
                throw new RuntimeException("Database is not opened.");
            }

            try {
                // LocalOAuthが保持しているクライアントシークレットを取得
                return (SQLiteClient) mClientManager.findById(clientId);
            } catch (SQLiteException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    /**
     * 長時間使用されていなかったclientIdをクリーンアップする(DBアクセスしたついでに有効クライアント数も取得する).
     * @return 有効クライアント数
     */
    private int cleanupClient() {
        int clientCount;
        synchronized (mLockForDbAccess) {
            if (!mDb.isOpen()) {
                throw new RuntimeException("Database is not opened.");
            }

            try {
                mDb.beginTransaction();
                
                SQLiteClientManager mgr = (SQLiteClientManager) mClientManager;

                mgr.cleanupClient(LocalOAuth2Settings.CLIENT_CLEANUP_TIME);
                clientCount = mgr.countClients();
                
                mDb.setTransactionSuccessful();
            } catch (SQLiteException e) {
                throw new RuntimeException(e);
            } finally {
                mDb.endTransaction();
            }
        }
        
        return clientCount;
    }

    /**
     * 認証処理を行う.
     * @param threadId 認証用スレッドID
     * @param isApproval 承認する場合はtrue、拒否する場合はfalse
     */
    private void processApproval(final long threadId, final boolean isApproval) {
        // 承認確認画面を表示する直前に保存しておいたパラメータデータを取得する
        ConfirmAuthRequest request = dequeueRequest(threadId, false);
        if (request != null && !request.isDoneResponse()) {
            request.setDoneResponse(true);
            request.stopTimer();

            PublishAccessTokenListener publishAccessTokenListener = request.getPublishAccessTokenListener();
            ConfirmAuthParams params = request.getConfirmAuthParams();

            if (isApproval) {
                AccessTokenData accessTokenData = null;
                AuthorizationException exception = null;

                synchronized (mLockForDbAccess) {
                    if (!mDb.isOpen()) {
                        exception = new AuthorizationException(AuthorizationException.SQLITE_ERROR);
                    } else {
                        try {
                            mDb.beginTransaction();

                            // アクセストークン発行する前に古い無効なトークン
                            // (クライアントIDが削除されて残っていたトークン)をクリーンアップする
                            ((SQLiteTokenManager) mTokenManager).cleanup();

                            // アクセストークン発行
                            accessTokenData = publishAccessToken(params);

                            mDb.setTransactionSuccessful();
                        } catch (AuthorizationException e) {
                            exception = e;
                        } catch (SQLiteException e) {
                            exception = new AuthorizationException(AuthorizationException.SQLITE_ERROR);
                        } finally {
                            mDb.endTransaction();
                        }
                    }
                }

                if (exception == null) {
                    // リスナーを通じてアクセストークンを返す
                    callPublishAccessTokenListener(accessTokenData, publishAccessTokenListener);
                } else {
                    // リスナーを通じて発生した例外を返す
                    callExceptionListener(exception, publishAccessTokenListener);
                }
            } else {
                // リスナーを通じて拒否通知を返す
                callPublishAccessTokenListener(null, publishAccessTokenListener);
            }

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Activityが終了するタイミングが取得できないので、ここでキューからリクエストを削除する
                    dequeueRequest(threadId, true);

                    // キューにリクエストが残っていれば、次のキューを取得してActivityを起動する
                    final ConfirmAuthRequest nextRequest = pickupRequest();
                    if (nextRequest != null) {
                        startConfirmAuthActivity(nextRequest);
                    }
                }
            }, 2000);
        }
    }

    /**
     * アクセストークンデータを返却する.
     * 
     * @param params 承認確認画面のパラメータ
     * @return アクセストークンデータ
     * @throws AuthorizationException Authorization例外.
     */
    private AccessTokenData publishAccessToken(final ConfirmAuthParams params) throws AuthorizationException {
        String clientId = params.getClientId();
        Client client = mClientManager.findById(clientId);
        if (client != null) {
            // AuthSessionを登録してセッションIDを取得
            RedirectRepresentation redirectRepresentation = callAuthorizationServerResource(client, true, null);
            if (redirectRepresentation != null) {
                String sessionId = redirectRepresentation.getOptions().get(RedirectRepresentation.SESSION_ID).toString();
                // ログイン処理
                ResultRepresentation result = callLoginPageServerResource(
                        SampleUser.LOCALOAUTH_USER, SampleUser.LOCALOAUTH_PASS);
                if (result.getResult()) {
                    RedirectRepresentation result3 = callAuthorizationServerResource(client, false, sessionId);
                    if (result3 != null) {
                        // デバイスプラグインならxmlファイルに有効期限が存在すれば取得して使用する
                        Map<String, DevicePluginXmlProfile> supportProfiles = null;
                        if (params.isForDevicePlugin()) {
                            supportProfiles = DevicePluginXmlUtil.getSupportProfiles(params.getContext(),
                                    params.getContext().getPackageName());
                        }
                        
                        // スコープを登録(Scope型に変換して有効期限を追加する)
                        String[] scopes = params.getScopes();
                        ArrayList<Scope> settingScopes = new ArrayList<>();
                        for (String scope : scopes) {
                            // デバイスプラグインならxmlファイルに有効期限が存在すれば取得して使用する(無ければデフォルト値)
                            long expirePeriod = LocalOAuth2Settings.DEFAULT_TOKEN_EXPIRE_PERIOD;
                            if (supportProfiles != null) {
                                DevicePluginXmlProfile xmlProfile = supportProfiles.get(scope);
                                if (xmlProfile != null) {
                                    expirePeriod = xmlProfile.getExpirePeriod();
                                }
                            }
                            
                            Scope s = new Scope(scope, System.currentTimeMillis(), expirePeriod);
                            settingScopes.add(s);
                        }

                        // 認可コード取得
                        String applicationName = params.getApplicationName();
                        RedirectRepresentation result4 = callAuthPageServerResource(sessionId, settingScopes, applicationName);
                        if (result4 != null) {
                            Map<String, Object> options = result4.getOptions();
                            String authCode = (String) options.get(AuthPageServerResource.CODE);
                            if (authCode != null) {
                                // アクセストークン取得
                                String accessToken = callAccessTokenServerResource(client, authCode, applicationName);
                                // トークンデータを参照
                                Token token = mTokenManager.findTokenByAccessToken(accessToken);
                                
                                // アクセストークンデータを返す
                                AccessTokenScope[] accessTokenScopes = scopesToAccessTokenScopes(token.getScope());
                                return new AccessTokenData(accessToken,
                                        token.getRegistrationDate(), accessTokenScopes);
                            }
                        }
                    }
                }
            }
        } else {
            throw new AuthorizationException(AuthorizationException.CLIENT_NOT_FOUND);
        }

        return null;
    }

    /**
     * リスナーを通じてアクセストークンを返す.
     * 
     * @param accessTokenData アクセストークンデータ
     * @param publishAccessTokenListener アクセストークン発行リスナー
     */
    private void callPublishAccessTokenListener(final AccessTokenData accessTokenData, final PublishAccessTokenListener publishAccessTokenListener) {
        if (publishAccessTokenListener != null) {
            // リスナーを実行してアクセストークンデータを返す
            publishAccessTokenListener.onReceiveAccessToken(accessTokenData);
        } else {
            // リスナーが登録されていないので通知できない
            throw new RuntimeException("publishAccessTokenListener is null.");
        }
    }

    /**
     * リスナーを通じてアクセストークンを返す.
     * 
     * @param exception 例外
     * @param publishAccessTokenListener アクセストークン発行リスナー
     */
    private void callExceptionListener(final Exception exception,
            final PublishAccessTokenListener publishAccessTokenListener) {
        if (publishAccessTokenListener != null) {
            // リスナーを実行して例外データを返す
            publishAccessTokenListener.onReceiveException(exception);
        } else {
            // リスナーが登録されていないので通知できない
            throw new RuntimeException("publishAccessTokenListener is null.");
        }
    }
    
    /**
     * AuthorizationServerResourceを実行.
     * 
     * @param client クライアント情報
     * @param initialize 初期化フラグ(trueにするとContextを初期化する)
     * @param sessionId セッションID(引き継ぐセッションIDが存在すれば指定する、無ければnullを設定する)
     * @return not null: RedirectRepresentation型の戻り値を返す / null: エラー
     * @throws AuthorizationException Authorization例外.
     */
    private RedirectRepresentation callAuthorizationServerResource(final Client client,
            final boolean initialize, final String sessionId) throws AuthorizationException {

        // AuthorizationServerResourceを初期化する
        if (initialize) {
            Context context = new Context(sLogger);
            AuthorizationServerResource.init(context);
        }

        // request, responseを初期化 *
        Request request = new Request();
        request.setOriginalRef(new Reference(DUMMY_ORIGINAL_REF));
        Response response = new Response(request);
        request.setResourceRef(new Reference(DUMMY_REFERENCE));

        // セッションIDが指定されていたらRequestに設定する
        if (sessionId != null) {
            Series<Cookie> cookies = new Series<>(Cookie.class);
            cookies.add(AuthorizationBaseServerResource.ClientCookieID, sessionId);
            request.setCookies(cookies);
        }

        AuthorizationServerResource.init(request, response, mClientManager, mTokenManager);
        
        // Formに設定する
        Form paramsA = new Form();
        paramsA.add(AuthorizationServerResource.CLIENT_ID, client.getClientId());
        paramsA.add(AuthorizationServerResource.REDIR_URI, DUMMY_REDIRECT_URI);
        paramsA.add(AuthorizationServerResource.RESPONSE_TYPE, "code");
        paramsA.add(AuthorizationServerResource.SCOPE, DUMMY_SCOPE1);

        // requestAuthorizationを実行する
        Representation representationA;
        try {
            representationA = AuthorizationServerResource.requestAuthorization(paramsA);
        } catch (OAuthException e) {
            throw new AuthorizationException(e);
        }

        // 正常終了(ログイン画面リダイレクト)
        if (representationA instanceof RedirectRepresentation) {
            return (RedirectRepresentation) representationA;
        }

        return null;
    }

    /**
     * LoginPageServerResourceを実行する.
     * 
     * @param userId ユーザーID
     * @param password パスワード
     * @return 戻り値(ResultRepresentation)
     */
    private ResultRepresentation callLoginPageServerResource(final String userId, final String password) {
        // 前の処理からセッションIDを引き継ぐ
        String sessionId = AuthorizationServerResource.getSessionId();
        Series<Cookie> cookies = new Series<>(Cookie.class);
        cookies.add(AuthorizationBaseServerResource.ClientCookieID, sessionId);

        // (B)の処理
        LoginPageServerResource.initResult();
        Request request = new Request();
        Reference requestReference = new Reference(DUMMY_ORIGINAL_REF);
        requestReference.addQueryParameter(LoginPageServerResource.USER_ID, userId);
        requestReference.addQueryParameter(LoginPageServerResource.PASSWORD, password);
        requestReference.addQueryParameter(LoginPageServerResource.CONTINUE,
                RedirectRepresentation.RedirectProc.requestAuthorization.toString());

        // QueryParameterとは別の変数に値を入れているので、直接値を設定する
        ArrayList<String> userIds = new ArrayList<>();
        userIds.add(userId);
        ArrayList<String> passwords = new ArrayList<>();
        passwords.add(password);
        ArrayList<String> continues = new ArrayList<>();
        continues.add(RedirectRepresentation.RedirectProc.requestAuthorization.toString());

        LoginPageServerResource.getQuery().put(LoginPageServerResource.USER_ID, userIds);
        LoginPageServerResource.getQuery().put(LoginPageServerResource.PASSWORD, passwords);
        LoginPageServerResource.getQuery().put(LoginPageServerResource.CONTINUE, continues);

        request.setCookies(cookies);
        request.setOriginalRef(requestReference);
        request.setResourceRef(requestReference);
        Response response = new Response(request);
        LoginPageServerResource.init(request, response);
        ResultRepresentation resultRepresentation;
        try {
            resultRepresentation = (ResultRepresentation) LoginPageServerResource.getPage(this);
        } catch (OAuthException e) {
            resultRepresentation = new ResultRepresentation();
            resultRepresentation.setResult(false);
            resultRepresentation.setError(e.getMessage(), e.getErrorDescription());
        }

        return resultRepresentation;
    }

    /**
     * AuthPageServerResourceを実行する.
     * 
     * @param sessionId セッションID
     * @param scopes スコープ
     * @param applicationName アプリケーション名
     * @return 戻り値(RedirectRepresentation)
     */
    private RedirectRepresentation callAuthPageServerResource(final String sessionId,
            final ArrayList<Scope> scopes, final String applicationName) {

        Series<Cookie> cookies = new Series<>(Cookie.class);
        cookies.add(AuthorizationBaseServerResource.ClientCookieID, sessionId);

        // scopesを文字列配列に変換する
        ArrayList<String> strScopes = ScopeUtil.scopesToStrings(scopes);
        
        ArrayList<String> actions = new ArrayList<>();
        actions.add(AuthPageServerResource.ACTION_ACCEPT);
        
        ArrayList<String> applicationNames = new ArrayList<>();
        applicationNames.add(applicationName);
        
        AuthPageServerResource.getQuery().put(AuthPageServerResource.SCOPE, strScopes);
        AuthPageServerResource.getQuery().put(AuthPageServerResource.GRANTED_SCOPE, new ArrayList<String>());
        AuthPageServerResource.getQuery().put(AuthPageServerResource.ACTION, actions);
        AuthPageServerResource.getQuery().put(AuthPageServerResource.APPLICATION_NAME, applicationNames);

        Request request = new Request();
        request.setCookies(cookies);
        Response response = new Response(request);
        AuthPageServerResource.init(request, response);

        RedirectRepresentation redirectRepresentation = null;
        try {
            Representation representation = AuthPageServerResource.showPage();
            if (representation != null) {
                if (representation instanceof RedirectRepresentation) {
                    redirectRepresentation = (RedirectRepresentation) representation;
                }
            }
        } catch (OAuthException e) {
            e.printStackTrace();
        }

        return redirectRepresentation;
    }

    /**
     * 認可コードを渡してアクセストークンを取得する.
     * 
     * @param client クライアント
     * @param authCode 認可コード(TokenManager.sessionsに存在するキー値を設定する)
     * @param applicationName アプリケーション名
     * @return not null: アクセストークン / null: アクセストークン取得失敗
     */
    private String callAccessTokenServerResource(final Client client, final String authCode,
            final String applicationName) {

        Request request = new Request();
        ClientInfo clientInfo = new ClientInfo();
        org.restlet.security.User user = new org.restlet.security.User(client.getClientId());
        clientInfo.setUser(user);
        request.setClientInfo(clientInfo);

        Response response = new Response(request);
        AccessTokenServerResource.init(request, response);

        // 入力値(アプリケーション名はbase64エンコードする)
        String base64ApplicationName = Base64.encodeToString(applicationName.getBytes(), Base64.URL_SAFE|Base64.NO_WRAP);
        StringRepresentation input = new StringRepresentation("grant_type=authorization_code&code=" + authCode + "&"
                + AccessTokenServerResource.REDIR_URI + "=" + DUMMY_REDIRECT_URI + "&"
                + AccessTokenServerResource.APPLICATION_NAME + "=" + base64ApplicationName);
        try {
            ResultRepresentation resultRepresentation = (ResultRepresentation) AccessTokenServerResource
                    .requestToken(input);
            if (resultRepresentation.getResult()) {
                return resultRepresentation.getText();
            }
        } catch (JSONException | OAuthException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * ユーザーデータ追加.
     * 
     * @param user ユーザーID
     * @param pass パスワード
     */
    private void addUserData(final String user, final String pass) {
        mUserManager.addUser(user).setPassword(pass.toCharArray());
    }

    /**
     * クライアントデータ追加.
     * 
     * @param packageInfo パッケージ名
     * @return クライアントデータ
     */
    private Client addClientData(final PackageInfoOAuth packageInfo) {
        String[] redirectURIs = {DUMMY_REDIRECT_URI};
        Map<String, Object> params = new HashMap<>();
        return mClientManager.createClient(packageInfo, ClientType.CONFIDENTIAL, redirectURIs, params);
    }

    /**
     * クライアントデータ削除.
     * 
     * @param clientId クライアントID
     */
    private void removeClientData(final String clientId) {
        Client client = mClientManager.findById(clientId);
        if (client != null) {
            mClientManager.deleteClient(clientId);
        }
    }

    /**
     * 指定したクライアントに紐づくトークンデータを削除する.
     * @param clientId トークンデータ
     */
    private void removeTokenData(final String clientId) {
        Client client = mClientManager.findById(clientId);
        if (client != null) {
            mTokenManager.revokeToken(client);
        }
    }

    /**
     * 承認確認画面リクエストをキューに追加する.
     * 
     * @param request リクエスト
     */
    private void enqueueRequest(final ConfirmAuthRequest request) {
        synchronized (mLockForRequestQueue) {
            mRequestQueue.add(request);
        }
    }

    /**
     * キュー先頭の承認確認画面リクエストをキューから取得する.
     * 
     * @return not null: 取得したリクエスト / null: キューにデータなし
     */
    private ConfirmAuthRequest pickupRequest() {
        synchronized (mLockForRequestQueue) {
            while (mRequestQueue.size() > 0) {
                ConfirmAuthRequest request = mRequestQueue.get(0);
                if (System.currentTimeMillis() - request.getRequestTime() < 60 * 1000) {
                    return request;
                }
                mRequestQueue.remove(0);
            }
        }
        return null;
    }

    /**
     * threadIdが一致する承認確認画面リクエストをキューから取得する。(キューから削除することも可能).
     * 
     * @param threadId キーとなるスレッドID
     * @param isDeleteRequest true: スレッドIDが一致したリクエストを返すと同時にキューから削除する。 / false: 削除しない。
     * @return not null: 取り出されたリクエスト / null: 該当するデータなし(存在しないthreadIdが渡された、またはキューにデータ無し)
     */
    private ConfirmAuthRequest dequeueRequest(final long threadId, final boolean isDeleteRequest) {
        ConfirmAuthRequest request = null;
        synchronized (mLockForRequestQueue) {
            // スレッドIDが一致するリクエストデータを検索する
            int requestCount = mRequestQueue.size();
            for (int i = 0; i < requestCount; i++) {
                ConfirmAuthRequest req = mRequestQueue.get(i);
                if (req.getThreadId() == threadId) {
                    if (isDeleteRequest) {
                        // スレッドIDに対応するリクエストデータを取得し、キューから削除する
                        request = mRequestQueue.remove(i);
                    } else {
                        // スレッドIDに対応するリクエストデータを取得
                        request = mRequestQueue.get(i);
                    }
                    break;
                }
            }
        }
        return request;
    }

    /**
     * Scope[]からAccessTokenScope[]に変換して返す.
     * @param scopes Scope[]の値
     * @return AccessTokenScope[]の値
     */
    private AccessTokenScope[] scopesToAccessTokenScopes(final Scope[] scopes) {
        if (scopes != null && scopes.length > 0) {
            AccessTokenScope[] accessTokenScopes = new AccessTokenScope[scopes.length];
            for (int i = 0; i < scopes.length; i++) {
                Scope scope = scopes[i];
                accessTokenScopes[i] = new AccessTokenScope(scope.getScope(), scope.getExpirePeriod()); 
            }
            return accessTokenScopes;
        }
        return null;
    }

    /**
     * リクエストデータを使ってアクセストークン発行承認確認画面を起動する.
     * @param request リクエストデータ
     */
    public void startConfirmAuthActivity(final ConfirmAuthRequest request) {
        if (request == null) {
            return;
        }

        android.content.Context context = request.getConfirmAuthParams().getContext();
        long threadId = request.getThreadId();
        ConfirmAuthParams params = request.getConfirmAuthParams();
        String[] displayScopes = request.getDisplayScopes();
        
        // Activity起動(許可・拒否の結果は、ApprovalHandlerへ送られる)
        Intent intent = new Intent();
        intent.setClass(params.getContext(), ConfirmAuthActivity.class);
        intent.putExtra(ConfirmAuthActivity.EXTRA_THREAD_ID, threadId);
        if (params.getServiceId() != null) {
            intent.putExtra(ConfirmAuthActivity.EXTRA_SERVICE_ID, params.getServiceId());
        }
        intent.putExtra(ConfirmAuthActivity.EXTRA_APPLICATION_NAME, params.getApplicationName());
        intent.putExtra(ConfirmAuthActivity.EXTRA_SCOPES, params.getScopes());
        intent.putExtra(ConfirmAuthActivity.EXTRA_DISPLAY_SCOPES, displayScopes);
        intent.putExtra(ConfirmAuthActivity.EXTRA_REQUEST_TIME, request.getRequestTime());
        intent.putExtra(ConfirmAuthActivity.EXTRA_IS_FOR_DEVICEPLUGIN, params.isForDevicePlugin());
        if (!params.isForDevicePlugin()) {
            intent.putExtra(ConfirmAuthActivity.EXTRA_PACKAGE_NAME, context.getPackageName());
            intent.putExtra(ConfirmAuthActivity.EXTRA_KEYWORD, params.getKeyword());
        }
        intent.putExtra(ConfirmAuthActivity.EXTRA_AUTO_FLAG, request.isAutoFlag());
        intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

        request.startTimer(new ConfirmAuthRequest.OnTimeoutCallback() {
            @Override
            public void onTimeout() {
                processApproval(request.getThreadId(), false);
            }
        });
    }

    /**
     * 認可の結果を受け取るためのLocalBroadcastReceiverを登録します.
     *
     * @param context 登録するコンテキスト
     */
    private void register(android.content.Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_TOKEN_APPROVAL);
        LocalBroadcastManager.getInstance(context).registerReceiver(mBroadcastReceiver, filter);
    }

    /**
     * 認可の結果を受け取るためのLocalBroadcastReceiverを解除します.
     *
     * @param context 解除するコンテキスト
     */
    private void unregister(android.content.Context context) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mBroadcastReceiver);
    }

    /**
     * 認可の結果を受け取るためのLocalBroadcastReceiverクラス.
     */
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(android.content.Context context, Intent intent) {
            if (intent == null) {
                return;
            }

            String action = intent.getAction();
            if (ACTION_TOKEN_APPROVAL.equals(action)) {
                long threadId = intent.getLongExtra(EXTRA_THREAD_ID, -1);
                boolean isApproval = intent.getBooleanExtra(EXTRA_APPROVAL, false);
                processApproval(threadId, isApproval);
            }
        }
    };
}
