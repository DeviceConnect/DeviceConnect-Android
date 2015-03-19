/*
 ChromeCastCanvasProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.chromecast.profile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import org.deviceconnect.android.deviceplugin.chromecast.BuildConfig;
import org.deviceconnect.android.deviceplugin.chromecast.ChromeCastService;
import org.deviceconnect.android.deviceplugin.chromecast.core.ChromeCastHttpServer;
import org.deviceconnect.android.deviceplugin.chromecast.core.ChromeCastMessage;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.CanvasProfile;
import org.deviceconnect.message.DConnectMessage;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;

/**
 * Canvas Profile (Chromecast)
 * @author NTT DOCOMO, INC.
 */
public class ChromeCastCanvasProfile extends CanvasProfile {

    /** Chromecastが無効になっているときのエラーメッセージ. */
    private static final String ERROR_MESSAGE_DEVICE_NOT_ENABLE = "Device is not enable";

    /** Logger. */
    private final Logger mLogger = Logger.getLogger("chromecast.dplugin");

    @Override
    protected boolean onPostDrawImage(final Intent request, final Intent response, final String serviceId,
            final String mimeType, final byte[] data, final double x, final double y, final String mode) {
        if (data == null) {
            MessageUtils.setInvalidRequestParameterError(response, "data is not specified.");
            return true;
        }

        try {
            String url = exposeImage(data, mimeType);
            ChromeCastMessage app = ((ChromeCastService) getContext()).getChromeCastMessage();
            if (!isDeviceEnable(response, app)) {
                return true;
            }
            String message = "{\"function\":\"canvas_draw\", \"url\":\"" + url + "\", \"x\":" + x
                + ", \"y\":" + y + ", \"mode\":\"" + mode + "\"}";
            app.sendMessage(response, message);
            mLogger.info("Send message successfully: " + message);

            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        } catch (IOException e) {
            MessageUtils.setUnknownError(response, "Failed to deploy image to Chromecast.");
            return true;
        } catch (Exception e) {
            MessageUtils.setUnknownError(response, e.getMessage());
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            return true;
        }
    }

    private String exposeImage(final byte[] data, final String mimeType) throws IOException {
        if (data == null) {
            throw new IllegalArgumentException("data is null.");
        }
        String fileName = generateRandomFileName(mimeType);
        File dir = getContext().getFilesDir();
        File file = new File(dir, fileName);
        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new IOException("Failed to store file on host device: " + fileName);
            }
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(data);
            fos.flush();
            mLogger.info("Store image: Path=" + file.getAbsolutePath() + " (" + data.length + " bytes)");
        } finally {
            if (fos != null) {
                fos.close();
            }
        }

        ChromeCastHttpServer server = ((ChromeCastService) getContext())
                .getChromeCastHttpServer();
        return server.exposeFile(file);
    }

    private String generateRandomFileName(final String mimeType) {
        String name = String.valueOf(System.currentTimeMillis());
        if (mimeType != null) {
            name += mimeType;
        }
        return name;
    }

    @Override
    protected boolean onDeleteDrawImage(final Intent request, final Intent response, final String serviceId) {
        ChromeCastMessage app = ((ChromeCastService) getContext()).getChromeCastMessage();
        if (!isDeviceEnable(response, app)) {
            return true;
        }
        app.sendMessage(response, "{\"function\":\"canvas_delete\"}");
        return false;
    }

    /**
     * デバイスが有効か否かを返す<br/>.
     * デバイスが無効の場合、レスポンスにエラーを設定する
     * 
     * @param   response    レスポンス
     * @param   app         ChromeCastMediaPlayer
     * @return  デバイスが有効か否か（有効: true, 無効: false）
     */
    private boolean isDeviceEnable(final Intent response, final ChromeCastMessage app) {
        if (!app.isDeviceEnable()) {
            MessageUtils.setIllegalDeviceStateError(response, ERROR_MESSAGE_DEVICE_NOT_ENABLE);
            setResult(response, DConnectMessage.RESULT_ERROR);
            return false;
        }
        return true;
    }
}
