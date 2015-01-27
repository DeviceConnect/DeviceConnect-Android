/*
 HostMediaStreamingRecordingProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.host.profile;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.HostDeviceService;
import org.deviceconnect.android.deviceplugin.host.audio.AudioConst;
import org.deviceconnect.android.deviceplugin.host.audio.AudioRecorder;
import org.deviceconnect.android.deviceplugin.host.camera.CameraActivity;
import org.deviceconnect.android.deviceplugin.host.camera.CameraConst;
import org.deviceconnect.android.deviceplugin.host.video.VideoConst;
import org.deviceconnect.android.deviceplugin.host.video.VideoRecorder;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.MediaStreamRecordingProfile;
import org.deviceconnect.android.provider.FileManager;
import org.deviceconnect.message.DConnectMessage;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;

/**
 * MediaStream Recording Profile.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HostMediaStreamingRecordingProfile extends MediaStreamRecordingProfile {
    /**
     * 写真用のカメラターゲットID.
     */
    private static final String PHOTO_TARGET_ID = "photo";

    /**
     * 写真用のカメラターゲット名.
     */
    private static final String PHOTO_TARGET_NAME = "AndroidHost Recorder";

    /**
     * VideoのカメラターゲットID.
     */
    private static final String VIDEO_TARGET_ID = "video";

    /**
     * Videoのカメラターゲット名.
     */
    private static final String VIDEO_TARGET_NAME = "AndroidHost Video Recorder";

    /**
     * AudioのカメラターゲットID.
     */
    private static final String AUDIO_TARGET_ID = "audio";

    /**
     * Audioのカメラターゲット名.
     */
    private static final String AUDIO_TARGET_NAME = "AndroidHost Audio Recorder";

    /** 日付のフォーマット. */
    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(
            "yyyyMMdd_kkmmss", Locale.JAPAN);

    /**
     * リクエストマップ.
     * - Key: カメラリクエストID - Val: カメラ応答Broadcast 未受信ならnull /
     * 受信済なら画像URI(画像ID)
     */
    private static Map<String, String> mRequestMap = new ConcurrentHashMap<String, String>();

    /**
     * リクエストマップを取得する.
     * 
     * @return リクエストマップ
     */
    public static Map<String, String> getRequestMap() {
        return mRequestMap;
    }

    @Override
    protected boolean onGetMediaRecorder(final Intent request, final Intent response, final String serviceId) {
        if (serviceId == null) {
            createEmptyServiceId(response);
        } else if (!checkserviceId(serviceId)) {
            createNotFoundDevice(response);
        } else {
            String className = getClassnameOfTopActivity();
            List<Bundle> recorders = new LinkedList<Bundle>();

            Bundle cameraRecorder = new Bundle();
            setRecorderId(cameraRecorder, PHOTO_TARGET_ID);
            setRecorderName(cameraRecorder, PHOTO_TARGET_NAME);
            setRecorderImageWidth(cameraRecorder, VideoConst.VIDEO_WIDTH);
            setRecorderImageHeight(cameraRecorder, VideoConst.VIDEO_HEIGHT);
            setRecorderMIMEType(cameraRecorder, "image/png");
            if (CameraActivity.class.getName().equals(className)) {
                setRecorderState(cameraRecorder, RecorderState.RECORDING);
            } else {
                setRecorderState(cameraRecorder, RecorderState.INACTIVE);
            }
            setRecorderConfig(cameraRecorder, "");
            recorders.add(cameraRecorder);

            Bundle videoRecorder = new Bundle();
            setRecorderId(videoRecorder, VIDEO_TARGET_ID);
            setRecorderName(videoRecorder, VIDEO_TARGET_NAME);
            setRecorderImageWidth(videoRecorder, VideoConst.VIDEO_WIDTH);
            setRecorderImageHeight(videoRecorder, VideoConst.VIDEO_HEIGHT);
            setRecorderMIMEType(videoRecorder, "video/3gp");
            if (VideoRecorder.class.getName().equals(className)) {
                setRecorderState(cameraRecorder, RecorderState.RECORDING);
            } else {
                setRecorderState(cameraRecorder, RecorderState.INACTIVE);
            }
            setRecorderConfig(videoRecorder, "");
            recorders.add(videoRecorder);

            Bundle audioRecorder = new Bundle();
            setRecorderId(audioRecorder, AUDIO_TARGET_ID);
            setRecorderName(audioRecorder, AUDIO_TARGET_NAME);
            setRecorderMIMEType(videoRecorder, "audio/3gp");
            if (AudioRecorder.class.getName().equals(className)) {
                setRecorderState(cameraRecorder, RecorderState.RECORDING);
            } else {
                setRecorderState(cameraRecorder, RecorderState.INACTIVE);
            }
            setRecorderConfig(audioRecorder, "");
            recorders.add(audioRecorder);

            setRecorders(response, recorders.toArray(new Bundle[recorders.size()]));

            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
    }

    @Override
    protected boolean onPutOnPhoto(final Intent request, final Intent response, final String serviceId,
            final String sessionKey) {
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
        // カメラアプリにシャッター通知
        final String requestid = "" + UUID.randomUUID().hashCode();

        if (serviceId == null) {
            createEmptyServiceId(response);
            return true;
        } else if (!checkServiceId(serviceId)) {
            createNotFoundDevice(response);
            return true;
        } else {

            if (target != null && !PHOTO_TARGET_ID.equals(target)) {
                MessageUtils.setInvalidRequestParameterError(response,
                        "target is invalid.");
                return true;
            }

            String className = getClassnameOfTopActivity();
            if (CameraActivity.class.getName().equals(className)) {
                // カメラアプリがすでに前にある
                Intent intent = new Intent();
                intent.setClass(getContext(), CameraActivity.class);
                intent.setAction(CameraConst.SEND_HOSTDP_TO_CAMERA);
                intent.putExtra(CameraConst.EXTRA_NAME, CameraConst.EXTRA_NAME_SHUTTER);
                intent.putExtra(CameraConst.EXTRA_REQUESTID, requestid);
                getContext().sendBroadcast(intent);
            } else {
                // カメラアプリを起動
                Intent intent = new Intent();
                intent.setClass(getContext(), CameraActivity.class);
                intent.setAction(CameraConst.SEND_HOSTDP_TO_CAMERA);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(CameraConst.EXTRA_NAME, CameraConst.EXTRA_NAME_SHUTTER);
                intent.putExtra(CameraConst.EXTRA_REQUESTID, requestid);
                getContext().startActivity(intent);
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    final long POLLING_WAIT_TIMEOUT = 10000;
                    final long POLLING_WAIT_TIME = 500;
                    long now = System.currentTimeMillis();
                    try {
                        do {
                            Thread.sleep(POLLING_WAIT_TIME);
                        } while (mRequestMap.get(requestid) == null
                                && System.currentTimeMillis() - now < POLLING_WAIT_TIMEOUT);
                    } catch (InterruptedException e) {
                        if (BuildConfig.DEBUG) {
                            e.printStackTrace();
                        }
                    }

                    String pictureUri = mRequestMap.remove(requestid);
                    if (pictureUri == null) {
                        setResult(response, DConnectMessage.RESULT_ERROR);
                        getContext().sendBroadcast(response);
                        return;
                    }

                    // レスポンスを返す
                    setResult(response, DConnectMessage.RESULT_OK);
                    setUri(response, pictureUri);
                    getContext().sendBroadcast(response);

                    List<Event> events = EventManager.INSTANCE.getEventList(serviceId,
                            PROFILE_NAME, null, ATTRIBUTE_ON_PHOTO);
                    for (int i = 0; i < events.size(); i++) {
                        Intent intent = EventManager.createEventMessage(events.get(i));
                        Bundle photo = new Bundle();
                        setPath(photo, pictureUri);
                        setMIMEType(photo, "image/png");
                        setPhoto(intent, photo);
                        getContext().sendBroadcast(intent);
                    }
                }
            }).start();
        }

        mLogger.exiting(getClass().getName(), "onPostReceive", false);
        return false;

    }

    @Override
    protected boolean onPutPreview(Intent request, Intent response, String serviceId) {
        if (serviceId == null) {
            createEmptyServiceId(response);
            return true;
        } else if (!checkServiceId(serviceId)) {
            createNotFoundDevice(response);
            return true;
        } else {
            String uri = ((HostDeviceService) getContext()).startWebServer();
            if (uri != null) {
                setResult(response, DConnectMessage.RESULT_OK);
                setUri(response, uri);
            } else {
                MessageUtils.setIllegalServerStateError(response, 
                        "Failed to start web server.");
                return true;
            }

            String className = getClassnameOfTopActivity();
            if (CameraActivity.class.getName().equals(className)) {
                Intent intent = new Intent();
                intent.setAction(CameraConst.SEND_HOSTDP_TO_CAMERA);
                intent.putExtra(CameraConst.EXTRA_NAME, CameraConst.EXTRA_NAME_PREVIEW);
                getContext().sendBroadcast(intent);
            } else {
                Intent intent = new Intent();
                intent.setClass(getContext(), CameraActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(CameraConst.SEND_HOSTDP_TO_CAMERA);
                intent.putExtra(CameraConst.EXTRA_NAME, CameraConst.EXTRA_NAME_PREVIEW);
                getContext().startActivity(intent);
            }
            return true;
        }
    }

    @Override
    protected boolean onDeletePreview(Intent request, Intent response, String serviceId) {
        if (serviceId == null) {
            createEmptyServiceId(response);
            return true;
        } else if (!checkServiceId(serviceId)) {
            createNotFoundDevice(response);
            return true;
        } else {
            ((HostDeviceService) getContext()).stopWebServer();
            String className = getClassnameOfTopActivity();
            if (CameraActivity.class.getName().equals(className)) {
                Intent intent = new Intent();
                intent.setAction(CameraConst.SEND_HOSTDP_TO_CAMERA);
                intent.putExtra(CameraConst.EXTRA_NAME, CameraConst.EXTRA_NAME_FINISH);
                getContext().sendBroadcast(intent);
            }
            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        }
    }

    @Override
    protected boolean onPostRecord(final Intent request, final Intent response, final String serviceId,
            final String target, final Long timeslice) {

        if (serviceId == null) {
            createEmptyServiceId(response);
            return true;
        } else if (!checkServiceId(serviceId)) {
            createNotFoundDevice(response);
            return true;
        } else {

            if (timeslice != null && timeslice <= 0) {
                MessageUtils.setIllegalServerStateError(response, 
                        "timeslice is invalid.");
                return true;
            }

            FileManager mgr = ((HostDeviceService) getContext()).getFileManager();
            String className = getClassnameOfTopActivity();

            if (target == null || target.equals(VIDEO_TARGET_ID)) {
                if (VideoRecorder.class.getName().equals(className)) {
                    MessageUtils.setIllegalDeviceStateError(response,
                            "Running video recoder, yet");
                    return true;
                }
                String filename = generateVideoFileName();
                Intent intent = new Intent();
                intent.setClass(getContext(), VideoRecorder.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(VideoConst.EXTRA_FILE_NAME, filename);
                getContext().startActivity(intent);
                setResult(response, DConnectMessage.RESULT_OK);
                setPath(response, "/" + filename);
                setUri(response, mgr.getContentUri() + "/" + filename);
            } else if (target.equals(AUDIO_TARGET_ID)) {
                if (AudioRecorder.class.getName().equals(className)) {
                    MessageUtils.setIllegalDeviceStateError(response,
                            "Running video recoder, yet");
                    return true;
                }
                String filename = generateAudioFileName();
                Intent intent = new Intent();
                intent.setClass(getContext(), AudioRecorder.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(AudioConst.EXTRA_FINE_NAME, filename);
                getContext().startActivity(intent);
                setResult(response, DConnectMessage.RESULT_OK);
                setPath(response, "/" + filename);
                setUri(response, mgr.getContentUri() + "/" + filename);
            } else {
                MessageUtils.setInvalidRequestParameterError(response, 
                        "target is invalid.");
            }

            return true;
        }
    }

    @Override
    protected boolean onPutStop(final Intent request, final Intent response, final String serviceId,
                                final String target) {
        if (serviceId == null) {
            createEmptyServiceId(response);
            return true;
        } else if (!checkServiceId(serviceId)) {
            createNotFoundDevice(response);
            return true;
        } else {

            // 今起動しているActivityを判定する
            String className = getClassnameOfTopActivity();
            if (VideoRecorder.class.getName().equals(className)) {
                Intent intent = new Intent(VideoConst.SEND_HOSTDP_TO_VIDEO);
                intent.putExtra(VideoConst.EXTRA_NAME, VideoConst.EXTRA_VALUE_VIDEO_RECORD_STOP);
                getContext().sendBroadcast(intent);
                setResult(response, DConnectMessage.RESULT_OK);
            } else if (AudioRecorder.class.getName().equals(className)) {
                Intent intent = new Intent(AudioConst.SEND_HOSTDP_TO_AUDIO);
                intent.putExtra(AudioConst.EXTRA_NAME, AudioConst.EXTRA_NAME_AUDIO_RECORD_STOP);
                getContext().sendBroadcast(intent);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setIllegalDeviceStateError(response);
            }

            return true;
        }
    }

    @Override
    protected boolean onPutPause(final Intent request, final Intent response, final String serviceId,
                            final String target) {

        if (serviceId == null) {
            createEmptyServiceId(response);
            return true;
        } else if (!checkServiceId(serviceId)) {
            createNotFoundDevice(response);
            return true;
        } else {

            String className = getClassnameOfTopActivity();
            if (VideoRecorder.class.getName().equals(className)) {
                MessageUtils.setNotSupportAttributeError(response);
            } else if (AudioRecorder.class.getName().equals(className)) {
                Intent intent = new Intent(AudioConst.SEND_HOSTDP_TO_AUDIO);
                intent.putExtra(AudioConst.EXTRA_NAME, AudioConst.EXTRA_NAME_AUDIO_RECORD_PAUSE);
                getContext().sendBroadcast(intent);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setIllegalDeviceStateError(response);
            }

            return true;
        }
    }

    @Override
    protected boolean onPutResume(final Intent request, final Intent response, final String serviceId,
            final String target) {

        if (serviceId == null) {
            createEmptyServiceId(response);
            return true;
        } else if (!checkServiceId(serviceId)) {
            createNotFoundDevice(response);
            return true;
        } else {
            String className = getClassnameOfTopActivity();
            if (VideoRecorder.class.getName().equals(className)) {
                MessageUtils.setNotSupportAttributeError(response);
            } else if (AudioRecorder.class.getName().equals(className)) {
                Intent intent = new Intent(AudioConst.SEND_HOSTDP_TO_AUDIO);
                intent.putExtra(AudioConst.EXTRA_NAME, AudioConst.EXTRA_NAME_AUDIO_RECORD_RESUME);
                getContext().sendBroadcast(intent);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setIllegalDeviceStateError(response);
            }
        }

        return true;
    }

    /**
     * Generate a file name for video.
     * @return file name
     */
    private String generateVideoFileName() {
        return "video" + mSimpleDateFormat.format(new Date()) + VideoConst.FORMAT_TYPE;
    }

    /**
     * Generate a file name for audio.
     * @return file name
     */
    private String generateAudioFileName() {
        return "audio" + mSimpleDateFormat.format(new Date()) + AudioConst.FORMAT_TYPE;
    }

    /**
     * サービスIDをチェックする.
     * 
     * @param serviceId サービスID
     * @return <code>serviceId</code>がテスト用サービスIDに等しい場合はtrue、そうでない場合はfalse
     */
    private boolean checkServiceId(final String serviceId) {
        String regex = HostServiceDiscoveryProfile.SERVICE_ID;
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(serviceId);
        return m.find();
    }

    /**
     * 画面の一番上にでているActivityのクラス名を取得.
     * 
     * @return クラス名
     */
    private String getClassnameOfTopActivity() {
        ActivityManager activitMgr = (ActivityManager) getContext().getSystemService(Service.ACTIVITY_SERVICE);
        String className = activitMgr.getRunningTasks(1).get(0).topActivity.getClassName();
        return className;
    }

    /**
     * デバイスが発見できなかった場合のエラーを作成する.
     * 
     * @param response レスポンスを格納するIntent
     */
    private void createNotFoundDevice(final Intent response) {
        MessageUtils.setNotFoundDeviceError(response, "Device is not found.");
    }

    /**
     * サービスIDをチェックする.
     * 
     * @param serviceId サービスID
     * @return <code>serviceId</code>がテスト用サービスIDに等しい場合はtrue、そうでない場合はfalse
     */
    private boolean checkserviceId(final String serviceId) {
        return HostServiceDiscoveryProfile.SERVICE_ID.equals(serviceId);
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
