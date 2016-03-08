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
import android.os.Bundle;

import com.serenegiant.usb.UVCCamera;

import org.deviceconnect.android.deviceplugin.uvc.UVCDevice;
import org.deviceconnect.android.deviceplugin.uvc.UVCDeviceManager;
import org.deviceconnect.android.deviceplugin.uvc.utils.MixedReplaceMediaServer;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.MediaStreamRecordingProfile;
import org.deviceconnect.message.DConnectMessage;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
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

    private static final String PARAM_MAX_FRAME_RATE = "maxFrameRate";

    private static final String RECORDER_ID = "0";

    private static final String RECORDER_MIME_TYPE_MJPEG = "video/x-mjpeg";

    private static final String[] RECORDER_MIME_TYPE_LIST = {
        RECORDER_MIME_TYPE_MJPEG
    };

    private static final String RECORDER_CONFIG = ""; // No config.

    private final Logger mLogger = Logger.getLogger("uvc.dplugin");

    private final UVCDeviceManager mDeviceMgr;

    private final Map<String, PreviewContext> mContexts = new HashMap<String, PreviewContext>();

    private final UVCDeviceManager.PreviewListener mPreviewListener
        = new UVCDeviceManager.PreviewListener() {
        @Override
        public void onFrame(final UVCDevice device, final byte[] frame, final int frameFormat,
                            final int width, final int height) {
            //mLogger.info("onFrame: " + frame.length);

            if (frameFormat != UVCCamera.FRAME_FORMAT_MJPEG) {
                mLogger.warning("onFrame: unsupported frame format: " + frameFormat);
                return;
            }

            PreviewContext context = mContexts.get(device.getId());
            if (context != null) {
                final byte[] media = context.willResize() ? context.resize(frame) : frame;
                context.mServer.offerMedia(media);
            }
        }
    };

    private final UVCDeviceManager.ConnectionListener mConnectionListener
        = new UVCDeviceManager.ConnectionListener() {
        @Override
        public void onConnect(final UVCDevice device) {
            // Nothing to do.
        }

        @Override
        public void onConnectionFailed(final UVCDevice device) {
            // Nothing to do.
        }

        @Override
        public void onDisconnect(final UVCDevice device) {
            stopMediaServer(device.getId());
        }
    };

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    public UVCMediaStreamRecordingProfile(final UVCDeviceManager deviceMgr) {
        mDeviceMgr = deviceMgr;
        mDeviceMgr.addPreviewListener(mPreviewListener);
        mDeviceMgr.addConnectionListener(mConnectionListener);
    }

    @Override
    protected boolean onGetMediaRecorder(final Intent request, final Intent response,
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
                if (!device.isInitialized()) {
                    MessageUtils.setIllegalDeviceStateError(response,
                        "UVC device is not permitted by user: " + device.getName());
                    sendResponse(response);
                    return;
                }

                setMediaRecorders(response, device);
                setResult(response, DConnectMessage.RESULT_OK);
                sendResponse(response);
            }
        });
        return false;
    }

    private static void setMediaRecorders(final Intent response, final UVCDevice device) {
        List<Bundle> recorderList = new ArrayList<Bundle>();
        Bundle recorder = new Bundle();
        setMediaRecorder(recorder, device);
        recorderList.add(recorder);
        setRecorders(response, recorderList);
    }

    private static void setMediaRecorder(final Bundle recorder, final UVCDevice device) {
        setRecorderId(recorder, RECORDER_ID);
        setRecorderName(recorder, device.getName());
        setRecorderState(recorder, device.hasStartedPreview() ? RecorderState.RECORDING
            : RecorderState.INACTIVE);
        setRecorderImageWidth(recorder, device.getPreviewWidth());
        setRecorderImageHeight(recorder, device.getPreviewHeight());
        setRecorderMIMEType(recorder, RECORDER_MIME_TYPE_MJPEG);
        setRecorderConfig(recorder, RECORDER_CONFIG);
    }

    @Override
    protected boolean onGetOptions(final Intent request, final Intent response,
                                   final String serviceId, final String target) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                UVCDevice device = mDeviceMgr.getDevice(serviceId);
                if (device == null) {
                    MessageUtils.setNotFoundServiceError(response);
                    sendResponse(response);
                    return;
                }
                if (target != null && !RECORDER_ID.equals(target)) {
                    MessageUtils.setInvalidRequestParameterError(response,
                        "No such target: " + target);
                    sendResponse(response);
                    return;
                }
                if (!device.isOpen()) {
                    if (!mDeviceMgr.connectDevice(device)) {
                        MessageUtils.setIllegalDeviceStateError(response, "Failed to open UVC device: " + device.getId());
                        sendResponse(response);
                        return;
                    }
                }
                if (!device.canPreview()) {
                    MessageUtils.setNotSupportAttributeError(response, "UVC device does not support MJPEG format: " + device.getId());
                    sendResponse(response);
                    return;
                }

                List<UVCDevice.PreviewOption> options = device.getPreviewOptions();
                if (options != null) {
                    setOptions(response, options);
                    setResult(response, DConnectMessage.RESULT_OK);
                } else {
                    MessageUtils.setUnknownError(response, "Failed to get preview options: " + device.getId());
                }
                sendResponse(response);
            }
        });
        return false;
    }

    private static void setOptions(final Intent response, final List<UVCDevice.PreviewOption> options) {
        if (options.size() > 0) {
            // imageWidth
            Collections.sort(options, new Comparator<UVCDevice.PreviewOption>() {
                @Override
                public int compare(final UVCDevice.PreviewOption op1,
                                   final UVCDevice.PreviewOption op2) {
                    return op1.getWidth() - op2.getWidth();
                }
            });
            int minWidth = options.get(0).getWidth();
            int maxWidth = options.get(options.size() - 1).getWidth();
            setImageWidth(response, minWidth, maxWidth);

            // imageHeight
            Collections.sort(options, new Comparator<UVCDevice.PreviewOption>() {
                @Override
                public int compare(final UVCDevice.PreviewOption op1,
                                   final UVCDevice.PreviewOption op2) {
                    return op1.getHeight() - op2.getHeight();
                }
            });
            int minHeight = options.get(0).getHeight();
            int maxHeight = options.get(options.size() - 1).getHeight();
            setImageHeight(response, minHeight, maxHeight);

            // mimeType
            setMIMEType(response, RECORDER_MIME_TYPE_LIST);
        }
    }

    @Override
    protected boolean onPutOptions(final Intent request, final Intent response,
                                   final String serviceId, final String target,
                                   final Integer imageWidth, final Integer imageHeight,
                                   final String mimeType) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                UVCDevice device = mDeviceMgr.getDevice(serviceId);
                if (device == null) {
                    MessageUtils.setNotFoundServiceError(response);
                    sendResponse(response);
                    return;
                }
                if (!checkOptionParams(target, imageWidth, imageHeight, mimeType, response)) {
                    sendResponse(response);
                    return;
                }
                if (!device.isOpen()) {
                    if (!mDeviceMgr.connectDevice(device)) {
                        MessageUtils.setIllegalDeviceStateError(response, "Failed to open UVC device: " + device.getId());
                        sendResponse(response);
                        return;
                    }
                }
                if (!device.canPreview()) {
                    MessageUtils.setNotSupportAttributeError(response, "UVC device does not support MJPEG format: " + device.getId());
                    sendResponse(response);
                    return;
                }

                if (device.setNearestPreviewSize(imageWidth, imageHeight)) {
                    setResult(response, DConnectMessage.RESULT_OK);
                } else {
                    MessageUtils.setUnknownError(response, "Failed to change preview size: "
                        + imageWidth + " x " + imageHeight);
                }
                sendResponse(response);
            }
        });
        return false;
    }

    private static boolean checkOptionParams(final String target, final Integer imageWidth,
                                             final Integer imageHeight, final String mimeType,
                                             final Intent response) {
        if (target != null && !RECORDER_ID.equals(target)) {
            MessageUtils.setInvalidRequestParameterError(response,
                "No such target: " + target);
            return false;
        }
        if (mimeType == null) {
            MessageUtils.setInvalidRequestParameterError(response, "mimeType is null.");
            return false;
        }
        if (!RECORDER_MIME_TYPE_MJPEG.equals(mimeType)) {
            MessageUtils.setInvalidRequestParameterError(response, mimeType + " is unsupported MIME-Type.");
            return false;
        }
        if (imageWidth == null) {
            MessageUtils.setInvalidRequestParameterError(response, "imageWidth is null.");
            return false;
        }
        if (imageWidth <= 0) {
            MessageUtils.setInvalidRequestParameterError(response, "imageWidth must be positive.");
            return false;
        }
        if (imageHeight == null) {
            MessageUtils.setInvalidRequestParameterError(response, "imageHeight is null.");
            return false;
        }
        if (imageHeight <= 0) {
            MessageUtils.setInvalidRequestParameterError(response, "imageHeight must be positive.");
            return false;
        }
        return true;
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
                device.setPreviewFrameRate(getMaxFrameRate(request));

                PreviewContext context = startMediaServer(device.getId());
                context.mWidth = getWidth(request);
                context.mHeight = getHeight(request);

                if (context.mServer.getUrl() == null) {
                    MessageUtils.setIllegalServerStateError(response, "Failed to start UVC preview server.");
                    sendResponse(response);
                    return;
                }
                if (!device.isOpen()) {
                    if (!mDeviceMgr.connectDevice(device)) {
                        MessageUtils.setIllegalDeviceStateError(response, "Failed to open UVC device: " + device.getId());
                        sendResponse(response);
                        return;
                    }
                }
                if (!device.canPreview()) {
                    MessageUtils.setNotSupportAttributeError(response, "UVC device does not support MJPEG format: " + device.getId());
                    sendResponse(response);
                    return;
                }

                if (device.startPreview()) {
                    setResult(response, DConnectMessage.RESULT_OK);
                    setUri(response, context.mServer.getUrl());
                } else {
                    MessageUtils.setIllegalDeviceStateError(response, "Failed to start the preview of UVC device: " + device.getId());
                }
                sendResponse(response);
            }
        });
        return false;
    }

    private static boolean checkPreviewParams(final Intent request, final Intent response) {
        if (!checkTypeInteger(request, response, PARAM_WIDTH)) {
            return false;
        }
        if (!checkTypeInteger(request, response, PARAM_HEIGHT)) {
            return false;
        }
        if (!checkTypeFloat(request, response, PARAM_MAX_FRAME_RATE)) {
            return false;
        }
        Integer width = getWidth(request);
        Integer height = getHeight(request);
        Float maxFrameRate = getMaxFrameRate(request);
        if (width != null && width <= 0) {
            MessageUtils.setInvalidRequestParameterError(response, PARAM_WIDTH + " must be positive.");
            return false;
        }
        if (height != null && height <= 0) {
            MessageUtils.setInvalidRequestParameterError(response, PARAM_HEIGHT + " must be positive.");
            return false;
        }
        if (maxFrameRate != null && maxFrameRate <= 0.0f) {
            MessageUtils.setInvalidRequestParameterError(response, PARAM_MAX_FRAME_RATE + " must be positive.");
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

    private static Float getMaxFrameRate(final Intent request) {
        return parseFloat(request, PARAM_MAX_FRAME_RATE);
    }

    private static boolean checkTypeInteger(final Intent request, final Intent response,
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
                MessageUtils.setInvalidRequestParameterError(response, paramName + " is invalid format: " + param);
                return false;
            }
        } else {
            MessageUtils.setInvalidRequestParameterError(response, paramName + " is invalid type: " + param);
            return false;
        }
    }

    private static boolean checkTypeFloat(final Intent request, final Intent response,
                                          final String paramName) {
        if (!request.hasExtra(paramName)) {
            return true;
        }
        Object param = request.getExtras().get(paramName);
        if (param instanceof Float) {
            return true;
        } else if (param instanceof String) {
            try {
                Float.parseFloat((String) param);
                return true;
            } catch (NumberFormatException e) {
                MessageUtils.setInvalidRequestParameterError(response, paramName + " is invalid format: " + param);
                return false;
            }
        } else {
            MessageUtils.setInvalidRequestParameterError(response, paramName + " is invalid type: " + param);
            return false;
        }
    }

    private static class PreviewContext {

        Integer mWidth;

        Integer mHeight;

        final MixedReplaceMediaServer mServer;

        final Logger mLogger = Logger.getLogger("uvc.dplugin");

        PreviewContext(final MixedReplaceMediaServer server) {
            if (server == null) {
                throw new IllegalArgumentException();
            }
            mServer = server;
        }

        boolean willResize() {
            return mWidth != null || mHeight != null;
        }

        byte[] resize(final byte[] frame) {
            Bitmap src = BitmapFactory.decodeByteArray(frame, 0, frame.length);
            if (src == null) {
                mLogger.warning("MotionJPEG Frame could not be decoded to bitmap.");
                return null;
            }

            int w = mWidth != null ? mWidth : src.getWidth();
            int h = mHeight != null ? mHeight : src.getHeight();

            Bitmap resizedBitmap = Bitmap.createScaledBitmap(src, w, h, true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] resizedBytes = baos.toByteArray();
            resizedBitmap.recycle();
            return resizedBytes;
        }

    }
}
