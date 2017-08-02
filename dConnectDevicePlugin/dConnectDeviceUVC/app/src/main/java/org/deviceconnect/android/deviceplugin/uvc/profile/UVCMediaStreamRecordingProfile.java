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

import org.deviceconnect.android.deviceplugin.uvc.core.UVCDevice;
import org.deviceconnect.android.deviceplugin.uvc.core.UVCDeviceManager;
import org.deviceconnect.android.deviceplugin.uvc.utils.MixedReplaceMediaServer;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.MediaStreamRecordingProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
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

    private final DConnectApi mGetMediaRecorderApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_MEDIARECORDER;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    UVCDevice device = mDeviceMgr.getDevice(getServiceID(request));
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
    };

    private final DConnectApi mGetOptionsApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_OPTIONS;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    UVCDevice device = mDeviceMgr.getDevice(getServiceID(request));
                    if (device == null) {
                        MessageUtils.setNotFoundServiceError(response);
                        sendResponse(response);
                        return;
                    }
                    String target = getTarget(request);
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

                    setOptions(response, device);
                    setResult(response, DConnectMessage.RESULT_OK);
                    sendResponse(response);
                }
            });
            return false;
        }
    };

    private final DConnectApi mPutOptionsApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_OPTIONS;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        String serviceId = getServiceID(request);
                        Integer imageWidth = getImageWidth(request);
                        Integer imageHeight = getImageHeight(request);
                        Integer previewWidth = getPreviewWidth(request);
                        Integer previewHeight = getPreviewHeight(request);
                        Double previewMaxFrameRate = getPreviewMaxFrameRate(request);

                        UVCDevice device = mDeviceMgr.getDevice(serviceId);
                        if (device == null) {
                            MessageUtils.setNotFoundServiceError(response);
                            return;
                        }
                        if (imageWidth != null && imageHeight == null) {
                            MessageUtils.setNotFoundServiceError(response,
                                "imageHeight must not be null if imageWidth is not null.");
                            return;
                        }
                        if (imageWidth == null && imageHeight != null) {
                            MessageUtils.setNotFoundServiceError(response,
                                "imageWidth must not be null if imageHeight is not null.");
                            return;
                        }
                        if (previewWidth != null && previewHeight == null) {
                            MessageUtils.setNotFoundServiceError(response,
                                "previewHeight must not be null if previewWidth is not null.");
                            return;
                        }
                        if (previewWidth == null && previewHeight != null) {
                            MessageUtils.setNotFoundServiceError(response,
                                "previewWidth must not be null if previewHeight is not null.");
                            return;
                        }
                        if (!device.isOpen()) {
                            if (!mDeviceMgr.connectDevice(device)) {
                                MessageUtils.setIllegalDeviceStateError(response, "Failed to open UVC device: " + device.getId());
                                return;
                            }
                        }
                        if (!device.canPreview()) {
                            MessageUtils.setNotSupportAttributeError(response, "UVC device does not support MJPEG format: " + device.getId());
                            return;
                        }

                        if (previewWidth != null && previewHeight != null) {
                            if (device.setPreviewSize(previewWidth, previewHeight)) {
                                setResult(response, DConnectMessage.RESULT_OK);
                            } else {
                                MessageUtils.setUnknownError(response, "Failed to change preview size: "
                                    + imageWidth + " x " + imageHeight);
                                return;
                            }
                        }
                        if (previewMaxFrameRate != null) {
                            device.setPreviewFrameRate(previewMaxFrameRate);
                        }
                    } finally {
                        sendResponse(response);
                    }
                }
            });
            return false;
        }
    };

    private final DConnectApi mPutPreviewApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_PREVIEW;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        UVCDevice device = mDeviceMgr.getDevice(getServiceID(request));
                        if (device == null) {
                            MessageUtils.setNotFoundServiceError(response);
                            return;
                        }
                        if (!device.isOpen()) {
                            if (!mDeviceMgr.connectDevice(device)) {
                                MessageUtils.setIllegalDeviceStateError(response, "Failed to open UVC device: " + device.getId());
                                return;
                            }
                        }
                        if (!device.canPreview()) {
                            MessageUtils.setNotSupportAttributeError(response, "UVC device does not support MJPEG format: " + device.getId());
                            return;
                        }
                        if (device.startPreview()) {
                            PreviewContext context = startMediaServer(device.getId());
                            if (context.mServer.getUrl() == null) {
                                MessageUtils.setIllegalServerStateError(response, "Failed to start UVC preview server.");
                                return;
                            }
                            context.mWidth = device.getPreviewWidth();
                            context.mHeight = device.getPreviewHeight();

                            setResult(response, DConnectMessage.RESULT_OK);
                            setUri(response, context.mServer.getUrl());
                        } else {
                            MessageUtils.setIllegalDeviceStateError(response, "Failed to start the preview of UVC device: " + device.getId());
                        }
                    } finally {
                        sendResponse(response);
                    }
                }
            });
            return false;
        }
    };

    private final DConnectApi mDeletePreviewApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_PREVIEW;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        UVCDevice device = mDeviceMgr.getDevice(getServiceID(request));
                        if (device == null) {
                            MessageUtils.setNotFoundServiceError(response);
                            return;
                        }

                        device.stopPreview();
                        stopMediaServer(device.getId());
                        setResult(response, DConnectMessage.RESULT_OK);
                    } finally {
                        sendResponse(response);
                    }
                }
            });
            return false;
        }
    };

    public UVCMediaStreamRecordingProfile(final UVCDeviceManager deviceMgr) {
        mDeviceMgr = deviceMgr;
        mDeviceMgr.addPreviewListener(mPreviewListener);
        mDeviceMgr.addConnectionListener(mConnectionListener);

        addApi(mGetMediaRecorderApi);
        addApi(mGetOptionsApi);
        addApi(mPutOptionsApi);
        addApi(mPutPreviewApi);
        addApi(mDeletePreviewApi);
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
        setRecorderPreviewWidth(recorder, device.getPreviewWidth());
        setRecorderPreviewHeight(recorder, device.getPreviewHeight());
        setRecorderPreviewMaxFrameRate(recorder, device.getFrameRate());
        setRecorderMIMEType(recorder, RECORDER_MIME_TYPE_MJPEG);
        setRecorderConfig(recorder, RECORDER_CONFIG);
    }

    private static void setOptions(final Intent response, final UVCDevice device) {
        List<UVCDevice.PreviewOption> options = device.getPreviewOptions();
        if (options != null && options.size() > 0) {
            // previewSizes
            List<Bundle> previewSizes = new ArrayList<Bundle>();
            for (UVCDevice.PreviewOption option : options) {
                Bundle size = new Bundle();
                setWidth(size, option.getWidth());
                setHeight(size, option.getHeight());
                previewSizes.add(size);
            }
            setPreviewSizes(response, previewSizes);
        }

        // mimeType
        setMIMEType(response, RECORDER_MIME_TYPE_LIST);
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

    public synchronized void stopPreviewAllUVCDevice() {
        List<UVCDevice> deviceList = mDeviceMgr.getDeviceList();
        for (UVCDevice device : deviceList) {
            device.stopPreview();
            stopMediaServer(device.getId());
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
            byte[] resizedBytes = null;
            try {
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
                resizedBytes = baos.toByteArray();
                resizedBitmap.recycle();
            } catch (OutOfMemoryError e) {
                mLogger.warning("MotionJPEG Frame could not be decoded to bitmap for: " + e.getMessage());
            }
            return resizedBytes;
        }

    }
}
