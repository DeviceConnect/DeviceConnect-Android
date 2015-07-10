package org.deviceconnect.android.deviceplugin.theta.profile;

import android.content.Intent;
import android.os.Bundle;

import com.theta360.lib.ThetaException;

import org.deviceconnect.android.deviceplugin.theta.ThetaApi;
import org.deviceconnect.android.deviceplugin.theta.ThetaApiClient;
import org.deviceconnect.android.deviceplugin.theta.ThetaApiTask;
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
import java.util.List;

/**
 * Theta MediaStream Recording Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaMediaStreamRecordingProfile extends MediaStreamRecordingProfile {

    private final ThetaApiClient mClient;
    private final FileManager mFileMgr;

    public ThetaMediaStreamRecordingProfile(final ThetaApiClient client,
                                            final FileManager fileMgr) {
        mClient = client;
        mFileMgr = fileMgr;
    }

    @Override
    protected boolean onPostTakePhoto(final Intent request, final Intent response,
                                      final String serviceId, final String target) {
        if (!mClient.hasDevice(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
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

                                List<Event> events = getOnPhotoEventList(photo.mServiceId);
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
    protected boolean onPutOnPhoto(Intent request, Intent response, String serviceId, String sessionKey) {
        if (!mClient.hasDevice(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
        } else if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response, "Not found sessionKey:" + sessionKey);
        } else {
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setUnknownError(response);
            }
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnPhoto(Intent request, Intent response, String serviceId, String sessionKey) {
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
                MessageUtils.setUnknownError(response, "Failed to delete event from db.");
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
        if (!mClient.hasDevice(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }
        mClient.execute(new ThetaApiTask() {
            @Override
            public void run(final ThetaApi api) {
                try {
                    api.startVideoRecording();
                    setResult(response, DConnectMessage.RESULT_OK);
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
        if (!mClient.hasDevice(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }
        mClient.execute(new ThetaApiTask() {
            @Override
            public void run(final ThetaApi api) {
                try {
                    api.stopVideoRecording();
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

    private List<Event> getOnPhotoEventList(final String serviceId) {
        return EventManager.INSTANCE.getEventList(serviceId, PROFILE_NAME, null, ATTRIBUTE_ON_PHOTO);
    }

    private ThetaDeviceService getService() {
        return ((ThetaDeviceService) getContext());
    }
}
