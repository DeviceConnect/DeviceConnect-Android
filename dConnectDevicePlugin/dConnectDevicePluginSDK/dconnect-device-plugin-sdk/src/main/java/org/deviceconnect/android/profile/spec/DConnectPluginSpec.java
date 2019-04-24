/*
 DConnectPluginSpec.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec;

import android.content.Context;
import android.content.res.AssetManager;

import org.deviceconnect.android.localoauth.DevicePluginXml;
import org.deviceconnect.android.localoauth.DevicePluginXmlUtil;
import org.deviceconnect.android.message.DevicePluginContext;
import org.deviceconnect.android.profile.spec.models.Swagger;
import org.deviceconnect.android.profile.spec.parser.OpenAPIParser;
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
 * プラグインのサポートする仕様を保持するクラス.
 *
 * <p>
 * プラグインのサポートするプロファイルのリストを持ちます.
 * </p>
 *
 * @author NTT DOCOMO, INC.
 */
public class DConnectPluginSpec {

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
    public DConnectPluginSpec(DevicePluginContext pluginContext) {
        if (pluginContext == null) {
            throw new NullPointerException("pluginContext is null");
        }
        mPluginContext = pluginContext;
    }

    /**
     * 指定されたプロファイル定義を assets から検索して追加します.
     *
     * <p>
     * プロファイル定義ファイルが見つからない場合には、例外が発生します。
     * </p>
     *
     * <p>
     * プロファイル定義ファイルは、以下の順に検索しプロファイル名と同じ定義ファイルを読み込みます。
     * <ul>
     *     <li>/assets/{パス}/api</li>
     *     <li>/assets/api</li>
     * </ul>
     *
     * /assets/{パス}/api は、AndroidManifest.xml の meta-data に定義されている xml から取得します。
     *
     * xml に記載されている deviceplugin-provider タグの spec-path アトリビュートが検索先のパスになります。
     *
     * <pre>
     * &lt;deviceplugin-provider spec-path="{パス}/api"&gt;
     *        ・・・省略・・・
     * &lt;/deviceplugin-provider&gt;
     * </pre>
     * </p>
     *
     * @param profileName プロファイル名
     * @throws IOException 入力ストリームの読み込みに失敗した場合
     * @throws JSONException JSONの構造が不正な場合
     */
    public void addProfileSpec(final String profileName) throws IOException, JSONException {
        addProfileSpec(profileName, openApiSpec(profileName));
    }

    /**
     * 入力ストリームからプロファイル定義を追加する.
     *
     * @param profileName プロファイル名
     * @param in 入力ストリーム
     * @throws IOException 入力ストリームの読み込みに失敗した場合
     * @throws JSONException JSONの構造が不正な場合
     */
    private void addProfileSpec(final String profileName, final InputStream in) throws IOException, JSONException {
        mProfileSpecs.put(profileName.toLowerCase(), OpenAPIParser.parse(loadFile(in)));
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
        return mProfileSpecs.remove(profileName.toLowerCase());
    }

    /**
     * 指定したプロファイルの仕様定義を取得する.
     *
     * <p>
     * プロファイルの定義は、DConnectService を追加する時に読み込まれます。
     * </p>
     *
     * <p>
     * ここで取得したインスタンスに値を設定すると Service Information
     * で返却されるパラメータが変更されます。
     * </p>
     *
     * @param profileName プロファイル名
     * @return {@link Swagger}のインスタンス
     */
    public Swagger findProfileSpec(final String profileName) {
        if (profileName == null) {
            return null;
        }
        return mProfileSpecs.get(profileName.toLowerCase());
    }

    /**
     * プラグインのサポートするプロファイルの仕様定義の一覧を取得する.
     *
     * <p>
     * このメソッドから返される一覧には、各プロファイル上で定義されているすべてのAPIの定義が含まれる.
     * </p>
     *
     * @return {@link Swagger}のマップ. キーはプロファイル名.
     */
    public Map<String, Swagger> getProfileSpecs() {
        return new HashMap<>(mProfileSpecs);
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
