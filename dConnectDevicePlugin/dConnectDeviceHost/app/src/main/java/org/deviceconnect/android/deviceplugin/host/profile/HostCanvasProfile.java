/*
 HostCanvasProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.host.profile;

import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.deviceconnect.android.deviceplugin.host.HostDeviceApplication;
import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.activity.profile.CanvasProfileActivity;
import org.deviceconnect.android.deviceplugin.host.canvas.CanvasDrawImageObject;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.CanvasProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.android.util.NotificationUtils;
import org.deviceconnect.message.DConnectMessage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Canvas Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class HostCanvasProfile extends CanvasProfile {
    /** ファイルが生存できる有効時間を定義する. */
    private static final long DEFAULT_EXPIRE = 1000 * 60 * 5;

    /** Canvasプロファイルのファイル名プレフィックス。 */
    private static final String CANVAS_PREFIX = "host_canvas";

    /** Notification Id */
    private static final int NOTIFICATION_ID = 3517;

    /** Edit Image Thread. */
    private final ExecutorService mImageService = Executors.newSingleThreadExecutor();

    // POST /gotapi/canvas/drawImage
    private final DConnectApi mDrawImageApi = new PostApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_DRAW_IMAGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String mode = getMode(request);
            String mimeType = getMIMEType(request);

            CanvasDrawImageObject.Mode enumMode = CanvasDrawImageObject.convertMode(mode);
            if (enumMode == null) {
                MessageUtils.setInvalidRequestParameterError(response);
                return true;
            }

            if (mimeType != null && !mimeType.contains("image")) {
                MessageUtils.setInvalidRequestParameterError(response,
                    "Unsupported mimeType: " + mimeType);
                return true;
            }

            byte[] data = getData(request);
            String uri = getURI(request);
            double x = getX(request);
            double y = getY(request);
            if (data == null) {
                if (uri != null) {
                    if (uri.startsWith("http")) {
                        drawImage(response, uri, enumMode, x, y);
                    } else {
                        MessageUtils.setInvalidRequestParameterError(response, "Invalid uri.");
                    }
                } else {
                    MessageUtils.setInvalidRequestParameterError(response, "Uri and data is null.");
                }
                return true;
            } else {
                mImageService.execute(() -> sendImage(data, response, enumMode, x, y));
                return false;
            }
        }
    };

    // DELETE /gotapi/canvas/drawImage
    private final DConnectApi mDeleteImageApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_DRAW_IMAGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            if (isCanvasProfileActivity()) {
                sendDeleteCanvasProfileActivity();
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
    public HostCanvasProfile() {
        addApi(mDrawImageApi);
        addApi(mDeleteImageApi);
    }

    /**
     * Send a image for CanvasProfileActivity.
     *
     * @param data binary
     * @param response response message
     * @param enumMode image mode
     * @param x position
     * @param y position
     */
    private void sendImage(byte[] data, Intent response, CanvasDrawImageObject.Mode enumMode, double x, double y) {
        try {
            drawImage(response, writeForImage(data), enumMode, x, y);
        } catch (Throwable e) {
            MessageUtils.setIllegalDeviceStateError(response, e.getMessage());
        }
        sendResponse(response);
    }

    /**
     * Start a CanvasProfileActivity.
     *
     * @param response response message
     * @param uri image url
     * @param enumMode image mode
     * @param x position
     * @param y position
     */
    private void drawImage(Intent response, String uri, CanvasDrawImageObject.Mode enumMode, double x, double y) {
        CanvasDrawImageObject drawObj = new CanvasDrawImageObject(uri, enumMode, x, y);
        if (isCanvasProfileActivity()) {
            sendDrawCanvas(drawObj);
        } else {
            Intent intent = new Intent();
            intent.setClass(getContext(), CanvasProfileActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            drawObj.setValueToIntent(intent);
            if (getApp().isDeviceConnectClassOfTopActivity() || Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                getContext().startActivity(intent);
            } else {
                NotificationUtils.createNotificationChannel(getContext());
                NotificationUtils.notify(getContext(), NOTIFICATION_ID, 0, intent,
                        getContext().getString(R.string.host_notification_canvas_warnning));
            }
        }
        setResult(response, DConnectMessage.RESULT_OK);
    }

    private void sendDrawCanvas(CanvasDrawImageObject drawObj) {
        Intent intent = new Intent(CanvasDrawImageObject.ACTION_DRAW_CANVAS);
        drawObj.setValueToIntent(intent);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }

    private void sendDeleteCanvasProfileActivity() {
        Intent intent = new Intent(CanvasDrawImageObject.ACTION_DELETE_CANVAS);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }

    private boolean isCanvasProfileActivity() {
        return getApp().isClassnameOfTopActivity(CanvasProfileActivity.class);
    }

    private HostDeviceApplication getApp() {
        return (HostDeviceApplication) getContext().getApplicationContext();
    }

    /**
     * CanvasProfileActivity に渡せるように画像をフォルダに一時的に保存します.
     *
     * @param data binary
     * @return URI
     * @throws IOException 画像の読み込みに失敗した場合
     * @throws OutOfMemoryError メモリ不足が発生した場合
     */
    private String writeForImage(final byte[] data) throws IOException, OutOfMemoryError {
        File file = getContext().getCacheDir();
        FileOutputStream out = null;
        checkAndRemove(file);
        File dstFile = File.createTempFile(CANVAS_PREFIX, ".tmp", file);
        try {
            out = new FileOutputStream(dstFile);
            out.write(data);
            out.close();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return dstFile.getAbsolutePath();
    }

    /**
     * ファイルをチェックして、中身を削除する.
     *
     * @param file 削除するファイル
     */
    private void checkAndRemove(@NonNull final File file) {
        if (file.isDirectory()) {
            for (File childFile : file.listFiles()) {
                checkAndRemove(childFile);
            }
        } else if (file.isFile() && file.getName().startsWith(CANVAS_PREFIX)) {
            long modified = file.lastModified();
            if (System.currentTimeMillis() - modified > DEFAULT_EXPIRE) {
                file.delete();
            }
        }
    }
}
