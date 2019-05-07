/*
 DConnectServiceSpec.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;

import org.deviceconnect.android.localoauth.DevicePluginXml;
import org.deviceconnect.android.localoauth.DevicePluginXmlUtil;
import org.deviceconnect.android.message.DevicePluginContext;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.spec.models.Method;
import org.deviceconnect.android.profile.spec.models.Operation;
import org.deviceconnect.android.profile.spec.models.Path;
import org.deviceconnect.android.profile.spec.models.Swagger;
import org.deviceconnect.android.profile.spec.parser.OpenAPIParser;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.android.service.DConnectServiceProvider;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * サービスのサポートする仕様を保持するクラス.
 *
 * <p>
 * このクラスで保持している仕様定義が Service Information で返却される情報になります。
 * </p>
 *
 * <p>
 * {@link DConnectServiceProvider#addService(DConnectService)} した時に、{@link DConnectService}
 * に {@link DConnectServiceSpec} が設定されていない場合には、登録されているプロファイルの仕様定義を assets
 * から読み込み設定を行います。
 * </p>
 *
 * <p>
 * {@link DConnectServiceSpec} のインスタンスを作成して、仕様定義を追加することもできます。
 * </p>
 *
 * <p>以下のサンプルでは、GET /gotapi/echo のパラメータにexampleを追加しています。</p>
 * <pre>{@code
 * DConnectServiceSpec spec = new DConnectServiceSpec(getPluginContext());
 * try {
 *     spec.addProfileSpec("echo");
 * } catch (Exception e) {
 *     // 読み込み失敗
 * }
 *
 * Operation operation = spec.findOperationSpec(Method.GET, "/gotapi/echo");
 * if (operation != null) {
 *     QueryParameter parameter = new QueryParameter();
 *     parameter.setName("example");
 *     parameter.setType(DataType.STRING);
 *     parameter.setRequired(false);
 *     operation.addParameter(parameter);
 * }
 *
 * DConnectService service = new DConnectService("my_service_id");
 * service.setName("MyPlugin Service");
 * service.setOnline(true);
 * service.setNetworkType(NetworkType.UNKNOWN);
 * service.addProfile(new MyEchoProfile());
 * service.setServiceSpec(spec);
 * getServiceProvider().addService(service);
 * }{/pre}
 *
 * @author NTT DOCOMO, INC.
 */
public class DConnectServiceSpec {
    /**
     * 各プロファイルの定義ファイルを保持するマップ.
     *
     * <p>
     * マップのキーは、lowerCase で格納します。
     * </p>
     */
    private final Map<String, Swagger> mProfileSpecs = new HashMap<>();

    /**
     * プラグインのコンテキスト.
     */
    private DevicePluginContext mPluginContext;

    /**
     * コンストラクタ.
     *
     * @param pluginContext プラグインコンテキスト
     */
    public DConnectServiceSpec(DevicePluginContext pluginContext) {
        if (pluginContext == null) {
            throw new NullPointerException("pluginContext is null");
        }
        mPluginContext = pluginContext;
    }

    /**
     * サービスがサポートするプロファイルの仕様定義の一覧を取得する.
     *
     * <p>
     * このメソッドから返される一覧には、各プロファイル上で定義されているすべてのAPIの定義が含まれる.
     * </p>
     *
     * @return {@link Swagger}のマップ. キーはプロファイル名.
     */
    public Map<String, Swagger> getProfileSpecs() {
        synchronized (mProfileSpecs) {
            return new HashMap<>(mProfileSpecs);
        }
    }

    /**
     * 指定されたプロファイル定義を assets から検索して追加します.
     *
     * <p>
     * プロファイル定義ファイルが見つからない場合やプロファイル定義ファイルのフォーマットが不正な場合には、例外が発生します。
     * </p>
     *
     * <p>
     * プロファイル定義ファイルは、以下の順に検索しプロファイル名と同じ定義ファイルを読み込みます。
     * <ul>
     *     <li>/assets/{spec-path}/api</li>
     *     <li>/assets/api</li>
     * </ul>
     *
     * /assets/{パス}/api は、AndroidManifest.xml の meta-data に定義されている xml から取得します。<br>
     *
     * この xml に記載されている deviceplugin-provider タグの spec-path アトリビュートが検索先のパスになります。<br>
     *
     * <pre>
     * {@literal <deviceplugin-provider spec-path="{spec-path}/api">}
     *        ・・・省略・・・
     * {@literal </deviceplugin-provider>}
     * </pre>
     * </p>
     *
     * @param profileName プロファイル名
     * @throws IOException 入力ストリームの読み込みに失敗した場合
     * @throws JSONException プロファイル定義のJSON構造が不正な場合
     */
    public void addProfileSpec(final String profileName) throws IOException, JSONException {
        addProfileSpec(profileName, openApiSpec(profileName));
    }

    /**
     * JSON の文字列からプロファイル定義を追加します.
     *
     * @param profileName プロファイル名
     * @param jsonString プロファイル定義のJSON
     * @throws JSONException プロファイル定義のJSON構造が不正な場合
     */
    public void addProfileSpec(final String profileName, final String jsonString) throws JSONException {
        synchronized (mProfileSpecs) {
            mProfileSpecs.put(profileName.toLowerCase(), OpenAPIParser.parse(jsonString));
        }
    }

    /**
     * 入力ストリームからプロファイル定義を追加する.
     *
     * @param profileName プロファイル名
     * @param in 入力ストリーム
     * @throws IOException 入力ストリームの読み込みに失敗した場合
     * @throws JSONException プロファイル定義のJSON構造が不正な場合
     */
    private void addProfileSpec(final String profileName, final InputStream in) throws IOException, JSONException {
        addProfileSpec(profileName, loadFile(in));
    }

    /**
     * 指定された名前のプロファイル定義を削除します.
     *
     * <p>
     * 削除するプロファイル定義がない場合には null を返却します。
     * </p>
     *
     * @param profileName 削除するプロファイル名
     * @return 削除されたプロファイル定義
     */
    public Swagger removeProfileSpec(final String profileName) {
        synchronized (mProfileSpecs) {
            return mProfileSpecs.remove(profileName.toLowerCase());
        }
    }

    /**
     * 指定したプロファイルの仕様定義を取得する.
     *
     * <p>
     * 指定されたプロファイル名の定義が見つからない場合には null を返却します。
     * </p>
     *
     * <p>
     * ここで取得したインスタンスに値を設定すると Service Information
     * で返却されるパラメータが変更されます。
     * </p>
     *
     * @param profileName プロファイル名
     * @return プロファイルの仕様定義が格納された{@link Swagger}のインスタンス
     */
    public Swagger findProfileSpec(final String profileName) {
        if (profileName == null) {
            return null;
        }
        synchronized (mProfileSpecs) {
            return mProfileSpecs.get(profileName.toLowerCase());
        }
    }

    /**
     * リクエストで指定された API 定義から Path を取得します.
     *
     * <p>
     * リクエストで指定されたパスに一致する Path が存在しない場合には null を返却します。
     * </p>
     *
     * @param path パス
     * @return {@link Path}のインスタンス
     */
    public Path findPathSpec(String path) {
        Swagger swagger = findProfileSpec(findProfileFromPath(path));
        if (swagger != null) {
            return findPathSpec(swagger, path);
        }
        return null;
    }

    /**
     * リクエストで指定された API 定義から Path を取得します.
     *
     * <p>
     * リクエストで指定されたパスに一致する Path が存在しない場合には null を返却します。
     * </p>
     *
     * @param request リクエスト
     * @return {@link Path}のインスタンス
     */
    public Path findPathSpec(Intent request) {
        if (request == null) {
            return null;
        }
        Swagger swagger = findProfileSpec(DConnectProfile.getProfile(request));
        if (swagger != null) {
            return findPathSpec(swagger, request);
        }
        return null;
    }

    /**
     * 指定されたパスとHTTPメソッドから Operation を取得します.
     *
     * <p>
     * 指定されたパスとHTTPメソッドに一致する Operation が存在しない場合には null を返却します。
     * </p>
     *
     * @param method HTTPメソッド
     * @param path パス
     * @return {@link Operation}のインスタンス
     */
    public Operation findOperationSpec(Method method, String path) {
        Swagger swagger = findProfileSpec(findProfileFromPath(path));
        if (swagger != null) {
            return findOperationSpec(swagger, method, path);
        }
        return null;
    }

    /**
     * 指定されたプロファイル名、アトリビュート名とHTTPメソッドから Operation を取得します.
     *
     * <p>
     * 指定されたプロファイル名、アトリビュート名とHTTPメソッドに一致する Operation が存在しない場合には null を返却します。
     * </p>
     *
     * @param method HTTPメソッド
     * @param profileName プロファイル名
     * @param attributeName プロファイル名
     * @return {@link Operation}のインスタンス
     */
    public Operation findOperationSpec(Method method, String profileName, String attributeName) {
        return findOperationSpec(method, "gotapi", profileName, null, attributeName);
    }

    /**
     * 指定された引数から Operation を取得します.
     *
     * <p>
     * 指定された引数に一致する Operation が存在しない場合には null を返却します。
     * </p>
     *
     * @param method HTTPメソッド
     * @param profileName プロファイル名
     * @param interfaceName プロファイル名
     * @param attributeName プロファイル名
     * @return {@link Operation}のインスタンス
     */
    public Operation findOperationSpec(Method method, String profileName, String interfaceName, String attributeName) {
        return findOperationSpec(method, "gotapi", profileName, interfaceName, attributeName);
    }

    /**
     * リクエストで指定された API 定義から Operation を取得します.
     *
     * <p>
     * リクエストで指定されたパスに一致する Operation が存在しない場合には null を返却します。
     * </p>
     *
     * @param method HTTPメソッド
     * @param apiName API名(gotapi)
     * @param profileName プロファイル名
     * @param interfaceName プロファイル名
     * @param attributeName プロファイル名
     * @return {@link Operation}のインスタンス
     */
    public Operation findOperationSpec(Method method, String apiName, String profileName, String interfaceName, String attributeName) {
        Swagger swagger = findProfileSpec(findProfileFromPath(profileName));
        if (swagger != null) {
            return findOperationSpec(swagger, method, createPath(apiName, profileName, interfaceName, attributeName));
        }
        return null;
    }

    /**
     * パスの中からプロファイル名を取得します.
     *
     * @param path パス
     * @return プロファイル名
     */
    private String findProfileFromPath(String path) {
        if (path == null) {
            return null;
        }
        String[] p = path.split("/");
        if (p.length > 2) {
            return p[2];
        }
        return null;
    }

    /**
     * リクエストで指定された API 定義から Operation を取得します.
     *
     * <p>
     * リクエストで指定されたパスに一致する Operation が存在しない場合には null を返却します。
     * </p>
     *
     * @param request リクエスト
     * @return {@link Operation}のインスタンス
     */
    public Operation findOperationSpec(Intent request) {
        if (request == null) {
            return null;
        }
        Swagger swagger = findProfileSpec(DConnectProfile.getProfile(request));
        if (swagger != null) {
            return findOperationSpec(swagger, request);
        }
        return null;
    }

    /**
     * 指定されたパスから Path を取得します.
     *
     * <p>
     * パスに一致する Path が存在しない場合には null を返却します。
     * </p>
     *
     * @param swagger API 定義
     * @param path リクエスト
     * @return {@link Path}のインスタンス
     */
    static Path findPathSpec(Swagger swagger, String path) {
        for (String key : swagger.getPaths().getKeySet()) {
            String path1 = createPath(swagger, key);
            if (path1.equalsIgnoreCase(path)) {
                return swagger.getPaths().getPath(key);
            }
        }
        return null;
    }

    /**
     * リクエストで指定された API 定義から Path を取得します.
     *
     * <p>
     * リクエストで指定されたパスに一致する Path が存在しない場合には null を返却します。
     * </p>
     *
     * @param swagger API 定義
     * @param request リクエスト
     * @return {@link Path}のインスタンス
     */
    static Path findPathSpec(Swagger swagger, Intent request) {
        return findPathSpec(swagger, createPath(request));
    }

    /**
     * リクエストで指定された API 定義から Operation を取得します.
     *
     * <p>
     * リクエストで指定されたパスに一致する Operation が存在しない場合には null を返却します。
     * </p>
     *
     * @param swagger API 定義
     * @param method HTTPメソッド
     * @param path パス
     * @return Operation
     */
    static Operation findOperationSpec(Swagger swagger, Method method, String path) {
        if (swagger == null || method == null || path == null) {
            return null;
        }

        if (path.endsWith("/")) {
            // 最後に / が付いている場合は削除
            path = path.substring(0, path.length() - 1);
        }

        for (String key : swagger.getPaths().getKeySet()) {
            String path1 = createPath(swagger, key);
            if (path1.equalsIgnoreCase(path)) {
                Path p = swagger.getPaths().getPath(key);
                if (p != null) {
                    return p.getOperation(method);
                }
            }
        }
        return null;
    }

    /**
     * リクエストで指定された API 定義から Operation を取得します.
     *
     * <p>
     * リクエストで指定されたパスに一致する Operation が存在しない場合には null を返却します。
     * </p>
     *
     * @param swagger API 定義
     * @param request リクエスト
     * @return Operation
     */
    static Operation findOperationSpec(Swagger swagger, Intent request) {
        return findOperationSpec(swagger, Method.fromAction(request.getAction()), createPath(request));
    }

    /**
     * コンテキストを取得します.
     *
     * @return コンテキスト
     */
    private Context getContext() {
        return mPluginContext.getContext();
    }

    /**
     * 定義ファイルからパスを作成します.
     *
     * @param swagger 定義ファイル
     * @param path パス
     * @return パス
     */
    private static String createPath(Swagger swagger, String path) {
        if (path != null && path.endsWith("/")) {
            // 最後に / が付いている場合は削除
            path = path.substring(0, path.length() - 1);
        }

        String basePath = swagger.getBasePath();
        if (basePath != null) {
            return basePath + path;
        } else {
            return path;
        }
    }

    /**
     * リクエストからパスを作成します.
     *
     * @param request リクエスト
     * @return パス
     */
    private static String createPath(Intent request) {
        String apiName = DConnectProfile.getApi(request);
        String profileName = DConnectProfile.getProfile(request);
        String interfaceName = DConnectProfile.getInterface(request);
        String attributeName = DConnectProfile.getAttribute(request);
        return createPath(apiName, profileName, interfaceName, attributeName);
    }

    /**
     * パスを作成します.
     *
     * @param apiName api名
     * @param profileName プロファイル名
     * @param interfaceName インターフェース名
     * @param attributeName アトリビュート名
     * @return パス
     */
    private static String createPath(String apiName, String profileName, String interfaceName , String attributeName) {
        StringBuilder path = new StringBuilder();

        if (apiName != null) {
            path.append("/").append(apiName);
        }

        if (profileName != null) {
            path.append("/").append(profileName);
        }

        if (interfaceName != null) {
            path.append("/").append(interfaceName);
        }

        if (attributeName != null) {
            path.append("/").append(attributeName);
        }

        return path.toString();
    }

    /**
     * ストリームから文字列を読み込みます．
     *
     * @param in ストリーム
     * @return 文字列
     * @throws IOException ストリームの読み込みに失敗した場合に発生
     */
    private static String loadFile(final InputStream in) throws IOException {
        try {
            byte[] buf = new byte[4096];
            int len;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            return new String(out.toByteArray());
        } finally {
            in.close();
        }
    }

    /**
     * ファイルパスから拡張子を削除します.
     *
     * @param filePath ファイルパス
     * @return 拡張子を削除したファイルパス
     */
    private static String removeExtension(String filePath) {
        return filePath.substring(0, filePath.lastIndexOf('.'));
    }

    /**
     * プロファイル仕様の定義ファイルへのパスを取得します.
     *
     * <p>
     * プロファイル使用の定義ファイルが見つからない場合は、null を返却します。
     * </p>
     *
     * @param fileList ファイル一覧
     * @param profileName ファイル名
     * @return 定義ファイルへのパス
     */
    private static String findProfileSpecName(final String[] fileList, final String profileName) {
        if (fileList == null) {
            return null;
        }

        for (String filePath : fileList) {
            if (!filePath.endsWith(".json")) {
                // JSON 以外の拡張子は無視
                continue;
            }

            String fileName = removeExtension(filePath);
            if (fileName.equalsIgnoreCase(profileName)) {
                return filePath;
            }
        }
        return null;
    }

    /**
     * API 定義ファイルが格納されるフォルダの候補リストを取得します.
     *
     * <p>
     * 以下のようなリストが返却されます。
     *   - /assets/パッケージ名/api
     *   - /assets/api
     * </p>
     *
     * <p>
     * MEMO: 必要に応じて、パスを追加すること。
     * </p>
     *
     * @return API 定義ファイルが格納されるフォルダの候補リスト
     */
    private List<String> getApiPath() {
        List<String> dirList = new ArrayList<>();
        DevicePluginXml xml = DevicePluginXmlUtil.getXml(getContext(), mPluginContext.getPluginXmlResId());
        if (xml != null && xml.getSpecPath() != null) {
            dirList.add(xml.getSpecPath());
        }
        dirList.add("api");
        return dirList;
    }

    /**
     * プロファイルの API 定義ファイルを検索します.
     *
     * <p>
     * プロファイルの API 定義ファイルへのパスが見つからない場合には例外が発生します。
     * </p>
     *
     * @param profileName プロファイル名
     * @return API定義ファイルへのパス
     * @throws IOException assetsへのアクセスに失敗した場合
     * @throws FileNotFoundException プロファイルの API 定義ファイルが見つからない場合
     */
    private String findApiPath(String profileName) throws IOException {
        AssetManager assets = getContext().getAssets();
        for (String dir : getApiPath()) {
            String[] fileNames = assets.list(dir);
            String fileName = findProfileSpecName(fileNames, profileName);
            if (fileName != null) {
                return dir + "/" + fileName;
            }
        }
        throw new FileNotFoundException(profileName + " is not found.");
    }

    /**
     * プロファイルの API 定義ファイルのストリームを開きます.
     *
     * <p>
     * ストリームを閉じる処理は、メソッドを呼び出した側で行うこと。
     * </p>
     *
     * @param profileName プロファイル名
     * @return API定義ファイルへのストリーム
     * @throws IOException assetsへのアクセスに失敗した場合
     * @throws FileNotFoundException ファイルが見つからない場合
     */
    private InputStream openApiSpec(String profileName)throws IOException {
        return getContext().getAssets().open(findApiPath(profileName));
    }
}
