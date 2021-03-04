/*
 UVCMediaStreamRecordingProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.uvc.profile;

import android.content.Intent;
import android.graphics.Rect;
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
import org.deviceconnect.android.deviceplugin.uvc.util.CapabilityUtil;
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

                UVCService service = (UVCService) getService();
                if (service == null) {
                    MessageUtils.setNotFoundServiceError(response, "service is not found.");
                    return true;
                }

                if (!service.isOnline()) {
                    MessageUtils.setIllegalDeviceStateError(response, "device is not connected.");
                    return true;
                }

                UvcRecorder recorder = getUvcRecorderByTarget(target);
                if (recorder == null) {
                    MessageUtils.setNotFoundServiceError(response, target + " is not found.");
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
                Integer previewClipLeft = parseInteger(request, "previewClipLeft");
                Integer previewClipTop = parseInteger(request, "previewClipTop");
                Integer previewClipRight = parseInteger(request, "previewClipRight");
                Integer previewClipBottom = parseInteger(request, "previewClipBottom");
                Boolean previewClipReset = parseBoolean(request, "previewClipReset");
                MediaRecorder.ProfileLevel profileLevel = null;
                Rect drawingRect = null;

                UVCService service = (UVCService) getService();
                if (service == null) {
                    MessageUtils.setNotFoundServiceError(response, "service is not found.");
                    return true;
                }

                if (!service.isOnline()) {
                    MessageUtils.setIllegalDeviceStateError(response, "device is not connected.");
                    return true;
                }

                UvcRecorder recorder = getUvcRecorderByTarget(target);
                if (recorder == null) {
                    MessageUtils.setNotFoundServiceError(response, target + " is not found.");
                    return true;
                }

                MediaRecorder.Settings settings = recorder.getSettings();

                // 値の妥当性チェック

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

                if (previewClipLeft != null || previewClipTop != null
                        || previewClipRight != null || previewClipBottom != null) {

                    if (previewClipLeft == null) {
                        MessageUtils.setInvalidRequestParameterError(response,
                                "previewClipLeft is not set.");
                        return true;
                    }

                    if (previewClipTop == null) {
                        MessageUtils.setInvalidRequestParameterError(response,
                                "previewClipTop is not set.");
                        return true;
                    }

                    if (previewClipRight == null) {
                        MessageUtils.setInvalidRequestParameterError(response,
                                "previewClipRight is not set.");
                        return true;
                    }

                    if (previewClipBottom == null) {
                        MessageUtils.setInvalidRequestParameterError(response,
                                "previewClipBottom is not set.");
                        return true;
                    }

                    if (previewClipLeft < 0) {
                        MessageUtils.setInvalidRequestParameterError(response,
                                "previewClipLeft cannot set a negative value.");
                        return true;
                    }

                    if (previewClipBottom < 0) {
                        MessageUtils.setInvalidRequestParameterError(response,
                                "previewClipBottom cannot set a negative value.");
                        return true;
                    }

                    if (previewClipLeft >= previewClipRight) {
                        MessageUtils.setInvalidRequestParameterError(response,
                                "previewClipLeft is larger than previewClipRight.");
                        return true;
                    }

                    if (previewClipTop >= previewClipBottom) {
                        MessageUtils.setInvalidRequestParameterError(response,
                                "previewClipTop is larger than previewClipBottom.");
                        return true;
                    }

                    drawingRect = new Rect(previewClipLeft, previewClipTop, previewClipRight, previewClipBottom);
                }

                // 設定

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

                if (previewClipReset != null && previewClipReset) {
                    settings.setDrawingRange(null);
                } else if (drawingRect != null) {
                    settings.setDrawingRange(drawingRect);
                }

                try {
                    recorder.onConfigChange();
                } catch (Exception e) {
                    MessageUtils.setIllegalDeviceStateError(response, "Failed to change a config.");
                    return true;
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

                UVCService service = (UVCService) getService();
                if (service == null) {
                    MessageUtils.setNotFoundServiceError(response, "service is not found.");
                    return true;
                }

                if (!service.isOnline()) {
                    MessageUtils.setIllegalDeviceStateError(response, "device is not connected.");
                    return true;
                }

                UvcRecorder recorder = getUvcRecorderByTarget(target);
                if (recorder == null) {
                    MessageUtils.setNotFoundServiceError(response, target + " is not found.");
                    return true;
                }

                recorder.requestPermission(new MediaRecorder.PermissionCallback() {
                    @Override
                    public void onAllowed() {
                        List<PreviewServer> servers = recorder.startPreview();
                        if (servers.isEmpty()) {
                            MessageUtils.setIllegalDeviceStateError(response,
                                    "Failed to start a preview server.");
                        } else {
                            setResult(response, DConnectMessage.RESULT_OK);
                            setUri(response, getDefaultUri(servers));
                            setStreams(response, servers);
                        }
                        sendResponse(response);
                    }

                    @Override
                    public void onDisallowed() {
                        MessageUtils.setUnknownError(response,
                                "Permission for camera is not granted.");
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
                String target = getTarget(request);

                UVCService service = (UVCService) getService();
                if (service == null) {
                    MessageUtils.setNotFoundServiceError(response, "service is not found.");
                    return true;
                }

                if (!service.isOnline()) {
                    MessageUtils.setIllegalDeviceStateError(response, "device is not connected.");
                    return true;
                }

                UvcRecorder recorder = getUvcRecorderByTarget(target);
                if (recorder == null) {
                    MessageUtils.setNotFoundServiceError(response, target + " is not found.");
                    return true;
                }

                recorder.requestPermission(new MediaRecorder.PermissionCallback() {
                    @Override
                    public void onAllowed() {
                        recorder.stopPreview();
                        setResult(response, DConnectMessage.RESULT_OK);
                        sendResponse(response);
                    }

                    @Override
                    public void onDisallowed() {
                        MessageUtils.setUnknownError(response,
                                "Permission for camera is not granted.");
                        sendResponse(response);
                    }
                });
                return false;
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

                UVCService service = (UVCService) getService();
                if (service == null) {
                    MessageUtils.setNotFoundServiceError(response, "service is not found.");
                    return true;
                }

                if (!service.isOnline()) {
                    MessageUtils.setIllegalDeviceStateError(response, "device is not connected.");
                    return true;
                }

                UvcRecorder recorder = getUvcRecorderByTarget(target);
                if (recorder == null) {
                    MessageUtils.setNotFoundServiceError(response, target + " is not found.");
                    return true;
                }

                if (recorder.isBroadcasterRunning()) {
                    MessageUtils.setIllegalDeviceStateError(response, "broadcast is already running.");
                    return true;
                }

                recorder.requestPermission(new MediaRecorder.PermissionCallback() {
                    @Override
                    public void onAllowed() {
                        Broadcaster b = recorder.startBroadcaster(broadcastURI);
                        if (b != null) {
                            setResult(response, DConnectMessage.RESULT_OK);
                        } else {
                            MessageUtils.setIllegalServerStateError(response,
                                    "Failed to start a broadcast.");
                        }
                        sendResponse(response);
                    }

                    @Override
                    public void onDisallowed() {
                        MessageUtils.setUnknownError(response,
                                "Permission for camera is not granted.");
                        sendResponse(response);
                    }
                });
                return false;
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

                UVCService service = (UVCService) getService();
                if (service == null) {
                    MessageUtils.setNotFoundServiceError(response, "service is not found.");
                    return true;
                }

                if (!service.isOnline()) {
                    MessageUtils.setIllegalDeviceStateError(response, "device is not connected.");
                    return true;
                }

                UvcRecorder recorder = getUvcRecorderByTarget(target);
                if (recorder == null) {
                    MessageUtils.setNotFoundServiceError(response, target + " is not found.");
                    return true;
                }

                recorder.requestPermission(new MediaRecorder.PermissionCallback() {
                    @Override
                    public void onAllowed() {
                        recorder.stopBroadcaster();

                        setResult(response, DConnectMessage.RESULT_OK);
                        sendResponse(response);
                    }

                    @Override
                    public void onDisallowed() {
                        MessageUtils.setUnknownError(response,
                                "Permission for camera is not granted.");
                        sendResponse(response);
                    }
                });
                return false;
            }
        });
    }

    private UvcRecorder getUvcRecorder() {
        UVCService service = (UVCService) getService();
        if (service != null) {
            return service.getDefaultRecorder();
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

    private static void setMediaRecorder(final Bundle info, final UvcRecorder recorder) {
        MediaRecorder.Settings settings = recorder.getSettings();
        Size previewSize = settings.getPreviewSize();

        setRecorderId(info, recorder.getId());
        setRecorderName(info, recorder.getName());
        setRecorderState(info, recorder.isPreviewRunning() ? RecorderState.RECORDING : RecorderState.INACTIVE);
        setRecorderPreviewWidth(info, previewSize.getWidth());
        setRecorderPreviewHeight(info, previewSize.getHeight());
        setRecorderPreviewMaxFrameRate(info, settings.getPreviewMaxFrameRate());
        setRecorderMIMEType(info, recorder.getMimeType());
        setRecorderConfig(info, "");
        info.putInt("previewBitRate", settings.getPreviewBitRate() / 1024);
        info.putInt("previewKeyFrameInterval", settings.getPreviewKeyFrameInterval());
        info.putString("previewEncoder", settings.getPreviewEncoder());
        info.putString("previewEncoder", settings.getPreviewEncoder());
        info.putFloat("previewJpegQuality", settings.getPreviewQuality() / 100.0f);
        MediaRecorder.ProfileLevel pl = settings.getProfileLevel();
        if (pl != null) {
            switch (MediaRecorder.VideoEncoderName.nameOf(settings.getPreviewEncoder())) {
                case H264:
                    info.putString("previewProfile", H264Profile.valueOf(pl.getProfile()).getName());
                    info.putString("previewLevel", H264Level.valueOf(pl.getLevel()).getName());
                    break;
                case H265:
                    info.putString("previewProfile", H265Profile.valueOf(pl.getProfile()).getName());
                    info.putString("previewLevel", H265Level.valueOf(pl.getLevel()).getName());
                    break;
            }
        }
        Bundle status = new Bundle();
        status.putBoolean("preview", recorder.isPreviewRunning());
        status.putBoolean("broadcast", recorder.isBroadcasterRunning());
//        status.putBoolean("recording", recorder.getState() == MediaRecorder.State.RECORDING);
        info.putParcelable("status", status);

        // 切り抜き設定
        Rect rect = settings.getDrawingRange();
        if (rect != null) {
            Bundle drawingRect = new Bundle();
            drawingRect.putInt("left", rect.left);
            drawingRect.putInt("top", rect.top);
            drawingRect.putInt("right", rect.right);
            drawingRect.putInt("bottom", rect.bottom);
            info.putBundle("previewClip", drawingRect);
        }
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
        List<Bundle> encoders = new ArrayList<>();
        for (String name : recorder.getSettings().getSupportedVideoEncoders()) {
            MediaRecorder.VideoEncoderName encoderName = MediaRecorder.VideoEncoderName.nameOf(name);
            Bundle encoder = new Bundle();
            encoder.putString("name", name);
            encoder.putParcelableArray("profileLevel", getProfileLevels(encoderName));
            encoders.add(encoder);
        }
        response.putExtra("encoder", encoders.toArray(new Bundle[0]));
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

    /**
     * エンコーダがサポートしているプロファイルとレベルを格納した Bundle の配列を取得します.
     *
     * @param encoderName エンコーダ
     * @return プロファイルとレベルを格納した Bundle の配列
     */
    private static Bundle[] getProfileLevels(MediaRecorder.VideoEncoderName encoderName) {
        List<Bundle> list = new ArrayList<>();
        for (MediaRecorder.ProfileLevel pl : CapabilityUtil.getSupportedProfileLevel(encoderName.getMimeType())) {
            switch (encoderName) {
                case H264: {
                    H264Profile p = H264Profile.valueOf(pl.getProfile());
                    H264Level l = H264Level.valueOf(pl.getLevel());
                    if (p != null && l != null) {
                        Bundle encoder = new Bundle();
                        encoder.putString("profile", p.getName());
                        encoder.putString("level", l.getName());
                        list.add(encoder);
                    }
                }   break;
                case H265: {
                    H265Profile p = H265Profile.valueOf(pl.getProfile());
                    H265Level l = H265Level.valueOf(pl.getLevel());
                    if (p != null && l != null) {
                        Bundle encoder = new Bundle();
                        encoder.putString("profile", p.getName());
                        encoder.putString("level", l.getName());
                        list.add(encoder);
                    }
                }   break;
            }
        }
        return list.toArray(new Bundle[0]);
    }
}
