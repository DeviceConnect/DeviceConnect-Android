/*
 HostMediaStreamingRecordingProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.host.profile;

import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;

import org.deviceconnect.android.deviceplugin.host.HostDevicePhotoRecorder;
import org.deviceconnect.android.deviceplugin.host.HostDevicePreviewServer;
import org.deviceconnect.android.deviceplugin.host.HostDeviceRecorder;
import org.deviceconnect.android.deviceplugin.host.HostDeviceRecorderManager;
import org.deviceconnect.android.deviceplugin.host.HostDeviceService;
import org.deviceconnect.android.deviceplugin.host.HostDeviceStreamRecorder;
import org.deviceconnect.android.deviceplugin.host.camera.CameraOverlay.OnTakePhotoListener;
import org.deviceconnect.android.deviceplugin.host.video.HostDeviceVideoRecorder;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.MediaStreamRecordingProfile;
import org.deviceconnect.android.provider.FileManager;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * MediaStream Recording Profile.
 *
 * @author NTT DOCOMO, INC.
 */
@SuppressWarnings("deprecation")
public class HostMediaStreamingRecordingProfile extends MediaStreamRecordingProfile {

    private final HostDeviceRecorderManager mRecorderMgr;

    public HostMediaStreamingRecordingProfile(final HostDeviceRecorderManager mgr) {
        mRecorderMgr = mgr;

        // Set default picture sizes.
        for (HostDeviceRecorder recorder : mRecorderMgr.getRecorders()) {
            if (recorder.usesCamera()) {
                recorder.setCameraPictureSize(getDefaultSize(recorder.getCameraId()));
            }
        }
    }

    @Override
    protected boolean onGetMediaRecorder(final Intent request, final Intent response,
                                         final String serviceId) {
        if (serviceId == null) {
            createEmptyServiceId(response);
            return true;
        }
        if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
            return true;
        }

        List<Bundle> recorders = new LinkedList<Bundle>();
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
            if (recorder instanceof HostDevicePhotoRecorder) {
                HostDeviceRecorder.PictureSize size = ((HostDevicePhotoRecorder) recorder).getInputPictureSize();
                setRecorderImageWidth(info, size.getWidth());
                setRecorderImageHeight(info, size.getHeight());
            } else if (recorder instanceof HostDeviceVideoRecorder) {
                HostDeviceRecorder.PictureSize size = ((HostDeviceVideoRecorder) recorder).getInputPictureSize();
                setRecorderImageWidth(info, size.getWidth());
                setRecorderImageHeight(info, size.getHeight());
            }
            setRecorderConfig(info, "");
            recorders.add(info);
        }
        setRecorders(response, recorders.toArray(new Bundle[recorders.size()]));
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    @Override
    protected boolean onGetOptions(final Intent request, final Intent response,
                                   final String serviceId, final String target) {
        if (serviceId == null) {
            createEmptyServiceId(response);
            return true;
        }
        if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
            return true;
        }

        HostDeviceRecorder recorder = mRecorderMgr.getRecorder(target);
        if (recorder == null) {
            MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
            return true;
        }

        setMIMEType(response, recorder.getSupportedMimeTypes());
        if (recorder.mutableInputPictureSize() && recorder.usesCamera()) {
            List<HostDeviceRecorder.PictureSize> sizes = getSupportedSizes(recorder.getCameraId());
            if (sizes.size() > 0) {
                sortByWidth(sizes);
                int minWidth = sizes.get(0).getWidth();
                int maxWidth = sizes.get(sizes.size() - 1).getWidth();
                sortByHeight(sizes);
                int minHeight = sizes.get(0).getHeight();
                int maxHeight = sizes.get(sizes.size() - 1).getHeight();
                setImageWidth(response, minWidth, maxWidth);
                setImageHeight(response, minHeight, maxHeight);
            }
        }
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    private void sortByWidth(List<HostDeviceRecorder.PictureSize> sizes) {
        Collections.sort(sizes, new Comparator<HostDeviceRecorder.PictureSize>() {
            @Override
            public int compare(final HostDeviceRecorder.PictureSize lhs,
                               final HostDeviceRecorder.PictureSize rhs) {
                return lhs.getWidth() - rhs.getWidth();
            }
        });
    }

    private void sortByHeight(List<HostDeviceRecorder.PictureSize> sizes) {
        Collections.sort(sizes, new Comparator<HostDeviceRecorder.PictureSize>() {
            @Override
            public int compare(final HostDeviceRecorder.PictureSize lhs,
                               final HostDeviceRecorder.PictureSize rhs) {
                return lhs.getHeight() - rhs.getHeight();
            }
        });
    }

    private List<HostDeviceRecorder.PictureSize> getSupportedSizes(final int cameraId) {
        Camera camera = Camera.open(cameraId);
        List<Camera.Size> sizes = camera.getParameters().getSupportedPictureSizes();
        List<HostDeviceRecorder.PictureSize> result = new ArrayList<HostDeviceRecorder.PictureSize>();
        for (Camera.Size size : sizes) {
            result.add(new HostDeviceRecorder.PictureSize(size.width, size.height));
        }
        camera.release();
        return result;
    }

    @Override
    protected boolean onPutOptions(final Intent request, final Intent response,
                                   final String serviceId, final String target,
                                   final Integer imageWidth, final Integer imageHeight,
                                   final String mimeType) {
        if (serviceId == null) {
            createEmptyServiceId(response);
            return true;
        }
        if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
            return true;
        }
        if (mimeType == null) {
            MessageUtils.setInvalidRequestParameterError(response, "mimeType is null.");
            return true;
        }

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
        if (recorder.mutableInputPictureSize()) {
            if (imageWidth == null) {
                MessageUtils.setInvalidRequestParameterError(response, "imageWidth is null.");
                return true;
            }
            if (imageHeight == null) {
                MessageUtils.setInvalidRequestParameterError(response, "imageHeight is null.");
                return true;
            }

            HostDeviceRecorder.PictureSize newSize;
            if (recorder.usesCamera()) {
                newSize = getOptimalSize(recorder.getCameraId(), imageWidth, imageHeight);
            } else {
                newSize = new HostDeviceRecorder.PictureSize(imageWidth, imageHeight);
            }
            recorder.setInputPictureSize(newSize);
            setResult(response, DConnectMessage.RESULT_OK);
        } else {
            MessageUtils.setInvalidRequestParameterError(response,
                "target=" + target + " does not support to set imageWidth and imageHeight.");
        }
        return true;
    }

    private HostDeviceRecorder.PictureSize getDefaultSize(final int cameraId) {
        Camera camera = Camera.open(cameraId);
        Camera.Parameters params = camera.getParameters();
        List<Camera.Size> sizes = params.getSupportedPictureSizes();
        if (sizes.size() > 0) {
            Collections.sort(sizes, new Comparator<Camera.Size>() {
                @Override
                public int compare(final Camera.Size s1, final Camera.Size s2) {
                    int a1 = s1.width * s1.height;
                    int a2 = s2.width * s2.height;
                    return a1 - a2;
                }
            });
            Camera.Size size = sizes.get(0);
            camera.release();
            return new HostDeviceRecorder.PictureSize(size.width, size.height);
        }
        camera.release();
        return null;
    }

    private HostDeviceRecorder.PictureSize getOptimalSize(final int cameraId,
                                                          final int width, final int height) {
        Camera camera = Camera.open(cameraId);
        Camera.Parameters params = camera.getParameters();
        List<Camera.Size> sizes = params.getSupportedPictureSizes();
        if (sizes.size() > 0) {
            Collections.sort(sizes, new Comparator<Camera.Size>() {
                @Override
                public int compare(final Camera.Size s1, final Camera.Size s2) {
                    int a = width * height;
                    int d1 = a - (s1.width * s1.height);
                    int d2 = a - (s2.width * s2.height);
                    return Math.abs(d1) - Math.abs(d2);
                }
            });
            Camera.Size size = sizes.get(0);
            camera.release();
            return new HostDeviceRecorder.PictureSize(size.width, size.height);
        }
        camera.release();
        return null;
    }

    private boolean supportsMimeType(final HostDeviceRecorder recorder, final String mimeType) {
        for (String supportedMimeType : recorder.getSupportedMimeTypes()) {
            if (supportedMimeType.equals(mimeType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean onPutOnPhoto(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response, "sessionKey does not exist.");
        }

        EventError error = EventManager.INSTANCE.addEvent(request);
        if (error == EventError.NONE) {
            setResult(response, DConnectMessage.RESULT_OK);
        } else {
            setResult(response, DConnectMessage.RESULT_ERROR);
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnPhoto(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
        if (sessionKey == null) {
            MessageUtils.setInvalidRequestParameterError(response, "sessionKey does not exist.");
        }

        EventError error = EventManager.INSTANCE.removeEvent(request);
        if (error == EventError.NONE) {
            setResult(response, DConnectMessage.RESULT_OK);
        } else {
            setResult(response, DConnectMessage.RESULT_ERROR);
        }
        return true;
    }

    @Override
    protected boolean onPostTakePhoto(final Intent request, final Intent response, final String serviceId,
            final String target) {
        if (serviceId == null) {
            createEmptyServiceId(response);
            return true;
        }
        if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
            return true;
        }
        HostDevicePhotoRecorder recorder = mRecorderMgr.getPhotoRecorder(target);
        if (recorder == null) {
            MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
            return true;
        }

        recorder.takePhoto(new OnTakePhotoListener() {
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

    @Override
    protected boolean onPutPreview(final Intent request, final Intent response, final String serviceId) {
        if (serviceId == null) {
            createEmptyServiceId(response);
            return true;
        }
        if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
            return true;
        }
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

    @Override
    protected boolean onDeletePreview(final Intent request, final Intent response, final String serviceId) {
        if (serviceId == null) {
            createEmptyServiceId(response);
            return true;
        }
        if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
            return true;
        }
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

    @Override
    protected boolean onPostRecord(final Intent request, final Intent response, final String serviceId,
            final String target, final Long timeslice) {

        if (serviceId == null) {
            createEmptyServiceId(response);
            return true;
        }
        if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
            return true;
        }
        if (timeslice != null && timeslice <= 0) {
            MessageUtils.setInvalidRequestParameterError(response, "timeslice is invalid.");
            return true;
        }

        HostDeviceStreamRecorder recorder = mRecorderMgr.getStreamRecorder(target);
        if (recorder == null) {
            MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
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

    @Override
    protected boolean onPutStop(final Intent request, final Intent response, final String serviceId,
            final String target) {
        if (serviceId == null) {
            createEmptyServiceId(response);
            return true;
        }
        if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
            return true;
        }

        HostDeviceStreamRecorder recorder = mRecorderMgr.getStreamRecorder(target);
        if (recorder == null) {
            MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
            return true;
        }
        recorder.stop();
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    @Override
    protected boolean onPutPause(final Intent request, final Intent response, final String serviceId,
            final String target) {
        if (serviceId == null) {
            createEmptyServiceId(response);
            return true;
        }
        if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
            return true;
        }

        HostDeviceStreamRecorder recorder = mRecorderMgr.getStreamRecorder(target);
        if (recorder == null) {
            MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
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

    @Override
    protected boolean onPutResume(final Intent request, final Intent response, final String serviceId,
            final String target) {
        if (serviceId == null) {
            createEmptyServiceId(response);
            return true;
        }
        if (!checkServiceId(serviceId)) {
            createNotFoundService(response);
            return true;
        }

        HostDeviceStreamRecorder recorder = mRecorderMgr.getStreamRecorder(target);
        if (recorder == null) {
            MessageUtils.setInvalidRequestParameterError(response, "target is invalid.");
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

    /**
     * サービスIDをチェックする.
     *
     * @param serviceId サービスID
     * @return <code>serviceId</code>がHostデバイスのIDに等しい場合はtrue、そうでない場合はfalse
     */
    private boolean checkServiceId(final String serviceId) {
        return HostServiceDiscoveryProfile.SERVICE_ID.equals(serviceId);
    }

    /**
     * デバイスが発見できなかった場合のエラーを作成する.
     *
     * @param response レスポンスを格納するIntent
     */
    private void createNotFoundService(final Intent response) {
        MessageUtils.setNotFoundServiceError(response, "Service is not found.");
    }

    /**
     * サービスIDが空の場合のエラーを作成する.
     *
     * @param response レスポンスを格納するIntent
     */
    private void createEmptyServiceId(final Intent response) {
        MessageUtils.setEmptyServiceIdError(response);
    }

}
