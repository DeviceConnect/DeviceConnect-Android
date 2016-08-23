/*
 DConnectProfileSpec.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec;


import android.os.Bundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * プロファイルについての仕様を保持するクラス.
 * <p>
 * 当該プロファイル上で定義されるAPIの仕様のリストを持つ.
 * </p>
 * @author NTT DOCOMO, INC.
 */
public class DConnectProfileSpec implements DConnectSpecConstants {

    private Bundle mBundle;

    private Map<String, Map<Method, DConnectApiSpec>> mAllApiSpecs;

    private DConnectProfileSpec() {
    }

    /**
     * API仕様定義ファイルから生成したBundleのインスタンスを設定する.
     * @param bundle Bundleのインスタンス
     */
    void setBundle(final Bundle bundle) {
        mBundle = bundle;
    }

    /**
     * APIの仕様定義のマップを設定する.
     * @param apiSpecs {@link DConnectApiSpec}のマップ
     */
    void setApiSpecs(final Map<String, Map<Method, DConnectApiSpec>> apiSpecs) {
        mAllApiSpecs = apiSpecs;
    }

    /**
     * 当該プロファイル上で定義されている、APIの仕様定義のリストを取得する.
     * @return {@link DConnectApiSpec}のリスト
     */
    public List<DConnectApiSpec> getApiSpecList() {
        List<DConnectApiSpec> list = new ArrayList<DConnectApiSpec>();
        if (mAllApiSpecs == null) {
            return list;
        }
        for (Map<Method, DConnectApiSpec> apiSpecs : mAllApiSpecs.values()) {
            for (DConnectApiSpec apiSpec : apiSpecs.values()) {
                list.add(apiSpec);
            }
        }
        return list;
    }

    /**
     * 指定されたパスで提供されるAPIの仕様定義のマップを取得する.
     * @param path APIのパス
     * @return {@link DConnectApiSpec}のマップ. キーはメソッド名.
     *         指定されたパスで提供しているAPIが存在しない場合は<code>null</code>
     */
    public Map<Method, DConnectApiSpec> findApiSpecs(final String path) {
        if (path == null) {
            throw new IllegalArgumentException("path is null.");
        }
        return mAllApiSpecs.get(path.toLowerCase());
    }

    /**
     * 指定されたパスとメソッドで提供されるAPIの仕様定義を取得する.
     * @param path APIのパス
     * @param method APIのメソッド名
     * @return {@link DConnectApiSpec}のインスタンス.
     *         指定されたパスとメソッドで提供しているAPIが存在しない場合は<code>null</code>
     */
    public DConnectApiSpec findApiSpec(final String path, final Method method) {
        if (method == null) {
            throw new IllegalArgumentException("method is null.");
        }
        Map<Method, DConnectApiSpec> apiSpecsOfPath = findApiSpecs(path);
        if (apiSpecsOfPath == null) {
            return null;
        }
        return apiSpecsOfPath.get(method);
    }

    /**
     * API仕様定義ファイルから生成したBundleのインスタンスを取得する.
     * @return Bundleのインスタンス
     */
    public Bundle toBundle() {
        return mBundle;
    }

    /**
     * {@link DConnectProfileSpec}のビルダー.
     *
     * @author NTT DOCOMO, INC.
     */
    public static class Builder {

        private final Map<String, Map<Method, DConnectApiSpec>> mAllApiSpecs =
            new HashMap<String, Map<Method, DConnectApiSpec>>();

        private Bundle mBundle;

        /**
         * APIの仕様定義を追加する.
         *
         * @param path パス
         * @param method メソッド
         * @param apiSpec 仕様定義
         * @return ビルダー自身のインスタンス
         */
        public Builder addApiSpec(final String path, final Method method,
                               final DConnectApiSpec apiSpec) {
            String[] names = path.split("/");
            if (names.length == 2) {
                if (!names[1].equals("")) {
                    apiSpec.setAttributeName(names[1]);
                }
            } else if (names.length == 3) {
                apiSpec.setInterfaceName(names[1]);
                apiSpec.setAttributeName(names[2]);
            }

            String pathKey = path.toLowerCase();
            Map<Method, DConnectApiSpec> apiSpecs = mAllApiSpecs.get(pathKey);
            if (apiSpecs == null) {
                apiSpecs = new HashMap<Method, DConnectApiSpec>();
                mAllApiSpecs.put(pathKey, apiSpecs);
            }
            apiSpecs.put(method, apiSpec);
            return this;
        }

        /**
         * API仕様定義ファイルから生成したBundleのインスタンスを取得する.
         *
         * @param bundle Bundleのインスタンス
         * @return ビルダー自身のインスタンス
         */
        public Builder setBundle(final Bundle bundle) {
            mBundle = bundle;
            return this;
        }

        /**
         * {@link DConnectProfileSpec}のインスタンスを生成する.
         *
         * @return {@link DConnectProfileSpec}のインスタンス
         */
        public DConnectProfileSpec build() {
            DConnectProfileSpec profileSpec = new DConnectProfileSpec();
            profileSpec.setApiSpecs(mAllApiSpecs);
            profileSpec.setBundle(mBundle);
            return profileSpec;
        }
    }
}
