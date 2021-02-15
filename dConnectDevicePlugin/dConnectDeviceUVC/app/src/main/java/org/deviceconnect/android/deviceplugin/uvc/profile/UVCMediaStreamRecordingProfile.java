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

import org.deviceconnect.android.deviceplugin.uvc.profile.utils.H264Level;
import org.deviceconnect.android.deviceplugin.uvc.profile.utils.H264Profile;
import org.deviceconnect.android.deviceplugin.uvc.profile.utils.H265Level;
import org.deviceconnect.android.deviceplugin.uvc.profile.utils.H265Profile;
import org.deviceconnect.android.deviceplugin.uvc.recorder.Broadcaster;
import org.deviceconnect.android.deviceplugin.uvc.recorder.MediaRecorder;
import org.deviceconnect.android.deviceplugin.uvc.recorder.PreviewServer;
import org.deviceconnect.android.deviceplugin.uvc.recorder.uvc.UvcRecorder;
import org.deviceconnect.android.deviceplugin.uvc.service.UVCService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.MediaStreamRecordingProfile;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * UVC MediaStream Recording Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class UVCMediaStreamRecordingProfile extends MediaStreamRecordingProfile {

    public UVCMediaStreamRecordingProfile() {
        // GET /gotapi/mediaStreamRecording/mediaRecorder
        addApi(new GetApi() {
            @Override
            public String getAttribute() {
                return ATTRIBUTE_MEDIARECORDER;
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                UVCService service = (UVCService) getService();

                if (service == null) {
                    MessageUtils.setNotFoundServiceError(response, "service is not found.");
                    return true;
                }

                if (!getService().isOnline()) {
                    MessageUtils.setIllegalDeviceStateError(response, "device is not connected.");
                    return true;
                }

                setResult(response, DConnectMessage.RESULT_OK);
                setMediaRecorders(response, service.getUvcRecorderList());
                return true;
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
                String target = getTarget(request);

                UvcRecorder recorder = getUvcRecorderByTarget(target);
                if (recorder == null) {
                    MessageUtils.setNotFoundServiceError(response, target + " is not found.");
                    return true;
                }

                if (!getService().isOnline()) {
                    MessageUtils.setIllegalDeviceStateError(response, "device is not connected.");
                    return true;
                }

                setResult(response, DConnectMessage.RESULT_OK);
                setOptions(response, recorder);
                return true;
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
                String target = getTarget(request);
                Integer imageWidth = getImageWidth(request);
                Integer imageHeight = getImageHeight(request);
                Integer previewWidth = getPreviewWidth(request);
                Integer previewHeight = getPreviewHeight(request);
                Double previewMaxFrameRate = getPreviewMaxFrameRate(request);
                Integer previewBitRate = parseInteger(request, "previewBitRate");
                Integer previewKeyFrameInterval = parseInteger(request, "previewKeyFrameInterval");
                String previewEncoder = request.getStringExtra("previewEncoder");
                String previewProfile = request.getStringExtra("previewProfile");
                String previewLevel = request.getStringExtra("previewLevel");
                Integer previewIntraRefresh = parseInteger("previewIntraRefresh");
                Double previewJpegQuality = parseDouble(request, "previewJpegQuality");
                MediaRecorder.ProfileLevel profileLevel = null;

                UvcRecorder recorder = getUvcRecorderByTarget(target);
                if (recorder == null) {
                    MessageUtils.setNotFoundServiceError(response, target + " is not found.");
                    return true;
                }

                MediaRecorder.Settings settings = recorder.getSettings();

                if (!getService().isOnline()) {
                    MessageUtils.setIllegalDeviceStateError(response, "device is not connected.");
                    return true;
                }

                if (imageWidth == null && imageHeight != null ||
                        imageWidth != null && imageHeight == null) {
                    MessageUtils.setInvalidRequestParameterError(response,
                            "imageWidth or imageHeight is not set.");
                    return true;
                }

                if (previewWidth == null && previewHeight != null ||
                        previewWidth != null && previewHeight == null) {
                    MessageUtils.setInvalidRequestParameterError(response,
                            "previewWidth or previewHeight is not set.");
                    return true;
                }

                if (imageWidth != null && !settings.isSupportedPictureSize(imageWidth, imageHeight)) {
                    MessageUtils.setInvalidRequestParameterError(response,
                            "imageWidth or imageHeight is not support value.");
                    return true;
                }

                if (previewWidth != null && !settings.isSupportedPreviewSize(previewWidth, previewHeight)) {
                    MessageUtils.setInvalidRequestParameterError(response,
                            "previewWidth or previewHeight is not support value.");
                    return true;
                }

                if (previewEncoder != null && !settings.isSupportedVideoEncoder(previewEncoder)) {
                    MessageUtils.setInvalidRequestParameterError(response,
                            "Unsupported preview encoder: " + previewEncoder);
                    return true;
                }

                if (previewProfile != null || previewLevel != null) {
                    if (previewProfile == null) {
                        MessageUtils.setInvalidRequestParameterError(response,
                                "previewProfile is not set.");
                        return true;
                    }

                    if (previewLevel == null) {
                        MessageUtils.setInvalidRequestParameterError(response,
                                "previewLevel is not set.");
                        return true;
                    }

                    MediaRecorder.VideoEncoderName encoderName = settings.getPreviewEncoderName();
                    if (previewEncoder != null) {
                        encoderName = MediaRecorder.VideoEncoderName.nameOf(previewEncoder);
                    }
                    switch (encoderName) {
                        case H264: {
                            H264Profile p = H264Profile.nameOf(previewProfile);
                            H264Level l = H264Level.nameOf(previewLevel);
                            if (p == null || l == null || !settings.isSupportedProfileLevel(p.getValue(), l.getValue())) {
                                MessageUtils.setInvalidRequestParameterError(response,
                                        "Unsupported preview profile and level: " + previewProfile + " - " + previewLevel);
                                return true;
                            }
                            profileLevel = new MediaRecorder.ProfileLevel(p.getValue(), l.getValue());
                        }   break;
                        case H265: {
                            H265Profile p = H265Profile.nameOf(previewProfile);
                            H265Level l = H265Level.nameOf(previewLevel);
                            if (p == null || l == null || !settings.isSupportedProfileLevel(p.getValue(), l.getValue())) {
                                MessageUtils.setInvalidRequestParameterError(response,
                                        "Unsupported preview profile and level: " + previewProfile + " - " + previewLevel);
                                return true;
                            }
                            profileLevel = new MediaRecorder.ProfileLevel(p.getValue(), l.getValue());
                        }   break;
                    }
                }

                if (previewJpegQuality != null && (previewJpegQuality < 0.0 || previewJpegQuality > 1.0)) {
                    MessageUtils.setInvalidRequestParameterError(response,
                            "previewJpegQuality is invalid. value=" + previewJpegQuality);
                    return true;
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

                if (previewBitRate != null) {
                    settings.setPreviewBitRate(previewBitRate * 1024);
                }

                if (previewKeyFrameInterval != null) {
                    settings.setPreviewKeyFrameInterval(previewKeyFrameInterval);
                }

                if (previewEncoder != null) {
                    settings.setPreviewEncoder(previewEncoder);
                    // エンコーダが切り替えられた場合は、プロファイル・レベルは設定無しにする
                    settings.setProfileLevel(null);
                }

                if (profileLevel != null) {
                    settings.setProfileLevel(profileLevel);
                }

                if (previewIntraRefresh != null) {
                    settings.setIntraRefresh(previewIntraRefresh);
                }

                if (previewJpegQuality != null) {
                    settings.setPreviewQuality((int) (previewJpegQuality * 100));
                }

                setResult(response, DConnectMessage.RESULT_OK);
                return true;
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
                String target = getTarget(request);

                UvcRecorder recorder = getUvcRecorderByTarget(target);
                if (recorder == null) {
                    MessageUtils.setNotFoundServiceError(response, target + " is not found.");
                    return true;
                }

                if (!getService().isOnline()) {
                    MessageUtils.setIllegalDeviceStateError(response, "device is not connected.");
                    return true;
                }

                List<PreviewServer> servers = recorder.startPreview();
                if (servers.isEmpty()) {
                    MessageUtils.setIllegalDeviceStateError(response, "Failed to start a preview server.");
                } else {
                    setResult(response, DConnectMessage.RESULT_OK);
                    setUri(response, getDefaultUri(servers));
                    setStreams(response, servers);
                }
                return true;
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
                String target = getTarget(request);

                UvcRecorder recorder = getUvcRecorderByTarget(target);
                if (recorder == null) {
                    MessageUtils.setNotFoundServiceError(response, target + " is not found.");
                    return true;
                }

                if (!getService().isOnline()) {
                    MessageUtils.setIllegalDeviceStateError(response, "device is not connected.");
                    return true;
                }

                recorder.stopPreview();

                setResult(response, DConnectMessage.RESULT_OK);
                return true;
            }
        });

        // PUT /gotapi/mediaStreamRecording/broadcast
        addApi(new PutApi() {
            @Override
            public String getAttribute() {
                return "broadcast";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String target = getTarget(request);
                String broadcastURI = request.getStringExtra("uri");

                if (broadcastURI == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "broadcastURI ");
                    return true;
                }

                UvcRecorder recorder = getUvcRecorderByTarget(target);
                if (recorder == null) {
                    MessageUtils.setNotFoundServiceError(response, target + " is not found.");
                    return true;
                }

                if (!getService().isOnline()) {
                    MessageUtils.setIllegalDeviceStateError(response, "device is not connected.");
                    return true;
                }

                if (recorder.isBroadcasterRunning()) {
                    MessageUtils.setIllegalDeviceStateError(response, "broadcast is already running.");
                    return true;
                }

                Broadcaster b = recorder.startBroadcaster(broadcastURI);
                if (b != null) {
                    setResult(response, DConnectMessage.RESULT_OK);
                } else {
                    MessageUtils.setIllegalServerStateError(response, "Failed to start a broadcast.");
                }
                return true;
            }
        });

        // DELETE /gotapi/mediaStreamRecording/broadcast
        addApi(new DeleteApi() {
            @Override
            public String getAttribute() {
                return "broadcast";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String target = getTarget(request);

                UvcRecorder recorder = getUvcRecorderByTarget(target);
                if (recorder == null) {
                    MessageUtils.setNotFoundServiceError(response, target + " is not found.");
                    return true;
                }

                if (!getService().isOnline()) {
                    MessageUtils.setIllegalDeviceStateError(response, "device is not connected.");
                    return true;
                }

                recorder.stopBroadcaster();
                return false;
            }
        });
    }

    private UvcRecorder getUvcRecorder() {
        UVCService service = (UVCService) getService();
        if (service != null) {
            return service.getUvcRecorderList().get(0);
        }
        return null;
    }

    private UvcRecorder getUvcRecorderByTarget(String target) {
        if (target == null) {
            return getUvcRecorder();
        }

        UVCService service = (UVCService) getService();
        if (service != null) {
            return service.findUvcRecorderById(target);
        }
        return null;
    }

    private static void setMediaRecorders(final Intent response, final List<UvcRecorder> uvcRecorders) {
        List<Bundle> recorderList = new ArrayList<>();
        for (UvcRecorder uvcRecorder : uvcRecorders) {
            Bundle recorder = new Bundle();
            setMediaRecorder(recorder, uvcRecorder);
            recorderList.add(recorder);
        }
        setRecorders(response, recorderList);
    }

    private static void setMediaRecorder(final Bundle recorder, final UvcRecorder uvcRecorder) {
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

    private static void setOptions(final Intent response, final UvcRecorder recorder) {
        List<Size> options = recorder.getSettings().getSupportedPreviewSizes();
        List<Bundle> previewSizes = new ArrayList<>();
        for (Size option : options) {
            Bundle size = new Bundle();
            setWidth(size, option.getWidth());
            setHeight(size, option.getHeight());
            previewSizes.add(size);
        }
        setPreviewSizes(response, previewSizes);
        setMIMEType(response, recorder.getServerProvider().getSupportedMimeType());
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

    private static void setStreams(Intent response, List<PreviewServer> servers) {
        response.putExtra("streams", createStreams(servers));
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
