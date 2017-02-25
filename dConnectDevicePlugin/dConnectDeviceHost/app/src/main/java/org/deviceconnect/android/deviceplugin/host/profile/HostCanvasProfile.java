/*
 HostCanvasProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.host.profile;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import org.deviceconnect.android.deviceplugin.host.activity.CanvasProfileActivity;
import org.deviceconnect.android.deviceplugin.host.canvas.CanvasDrawImageObject;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.CanvasProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.android.provider.FileManager;
import org.deviceconnect.message.DConnectMessage;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Canvas Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class HostCanvasProfile extends CanvasProfile {
    /** ファイル管理クラス. */
    private FileManager mFileMgr;
    /** ファイル名に付けるプレフィックス. */
    private static final String FILENAME_PREFIX = "android_canvas_";
    /** ファイルの拡張子. */
    private static final String FILE_EXTENSION = ".jpg";

    /** 日付のフォーマット. */
    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyyMMdd_kkmmss", Locale.JAPAN);

    private ExecutorService mImageService = Executors.newSingleThreadExecutor();

    private final DConnectApi mDrawImageApi = new PostApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_DRAW_IMAGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            // キャッシュの削除
            mFileMgr.checkAndRemove("temp", new FileManager.RemoveFileCallback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onFail(@NonNull Throwable throwable) {

                }
            });
            String mode = getMode(request);
            String mimeType = getMIMEType(request);
            final CanvasDrawImageObject.Mode enumMode = CanvasDrawImageObject.convertMode(mode);
            if (enumMode == null) {
                MessageUtils.setInvalidRequestParameterError(response);
                return true;
            }

            if (mimeType != null && !mimeType.contains("image")) {
                MessageUtils.setInvalidRequestParameterError(response,
                    "Unsupported mimeType: " + mimeType);
                return true;
            }
            final byte[] data = getData(request);
            final String uri = getURI(request);
            final double x = getX(request);
            final double y = getY(request);
            if (data == null) {
                drawImage(response, uri, enumMode, x, y);
                return true;
            } else {
                mImageService.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (mFileMgr == null) {
                            MessageUtils.setInvalidRequestParameterError(response, "FileManager is not implemented.");
                        }
                        File mBaseDir = mFileMgr.getBasePath();
                        File mMakeDir = new File(mBaseDir, "temp");

                        if (!mMakeDir.isDirectory()) {
                            mMakeDir.mkdirs();
                        }
                        mFileMgr.saveFile(createNewFileName(), data, new FileManager.SaveFileCallback() {
                            @Override
                            public void onSuccess(@NonNull String uri) {
                                drawImage(response, uri, enumMode, x, y);
                                sendResponse(response);

                            }

                            @Override
                            public void onFail(@NonNull Throwable throwable) {
                                MessageUtils.setInvalidRequestParameterError(response, throwable.getMessage());
                                sendResponse(response);
                            }
                        });
                    }
                });
                return false;
            }
        }
    };

    private final DConnectApi mDeleteImageApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_DRAW_IMAGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String className = getClassnameOfTopActivity();
            if (CanvasProfileActivity.class.getName().equals(className)) {
                Intent intent = new Intent(CanvasDrawImageObject.ACTION_DELETE_CANVAS);
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setIllegalDeviceStateError(response, "canvas not display");
            }

            return true;
        }
    };

    /**
     * コンストラクタ.
     */
    public HostCanvasProfile(final FileManager fm) {
        mFileMgr = fm;
        addApi(mDrawImageApi);
        addApi(mDeleteImageApi);
    }

    private void drawImage(Intent response, String uri, CanvasDrawImageObject.Mode enumMode, double x, double y) {
        CanvasDrawImageObject drawObj = new CanvasDrawImageObject(uri, enumMode, x, y);

        String className = getClassnameOfTopActivity();
        if (CanvasProfileActivity.class.getName().equals(className)) {
            Intent intent = new Intent(CanvasDrawImageObject.ACTION_DRAW_CANVAS);
            drawObj.setValueToIntent(intent);
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
        } else {
            Intent intent = new Intent();
            intent.setClass(getContext(), CanvasProfileActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            drawObj.setValueToIntent(intent);
            getContext().startActivity(intent);
        }

        setResult(response, DConnectMessage.RESULT_OK);
    }

    /**
     * 画面の一番上にでているActivityのクラス名を取得.
     *
     * @return クラス名
     */
    private String getClassnameOfTopActivity() {
        ActivityManager activityMgr = (ActivityManager) getContext().getSystemService(Service.ACTIVITY_SERVICE);
        return activityMgr.getRunningTasks(1).get(0).topActivity.getClassName();
    }

    /**
     * 新規のファイル名を作成する.
     *
     * @return ファイル名
     */
    private String createNewFileName() {
        return "temp/" + FILENAME_PREFIX + mSimpleDateFormat.format(new Date()) + FILE_EXTENSION;
    }
}
