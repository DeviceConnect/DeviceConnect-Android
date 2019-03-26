package org.deviceconnect.android.manager.util;

import android.content.Context;
import android.content.res.AssetManager;

import org.deviceconnect.android.manager.core.BuildConfig;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.spec.DConnectProfileSpec;
import org.deviceconnect.android.profile.spec.parser.DConnectProfileSpecJsonParser;
import org.deviceconnect.android.profile.spec.parser.DConnectProfileSpecJsonParserFactory;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Logger;

public final class DConnectProfileUtil {
    /**
     * ロガー.
     */
    private static final Logger mLogger = Logger.getLogger("dconnect.manager");

    /**
     * プロファイル仕様定義ファイルの拡張子.
     */
    private static final String SPEC_FILE_EXTENSION = ".json";

    private DConnectProfileUtil() {}

    /**
     * 指定されたプロファイルのインスタンスにspecのデータを読み込みます.
     * @param context コンテキスト
     * @param profileMap プロファイル一覧
     */
    public static void loadProfileSpecs(final Context context, final Map<String, DConnectProfile> profileMap) {
        for (DConnectProfile profile : profileMap.values()) {
            final String profileName = profile.getProfileName();
            try {
                profile.setProfileSpec(loadProfileSpec(context, profileName));
                if (BuildConfig.DEBUG) {
                    mLogger.info("Loaded a profile spec: " + profileName);
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to load a profile spec: " + profileName, e);
            } catch (JSONException e) {
                throw new RuntimeException("Failed to load a profile spec: " + profileName, e);
            }
        }
    }

    /**
     * 指定されたプロファイル名のファイルから DConnectProfileSpec を取得します.
     *
     * @param context コンテキスト
     * @param profileName プロファイル名
     * @return DConnectProfileSpec のインスタンス
     * @throws IOException 読み込み失敗した場合に発生
     * @throws JSONException 不正なJSONファイルの場合に発生
     */
    private static DConnectProfileSpec loadProfileSpec(final Context context, final String profileName) throws IOException, JSONException {
        AssetManager assets = context.getAssets();
        String path = findProfileSpecPath(assets, profileName);
        if (path == null) {
            throw new FileNotFoundException("A spec file is not found: " + profileName);
        }

        String json = loadFile(assets.open(path));
        DConnectProfileSpecJsonParser parser = DConnectProfileSpecJsonParserFactory.getDefaultFactory().createParser();
        return parser.parseJson(new JSONObject(json));
    }

    /**
     * プロファイルの SPEC ファイルのパスを探します.
     *
     * @param assets Assets
     * @param profileName プロファイル名
     * @return プロファイルの SPEC ファイルへのパス
     * @throws IOException ファイルが見つからない場合に発生
     */
    private static String findProfileSpecPath(final AssetManager assets, final String profileName) throws IOException {
        String[] fileNames = assets.list("api");
        if (fileNames == null) {
            return null;
        }

        for (String fileFullName : fileNames) {
            if (!fileFullName.endsWith(SPEC_FILE_EXTENSION)) {
                continue;
            }
            String fileName = fileFullName.substring(0, fileFullName.length() - SPEC_FILE_EXTENSION.length());
            if (fileName.equalsIgnoreCase(profileName)) {
                return "api/" + fileFullName;
            }
        }
        throw new FileNotFoundException("A spec file is not found: " + profileName);
    }

    /**
     * ファイルのデータを読み込みます.
     * @param in ファイルのストリーム
     * @return ファイルデータ
     * @throws IOException ファイルの読み込みに失敗した場合に発生
     */
    private static String loadFile(final InputStream in) throws IOException {
        try {
            byte[] buf = new byte[1024];
            int len;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((len = in.read(buf)) > 0) {
                baos.write(buf, 0, len);
            }
            return new String(baos.toByteArray());
        } finally {
            in.close();
        }
    }
}
