/*
 UVCMediaStreamRecordingProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.uvc.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Size;

import org.deviceconnect.android.deviceplugin.uvc.recorder.MediaRecorder;
import org.deviceconnect.android.deviceplugin.uvc.recorder.PreviewServer;
import org.deviceconnect.android.deviceplugin.uvc.recorder.h264.UvcH264Recorder;
import org.deviceconnect.android.deviceplugin.uvc.service.UVCService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.MediaStreamRecordingProfile;
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

    public UVCMediaStreamRecordingProfile() {
        // GET /gotapi/mediaStreamRecording/mediaRecorder
        addApi(new GetApi() {
            @Override
            public String getAttribute() {
                return ATTRIBUTE_MEDIARECORDER;
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                mExecutor.execute(() -> {
                    try {
                        UvcH264Recorder recorder = getUvcRecorder();
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
        });

        // GET /gotapi/mediaStreamRecording/options
        addApi(new GetApi() {
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

                        UvcH264Recorder recorder = getUvcRecorder();
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
        });

        // PUT /gotapi/mediaStreamRecording/options
        addApi(new PutApi() {
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

                        UvcH264Recorder recorder = getUvcRecorder();
                        if (recorder == null) {
                            MessageUtils.setNotFoundServiceError(response);
                            return;
                        }

                        MediaRecorder.Settings settings = recorder.getSettings();

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

                        if (imageWidth != null && !settings.isSupportedPictureSize(imageWidth, imageHeight)) {
                            MessageUtils.setInvalidRequestParameterError(response,
                                    "imageWidth or imageHeight is not support value.");
                            return;
                        }

                        if (previewWidth != null && !settings.isSupportedPreviewSize(previewWidth, previewHeight)) {
                            MessageUtils.setInvalidRequestParameterError(response,
                                    "previewWidth or previewHeight is not support value.");
                            return;
                        }

                        if (imageWidth != null) {
                            settings.setPictureSize(new Size(imageWidth, imageHeight));
                        }

                        if (previewWidth != null) {
                            settings.setPreviewSize(new Size(previewWidth, previewHeight));
                        }

                        if (previewMaxFrameRate != null) {
                            settings.setPreviewMaxFrameRate(previewMaxFrameRate.intValue());
                        }

                        setResult(response, DConnectMessage.RESULT_OK);
                    } finally {
                        sendResponse(response);
                    }
                });
                return false;
            }
        });

        // PUT /gotapi/mediaStreamRecording/preview
        addApi(new PutApi() {
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

                        UvcH264Recorder recorder = getUvcRecorder();
                        if (recorder == null) {
                            MessageUtils.setIllegalDeviceStateError(response, "device is not connected.");
                            return;
                        }

                        List<PreviewServer> servers = recorder.startPreview();
                        if (servers.isEmpty()) {
                            MessageUtils.setIllegalDeviceStateError(response, "Failed to start a preview server.");
                        } else {
                            setResult(response, DConnectMessage.RESULT_OK);
                            setUri(response, getDefaultUri(servers));
                            response.putExtra("streams", createStreams(servers));
                        }
                    } finally {
                        sendResponse(response);
                    }
                });
                return false;
            }
        });

        // DELETE /gotapi/mediaStreamRecording/preview
        addApi(new DeleteApi() {

            @Override
            public String getAttribute() {
                return ATTRIBUTE_PREVIEW;
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                mExecutor.execute(() -> {
                    try {
                        UvcH264Recorder recorder = getUvcRecorder();
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
        });
    }

    private UvcH264Recorder getUvcRecorder() {
        UVCService service = (UVCService) getService();
        if (service != null) {
            return service.getUvcRecorder();
        }
        return null;
    }

    private static void setMediaRecorders(final Intent response, final UvcH264Recorder uvcRecorder) {
        List<Bundle> recorderList = new ArrayList<>();
        Bundle recorder = new Bundle();
        setMediaRecorder(recorder, uvcRecorder);
        recorderList.add(recorder);
        setRecorders(response, recorderList);
    }

    private static void setMediaRecorder(final Bundle recorder, final UvcH264Recorder uvcRecorder) {
        Size previewSize = uvcRecorder.getSettings().getPreviewSize();

        setRecorderId(recorder, uvcRecorder.getId());
        setRecorderName(recorder, uvcRecorder.getName());
        setRecorderState(recorder, uvcRecorder.isPreviewRunning() ? RecorderState.RECORDING
            : RecorderState.INACTIVE);
        setRecorderPreviewWidth(recorder, previewSize.getWidth());
        setRecorderPreviewHeight(recorder, previewSize.getHeight());
        setRecorderPreviewMaxFrameRate(recorder, uvcRecorder.getSettings().getPreviewMaxFrameRate());
        setRecorderMIMEType(recorder, uvcRecorder.getMimeType());
        setRecorderConfig(recorder, "");
    }

    private static void setOptions(final Intent response, final UvcH264Recorder recorder) {
//        List<MediaRecorder.Size> options = recorder.getSupportedPreviewSizes();
//        List<Bundle> previewSizes = new ArrayList<>();
//        for (MediaRecorder.Size option : options) {
//            Bundle size = new Bundle();
//            setWidth(size, option.getWidth());
//            setHeight(size, option.getHeight());
//            previewSizes.add(size);
//        }
//        setPreviewSizes(response, previewSizes);
//        setMIMEType(response, recorder.getSupportedMimeTypes());
    }

    private static void setMIMEType(final Intent response, final List<String> mimeTypes) {
        response.putExtra("mimeType", mimeTypes.toArray(new String[0]));
    }

    private static String getDefaultUri(List<PreviewServer> servers) {
        String defaultUri = null;
        for (PreviewServer server : servers) {
            // Motion-JPEG をデフォルトの値として使用します
            if (defaultUri == null && "video/x-mjpeg".equals(server.getMimeType())) {
                defaultUri = server.getUri();
            }
        }
        return defaultUri != null ? defaultUri : "";
    }

    private static Bundle[] createStreams(List<PreviewServer> servers) {
        List<Bundle> streams = new ArrayList<>();
        for (PreviewServer server : servers) {
            Bundle stream = new Bundle();
            stream.putString("mimeType", server.getMimeType());
            stream.putString("uri", server.getUri());
            streams.add(stream);
        }
        return streams.toArray(new Bundle[0]);
    }
}
