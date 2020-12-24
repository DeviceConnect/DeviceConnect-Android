/*
 HostMediaStreamingRecordingProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Size;

import org.deviceconnect.android.deviceplugin.host.HostDevicePlugin;
import org.deviceconnect.android.deviceplugin.host.recorder.Broadcaster;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDevicePhotoRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceStreamRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorderManager;
import org.deviceconnect.android.deviceplugin.host.recorder.PreviewServer;
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
import org.deviceconnect.profile.MediaStreamRecordingProfileConstants;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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

    // GET /gotapi/mediaStreamRecording/mediaRecorder
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
                        HostMediaRecorder.Settings settings = recorder.getSettings();

                        Bundle info = new Bundle();
                        setRecorderId(info, recorder.getId());
                        setRecorderName(info, recorder.getName());
                        setRecorderMIMEType(info, recorder.getMimeType());

                        if (recorder.getState() == HostMediaRecorder.State.RECORDING) {
                            setRecorderState(info, RecorderState.RECORDING);
                        } else {
                            setRecorderState(info, RecorderState.INACTIVE);
                        }

                        if (recorder.getMimeType().startsWith("image/") || recorder.getMimeType().startsWith("video/")) {
                            // 静止画の設定
                            Size pictureSize = settings.getPictureSize();
                            if (pictureSize != null) {
                                setRecorderImageWidth(info, pictureSize.getWidth());
                                setRecorderImageHeight(info, pictureSize.getHeight());
                            }

                            Size previewSize = settings.getPreviewSize();
                            if (previewSize != null) {
                                setRecorderPreviewWidth(info, previewSize.getWidth());
                                setRecorderPreviewHeight(info, previewSize.getHeight());
                                setRecorderPreviewMaxFrameRate(info, settings.getPreviewMaxFrameRate());
                                info.putInt("previewBitRate", settings.getPreviewBitRate() / 1024);
                                info.putInt("previewKeyFrameInterval", settings.getPreviewKeyFrameInterval());
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

    // GET /gotapi/mediaStreamRecording/options
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
                    HostMediaRecorder.Settings settings = recorder.getSettings();
                    if (recorder.getMimeType().startsWith("image/") || recorder.getMimeType().startsWith("video/")) {
                        // 映像系の設定
                        setSupportedImageSizes(response, settings.getSupportedPictureSizes());
                        setSupportedPreviewSizes(response, settings.getSupportedPreviewSizes());
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

    // PUT /gotapi/mediaStreamRecording/options
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

            HostMediaRecorder.Settings settings = recorder.getSettings();

            if (!isSupportsMimeType(recorder, mimeType)) {
                MessageUtils.setInvalidRequestParameterError(response, "MIME-Type " + mimeType + " is unsupported.");
                return;
            }

            if (recorder.getState() != HostMediaRecorder.State.INACTIVE
                && recorder.getState() != HostMediaRecorder.State.PREVIEW) {
                MessageUtils.setInvalidRequestParameterError(response, "settings of active target cannot be changed.");
                return;
            }

            if (imageWidth != null && imageHeight != null) {
                if (!settings.isSupportedPictureSize(imageWidth, imageHeight)) {
                    MessageUtils.setInvalidRequestParameterError(response, "Unsupported image size: imageWidth = "
                            + imageWidth + ", imageHeight = " + imageHeight);
                    return;
                }
                Size newSize = new Size(imageWidth, imageHeight);
                settings.setPictureSize(newSize);
            }

            if (previewWidth != null && previewHeight != null) {
                if (!settings.isSupportedPreviewSize(previewWidth, previewHeight)) {
                    MessageUtils.setInvalidRequestParameterError(response, "Unsupported preview size: previewWidth = "
                            + previewWidth + ", previewHeight = " + previewHeight);
                    return;
                }

                Size newSize = new Size(previewWidth, previewHeight);
                settings.setPreviewSize(newSize);
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

            recorder.onConfigChange();

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

    // POST /gotapi/mediaStreamRecording/preview/requestKeyFrame
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

            if (!recorder.isBroadcasterRunning() && !recorder.isPreviewRunning()) {
                MessageUtils.setIllegalServerStateError(response, "Recorder has not started previewing.");
            } else {
                recorder.requestKeyFrame();
                setResult(response, DConnectMessage.RESULT_OK);
            }

            return true;
        }
    };

    // PUT /gotapi/mediaStreamRecording/onPhoto
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

    // DELETE /gotapi/mediaStreamRecording/onPhoto
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

    // PUT /gotapi/mediaStreamRecording/onRecordingChange
    private final DConnectApi mPutOnRecordingChangeApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_RECORDING_CHANGE;
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

    // DELETE /gotapi/mediaStreamRecording/onRecordingChange
    private final DConnectApi mDeleteOnRecordingChangeApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_RECORDING_CHANGE;
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

    // POST /gotapi/mediaStreamRecording/takePhoto
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

            recorder.requestPermission(new HostMediaRecorder.PermissionCallback() {
                @Override
                public void onAllowed() {
                    recorder.takePhoto(new HostDevicePhotoRecorder.OnPhotoEventListener() {
                        @Override
                        public void onTakePhoto(final String uri, final String filePath, final String mimeType) {
                            setResult(response, DConnectMessage.RESULT_OK);
                            setUri(response, uri);
                            setPath(response, filePath);
                            sendResponse(response);
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

    // PUT /gotapi/mediaStreamRecording/preview
    private final DConnectApi mPutPreviewApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_PREVIEW;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String target = getTarget(request);

            HostMediaRecorder recorder = mRecorderMgr.getRecorder(target);

            if (recorder == null) {
                MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
                return true;
            }

            recorder.requestPermission(new HostMediaRecorder.PermissionCallback() {
                @Override
                public void onAllowed() {
                    HostDevicePlugin plugin = getHostDevicePlugin();
                    plugin.getSSLContext(sslContext -> {
                        recorder.setSSLContext(sslContext);

                        List<PreviewServer> servers = recorder.startPreview();
                        if (servers.isEmpty()) {
                            MessageUtils.setIllegalServerStateError(response, "Failed to start web server.");
                        } else {
                            String defaultUri = null;
                            List<Bundle> streams = new ArrayList<>();
                            for (PreviewServer server : servers) {
                                // Motion-JPEG をデフォルトの値として使用します
                                if (defaultUri == null && "video/x-mjpeg".equals(server.getMimeType())) {
                                    defaultUri = server.getUri();
                                }

                                Bundle stream = new Bundle();
                                stream.putString("mimeType", server.getMimeType());
                                stream.putString("uri", server.getUri());
                                streams.add(stream);
                            }
                            setResult(response, DConnectMessage.RESULT_OK);
                            setUri(response, defaultUri != null ? defaultUri : "");
                            response.putExtra("streams", streams.toArray(new Bundle[0]));
                        }

                        sendResponse(response);
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

    // DELETE /gotapi/mediaStreamRecording/preview
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
                    recorder.stopPreview();
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

    // PUT /gotapi/mediaStreamRecording/preview/mute
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

    // DELETE /gotapi/mediaStreamRecording/preview/mute
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

        HostMediaRecorder recorder = mRecorderMgr.getRecorder(target);
        if (recorder == null) {
            MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
            return true;
        }

        if (!recorder.getSettings().isAudioEnabled()) {
            MessageUtils.setNotSupportAttributeError(response, "mute is not supported.");
            return true;
        }

        recorder.requestPermission(new HostMediaRecorder.PermissionCallback() {
            @Override
            public void onAllowed() {
                recorder.setMute(muted);
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

    // POST /gotapi/mediaStreamRecording/record
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

            // 撮影がすでに行われている場合はエラーを返却
            if (recorder.getState() != HostMediaRecorder.State.INACTIVE
                    && recorder.getState() != HostMediaRecorder.State.PREVIEW) {
                MessageUtils.setIllegalDeviceStateError(response,
                        recorder.getName() + " is already running.");
                return true;
            }

            recorder.requestPermission(new HostMediaRecorder.PermissionCallback() {
                @Override
                public void onAllowed() {
                    recorder.startRecording(new HostDeviceStreamRecorder.RecordingCallback() {
                        @Override
                        public void onRecorded(final HostDeviceStreamRecorder streamRecorder, final String fileName) {
                            setResult(response, DConnectMessage.RESULT_OK);
                            setPath(response, "/" + fileName);
                            setUri(response, mFileManager.getContentUri() + "/" + fileName);
                            sendResponse(response);
                        }

                        @Override
                        public void onFailed(final HostDeviceStreamRecorder recorder, final String errorMessage) {
                            MessageUtils.setIllegalServerStateError(response, errorMessage);
                            sendResponse(response);
//                            sendEventForRecordingChange(getServiceID(request), HostMediaRecorder.State.ERROR,"",
//                                    "", recorder.getStreamMimeType(), errorMessage);
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

    // PUT /gotapi/mediaStreamRecording/stop
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

            if (recorder.getState() == HostMediaRecorder.State.INACTIVE) {
                MessageUtils.setIllegalDeviceStateError(response, "recorder is stopped already.");
                return true;
            }

            recorder.requestPermission(new HostMediaRecorder.PermissionCallback() {
                @Override
                public void onAllowed() {
                    recorder.stopRecording(new HostDeviceStreamRecorder.StoppingCallback() {
                        @Override
                        public void onStopped(HostDeviceStreamRecorder streamRecorder, String fileName) {
                            setResult(response, DConnectMessage.RESULT_OK);
                            setPath(response, "/" + fileName);
                            setUri(response, mFileManager.getContentUri() + "/" + fileName);
                            sendResponse(response);
                        }

                        @Override
                        public void onFailed(HostDeviceStreamRecorder recorder, String errorMessage) {
                            MessageUtils.setIllegalServerStateError(response, errorMessage);
                            sendResponse(response);
//                            sendEventForRecordingChange(getServiceID(request), HostMediaRecorder.State.ERROR,"",
//                                    "", recorder.getStreamMimeType(), errorMessage);
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

    // PUT /gotapi/mediaStreamRecording/pause
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

            if (!recorder.canPauseRecording()) {
                MessageUtils.setNotSupportAttributeError(response);
                return true;
            }

            if (recorder.getState() != HostMediaRecorder.State.RECORDING) {
                MessageUtils.setIllegalDeviceStateError(response);
                return true;
            }

            recorder.requestPermission(new HostMediaRecorder.PermissionCallback() {
                @Override
                public void onAllowed() {
                    recorder.pauseRecording();

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

    // PUT /gotapi/mediaStreamRecording/resume
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

            if (!recorder.canPauseRecording()) {
                MessageUtils.setNotSupportAttributeError(response);
                return true;
            }

            if (recorder.getState() != HostMediaRecorder.State.PAUSED) {
                MessageUtils.setIllegalDeviceStateError(response);
                return true;
            }

            recorder.requestPermission(new HostMediaRecorder.PermissionCallback() {
                @Override
                public void onAllowed() {
                    recorder.resumeRecording();
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

    private final HostMediaRecorderManager.OnEventListener mOnEventListener = new HostMediaRecorderManager.OnEventListener() {
        @Override
        public void onPreviewStarted(HostMediaRecorder recorder, List<PreviewServer> servers) {
        }

        @Override
        public void onPreviewStopped(HostMediaRecorder recorder) {
        }

        @Override
        public void onBroadcasterStarted(HostMediaRecorder recorder, Broadcaster broadcaster) {
        }

        @Override
        public void onBroadcasterStopped(HostMediaRecorder recorder) {
        }

        @Override
        public void onTakePhoto(HostMediaRecorder recorder, String uri, String filePath, String mimeType) {
            sendEventForTakePhoto(getService().getId(), uri, filePath, mimeType);
        }

        @Override
        public void onRecordingStarted(HostMediaRecorder recorder, String fileName) {
            sendEventForRecordingChange(getService().getId(), recorder.getState(),
                    mFileManager.getContentUri() + "/" + fileName,
                    "/" + fileName, recorder.getMimeType(), null);
        }

        @Override
        public void onRecordingPause(HostMediaRecorder recorder) {
        }

        @Override
        public void onRecordingResume(HostMediaRecorder recorder) {
        }

        @Override
        public void onRecordingStopped(HostMediaRecorder recorder, String fileName) {
            sendEventForRecordingChange(getService().getId(), recorder.getState(),
                    mFileManager.getContentUri() + "/" + fileName,
                    "/" + fileName, recorder.getMimeType(), null);

        }

        @Override
        public void onError(HostMediaRecorder recorder, Exception e) {
        }
    };

    public HostMediaStreamingRecordingProfile(final HostMediaRecorderManager mgr, final FileManager fileMgr) {
        mRecorderMgr = mgr;
        mRecorderMgr.addOnEventListener(mOnEventListener);

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

    private HostDevicePlugin getHostDevicePlugin() {
        return (HostDevicePlugin) getContext();
     }

    private static void setSupportedImageSizes(final Intent response, final List<Size> sizes) {
        Bundle[] array = new Bundle[sizes.size()];
        int i = 0;
        for (Size size : sizes) {
            Bundle info = new Bundle();
            setWidth(info, size.getWidth());
            setHeight(info, size.getHeight());
            array[i++] = info;
        }
        setImageSizes(response, array);
    }

    private static void setSupportedPreviewSizes(final Intent response, final List<Size> sizes) {
        Bundle[] array = new Bundle[sizes.size()];
        int i = 0;
        for (Size size : sizes) {
            Bundle info = new Bundle();
            setWidth(info, size.getWidth());
            setHeight(info, size.getHeight());
            array[i++] = info;
        }
        setPreviewSizes(response, array);
    }

    private boolean isSupportsMimeType(final HostMediaRecorder recorder, final String mimeType) {
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

    private void sendEventForTakePhoto(String serviceId, String uri, String filePath, String mimeType) {
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

    private void sendEventForRecordingChange(final String serviceId, final HostMediaRecorder.State state,
                                            final String uri, final String path,
                                            final String mimeType, final String errorMessage) {
        List<Event> evts = EventManager.INSTANCE.getEventList(serviceId,
                MediaStreamRecordingProfile.PROFILE_NAME, null,
                MediaStreamRecordingProfile.ATTRIBUTE_ON_RECORDING_CHANGE);

        Bundle record = new Bundle();
        switch (state) {
            case RECORDING:
                MediaStreamRecordingProfile.setStatus(record, MediaStreamRecordingProfileConstants.RecordingState.RECORDING);
                break;
            case INACTIVE:
                MediaStreamRecordingProfile.setStatus(record, MediaStreamRecordingProfileConstants.RecordingState.STOP);
                break;
            case ERROR:
                MediaStreamRecordingProfile.setStatus(record, MediaStreamRecordingProfileConstants.RecordingState.ERROR);
                break;
            default:
                MediaStreamRecordingProfile.setStatus(record, MediaStreamRecordingProfileConstants.RecordingState.UNKNOWN);
                break;
        }
        record.putString(MediaStreamRecordingProfile.PARAM_URI, uri);
        record.putString(MediaStreamRecordingProfile.PARAM_PATH, path);
        record.putString(MediaStreamRecordingProfile.PARAM_MIME_TYPE, mimeType);
        if (errorMessage != null) {
            record.putString(MediaStreamRecordingProfile.PARAM_ERROR_MESSAGE, errorMessage);
        }

        for (Event evt : evts) {
            Intent intent = EventManager.createEventMessage(evt);
            intent.putExtra(MediaStreamRecordingProfile.PARAM_MEDIA, record);
            getPluginContext().sendEvent(intent, evt.getAccessToken());
        }
    }
}
