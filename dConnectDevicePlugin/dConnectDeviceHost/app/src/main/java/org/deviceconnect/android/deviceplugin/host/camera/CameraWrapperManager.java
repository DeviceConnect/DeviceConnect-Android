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
import java.util.Arrays;
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
    private final Context mContext;

    /**
     * コンストラクタ.
     * @param context コンテキスト
     */
    public CameraWrapperManager(final Context context) {
        mContext = context;
        try {
            loadCamera();
        } catch (Exception e) {
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
     * 指定された ID のカメラを取得する.
     *
     * @param cameraId カメラの識別子
     * @return CameraWrapper、存在しない場合は null
     */
    public synchronized CameraWrapper getCameraById(String cameraId) {
        return mCameras.get(cameraId);
    }

    /**
     * ロストしたカメラを削除します.
     *
     * @throws CameraAccessException カメラへのアクセス失敗した場合に発生
     */
    private void removeLostCamera() throws CameraAccessException {
        CameraManager cameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        List<String> cameraIdList = Arrays.asList(cameraManager.getCameraIdList());
        for (String key : mCameras.keySet()) {
            if (cameraIdList.contains(key)) {
                continue;
            }
            CameraWrapper cameraWrapper = mCameras.remove(key);
            if (cameraWrapper != null) {
                cameraWrapper.destroy();
            }
        }
    }

    /**
     * カメラを読み込みます.
     *
     * @throws CameraAccessException カメラへのアクセス失敗した場合に発生
     */
    private void loadCamera() throws CameraAccessException {
        CameraManager cameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        for (String cameraId : cameraManager.getCameraIdList()) {
            if (mCameras.containsKey(cameraId)) {
                continue;
            }

            try {
                mCameras.put(cameraId, new CameraWrapper(mContext, cameraId));
            } catch (Exception e) {
                // ignore.
            }
        }
    }

    /**
     * カメラを再読み込みする.
     */
    public synchronized void reload() {
        try {
            removeLostCamera();
            loadCamera();
        } catch (Exception e) {
            // No camera is available now.
        }
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
