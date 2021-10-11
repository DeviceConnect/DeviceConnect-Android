/*
 HostMediaStreamingRecordingProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.profile;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Range;
import android.util.Size;

import org.deviceconnect.android.deviceplugin.host.HostDevicePlugin;
import org.deviceconnect.android.deviceplugin.host.profile.utils.H264Level;
import org.deviceconnect.android.deviceplugin.host.profile.utils.H264Profile;
import org.deviceconnect.android.deviceplugin.host.profile.utils.H265Level;
import org.deviceconnect.android.deviceplugin.host.profile.utils.H265Profile;
import org.deviceconnect.android.deviceplugin.host.recorder.AbstractMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.CropInterface;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDevicePhotoRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceStreamRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorderManager;
import org.deviceconnect.android.deviceplugin.host.recorder.LiveStreaming;
import org.deviceconnect.android.deviceplugin.host.recorder.camera.Camera2Recorder;
import org.deviceconnect.android.deviceplugin.host.recorder.util.CapabilityUtil;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.MediaStreamRecordingProfile;
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

    private final HostMediaRecorderManager.OnEventListener mOnEventListener = new HostMediaRecorderManager.OnEventListener() {
        @Override
        public void onMuteChanged(HostMediaRecorder recorder, boolean mute) {
        }

        @Override
        public void onConfigChanged(HostMediaRecorder recorder) {
        }

        @Override
        public void onPreviewStarted(HostMediaRecorder recorder, List<LiveStreaming> servers) {
        }

        @Override
        public void onPreviewStopped(HostMediaRecorder recorder) {
        }

        @Override
        public void onPreviewError(HostMediaRecorder recorder, Exception e) {
        }

        @Override
        public void onBroadcasterStarted(HostMediaRecorder recorder, List<LiveStreaming> broadcasters) {
        }

        @Override
        public void onBroadcasterStopped(HostMediaRecorder recorder) {
        }

        @Override
        public void onBroadcasterError(HostMediaRecorder recorder, LiveStreaming broadcaster, Exception e) {
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
        mFileManager = fileMgr;
        mRecorderMgr = mgr;
        mRecorderMgr.addOnEventListener(mOnEventListener);

        // GET /gotapi/mediaStreamRecording/mediaRecorder
        addApi(new GetApi() {
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
                        if (target == null) {
                            for (HostMediaRecorder recorder : mRecorderMgr.getRecorders()) {
                                recorders.add(createMediaRecorderInfo(recorder));
                            }
                            setRecorders(response, recorders.toArray(new Bundle[0]));
                        } else {
                            response.putExtras(createMediaRecorderInfo(recorder));
                        }
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

                HostMediaRecorder recorder = mRecorderMgr.getRecorder(target);
                if (recorder == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
                    return true;
                }

                recorder.requestPermission(new HostMediaRecorder.PermissionCallback() {
                    @Override
                    public void onAllowed() {
                        if (target == null) {
                            List<Bundle> recorders = new LinkedList<>();
                            for (HostMediaRecorder recorder : mRecorderMgr.getRecorders()) {
                                recorders.add(createRecorderOption(recorder));
                            }
                            setRecorders(response, recorders.toArray(new Bundle[0]));
                        } else {
                            response.putExtras(createRecorderOption(recorder));
                        }
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

                HostMediaRecorder recorder = mRecorderMgr.getRecorder(target);
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
        });

        // PUT /gotapi/mediaStreamRecording/onPhoto
        addApi(new PutApi() {
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
        });

        // DELETE /gotapi/mediaStreamRecording/onPhoto
        addApi(new DeleteApi() {
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
        });

        // POST /gotapi/mediaStreamRecording/takePhoto
        addApi(new PostApi() {
            @Override
            public String getAttribute() {
                return ATTRIBUTE_TAKE_PHOTO;
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String target = getTarget(request);

                HostMediaRecorder recorder = mRecorderMgr.getRecorder(target);
                if (recorder == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
                    return true;
                }

                // TODO 他のカメラとは排他的に処理を行うようにします。
                if (!mRecorderMgr.canUseRecorder(recorder)) {
                    // 他のカメラが使用中の場合はエラーを返却
                    MessageUtils.setIllegalDeviceStateError(response, "Other cameras are being used.");
                    return true;
                }

                recorder.requestPermission(new HostMediaRecorder.PermissionCallback() {
                    @Override
                    public void onAllowed() {
                        // TODO 他のカメラとは排他的に処理を行うようにします。
                        if (recorder instanceof Camera2Recorder) {
                            // 使用する予定のレコーダがカメラの場合は、使用していない他のカメラを停止する
                            mRecorderMgr.stopCameraRecorder(recorder);
                        }

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
        });

        // POST /gotapi/mediaStreamRecording/record
        addApi(new PostApi() {
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

                // TODO 他のカメラとは排他的に処理を行うようにします。
                if (!mRecorderMgr.canUseRecorder(recorder)) {
                    // 他のカメラが使用中の場合はエラーを返却
                    MessageUtils.setIllegalDeviceStateError(response, "Other cameras are being used.");
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
        });

        // PUT /gotapi/mediaStreamRecording/stop
        addApi(new PutApi() {
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
        });

        // PUT /gotapi/mediaStreamRecording/pause
        addApi(new PutApi() {
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
        });

        // PUT /gotapi/mediaStreamRecording/resume
        addApi(new PutApi() {
            @Override
            public String getAttribute() {
                return ATTRIBUTE_RESUME;
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
        });

        // PUT /gotapi/mediaStreamRecording/onRecordingChange
        addApi(new PutApi() {
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
        });

        // DELETE /gotapi/mediaStreamRecording/onRecordingChange
        addApi(new DeleteApi() {
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

                HostMediaRecorder recorder = mRecorderMgr.getRecorder(target);
                if (recorder == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
                    return true;
                }

                // TODO 他のカメラとは排他的に処理を行うようにします。
                if (!mRecorderMgr.canUseRecorder(recorder)) {
                    // 他のカメラが使用中の場合はエラーを返却
                    MessageUtils.setIllegalDeviceStateError(response, "Other cameras are being used.");
                    return true;
                }

                recorder.requestPermission(new HostMediaRecorder.PermissionCallback() {
                    @Override
                    public void onAllowed() {
                        HostDevicePlugin plugin = getHostDevicePlugin();
                        plugin.getSSLContext(sslContext -> {
                            recorder.setSSLContext(sslContext);

                            List<LiveStreaming> servers = recorder.startPreview();
                            if (servers.isEmpty()) {
                                MessageUtils.setIllegalServerStateError(response, "Failed to start web server.");
                            } else {
                                String defaultUri = null;
                                List<Bundle> streams = new ArrayList<>();
                                for (LiveStreaming server : servers) {
                                    // Motion-JPEG をデフォルトの値として使用します
                                    if (defaultUri == null && server.getUri().startsWith("http://")
                                            && "video/x-mjpeg".equals(server.getMimeType())) {
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
        });

        // POST /gotapi/mediaStreamRecording/preview/requestKeyFrame
        addApi(new PostApi() {
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
        });

        // PUT /gotapi/mediaStreamRecording/preview/mute
        addApi(new PutApi() {
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
        });

        // DELETE /gotapi/mediaStreamRecording/preview/mute
        addApi(new DeleteApi() {

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
        });

        // PUT /gotapi/mediaStreamRecording/encoder
        addApi(new PutApi() {
            @Override
            public String getAttribute() {
                return "encoder";
            }

            @Override
            public boolean onRequest(Intent request, Intent response) {
                String target = getTarget(request);
                String mimeTypeValue = request.getStringExtra("mimeType");
                String name = request.getStringExtra("name");
                Integer width = parseInteger(request, "width");
                Integer height = parseInteger(request, "height");
                Integer frameRate = parseInteger(request, "frameRate");
                Integer bitRate = parseInteger(request, "bitRate");
                Integer keyFrameInterval = parseInteger(request, "keyFrameInterval");
                String codec = request.getStringExtra("codec");
                String profile = request.getStringExtra("profile");
                String level = request.getStringExtra("level");
                Integer intraRefresh = parseInteger(request,"intraRefresh");
                Boolean useSoftwareEncoder = parseBoolean(request,"useSoftwareEncoder");
                Double jpegQuality = parseDouble(request, "jpegQuality");
                String broadcastUri = request.getStringExtra("broadcastUri");
                Integer retryCount = parseInteger(request, "retryCount");
                Integer retryInterval = parseInteger(request, "retryInterval");

                HostMediaRecorder recorder = mRecorderMgr.getRecorder(target);
                if (recorder == null) {
                    MessageUtils.setInvalidRequestParameterError(response,
                            "target is invalid.");
                    return true;
                }

                List<String> encoderIdList = getEncoderSettings(recorder, name, false);

                if (width != null && width < 0) {
                    MessageUtils.setInvalidRequestParameterError(response,
                            "width is invalid. value=" + width);
                    return true;
                }

                if (height != null && height < 0) {
                    MessageUtils.setInvalidRequestParameterError(response,
                            "height is invalid. value=" + height);
                    return true;
                }

                if (frameRate != null && frameRate <= 0) {
                    MessageUtils.setInvalidRequestParameterError(response,
                            "frameRate is invalid. value=" + frameRate);
                    return true;
                }

                if (bitRate != null && bitRate <= 0) {
                    MessageUtils.setInvalidRequestParameterError(response,
                            "bitRate. value=" + bitRate);
                    return true;
                }

                if (keyFrameInterval != null && keyFrameInterval < 0) {
                    MessageUtils.setInvalidRequestParameterError(response,
                            "keyFrameInterval. value=" + keyFrameInterval);
                    return true;
                }

                if (intraRefresh != null && intraRefresh < 0) {
                    MessageUtils.setInvalidRequestParameterError(response,
                            "intraRefresh is invalid. value=" + intraRefresh);
                    return true;
                }

                if (jpegQuality != null) {
                    if (jpegQuality < 0.0 || jpegQuality > 1.0) {
                        MessageUtils.setInvalidRequestParameterError(response,
                                "jpegQuality is invalid. value=" + jpegQuality);
                        return true;
                    }
                }

                if (retryCount != null) {
                    if (retryCount < 0) {
                        MessageUtils.setInvalidRequestParameterError(response,
                                "retryCount is invalid. value=" + retryCount);
                        return true;
                    }
                }

                if (retryInterval != null) {
                    if (retryInterval <= 0) {
                        MessageUtils.setInvalidRequestParameterError(response,
                                "retryInterval is invalid. value=" + retryInterval);
                        return true;
                    }
                }

                HostMediaRecorder.ProfileLevel profileLevel = convertProfileLevel(recorder, codec, profile, level);

                recorder.requestPermission(new HostMediaRecorder.PermissionCallback() {
                    @Override
                    public void onAllowed() {
                        HostMediaRecorder.Settings settings = recorder.getSettings();

                        for (String encoderId : encoderIdList) {
                            HostMediaRecorder.EncoderSettings encoderSettings = settings.getEncoderSetting(encoderId);
                            if (encoderSettings == null) {
                                if (mimeTypeValue == null) {
                                    MessageUtils.setInvalidRequestParameterError(response, "mimeType is not set.");
                                    sendResponse(response);
                                    return;
                                }

                                HostMediaRecorder.MimeType mimeType = HostMediaRecorder.MimeType.typeOf(mimeTypeValue);
                                if (mimeType == HostMediaRecorder.MimeType.UNKNOWN) {
                                    MessageUtils.setInvalidRequestParameterError(response, "mimeType is unknown.");
                                    sendResponse(response);
                                    return;
                                }

                                encoderSettings = new HostMediaRecorder.EncoderSettings(getContext(), encoderId);
                                encoderSettings.setName(name);
                                encoderSettings.setMimeType(mimeType);
                                encoderSettings.setPreviewSize(new Size(640, 480));
                                encoderSettings.setPort(14000);
                                encoderSettings.setPreviewMaxFrameRate(30);
                                encoderSettings.setPreviewBitRate(2 * 1024 * 1024);
                                encoderSettings.setPreviewKeyFrameInterval(5);
                                encoderSettings.setPreviewQuality(80);
                                encoderSettings.setBroadcastURI("rtmp://localhost:1935");
                                encoderSettings.setRetryCount(0);
                                encoderSettings.setRetryInterval(3000);

                                ((AbstractMediaRecorder) recorder).addEncoder(encoderId, encoderSettings);
                            }
                        }

                        if (width != null && height != null) {
                            for (String encoderId : encoderIdList) {
                                settings.getEncoderSetting(encoderId).setPreviewSize(new Size(width, height));
                            }
                        }

                        if (frameRate != null) {
                            for (String encoderId : encoderIdList) {
                                settings.getEncoderSetting(encoderId).setPreviewMaxFrameRate(frameRate);
                            }
                        }

                        if (bitRate != null) {
                            for (String encoderId : encoderIdList) {
                                settings.getEncoderSetting(encoderId).setPreviewBitRate(bitRate * 1024);
                            }
                        }

                        if (keyFrameInterval != null) {
                            for (String encoderId : encoderIdList) {
                                settings.getEncoderSetting(encoderId).setPreviewKeyFrameInterval(keyFrameInterval);
                            }
                        }

                        if (codec != null) {
                            for (String encoderId : encoderIdList) {
                                settings.getEncoderSetting(encoderId).setPreviewEncoder(codec);
                                settings.getEncoderSetting(encoderId).setProfileLevel(null);
                            }
                        }

                        if (profileLevel != null) {
                            for (String encoderId : encoderIdList) {
                                settings.getEncoderSetting(encoderId).setProfileLevel(profileLevel);
                            }
                        }

                        if (intraRefresh != null) {
                            for (String encoderId : encoderIdList) {
                                settings.getEncoderSetting(encoderId).setIntraRefresh(intraRefresh);
                            }
                        }

                        if (useSoftwareEncoder != null) {
                            for (String encoderId : encoderIdList) {
                                settings.getEncoderSetting(encoderId).setUseSoftwareEncoder(useSoftwareEncoder);
                            }
                        }

                        if (jpegQuality != null) {
                            for (String encoderId : encoderIdList) {
                                settings.getEncoderSetting(encoderId).setPreviewQuality((int) (jpegQuality * 100));
                            }
                        }

                        if (broadcastUri != null) {
                            for (String encoderId : encoderIdList) {
                                settings.getEncoderSetting(encoderId).setBroadcastURI(broadcastUri);
                            }
                        }

                        if (retryCount != null) {
                            for (String encoderId : encoderIdList) {
                                settings.getEncoderSetting(encoderId).setRetryCount(retryCount);
                            }
                        }

                        if (retryInterval != null) {
                            for (String encoderId : encoderIdList) {
                                settings.getEncoderSetting(encoderId).setRetryInterval(retryInterval);
                            }
                        }

                        try {
                            recorder.onConfigChange();
                            setResult(response, DConnectMessage.RESULT_OK);
                        } catch (Exception e) {
                            MessageUtils.setIllegalDeviceStateError(response, "Failed to change a config.");
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
        });

        // DELETE /gotapi/mediaStreamRecording/encoder
        addApi(new DeleteApi() {
            @Override
            public String getAttribute() {
                return "encoder";
            }

            @Override
            public boolean onRequest(Intent request, Intent response) {
                String target = getTarget(request);
                String name = request.getStringExtra("name");

                if (name == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "name is not set.");
                    return true;
                }

                HostMediaRecorder recorder = mRecorderMgr.getRecorder(target);
                if (recorder == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
                    return true;
                }

                String encoderId = recorder.getId() + "-" + name;

                ((AbstractMediaRecorder) recorder).removeEncoder(encoderId);

                setResult(response, DConnectMessage.RESULT_OK);
                return true;
            }
        });

        // PUT /gotapi/mediaStreamRecording/crop
        addApi(new PutApi() {
            @Override
            public String getAttribute() {
                return "crop";
            }

            @Override
            public boolean onRequest(Intent request, Intent response) {
                String target = getTarget(request);
                String name = request.getStringExtra("name");
                Integer left = parseInteger(request, "left");
                Integer top = parseInteger(request, "top");
                Integer right = parseInteger(request, "right");
                Integer bottom = parseInteger(request, "bottom");
                Integer duration = parseInteger(request, "duration");

                HostMediaRecorder recorder = mRecorderMgr.getRecorder(target);
                if (recorder == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
                    return true;
                }

                if (left == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "left is not set.");
                    return true;
                }

                if (top == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "top is not set.");
                    return true;
                }

                if (right == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "right is not set.");
                    return true;
                }

                if (bottom == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "bottom is not set.");
                    return true;
                }

                List<String> encoderIdList = getEncoderSettings(recorder, name, true);
                if (encoderIdList.isEmpty()) {
                    MessageUtils.setInvalidRequestParameterError(response, "name is invalid.");
                    return true;
                }

                if (left >= right || top >= bottom) {
                    MessageUtils.setInvalidRequestParameterError(response, "parameter is invalid.");
                    return true;
                }

                recorder.requestPermission(new HostMediaRecorder.PermissionCallback() {
                    @Override
                    public void onAllowed() {
                        for (String encoderId : encoderIdList) {
                            setCrop(recorder, encoderId, left, top, right, bottom, duration);
                        }
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
        });

        // DELETE /gotapi/mediaStreamRecording/crop
        addApi(new DeleteApi() {
            @Override
            public String getAttribute() {
                return "crop";
            }

            @Override
            public boolean onRequest(Intent request, Intent response) {
                String target = getTarget(request);
                String name = request.getStringExtra("name");

                HostMediaRecorder recorder = mRecorderMgr.getRecorder(target);
                if (recorder == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
                    return true;
                }

                List<String> encoderIdList = getEncoderSettings(recorder, name, true);
                if (encoderIdList.isEmpty()) {
                    MessageUtils.setInvalidRequestParameterError(response, "name is invalid.");
                    return true;
                }

                recorder.requestPermission(new HostMediaRecorder.PermissionCallback() {
                    @Override
                    public void onAllowed() {
                        for (String encoderId : encoderIdList) {
                            clearCrop(recorder, encoderId);
                        }
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
                String broadcastURI = request.getStringExtra("broadcastURI");

                HostMediaRecorder recorder = mRecorderMgr.getRecorder(target);

                if (recorder == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
                    return true;
                }

                if (broadcastURI == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "broadcastURI is not set.");
                    return true;
                }

                if (recorder.isBroadcasterRunning()) {
                    MessageUtils.setIllegalServerStateError(response, "broadcastURI is already running.");
                    return true;
                }

                // TODO 他のカメラとは排他的に処理を行うようにします。
                if (!mRecorderMgr.canUseRecorder(recorder)) {
                    // 他のカメラが使用中の場合はエラーを返却
                    MessageUtils.setIllegalDeviceStateError(response, "Other cameras are being used.");
                    return true;
                }

                recorder.requestPermission(new HostMediaRecorder.PermissionCallback() {
                    @Override
                    public void onAllowed() {
                        // TODO 他のカメラとは排他的に処理を行うようにします。
                        if (recorder instanceof Camera2Recorder) {
                            // 使用する予定のレコーダがカメラの場合は、使用していない他のカメラを停止する
                            mRecorderMgr.stopCameraRecorder(recorder);
                        }

                        for (String encoderId : recorder.getSettings().getEncoderIdList()) {
                            HostMediaRecorder.EncoderSettings encoderSettings = recorder.getSettings().getEncoderSetting(encoderId);
                            if (encoderSettings != null && encoderSettings.getMimeType() == HostMediaRecorder.MimeType.RTMP) {
                                encoderSettings.setBroadcastURI(broadcastURI);
                            }
                        }

                        List<LiveStreaming> broadcasters = recorder.startBroadcaster(broadcastURI);
                        if (!broadcasters.isEmpty()) {
                            setResult(response, DConnectMessage.RESULT_OK);
                        } else {
                            MessageUtils.setIllegalServerStateError(response, "Failed to start a broadcast.");
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

                HostMediaRecorder recorder = mRecorderMgr.getRecorder(target);

                if (recorder == null) {
                    MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
                    return true;
                }

                recorder.requestPermission(new HostMediaRecorder.PermissionCallback() {
                    @Override
                    public void onAllowed() {
                        recorder.stopBroadcaster();
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
        });
    }

    private HostDevicePlugin getHostDevicePlugin() {
        return (HostDevicePlugin) getContext();
    }

    private List<String> getEncoderSettings(HostMediaRecorder recorder, String name, boolean checkExist) {
        List<String> encoderIdList = new ArrayList<>();
        if (name == null) {
            encoderIdList.addAll(recorder.getSettings().getEncoderIdList());
        } else {
            String[] names = name.split(",");
            for (String n : names) {
                String encoderId = recorder.getId() + "-" + n;
                if (!checkExist || recorder.getSettings().existEncoderId(encoderId)) {
                    encoderIdList.add(encoderId);
                }
            }
        }
        return encoderIdList;
    }

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
        String previewEncoder = request.getStringExtra("previewEncoder");
        String previewProfile = request.getStringExtra("previewProfile");
        String previewLevel = request.getStringExtra("previewLevel");
        Integer previewIntraRefresh = parseInteger("previewIntraRefresh");
        Double previewJpegQuality = parseDouble(request, "previewJpegQuality");
        Integer previewClipLeft = parseInteger(request, "previewClipLeft");
        Integer previewClipTop = parseInteger(request, "previewClipTop");
        Integer previewClipRight = parseInteger(request, "previewClipRight");
        Integer previewClipBottom = parseInteger(request, "previewClipBottom");
        Integer previewClipDuration = parseInteger(request, "previewClipDuration");
        Boolean previewClipReset = parseBoolean(request, "previewClipReset");

        HostMediaRecorder.ProfileLevel profileLevel = null;
        Range<Integer> fps = null;
        boolean isChangeConfig = false;

        HostMediaRecorder recorder = mRecorderMgr.getRecorder(target);
        if (recorder == null) {
            MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
            return;
        }

        HostMediaRecorder.Settings settings = recorder.getSettings();

//        if (!isSupportedMimeType(recorder, mimeType)) {
//            MessageUtils.setInvalidRequestParameterError(response, "MIME-Type " + mimeType + " is unsupported.");
//            return;
//        }

        if (recorder.getState() != HostMediaRecorder.State.INACTIVE
                && recorder.getState() != HostMediaRecorder.State.PREVIEW) {
            MessageUtils.setInvalidRequestParameterError(response, "settings of active target cannot be changed.");
            return;
        }

        // 値の妥当性チェック

        if (imageWidth != null || imageHeight != null) {
            if (imageWidth == null) {
                MessageUtils.setInvalidRequestParameterError(response, "imageWidth is not set.");
                return;
            }

            if (imageHeight == null) {
                MessageUtils.setInvalidRequestParameterError(response, "imageHeight is not set.");
                return;
            }

            if (!settings.isSupportedPictureSize(imageWidth, imageHeight)) {
                MessageUtils.setInvalidRequestParameterError(response, "Unsupported image size: imageWidth = "
                        + imageWidth + ", imageHeight = " + imageHeight);
                return;
            }
        }

        if (previewWidth != null || previewHeight != null) {
            if (previewWidth == null) {
                MessageUtils.setInvalidRequestParameterError(response, "previewWidth is not set.");
                return;
            }

            if (previewHeight == null) {
                MessageUtils.setInvalidRequestParameterError(response, "previewHeight is not set.");
                return;
            }

            if (!settings.isSupportedPreviewSize(previewWidth, previewHeight)) {
                MessageUtils.setInvalidRequestParameterError(response, "Unsupported preview size: previewWidth = "
                        + previewWidth + ", previewHeight = " + previewHeight);
                return;
            }
        }

        if (previewMaxFrameRate != null) {
            fps = recorder.getSettings().getPreviewFpsFromFrameRate(previewMaxFrameRate.intValue());
            if (fps == null) {
                MessageUtils.setInvalidRequestParameterError(response, "previewMaxFrameRate is not supported.");
            }
        }

        if (previewEncoder != null) {
            if (!settings.isSupportedVideoEncoder(previewEncoder)) {
                MessageUtils.setInvalidRequestParameterError(response,
                        "Unsupported preview encoder: " + previewEncoder);
                return;
            }
        }

        if (previewProfile != null || previewLevel != null) {
            if (previewProfile == null) {
                MessageUtils.setInvalidRequestParameterError(response,
                        "previewProfile is not set.");
                return;
            }

            if (previewLevel == null) {
                MessageUtils.setInvalidRequestParameterError(response,
                        "previewLevel is not set.");
                return;
            }

            HostMediaRecorder.VideoCodec codec = HostMediaRecorder.VideoCodec.nameOf(previewEncoder);
            switch (codec) {
                case H264: {
                    H264Profile p = H264Profile.nameOf(previewProfile);
                    H264Level l = H264Level.nameOf(previewLevel);
                    if (p == null || l == null || !settings.isSupportedProfileLevel(codec, p.getValue(), l.getValue())) {
                        MessageUtils.setInvalidRequestParameterError(response,
                                "Unsupported preview profile and level: " + previewProfile + " - " + previewLevel);
                        return;
                    }
                    profileLevel = new HostMediaRecorder.ProfileLevel(p.getValue(), l.getValue());
                }   break;
                case H265: {
                    H265Profile p = H265Profile.nameOf(previewProfile);
                    H265Level l = H265Level.nameOf(previewLevel);
                    if (p == null || l == null || !settings.isSupportedProfileLevel(codec, p.getValue(), l.getValue())) {
                        MessageUtils.setInvalidRequestParameterError(response,
                                "Unsupported preview profile and level: " + previewProfile + " - " + previewLevel);
                        return;
                    }
                    profileLevel = new HostMediaRecorder.ProfileLevel(p.getValue(), l.getValue());
                }   break;
            }
        }

        if (previewJpegQuality != null) {
            if (previewJpegQuality < 0.0 || previewJpegQuality > 1.0) {
                MessageUtils.setInvalidRequestParameterError(response,
                        "previewJpegQuality is invalid. value=" + previewJpegQuality);
                return;
            }
        }

        if (previewClipLeft != null || previewClipTop != null
                || previewClipRight != null || previewClipBottom != null) {
            if (previewClipLeft == null) {
                MessageUtils.setInvalidRequestParameterError(response, "previewClipLeft is not set.");
                return;
            }

            if (previewClipTop == null) {
                MessageUtils.setInvalidRequestParameterError(response, "previewClipTop is not set.");
                return;
            }

            if (previewClipRight == null) {
                MessageUtils.setInvalidRequestParameterError(response, "previewClipRight is not set.");
                return;
            }

            if (previewClipBottom == null) {
                MessageUtils.setInvalidRequestParameterError(response, "previewClipBottom is not set.");
                return;
            }

            if (previewClipLeft < 0) {
                MessageUtils.setInvalidRequestParameterError(response,
                        "previewClipLeft cannot set a negative value.");
                return;
            }

            if (previewClipBottom < 0) {
                MessageUtils.setInvalidRequestParameterError(response,
                        "previewClipBottom cannot set a negative value.");
                return;
            }

            if (previewClipLeft >= previewClipRight) {
                MessageUtils.setInvalidRequestParameterError(response,
                        "previewClipLeft is larger than previewClipRight.");
                return;
            }

            if (previewClipTop >= previewClipBottom) {
                MessageUtils.setInvalidRequestParameterError(response,
                        "previewClipTop is larger than previewClipBottom.");
                return;
            }

            if (previewClipDuration != null) {
                if (previewClipDuration < 0) {
                    MessageUtils.setInvalidRequestParameterError(response,
                            "previewClipDuration is negative value.");
                    return;
                }
            } else {
                previewClipDuration = 0;
            }
        }

        // 値の設定

        if (imageWidth != null && imageHeight != null) {
            settings.setPictureSize(new Size(imageWidth, imageHeight));
        }

        if (previewWidth != null && previewHeight != null) {
            settings.setPreviewSize(new Size(previewWidth, previewHeight));

            for (String encoderId : settings.getEncoderIdList()) {
                HostMediaRecorder.EncoderSettings encoderSettings = settings.getEncoderSetting(encoderId);
                if (encoderId != null && mimeType.equals(encoderSettings.getMimeType().getValue())) {
                    encoderSettings.setPreviewSize(new Size(previewWidth, previewHeight));
                }
            }

            isChangeConfig = true;
        }

        if (fps != null) {
            settings.setPreviewFps(fps);

            for (String encoderId : settings.getEncoderIdList()) {
                HostMediaRecorder.EncoderSettings encoderSettings = settings.getEncoderSetting(encoderId);
                if (encoderId != null && mimeType.equals(encoderSettings.getMimeType().getValue())) {
                    encoderSettings.setPreviewMaxFrameRate(previewMaxFrameRate.intValue());
                }
            }

            isChangeConfig = true;
        }

        if (previewBitRate != null) {
            for (String encoderId : settings.getEncoderIdList()) {
                HostMediaRecorder.EncoderSettings encoderSettings = settings.getEncoderSetting(encoderId);
                if (encoderId != null && mimeType.equals(encoderSettings.getMimeType().getValue())) {
                    encoderSettings.setPreviewBitRate(previewBitRate * 1024);
                }
            }

            isChangeConfig = true;
        }

        if (previewKeyFrameInterval != null) {
            for (String encoderId : settings.getEncoderIdList()) {
                HostMediaRecorder.EncoderSettings encoderSettings = settings.getEncoderSetting(encoderId);
                if (encoderId != null && mimeType.equals(encoderSettings.getMimeType().getValue())) {
                    encoderSettings.setPreviewKeyFrameInterval(previewKeyFrameInterval);
                }
            }

            isChangeConfig = true;
        }

        if (previewEncoder != null) {
            for (String encoderId : settings.getEncoderIdList()) {
                HostMediaRecorder.EncoderSettings encoderSettings = settings.getEncoderSetting(encoderId);
                if (encoderId != null && mimeType.equals(encoderSettings.getMimeType().getValue())) {
                    encoderSettings.setPreviewEncoder(previewEncoder);
                    encoderSettings.setProfileLevel(null);
                }
            }

            isChangeConfig = true;
        }

        if (profileLevel != null) {
            for (String encoderId : settings.getEncoderIdList()) {
                HostMediaRecorder.EncoderSettings encoderSettings = settings.getEncoderSetting(encoderId);
                if (encoderId != null && mimeType.equals(encoderSettings.getMimeType().getValue())) {
                    encoderSettings.setProfileLevel(profileLevel);
                }
            }

            isChangeConfig = true;
        }

        if (previewIntraRefresh != null) {
            for (String encoderId : settings.getEncoderIdList()) {
                HostMediaRecorder.EncoderSettings encoderSettings = settings.getEncoderSetting(encoderId);
                if (encoderId != null && mimeType.equals(encoderSettings.getMimeType().getValue())) {
                    encoderSettings.setIntraRefresh(previewIntraRefresh);
                }
            }

            isChangeConfig = true;
        }

        if (previewJpegQuality != null) {
            for (String encoderId : settings.getEncoderIdList()) {
                HostMediaRecorder.EncoderSettings encoderSettings = settings.getEncoderSetting(encoderId);
                if (encoderId != null && mimeType.equals(encoderSettings.getMimeType().getValue())) {
                    encoderSettings.setPreviewQuality((int) (previewJpegQuality * 100));
                }
            }
        }

        if (previewClipReset != null && previewClipReset) {
            for (String encoderId : settings.getEncoderIdList()) {
                HostMediaRecorder.EncoderSettings encoderSettings = settings.getEncoderSetting(encoderId);
                if (encoderId != null && mimeType.equals(encoderSettings.getMimeType().getValue())) {
                    clearCrop(recorder, encoderId);
                }
            }
        } else if (previewClipLeft != null) {
            for (String encoderId : settings.getEncoderIdList()) {
                HostMediaRecorder.EncoderSettings encoderSettings = settings.getEncoderSetting(encoderId);
                if (encoderId != null && mimeType.equals(encoderSettings.getMimeType().getValue())) {
                    setCrop(recorder, encoderId, previewClipLeft, previewClipTop,
                            previewClipRight, previewClipBottom, previewClipDuration);
                }
            }
        }

        if (isChangeConfig) {
            try {
                recorder.onConfigChange();
            } catch (Exception e) {
                MessageUtils.setIllegalDeviceStateError(response, "Failed to change a config.");
                return;
            }
        }

        setResult(response, DConnectMessage.RESULT_OK);
    }

    private void setCrop(HostMediaRecorder recorder, String encoderId, int left, int top, int right, int bottom, int duration) {
        HostMediaRecorder.EncoderSettings encoderSettings = recorder.getSettings().getEncoderSetting(encoderId);
        if (encoderSettings == null) {
            return;
        }

        Rect start = encoderSettings.getCropRect();
        if (start == null) {
            int width = recorder.getSettings().getPreviewSize().getWidth();
            int height = recorder.getSettings().getPreviewSize().getHeight();
            start = new Rect(0, 0, width, height);
        }

        Rect end = new Rect(left, top, right, bottom);

        for (LiveStreaming previewServer : recorder.getServerProvider().getLiveStreamingList()) {
            if (encoderId.equals(previewServer.getId())) {
                ((CropInterface) previewServer).moveCropRect(start, end, duration);
            }
        }

        for (LiveStreaming broadcaster : recorder.getBroadcasterProvider().getLiveStreamingList()) {
            if (encoderId.equals(broadcaster.getId())) {
                ((CropInterface) broadcaster).moveCropRect(start, end, duration);
            }
        }
    }

    private void clearCrop(HostMediaRecorder recorder, String encoderId) {
        for (LiveStreaming previewServer : recorder.getServerProvider().getLiveStreamingList()) {
            if (encoderId.equals(previewServer.getId())) {
                ((CropInterface) previewServer).setCropRect(null);
            }
        }

        for (LiveStreaming broadcaster : recorder.getBroadcasterProvider().getLiveStreamingList()) {
            if (encoderId.equals(broadcaster.getId())) {
                ((CropInterface) broadcaster).setCropRect(null);
            }
        }
    }

    private HostMediaRecorder.ProfileLevel convertProfileLevel(HostMediaRecorder recorder, String codec, String profile, String level) {
        if (profile != null && level != null) {
            HostMediaRecorder.VideoCodec codec1 = HostMediaRecorder.VideoCodec.H264;
            if (codec != null) {
                codec1 = HostMediaRecorder.VideoCodec.nameOf(codec);
            }
            switch (codec1) {
                case H264: {
                    H264Profile p = H264Profile.nameOf(profile);
                    H264Level l = H264Level.nameOf(level);
                    if (p == null || l == null || !recorder.getSettings().isSupportedProfileLevel(codec1, p.getValue(), l.getValue())) {
                        return null;
                    }
                    return new HostMediaRecorder.ProfileLevel(p.getValue(), l.getValue());
                }
                case H265: {
                    H265Profile p = H265Profile.nameOf(profile);
                    H265Level l = H265Level.nameOf(level);
                    if (p == null || l == null || !recorder.getSettings().isSupportedProfileLevel(codec1, p.getValue(), l.getValue())) {
                        return null;
                    }
                    return new HostMediaRecorder.ProfileLevel(p.getValue(), l.getValue());
                }
            }
        }
        return null;
    }

    private Bundle createMediaRecorderInfo(HostMediaRecorder recorder) {
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

        // 静止画の解像度
        Size pictureSize = settings.getPictureSize();
        if (pictureSize != null) {
            setRecorderImageWidth(info, pictureSize.getWidth());
            setRecorderImageHeight(info, pictureSize.getHeight());
        }

        // プレビュー解像度
        Size previewSize = settings.getPreviewSize();
        if (previewSize != null) {
            setRecorderPreviewWidth(info, previewSize.getWidth());
            setRecorderPreviewHeight(info, previewSize.getHeight());
        }

        // カメラのフレームレート
        Range<Integer> previewFps = settings.getPreviewFps();
        if (previewFps != null) {
            info.putString("previewFps", previewFps.getLower() + "-" + previewFps.getUpper());
            info.putInt("previewMaxFrameRate", previewFps.getUpper());
        }

        // エンコーダ設定
        List<Bundle> encoders = new ArrayList<>();
        for (String encoderId : settings.getEncoderIdList()) {
            HostMediaRecorder.EncoderSettings s = settings.getEncoderSetting(encoderId);
            if (s != null) {
                encoders.add(createVideoEncoder(s));
            }
        }
        info.putParcelableArray("encoders", encoders.toArray(new Bundle[0]));

        // 各機能の状態
        Bundle status = new Bundle();
        status.putBoolean("preview", recorder.isPreviewRunning());
        status.putBoolean("broadcast", recorder.isBroadcasterRunning());
        status.putBoolean("recording", recorder.getState() == HostMediaRecorder.State.RECORDING);
        info.putParcelable("status", status);

        info.putString("audioSource", settings.getPreviewAudioSource().getValue());
        info.putInt("audioBitrate", settings.getPreviewAudioBitRate() / 1024);
        info.putInt("audioSampleRate", settings.getPreviewSampleRate());
        info.putInt("audioChannel", settings.getPreviewChannel());
        info.putBoolean("audioEchoCanceler", settings.isUseAEC());

        setRecorderConfig(info, "");
        return info;
    }

    private Bundle createVideoEncoder(HostMediaRecorder.EncoderSettings s) {
        Bundle bundle = new Bundle();
        bundle.putString("name", s.getName());
        bundle.putString("mimeType", s.getMimeType().getValue());
        bundle.putInt("width", s.getPreviewSize().getWidth());
        bundle.putInt("height", s.getPreviewSize().getHeight());
        bundle.putInt("bitrate", s.getPreviewBitRate() / 1024);

        if ("video/x-mjpeg".equals(s.getMimeType().getValue())) {
            bundle.putFloat("jpegQuality", s.getPreviewQuality() / 100.0f);
        } else if (s.getMimeType().getValue().startsWith("video/")) {
            bundle.putInt("keyFrameInterval", s.getPreviewKeyFrameInterval());
            bundle.putString("encoder", s.getPreviewEncoder());
            HostMediaRecorder.ProfileLevel pl = s.getProfileLevel();
            if (pl != null) {
                switch (HostMediaRecorder.VideoCodec.nameOf(s.getPreviewEncoder())) {
                    case H264:
                        bundle.putString("previewProfile", H264Profile.valueOf(pl.getProfile()).getName());
                        bundle.putString("previewLevel", H264Level.valueOf(pl.getLevel()).getName());
                        break;
                    case H265:
                        bundle.putString("previewProfile", H265Profile.valueOf(pl.getProfile()).getName());
                        bundle.putString("previewLevel", H265Level.valueOf(pl.getLevel()).getName());
                        break;
                }
            }
            bundle.putBoolean("useSoftwareEncoder", s.isUseSoftwareEncoder());
            Integer intraRefresh = s.getIntraRefresh();
            if (intraRefresh != null) {
                bundle.putInt("intraRefresh", intraRefresh);
            }
        }

        if (s.getPort() > 0) {
            bundle.putInt("port", s.getPort());
        }

        // 切り抜き設定
        Rect rect = s.getCropRect();
        if (rect != null) {
            Bundle drawingRect = new Bundle();
            drawingRect.putInt("left", rect.left);
            drawingRect.putInt("top", rect.top);
            drawingRect.putInt("right", rect.right);
            drawingRect.putInt("bottom", rect.bottom);
            bundle.putBundle("crop", drawingRect);
        }

        return bundle;
    }

    private Bundle createRecorderOption(HostMediaRecorder recorder) {
        Bundle bundle = new Bundle();
        HostMediaRecorder.Settings settings = recorder.getSettings();
        setRecorderId(bundle, recorder.getId());
        setRecorderName(bundle, recorder.getName());
        if (recorder.getMimeType().startsWith("image/") || recorder.getMimeType().startsWith("video/")) {
            setSupportedImageSizes(bundle, settings.getSupportedPictureSizes());
            setSupportedPreviewSizes(bundle, settings.getSupportedPreviewSizes());
            setSupportedVideoEncoders(bundle, settings.getSupportedVideoEncoders());
            setSupportedFps(bundle, settings.getSupportedFps());
        }
        setMIMEType(bundle, recorder.getSupportedMimeTypes());
        return bundle;
    }

    private static Bundle[] createSupportedImageSizes(List<Size> sizes) {
        Bundle[] array = new Bundle[sizes.size()];
        int i = 0;
        for (Size size : sizes) {
            Bundle info = new Bundle();
            setWidth(info, size.getWidth());
            setHeight(info, size.getHeight());
            array[i++] = info;
        }
        return array;
    }

    /**
     * サポートしている静止画の解像度をレスポンスに格納します.
     *
     * @param response 静止画の解像度を格納するレスポンス
     * @param sizes サポートしている静止画の解像度のリスト
     */
    private static void setSupportedImageSizes(final Intent response, final List<Size> sizes) {
        setImageSizes(response, createSupportedImageSizes(sizes));
    }

    /**
     * サポートしている静止画の解像度をレスポンスに格納します.
     *
     * @param bundle 静止画の解像度を格納するレスポンス
     * @param sizes サポートしている静止画の解像度のリスト
     */
    private static void setSupportedImageSizes(final Bundle bundle, final List<Size> sizes) {
        bundle.putParcelableArray("imageSizes", createSupportedImageSizes(sizes));
    }

    private static Bundle[] createSupportedPreviewSizes(List<Size> sizes) {
        Bundle[] array = new Bundle[sizes.size()];
        int i = 0;
        for (Size size : sizes) {
            Bundle info = new Bundle();
            setWidth(info, size.getWidth());
            setHeight(info, size.getHeight());
            array[i++] = info;
        }
        return array;
    }

    /**
     * サポートしているプレビューの解像度をレスポンスに格納します.
     *
     * @param response プレビューの解像度を格納するレスポンス
     * @param sizes サポートしているプレビューの解像度のリスト
     */
    private static void setSupportedPreviewSizes(final Intent response, final List<Size> sizes) {
        setPreviewSizes(response, createSupportedPreviewSizes(sizes));
    }

    /**
     * サポートしているプレビューの解像度をレスポンスに格納します.
     *
     * @param bundle プレビューの解像度を格納するレスポンス
     * @param sizes サポートしているプレビューの解像度のリスト
     */
    private static void setSupportedPreviewSizes(final Bundle bundle, final List<Size> sizes) {
        bundle.putParcelableArray("previewSizes", createSupportedPreviewSizes(sizes));
    }

    private static Bundle[] createSupportedVideoEncoders(List<String> encoderNames) {
        List<Bundle> encoders = new ArrayList<>();
        for (String name : encoderNames) {
            HostMediaRecorder.VideoCodec videoCodec = HostMediaRecorder.VideoCodec.nameOf(name);
            Size maxSize = CapabilityUtil.getSupportedMaxSize(videoCodec.getMimeType());
            Bundle encoder = new Bundle();
            encoder.putString("name", name);
            encoder.putParcelableArray("profileLevel", getProfileLevels(videoCodec));
            if (maxSize != null) {
                encoder.putInt("maxWidth", maxSize.getWidth());
                encoder.putInt("maxHeight", maxSize.getHeight());
            }
            encoders.add(encoder);
        }
        return encoders.toArray(new Bundle[0]);
    }

    /**
     * サポートしているエンコーダをレスポンスに格納します.
     *
     * @param response レスポンス
     * @param encoderNames エンコーダのリスト
     */
    private static void setSupportedVideoEncoders(Intent response, List<String> encoderNames) {
        response.putExtra("encoder", createSupportedVideoEncoders(encoderNames));
    }

    /**
     * サポートしているエンコーダをレスポンスに格納します.
     *
     * @param bundle レスポンス
     * @param encoderNames エンコーダのリスト
     */
    private static void setSupportedVideoEncoders(Bundle bundle, List<String> encoderNames) {
        bundle.putParcelableArray("encoder", createSupportedVideoEncoders(encoderNames));
    }

    /**
     * エンコーダがサポートしているプロファイルとレベルを格納した Bundle の配列を取得します.
     *
     * @param videoCodec エンコーダ
     * @return プロファイルとレベルを格納した Bundle の配列
     */
    private static Bundle[] getProfileLevels(HostMediaRecorder.VideoCodec videoCodec) {
        List<Bundle> list = new ArrayList<>();
        for (HostMediaRecorder.ProfileLevel pl : CapabilityUtil.getSupportedProfileLevel(videoCodec.getMimeType())) {
            switch (videoCodec) {
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

    private static String[] createSupportedFps(List<Range<Integer>> supportedFps) {
        List<String> fpsList = new ArrayList<>();
        for (Range<Integer> fps : supportedFps) {
            fpsList.add(fps.getLower() + "-" + fps.getUpper());
        }
        return fpsList.toArray(new String[0]);
    }

    /**
     * カメラがサポートしている Fps のリストをレスポンスに格納します.
     *
     * @param response レスポンス
     * @param supportedFps サポートしている fps のリスト
     */
    private static void setSupportedFps(Intent response, List<Range<Integer>> supportedFps) {
        response.putExtra("frameRate", createSupportedFps(supportedFps));
    }

    /**
     * カメラがサポートしている Fps のリストをレスポンスに格納します.
     *
     * @param bundle レスポンス
     * @param supportedFps サポートしている fps のリスト
     */
    private static void setSupportedFps(Bundle bundle, List<Range<Integer>> supportedFps) {
        bundle.putStringArray("frameRate", createSupportedFps(supportedFps));
    }

    /**
     * 指定されたマイムタイプがレコーダでサポートされているか確認します.
     *
     * @param recorder レコーダ
     * @param mimeType マイムタイプ
     * @return サポートされている場合はtrue、それ以外はfalse
     */
    private boolean isSupportedMimeType(final HostMediaRecorder recorder, final String mimeType) {
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
