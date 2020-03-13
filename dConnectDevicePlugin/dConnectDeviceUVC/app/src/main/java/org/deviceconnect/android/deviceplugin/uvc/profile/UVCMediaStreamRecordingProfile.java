/*
 UVCMediaStreamRecordingProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.uvc.profile;


import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.uvc.recorder.MediaRecorder;
import org.deviceconnect.android.deviceplugin.uvc.recorder.UVCRecorder;
import org.deviceconnect.android.deviceplugin.uvc.recorder.preview.PreviewServer;
import org.deviceconnect.android.deviceplugin.uvc.service.UVCService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.MediaStreamRecordingProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * UVC MediaStream Recording Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class UVCMediaStreamRecordingProfile extends MediaStreamRecordingProfile {
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private final DConnectApi mGetMediaRecorderApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_MEDIARECORDER;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            mExecutor.execute(() -> {
                try {
                    UVCRecorder recorder = getUVCRecorder();
                    if (recorder == null) {
                        MessageUtils.setNotFoundServiceError(response);
                        return;
                    }

                    if (!getService().isOnline()) {
                        MessageUtils.setIllegalDeviceStateError(response, "device is not connected.");
                        return;
                    }

                    setMediaRecorders(response, recorder);
                    setResult(response, DConnectMessage.RESULT_OK);
                } finally {
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
            mExecutor.execute(() -> {
                try {
                    if (!getService().isOnline()) {
                        MessageUtils.setIllegalDeviceStateError(response, "device is not connected.");
                        return;
                    }

                    UVCRecorder recorder = getUVCRecorder();
                    if (recorder == null) {
                        MessageUtils.setNotFoundServiceError(response);
                        return;
                    }

                    setOptions(response, recorder);
                    setResult(response, DConnectMessage.RESULT_OK);
                } finally {
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
            mExecutor.execute(() -> {
                try {
                    Integer imageWidth = getImageWidth(request);
                    Integer imageHeight = getImageHeight(request);
                    Integer previewWidth = getPreviewWidth(request);
                    Integer previewHeight = getPreviewHeight(request);
                    Double previewMaxFrameRate = getPreviewMaxFrameRate(request);

                    UVCRecorder recorder = getUVCRecorder();
                    if (recorder == null) {
                        MessageUtils.setNotFoundServiceError(response);
                        return;
                    }

                    if (!getService().isOnline()) {
                        MessageUtils.setIllegalDeviceStateError(response, "device is not connected.");
                        return;
                    }

                    if (imageWidth == null && imageHeight != null ||
                            imageWidth != null && imageHeight == null) {
                        MessageUtils.setInvalidRequestParameterError(response,
                            "imageWidth or imageHeight is not set.");
                        return;
                    }

                    if (previewWidth == null && previewHeight != null ||
                            previewWidth != null && previewHeight == null) {
                        MessageUtils.setInvalidRequestParameterError(response,
                            "previewWidth or previewHeight is not set.");
                        return;
                    }

                    if (imageWidth != null && imageHeight != null) {
                        recorder.setPictureSize(new MediaRecorder.Size(imageWidth, imageHeight));
                    }

                    if (previewWidth != null && previewHeight != null) {
                        recorder.setPreviewSize(new MediaRecorder.Size(previewWidth, previewHeight));
                    }

                    if (previewMaxFrameRate != null) {
                        recorder.setMaxFrameRate(previewMaxFrameRate);
                    }
                } finally {
                    sendResponse(response);
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
            mExecutor.execute(() -> {
                try {
                    if (!getService().isOnline()) {
                        MessageUtils.setIllegalDeviceStateError(response, "device is not connected.");
                        return;
                    }

                    UVCRecorder recorder = getUVCRecorder();
                    List<PreviewServer> servers = recorder.startPreview();
                    if (servers.isEmpty()) {
                        MessageUtils.setIllegalDeviceStateError(response, "Failed to start a preview server.");
                    } else {
                        String defaultUri = null;
                        List<Bundle> streams = new ArrayList<>();
                        for (PreviewServer server : servers) {
                            // Motion-JPEG をデフォルトの値として使用します
                            if ("video/x-mjpeg".equals(server.getMimeType())) {
                                defaultUri = server.getUrl();
                            }

                            Bundle stream = new Bundle();
                            stream.putString("mimeType", server.getMimeType());
                            stream.putString("uri", server.getUrl());
                            streams.add(stream);
                        }
                        setResult(response, DConnectMessage.RESULT_OK);
                        setUri(response, defaultUri != null ? defaultUri : "");
                        response.putExtra("streams", streams.toArray(new Bundle[streams.size()]));
                    }
                } finally {
                    sendResponse(response);
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
            mExecutor.execute(() -> {
                try {
                    UVCRecorder recorder = getUVCRecorder();
                    if (recorder != null) {
                        recorder.stopPreview();
                    }

                    if (!getService().isOnline()) {
                        MessageUtils.setIllegalDeviceStateError(response, "device is not connected.");
                        return;
                    }

                    setResult(response, DConnectMessage.RESULT_OK);
                } finally {
                    sendResponse(response);
                }
            });
            return false;
        }
    };

    public UVCMediaStreamRecordingProfile() {
        addApi(mGetMediaRecorderApi);
        addApi(mGetOptionsApi);
        addApi(mPutOptionsApi);
        addApi(mPutPreviewApi);
        addApi(mDeletePreviewApi);
    }

    private UVCRecorder getUVCRecorder() {
        UVCService service = (UVCService) getService();
        return service != null ? service.getUVCRecorder() : null;
    }

    private static void setMediaRecorders(final Intent response, final UVCRecorder uvcRecorder) {
        List<Bundle> recorderList = new ArrayList<>();
        Bundle recorder = new Bundle();
        setMediaRecorder(recorder, uvcRecorder);
        recorderList.add(recorder);
        setRecorders(response, recorderList);
    }

    private static void setMediaRecorder(final Bundle recorder, final UVCRecorder uvcRecorder) {
        MediaRecorder.Size previewSize = uvcRecorder.getPreviewSize();

        setRecorderId(recorder, uvcRecorder.getId());
        setRecorderName(recorder, uvcRecorder.getName());
        setRecorderState(recorder, uvcRecorder.isStartedPreview() ? RecorderState.RECORDING
            : RecorderState.INACTIVE);
        setRecorderPreviewWidth(recorder, previewSize.getWidth());
        setRecorderPreviewHeight(recorder, previewSize.getHeight());
        setRecorderPreviewMaxFrameRate(recorder, uvcRecorder.getMaxFrameRate());
        setRecorderMIMEType(recorder, uvcRecorder.getMimeType());
        setRecorderConfig(recorder, "");
    }

    private static void setOptions(final Intent response, final UVCRecorder recorder) {
        List<MediaRecorder.Size> options = recorder.getSupportedPreviewSizes();
        List<Bundle> previewSizes = new ArrayList<>();
        for (MediaRecorder.Size option : options) {
            Bundle size = new Bundle();
            setWidth(size, option.getWidth());
            setHeight(size, option.getHeight());
            previewSizes.add(size);
        }
        setPreviewSizes(response, previewSizes);
        setMIMEType(response.getExtras(), recorder.getSupportedMimeTypes());
    }
}
