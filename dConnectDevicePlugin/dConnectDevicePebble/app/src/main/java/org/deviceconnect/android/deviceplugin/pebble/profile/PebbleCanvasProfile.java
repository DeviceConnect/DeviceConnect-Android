/*
 PebbleCanvasProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.pebble.profile;

import android.content.Intent;

import com.getpebble.android.kit.util.PebbleDictionary;

import org.deviceconnect.android.deviceplugin.pebble.PebbleDeviceService;
import org.deviceconnect.android.deviceplugin.pebble.util.PebbleManager;
import org.deviceconnect.android.deviceplugin.pebble.util.PebbleManager.OnSendCommandListener;
import org.deviceconnect.android.deviceplugin.pebble.util.PebbleManager.OnSendDataListener;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.CanvasProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * Pebble 用 Canvasプロファイル.
 *
 * @author NTT DOCOMO, INC.
 */
public class PebbleCanvasProfile extends CanvasProfile {

    private ExecutorService mImageService = Executors.newSingleThreadExecutor();

    private final Pattern PATTERN_MIME_TYPE = Pattern.compile("^[^/]+/[^/]+$");

    private boolean isMIMEType(final String mimeType) {
        return PATTERN_MIME_TYPE.matcher(mimeType).matches();
    }

    private final DConnectApi mPostDrawImageApi = new PostApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_DRAW_IMAGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final byte[] data = getData(request);
            final String mode = getMode(request);
            final double x = getX(request);
            final double y = getY(request);
            final String mimeType = getMIMEType(request);
            if (mimeType != null) {
                boolean isValid = isMIMEType(mimeType);
                if (!isValid) {
                    MessageUtils.setInvalidRequestParameterError(response, "mimeType is invalid: " + mimeType);
                    return true;
                } else if (!mimeType.startsWith("image/")) {
                    MessageUtils.setInvalidRequestParameterError(response, "this mimeType is unsupported: " + mimeType);
                    return true;
                }
            }

            if (data == null) {
                mImageService.execute(new Runnable() {
                    @Override
                    public void run() {
                        String uri = getURI(request);
                        byte[] result = getData(uri);
                        if (result == null) {
                            MessageUtils.setInvalidRequestParameterError(response, "could not get image from uri.");
                            sendResponse(response);
                            return;
                        }
                        if (drawImage(response, result, mode, x, y)) {
                            sendResponse(response);
                        }
                    }
                });
                return false;
            } else {
                try {
                    return drawImage(response, data, mode, x, y);
                } catch (Throwable e) {
                    e.printStackTrace();
                    MessageUtils.setUnknownError(response, e.getMessage());
                    return true;
                }

            }
        }
    };

    private final DConnectApi mDeleteDrawImage = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_DRAW_IMAGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            PebbleDeviceService service = (PebbleDeviceService) getContext();
            PebbleManager mgr = service.getPebbleManager();
            PebbleDictionary dic = new PebbleDictionary();
            dic.addInt8(PebbleManager.KEY_PROFILE, (byte) PebbleManager.PROFILE_CANVAS);
            dic.addInt8(PebbleManager.KEY_ATTRIBUTE, (byte) PebbleManager.CANVAS_ATTRBIUTE_DRAW_IMAGE);
            dic.addInt8(PebbleManager.KEY_ACTION, (byte) PebbleManager.ACTION_DELETE);
            mgr.sendCommandToPebble(dic, new OnSendCommandListener() {
                @Override
                public void onReceivedData(final PebbleDictionary dic) {
                    if (dic == null) {
                        MessageUtils.setUnknownError(response);
                    } else {
                        setResult(response, DConnectMessage.RESULT_OK);
                    }
                    sendResponse(response);
                }
            });
            return false;
        }
    };

    public PebbleCanvasProfile() {
        addApi(mPostDrawImageApi);
        addApi(mDeleteDrawImage);
    }

    private boolean drawImage(final Intent response, byte[] data, String mode, double x, double y) {
        PebbleManager mgr = ((PebbleDeviceService) getContext()).getPebbleManager();
        byte[] buf = PebbleManager.convertImage(data, mode, x, y);
        if (buf == null) {
            // unknown mode-value
            MessageUtils.setInvalidRequestParameterError(response);
            return true;
        }

        mgr.sendDataToPebble(buf, new OnSendDataListener() {
            @Override
            public void onSend(final boolean successed) {
                if (successed) {
                    setResult(response, DConnectMessage.RESULT_OK);
                } else {
                    MessageUtils.setUnknownError(response);
                }
                sendResponse(response);
            }
        });
        return false;
    }

}

