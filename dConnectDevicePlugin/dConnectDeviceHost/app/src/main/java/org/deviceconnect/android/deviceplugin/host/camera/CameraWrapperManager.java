/*
 CameraWrapperManager.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.camera;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * カメラ管理クラス.
 *
 * <p>アプリケーションに対してただ1つのインスタンスを作成すること.</p>
 *
 * @author NTT DOCOMO, INC.
 */
public class CameraWrapperManager {

    /**
     * カメラ操作クラスの一覧.
     */
    private final Map<String, CameraWrapper> mCameras = new LinkedHashMap<>();

    /**
     * コンストラクタ.
     * @param context コンテキスト
     */
    public CameraWrapperManager(final Context context) {
        try {
            CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraWrapper camera = null;
                try {
                    camera = new CameraWrapper(context, cameraId);
                    mCameras.put(cameraId, camera);
                } catch (Exception e) {
                    // ignore.
                    if (BuildConfig.DEBUG) {
                        Log.w("CameraWrapperManager", "Failed to create a CameraWrapper.", e);
                    }
                }
            }
        } catch (CameraAccessException e) {
            // No camera is available now.
        }
    }

    /**
     * カメラ操作クラスのリストを取得する.
     * @return カメラ操作クラスのリスト
     */
    public synchronized List<CameraWrapper> getCameraList() {
        return new ArrayList<>(mCameras.values());
    }

    /**
     * カメラ操作クラスを全て破棄する.
     * アプリケーションを終了するときにのみ実行すること.
     */
    public synchronized void destroy() {
        for (CameraWrapper camera : mCameras.values()) {
            camera.destroy();
        }
        mCameras.clear();
    }
}
