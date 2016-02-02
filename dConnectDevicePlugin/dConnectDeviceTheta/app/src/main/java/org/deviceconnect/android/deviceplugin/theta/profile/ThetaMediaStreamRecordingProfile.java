/*
 ThetaMediaStreamRecordingProfile
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.profile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.theta.ThetaDeviceService;
import org.deviceconnect.android.deviceplugin.theta.core.LiveCamera;
import org.deviceconnect.android.deviceplugin.theta.core.LivePreviewTask;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDevice;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceClient;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceException;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceModel;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaObject;
import org.deviceconnect.android.deviceplugin.theta.profile.param.IntegerParamDefinition;
import org.deviceconnect.android.deviceplugin.theta.profile.param.ParamDefinitionSet;
import org.deviceconnect.android.deviceplugin.theta.utils.BitmapUtils;
import org.deviceconnect.android.deviceplugin.theta.utils.MixedReplaceMediaServer;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.MediaStreamRecordingProfile;
import org.deviceconnect.android.provider.FileManager;
import org.deviceconnect.message.DConnectMessage;

import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Theta MediaStream Recording Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaMediaStreamRecordingProfile extends MediaStreamRecordingProfile {

    private static final String PARAM_WIDTH = "width";
    private static final String PARAM_HEIGHT = "height";

    private static final String SEGMENT_LIVE_PREVIEW = "1";

    private final ThetaDeviceClient mClient;
    private final FileManager mFileMgr;
    private final Object mLockObj = new Object();
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final ParamDefinitionSet mPreviewParamSet;
    {
        mPreviewParamSet = new ParamDefinitionSet();
        mPreviewParamSet.add(new IntegerParamDefinition(PARAM_WIDTH, new IntegerParamDefinition.Range() {
            @Override
            public boolean validate(final int value) {
                return 0 < value;
            }
        }));
        mPreviewParamSet.add(new IntegerParamDefinition(PARAM_HEIGHT, new IntegerParamDefinition.Range() {
            @Override
            public boolean validate(final int value) {
                return 0 < value;
            }
        }));
    }

    private LivePreviewTask mLivePreviewTask;
    private MixedReplaceMediaServer mServer;

    /**
     * Constructor.
     *
     * @param client an instance of {@link ThetaDeviceClient}
     * @param fileMgr an instance of {@link FileManager}
     */
    public ThetaMediaStreamRecordingProfile(final ThetaDeviceClient client,
                                            final FileManager fileMgr) {
        mClient = client;
        mFileMgr = fileMgr;
    }

    @Override
    protected boolean onGetMediaRecorder(final Intent request, final Intent response,
                                         final String serviceId) {
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

    @Override
    protected boolean onPostTakePhoto(final Intent request, final Intent response,
                                      final String serviceId, final String target) {
        mClient.takePicture(serviceId, target, new ThetaDeviceClient.DefaultListener() {

            @Override
            public void onTakenPicture(final ThetaObject picture) {
                try {
                    picture.fetch(ThetaObject.DataType.MAIN);
                    byte[] data = picture.getMainData();
                    picture.clear(ThetaObject.DataType.MAIN);

                    mFileMgr.saveFile(picture.getFileName(), data, new FileManager.SaveFileCallback() {

                        @Override
                        public void onSuccess(final String uri) {
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
                    default:
                        MessageUtils.setUnknownError(response, cause.getMessage());
                        break;
                }
                sendResponse(response);
            }

        });
        return false;
    }

    @Override
    protected boolean onPutOnPhoto(final Intent request, final Intent response,
                                   final String serviceId, final String sessionKey) {
        if (!mClient.hasDevice(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response, "Not found sessionKey:" + sessionKey);
        } else {
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else if (error == EventError.INVALID_PARAMETER) {
                MessageUtils.setInvalidRequestParameterError(response);
            } else {
                MessageUtils.setUnknownError(response);
            }
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnPhoto(final Intent request, final Intent response,
                                      final String serviceId, final String sessionKey) {
        if (!mClient.hasDevice(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response, "There is no sessionKey.");
        } else {
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
        }
        return true;
    }

    @Override
    protected boolean onPostRecord(final Intent request, final Intent response,
                                   final String serviceId, final String target,
                                   final Long timeslice) {
        if (timeslice != null && timeslice <= 0) {
            MessageUtils.setInvalidRequestParameterError(response, "timeslice must be more than 0.");
            return true;
        }

        mClient.startVideoRecording(serviceId, target, new ThetaDeviceClient.DefaultListener() {

            @Override
            public void onStartedVideoRecording(final ThetaDevice.Recorder recorder,
                                                final boolean hasStarted) {
                if (hasStarted) {
                    MessageUtils.setIllegalDeviceStateError(response, "Video recording has started already.");
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
                    default:
                        MessageUtils.setUnknownError(response, cause.getMessage());
                        break;
                }
                sendResponse(response);
            }

        });
        return false;
    }

    @Override
    protected boolean onPutStop(final Intent request, final Intent response,
                                final String serviceId, final String target) {
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
                    default:
                        MessageUtils.setUnknownError(response, cause.getMessage());
                        break;
                }
                sendResponse(response);
            }

        });
        return false;
    }

    @Override
    protected boolean onPutPreview(final Intent request, final Intent response, final String serviceId) {
        try {
            ThetaDevice device = mClient.getConnectedDevice(serviceId);
            if (device.getModel() != ThetaDeviceModel.THETA_S) {
                MessageUtils.setNotSupportAttributeError(response);
                return true;
            }
            if (!mPreviewParamSet.validateRequest(request, response)) {
                return true;
            }

            String uri = startLivePreview(device, getWidth(request), getHeight(request));
            setUri(response, uri);
            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        } catch (ThetaDeviceException cause) {
            switch (cause.getReason()) {
                case ThetaDeviceException.NOT_FOUND_THETA:
                    MessageUtils.setNotFoundServiceError(response);
                    break;
                default:
                    MessageUtils.setUnknownError(response, cause.getMessage());
                    break;
            }
            return true;
        }
    }

    @Override
    protected boolean onDeletePreview(final Intent request, final Intent response, final String serviceId) {
        if (!mClient.hasDevice(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }
        stopLivePreview();
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    private String startLivePreview(final LiveCamera liveCamera, final Integer width, final Integer height) {
        synchronized (mLockObj) {
            if (mServer == null) {
                mServer = new MixedReplaceMediaServer();
                mServer.setServerName("Live Preview Server");
                mServer.start();
            }
            final String segment = SEGMENT_LIVE_PREVIEW;
            if (mLivePreviewTask == null) {
                mLivePreviewTask = new LivePreviewTask(liveCamera) {
                    @Override
                    protected void onFrame(final byte[] frame) {
                        byte[] b;
                        if (width != null || height != null) {
                            b = resizeFrame(frame, width, height);
                        } else {
                            b = frame;
                        }
                        offerFrame(segment, b);
                    }
                };
                mExecutor.execute(mLivePreviewTask);
            }
            return mServer.getUrl() + "/" + segment;
        }
    }

    private byte[] resizeFrame(final byte[] frame, final Integer newWidth, final Integer newHeight) {
        Bitmap preview = BitmapFactory.decodeByteArray(frame, 0, frame.length);
        int w = newWidth != null ? newWidth : preview.getWidth();
        int h = newHeight != null ? newHeight : preview.getHeight();
        Bitmap resized = BitmapUtils.resize(preview, w, h);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        return baos.toByteArray();
    }

    private void offerFrame(final String segment, final byte[] frame) {
        synchronized (mLockObj) {
            if (mServer != null) {
                mServer.offerMedia(segment, frame);
            }
        }
    }

    private void stopLivePreview() {
        synchronized (mLockObj) {
            if (mLivePreviewTask != null) {
                mLivePreviewTask.stop();
                mLivePreviewTask = null;
            }
            if (mServer != null) {
                mServer.stop();
                mServer = null;
            }
        }
    }

    @Override
    protected boolean onPutOnRecordingChange(final Intent request, final Intent response,
                                             final String serviceId, final String sessionKey) {
        if (!mClient.hasDevice(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response, "Not found sessionKey:" + sessionKey);
        } else {
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else if (error == EventError.INVALID_PARAMETER) {
                MessageUtils.setInvalidRequestParameterError(response);
            } else {
                MessageUtils.setUnknownError(response);
            }
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnRecordingChange(final Intent request, final Intent response,
                                                final String serviceId, final String sessionKey) {
        if (!mClient.hasDevice(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response, "There is no sessionKey.");
        } else {
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
        }
        return true;
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
            getService().sendEvent(message, event.getAccessToken());
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
            getService().sendEvent(message, event.getAccessToken());
        }
    }

    private List<Event> getOnPhotoEventList(final String serviceId) {
        return EventManager.INSTANCE.getEventList(serviceId, PROFILE_NAME, null, ATTRIBUTE_ON_PHOTO);
    }

    private List<Event> getOnRecordingChangeEventList(final String serviceId) {
        return EventManager.INSTANCE.getEventList(serviceId, PROFILE_NAME, null, ATTRIBUTE_ON_RECORDING_CHANGE);
    }

    private ThetaDeviceService getService() {
        return ((ThetaDeviceService) getContext());
    }

    private static Integer getWidth(final Intent request) {
        return parseInteger(request, PARAM_WIDTH);
    }

    private static Integer getHeight(final Intent request) {
        return parseInteger(request, PARAM_HEIGHT);
    }
}
