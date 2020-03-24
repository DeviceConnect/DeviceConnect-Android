/*
 HostMediaStreamingRecordingProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.host.profile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.host.mediaplayer.VideoConst;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDevicePhotoRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceStreamRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorderManager;
import org.deviceconnect.android.deviceplugin.host.recorder.PreviewServer;
import org.deviceconnect.android.deviceplugin.host.recorder.PreviewServerProvider;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.MediaStreamRecordingProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.android.provider.FileManager;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static org.deviceconnect.android.deviceplugin.host.mediaplayer.VideoConst.SEND_VIDEO_TO_HOSTDP;

/**
 * MediaStream Recording Profile.
 *
 * @author NTT DOCOMO, INC.
 */
@SuppressWarnings("deprecation")
public class HostMediaStreamingRecordingProfile extends MediaStreamRecordingProfile {

    /**
     * レコーダー管理クラス.
     */
    private final HostMediaRecorderManager mRecorderMgr;

    /**
     * ファイル管理クラス.
     */
    private final FileManager mFileManager;

    /**
     * KeyEventProfileActivity からの KeyEvent を中継する Broadcast Receiver.
     */
    private BroadcastReceiver mAudioEventBR = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (intent.getAction().equals(SEND_VIDEO_TO_HOSTDP)) {

                String serviceId = intent.getStringExtra(VideoConst.EXTRA_SERVICE_ID);
                HostMediaRecorder.RecorderState state =
                        (HostMediaRecorder.RecorderState) intent.getSerializableExtra(VideoConst.EXTRA_VIDEO_RECORDER_STATE);
                Uri uri = intent.getParcelableExtra(VideoConst.EXTRA_URI);
                String path = intent.getStringExtra(VideoConst.EXTRA_FILE_NAME);
                String u = uri != null ? uri.toString() : null;
                mRecorderMgr.sendEventForRecordingChange(serviceId, state, u, path, "audio/aac", "");
            }
        }
    };

    private final DConnectApi mGetMediaRecorderApi = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_MEDIARECORDER;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String target = getTarget(request);

            final HostMediaRecorder recorder = mRecorderMgr.getRecorder(target);
            if (recorder == null) {
                MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
                return true;
            }

            recorder.requestPermission(new HostMediaRecorder.PermissionCallback() {
                @Override
                public void onAllowed() {
                    List<Bundle> recorders = new LinkedList<>();
                    for (HostMediaRecorder recorder : mRecorderMgr.getRecorders()) {
                        Bundle info = new Bundle();
                        setRecorderId(info, recorder.getId());
                        setRecorderName(info, recorder.getName());
                        setRecorderMIMEType(info, recorder.getMimeType());
                        switch (recorder.getState()) {
                            case RECORDING:
                                setRecorderState(info, RecorderState.RECORDING);
                                break;
                            default:
                                setRecorderState(info, RecorderState.INACTIVE);
                                break;
                        }

                        if (recorder.getMimeType().startsWith("image/") || recorder.getMimeType().startsWith("video/")) {
                            // 静止画の設定
                            HostMediaRecorder.PictureSize pictureSize = recorder.getPictureSize();
                            if (pictureSize != null) {
                                setRecorderImageWidth(info, pictureSize.getWidth());
                                setRecorderImageHeight(info, pictureSize.getHeight());
                            }

                            HostMediaRecorder.PictureSize previewSize = recorder.getPreviewSize();
                            if (previewSize != null) {
                                setRecorderPreviewWidth(info, previewSize.getWidth());
                                setRecorderPreviewHeight(info, previewSize.getHeight());
                                setRecorderPreviewMaxFrameRate(info, recorder.getMaxFrameRate());
                                info.putInt("previewBitRate", recorder.getPreviewBitRate() / 1024);
                                info.putInt("previewKeyFrameInterval", recorder.getIFrameInterval());
                            }
                        } else if (recorder.getMimeType().startsWith("audio/")) {
                            // 音声の設定
                        }

                        setRecorderConfig(info, "");
                        recorders.add(info);
                    }
                    setRecorders(response, recorders.toArray(new Bundle[recorders.size()]));
                    setResult(response, DConnectMessage.RESULT_OK);
                    sendResponse(response);
                }

                @Override
                public void onDisallowed() {
                    MessageUtils.setUnknownError(response, "Permission for camera is not granted.");
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
            String target = getTarget(request);

            final HostMediaRecorder recorder = mRecorderMgr.getRecorder(target);
            if (recorder == null) {
                MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
                return true;
            }

            recorder.requestPermission(new HostMediaRecorder.PermissionCallback() {
                @Override
                public void onAllowed() {
                    if (recorder.getMimeType().startsWith("image/") || recorder.getMimeType().startsWith("video/")) {
                        // 映像系の設定
                        setSupportedImageSizes(response, recorder.getSupportedPictureSizes());
                        setSupportedPreviewSizes(response, recorder.getSupportedPreviewSizes());
                    } else if (recorder.getMimeType().startsWith("audio/")) {
                        // 音声系の設定
                    }

                    setResult(response, DConnectMessage.RESULT_OK);
                    setMIMEType(response, recorder.getSupportedMimeTypes());

                    sendResponse(response);
                }

                @Override
                public void onDisallowed() {
                    MessageUtils.setUnknownError(response, "Permission for camera is not granted.");
                    sendResponse(response);
                }
            });

            return false;
        }
    };

    private final DConnectApi mPutOptionsApi = new PutApi() {
        private void setOptions(final Intent request, final Intent response) {
            String target = getTarget(request);
            String mimeType = getMIMEType(request);
            Integer imageWidth = getImageWidth(request);
            Integer imageHeight = getImageHeight(request);
            Integer previewWidth = getPreviewWidth(request);
            Integer previewHeight = getPreviewHeight(request);
            Double previewMaxFrameRate = getPreviewMaxFrameRate(request);
            Integer previewBitRate = parseInteger(request, "previewBitRate");
            Integer previewKeyFrameInterval = parseInteger(request, "previewKeyFrameInterval");

            HostMediaRecorder recorder = mRecorderMgr.getRecorder(target);
            if (recorder == null) {
                MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
                return;
            }

            if (!supportsMimeType(recorder, mimeType)) {
                MessageUtils.setInvalidRequestParameterError(response, "MIME-Type " + mimeType + " is unsupported.");
                return;
            }

            if (recorder.getState() != HostMediaRecorder.RecorderState.INACTTIVE) {
                MessageUtils.setInvalidRequestParameterError(response, "settings of active target cannot be changed.");
                return;
            }

            if (imageWidth != null && imageHeight != null) {
                if (!recorder.isSupportedPictureSize(imageWidth, imageHeight)) {
                    MessageUtils.setInvalidRequestParameterError(response, "Unsupported image size: imageWidth = "
                            + imageWidth + ", imageHeight = " + imageHeight);
                    return;
                }
                HostMediaRecorder.PictureSize newSize = new HostMediaRecorder.PictureSize(imageWidth, imageHeight);
                recorder.setPictureSize(newSize);
            }

            if (previewWidth != null && previewHeight != null) {
                if (!recorder.isSupportedPreviewSize(previewWidth, previewHeight)) {
                    MessageUtils.setInvalidRequestParameterError(response, "Unsupported preview size: previewWidth = "
                            + previewWidth + ", previewHeight = " + previewHeight);
                    return;
                }

                HostMediaRecorder.PictureSize newSize = new HostMediaRecorder.PictureSize(previewWidth, previewHeight);
                recorder.setPreviewSize(newSize);
            }

            if (previewMaxFrameRate != null) {
                recorder.setMaxFrameRate(previewMaxFrameRate);
            }

            if (previewBitRate != null) {
                recorder.setPreviewBitRate(previewBitRate * 1024);
            }

            if (previewKeyFrameInterval != null) {
                recorder.setIFrameInterval(previewKeyFrameInterval);
            }

            // 設定をプレビューサーバに反映
            PreviewServerProvider provider = recorder.getServerProvider();
            if (provider != null) {
                provider.onConfigChange();
            }

            setResult(response, DConnectMessage.RESULT_OK);
        }

        @Override
        public String getAttribute() {
            return ATTRIBUTE_OPTIONS;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final String target = getTarget(request);

            final HostMediaRecorder recorder = mRecorderMgr.getRecorder(target);

            if (recorder == null) {
                MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
                return true;
            }

            recorder.requestPermission(new HostMediaRecorder.PermissionCallback() {
                @Override
                public void onAllowed() {
                    setOptions(request, response);
                    sendResponse(response);
                }

                @Override
                public void onDisallowed() {
                    MessageUtils.setUnknownError(response, "Permission for camera is not granted.");
                    sendResponse(response);
                }
            });
            return false;
        }
    };

    private final DConnectApi mPostPreviewRequestKeyFrameApi = new PostApi() {

        @Override
        public String getInterface() {
            return "preview";
        }

        @Override
        public String getAttribute() {
            return "requestKeyFrame";
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final String target = getTarget(request);

            final HostMediaRecorder recorder = mRecorderMgr.getRecorder(target);

            if (recorder == null) {
                MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
                return true;
            }

            PreviewServerProvider provider = recorder.getServerProvider();
            List<PreviewServer> result = provider.requestSyncFrame();
            List<String> serverUrls = new ArrayList<>();
            for (PreviewServer server : result) {
                serverUrls.add(server.getUri());
            }
            response.putExtra("servers", serverUrls.toArray(new String[0]));
            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        }
    };

    private final DConnectApi mPutOnPhotoApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_PHOTO;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                setResult(response, DConnectMessage.RESULT_ERROR);
            }
            return true;
        }
    };

    private final DConnectApi mDeleteOnPhotoApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_PHOTO;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                setResult(response, DConnectMessage.RESULT_ERROR);
            }
            return true;
        }
    };

    private final DConnectApi mPutOnRecordingChangeApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_RECORDING_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                IntentFilter filter = new IntentFilter(VideoConst.SEND_VIDEO_TO_HOSTDP);
                LocalBroadcastManager.getInstance(getContext()).registerReceiver(mAudioEventBR, filter);

                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                setResult(response, DConnectMessage.RESULT_ERROR);
            }
            return true;
        }
    };

    private final DConnectApi mDeleteOnRecordingChangeApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_RECORDING_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mAudioEventBR);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                setResult(response, DConnectMessage.RESULT_ERROR);
            }
            return true;
        }
    };

    private final DConnectApi mPostTakePhotoApi = new PostApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_TAKE_PHOTO;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final String target = getTarget(request);
            final String serviceId = getServiceID(request);

            final HostMediaRecorder recorder = mRecorderMgr.getRecorder(target);

            if (recorder == null) {
                MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
                return true;
            }
            if (mRecorderMgr.usingPreviewOrStreamingRecorder(recorder.getId())) {
                MessageUtils.setInvalidRequestParameterError(response, "Another target in using.");
                return true;
            }

            if (!(recorder instanceof HostDevicePhotoRecorder)) {
                MessageUtils.setNotSupportAttributeError(response,
                        "target does not support take a photo.");
                return true;
            }

            recorder.requestPermission(new HostMediaRecorder.PermissionCallback() {
                @Override
                public void onAllowed() {
                    ((HostDevicePhotoRecorder) recorder).takePhoto(new HostDevicePhotoRecorder.OnPhotoEventListener() {
                        @Override
                        public void onTakePhoto(final String uri, final String filePath, final String mimeType) {
                            setResult(response, DConnectMessage.RESULT_OK);
                            setUri(response, uri);
                            setPath(response, filePath);
                            sendResponse(response);

                            List<Event> evts = EventManager.INSTANCE.getEventList(serviceId,
                                    MediaStreamRecordingProfile.PROFILE_NAME, null,
                                    MediaStreamRecordingProfile.ATTRIBUTE_ON_PHOTO);

                            Bundle photo = new Bundle();
                            photo.putString(MediaStreamRecordingProfile.PARAM_URI, uri);
                            photo.putString(MediaStreamRecordingProfile.PARAM_PATH, filePath);
                            photo.putString(MediaStreamRecordingProfile.PARAM_MIME_TYPE, mimeType);

                            for (Event evt : evts) {
                                Intent intent = EventManager.createEventMessage(evt);
                                intent.putExtra(MediaStreamRecordingProfile.PARAM_PHOTO, photo);
                                sendEvent(intent, evt.getAccessToken());
                            }
                        }

                        @Override
                        public void onFailedTakePhoto(final String errorMessage) {
                            MessageUtils.setUnknownError(response, errorMessage);
                            sendResponse(response);
                        }
                    });
                }

                @Override
                public void onDisallowed() {
                    MessageUtils.setUnknownError(response, "Permission for camera is not granted.");
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
            String target = getTarget(request);

            final HostMediaRecorder recorder = mRecorderMgr.getRecorder(target);

            if (recorder == null) {
                MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
                return true;
            }
            if (mRecorderMgr.usingPreviewOrStreamingRecorder(recorder.getId())) {
                MessageUtils.setInvalidRequestParameterError(response, "Another target in using.");
                return true;
            }
            recorder.requestPermission(new HostMediaRecorder.PermissionCallback() {
                @Override
                public void onAllowed() {
                    mRecorderMgr.initialize();

                    List<PreviewServer> servers = recorder.startPreviews();
                    if (servers.isEmpty()) {
                        MessageUtils.setIllegalServerStateError(response, "Failed to start web server.");
                    } else {
                        String defaultUri = null;
                        List<Bundle> streams = new ArrayList<>();
                        for (PreviewServer server : servers) {
                            // Motion-JPEG をデフォルトの値として使用します
                            if ("video/x-mjpeg".equals(server.getMimeType())) {
                                defaultUri = server.getUri();
                            }

                            Bundle stream = new Bundle();
                            stream.putString("mimeType", server.getMimeType());
                            stream.putString("uri", server.getUri());
                            streams.add(stream);
                        }
                        setResult(response, DConnectMessage.RESULT_OK);
                        setUri(response, defaultUri != null ? defaultUri : "");
                        response.putExtra("streams", streams.toArray(new Bundle[streams.size()]));
                    }
                    sendResponse(response);
                }

                @Override
                public void onDisallowed() {
                    MessageUtils.setUnknownError(response, "Permission for camera is not granted.");
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
            String target = getTarget(request);

            final HostMediaRecorder recorder = mRecorderMgr.getRecorder(target);

            if (recorder == null) {
                MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
                return true;
            }
            recorder.requestPermission(new HostMediaRecorder.PermissionCallback() {
                @Override
                public void onAllowed() {
                    recorder.stopPreviews();
                    setResult(response, DConnectMessage.RESULT_OK);
                    sendResponse(response);
                }

                @Override
                public void onDisallowed() {
                    MessageUtils.setUnknownError(response, "Permission for camera is not granted.");
                    sendResponse(response);
                }
            });
            return false;
        }
    };

    private final DConnectApi mPutPreviewMuteApi = new PutApi() {

        @Override
        public String getInterface() { return ATTRIBUTE_PREVIEW; }
        @Override
        public String getAttribute() {
            return "mute";
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return setMute(true, request, response);
        }
    };

    private final DConnectApi mDeletePreviewMuteApi = new DeleteApi() {

        @Override
        public String getInterface() { return ATTRIBUTE_PREVIEW; }
        @Override
        public String getAttribute() {
            return "mute";
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            return setMute(false, request, response);
        }
    };

    /**
     * RecorderのMute状態を切り返す.
     * RTSPをサポートしているRecorderのみ対応する.
     * @param muted true: mute状態にする。  false: mute解除状態にする。
     * @param request リクエスト
     * @param response レスポンス
     * @return true: 同期 false: 非同期
     */
    private boolean setMute(final boolean muted, final Intent request, final Intent response) {
        String target = getTarget(request);

        final HostMediaRecorder recorder = mRecorderMgr.getRecorder(target);

        if (recorder == null) {
            MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
            return true;
        }

        if (!recorder.isAudioEnabled()) {
            MessageUtils.setNotSupportAttributeError(response, "mute is not supported.");
            return true;
        }

        recorder.requestPermission(new HostMediaRecorder.PermissionCallback() {
            @Override
            public void onAllowed() {
                PreviewServerProvider provider = recorder.getServerProvider();
                if (provider != null) {
                    for (PreviewServer server : provider.getServers()) {
                        if (muted) {
                            server.mute();
                        } else {
                            server.unMute();
                        }
                    }
                    setResult(response, DConnectMessage.RESULT_OK);
                    sendResponse(response);
                } else {
                    // RecorderがRTSPをサポートしていない場合はエラーを返す。
                    MessageUtils.setIllegalDeviceStateError(response, "Unsupported.");
                    sendResponse(response);
                }
            }

            @Override
            public void onDisallowed() {
                MessageUtils.setUnknownError(response, "Permission for camera is not granted.");
                sendResponse(response);
            }
        });
        return false;
    }


    private final DConnectApi mPostRecordApi = new PostApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_RECORD;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String target = getTarget(request);

            HostMediaRecorder recorder = mRecorderMgr.getRecorder(target);
            if (recorder == null) {
                MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
                return true;
            }
            if (mRecorderMgr.usingPreviewOrStreamingRecorder(recorder.getId())) {
                MessageUtils.setInvalidRequestParameterError(response, "Another target in using.");
                return true;
            }

            if (!(recorder instanceof HostDeviceStreamRecorder)) {
                MessageUtils.setNotSupportAttributeError(response,
                        "target does not support stream recording.");
                return true;
            }

            if (recorder.getState() != HostMediaRecorder.RecorderState.INACTTIVE) {
                MessageUtils.setIllegalDeviceStateError(response,
                        recorder.getName() + " is already running.");
                return true;
            }

            final HostDeviceStreamRecorder streamRecorder = (HostDeviceStreamRecorder) recorder;
            recorder.requestPermission(new HostMediaRecorder.PermissionCallback() {
                @Override
                public void onAllowed() {
                    streamRecorder.startRecording(new HostDeviceStreamRecorder.RecordingListener() {
                        @Override
                        public void onRecorded(final HostDeviceStreamRecorder streamRecorder, final String fileName) {
                            setResult(response, DConnectMessage.RESULT_OK);
                            setPath(response, "/" + fileName);
                            setUri(response, mFileManager.getContentUri() + "/" + fileName);
                            sendResponse(response);

                            mRecorderMgr.sendEventForRecordingChange(getServiceID(request), recorder.getState(),
                                    mFileManager.getContentUri() + "/" + fileName,
                                    "/" + fileName, recorder.getMimeType(), null);
                        }

                        @Override
                        public void onFailed(final HostDeviceStreamRecorder recorder, final String errorMessage) {
                            MessageUtils.setIllegalServerStateError(response, errorMessage);
                            sendResponse(response);
                            mRecorderMgr.sendEventForRecordingChange(getServiceID(request), HostMediaRecorder.RecorderState.ERROR,"",
                                    "", recorder.getStreamMimeType(), errorMessage);
                        }
                    });
                }

                @Override
                public void onDisallowed() {
                    MessageUtils.setUnknownError(response, "Permission for camera is not granted.");
                    sendResponse(response);
                }
            });
            return false;
        }
    };

    private final DConnectApi mPutStopApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_STOP;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String target = getTarget(request);

            HostMediaRecorder recorder = mRecorderMgr.getRecorder(target);
            if (recorder == null) {
                MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
                return true;
            }
            if (!(recorder instanceof HostDeviceStreamRecorder)) {
                MessageUtils.setNotSupportAttributeError(response,
                        "target does not support stream recording.");
                return true;
            }

            if (recorder.getState() == HostMediaRecorder.RecorderState.INACTTIVE) {
                MessageUtils.setIllegalDeviceStateError(response, "recorder is stopped already.");
                return true;
            }

            final HostDeviceStreamRecorder streamRecorder = (HostDeviceStreamRecorder) recorder;
            recorder.requestPermission(new HostMediaRecorder.PermissionCallback() {
                @Override
                public void onAllowed() {
                    streamRecorder.stopRecording(new HostDeviceStreamRecorder.StoppingListener() {
                        @Override
                        public void onStopped(HostDeviceStreamRecorder streamRecorder, String fileName) {
                            setResult(response, DConnectMessage.RESULT_OK);
                            setPath(response, "/" + fileName);
                            setUri(response, mFileManager.getContentUri() + "/" + fileName);
                            sendResponse(response);

                            mRecorderMgr.sendEventForRecordingChange(getServiceID(request), recorder.getState(),
                                    mFileManager.getContentUri() + "/" + fileName,
                                    "/" + fileName, recorder.getMimeType(), null);
                        }

                        @Override
                        public void onFailed(HostDeviceStreamRecorder recorder, String errorMessage) {
                            MessageUtils.setIllegalServerStateError(response, errorMessage);
                            sendResponse(response);
                            mRecorderMgr.sendEventForRecordingChange(getServiceID(request), HostMediaRecorder.RecorderState.ERROR,"",
                                    "", recorder.getStreamMimeType(), errorMessage);
                        }
                    });
                }

                @Override
                public void onDisallowed() {
                    MessageUtils.setUnknownError(response, "Permission for camera is not granted.");
                    sendResponse(response);
                }
            });

            return false;
        }
    };

    private final DConnectApi mPutPauseApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_PAUSE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String target = getTarget(request);

            HostMediaRecorder recorder = mRecorderMgr.getRecorder(target);
            if (recorder == null) {
                MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
                return true;
            }

            if (!(recorder instanceof HostDeviceStreamRecorder)) {
                MessageUtils.setNotSupportAttributeError(response,
                        "target does not support stream recording.");
                return true;
            }

            final HostDeviceStreamRecorder streamRecorder = (HostDeviceStreamRecorder) recorder;

            if (!streamRecorder.canPauseRecording()) {
                MessageUtils.setNotSupportAttributeError(response);
                return true;
            }

            if (recorder.getState() != HostMediaRecorder.RecorderState.RECORDING) {
                MessageUtils.setIllegalDeviceStateError(response);
                return true;
            }

            recorder.requestPermission(new HostMediaRecorder.PermissionCallback() {
                @Override
                public void onAllowed() {
                    streamRecorder.pauseRecording();

                    setResult(response, DConnectMessage.RESULT_OK);
                    sendResponse(response);
                }

                @Override
                public void onDisallowed() {
                    MessageUtils.setUnknownError(response, "Permission for camera is not granted.");
                    sendResponse(response);
                }
            });

            return false;
        }
    };

    private final DConnectApi mPutResumeApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_RESUME;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String target = getTarget(request);

            HostMediaRecorder recorder = mRecorderMgr.getRecorder(target);
            if (recorder == null) {
                MessageUtils.setInvalidRequestParameterError(response,
                        "target is invalid.");
                return true;
            }

            if (!(recorder instanceof HostDeviceStreamRecorder)) {
                MessageUtils.setNotSupportAttributeError(response,
                        "target does not support stream recording.");
                return true;
            }

            final HostDeviceStreamRecorder streamRecorder = (HostDeviceStreamRecorder) recorder;

            if (!streamRecorder.canPauseRecording()) {
                MessageUtils.setNotSupportAttributeError(response);
                return true;
            }

            if (recorder.getState() != HostMediaRecorder.RecorderState.PAUSED) {
                MessageUtils.setIllegalDeviceStateError(response);
                return true;
            }

            recorder.requestPermission(new HostMediaRecorder.PermissionCallback() {
                @Override
                public void onAllowed() {
                    streamRecorder.resumeRecording();
                    setResult(response, DConnectMessage.RESULT_OK);
                    sendResponse(response);
                }

                @Override
                public void onDisallowed() {
                    MessageUtils.setUnknownError(response, "Permission for camera is not granted.");
                    sendResponse(response);
                }
            });

            return false;
        }
    };

    public HostMediaStreamingRecordingProfile(final HostMediaRecorderManager mgr, final FileManager fileMgr) {
        mRecorderMgr = mgr;
        mFileManager = fileMgr;
        addApi(mGetMediaRecorderApi);
        addApi(mGetOptionsApi);
        addApi(mPutOptionsApi);
        addApi(mPostPreviewRequestKeyFrameApi);
        addApi(mPutOnPhotoApi);
        addApi(mDeleteOnPhotoApi);
        addApi(mPostTakePhotoApi);
        addApi(mPutPreviewApi);
        addApi(mDeletePreviewApi);
        addApi(mPostRecordApi);
        addApi(mPutStopApi);
        addApi(mPutPauseApi);
        addApi(mPutResumeApi);
        addApi(mPutOnRecordingChangeApi);
        addApi(mDeleteOnRecordingChangeApi);
        addApi(mPutPreviewMuteApi);
        addApi(mDeletePreviewMuteApi);
    }

    public void destroy() {
    }

    private static void setSupportedImageSizes(final Intent response,
                                               final List<HostMediaRecorder.PictureSize> sizes) {
        Bundle[] array = new Bundle[sizes.size()];
        int i = 0;
        for (HostMediaRecorder.PictureSize size : sizes) {
            Bundle info = new Bundle();
            setWidth(info, size.getWidth());
            setHeight(info, size.getHeight());
            array[i++] = info;
        }
        setImageSizes(response, array);
    }

    private static void setSupportedPreviewSizes(final Intent response,
                                                 final List<HostMediaRecorder.PictureSize> sizes) {
        Bundle[] array = new Bundle[sizes.size()];
        int i = 0;
        for (HostMediaRecorder.PictureSize size : sizes) {
            Bundle info = new Bundle();
            setWidth(info, size.getWidth());
            setHeight(info, size.getHeight());
            array[i++] = info;
        }
        setPreviewSizes(response, array);
    }

    private boolean supportsMimeType(final HostMediaRecorder recorder, final String mimeType) {
        for (String supportedMimeType : recorder.getSupportedMimeTypes()) {
            if (supportedMimeType.equals(mimeType)) {
                return true;
            }
        }
        return false;
    }


    /**
     * MIMEタイプを設定する.
     *
     * @param response レスポンス
     * @param mimeType MIMEタイプ
     */
    public static void setMIMEType(final Intent response, final List<String> mimeType) {
        response.putExtra(PARAM_MIME_TYPE, mimeType.toArray(new String[mimeType.size()]));
    }
}
