/*
 ThetaMediaStreamRecordingProfile
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.profile;

import android.content.Intent;
import android.os.Bundle;

import com.theta360.lib.ThetaException;

import org.deviceconnect.android.deviceplugin.theta.RecorderInfo;
import org.deviceconnect.android.deviceplugin.theta.ThetaApi;
import org.deviceconnect.android.deviceplugin.theta.ThetaApiClient;
import org.deviceconnect.android.deviceplugin.theta.ThetaApiTask;
import org.deviceconnect.android.deviceplugin.theta.ThetaDeviceInfo;
import org.deviceconnect.android.deviceplugin.theta.ThetaDeviceService;
import org.deviceconnect.android.deviceplugin.theta.ThetaPhoto;
import org.deviceconnect.android.deviceplugin.theta.ThetaPhotoEventListener;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.MediaStreamRecordingProfile;
import org.deviceconnect.android.provider.FileManager;
import org.deviceconnect.message.DConnectMessage;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Theta MediaStream Recording Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaMediaStreamRecordingProfile extends MediaStreamRecordingProfile {

    private final ThetaApiClient mClient;
    private final FileManager mFileMgr;

    /**
     * Constructor.
     *
     * @param client an instance of {@link ThetaApiClient}
     * @param fileMgr an instance of {@link FileManager}
     */
    public ThetaMediaStreamRecordingProfile(final ThetaApiClient client,
                                            final FileManager fileMgr) {
        mClient = client;
        mFileMgr = fileMgr;
    }

    @Override
    protected boolean onGetMediaRecorder(final Intent request, final Intent response,
                                         final String serviceId) {
        final ThetaDeviceInfo deviceInfo = mClient.getDevice(serviceId);
        if (deviceInfo == null) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }
        mClient.execute(new ThetaApiTask() {
            @Override
            public void run(final ThetaApi api) {
                try {
                    short status = api.getRecordingStatus();

                    List<Bundle> recorders = new LinkedList<Bundle>();
                    Bundle recorder = new Bundle();
                    RecorderInfo recorderInfo = deviceInfo.getCurrentRecoderInfo();
                    setRecorderId(recorder, recorderInfo.mId);
                    setRecorderName(recorder, recorderInfo.mName);
                    setRecorderImageWidth(recorder, recorderInfo.mImageWidth);
                    setRecorderImageHeight(recorder, recorderInfo.mImageHeight);
                    setRecorderMIMEType(recorder, recorderInfo.mMimeType);
                    switch (status) {
                    case RecorderInfo.STATUS_RECORDING:
                        setRecorderState(recorder, RecorderState.RECORDING);
                        break;
                    case RecorderInfo.STATUS_INACTIVE:
                        setRecorderState(recorder, RecorderState.INACTIVE);
                        break;
                    default:
                        break;
                    }
                    setRecorderConfig(recorder, "");
                    recorders.add(recorder);
                    setRecorders(response, recorders.toArray(new Bundle[recorders.size()]));
                    setResult(response, DConnectMessage.RESULT_OK);
                } catch (ThetaException e) {
                    MessageUtils.setUnknownError(response, e.getMessage());
                } catch (IOException e) {
                    MessageUtils.setUnknownError(response, e.getMessage());
                }
                getService().sendResponse(response);
            }
        });
        return false;
    }

    @Override
    protected boolean onPostTakePhoto(final Intent request, final Intent response,
                                      final String serviceId, final String target) {
        ThetaDeviceInfo deviceInfo = mClient.getDevice(serviceId);
        if (deviceInfo == null) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }
        if (deviceInfo.getPhotoRecorderInfo(target) == null) {
            MessageUtils.setInvalidRequestParameterError(response);
            return true;
        }
        mClient.execute(new ThetaApiTask() {
            @Override
            public void run(final ThetaApi api) {
                try {
                    api.takePhoto(new ThetaPhotoEventListener() {
                        @Override
                        public void onPhoto(final ThetaPhoto photo) {
                            try {
                                String uri = mFileMgr.saveFile(photo.mFilename, photo.mData);
                                String path = "/" + photo.mFilename;

                                setUri(response, uri);
                                setPath(response, path);
                                setResult(response, DConnectMessage.RESULT_OK);
                                getService().sendResponse(response);

                                sendOnPhotoEvent(photo, uri, path);
                            } catch (IOException e) {
                                MessageUtils.setUnknownError(response, e.getMessage());
                                getService().sendResponse(response);
                            }
                        }

                        @Override
                        public void onError() {
                            MessageUtils.setUnknownError(response);
                            getService().sendResponse(response);
                        }
                    });
                } catch (IllegalStateException e) {
                    MessageUtils.setIllegalDeviceStateError(response, "Theta's current mode is not video mode.");
                } catch (ThetaException e) {
                    MessageUtils.setUnknownError(response, e.getMessage());
                    getService().sendResponse(response);
                } catch (IOException e) {
                    MessageUtils.setUnknownError(response, e.getMessage());
                    getService().sendResponse(response);
                }
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
        ThetaDeviceInfo deviceInfo = mClient.getDevice(serviceId);
        if (deviceInfo == null) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }
        final RecorderInfo recorder = deviceInfo.getVideoRecorderInfo(target);
        if (recorder == null) {
            MessageUtils.setInvalidRequestParameterError(response);
            return true;
        }
        if (timeslice != null && timeslice <= 0) {
            MessageUtils.setInvalidRequestParameterError(response, "timeslice must be more than 0.");
            return true;
        }
        mClient.execute(new ThetaApiTask() {
            @Override
            public void run(final ThetaApi api) {
                try {
                    api.startVideoRecording();
                    setResult(response, DConnectMessage.RESULT_OK);

                    sendOnRecordingChangeEvent(serviceId, recorder, RecordingState.RECORDING);
                } catch (IllegalStateException e) {
                    MessageUtils.setIllegalDeviceStateError(response, "Theta's current mode is not video mode.");
                } catch (ThetaException e) {
                    MessageUtils.setUnknownError(response, e.getMessage());
                } catch (IOException e) {
                    MessageUtils.setUnknownError(response, e.getMessage());
                }
                getService().sendResponse(response);
            }
        });
        return false;
    }

    @Override
    protected boolean onPutStop(final Intent request, final Intent response,
                                final String serviceId, final String target) {
        ThetaDeviceInfo deviceInfo = mClient.getDevice(serviceId);
        if (deviceInfo == null) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }
        final RecorderInfo recorder = deviceInfo.getVideoRecorderInfo(target);
        if (recorder == null) {
            MessageUtils.setInvalidRequestParameterError(response);
            return true;
        }
        mClient.execute(new ThetaApiTask() {
            @Override
            public void run(final ThetaApi api) {
                try {
                    if (api.stopVideoRecording()) {
                        setResult(response, DConnectMessage.RESULT_OK);

                        sendOnRecordingChangeEvent(serviceId, recorder, RecordingState.STOP);
                    } else {
                        MessageUtils.setIllegalDeviceStateError(response, "Video recording is stopped already.");
                    }
                } catch (ThetaException e) {
                    MessageUtils.setUnknownError(response, e.getMessage());
                } catch (IOException e) {
                    MessageUtils.setUnknownError(response, e.getMessage());
                }
                getService().sendResponse(response);
            }
        });
        return false;
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

    private void sendOnPhotoEvent(final ThetaPhoto photo, final String uri, final String path) {
        List<Event> events = getOnPhotoEventList(photo.mServiceId);
        mLogger.info("Send onphoto events: " + events.size());
        for (Iterator<Event> it = events.iterator(); it.hasNext(); ) {
            Event event = it.next();
            Intent message = EventManager.createEventMessage(event);
            Bundle photoInfo = new Bundle();
            setUri(photoInfo, uri);
            setPath(photoInfo, path);
            setMIMEType(photoInfo, photo.mMimeType);
            setPhoto(message, photoInfo);
            getService().sendEvent(message, event.getAccessToken());
        }
    }

    private void sendOnRecordingChangeEvent(final String serviceId, final RecorderInfo recorder, final RecordingState state) {
        List<Event> events = getOnRecordingChangeEventList(serviceId);
        mLogger.info("Send onrecordingchange events: " + events.size());
        for (Iterator<Event> it = events.iterator(); it.hasNext(); ) {
            Event event = it.next();
            Intent message = EventManager.createEventMessage(event);
            Bundle media = new Bundle();
            setStatus(media, state);
            setMIMEType(media, recorder.mMimeType);
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
}
