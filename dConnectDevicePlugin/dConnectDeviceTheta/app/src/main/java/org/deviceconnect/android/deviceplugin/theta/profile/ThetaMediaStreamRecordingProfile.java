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
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.MediaStreamRecordingProfile;
import org.deviceconnect.android.provider.FileManager;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.profile.MediaStreamRecordingProfileConstants;

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
        mClient.execute(new ThetaApiTask() {
            @Override
            public void run(final ThetaApi api) {
                try {
                    api.takePhoto(new ThetaPhotoEventListener() {
                        @Override
                        public void onPhoto(final ThetaPhoto photo) {
                            try {
                                String uri = mFileMgr.saveFile(photo.mFilename, photo.mData);
                                String path = mFileMgr.getBasePath().toString() + "/" + photo.mFilename;

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

    private List<Event> getOnPhotoEventList(final String serviceId) {
        return EventManager.INSTANCE.getEventList(serviceId, PROFILE_NAME, null, ATTRIBUTE_ON_PHOTO);
    }

    private ThetaDeviceService getService() {
        return ((ThetaDeviceService) getContext());
    }
}
