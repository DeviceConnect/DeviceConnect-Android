/*
 DConnectPluginSpec.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec;


import org.deviceconnect.android.profile.spec.parser.DConnectProfileSpecJsonParser;
import org.deviceconnect.android.profile.spec.parser.DConnectProfileSpecJsonParserFactory;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * プラグインのサポートする仕様を保持するクラス.
 * <p>
 * プラグインのサポートするプロファイルのリストを持つ.
 * </p>
 * @author NTT DOCOMO, INC.
 */
public class DConnectPluginSpec {

    private static final int BUFFER_SIZE = 1024;

    private final DConnectProfileSpecJsonParser mJsonParser
        = DConnectProfileSpecJsonParserFactory.getDefaultFactory().createParser();

    private final Map<String, DConnectProfileSpec> mProfileSpecs
        = new ConcurrentHashMap<String, DConnectProfileSpec>();

    /**
     * 入力ストリームからDevice Connectプロファイルの仕様定義を追加する.
     *
     * @param profileName プロファイル名
     * @param in 入力ストリーム
     * @throws IOException 入力ストリームの読み込みに失敗した場合
     * @throws JSONException JSONの構造が不正な場合
     */
    public void addProfileSpec(final String profileName, final InputStream in)
        throws IOException, JSONException {
        DConnectProfileSpec profileSpec = mJsonParser.parseJson(new JSONObject(loadFile(in)));
        if (profileSpec.mProfileName == null) {
            profileSpec.mProfileName = profileName;
        }
        for (DConnectApiSpec apiSpec : profileSpec.getApiSpecList()) {
            apiSpec.setApiName(profileSpec.mApiName);
            apiSpec.setProfileName(profileSpec.mProfileName);
        }
        mProfileSpecs.put(profileName, profileSpec);
    }

    private static String loadFile(final InputStream in) throws IOException {
        try {
            byte[] buf = new byte[BUFFER_SIZE];
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

    /**
     * 指定したプロファイルの仕様定義を取得する.
     * @param profileName プロファイル名
     * @return {@link DConnectProfileSpec}のインスタンス
     */
    public DConnectProfileSpec findProfileSpec(final String profileName) {
        return mProfileSpecs.get(profileName);
    }

    /**
     * プラグインのサポートするプロファイルの仕様定義の一覧を取得する.
     * <p>
     * このメソッドから返される一覧には、各プロファイル上で定義されているすべてのAPIの定義が含まれる.
     * </p>
     * @return {@link DConnectProfileSpec}のマップ. キーはプロファイル名.
     */
    public Map<String, DConnectProfileSpec> getProfileSpecs() {
        return new HashMap<String, DConnectProfileSpec>(mProfileSpecs);
    }

}
