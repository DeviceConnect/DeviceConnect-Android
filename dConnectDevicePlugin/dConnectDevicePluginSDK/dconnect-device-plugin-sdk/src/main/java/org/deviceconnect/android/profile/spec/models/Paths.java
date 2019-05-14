/*
 Paths.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec.models;

import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 個々のエンドポイントへの相対パスを保持します.
 *
 * @author NTT DOCOMO, INC.
 */
public class Paths extends AbstractSpec {
    /**
     * API で利用可能なパスを格納するマップ.
     * <p>
     * Required.
     * </p>
     */
    private Map<String, Path> mPaths;

    /**
     * API で利用可能なパスを格納するマップを取得します.
     *
     * @return API で利用可能なパスを格納するマップ
     */
    public Map<String, Path> getPaths() {
        return mPaths;
    }

    /**
     * API で利用可能なパスを格納するマップを設定します.
     *
     * @param paths API で利用可能なパスを格納するマップ
     */
    public void setPaths(Map<String, Path> paths) {
        mPaths = paths;
    }

    /**
     * API で利用可能なパスをマップに追加します.
     *
     * @param key エンドポイントへの相対パス
     * @param path パス情報
     */
    public void addPath(String key, Path path) {
        if (mPaths == null) {
            mPaths = new HashMap<>();
        }
        mPaths.put(key, path);
    }

    /**
     * API で利用可能なパスをマップに削除します.
     *
     * @param key エンドポイントへの相対パス
     */
    public Path removePath(String key) {
        if (mPaths != null) {
            return mPaths.remove(key);
        }
        return null;
    }

    /**
     * エンドポイントへの相対パスのリストを取得します.
     *
     * @return エンドポイントへの相対パスのリスト
     */
    public Set<String> getKeySet() {
        return mPaths.keySet();
    }

    /**
     * 指定されたエンドポイントへの相対パスのパス情報を取得します.
     *
     * @param key エンドポイントへの相対パス
     * @return パス情報
     */
    public Path getPath(String key) {
        return mPaths.get(key);
    }

    @Override
    public Bundle toBundle() {
        Bundle bundle = new Bundle();

        if (mPaths != null && !mPaths.isEmpty()) {
            for (Map.Entry<String, Path> entry : mPaths.entrySet()) {
                bundle.putParcelable(entry.getKey(), entry.getValue().toBundle());
            }
        }

        copyVendorExtensions(bundle);

        return bundle;
    }
}
