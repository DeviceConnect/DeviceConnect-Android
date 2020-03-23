/*
 DConnectProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.message.DevicePluginContext;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.spec.DConnectServiceSpec;
import org.deviceconnect.android.profile.spec.OpenAPIValidator;
import org.deviceconnect.android.profile.spec.models.Method;
import org.deviceconnect.android.profile.spec.models.Swagger;
import org.deviceconnect.android.profile.spec.models.parameters.Parameter;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.deviceconnect.profile.DConnectProfileConstants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * DConnect プロファイルクラス.
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class DConnectProfile implements DConnectProfileConstants {
    /**
     * バッファサイズを定義.
     */
    private static final int BUF_SIZE = 4096;

    /**
     * 内部エクストラ: {@value}.
     */
    private static final String INNER_EXTRA_ORIGIN = "_origin";

    /**
     * コンテキスト.
     */
    private Context mContext;

    /**
     * プラグインのコンテキストクラス.
     */
    private DevicePluginContext mPluginContext;

    /**
     * DeviceConnectサービス.
     */
    private DConnectService mService;

    /**
     * レスポンスを返却するためのインターフェース.
     */
    private Responder mResponder;

    /**
     * ロガー.
     */
    protected final Logger mLogger = Logger.getLogger("org.deviceconnect.dplugin");

    /**
     * サポートするAPI.
     */
    protected final Map<ApiIdentifier, DConnectApi> mApis = new HashMap<>();

    /**
     * プロファイルに設定されているDevice Connect API実装のリストを返す.
     *
     * @return API実装のリスト
     */
    public List<DConnectApi> getApiList() {
        return new ArrayList<>(mApis.values());
    }

    /**
     * 指定されたリクエストに対応するDevice Connect API実装を返す.
     *
     * @param request リクエスト
     * @return 指定されたリクエストに対応するAPI実装を返す. 存在しない場合は<code>null</code>
     */
    public DConnectApi findApi(final Intent request) {
        String action = request.getAction();
        Method method = Method.fromAction(action);
        if (method == null) {
            return null;
        }
        String path = getApiPath(request);
        return findApi(path, method);
    }

    /**
     * 指定されたリクエストに対応するDevice Connect API実装を返す.
     *
     * @param path   リクエストされたAPIのパス
     * @param method リクエストされたAPIのメソッド
     * @return 指定されたリクエストに対応するAPI実装を返す. 存在しない場合は<code>null</code>
     */
    public DConnectApi findApi(final String path, final Method method) {
        return mApis.get(new ApiIdentifier(path, method));
    }

    /**
     * Device Connect API実装を追加する.
     *
     * @param api API 追加するAPI実装
     */
    public void addApi(final DConnectApi api) {
        mApis.put(new ApiIdentifier(getApiPath(api), api.getMethod()), api);
    }

    /**
     * Device Connect API実装を削除する.
     *
     * @param api 削除するAPI実装
     */
    public void removeApi(final DConnectApi api) {
        mApis.remove(new ApiIdentifier(getApiPath(api), api.getMethod()));
    }

    /**
     * 指定されたパスとメソッドが存在するか確認します.
     * @param path パス
     * @param method メソッド
     * @return 存在する場合はtrue、それ以外はfalse
     */
    public boolean hasApi(final String path, final Method method) {
        return findApi(path, method) != null;
    }

    /**
     * 指定されたDevice Connect APIへのパスを返す.
     *
     * @param api API実装
     * @return パス
     */
    private String getApiPath(final DConnectApi api) {
        return getApiPath(api.getInterface(), api.getAttribute());
    }

    /**
     * リクエストで指定されたパスを返す.
     *
     * @param request リクエスト
     * @return パス
     */
    private String getApiPath(final Intent request) {
        return getApiPath(getInterface(request), getAttribute(request));
    }

    /**
     * インターフェース名、アトリビュート名からパスを作成する.
     *
     * @param interfaceName インターフェース名
     * @param attributeName アトリビュート名
     * @return パス
     */
    private String getApiPath(final String interfaceName, final String attributeName) {
        StringBuilder path = new StringBuilder();
        path.append("/");
        if (interfaceName != null) {
            path.append(interfaceName);
            path.append("/");
        }
        if (attributeName != null) {
            path.append(attributeName);
        }
        return path.toString();
    }

    /**
     * 指定されたリクエストのパスがプロファイル仕様の中にあるか確認します.
     *
     * @param request リクエスト
     * @return プロファイル仕様がある場合はtrue、それ以外はfalse
     */
    private boolean isKnownPath(final Intent request) {
        DConnectService service = getService();
        if (service != null) {
            return service.getServiceSpec().findPathSpec(request) != null;
        }
        return false;
    }

    /**
     * 指定されたリクエストのHTTPメソッドがプロファイル仕様の中にあるか確認します.
     *
     * @param request リクエスト
     * @return プロファイル仕様がある場合はtrue、それ以外はfalse
     */
    private boolean isKnownMethod(final Intent request) {
        DConnectService service = getService();
        if (service != null) {
            return service.getServiceSpec().findOperationSpec(request) != null;
        }
        return false;
    }

    /**
     * プロファイル名を取得する.
     *
     * @return プロファイル名
     */
    public abstract String getProfileName();

    /**
     * リクエストのパラメータの妥当性を確認します.
     *
     * <p>
     * プロファイル定義が存在しない場合は、パラメータのチェックが
     * できないので妥当として true を返却します。
     * </p>
     *
     * @param request リクエスト
     * @return 妥当な場合はtrue、それ以外はfalse
     */
    private boolean validateRequest(final Intent request) {
        DConnectService service = getService();
        if (service != null) {
            DConnectServiceSpec spec = service.getServiceSpec();
            if (spec != null) {
                Swagger swagger = spec.findProfileSpec(getProfile(request));
                if (swagger != null) {
                    return OpenAPIValidator.validate(swagger, request);
                }
            }
        }
        return true;
    }
    /**
     * 不正なリクエストパラメータを返します.
     *
     * @param request リクエスト
     * @return 不正なリクエストパラメータ
     */
    private String getInvalidParameter(final Intent request) {
        DConnectService service = getService();
        StringBuilder builder = new StringBuilder();
        if (service != null) {
            DConnectServiceSpec spec = service.getServiceSpec();
            if (spec != null) {
                Swagger swagger = spec.findProfileSpec(getProfile(request));
                if (swagger != null) {
                    Bundle extras = request.getExtras();
                    if (extras == null) {
                        return "Unknown";
                    }
                    List<Parameter> parameters = OpenAPIValidator.findParameters(swagger, request);
                    for (Parameter parameter : parameters) {
                        Object value = extras.get(parameter.getName());
                        if (!OpenAPIValidator.validate(parameter, value)) {
                            builder.append(parameter.getName()).append(",");
                        }
                    }
                }
            }
        }
        return builder.toString().substring(0, builder.length() - 1);
    }
    /**
     * RESPONSEメソッドハンドラー.
     *
     * <p>
     * {@link DConnectApi#onRequest(Intent, Intent)}を実行する.
     * そうでない場合は、即座にエラーレスポンスを送信する.
     * </p>
     *
     * @param request  リクエストパラメータ
     * @param response レスポンスパラメータ
     * @return レスポンスパラメータを送信するか否か
     */
    public boolean onRequest(final Intent request, final Intent response) {
        DConnectApi api = findApi(request);
        if (api != null) {
            if (!validateRequest(request)) {
                String invalidDetails = getInvalidParameter(request);
                // API 定義ファイルでパラメータエラーとなった
                MessageUtils.setInvalidRequestParameterError(response, "Request parameters are invalid: " + invalidDetails);
                return true;
            }
            return api.onRequest(request, response);
        } else {
            if (isKnownPath(request)) {
                if (isKnownMethod(request)) {
                    MessageUtils.setNotSupportAttributeError(response);
                } else {
                    MessageUtils.setNotSupportActionError(response);
                }
            } else {
                MessageUtils.setUnknownAttributeError(response);
            }
            return true;
        }
    }

    /**
     * Local OAuth の有効・無効の状態を取得します.
     * <p>
     * プラグインに設定されていない場合には、false を返却します。
     * </p>
     * @return 有効になっている場合はtrue、それ以外はfalse
     */
    protected boolean isUseLocalOAuth() {
        if (mPluginContext == null) {
            // TODO 例外の方が良いか検討
            return false;
        }
        return mPluginContext.isUseLocalOAuth();
    }

    /**
     * 指定されたプロファイルが Local OAuth の判定に入るか確認します.
     * <p>
     * プラグインに設定されていない場合には、false を返却します。
     * </p>
     * @param profileName プロファイル名
     * @return Local OAuth の範囲に入る場合はtrue、それ以外はfalse
     */
    protected boolean isIgnoredProfile(final String profileName) {
        if (mPluginContext == null) {
            // TODO 例外の方が良いか検討
            return false;
        }
        return mPluginContext.isIgnoredProfile(profileName);
    }

    /**
     * コンテキストの設定する.
     *
     * @param context コンテキスト
     */
    public void setContext(final Context context) {
        mContext = context;
    }

    /**
     * コンテキストの取得する.
     *
     * @return コンテキスト
     */
    public Context getContext() {
        return mContext;
    }

    /**
     * プラグインコンテキストを設定します.
     *
     * @param pluginContext コンテキスト
     */
    public void setPluginContext(final DevicePluginContext pluginContext) {
        mPluginContext = pluginContext;
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
     * レスポンスを返却するクラスを設定します.
     *
     * @param responder レスポンス返却クラス
     */
    public void setResponder(final Responder responder) {
        mResponder = responder;
    }

    /**
     * 本プロファイル実装を提供するサービスを設定する.
     *
     * @param service サービス
     */
    public void setService(final DConnectService service) {
        mService = service;
    }

    /**
     * 本プロファイル実装を提供するサービスを取得する.
     *
     * @return サービス
     */
    public DConnectService getService() {
        return mService;
    }

    /**
     * DConnectApi からパスを作成します.
     *
     * @param api api
     * @return パス
     */
    private String createPath(final DConnectApi api) {
        String interfaceName = api.getInterface();
        String attributeName = api.getAttribute();
        StringBuffer path = new StringBuffer();
        path.append("/");
        if (interfaceName != null) {
            path.append(interfaceName);
            path.append("/");
        }
        if (attributeName != null) {
            path.append(attributeName);
        }
        return path.toString();
    }

    /**
     * 指定されたオブジェクトがStringか指定されたNumberクラスかを判定し、指定されたNumberクラスへ変換する.
     *
     * @param o     値
     * @param clazz 型情報
     * @param <T>   ナンバークラスの型。判定出来るのは {@link Byte}、{@link Short}、{@link Integer}、
     *              {@link Long}、{@link Float}、{@link Double} のみ。
     * @return 指定されたナンバークラスのオブジェクト。変換に失敗した場合はnullを返す。
     */
    @SuppressWarnings("unchecked")
    private static <T extends Number> Number valueOf(final Object o, final Class<T> clazz) {
        if (o == null) {
            return null;
        }

        Number result = null;

        if (o instanceof String) {
            try {
                if (Integer.class.equals(clazz)) {
                    result = Integer.valueOf((String) o);
                } else if (Long.class.equals(clazz)) {
                    result = Long.valueOf((String) o);
                } else if (Double.class.equals(clazz)) {
                    result = Double.valueOf((String) o);
                } else if (Byte.class.equals(clazz)) {
                    result = Byte.valueOf((String) o);
                } else if (Short.class.equals(clazz)) {
                    result = Short.valueOf((String) o);
                } else if (Float.class.equals(clazz)) {
                    result = Float.valueOf((String) o);
                }
            } catch (NumberFormatException e) {
                result = null;
            }
        } else if (o.getClass().equals(clazz)) {
            result = (T) o;
        }

        return result;
    }

    /**
     * 指定されたオブジェクトがStringかIntegerかを判定し、Integerへ変換する.
     *
     * @param o 値
     * @return 変換後の値。変換に失敗した場合はnullを返す。
     */
    public static Integer parseInteger(final Object o) {
        return (Integer) valueOf(o, Integer.class);
    }

    /**
     * Intentの指定されたパラメータがStringかIntegerかを判定し、Integerへ変換する.
     *
     * @param intent インテント
     * @param key    パラメータキー
     * @return 変換後の値。変換に失敗した場合、またはパラメータが無い場合はnullを返す。
     */
    public static Integer parseInteger(final Intent intent, final String key) {
        Bundle b = intent.getExtras();
        if (b == null) {
            return null;
        }
        return parseInteger(b.get(key));
    }

    /**
     * 指定されたオブジェクトがStringかLongかを判定し、Longへ変換する.
     *
     * @param o 値
     * @return 変換後の値。変換に失敗した場合はnullを返す。
     */
    public static Long parseLong(final Object o) {
        return (Long) valueOf(o, Long.class);
    }

    /**
     * Intentの指定されたパラメータがStringかLongかを判定し、Longへ変換する.
     *
     * @param intent インテント
     * @param key    パラメータキー
     * @return 変換後の値。変換に失敗した場合、またはパラメータが無い場合はnullを返す。
     */
    public static Long parseLong(final Intent intent, final String key) {
        Bundle b = intent.getExtras();
        if (b == null) {
            return null;
        }
        return parseLong(b.get(key));
    }

    /**
     * 指定されたオブジェクトがStringかDoubleかを判定し、Doubleへ変換する.
     *
     * @param o 値
     * @return 変換後の値。変換に失敗した場合はnullを返す。
     */
    public static Double parseDouble(final Object o) {
        return (Double) valueOf(o, Double.class);
    }

    /**
     * Intentの指定されたパラメータがStringかDoubleかを判定し、Doubleへ変換する.
     *
     * @param intent インテント
     * @param key    パラメータキー
     * @return 変換後の値。変換に失敗した場合、またはパラメータが無い場合はnullを返す。
     */
    public static Double parseDouble(final Intent intent, final String key) {
        Bundle b = intent.getExtras();
        if (b == null) {
            return null;
        }
        return parseDouble(b.get(key));
    }

    /**
     * 指定されたオブジェクトがStringかFloatかを判定し、Floatへ変換する.
     *
     * @param o 値
     * @return 変換後の値。変換に失敗した場合はnullを返す。
     */
    public static Float parseFloat(final Object o) {
        return (Float) valueOf(o, Float.class);
    }

    /**
     * Intentの指定されたパラメータがStringかFloatかを判定し、Floatへ変換する.
     *
     * @param intent インテント
     * @param key    パラメータキー
     * @return 変換後の値。変換に失敗した場合、またはパラメータが無い場合はnullを返す。
     */
    public static Float parseFloat(final Intent intent, final String key) {
        Bundle b = intent.getExtras();
        if (b == null) {
            return null;
        }
        return parseFloat(b.get(key));
    }

    /**
     * 指定されたオブジェクトがStringかByteかを判定し、Byteへ変換する.
     *
     * @param o 値
     * @return 変換後の値。変換に失敗した場合はnullを返す。
     */
    public static Byte parseByte(final Object o) {
        return (Byte) valueOf(o, Byte.class);
    }

    /**
     * Intentの指定されたパラメータがStringかByteかを判定し、Byteへ変換する.
     *
     * @param intent インテント
     * @param key    パラメータキー
     * @return 変換後の値。変換に失敗した場合、またはパラメータが無い場合はnullを返す。
     */
    public static Byte parseByte(final Intent intent, final String key) {
        Bundle b = intent.getExtras();
        if (b == null) {
            return null;
        }
        return parseByte(b.get(key));
    }

    /**
     * 指定されたオブジェクトがStringかShortかを判定し、Shortへ変換する.
     *
     * @param o 値
     * @return 変換後の値。変換に失敗した場合はnullを返す。
     */
    public static Short parseShort(final Object o) {
        return (Short) valueOf(o, Short.class);
    }

    /**
     * Intentの指定されたパラメータがStringかShortかを判定し、Shortへ変換する.
     *
     * @param intent インテント
     * @param key    パラメータキー
     * @return 変換後の値。変換に失敗した場合、またはパラメータが無い場合はnullを返す。
     */
    public static Short parseShort(final Intent intent, final String key) {
        Bundle b = intent.getExtras();
        if (b == null) {
            return null;
        }
        return parseShort(b.get(key));
    }

    /**
     * 指定されたオブジェクトがStringかBooleanかを判定し、Booleanへ変換する.
     * <p>
     * Stringの場合は、"true"の場合true、"false"の場合falseを返す。その他はnullを返す。
     * </p>
     *
     * @param o 値
     * @return 変換後の値。変換に失敗した場合はnullを返す。
     */
    public static Boolean parseBoolean(final Object o) {

        if (o instanceof String) {
            if (o.equals("true")) {
                return Boolean.TRUE;
            } else if (o.equals("false")) {
                return Boolean.FALSE;
            }
        } else if (o instanceof Boolean) {
            return (Boolean) o;
        }

        return null;
    }

    /**
     * Intentの指定されたパラメータがStringかBooleanかを判定し、Booleanへ変換する.
     * <p>
     * Stringの場合は、"true"の場合true、"false"の場合falseを返す。その他はnullを返す。
     * </p>
     *
     * @param intent インテント
     * @param key    キー
     * @return 変換後の値。変換に失敗した場合、またはパラメータが無い場合はnullを返す。
     */
    public static Boolean parseBoolean(final Intent intent, final String key) {
        Bundle b = intent.getExtras();
        if (b == null) {
            return null;
        }
        return parseBoolean(b.get(key));
    }

    /**
     * リクエストからサービスIDを取得する.
     *
     * @param request リクエストパラメータ
     * @return サービスID。無い場合はnullを返す。
     */
    public static String getServiceID(final Intent request) {
        return request.getStringExtra(PARAM_SERVICE_ID);
    }

    /**
     * メッセージにサービスIDを設定する.
     *
     * @param message   メッセージパラメータ
     * @param serviceId サービスID
     */
    public static void setServiceID(final Intent message, final String serviceId) {
        message.putExtra(PARAM_SERVICE_ID, serviceId);
    }

    /**
     * リクエストからAPI名を取得する.
     *
     * @param request リクエストパラメータ
     * @return API名。無い場合はnullを返す。
     */
    public static String getApi(final Intent request) {
        return request.getStringExtra(DConnectMessage.EXTRA_API);
    }

    /**
     * メッセージにAPI名を設定する.
     *
     * @param message メッセージパラメータ
     * @param api     API名
     */
    public static void setApi(final Intent message, final String api) {
        message.putExtra(DConnectMessage.EXTRA_API, api);
    }

    /**
     * リクエストからプロファイル名を取得する.
     *
     * @param request リクエストパラメータ
     * @return プロファイル名。無い場合はnullを返す。
     */
    public static String getProfile(final Intent request) {
        return request.getExtras().getString(DConnectMessage.EXTRA_PROFILE);
    }

    /**
     * メッセージにプロファイル名を設定する.
     *
     * @param message メッセージパラメータ
     * @param profile プロファイル名
     */
    public static void setProfile(final Intent message, final String profile) {
        message.putExtra(DConnectMessage.EXTRA_PROFILE, profile);
    }

    /**
     * リクエストからインターフェース名を取得する.
     *
     * @param request リクエストパラメータsetProfile
     * @return インターフェース。無い場合はnullを返す。
     */
    public static String getInterface(final Intent request) {
        return request.getExtras().getString(DConnectMessage.EXTRA_INTERFACE);
    }

    /**
     * メッセージにインターフェース名を設定する.
     *
     * @param message メッセージパラメータ
     * @param inter   インターフェース名
     */
    public static void setInterface(final Intent message, final String inter) {
        message.putExtra(DConnectMessage.EXTRA_INTERFACE, inter);
    }

    /**
     * リクエストから属性名を取得する.
     *
     * @param request リクエストパラメータ
     * @return 属性名。無い場合はnullを返す。
     */
    public static String getAttribute(final Intent request) {
        return request.getExtras().getString(DConnectMessage.EXTRA_ATTRIBUTE);
    }

    /**
     * メッセージに属性名を設定する.
     *
     * @param message   メッセージパラメータ
     * @param attribute コールバック名
     */
    public static void setAttribute(final Intent message, final String attribute) {
        message.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, attribute);
    }

    /**
     * レスポンス結果を設定する.
     *
     * @param response レスポンスパラメータ
     * @param result   レスポンス結果
     */
    public static void setResult(final Intent response, final int result) {
        response.putExtra(DConnectMessage.EXTRA_RESULT, result);
    }

    /**
     * レスポンス結果を取得する.
     *
     * @param response レスポンスパラメータ
     * @return レスポンス結果
     */
    public static int getResult(final Intent response) {
        return response.getIntExtra(DConnectMessage.EXTRA_RESULT, -1);
    }

    /**
     * リクエストからオリジンを取得する.
     *
     * @param request リクエストパラメータ
     * @return オリジン。無い場合はnullを返す。
     */
    public static String getOrigin(final Intent request) {
        return request.getStringExtra(IntentDConnectMessage.EXTRA_ORIGIN);
    }

    /**
     * リクエストからセッションキーを取得する.
     *
     * @param request リクエストパラメータ
     * @return セッションキー。無い場合はnullを返す。
     * @see {@link #getOrigin(Intent)}
     * @deprecated GotAPI 1.1では、セッションキーではなくオリジンごとにイベントを管理
     */
    public static String getSessionKey(final Intent request) {
        return request.getStringExtra(PARAM_SESSION_KEY);
    }

    /**
     * メッセージにセッションキーを設定する.
     *
     * @param message    メッセージパラメータ
     * @param sessionKey セッションキー
     * @deprecated GotAPI 1.1では、セッションキーではなくオリジンごとにイベントを管理
     */
    public static void setSessionKey(final Intent message, final String sessionKey) {
        message.putExtra(PARAM_SESSION_KEY, sessionKey);
    }

    /**
     * リクエストからアクセストークンを取得する.
     *
     * @param request リクエストパラメータ
     * @return アクセストークン。無い場合はnullを返す。
     */
    public static String getAccessToken(final Intent request) {
        return request.getStringExtra(DConnectMessage.EXTRA_ACCESS_TOKEN);
    }

    /**
     * メッセージにアクセストークンを設定する.
     *
     * @param message     メッセージパラメータ
     * @param accessToken アクセストークン
     */
    public static void setAccessToken(final Intent message, final String accessToken) {
        message.putExtra(DConnectMessage.EXTRA_ACCESS_TOKEN, accessToken);
    }

    /**
     * リクエストからDeviceConnectManagerのバージョン名を取得する.
     *
     * @param request リクエストパラメータ
     * @return DeviceConnectManagerのバージョン名。無い場合はnullを返す。
     */
    public static String getVersion(final Intent request) {
        return request.getStringExtra(DConnectMessage.EXTRA_VERSION);
    }

    /**
     * リクエストからDeviceConnectManagerのバージョン名を設定する.
     *
     * @param message メッセージパラメータ
     * @param version DeviceConnectManagerのバージョン名
     */
    public static void setVersion(final Intent message, final String version) {
        message.putExtra(DConnectMessage.EXTRA_VERSION, version);
    }

    /**
     * リクエストからDeviceConnectManagerのアプリ名を取得する.
     *
     * @param request リクエストパラメータ
     * @return DeviceConnectManagerのアプリ名。無い場合はnullを返す。
     */
    public static String getProduct(final Intent request) {
        return request.getStringExtra(DConnectMessage.EXTRA_PRODUCT);
    }

    /**
     * リクエストからDeviceConnectManagerのアプリ名を設定する.
     *
     * @param message メッセージパラメータ
     * @param product DeviceConnectManagerのアプリ名
     */
    public static void setProduct(final Intent message, final String product) {
        message.putExtra(DConnectMessage.EXTRA_PRODUCT, product);
    }

    /**
     * レスポンスの結果として非サポートエラーを設定する.
     *
     * @param response レスポンスパラメータ
     */
    public static void setUnsupportedError(final Intent response) {
        MessageUtils.setNotSupportAttributeError(response);
    }

    /**
     * レスポンスにリクエストコードを設定する.
     *
     * @param response    レスポンスパラメータ
     * @param requestCode リクエストコード
     */
    public static void setRequestCode(final Intent response, final int requestCode) {
        response.putExtra(DConnectMessage.EXTRA_REQUEST_CODE, requestCode);
    }

    /**
     * リクエストからリクエストコードを取得する.
     *
     * @param request リクエストパラメータ
     * @return リクエストコード
     */
    public static int getRequestCode(final Intent request) {
        return request.getIntExtra(DConnectMessage.EXTRA_REQUEST_CODE, Integer.MIN_VALUE);
    }

    /**
     * レスポンスを返却します.
     *
     * @param response レスポンス
     */
    protected final boolean sendResponse(final Intent response) {
        return mResponder != null && mResponder.sendResponse(response);
    }

    /**
     * イベントを送信します.
     *
     * @param event       イベント
     * @param accessToken アクセストークン
     * @return 送信成功の場合true、アクセストークンエラーの場合はfalseを返す。
     */
    protected final boolean sendEvent(final Intent event, final String accessToken) {
        return mResponder != null && mResponder.sendEvent(event, accessToken);
    }

    /**
     * イベントを送信します.
     *
     * @param event  イベント
     * @param bundle パラメータ
     * @return 送信成功の場合true、アクセストークンエラーの場合はfalseを返す。
     */
    protected final boolean sendEvent(final Event event, final Bundle bundle) {
        return mResponder != null && mResponder.sendEvent(event, bundle);
    }

    /**
     * 大文字・小文字を無視して、文字列を比較します.
     *
     * @param s1 比較する文字列1
     * @param s2 比較する文字列2
     * @return 同じ場合はtrue、それ以外はfalse
     */
    protected boolean isEqual(final String s1, final String s2) {
        if (s1 == null && s2 == null) {
            return true;
        }
        if (s1 != null) {
            return s1.equalsIgnoreCase(s2);
        } else {
            return s2.equalsIgnoreCase(s1);
        }
    }

    /**
     * コンテンツデータを取得する.
     *
     * @param uri URI
     * @return コンテンツデータ
     */
    protected final byte[] getContentData(final String uri) {
        if (uri == null) {
            return null;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream in = null;
        byte[] buf = new byte[BUF_SIZE];
        int len;
        try {
            ContentResolver r = getContext().getContentResolver();
            in = r.openInputStream(Uri.parse(uri));
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            return out.toByteArray();
        } catch (OutOfMemoryError e) {
            return null;
        } catch (IOException e) {
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 指定されたURIからデータを取得します.
     * <p>
     * 取得に失敗した場合には null を返却します。
     * </p>
     * @param uri URI
     * @return データ
     * @throws OutOfMemoryError メモリ不足の場合に発生
     */
    protected byte[] getData(final String uri) throws OutOfMemoryError {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        byte[] data = null;
        try {
            URL url = new URL(uri);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            inputStream = connection.getInputStream();
            data = readAll(inputStream);
        } catch (IOException e) {
            // ignore.
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                // ignore.
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return data;
    }

    /**
     * ストリームのデータを全て読み出します.
     *
     * @param inputStream ストリーム
     * @return データ
     * @throws IOException ストリームからデータの読み込み失敗した場合に発生
     */
    private byte[] readAll(InputStream inputStream) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while (true) {
            int len = inputStream.read(buffer);
            if (len < 0) {
                break;
            }
            bout.write(buffer, 0, len);
        }
        return bout.toByteArray();
    }

    /**
     * レスポンスを返却するためのクラス.
     */
    public interface Responder {
        /**
         * Device Connect Managerにレスポンスを返却するためのメソッド.
         * @param response レスポンス
         * @return 送信成功の場合true、それ以外はfalse
         */
        boolean sendResponse(final Intent response);

        /**
         * Device Connectにイベントを送信する.
         *
         * @param event イベントパラメータ
         * @param accessToken 送り先のアクセストークン
         * @return 送信成功の場合true、アクセストークンエラーの場合はfalseを返す。
         */
        boolean sendEvent(final Intent event, final String accessToken);

        /**
         * Device Connectにイベントを送信する.
         *
         * @param event イベントパラメータ
         * @param bundle パラメータ
         * @return 送信成功の場合true、アクセストークンエラーの場合はfalseを返す。
         */
        boolean sendEvent(final Event event, final Bundle bundle);
    }

    private static class ApiIdentifier {
        /**
         * Device Connect Method.
         */
        private final Method mMethod;

        /**
         * パス.
         */
        private final String mPath;

        ApiIdentifier(final String path, final Method method) {
            if (path == null) {
                throw new IllegalArgumentException("path is null.");
            }
            if (method == null) {
                throw new IllegalArgumentException("method is null.");
            }
            mPath = path;
            mMethod = method;
        }

        @Override
        public int hashCode() {
            int result = mPath.toLowerCase().hashCode(); // XXXX パスの大文字小文字を無視
            result = 31 * result + mMethod.hashCode();
            return result;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ApiIdentifier)) {
                return false;
            }
            ApiIdentifier that = ((ApiIdentifier) o);
            // XXXX パスの大文字小文字を無視
            return mPath.equalsIgnoreCase(that.mPath) && mMethod == that.mMethod;
        }
    }
}
