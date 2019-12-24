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
import android.os.Handler;
import android.os.HandlerThread;
import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.host.mediaplayer.VideoConst;
import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServerProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDevicePhotoRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceRecorderManager;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceStreamRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.PreviewServer;
import org.deviceconnect.android.deviceplugin.host.recorder.PreviewServerProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.camera.Camera2Recorder;
import org.deviceconnect.android.deviceplugin.host.recorder.util.CapabilityUtil;
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
import java.util.concurrent.CountDownLatch;

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
    private final HostDeviceRecorderManager mRecorderMgr;

    /**
     * ファイル管理クラス.
     */
    private final FileManager mFileManager;

    /**
     * ライト操作結果のリスナーを実行するハンドラー.
     */
    private final Handler mLightHandler;
    /**
     * KeyEventProfileActivityからのKeyEventを中継するBroadcast Receiver.
     */
    private BroadcastReceiver mAudioEventBR = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (intent.getAction().equals(SEND_VIDEO_TO_HOSTDP)) {

                String serviceId = intent.getStringExtra(VideoConst.EXTRA_SERVICE_ID);
                HostDeviceRecorder.RecorderState state =
                        (HostDeviceRecorder.RecorderState) intent.getSerializableExtra(VideoConst.EXTRA_VIDEO_RECORDER_STATE);
                Uri uri = intent.getParcelableExtra(VideoConst.EXTRA_URI);
                String path = intent.getStringExtra(VideoConst.EXTRA_FILE_NAME);
                String u = uri != null ? uri.toString() : null;
                mRecorderMgr.sendEventForRecordingChange(serviceId, state, u, path, "audio/3gp", "");
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
            init(new PermissionUtility.PermissionRequestCallback() {
                @Override
                public void onSuccess() {
                    List<Bundle> recorders = new LinkedList<>();
                    for (HostDeviceRecorder recorder : mRecorderMgr.getRecorders()) {
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
                        if (recorder instanceof Camera2Recorder) {
                            HostDeviceRecorder.PictureSize size = recorder.getPictureSize();
                            setRecorderImageWidth(info, size.getWidth());
                            setRecorderImageHeight(info, size.getHeight());
                        }
                        if (recorder instanceof AbstractPreviewServerProvider) {
                            HostDeviceRecorder.PictureSize size = recorder.getPreviewSize();
                            setRecorderPreviewWidth(info, size.getWidth());
                            setRecorderPreviewHeight(info, size.getHeight());
                            setRecorderPreviewMaxFrameRate(info, recorder.getMaxFrameRate());
                            info.putInt("previewBitRate", recorder.getPreviewBitRate() / 1024);
                        }
                        setRecorderConfig(info, "");
                        recorders.add(info);
                    }
                    setRecorders(response, recorders.toArray(new Bundle[recorders.size()]));
                    setResult(response, DConnectMessage.RESULT_OK);
                    sendResponse(response);
                }

                @Override
                public void onFail(final String deniedPermission) {
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

            final HostDeviceRecorder recorder = mRecorderMgr.getRecorder(target);
            if (recorder == null) {
                MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
                return true;
            }

            init(new PermissionUtility.PermissionRequestCallback() {
                @Override
                public void onSuccess() {
                    setResult(response, DConnectMessage.RESULT_OK);
                    setMIMEType(response, recorder.getSupportedMimeTypes());
                    if (recorder instanceof HostDevicePhotoRecorder) {
                        setSupportedImageSizes(response, recorder.getSupportedPictureSizes());
                    }
                    if (recorder instanceof AbstractPreviewServerProvider) {
                        setSupportedPreviewSizes(response, recorder.getSupportedPreviewSizes());
                    }
                    sendResponse(response);
                }

                @Override
                public void onFail(final String deniedPermission) {
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

            HostDeviceRecorder recorder = mRecorderMgr.getRecorder(target);
            if (recorder == null) {
                MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
                return;
            }

            if (!supportsMimeType(recorder, mimeType)) {
                MessageUtils.setInvalidRequestParameterError(response, "MIME-Type " + mimeType + " is unsupported.");
                return;
            }

            if (recorder.getState() != HostDeviceRecorder.RecorderState.INACTTIVE) {
                MessageUtils.setInvalidRequestParameterError(response, "settings of active target cannot be changed.");
                return;
            }

            if (imageWidth != null && imageHeight != null) {
                if (!recorder.isSupportedPictureSize(imageWidth, imageHeight)) {
                    MessageUtils.setInvalidRequestParameterError(response, "Unsupported image size: imageWidth = "
                            + imageWidth + ", imageHeight = " + imageHeight);
                    return;
                }
                HostDeviceRecorder.PictureSize newSize = new HostDeviceRecorder.PictureSize(imageWidth, imageHeight);
                recorder.setPictureSize(newSize);
            }

            if (previewWidth != null && previewHeight != null) {
                if (!recorder.isSupportedPreviewSize(previewWidth, previewHeight)) {
                    MessageUtils.setInvalidRequestParameterError(response, "Unsupported preview size: previewWidth = "
                            + previewWidth + ", previewHeight = " + previewHeight);
                    return;
                }
                HostDeviceRecorder.PictureSize newSize = new HostDeviceRecorder.PictureSize(previewWidth, previewHeight);
                recorder.setPreviewSize(newSize);
            }

            if (previewMaxFrameRate != null) {
                if (!(recorder instanceof AbstractPreviewServerProvider)) {
                    MessageUtils.setInvalidRequestParameterError(response, "preview is unsupported.");
                    return;
                }
                recorder.setMaxFrameRate(previewMaxFrameRate);
            }

            if (previewBitRate != null) {
                if (!(recorder instanceof AbstractPreviewServerProvider)) {
                    MessageUtils.setInvalidRequestParameterError(response, "preview is unsupported.");
                    return;
                }
                recorder.setPreviewBitRate(previewBitRate * 1024);
            }

            setResult(response, DConnectMessage.RESULT_OK);
        }

        @Override
        public String getAttribute() {
            return ATTRIBUTE_OPTIONS;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            init(new PermissionUtility.PermissionRequestCallback() {
                @Override
                public void onSuccess() {
                    setOptions(request, response);
                    sendResponse(response);
                }

                @Override
                public void onFail(final String deniedPermission) {
                    MessageUtils.setUnknownError(response, "Permission for camera is not granted.");
                    sendResponse(response);
                }
            });
            return false;
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

            final HostDevicePhotoRecorder recorder = mRecorderMgr.getCameraRecorder(target);
            if (recorder == null) {
                MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
                return true;
            }

            init(new PermissionUtility.PermissionRequestCallback() {
                @Override
                public void onSuccess() {
                    recorder.takePhoto(new HostDevicePhotoRecorder.OnPhotoEventListener() {
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
                public void onFail(final String deniedPermission) {
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

            final PreviewServerProvider serverProvider = mRecorderMgr.getPreviewServerProvider(target);

            if (serverProvider == null) {
                MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
                return true;
            }

            serverProvider.requestPermission(new PreviewServerProvider.PermissionCallback() {
                @Override
                public void onAllowed() {
                    mRecorderMgr.initialize();

                    final List<PreviewServer> servers = serverProvider.getServers();
                    final String[] defaultUri = new String[1];
                    final CountDownLatch lock = new CountDownLatch(servers.size());
                    final List<Bundle> streams = new ArrayList<>();
                    for (final PreviewServer server : servers) {
                        server.startWebServer(new PreviewServer.OnWebServerStartCallback() {
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

            final PreviewServerProvider serverProvider = mRecorderMgr.getPreviewServerProvider(target);
            if (serverProvider == null) {
                MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
                return true;
            }

            init(new PermissionUtility.PermissionRequestCallback() {
                @Override
                public void onSuccess() {
                    serverProvider.stopWebServers();
                    setResult(response, DConnectMessage.RESULT_OK);
                    sendResponse(response);
                }

                @Override
                public void onFail(final String deniedPermission) {
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

        final PreviewServerProvider serverProvider = mRecorderMgr.getPreviewServerProvider(target);

        if (serverProvider == null) {
            MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
            return true;
        }

        serverProvider.requestPermission(new PreviewServerProvider.PermissionCallback() {
            @Override
            public void onAllowed() {
                PreviewServer server = serverProvider.getServerForMimeType("video/x-rtp");
                if (server != null) {
                    if (muted) {
                        server.mute();
                    } else {
                        server.unMute();
                    }
                    setResult(response, DConnectMessage.RESULT_OK);
                    sendResponse(response);
                } else {
                    // RecorderがRTSPをサポートしていない場合はエラーを返す。
                    MessageUtils.setIllegalDeviceStateError(response, "Unsupported RTSP.");
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

            if (mRecorderMgr.getRecorder(target) == null) {
                MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
                return true;
            }

            final HostDeviceStreamRecorder recorder = mRecorderMgr.getStreamRecorder(target);
            if (recorder == null) {
                MessageUtils.setNotSupportAttributeError(response,
                        "target does not support stream recording.");
                return true;
            }

            if (recorder.getState() != HostDeviceRecorder.RecorderState.INACTTIVE) {
                MessageUtils.setIllegalDeviceStateError(response, recorder.getName()
                        + " is already running.");
                return true;
            }

            init(new PermissionUtility.PermissionRequestCallback() {
                @Override
                public void onSuccess() {
                    recorder.startRecording(getServiceID(request), new HostDeviceStreamRecorder.RecordingListener() {
                        @Override
                        public void onRecorded(final HostDeviceStreamRecorder recorder, final String fileName) {
                            setResult(response, DConnectMessage.RESULT_OK);
                            setPath(response, "/" + fileName);
                            setUri(response, mFileManager.getContentUri() + "/" + fileName);
                            sendResponse(response);
                            mRecorderMgr.sendEventForRecordingChange(getServiceID(request), recorder.getState(),mFileManager.getContentUri() + "/" + fileName,
                                    "/" + fileName, recorder.getMimeType(), null);
                        }

                        @Override
                        public void onFailed(final HostDeviceStreamRecorder recorder, final String errorMessage) {
                            MessageUtils.setIllegalServerStateError(response, errorMessage);
                            sendResponse(response);
                            mRecorderMgr.sendEventForRecordingChange(getServiceID(request), HostDeviceRecorder.RecorderState.ERROR,"",
                                    "", recorder.getStreamMimeType(), errorMessage);
                        }
                    });
                }

                @Override
                public void onFail(@NonNull String deniedPermission) {
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

            if (mRecorderMgr.getRecorder(target) == null) {
                MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
                return true;
            }

            final HostDeviceStreamRecorder recorder = mRecorderMgr.getStreamRecorder(target);
            if (recorder == null) {
                MessageUtils.setNotSupportAttributeError(response,
                        "target does not support stream recording.");
                return true;
            }
            if (recorder.getState() == HostDeviceRecorder.RecorderState.INACTTIVE) {
                MessageUtils.setIllegalDeviceStateError(response, "recorder is stopped already.");
                return true;
            }

            init(new PermissionUtility.PermissionRequestCallback() {
                @Override
                public void onSuccess() {

                    recorder.stopRecording(new HostDeviceStreamRecorder.StoppingListener() {
                        @Override
                        public void onStopped(HostDeviceStreamRecorder recorder, String fileName) {
                            setResult(response, DConnectMessage.RESULT_OK);
                            setPath(response, "/" + fileName);
                            setUri(response, mFileManager.getContentUri() + "/" + fileName);
                            sendResponse(response);
                            mRecorderMgr.sendEventForRecordingChange(getServiceID(request), recorder.getState(),mFileManager.getContentUri() + "/" + fileName,
                                    "/" + fileName, recorder.getMimeType(), null);
                        }

                        @Override
                        public void onFailed(HostDeviceStreamRecorder recorder, String errorMessage) {
                            MessageUtils.setIllegalServerStateError(response, errorMessage);
                            sendResponse(response);
                            mRecorderMgr.sendEventForRecordingChange(getServiceID(request), HostDeviceRecorder.RecorderState.ERROR,"",
                                    "", recorder.getStreamMimeType(), errorMessage);
                        }
                    });

                }

                @Override
                public void onFail(@NonNull String deniedPermission) {
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

            if (mRecorderMgr.getRecorder(target) == null) {
                MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
                return true;
            }

            final HostDeviceStreamRecorder recorder = mRecorderMgr.getStreamRecorder(target);
            if (recorder == null) {
                MessageUtils.setNotSupportAttributeError(response,
                        "target does not support stream recording.");
                return true;
            }

            if (!recorder.canPauseRecording()) {
                MessageUtils.setNotSupportAttributeError(response);
                return true;
            }

            if (recorder.getState() != HostDeviceRecorder.RecorderState.RECORDING) {
                MessageUtils.setIllegalDeviceStateError(response);
                return true;
            }

            init(new PermissionUtility.PermissionRequestCallback() {
                @Override
                public void onSuccess() {
                    recorder.pauseRecording();

                    setResult(response, DConnectMessage.RESULT_OK);
                    sendResponse(response);
                }

                @Override
                public void onFail(@NonNull String deniedPermission) {
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
            if (mRecorderMgr.getRecorder(target) == null) {
                MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
                return true;
            }

            final HostDeviceStreamRecorder recorder = mRecorderMgr.getStreamRecorder(target);
            if (recorder == null) {
                MessageUtils.setNotSupportAttributeError(response,
                    "target does not support stream recording.");
                return true;
            }

            if (!recorder.canPauseRecording()) {
                MessageUtils.setNotSupportAttributeError(response);
                return true;
            }

            init(new PermissionUtility.PermissionRequestCallback() {
                @Override
                public void onSuccess() {
                    recorder.resumeRecording();
                    setResult(response, DConnectMessage.RESULT_OK);
                    sendResponse(response);
                }

                @Override
                public void onFail(@NonNull String deniedPermission) {
                    MessageUtils.setUnknownError(response, "Permission for camera is not granted.");
                    sendResponse(response);
                }
            });
            return false;
        }
    };

    public HostMediaStreamingRecordingProfile(final HostDeviceRecorderManager mgr, final FileManager fileMgr) {
        mRecorderMgr = mgr;
        mFileManager = fileMgr;

        HandlerThread thread = new HandlerThread("light");
        thread.start();
        mLightHandler = new Handler(thread.getLooper());

        addApi(mGetMediaRecorderApi);
        addApi(mGetOptionsApi);
        addApi(mPutOptionsApi);
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
        mLightHandler.getLooper().quit();
    }

    private void init(final PermissionUtility.PermissionRequestCallback callback) {
        CapabilityUtil.requestPermissions(getContext(), new PermissionUtility.PermissionRequestCallback() {
            @Override
            public void onSuccess() {
                mRecorderMgr.initialize();
                callback.onSuccess();
            }

            @Override
            public void onFail(final String deniedPermission) {
                callback.onFail(deniedPermission);
            }
        });
    }

    private static void setSupportedImageSizes(final Intent response,
                                               final List<HostDeviceRecorder.PictureSize> sizes) {
        Bundle[] array = new Bundle[sizes.size()];
        int i = 0;
        for (HostDeviceRecorder.PictureSize size : sizes) {
            Bundle info = new Bundle();
            setWidth(info, size.getWidth());
            setHeight(info, size.getHeight());
            array[i++] = info;
        }
        setImageSizes(response, array);
    }

    private static void setSupportedPreviewSizes(final Intent response,
                                                 final List<HostDeviceRecorder.PictureSize> sizes) {
        Bundle[] array = new Bundle[sizes.size()];
        int i = 0;
        for (HostDeviceRecorder.PictureSize size : sizes) {
            Bundle info = new Bundle();
            setWidth(info, size.getWidth());
            setHeight(info, size.getHeight());
            array[i++] = info;
        }
        setPreviewSizes(response, array);
    }

    private boolean supportsMimeType(final HostDeviceRecorder recorder, final String mimeType) {
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

    /**
     * カメラライト点灯状態確認.
     */
    public void checkCameraLightState() {
        // ライト点灯中なら消灯処理を実施.
        if (mRecorderMgr.getCameraRecorder(null).isUseFlashLight()) {
            mRecorderMgr.getCameraRecorder(null).turnOffFlashLight(new HostDevicePhotoRecorder.TurnOffFlashLightListener() {
                @Override
                public void onRequested() {
                    // NOP.
                }

                @Override
                public void onTurnOff() {
                    // NOP.
                }

                @Override
                public void onError(final HostDevicePhotoRecorder.Error error) {
                    // NOP.
                }
            }, mLightHandler);
        }
    }

}
