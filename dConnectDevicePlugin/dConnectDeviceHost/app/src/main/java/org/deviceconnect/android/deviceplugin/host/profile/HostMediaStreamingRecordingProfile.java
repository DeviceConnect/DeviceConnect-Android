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
import org.deviceconnect.android.deviceplugin.host.camera.HostDevicePhotoRecorder;
import org.deviceconnect.android.deviceplugin.host.HostDevicePreviewServer;
import org.deviceconnect.android.deviceplugin.host.HostDeviceRecorder;
import org.deviceconnect.android.deviceplugin.host.HostDeviceRecorderManager;
import org.deviceconnect.android.deviceplugin.host.HostDeviceService;
import org.deviceconnect.android.deviceplugin.host.HostDeviceStreamRecorder;
import org.deviceconnect.android.deviceplugin.host.camera.CameraOverlay.OnTakePhotoListener;
import org.deviceconnect.android.deviceplugin.host.camera.HostDeviceCameraRecorder;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.MediaStreamRecordingProfile;
import org.deviceconnect.android.provider.FileManager;
import org.deviceconnect.message.DConnectMessage;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.deviceconnect.android.deviceplugin.host.profile.RequestParam.Range;
import static org.deviceconnect.android.deviceplugin.host.profile.RequestParam.Type;

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

    private final RequestParam[] mOptionApiPutParams = {
        new RequestParam(PARAM_IMAGE_WIDTH, Type.INT, new Range() {
            @Override
            boolean checkInt(final int imageWidth) {
                return imageWidth > 0;
            }
        }),
        new RequestParam(PARAM_IMAGE_HEIGHT, Type.INT, new Range() {
            @Override
            boolean checkInt(final int imageHeight) {
                return imageHeight > 0;
            }
        }),
        new RequestParam(PARAM_PREVIEW_WIDTH, Type.INT, new Range() {
            @Override
            boolean checkInt(final int previewWidth) {
                return previewWidth > 0;
            }
        }),
        new RequestParam(PARAM_IMAGE_HEIGHT, Type.INT, new Range() {
            @Override
            boolean checkInt(final int previewHeight) {
                return previewHeight > 0;
            }
        }),
        new RequestParam(PARAM_PREVIEW_MAX_FRAME_RATE, Type.DOUBLE, new Range() {
            @Override
            boolean checkDouble(final double maxFrameRate) {
                return maxFrameRate > 0.0;
            }
        })
    };

    public HostMediaStreamingRecordingProfile(final HostDeviceRecorderManager mgr) {
        mRecorderMgr = mgr;
        mHandler = new Handler();
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

        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (!initRecorders(response)) {
                    sendResponse(response);
                    return;
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
                    if (recorder instanceof HostDeviceCameraRecorder) {
                        HostDeviceRecorder.PictureSize size = ((HostDeviceCameraRecorder) recorder).getPictureSize();
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
        if (!checkOptionsParam(request, response)) {
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

        Integer previewWidth = getPreviewWidth(request);
        Integer previewHeight = getPreviewHeight(request);
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
        Double previewMaxFrameRate = getPreviewMaxFrameRate(request);
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

    private boolean checkOptionsParam(final Intent request, final Intent response) {
        for (RequestParam param : mOptionApiPutParams) {
            if (!param.check(request, response)) {
                return false;
            }
        }
        Integer imageWidth = getImageWidth(request);
        Integer imageHeight = getImageHeight(request);
        if ((imageWidth != null && imageHeight == null)
            || (imageWidth == null && imageHeight != null)) {
            return false;
        }
        Integer previewWidth = getPreviewWidth(request);
        Integer previewHeight = getPreviewHeight(request);
        if ((previewWidth != null && previewHeight == null)
            || (previewWidth == null && previewHeight != null)) {
            return false;
        }
        return true;
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

    private static final String PARAM_PREVIEW_WIDTH = "previewWidth";

    private static final String PARAM_PREVIEW_HEIGHT = "previewHeight";

    private static final String PARAM_PREVIEW_MAX_FRAME_RATE = "previewMaxFrameRate";

    private static final String PARAM_AUDIO = "audio";

    private static final String PARAM_CHANNELS = "channels";

    private static final String PARAM_SAMPLE_RATE = "sampleRate";

    private static final String PARAM_SAMPLE_SIZE = "sampleSize";

    private static final String PARAM_BLOCK_SIZE = "blockSize";

    private static final String PARAM_IMAGE_SIZES = "imageSizes";

    private static final String PARAM_PREVIEW_SIZES = "previewSizes";

    private static final String PARAM_WIDTH = "width";

    private static final String PARAM_HEIGHT = "height";

    public static Integer getPreviewWidth(final Intent request) {
        return parseInteger(request, PARAM_PREVIEW_WIDTH);
    }

    public static Integer getPreviewHeight(final Intent request) {
        return parseInteger(request, PARAM_PREVIEW_HEIGHT);
    }

    public static Double getPreviewMaxFrameRate(final Intent request) {
        return parseDouble(request, PARAM_PREVIEW_MAX_FRAME_RATE);
    }

    public static void setImageSizes(final Intent response, final Bundle[] imageSizes) {
        response.putExtra(PARAM_IMAGE_SIZES, imageSizes);
    }

    public static void setPreviewSizes(final Intent response, final Bundle[] previewSizes) {
        response.putExtra(PARAM_PREVIEW_SIZES, previewSizes);
    }

    public static void setWidth(final Bundle size, final int width) {
        size.putInt(PARAM_WIDTH, width);
    }

    public static void setHeight(final Bundle size, final int height) {
        size.putInt(PARAM_HEIGHT, height);
    }

    public static void setRecorderPreviewWidth(final Bundle recorder, final int imageWidth) {
        recorder.putInt(PARAM_PREVIEW_WIDTH, imageWidth);
    }

    public static void setRecorderPreviewHeight(final Bundle recorder, final int imageHeight) {
        recorder.putInt(PARAM_PREVIEW_HEIGHT, imageHeight);
    }

    public static void setRecorderPreviewMaxFrameRate(final Bundle recorder, final double maxFrameRate) {
        recorder.putDouble(PARAM_PREVIEW_MAX_FRAME_RATE, maxFrameRate);
    }

    public static void setRecorderAudio(final Bundle recorder, final Bundle audio) {
        recorder.putBundle(PARAM_AUDIO, audio);
    }

    public static void setAudioChannels(final Bundle audio, final int channels) {
        audio.putInt(PARAM_CHANNELS, channels);
    }

    public static void setAudioSampleRate(final Bundle audio, final int sampleRate) {
        audio.putInt(PARAM_SAMPLE_RATE, sampleRate);
    }

    public static void setAudioSampleSize(final Bundle audio, final int sampleSize) {
        audio.putInt(PARAM_SAMPLE_SIZE, sampleSize);
    }

    public static void setAudioBlockSize(final Bundle audio, final int blockSize) {
        audio.putInt(PARAM_BLOCK_SIZE, blockSize);
    }

    public static void setAudio(final Intent response, final Bundle audio) {
        response.putExtra(PARAM_AUDIO, audio);
    }

    public static void setAudioUri(final Bundle audio, final String uri) {
        audio.putString(PARAM_URI, uri);
    }
}
