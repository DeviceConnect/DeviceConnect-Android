/*
 HostMediaStreamingRecordingProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.host.profile;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDevicePreviewServer;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceRecorderManager;
import org.deviceconnect.android.deviceplugin.host.HostDeviceService;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceStreamRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.camera.CameraOverlay;
import org.deviceconnect.android.deviceplugin.host.recorder.camera.HostDeviceCameraRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.camera.HostDevicePhotoRecorder;
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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * MediaStream Recording Profile.
 *
 * @author NTT DOCOMO, INC.
 */
@SuppressWarnings("deprecation")
public class HostMediaStreamingRecordingProfile extends MediaStreamRecordingProfile {

    private final HostDeviceRecorderManager mRecorderMgr;

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private final Handler mHandler;

    private final DConnectApi mGetMediaRecorderApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_MEDIARECORDER;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    if (!initRecorders(response)) {
                        sendResponse(response);
                        return;
                    }

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
                        if (recorder instanceof HostDeviceCameraRecorder) {
                            HostDeviceRecorder.PictureSize size = recorder.getPictureSize();
                            setRecorderImageWidth(info, size.getWidth());
                            setRecorderImageHeight(info, size.getHeight());
                        }
                        if (recorder instanceof HostDevicePreviewServer) {
                            HostDevicePreviewServer server = (HostDevicePreviewServer) recorder;
                            HostDeviceRecorder.PictureSize size = server.getPreviewSize();
                            setRecorderPreviewWidth(info, size.getWidth());
                            setRecorderPreviewHeight(info, size.getHeight());
                            setRecorderPreviewMaxFrameRate(info, server.getPreviewMaxFrameRate());
                        }
                        setRecorderConfig(info, "");
                        recorders.add(info);
                    }
                    setRecorders(response, recorders.toArray(new Bundle[recorders.size()]));
                    setResult(response, DConnectMessage.RESULT_OK);
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
            HostDeviceRecorder recorder = mRecorderMgr.getRecorder(target);
            if (recorder == null) {
                MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
                return true;
            }

            setMIMEType(response, recorder.getSupportedMimeTypes());
            if (recorder instanceof HostDeviceCameraRecorder) {
                HostDeviceCameraRecorder camera = (HostDeviceCameraRecorder) recorder;
                setSupportedImageSizes(response, camera.getSupportedPictureSizes());
            }
            if (recorder instanceof HostDevicePreviewServer) {
                HostDevicePreviewServer server = (HostDevicePreviewServer) recorder;
                setSupportedPreviewSizes(response, server.getSupportedPreviewSizes());
            }
            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        }
    };

    private final DConnectApi mPutOptionsApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_OPTIONS;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String target = getTarget(request);
            String mimeType = getMIMEType(request);
            Integer imageWidth = getImageWidth(request);
            Integer imageHeight = getImageHeight(request);
            Integer previewWidth = getPreviewWidth(request);
            Integer previewHeight = getPreviewHeight(request);
            Double previewMaxFrameRate = getPreviewMaxFrameRate(request);

            HostDeviceRecorder recorder = mRecorderMgr.getRecorder(target);
            if (recorder == null) {
                MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
                return true;
            }

            if (!supportsMimeType(recorder, mimeType)) {
                MessageUtils.setInvalidRequestParameterError(response,
                    "MIME-Type " + mimeType + " is unsupported.");
                return true;
            }
            if (recorder.getState() != HostDeviceRecorder.RecorderState.INACTTIVE) {
                MessageUtils.setInvalidRequestParameterError(response, "settings of active target cannot be changed.");
                return true;
            }

            if (imageWidth != null && imageHeight != null) {
                if (!recorder.supportsPictureSize(imageWidth, imageHeight)) {
                    MessageUtils.setInvalidRequestParameterError(response, "Unsupported image size: imageWidth = "
                        + imageWidth + ", imageHeight = " + imageHeight);
                    return true;
                }
                HostDeviceRecorder.PictureSize newSize = new HostDeviceRecorder.PictureSize(imageWidth, imageHeight);
                recorder.setPictureSize(newSize);
            }
            if (previewWidth != null && previewHeight != null) {
                if (!(recorder instanceof HostDevicePreviewServer)) {
                    MessageUtils.setInvalidRequestParameterError(response, "preview is unsupported.");
                    return true;
                }
                HostDevicePreviewServer server = (HostDevicePreviewServer) recorder;
                if (!server.supportsPreviewSize(previewWidth, previewHeight)) {
                    MessageUtils.setInvalidRequestParameterError(response, "Unsupported preview size: previewWidth = "
                        + previewWidth + ", previewHeight = " + previewHeight);
                    return true;
                }
                HostDeviceRecorder.PictureSize newSize = new HostDeviceRecorder.PictureSize(previewWidth, previewHeight);
                server.setPreviewSize(newSize);
            }
            if (previewMaxFrameRate != null) {
                if (!(recorder instanceof HostDevicePreviewServer)) {
                    MessageUtils.setInvalidRequestParameterError(response, "preview is unsupported.");
                    return true;
                }
                HostDevicePreviewServer server = (HostDevicePreviewServer) recorder;
                server.setPreviewFrameRate(previewMaxFrameRate);
            }

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

    private final DConnectApi mPostTakePhotoApi = new PostApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_TAKE_PHOTO;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            final String target = getTarget(request);
            final String serviceId = getServiceID(request);

            HostDevicePhotoRecorder recorder = mRecorderMgr.getPhotoRecorder(target);
            if (recorder == null) {
                MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
                return true;
            }

            recorder.takePhoto(new CameraOverlay.OnTakePhotoListener() {
                @Override
                public void onTakenPhoto(final String uri, final String filePath) {
                    setResult(response, DConnectMessage.RESULT_OK);
                    setUri(response, uri);
                    sendResponse(response);

                    List<Event> evts = EventManager.INSTANCE.getEventList(serviceId,
                        MediaStreamRecordingProfile.PROFILE_NAME, null,
                        MediaStreamRecordingProfile.ATTRIBUTE_ON_PHOTO);

                    Bundle photo = new Bundle();
                    photo.putString(MediaStreamRecordingProfile.PARAM_URI, uri);
                    photo.putString(MediaStreamRecordingProfile.PARAM_PATH, filePath);
                    photo.putString(MediaStreamRecordingProfile.PARAM_MIME_TYPE, "image/png");

                    for (Event evt : evts) {
                        Intent intent = EventManager.createEventMessage(evt);
                        intent.putExtra(MediaStreamRecordingProfile.PARAM_PHOTO, photo);
                        sendEvent(intent, evt.getAccessToken());
                    }
                }

                @Override
                public void onFailedTakePhoto() {
                    MessageUtils.setUnknownError(response, "Failed to take a photo");
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
            HostDevicePreviewServer server = mRecorderMgr.getPreviewServer(target);
            if (server == null) {
                MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
                return true;
            }

            server.startWebServer(new HostDevicePhotoRecorder.OnWebServerStartCallback() {
                @Override
                public void onStart(@NonNull String uri) {
                    setResult(response, DConnectMessage.RESULT_OK);
                    setUri(response, uri);
                    sendResponse(response);
                }

                @Override
                public void onFail() {
                    MessageUtils.setIllegalServerStateError(response, "Failed to start web server.");
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
            HostDevicePreviewServer server = mRecorderMgr.getPreviewServer(target);
            if (server == null) {
                MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
                return true;
            }

            server.stopWebServer();
            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        }
    };

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
            HostDeviceStreamRecorder recorder = mRecorderMgr.getStreamRecorder(target);
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
            recorder.start(new HostDeviceStreamRecorder.RecordingListener() {
                @Override
                public void onRecorded(final HostDeviceRecorder recorder, final String fileName) {
                    FileManager mgr = ((HostDeviceService) getContext()).getFileManager();
                    setResult(response, DConnectMessage.RESULT_OK);
                    setPath(response, "/" + fileName);
                    setUri(response, mgr.getContentUri() + "/" + fileName);
                    sendResponse(response);
                }

                @Override
                public void onFailed(final HostDeviceRecorder recorder, final String errorMesage) {
                    MessageUtils.setIllegalServerStateError(response, errorMesage);
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
            HostDeviceStreamRecorder recorder = mRecorderMgr.getStreamRecorder(target);
            if (recorder == null) {
                MessageUtils.setNotSupportAttributeError(response,
                    "target does not support stream recording.");
                return true;
            }
            if (recorder.getState() == HostDeviceRecorder.RecorderState.INACTTIVE) {
                MessageUtils.setIllegalDeviceStateError(response, "recorder is stopped already.");
                return true;
            }

            recorder.stop();
            setResult(response, DConnectMessage.RESULT_OK);
            return true;
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
            HostDeviceStreamRecorder recorder = mRecorderMgr.getStreamRecorder(target);
            if (recorder == null) {
                MessageUtils.setNotSupportAttributeError(response,
                    "target does not support stream recording.");
                return true;
            }

            if (!recorder.canPause()) {
                MessageUtils.setNotSupportAttributeError(response);
                return true;
            }
            if (recorder.getState() != HostDeviceRecorder.RecorderState.RECORDING) {
                MessageUtils.setIllegalDeviceStateError(response);
                return true;
            }
            recorder.pause();
            setResult(response, DConnectMessage.RESULT_OK);
            return true;
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
            HostDeviceStreamRecorder recorder = mRecorderMgr.getStreamRecorder(target);
            if (recorder == null) {
                MessageUtils.setNotSupportAttributeError(response,
                    "target does not support stream recording.");
                return true;
            }

            if (!recorder.canPause()) {
                MessageUtils.setNotSupportAttributeError(response);
                return true;
            }
            recorder.resume();
            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        }
    };

    public HostMediaStreamingRecordingProfile(final HostDeviceRecorderManager mgr) {
        mRecorderMgr = mgr;
        mHandler = new Handler();

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
    }

    private boolean initRecorders(final Intent response) {
        try {
            if (!requestPermission()) {
                MessageUtils.setUnknownError(response, "Permission for camera is not granted.");
                return false;
            }
        } catch (InterruptedException e) {
            MessageUtils.setUnknownError(response, "Permission for camera is not granted.");
            return false;
        }

        for (HostDeviceRecorder recorder : mRecorderMgr.getRecorders()) {
            recorder.initialize();
        }
        return true;
    }

    private boolean requestPermission() throws InterruptedException {
        final Boolean[] isPermitted = new Boolean[1];
        final CountDownLatch lockObj = new CountDownLatch(1);
        PermissionUtility.requestPermissions(getContext(), mHandler, new String[]{Manifest.permission.CAMERA},
            new PermissionUtility.PermissionRequestCallback() {
                @Override
                public void onSuccess() {
                    isPermitted[0] = true;
                    lockObj.countDown();
                }

                @Override
                public void onFail(@NonNull String deniedPermission) {
                    isPermitted[0] = false;
                    lockObj.countDown();
                }
            });
        while (isPermitted[0] == null) {
            lockObj.await(100, TimeUnit.MILLISECONDS);
        }
        return isPermitted[0];
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
}
