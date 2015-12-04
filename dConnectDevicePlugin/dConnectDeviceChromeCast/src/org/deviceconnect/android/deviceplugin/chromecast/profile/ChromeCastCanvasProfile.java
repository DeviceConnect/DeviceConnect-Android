/*
 ChromeCastCanvasProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.chromecast.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.chromecast.ChromeCastService;
import org.deviceconnect.android.deviceplugin.chromecast.core.ChromeCastHttpServer;
import org.deviceconnect.android.deviceplugin.chromecast.core.ChromeCastMessage;
import org.deviceconnect.android.deviceplugin.chromecast.core.MediaFile;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.CanvasProfile;
import org.deviceconnect.message.DConnectMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Canvas Profile (Chromecast).
 * @author NTT DOCOMO, INC.
 */
public class ChromeCastCanvasProfile extends CanvasProfile implements ChromeCastConstants {

    /** Error message when Chromecast is not enabled. */
    private static final String ERROR_MESSAGE_DEVICE_NOT_ENABLED = "Chromecast is not enabled.";

    /** Prefix of image file name. */
    private static final String PREFIX = "dConnectDeviceChromecast_";

    /** The instance of {@link java.text.SimpleDateFormat}. */
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

    /** Logger. */
    private final Logger mLogger = Logger.getLogger("chromecast.dplugin");

    /**
     * Generates an image file name.
     * @return an image file name
     */
    private static String generateFileName() {
        Date timestamp = new Date(System.currentTimeMillis());
        return PREFIX + FORMAT.format(timestamp);
    }

    @Override
    protected boolean onPostDrawImage(final Intent request, final Intent response, final String serviceId,
            final String mimeType, final byte[] data, final double x, final double y, final String mode) {
        ((ChromeCastService) getContext()).connectChromeCast(serviceId, new ChromeCastService.Callback() {

            @Override
            public void onResponse() {
                if (data == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "data is not specified.");
                    sendResponse(response);
                    return;
                }

                try {
                    String path = exposeImage(data, mimeType);
                    mLogger.info("Exposed image: URL=" + path);
                    if (path == null) {
                        MessageUtils.setUnknownError(response, "The host device is not in local network.");
                        sendResponse(response);
                        return;
                    }
                    ChromeCastMessage app = ((ChromeCastService) getContext()).getChromeCastMessage();
                    if (!isDeviceEnable(response, app)) {
                        sendResponse(response);
                        return;
                    }
                    JSONObject json = new JSONObject();
                    json.put(KEY_FUNCTION, FUNCTION_POST_IMAGE);
                    json.put(KEY_URL, path);
                    json.put(KEY_MODE, mode);
                    json.put(KEY_X, x);
                    json.put(KEY_Y, y);
                    String message = json.toString();
                    mLogger.info("Send message successfully: " + message);
                    setResult(response, DConnectMessage.RESULT_OK);
                    app.sendMessage(response, message);
                } catch (IOException e) {
                    MessageUtils.setUnknownError(response, "Failed to deploy image to Chromecast.");
                    sendResponse(response);
                } catch (Exception e) {
                    MessageUtils.setUnknownError(response, e.getMessage());
                    sendResponse(response);
                }
            }
        });
        return false;
    }

    /**
     * Expose an image.
     *
     * @param data the binary data of an image
     * @param mimeType the mime type of an image
     * @return the path of the exposed image
     * @throws IOException if the specified image could not be exposed.
     */
    private String exposeImage(final byte[] data, final String mimeType) throws IOException {
        if (data == null) {
            throw new IllegalArgumentException("data is null.");
        }
        File file = saveFile(generateFileName(), data);
        return getHttpServer().exposeFile(new MediaFile(file, mimeType));
    }

    /**
     * Save binary data on local file system.
     *
     * @param fileName filename
     * @param data binary data
     * @return stored file
     * @throws IOException if the specified binary data could not be stored.
     */
    private File saveFile(final String fileName, final byte[] data) throws IOException {
        File dir = getContext().getFilesDir();
        File file = new File(dir, generateFileName());
        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new IOException("File is not be stored: " + fileName);
            }
        }
        FileOutputStream fos = new FileOutputStream(file);
        try {
            fos.write(data);
            fos.flush();
        } finally {
            fos.close();
        }
        return file;
    }

    /**
     * Gets HTTP server to expose images.
     * @return an instance of {@link ChromeCastHttpServer}
     */
    private ChromeCastHttpServer getHttpServer() {
        return ((ChromeCastService) getContext()).getChromeCastHttpServer();
    }

    @Override
    protected boolean onDeleteDrawImage(final Intent request, final Intent response, final String serviceId) {
        ((ChromeCastService) getContext()).connectChromeCast(serviceId, new ChromeCastService.Callback() {

            @Override
            public void onResponse() {
                ChromeCastMessage app = ((ChromeCastService) getContext()).getChromeCastMessage();
                if (!isDeviceEnable(response, app)) {
                    sendResponse(response);
                    return;
                }
                try {
                    JSONObject json = new JSONObject();
                    json.put(KEY_FUNCTION, FUNCTION_DELETE_IMAGE);
                    app.sendMessage(response, json.toString());
                } catch (JSONException e) {
                    MessageUtils.setUnknownError(response);
                    sendResponse(response);
                }
            }
        });
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
            MessageUtils.setIllegalDeviceStateError(response, ERROR_MESSAGE_DEVICE_NOT_ENABLED);
            setResult(response, DConnectMessage.RESULT_ERROR);
            return false;
        }
        return true;
    }
}
