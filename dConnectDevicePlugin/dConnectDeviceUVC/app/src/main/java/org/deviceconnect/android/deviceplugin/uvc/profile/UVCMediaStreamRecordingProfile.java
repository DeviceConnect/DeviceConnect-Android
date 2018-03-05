/*
 UVCMediaStreamRecordingProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.uvc.profile;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import org.deviceconnect.android.deviceplugin.uvc.core.UVCDevice;
import org.deviceconnect.android.deviceplugin.uvc.core.UVCDeviceManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.MediaStreamRecordingProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
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
            stopPreviewServer(device);
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

    private final Map<String, PreviewServerProvider> mPreviewServerProviders = new HashMap<>();

    private PreviewServerProvider getServerProvider(final UVCDevice device) {
        PreviewServerProvider provider;
        synchronized (mPreviewServerProviders) {
            provider = mPreviewServerProviders.get(device.getId());
            if (provider == null) {
                provider = createServerProvider(device);
                mPreviewServerProviders.put(device.getId(), provider);
            }
        }
        return provider;
    }

    private PreviewServerProvider createServerProvider(final UVCDevice device) {
        return new PreviewServerProvider() {

            private final List<PreviewServer> mServers = new ArrayList<>();
            {
                mServers.add(new MJPEGPreviewServer(mDeviceMgr, device));
                mServers.add(new RTSPPreviewServer(getContext(), mDeviceMgr, device));
            }

            @Override
            public List<PreviewServer> getServers() {
                return new ArrayList<>(mServers);
            }

            @Override
            public void stopAll() {
                for (PreviewServer server : getServers()) {
                    server.stop();
                }
            }
        };
    }

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

                        PreviewServerProvider serverProvider = getServerProvider(device);
                        final List<PreviewServer> servers = serverProvider.getServers();
                        final String[] defaultUri = new String[1];
                        final CountDownLatch lock = new CountDownLatch(servers.size());
                        final List<Bundle> streams = new ArrayList<>();
                        for (final PreviewServer server : servers) {
                            server.start(new PreviewServer.OnWebServerStartCallback() {
                                @Override
                                public void onStart(@NonNull String uri) {
                                    if ("video/x-mjpeg".equals(server.getMimeType())) {
                                        defaultUri[0] = uri;
                                    }

                                    Bundle stream = new Bundle();
                                    stream.putString("mimeType", server.getMimeType());
                                    stream.putString("uri", uri);
                                    streams.add(stream);

                                    lock.countDown();
                                }

                                @Override
                                public void onFail() {
                                    lock.countDown();
                                }
                            });
                        }
                        try {
                            lock.await();
                            if (streams.size() > 0) {
                                setResult(response, DConnectMessage.RESULT_OK);
                                setUri(response, defaultUri[0] != null ? defaultUri[0] : "");
                                response.putExtra("streams", streams.toArray(new Bundle[streams.size()]));
                            } else {
                                MessageUtils.setIllegalServerStateError(response, "Failed to start web server.");
                            }
                        } catch (InterruptedException e) {
                            MessageUtils.setIllegalServerStateError(response, "Forced to shutdown request thread.");
                        } finally {
                            sendResponse(response);
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

                        // サーバー停止
                        stopPreviewServer(device);

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

    private void stopPreviewServer(final UVCDevice device) {
        synchronized (mPreviewServerProviders) {
            PreviewServerProvider provider = mPreviewServerProviders.get(device.getId());
            if (provider != null) {
                provider.stopAll();
                mPreviewServerProviders.remove(device.getId());
            }
        }
    }

    public synchronized void stopPreviewAllUVCDevice() {
        List<UVCDevice> deviceList = mDeviceMgr.getDeviceList();
        for (UVCDevice device : deviceList) {
            stopPreviewServer(device);
            device.stopPreview();
        }
    }
}
