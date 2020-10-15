/*
 ThetaMediaStreamRecordingProfile
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.profile;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import org.deviceconnect.android.deviceplugin.theta.ThetaDeviceService;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDevice;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceClient;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceException;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaObject;
import org.deviceconnect.android.deviceplugin.theta.core.preview.PreviewServer;
import org.deviceconnect.android.deviceplugin.theta.core.preview.PreviewServerProvider;
import org.deviceconnect.android.deviceplugin.theta.utils.MediaSharing;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.SSLContext;

/**
 * Theta MediaStream Recording Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class ThetaMediaStreamRecordingProfile extends MediaStreamRecordingProfile {

    private static final String PARAM_WIDTH = "width";
    private static final String PARAM_HEIGHT = "height";

    private final ThetaDeviceClient mClient;
    private final FileManager mFileMgr;
    private final MediaSharing mMediaSharing = MediaSharing.getInstance();
    private final Object mLockObj = new Object();

    protected final DConnectApi mGetMediaRecorderApi = new GetApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_MEDIARECORDER;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String serviceId = getServiceID(request);
            mClient.fetchRecorder(serviceId, new ThetaDeviceClient.DefaultListener() {

                @Override
                public void onRecorder(final ThetaDevice.Recorder recorder) {
                    List<Bundle> recorders = new LinkedList<Bundle>();
                    if (recorder != null) {
                        Bundle r = new Bundle();
                        setRecorderId(r, recorder.getId());
                        setRecorderName(r, recorder.getName());
                        setRecorderImageWidth(r, recorder.getImageWidth());
                        setRecorderImageHeight(r, recorder.getImageHeight());
                        if (recorder.supportsPreview()) {
                            setRecorderPreviewWidth(r, recorder.getPreviewWidth());
                            setRecorderPreviewHeight(r, recorder.getPreviewHeight());
                            setRecorderPreviewMaxFrameRate(r, recorder.getPreviewMaxFrameRate());
                        }
                        setRecorderMIMEType(r, recorder.getMimeType());
                        setRecorderConfig(r, "");
                        try {
                            ThetaDevice.RecorderState state = recorder.getState();
                            switch (state) {
                                case RECORDING:
                                    setRecorderState(r, RecorderState.RECORDING);
                                    break;
                                case INACTIVE:
                                    setRecorderState(r, RecorderState.INACTIVE);
                                    break;
                                default:
                                    break;
                            }
                        } catch (ThetaDeviceException e) {
                            onFailed(e);
                            return;
                        }
                        recorders.add(r);
                    }
                    setRecorders(response, recorders.toArray(new Bundle[recorders.size()]));
                    setResult(response, DConnectMessage.RESULT_OK);
                    sendResponse(response);
                }

                @Override
                public void onFailed(final ThetaDeviceException cause) {
                    switch (cause.getReason()) {
                        case ThetaDeviceException.NOT_FOUND_THETA:
                            MessageUtils.setNotFoundServiceError(response);
                            break;
                        default:
                            MessageUtils.setUnknownError(response, cause.getMessage());
                            break;
                    }
                    sendResponse(response);
                }

            });
            return false;
        }
    };

    protected final DConnectApi mPostTakePhotoApi = new PostApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_TAKE_PHOTO;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final String serviceId = getServiceID(request);
            final String target = getTarget(request);
            mClient.takePicture(serviceId, target, new ThetaDeviceClient.DefaultListener() {

                @Override
                public void onTakenPicture(final ThetaObject picture) {
                    try {
                        picture.fetch(ThetaObject.DataType.MAIN);
                        byte[] data = picture.getMainData();
                        picture.clear(ThetaObject.DataType.MAIN);

                        mFileMgr.saveFile(picture.getFileName(), data, true, new FileManager.SaveFileCallback() {

                            @Override
                            public void onSuccess(final @NonNull String uri) {
                                mMediaSharing.sharePhoto(getContext(), new File(mFileMgr.getBasePath(), picture.getFileName()));

                                String path = "/" + picture.getFileName();
                                setUri(response, uri);
                                setPath(response, path);
                                setResult(response, DConnectMessage.RESULT_OK);
                                sendResponse(response);

                                sendOnPhotoEvent(serviceId, picture.getMimeType(), uri, path);
                            }

                            @Override
                            public void onFail(final Throwable e) {
                                MessageUtils.setUnknownError(response, e.getMessage());
                                sendResponse(response);
                            }
                        });
                    } catch (ThetaDeviceException e) {
                        onFailed(e);
                    }
                }

                @Override
                public void onFailed(final ThetaDeviceException cause) {
                    switch (cause.getReason()) {
                        case ThetaDeviceException.NOT_FOUND_THETA:
                            MessageUtils.setNotFoundServiceError(response);
                            break;
                        case ThetaDeviceException.NOT_FOUND_RECORDER:
                            MessageUtils.setInvalidRequestParameterError(response, cause.getMessage());
                            break;
                        case ThetaDeviceException.NOT_SUPPORTED_FEATURE:
                            MessageUtils.setNotSupportAttributeError(response, cause.getMessage());
                            break;
                        default:
                            MessageUtils.setUnknownError(response, cause.getMessage());
                            break;
                    }
                    sendResponse(response);
                }

            });
            return false;
        }
    };

    protected final DConnectApi mPutOnPhotoApi = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_PHOTO;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else if (error == EventError.INVALID_PARAMETER) {
                MessageUtils.setInvalidRequestParameterError(response);
            } else {
                MessageUtils.setUnknownError(response);
            }
            return true;
        }
    };

    protected final DConnectApi mDeleteOnPhotoApi = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_PHOTO;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else if (error == EventError.INVALID_PARAMETER) {
                MessageUtils.setInvalidRequestParameterError(response);
            } else if (error == EventError.FAILED) {
                MessageUtils.setUnknownError(response, "Failed to delete event from cache");
            } else if (error == EventError.NOT_FOUND) {
                MessageUtils.setUnknownError(response, "Not found event.");
            } else {
                MessageUtils.setUnknownError(response);
            }
            return true;
        }
    };

    protected final DConnectApi mPostRecordApi = new PostApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_RECORD;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final String serviceId = getServiceID(request);
            final String target = getTarget(request);
            Long timeslice = getTimeSlice(request);
            if (timeslice != null && timeslice < 0) {
                MessageUtils.setInvalidRequestParameterError(response,
                        "Invalid timeslice");
                return true;
            }
            mClient.startVideoRecording(serviceId, target, new ThetaDeviceClient.DefaultListener() {

                @Override
                public void onStartedVideoRecording(final ThetaDevice.Recorder recorder,
                                                    final boolean hasStarted) {
                    if (hasStarted) {
                        MessageUtils.setIllegalDeviceStateError(response, "Video recording has started already.");
                        sendResponse(response);
                    } else {
                        setResult(response, DConnectMessage.RESULT_OK);
                        sendResponse(response);
                        sendOnRecordingChangeEvent(serviceId, recorder, RecordingState.RECORDING);
                    }
                }

                @Override
                public void onFailed(final ThetaDeviceException cause) {
                    switch (cause.getReason()) {
                        case ThetaDeviceException.NOT_FOUND_THETA:
                            MessageUtils.setNotFoundServiceError(response);
                            break;
                        case ThetaDeviceException.NOT_FOUND_RECORDER:
                            MessageUtils.setInvalidRequestParameterError(response, "recorder is not found.");
                            break;
                        case ThetaDeviceException.NOT_SUPPORTED_FEATURE:
                            MessageUtils.setNotSupportAttributeError(response, cause.getMessage());
                            break;
                        default:
                            MessageUtils.setUnknownError(response, cause.getMessage());
                            break;
                    }
                    sendResponse(response);
                }

            });
            return false;
        }
    };

    protected final DConnectApi mPutStopApi = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_STOP;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final String serviceId = getServiceID(request);
            final String target = getTarget(request);
            mClient.stopVideoRecording(serviceId, target, new ThetaDeviceClient.DefaultListener() {

                @Override
                public void onStoppedVideoRecording(final ThetaDevice.Recorder recorder,
                                                    final boolean hasStopped) {
                    if (hasStopped) {
                        MessageUtils.setIllegalDeviceStateError(response, "Video recording has stopped already.");
                        sendResponse(response);
                    } else {
                        setResult(response, DConnectMessage.RESULT_OK);
                        sendResponse(response);
                        sendOnRecordingChangeEvent(serviceId, recorder, RecordingState.STOP);
                    }
                }

                @Override
                public void onFailed(final ThetaDeviceException cause) {
                    switch (cause.getReason()) {
                        case ThetaDeviceException.NOT_FOUND_THETA:
                            MessageUtils.setNotFoundServiceError(response);
                            break;
                        case ThetaDeviceException.NOT_FOUND_RECORDER:
                            MessageUtils.setInvalidRequestParameterError(response, "recorder is not found.");
                            break;
                        case ThetaDeviceException.NOT_SUPPORTED_FEATURE:
                            MessageUtils.setNotSupportAttributeError(response, cause.getMessage());
                            break;
                        default:
                            MessageUtils.setUnknownError(response, cause.getMessage());
                            break;
                    }
                    sendResponse(response);
                }

            });
            return false;
        }
    };

    protected final DConnectApi mPutPreviewApi = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_PREVIEW;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final String serviceId = getServiceID(request);
            final String target = getTarget(request);
            mClient.execute(() -> {
                try {
                    ThetaDevice device = mClient.getConnectedDevice(serviceId);
                    ThetaDevice.Recorder recorder = device.getRecorder();
                    if (recorder == null) {
                        MessageUtils.setIllegalDeviceStateError(response, "device is not initialized.");
                        return;
                    }
                    if (target != null && !target.equals(recorder.getId())) {
                        MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
                        return;
                    }
                    if (!recorder.supportsPreview()) {
                        MessageUtils.setNotSupportAttributeError(response,
                            recorder.getName() + " does not support preview.");
                        return;
                    }
                    ThetaDeviceService thetaDeviceService = (ThetaDeviceService) getContext();
                    thetaDeviceService.getSSLContext(new ThetaDeviceService.SSLContextCallback() {
                        @Override
                        public void onGet(SSLContext context) {
                            startPreviewServers(context, response, device);
                        }

                        @Override
                        public void onError() {
                            startPreviewServers(null, response, device);
                        }
                    });
                } catch (ThetaDeviceException cause) {
                    switch (cause.getReason()) {
                        case ThetaDeviceException.NOT_FOUND_THETA:
                            MessageUtils.setNotFoundServiceError(response);
                            break;
                        default:
                            MessageUtils.setUnknownError(response, cause.getMessage());
                            break;
                    }
                } finally {
                    sendResponse(response);
                }
            });
            return false;
        }
    };

    protected final DConnectApi mDeletePreviewApi = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_PREVIEW;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final String serviceId = getServiceID(request);
            final String target = getTarget(request);
            mClient.execute(() -> {
                try {
                    ThetaDevice device = mClient.getConnectedDevice(serviceId);
                    ThetaDevice.Recorder recorder = device.getRecorder();
                    if (recorder == null) {
                        MessageUtils.setIllegalDeviceStateError(response, "device is not initialized.");
                        return;
                    }
                    if (target != null && !target.equals(recorder.getId())) {
                        MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
                        return;
                    }
                    if (!recorder.supportsPreview()) {
                        MessageUtils.setNotSupportAttributeError(response,
                            recorder.getName() + " does not support preview.");
                        return;
                    }

                    PreviewServerProvider provider = device.getServerProvider();
                    provider.stopServers();
                    setResult(response, DConnectMessage.RESULT_OK);
                } catch (ThetaDeviceException cause) {
                    switch (cause.getReason()) {
                        case ThetaDeviceException.NOT_FOUND_THETA:
                            MessageUtils.setNotFoundServiceError(response);
                            break;
                        default:
                            MessageUtils.setUnknownError(response, cause.getMessage());
                            break;
                    }
                } finally {
                    sendResponse(response);
                }
            });
            return false;
        }
    };

    protected final DConnectApi mPutOnRecordingChangeApi = new PutApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_RECORDING_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else if (error == EventError.INVALID_PARAMETER) {
                MessageUtils.setInvalidRequestParameterError(response);
            } else {
                MessageUtils.setUnknownError(response);
            }
            return true;
        }
    };

    protected final DConnectApi mDeleteOnRecordingChangeApi = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_RECORDING_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else if (error == EventError.INVALID_PARAMETER) {
                MessageUtils.setInvalidRequestParameterError(response);
            } else if (error == EventError.FAILED) {
                MessageUtils.setUnknownError(response, "Failed to delete event from cache.");
            } else if (error == EventError.NOT_FOUND) {
                MessageUtils.setUnknownError(response, "Not found event.");
            } else {
                MessageUtils.setUnknownError(response);
            }
            return true;
        }
    };

    /**
     * Constructor.
     *
     * @param client an instance of {@link ThetaDeviceClient}
     * @param fileMgr an instance of {@link FileManager}
     */
    protected ThetaMediaStreamRecordingProfile(final ThetaDeviceClient client,
                                               final FileManager fileMgr) {
        mClient = client;
        mFileMgr = fileMgr;
        addApi(mGetMediaRecorderApi);
        addApi(mPostTakePhotoApi);
        addApi(mPutOnPhotoApi);
        addApi(mDeleteOnPhotoApi);
        addApi(mPostRecordApi);
        addApi(mPutStopApi);
        addApi(mPutOnRecordingChangeApi);
        addApi(mDeleteOnRecordingChangeApi);
    }
    private void startPreviewServers(final SSLContext sslContext,
                                     final Intent response,
                                     final ThetaDevice recorder) {

        // SSLContext を設定
        for (PreviewServer server : recorder.getServerProvider().getServers()) {
            if (sslContext != null && server.usesSSLContext()) {
                server.setSSLContext(sslContext);
            }
        }

        List<PreviewServer> servers = recorder.startPreviews();
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
    }


    private void sendOnPhotoEvent(final String serviceId, final String mimeType, final String uri, final String path) {
        List<Event> events = getOnPhotoEventList(serviceId);
        mLogger.info("Send onphoto events: " + events.size());
        for (Iterator<Event> it = events.iterator(); it.hasNext(); ) {
            Event event = it.next();
            Intent message = EventManager.createEventMessage(event);
            Bundle photoInfo = new Bundle();
            setUri(photoInfo, uri);
            setPath(photoInfo, path);
            setMIMEType(photoInfo, mimeType);
            setPhoto(message, photoInfo);
            sendEvent(message, event.getAccessToken());
        }
    }

    private void sendOnRecordingChangeEvent(final String serviceId,
                                            final ThetaDevice.Recorder recorder,
                                            final RecordingState state) {
        List<Event> events = getOnRecordingChangeEventList(serviceId);
        mLogger.info("Send onrecordingchange events: " + events.size());
        for (Iterator<Event> it = events.iterator(); it.hasNext(); ) {
            Event event = it.next();
            Intent message = EventManager.createEventMessage(event);
            Bundle media = new Bundle();
            setStatus(media, state);
            setMIMEType(media, recorder.getMimeType());
            setMedia(message, media);
            sendEvent(message, event.getAccessToken());
        }
    }

    private List<Event> getOnPhotoEventList(final String serviceId) {
        return EventManager.INSTANCE.getEventList(serviceId, PROFILE_NAME, null, ATTRIBUTE_ON_PHOTO);
    }

    private List<Event> getOnRecordingChangeEventList(final String serviceId) {
        return EventManager.INSTANCE.getEventList(serviceId, PROFILE_NAME, null, ATTRIBUTE_ON_RECORDING_CHANGE);
    }

    private static Integer getWidth(final Intent request) {
        return parseInteger(request, PARAM_WIDTH);
    }

    private static Integer getHeight(final Intent request) {
        return parseInteger(request, PARAM_HEIGHT);
    }

    public void forcedStopRecording() {
        /* 動画記録停止処理 */
        mClient.execute(() -> {
            try {
                ThetaDevice device = mClient.getCurrentConnectDevice();
                ThetaDevice.Recorder recorder = device.getRecorder();
                if (recorder != null && recorder.supportsVideoRecording()) {
                    ThetaDevice.RecorderState state = recorder.getState();
                    switch (state) {
                        case RECORDING:
                            device.stopVideoRecording();
                            break;
                        case INACTIVE:
                        default:
                            break;
                    }
                }
            } catch (ThetaDeviceException e) {
                // Not operation.
            }
        });

        /* プレビュー停止処理 */
        mClient.execute(() -> {
            try {
                ThetaDevice device = mClient.getCurrentConnectDevice();
                ThetaDevice.Recorder recorder = device.getRecorder();
                if (recorder != null && recorder.supportsPreview()) {
                    device.getServerProvider().stopServers();
                }
            } catch (ThetaDeviceException cause) {
                // Not operation.
            }
        });
    }
}
