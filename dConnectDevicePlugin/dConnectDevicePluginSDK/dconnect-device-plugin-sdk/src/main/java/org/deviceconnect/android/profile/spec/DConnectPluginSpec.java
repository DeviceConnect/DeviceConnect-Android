package org.deviceconnect.android.profile.spec;

import org.deviceconnect.android.profile.spec.models.Swagger;
import org.deviceconnect.android.profile.spec.parser.OpenAPIParser;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * プラグインのサポートする仕様を保持するクラス.
 *
 * <p>
 * プラグインのサポートするプロファイルのリストを持つ.
 * </p>
 *
 * @author NTT DOCOMO, INC.
 */
public class DConnectPluginSpec {

    /**
     * 各プロファイルの定義ファイルを保持するマップ.
     */
    private final Map<String, Swagger> mProfileSpecs = new HashMap<>();

    /**
     * 入力ストリームからDevice Connectプロファイルの仕様定義を追加する.
     *
     * @param profileName プロファイル名
     * @param in 入力ストリーム
     * @throws IOException 入力ストリームの読み込みに失敗した場合
     * @throws JSONException JSONの構造が不正な場合
     */
    public void addProfileSpec(final String profileName, final InputStream in) throws IOException, JSONException {
        mProfileSpecs.put(profileName.toLowerCase(), OpenAPIParser.parse(loadFile(in)));
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
     * 指定したプロファイルの仕様定義を取得する.
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
}
