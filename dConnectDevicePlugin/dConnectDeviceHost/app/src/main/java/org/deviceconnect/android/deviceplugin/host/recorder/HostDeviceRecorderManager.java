/*
 HostDeviceRecorderManager.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder;


import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;

import org.deviceconnect.android.deviceplugin.host.HostDeviceService;
import org.deviceconnect.android.deviceplugin.host.camera.CameraWrapper;
import org.deviceconnect.android.deviceplugin.host.camera.CameraWrapperManager;
import org.deviceconnect.android.deviceplugin.host.recorder.audio.HostDeviceAudioRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.camera.Camera2Recorder;
import org.deviceconnect.android.deviceplugin.host.recorder.screen.HostDeviceScreenCastRecorder;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.profile.MediaStreamRecordingProfile;
import org.deviceconnect.android.provider.FileManager;
import org.deviceconnect.profile.MediaStreamRecordingProfileConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Host Device Recorder Manager.
 *
 * @author NTT DOCOMO, INC.
 */
public class HostDeviceRecorderManager {

    /** List of HostDeviceRecorder. */
    private HostDeviceRecorder[] mRecorders;

    /** HostDevicePhotoRecorder. */
    private Camera2Recorder mDefaultPhotoRecorder;

    /** コンテキスト. */
    private HostDeviceService mHostDeviceService;

    public HostDeviceRecorderManager(final @NonNull HostDeviceService service) {
        mHostDeviceService = service;
    }

    public void createRecorders(final CameraWrapperManager cameraMgr, final FileManager fileMgr) {
        List<Camera2Recorder> photoRecorders = new ArrayList<>();
        for (CameraWrapper camera : cameraMgr.getCameraList()) {
            photoRecorders.add(new Camera2Recorder(mHostDeviceService, camera, fileMgr));
        }

        if (!photoRecorders.isEmpty()) {
            mDefaultPhotoRecorder = photoRecorders.get(0);
        }

        List<HostDeviceRecorder> recorders = new ArrayList<>();
        recorders.addAll(photoRecorders);
        recorders.add(new HostDeviceAudioRecorder(mHostDeviceService));
        if (isSupportedMediaProjection()) {
            recorders.add(new HostDeviceScreenCastRecorder(mHostDeviceService, fileMgr));
        }
        mRecorders = recorders.toArray(new HostDeviceRecorder[recorders.size()]);
    }

    public void initialize() {
        for (HostDeviceRecorder recorder : getRecorders()) {
            recorder.initialize();
        }
    }

    public void clean() {
        for (HostDeviceRecorder recorder : getRecorders()) {
            recorder.clean();
        }
    }

    public HostDeviceRecorder[] getRecorders() {
        return mRecorders;
    }

    public HostDeviceRecorder getRecorder(final String id) {
        if (mRecorders.length == 0) {
            return null;
        }
        if (id == null) {
            return mRecorders[0];
        }
        for (HostDeviceRecorder recorder : mRecorders) {
            if (id.equals(recorder.getId())) {
                return recorder;
            }
        }
        return null;
    }

    public HostDevicePhotoRecorder getCameraRecorder(final String id) {
        if (id == null) {
            return mDefaultPhotoRecorder;
        }
        for (HostDeviceRecorder recorder : mRecorders) {
            if (id.equals(recorder.getId()) && recorder instanceof HostDevicePhotoRecorder) {
                return (HostDevicePhotoRecorder) recorder;
            }
        }
        return null;
    }

    public HostDeviceStreamRecorder getStreamRecorder(final String id) {
        if (id == null) {
            return mDefaultPhotoRecorder;
        }
        for (HostDeviceRecorder recorder : mRecorders) {
            if (id.equals(recorder.getId()) && recorder instanceof HostDeviceStreamRecorder) {
                return (HostDeviceStreamRecorder) recorder;
            }
        }
        return null;
    }

    public PreviewServerProvider getPreviewServerProvider(final String id) {
        if (id == null) {
            return mDefaultPhotoRecorder;
        }
        for (HostDeviceRecorder recorder : mRecorders) {
            if (id.equals(recorder.getId()) && recorder instanceof PreviewServerProvider) {
                return (AbstractPreviewServerProvider) recorder;
            }
        }
        return null;
    }

    public void start() {
    }

    public void stop() {
    }

    public void stopWebServer(final String id) {
        if (id == null) {
            return;
        }
        HostDeviceRecorder recorder = getRecorder(id);
        if (recorder instanceof PreviewServerProvider) {
            ((PreviewServerProvider) recorder).stopWebServers();
        }
    }

    @SuppressWarnings("deprecation")
    public void sendEventForRecordingChange(final String serviceId, final HostDeviceRecorder.RecorderState state,
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
            case INACTTIVE:
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
            mHostDeviceService.sendEvent(intent, evt.getAccessToken());
        }
    }

    public static boolean isSupportedMediaProjection() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    private Context getContext() {
        return mHostDeviceService;
    }

}
