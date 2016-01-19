/*
 UVCMediaStreamRecordingProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.uvc.profile;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.serenegiant.usb.UVCCamera;

import org.deviceconnect.android.deviceplugin.uvc.UVCDevice;
import org.deviceconnect.android.deviceplugin.uvc.UVCDeviceManager;
import org.deviceconnect.android.deviceplugin.uvc.utils.BitmapUtils;
import org.deviceconnect.android.deviceplugin.uvc.utils.MixedReplaceMediaServer;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.MediaStreamRecordingProfile;
import org.deviceconnect.message.DConnectMessage;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * UVC MediaStream Recording Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class UVCMediaStreamRecordingProfile extends MediaStreamRecordingProfile {

    private static final String PARAM_WIDTH = "width";

    private static final String PARAM_HEIGHT = "height";

    private final Logger mLogger = Logger.getLogger("uvc.dplugin");

    private final UVCDeviceManager mDeviceMgr;

    private final Map<String, PreviewContext> mContexts = new HashMap<String, PreviewContext>();

    private final UVCDeviceManager.PreviewListener mPreviewListener
        = new UVCDeviceManager.PreviewListener() {
        @Override
        public void onFrame(final UVCDevice device, final byte[] frame, final int frameFormat,
                            final int width, final int height) {
            mLogger.info("onFrame: " + frame.length);

            PreviewContext context = mContexts.get(device.getId());
            if (context != null) {
                final byte[] media;
                if (context.mWidth == null && context.mHeight == null) {
                    media = frame;
                } else {
                    long start = 0;
                    long end = 0;

                    Bitmap bitmap;
                    switch (frameFormat) {
                        case UVCCamera.FRAME_FORMAT_MJPEG:
                            bitmap = BitmapFactory.decodeByteArray(frame, 0, frame.length);
                            break;
                        default:
                            // Nothing to do.
                            return;
                    }

                    // Resize bitmap
                    if (bitmap == null) {
                        mLogger.warning("MotionJPEG Frame could not be decoded to bitmap.");
                        return;
                    }
                    int w = context.mWidth != null ? context.mWidth : bitmap.getWidth();
                    int h = context.mHeight != null ? context.mHeight : bitmap.getHeight();
                    start = System.currentTimeMillis();
                    Bitmap resizedBitmap = BitmapUtils.resize(bitmap, w, h);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    media = baos.toByteArray();
                    resizedBitmap.recycle();
                    end = System.currentTimeMillis();
                    Log.d("AAA", "***** Resize Bitmap: " + (end - start) + " msec.");

                }
                context.mServer.offerMedia(media);
            }
        }
    };

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    public UVCMediaStreamRecordingProfile(final UVCDeviceManager deviceMgr) {
        mDeviceMgr = deviceMgr;
        mDeviceMgr.addPreviewListener(mPreviewListener);
    }

    @Override
    protected boolean onPutPreview(final Intent request, final Intent response,
                                   final String serviceId) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                UVCDevice device = mDeviceMgr.getDevice(serviceId);
                if (device == null) {
                    MessageUtils.setNotFoundServiceError(response);
                    sendResponse(response);
                    return;
                }
                if (!checkPreviewParams(request, response)) {
                    sendResponse(response);
                    return;
                }

                PreviewContext context = startMediaServer(device.getId());
                context.mWidth = getWidth(request);
                context.mHeight = getHeight(request);

                if (device.startPreview()) {
                    setResult(response, DConnectMessage.RESULT_OK);
                    setUri(response, context.mServer.getUrl());
                } else {
                    MessageUtils.setIllegalDeviceStateError(response, "UVC device is not open.");
                }
                sendResponse(response);
            }
        });
        return false;
    }

    private boolean checkPreviewParams(final Intent request, final Intent response) {
        if (!checkType(request, response, PARAM_WIDTH)) {
            return false;
        }
        if (!checkType(request, response, PARAM_HEIGHT)) {
            return false;
        }
        Integer width = getWidth(request);
        Integer height = getHeight(request);
        if (width != null && width <= 0) {
            MessageUtils.setInvalidRequestParameterError(response, PARAM_WIDTH + " must be positive.");
            return false;
        }
        if (height != null && height <= 0) {
            MessageUtils.setInvalidRequestParameterError(response, PARAM_HEIGHT + " must be positive.");
            return false;
        }
        return true;
    }

    @Override
    protected boolean onDeletePreview(final Intent request, final Intent response,
                                      final String serviceId) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                UVCDevice device = mDeviceMgr.getDevice(serviceId);
                if (device == null) {
                    MessageUtils.setNotFoundServiceError(response);
                    sendResponse(response);
                    return;
                }

                device.stopPreview();
                stopMediaServer(device.getId());

                setResult(response, DConnectMessage.RESULT_OK);
                sendResponse(response);
            }
        });
        return false;
    }

    private synchronized PreviewContext startMediaServer(final String id) {
        PreviewContext context = mContexts.get(id);
        if (context == null) {
            MixedReplaceMediaServer server = new MixedReplaceMediaServer();
            server.setServerName("UVC Video Server");
            server.setContentType("image/jpg");
            server.start();

            context = new PreviewContext(server);
            mContexts.put(id, context);
        }
        return context;
    }

    public synchronized void stopMediaServer(final String id) {
        PreviewContext context = mContexts.remove(id);
        if (context != null) {
            context.mServer.stop();
        }
    }

    private static Integer getWidth(final Intent request) {
        return parseInteger(request, PARAM_WIDTH);
    }

    private static Integer getHeight(final Intent request) {
        return parseInteger(request, PARAM_HEIGHT);
    }

    private boolean checkType(final Intent request, final Intent response,
                              final String paramName) {
        if (!request.hasExtra(paramName)) {
            return true;
        }
        Object param = request.getExtras().get(paramName);
        if (param instanceof Integer) {
            return true;
        } else if (param instanceof String) {
            try {
                Integer.parseInt((String) param);
                return true;
            } catch (NumberFormatException e) {
                MessageUtils.setInvalidRequestParameterError(response, paramName + " is invalid format.");
                return false;
            }
        } else {
            MessageUtils.setInvalidRequestParameterError(response, paramName + " is invalid type.");
            return false;
        }
    }

    private static class PreviewContext {

        Integer mWidth;

        Integer mHeight;

        final MixedReplaceMediaServer mServer;

        PreviewContext(final MixedReplaceMediaServer server) {
            if (server == null) {
                throw new IllegalArgumentException();
            }
            mServer = server;
        }

    }
}
