/*
SonyCameraDeviceService
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.sonycamera;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import com.example.sony.cameraremote.ServerDevice;
import com.example.sony.cameraremote.SimpleCameraEventObserver;
import com.example.sony.cameraremote.SimpleRemoteApi;
import com.example.sony.cameraremote.SimpleSsdpClient;
import com.example.sony.cameraremote.utils.SimpleLiveviewSlicer;
import com.example.sony.cameraremote.utils.SimpleLiveviewSlicer.Payload;

import org.deviceconnect.android.deviceplugin.sonycamera.profile.SonyCameraMediaStreamRecordingProfile;
import org.deviceconnect.android.deviceplugin.sonycamera.profile.SonyCameraServiceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.sonycamera.profile.SonyCameraSystemProfile;
import org.deviceconnect.android.deviceplugin.sonycamera.profile.SonyCameraZoomProfile;
import org.deviceconnect.android.deviceplugin.sonycamera.utils.DConnectUtil;
import org.deviceconnect.android.deviceplugin.sonycamera.utils.MixedReplaceMediaServer;
import org.deviceconnect.android.deviceplugin.sonycamera.utils.MixedReplaceMediaServer.ServerEventListener;
import org.deviceconnect.android.deviceplugin.sonycamera.utils.UserSettings;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.MediaStreamRecordingProfile;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.ServiceInformationProfile;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.provider.FileManager;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.deviceconnect.profile.MediaStreamRecordingProfileConstants;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants.NetworkType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * SonyCameraデバイスプラグイン用サービス.
 * @author NTT DOCOMO, INC.
 */
public class SonyCameraDeviceService extends DConnectMessageService {
    /** ファイル名に付けるプレフィックス. */
    private static final String FILENAME_PREFIX = "sony_camera_";
    /** ファイルの拡張子. */
    private static final String FILE_EXTENSION = ".png";
    /** デバイス名. */
    private static final String DEVICE_NAME = "Sony Camera";
    /** サービスID. */
    private static final String SERVICE_ID = "sony_camera";
    /** リトライ回数. */
    private static final int MAX_RETRY_COUNT = 3;
    /** 待機時間. */
    private static final int WAIT_TIME = 100;

    /** 動画撮影モード. */
    private static final String SONY_CAMERA_SHOOT_MODE_MOVIE = "movie";
    /** 静止画撮影モード. */
    private static final String SONY_CAMERA_SHOOT_MODE_PIC = "still";

    /** 撮影中. */
    private static final String SONY_CAMERA_STATUS_RECORDING = "MovieRecording";
    /** 停止中. */
    private static final String SONY_CAMERA_STATUS_IDLE = "IDLE";

    /** Defines a period 50 millisecond between server shutdown. */
    private static final int PERIOD_WAIT_TIME = 50;

    /** ターゲットID. */
    private static final String TARGET_ID = "sonycamera";

    /** ロガー. */
    private Logger mLogger = Logger.getLogger("sonycamera.dplugin");

    /** 接続中カメラが利用できるRemote API一覧. */
    private String mAvailableApiList = "";

    /** SonyCameraとの接続管理クライアント. */
    private SimpleSsdpClient mSsdpClient;
    /** SonyCameraのリモートAPI. */
    private SimpleRemoteApi mRemoteApi;
    /** SonyCameraのイベント監視クラス. */
    private SimpleCameraEventObserver mEventObserver;

    /** SonyCameraの設定. */
    private UserSettings mSettings;

    /** 日付のフォーマット. */
    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyyMMdd_kkmmss", Locale.JAPAN);

    /** プレビューフラグ. */
    private boolean mWhileFetching = false;

    /** ファイル管理クラス. */
    private FileManager mFileMgr;

    /** リトライ回数. */
    private int mRetryCount;

    /** 撮影画像サイズ20Mの場合のピクセル数. */
    private static final int PIXELS_20_M = 20000000;
    /** 撮影画像サイズ18Mの場合のピクセル数. */
    private static final int PIXELS_18_M = 18000000;
    /** 撮影画像サイズ17Mの場合のピクセル数. */
    private static final int PIXELS_17_M = 17000000;
    /** 撮影画像サイズ13Mの場合のピクセル数. */
    private static final int PIXELS_13_M = 13000000;
    /** 撮影画像サイズ7.5Mの場合のピクセル数. */
    private static final int PIXELS_7_5_M = 7500000;
    /** 撮影画像サイズ25Mの場合のピクセル数. */
    private static final int PIXELS_5_M = 5000000;
    /** 撮影画像サイズ4.2Mの場合のピクセル数. */
    private static final int PIXELS_4_2_M = 4200000;
    /** 撮影画像サイズ3.7Mの場合のピクセル数. */
    private static final int PIXELS_3_7_M = 3700000;
    /** 入力タイムゾーンのパース計算用パラメータ. */
    private static final int CUL_PARAM_DATETIME = 100;
    /** 入力タイムゾーンのパース計算用パラメータ. */
    private static final int CUL_PARAM_MINITE = 60;

    /**
     * パーセント表示変換用.
     */
    private static final double VAL_TO_PERCENTAGE = 100.0;

    /**
     * executorインスタンス.
     */
    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    /**
     * 接続中のSonyCameraのSSIDを保持する.
     */
    private String mSSID;

    /**
     * Server for MotionJPEG.
     */
    private MixedReplaceMediaServer mServer;
    
    @Override
    public void onCreate() {
        mLogger.entering(this.getClass().getName(), "onCreate");
        super.onCreate();

        EventManager.INSTANCE.setController(new MemoryCacheController());

        mSettings = new UserSettings(this);
        mSsdpClient = new SimpleSsdpClient();

        // ファイル管理クラスの作成
        mFileMgr = new FileManager(this);

        addProfile(new SonyCameraMediaStreamRecordingProfile());
        addProfile(new SonyCameraZoomProfile());

        // SonyCameraデバイスプラグインではSettingsプロファイルは非サポート.
        //addProfile(new SonyCameraSettingsProfile());

        WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        if (DConnectUtil.checkSSID(wifiInfo.getSSID())) {
            connectSonyCamera();
        } else {
            deleteSonyCameraSDK();
        }

        mLogger.exiting(this.getClass().getName(), "onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mExecutor.shutdown();
        deleteSonyCameraSDK();
        if (mServer != null) {
            mServer.stop();
            mServer = null;
        }
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (intent == null) {
            return START_STICKY;
        }

        String action = intent.getAction();
        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
            if (state == WifiManager.WIFI_STATE_ENABLED) {
                WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
                if (DConnectUtil.checkSSID(wifiInfo.getSSID())) {
                    connectSonyCamera();
                } else {
                    deleteSonyCameraSDK();
                }
            } else if (state == WifiManager.WIFI_STATE_DISABLED) {
                deleteSonyCameraSDK();
            }
            return START_STICKY;
        } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            NetworkInfo ni = cm.getActiveNetworkInfo();
            if (ni != null) {
                NetworkInfo.State state = ni.getState();
                int type = ni.getType();
                if (ni.isConnected() && state == NetworkInfo.State.CONNECTED && type == ConnectivityManager.TYPE_WIFI) {
                    WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
                    if (DConnectUtil.checkSSID(wifiInfo.getSSID())) {
                        if (!wifiInfo.getSSID().equals(mSSID)) {
                            connectSonyCamera();
                        }
                    } else {
                        deleteSonyCameraSDK();
                    }
                }
            }
            return START_STICKY;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * SonyCameraデバイスの検索を行う. 
     * <p>
     * Network Service Deiscovery APIに対応する.
     * </p>
     * @param request リクエスト
     * @param response レスポンス
     * @return 即座にレスポンスを返す場合はtrue、それ以外はfalse
     */
    public boolean searchSonyCameraDevice(final Intent request, final Intent response) {
        mLogger.entering(this.getClass().getName(), "createSearchResponse");

        WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        List<Bundle> services = new ArrayList<Bundle>();
        if (checkDevice() && DConnectUtil.checkSSID(wifiInfo.getSSID())) {
            mLogger.fine("device found: " + checkDevice());

            Bundle service = new Bundle();
            service.putString(ServiceDiscoveryProfile.PARAM_ID, SERVICE_ID);
            service.putString(ServiceDiscoveryProfile.PARAM_NAME, DEVICE_NAME);
            service.putString(ServiceDiscoveryProfile.PARAM_TYPE,
                    ServiceDiscoveryProfile.NetworkType.WIFI.getValue());
            service.putBoolean(ServiceDiscoveryProfile.PARAM_ONLINE, true);
            service.putString(ServiceDiscoveryProfile.PARAM_CONFIG, wifiInfo.getSSID());
            setScopes(service);
            services.add(service);

            // SonyCameraを見つけたので、SSIDを保存しておく
            mSettings.setSSID(wifiInfo.getSSID());
        }

        response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
        response.putExtra(ServiceDiscoveryProfile.PARAM_SERVICES, services.toArray(new Bundle[services.size()]));

        mLogger.exiting(this.getClass().getName(), "createSearchResponse");
        return true;
    }

    /**
     * デバイスプラグインのサポートするプロファイル一覧を設定する.
     * 
     * @param service デバイスパラメータ
     */
    private void setScopes(final Bundle service) {
        ArrayList<String> scopes = new ArrayList<String>();
        for (DConnectProfile profile : getProfileList()) {
            scopes.add(profile.getProfileName());
        }
        service.putStringArray(ServiceDiscoveryProfileConstants.PARAM_SCOPES, 
                scopes.toArray(new String[scopes.size()]));
    }

    /**
     * カメラの情報を取得する.
     * 
     * @param request リクエスト
     * @param response レスポンス
     * @param serviceId サービスID
     * @return 即座にレスポンスする場合はtrue、それ以外はfalse
     */
    public boolean onGetMediaRecorder(final Intent request, final Intent response, final String serviceId) {

        if (serviceId == null || !serviceId.equals(SERVICE_ID)) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }

        if (mAvailableApiList == null) {
            MessageUtils.setIllegalDeviceStateError(response, "device is not ready.");
            return true;
        }

        if (mAvailableApiList.indexOf("getStillSize") == -1) {
            MessageUtils.setNotSupportAttributeError(response);
            return true;
        }

        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    getMediaRecorder(request, response);
                } catch (IOException e) {
                    sendErrorResponse(request, response);
                } catch (JSONException e) {
                    sendErrorResponse(request, response);
                }
            }
        });
        return false;
    }

    /**
     * カメラ情報を取得する.
     * @param request リクエスト
     * @param response レスポンス
     * @throws IOException SonyCameraとの通信に失敗した場合に発生
     * @throws JSONException JSONの解析に失敗した場合に発生
     */
    private void getMediaRecorder(final Intent request, final Intent response)
            throws IOException, JSONException {

        List<Bundle> recorders = new ArrayList<Bundle>();

        String state = convertCameraState(getCameraState());
        int[] size = getCameraSize();

        Bundle recorder = new Bundle();
        recorder.putString(MediaStreamRecordingProfile.PARAM_ID, TARGET_ID);
        recorder.putString(MediaStreamRecordingProfile.PARAM_NAME, DEVICE_NAME);
        recorder.putString(MediaStreamRecordingProfile.PARAM_STATE, state);
        if (size != null) {
            recorder.putInt(MediaStreamRecordingProfile.PARAM_IMAGE_WIDTH, size[0]);
            recorder.putInt(MediaStreamRecordingProfile.PARAM_IMAGE_HEIGHT, size[1]);
        }
        recorder.putString(MediaStreamRecordingProfile.PARAM_MIME_TYPE, "image/png");
        recorders.add(recorder);

        response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
        response.putExtra(MediaStreamRecordingProfile.PARAM_RECORDERS,
                recorders.toArray(new Bundle[recorders.size()]));
        sendResponse(request, response);
    }

    /**
     * カメラのサイズを取得する.
     * @return カメラのサイズ
     * @throws IOException 通信に失敗した場合に発生
     * @throws JSONException JSONの解析に失敗した場合に発生
     */
    private String[] getStillSize() throws IOException, JSONException {
        try {
            JSONObject replyJson = mRemoteApi.getStillSize();
            if (!isErrorReply(replyJson)) {
                JSONArray resultsObj = replyJson.optJSONArray("result");
                replyJson = resultsObj.optJSONObject(0);
                String[] str = new String[2];
                str[0] = replyJson.optString("aspect");
                str[1] = replyJson.optString("size");
                return str;
            }
        } catch (IOException e) {
            // ファームウェアがアップデートされていないと使えない
            return null;
        }
        return null;
    }

    /**
     * カメラの状態を取得する.
     * @return カメラの状態
     * @throws IOException 通信に失敗した場合に発生
     * @throws JSONException JSONの解析に失敗した場合に発生
     */
    private String getCameraState() throws IOException, JSONException {
        JSONObject replyJson;
        replyJson = mRemoteApi.getEvent(false);
        if (!isErrorReply(replyJson)) {
            JSONArray resultObject = replyJson.getJSONArray("result");
            replyJson = resultObject.getJSONObject(1);
            return replyJson.getString("cameraStatus");
        }
        return null;
    }

    /**
     * カメラのサイズを取得する.
     * @return カメラのサイズ
     * @throws IOException 通信に失敗した場合に発生
     * @throws JSONException JSONの解析に失敗した場合
     */
    private int[] getCameraSize() throws IOException, JSONException {
        String[] stillSize = getStillSize();
        if (stillSize != null) {
            String aspect = stillSize[0];
            String size = stillSize[1];
            int width = 0;
            int height = 0;
            int index = aspect.indexOf(":");
            if (index != -1) {
                width = Integer.valueOf(aspect.substring(0, index));
                height = Integer.valueOf(aspect.substring(index + 1));
                int ss = (int) pixelValueCalculate(width, height, size);
                if (ss != 0) {
                    width *= ss;
                    height *= ss;
                }
                
                int[] s = new int[2];
                s[0] = width;
                s[1] = height;
            }
        }
        return null;
    }
    /**
     * カメラの状態をDeviceConnectの状態に変換する.
     * @param cameraState カメラの状態
     * @return DeviceConnectの状態
     */
    private String convertCameraState(final String cameraState) {
        if (cameraState == null) {
            return "unknown";
        } else if (cameraState.equals("Error") || cameraState.equals("NotReady")
                || cameraState.equals("MovieSaving") || cameraState.equals("AudioSaving")
                || cameraState.equals("StillSaving") || cameraState.equals("IDLE")) {
            return "inactive";
        } else if (cameraState.equals("StillCapturing") || cameraState.equals("MediaRecording")
                || cameraState.equals("AudioRecording") || cameraState.equals("IntervalRecording")) {
            return "recording";
        } else if (cameraState.equals("MovieWaitRecStart") || cameraState.equals("MoviewWaitRecStop")
                || cameraState.equals("AudioWaitRecStart") || cameraState.equals("AudioRecWaitRecStop")
                || cameraState.equals("IntervalWaitRecStart")
                || cameraState.equals("IntervalWaitRecStop")) {
            return "paused";
        } else {
            return "unknown";
        }
    }

    /**
     * ピクセル数計算用メソッド.
     * 
     * @param widthVal width
     * @param heightVal height
     * @param size aspect
     * @return stillSize
     */
    private double pixelValueCalculate(final int widthVal, final int heightVal, final String size) {
        int pixels = 0;
        int width = widthVal;
        int height = heightVal;
        double pixelValue = 0;

        if (size.equals("20M")) {
            pixels = PIXELS_20_M;
            pixelValue = Math.sqrt(pixels / (width * height));
        } else if (size.equals("18M")) {
            pixels = PIXELS_18_M;
            pixelValue = Math.sqrt(pixels / (width * height));
        } else if (size.equals("17M")) {
            pixels = PIXELS_17_M;
            pixelValue = Math.sqrt(pixels / (width * height));
        } else if (size.equals("13M")) {
            pixels = PIXELS_13_M;
            pixelValue = Math.sqrt(pixels / (width * height));
        } else if (size.equals("7.5M")) {
            pixels = PIXELS_7_5_M;
            pixelValue = Math.sqrt(pixels / (width * height));
        } else if (size.equals("5M")) {
            pixels = PIXELS_5_M;
            pixelValue = Math.sqrt(pixels / (width * height));
        } else if (size.equals("4.2M")) {
            pixels = PIXELS_4_2_M;
            pixelValue = Math.sqrt(pixels / (width * height));
        } else if (size.equals("3.7M")) {
            pixels = PIXELS_3_7_M;
            pixelValue = Math.sqrt(pixels / (width * height));
        }

        return pixelValue;
    }

    /**
     * takephotoに対応するメソッド.
     * 
     * @param request リクエスト
     * @param response レスポンス
     * @param serviceId サービスID
     * @param target ターゲット
     * @return 即座にレスポンスを返す場合はtrue、それ以外はfalse
     */
    public boolean onPostTakePhoto(final Intent request, final Intent response, final String serviceId,
            final String target) {
        mLogger.entering(this.getClass().getName(), "onPostTakePhoto");

        response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
        if (serviceId == null || !serviceId.equals(SERVICE_ID)) {
            mLogger.warning("serviceId is invalid. serviceId=" + serviceId);
            mLogger.exiting(this.getClass().getName(), "onPostTakePhoto");
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }

        if (target != null) {
            if (!TARGET_ID.equals(target)) {
                MessageUtils.setInvalidRequestParameterError(
                        response, "target is invalid.");
                return true;
            }
        }

        if (SONY_CAMERA_STATUS_RECORDING.equals(mEventObserver.getCameraStatus())) {
            // 撮影中は、さらに撮影できないのでエラーを返す
            MessageUtils.setIllegalDeviceStateError(response);
            return true;
        }

        if (SONY_CAMERA_SHOOT_MODE_PIC.equals(mEventObserver.getShootMode())) {
            takePicture(request, response);
        } else {
            // 撮影モードが静止画になっていない場合はモードを切り替えてから撮影する
            setShootMode(request, response, SONY_CAMERA_SHOOT_MODE_PIC, new Runnable() {
                @Override
                public void run() {
                    takePicture(request, response);
                }
            });
        }
        mLogger.exiting(this.getClass().getName(), "onPostTakePhoto");
        return false;
    }

    /**
     * 写真撮影を行う.
     * 
     * @param request リクエスト
     * @param response レスポンス
     */
    private void takePicture(final Intent request, final Intent response) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                InputStream istream = null;
                response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
                try {
                    JSONObject replyJson = mRemoteApi.actTakePicture();
                    if (isErrorReply(replyJson)) {
                        sendErrorResponse(request, response);
                    } else {
                        JSONArray resultsObj = replyJson.getJSONArray("result");
                        JSONArray imageUrlsObj = resultsObj.getJSONArray(0);
                        String postImageUrl = null;
                        if (1 <= imageUrlsObj.length()) {
                            postImageUrl = imageUrlsObj.getString(0);
                        }
                        if (postImageUrl == null) {
                            sendErrorResponse(request, response);
                        } else {
                            istream = new URL(postImageUrl).openStream();
                            String filename = getFileName();
                            String uri = mFileMgr.saveFile(filename, istream);
                            if (filename != null) {
                                response.putExtra(MediaStreamRecordingProfile.PARAM_URI, uri);
                                response.putExtra(MediaStreamRecordingProfile.PARAM_PATH, mFileMgr.getBasePath()
                                        .toString() + "/" + filename);
                                notifyTakePhoto(filename, uri);
                                sendResponse(request, response);
                            } else {
                                sendErrorResponse(request, response);
                            }
                        }
                    }
                } catch (IOException e) {
                    mLogger.warning("Exception in takePicture." + e.toString());
                    sendErrorResponse(request, response);
                } catch (JSONException e) {
                    mLogger.warning("Exception in takePicture." + e.toString());
                    sendErrorResponse(request, response);
                } finally {
                    if (istream != null) {
                        try {
                            istream.close();
                        } catch (IOException e) {
                            mLogger.warning("Exception occurred in close.");
                        }
                    }
                }
            }
        });
    }

    /**
     * 動画撮影の要求を処理する.
     * 
     * @param request リクエスト
     * @param response レスポンス
     * @param serviceId サービスID
     * @param target ターゲット
     * @param timeslice タイムスライス
     * @return 即座に返答する場合はtrue、それ以外はfalse
     */
    public boolean onPostRecord(final Intent request, final Intent response, final String serviceId,
            final String target, final Long timeslice) {

        if (serviceId == null || !serviceId.equals(SERVICE_ID)) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }

        if (SONY_CAMERA_STATUS_RECORDING.equals(mEventObserver.getCameraStatus())) {
            // 撮影中は、さらに撮影できないのでエラーを返す
            MessageUtils.setIllegalDeviceStateError(response);
            return true;
        }

        if (SONY_CAMERA_SHOOT_MODE_MOVIE.equals(mEventObserver.getShootMode())) {
            startMovieRec(request, response);
        } else {
            // 撮影モードが動画になっていない場合には、撮影モードを切り替えてから撮影する
            setShootMode(request, response, SONY_CAMERA_SHOOT_MODE_MOVIE, new Runnable() {
                @Override
                public void run() {
                    startMovieRec(request, response);
                }
            });
        }
        return false;
    }

    /**
     * 動画撮影を開始する.
     * 
     * @param request リクエスト
     * @param response レスポンス
     */
    private void startMovieRec(final Intent request, final Intent response) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject replyJson = mRemoteApi.startMovieRec();
                    if (isErrorReply(replyJson)) {
                        MessageUtils.setIllegalDeviceStateError(response);
                        sendErrorResponse(request, response);
                    } else {
                        JSONArray resultsObj = replyJson.getJSONArray("result");
                        int resultCode = resultsObj.getInt(0);
                        if (resultCode == 0) {
                            sendResponse(request, response);
                        } else {
                            sendErrorResponse(request, response);
                        }
                    }
                } catch (IOException e) {
                    mLogger.warning("Exception occurred in startMovieRec.");
                    sendErrorResponse(request, response);
                } catch (JSONException e) {
                    mLogger.warning("Exception occurred in startMovieRec.");
                    sendErrorResponse(request, response);
                }
            }
        });
    }

    /**
     * 撮影の停止要求を処理する.
     * 
     * @param request リクエスト
     * @param response レスポンス
     * @param serviceId サービスID
     * @param mediaId メディアID
     * @return 即座に返答する場合はtrue、それ以外はfalse
     */
    public boolean onPutStop(final Intent request, final Intent response, final String serviceId,
            final String mediaId) {

        if (serviceId == null || !serviceId.equals(SERVICE_ID)) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }

        if (SONY_CAMERA_STATUS_IDLE.equals(mEventObserver.getCameraStatus())) {
            // 撮影が開始されていないので、エラーを返す。
            MessageUtils.setIllegalDeviceStateError(response);
            return true;
        }

        if (!SONY_CAMERA_SHOOT_MODE_MOVIE.equals(mEventObserver.getShootMode())) {
            // 撮影モードが違うのでエラー
            MessageUtils.setIllegalDeviceStateError(response);
            return true;
        }

        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject replyJson = mRemoteApi.stopMovieRec();
                    if (isErrorReply(replyJson)) {
                        sendErrorResponse(request, response);
                    } else {
                        JSONArray resultsObj = replyJson.getJSONArray("result");
                        String thumbnailUrl = resultsObj.getString(0);
                        if (thumbnailUrl != null) {
                            sendResponse(request, response);
                        } else {
                            sendErrorResponse(request, response);
                        }
                    }
                } catch (IOException e) {
                    mLogger.warning("Exception occurred in stopMovieRec." + e.toString());
                    sendErrorResponse(request, response);
                } catch (JSONException e) {
                    mLogger.warning("Exception occured in stopMovieRec." + e.toString());
                    sendErrorResponse(request, response);
                }
            }
        });
        return false;
    }

    /**
     * SonyCameraの撮影モードを切り替える.
     * 
     * @param request リクエスト
     * @param response レスポンス
     * @param mode モード
     * @param run 切り替え後の処理
     */
    private void setShootMode(final Intent request, final Intent response, final String mode, final Runnable run) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject replyJson = mRemoteApi.setShootMode(mode);
                    if (isErrorReply(replyJson)) {
                        sendErrorResponse(request, response);
                    } else {
                        JSONArray resultsObj = replyJson.getJSONArray("result");
                        int resultCode = resultsObj.getInt(0);
                        if (resultCode == 0) {
                            run.run();
                        } else {
                            sendErrorResponse(request, response);
                        }
                    }
                } catch (IOException e) {
                    mLogger.warning("Exception occurred in setShootMode.");
                    sendErrorResponse(request, response);
                } catch (JSONException e) {
                    mLogger.warning("Exception occurred in setShootMode.");
                    sendErrorResponse(request, response);
                }
            }
        });
    }

    /**
     * 写真のデータを保存するファイル名を取得する.
     * 
     * @return 保存先のファイル名
     */
    private String getFileName() {
        return FILENAME_PREFIX + mSimpleDateFormat.format(new Date()) + FILE_EXTENSION;
    }

    /**
     * 写真撮影を通知する.
     * 
     * @param path 写真へのパス
     * @param uri 写真へのURI
     */
    public void notifyTakePhoto(final String path, final String uri) {
        mLogger.entering(this.getClass().getName(), "notifyTakePhoto");

        List<Event> evts = EventManager.INSTANCE.getEventList(SERVICE_ID,
                MediaStreamRecordingProfileConstants.PROFILE_NAME, null,
                MediaStreamRecordingProfileConstants.ATTRIBUTE_ON_PHOTO);

        String photoPath = mFileMgr.getBasePath().getPath().toString() + "/" + path;
        for (Event evt : evts) {
            Bundle photo = new Bundle();
            photo.putString(MediaStreamRecordingProfile.PARAM_URI, uri);
            photo.putString(MediaStreamRecordingProfile.PARAM_PATH, photoPath);
            photo.putString(MediaStreamRecordingProfile.PARAM_MIME_TYPE, "image/png");

            Intent intent = new Intent(IntentDConnectMessage.ACTION_EVENT);
            intent.setComponent(ComponentName.unflattenFromString(evt.getReceiverName()));
            intent.putExtra(DConnectMessage.EXTRA_SERVICE_ID, SERVICE_ID);
            intent.putExtra(DConnectMessage.EXTRA_PROFILE, MediaStreamRecordingProfile.PROFILE_NAME);
            intent.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, MediaStreamRecordingProfile.ATTRIBUTE_ON_PHOTO);
            intent.putExtra(DConnectMessage.EXTRA_SESSION_KEY, evt.getSessionKey());
            intent.putExtra(MediaStreamRecordingProfile.PARAM_PHOTO, photo);

            sendEvent(intent, evt.getAccessToken());
        }
        mLogger.exiting(this.getClass().getName(), "notifyTakePhoto");
    }

    /**
     * SonyCameraに接続する.
     */
    private void connectSonyCamera() {
        mLogger.fine("Start a search for a SonyCamera device.");
        final boolean[] found = new boolean[1];
        mSsdpClient.search(new SimpleSsdpClient.SearchResultHandler() {
            @Override
            public void onDeviceFound(final ServerDevice device) {
                mLogger.fine("Found SonyCamera device." + device.getModelName());
                found[0] = true;
                createSonyCameraSDK(device);
                
                // 接続したwifiのSSIDを保持
                WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
                mSSID = wifiInfo.getSSID();
            }

            @Override
            public void onFinished() {
                if (!found[0]) {
                    mLogger.warning("Cannot found SonyCamera device.");
                    // 見つからない場合には、何回か確認を行う
                    mRetryCount++;
                    if (mRetryCount < MAX_RETRY_COUNT) {
                        mExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(WAIT_TIME);
                                } catch (InterruptedException e) {
                                    mLogger.warning("Exception ocurred in Thread.sleep.");
                                }
                                mLogger.fine("Retry connect to SonyCamera device.");
                                connectSonyCamera();
                            }
                        });
                    } else {
                        deleteSonyCameraSDK();
                    }
                }
            }

            @Override
            public void onErrorFinished() {
                mLogger.warning("Error occurred in SsdpClient#serarch.");
            }
        });
    }

    /**
     * SonyCameraデバイスSDKを作成する.
     * 
     * @param device SonyCameraデバイス
     */
    private void createSonyCameraSDK(final ServerDevice device) {
        if (mRemoteApi != null) {
            deleteSonyCameraSDK();
        }
        mRemoteApi = new SimpleRemoteApi(device);
        mAvailableApiList = getAvailableApi();

        createEventObserver();

        List<Event> evts = EventManager.INSTANCE.getEventList(SERVICE_ID,
                ServiceDiscoveryProfileConstants.PROFILE_NAME, null,
                ServiceDiscoveryProfileConstants.ATTRIBUTE_ON_SERVICE_CHANGE);

        for (Event evt : evts) {
            Bundle camera = new Bundle();
            camera.putString(ServiceDiscoveryProfile.PARAM_NAME, DEVICE_NAME);
            camera.putString(ServiceDiscoveryProfile.PARAM_TYPE, NetworkType.WIFI.getValue());
            camera.putBoolean(ServiceDiscoveryProfile.PARAM_STATE, true);
            camera.putBoolean(ServiceDiscoveryProfile.PARAM_ONLINE, true);
            camera.putString(ServiceDiscoveryProfile.PARAM_CONFIG, "");

            Intent intent = new Intent(IntentDConnectMessage.ACTION_EVENT);
            intent.setComponent(ComponentName.unflattenFromString(evt.getReceiverName()));
            intent.putExtra(DConnectMessage.EXTRA_SERVICE_ID, SERVICE_ID);
            intent.putExtra(DConnectMessage.EXTRA_PROFILE,
                    ServiceDiscoveryProfile.PROFILE_NAME);
            intent.putExtra(DConnectMessage.EXTRA_ATTRIBUTE,
                    ServiceDiscoveryProfile.ATTRIBUTE_ON_SERVICE_CHANGE);
            intent.putExtra(DConnectMessage.EXTRA_SESSION_KEY, evt.getSessionKey());
            intent.putExtra(ServiceDiscoveryProfile.PARAM_NETWORK_SERVICE, camera);

            sendEvent(intent, evt.getAccessToken());
        }
        mLogger.exiting(this.getClass().getName(), "createSonyCameraSDK");
    }

    /**
     * SonyCameraデバイスSDKを破棄する.
     */
    private void deleteSonyCameraSDK() {
        mWhileFetching = false;

        if (mEventObserver != null) {
            mEventObserver.stop();
            mEventObserver = null;
        }
        if (mRemoteApi != null) {
            mRemoteApi = null;
            // dConnectManagerにデバイスの消失を通知
            // Intent event = MessageUtils.createEventIntent();
        }
        mRetryCount = 0;
        mSSID = null;
    }

    /**
     * SonyCameraデバイスからのイベントを待つスレッドを作成する.
     */
    private void createEventObserver() {
        if (mEventObserver == null || !mEventObserver.isStarted()) {
            mEventObserver = new SimpleCameraEventObserver(this, mRemoteApi);
            mEventObserver.setEventChangeListener(new SimpleCameraEventObserver.ChangeListener() {
                @Override
                public void onShootModeChanged(final String shootMode) {
                }

                @Override
                public void onCameraStatusChanged(final String status) {
                }

                @Override
                public void onApiListModified(final List<String> apis) {
                }

                @Override
                public void onZoomPositionChanged(final int zoomPosition) {
                }

                @Override
                public void onLiveviewStatusChanged(final boolean status) {
                }

                @Override
                public void onTakePicture(final String postImageUrl) {
                    mLogger.entering(this.getClass().getName(), "onTakePicture", postImageUrl);

                    InputStream istream = null;
                    try {
                        istream = new URL(postImageUrl).openStream();
                        String filename = getFileName();
                        String uri = mFileMgr.saveFile(filename, istream);
                        notifyTakePhoto(filename, uri);
                    } catch (IOException e) {
                        mLogger.warning("Exception in onTakePicture." + e.toString());
                    } finally {
                        if (istream != null) {
                            try {
                                istream.close();
                            } catch (IOException e) {
                                mLogger.warning("Exception occurred in close.");
                            }
                        }
                    }
                    mLogger.exiting(this.getClass().getName(), "onTakePicture");
                }
            });
            mEventObserver.start();
        }
    }

    /**
     * プレビューを追加する.
     * 
     * @param request リクエスト
     * @param response レスポンス
     * 
     * @return プレビューの開始ができた場合はtrue、それ以外はfalse
     */
    public synchronized boolean onPutPreview(final Intent request, final Intent response) {
        if (mRemoteApi == null) {
            MessageUtils.setIllegalDeviceStateError(response, "The sony camera is not connected.");
            return true;
        }
        if (mWhileFetching) {
            response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
            MediaStreamRecordingProfile.setUri(response, mServer.getUrl());
            return true;
        } else {
            if (mServer != null) {
                mExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        // このスレッドが動く前にサーバの起動が行われた場合にはすぐにレスポンスを返却する
                        if (mWhileFetching && mServer != null) {
                            MediaStreamRecordingProfile.setResult(response,
                                    DConnectMessage.RESULT_OK);
                            MediaStreamRecordingProfile.setUri(response,
                                    mServer.getUrl());
                            sendErrorResponse(request, response);
                            return;
                        }
                        // 前回起動時のサーバが停止していないので、ここで待つ
                        while (!mWhileFetching && mServer != null) {
                            try {
                                Thread.sleep(PERIOD_WAIT_TIME);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }
                        startPreview(request, response);
                    }
                });
            } else {
                startPreview(request, response);
            }
        }
        return false;
    }

    /**
     * プレビューを開始する.
     * 
     * @param request リクエスト
     * @param response レスポンス
     */
    private void startPreview(final Intent request, final Intent response) {
        mWhileFetching = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                SimpleLiveviewSlicer slicer = null;
                try {
                    // Prepare for connecting.
                    JSONObject replyJson = null;
                    replyJson = mRemoteApi.startLiveview();
                    if (!isErrorReply(replyJson)) {
                        JSONArray resultsObj = replyJson.getJSONArray("result");
                        String liveviewUrl = null;
                        if (1 <= resultsObj.length()) {
                            // Obtain liveview URL from the result.
                            liveviewUrl = resultsObj.getString(0);
                        }
                        if (liveviewUrl != null) {
                            // Create Slicer to open the stream and parse it.
                            slicer = new SimpleLiveviewSlicer();
                            slicer.open(liveviewUrl);
                        }
                    }

                    if (slicer == null) {
                        mWhileFetching = false;
                        mLogger.warning("Failed to start a SimpleLiveviewSlicer.");
                        return;
                    }

                    if (mServer == null) {
                        mServer = new MixedReplaceMediaServer();
                        mServer.setServerEventListener(new ServerEventListener() {
                            @Override
                            public void onStart() {
                            }
                            @Override
                            public void onStop() {
                                mWhileFetching = false;
                            }
                            @Override
                            public void onError() {
                                mWhileFetching = false;
                            }
                        });
                        mServer.setServerName("SonyCameraDevicePlugin Server");
                        mServer.setContentType("image/jpg");
                        String ip = mServer.start();
                        if (ip == null) {
                            mWhileFetching = false;
                            mLogger.warning("Failed to start server.");
                            sendErrorResponse(request, response);
                            return;
                        }
                    }

                    MediaStreamRecordingProfile.setUri(response, mServer.getUrl());
                    sendResponse(request, response);

                    while (mWhileFetching) {
                        final Payload payload = slicer.nextPayload();
                        if (payload == null) { // never occurs
                            continue;
                        }
                        mServer.offerMedia(payload.getJpegData());
                    }
                } catch (IOException e) {
                    mLogger.warning("IOException while fetching: " + e.getMessage());
                } catch (JSONException e) {
                    mLogger.warning("JSONException while fetching");
                } finally {
                    if (slicer != null) {
                        try {
                            slicer.close();
                        } catch (IOException e) {
                            mLogger.warning(
                                    "IOException while closing slicer: " + e.getMessage());
                        }
                    }
                    try {
                        mRemoteApi.stopLiveview();
                    } catch (IOException e) {
                        mLogger.warning("IOException while closing slicer: " + e.getMessage());
                    }

                    if (mServer != null) {
                        mServer.stop();
                        mServer = null;
                    }

                    mWhileFetching = false;
                }
            }
        }).start();
    }
    /**
     * プレビューを停止する.
     * @param request リクエスト
     * @param response レスポンス
     * @return result
     */
    public synchronized boolean onDeletePreview(final Intent request, final Intent response) {
        if (mRemoteApi == null) {
            MessageUtils.setIllegalDeviceStateError(response, "The sony camera is not connected.");
            return true;
        }
        if (!mWhileFetching) {
            MessageUtils.setIllegalDeviceStateError(response, "Preview has not been started.");
            return true;
        }
        stopPreview();
        response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
        return true;
    }

    /**
     * プレビューを削除する.
     */
    private void stopPreview() {
        mWhileFetching = false;
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject replyJson = mRemoteApi.stopLiveview();
                    if (!isErrorReply(replyJson)) {
                        mLogger.warning("cannot stop preview.");
                    }
                } catch (IOException e) {
                    mLogger.warning("IOException while fetching: " + e.getMessage());
                }
            }
        });
    }


    /**
     * SonyCameraデバイスのチェックを行う.
     * 
     * @return 有効な場合はtrue、それ以外はfalse
     */
    private boolean checkDevice() {
        return (mRemoteApi != null);
    }

    /**
     * 指定されたデータからレスポンスを作成する.
     * 
     * @param request リクエスト
     * @param response レスポンスするデータ
     */
    private void sendResponse(final Intent request, final Intent response) {
        response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
        sendResponse(response);
    }

    /**
     * エラーのレスポンスを作成し、送信する.
     * 
     * @param request リクエスト
     * @param response レスポンス
     */
    private void sendErrorResponse(final Intent request, final Intent response) {
        response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_ERROR);
        sendResponse(response);
    }

    /**
     * SonyCameraからの返り値のエラーチェック.
     * 
     * @param replyJson レスポンスJSON
     * @return エラーの場合はtrue、それ以外はfalse
     */
    private boolean isErrorReply(final JSONObject replyJson) {
        boolean hasError = (replyJson != null && replyJson.has("error"));
        return hasError;
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new SonyCameraSystemProfile();
    }

    @Override
    protected ServiceInformationProfile getServiceInformationProfile() {
        return new ServiceInformationProfile(this) { };
    }

    @Override
    protected ServiceDiscoveryProfile getServiceDiscoveryProfile() {
        return new SonyCameraServiceDiscoveryProfile(this);
    }

    /**
     * 時間を設定する.
     * 
     * @param request リクエスト
     * @param response レスポンス
     * @param serviceId サービスID
     * @param date 時間 yyyy-mm-ddThh:mm:ss+TimeZone(ex:0900)
     * @return 即座に返答する場合はtrue、それ以外はfalse
     */
    public boolean onPutDate(final Intent request, final Intent response, final String serviceId, final String date) {

        if (serviceId == null || !serviceId.equals(SERVICE_ID)) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }
        if (date == null) {
            MessageUtils.setInvalidRequestParameterError(response);
            return true;
        }
        if (!date.contains("+")) {
            MessageUtils.setInvalidRequestParameterError(response);
            return true;
        }
        if (mAvailableApiList == null) {
            MessageUtils.setUnknownError(response);
            return true;
        }
        if (mAvailableApiList.indexOf("setCurrentTime") == -1) {
            MessageUtils.setNotSupportActionError(response);
            return true;
        }

         // タイムゾーンをSonyCameraの入力仕様に合わせて変換
        int index = date.indexOf("+");
        final String mDate = date.substring(0, index) + "Z";
        String timeZoneData = date.substring(index + 1);
        int timeZone;

        try {
            timeZone = Integer.valueOf(timeZoneData);
        } catch (NumberFormatException e) {
            MessageUtils.setInvalidRequestParameterError(response);
            return true;
        }

        int hTime = timeZone / CUL_PARAM_DATETIME;
        int hMinute = timeZone - (hTime * CUL_PARAM_DATETIME);
        final int mTimeZone = (hTime * CUL_PARAM_MINITE) + hMinute;
        mLogger.fine("Date:" + mDate + "\nTimeZone:" + mTimeZone);

        response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject replyJson = mRemoteApi.setCurrentTime(mDate, mTimeZone);
                    if (replyJson == null) {
                        sendErrorResponse(request, response);
                    }
                    if (isErrorReply(replyJson)) {
                        sendErrorResponse(request, response);
                    } else {
                        JSONArray resultsObj = replyJson.getJSONArray("result");
                        if (resultsObj != null) {
                            sendResponse(request, response);
                        } else {
                            sendErrorResponse(request, response);
                        }
                    }
                } catch (IOException e) {
                    mLogger.warning("Exception in setCurrentTime." + e.toString());
                    sendErrorResponse(request, response);
                } catch (JSONException e) {
                    mLogger.warning("Exception in setCurrentTime." + e.toString());
                    sendErrorResponse(request, response);
                }
            }
        });
        return false;
    }

    /**
     * ズーム用メソッド.
     * 
     * @param request リクエスト
     * @param response レスポンス
     * @param serviceId サービスID
     * @param direction ズーム方向
     * @param movement ズーム動作
     * @return true
     */
    public boolean onPutActZoom(final Intent request, final Intent response, final String serviceId,
            final String direction, final String movement) {
        mLogger.entering(this.getClass().getName(), "onPutActZoom");

        response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
        if (serviceId == null || !serviceId.equals(SERVICE_ID)) {
            mLogger.warning("serviceId is invalid. serviceId=" + serviceId);
            mLogger.exiting(this.getClass().getName(), "onPutActZoom");
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }
        if (direction == null || movement == null) {
            MessageUtils.setInvalidRequestParameterError(response);
            return true;
        }
        if (mAvailableApiList == null) {
            MessageUtils.setUnknownError(response);
            return true;
        }
        
        if (mAvailableApiList.indexOf("actZoom") == -1) {
            MessageUtils.setNotSupportActionError(response);
            return true;
        }
        if (!direction.equals("in")) {
            if (!direction.equals("out")) {
                MessageUtils.setInvalidRequestParameterError(response);
            }
        }
        if (!movement.equals("start")) {
            if (!movement.equals("stop")) {
                if (!movement.equals("1shot")) {
                    if (!movement.equals("max")) {
                        MessageUtils.setInvalidRequestParameterError(response);
                    }
                }
            }
        }

        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject replyJson;
                    if (movement.equals("max")) {
                        replyJson = mRemoteApi.actZoom(direction, "start");
                    } else {
                        replyJson = mRemoteApi.actZoom(direction, movement);
                    }

                    if (isErrorReply(replyJson)) {
                        sendErrorResponse(request, response);
                    } else {
                        JSONArray resultsObj = replyJson.getJSONArray("result");
                        if (resultsObj != null) {
                            sendResponse(request, response);
                        } else {
                            sendErrorResponse(request, response);
                        }
                    }
                } catch (IOException e) {
                    mLogger.warning("Exception in actZoom." + e.toString());
                    sendErrorResponse(request, response);
                } catch (JSONException e) {
                    mLogger.warning("Exception in actZoom." + e.toString());
                    sendErrorResponse(request, response);
                }
            }
        });

        return false;
    }

    /**
     * ズーム倍率取得用メソッド.
     * 
     * @param request request
     * @param response response
     * @param serviceId deviceID
     * @return result
     */
    public boolean onGetZoomDiameter(final Intent request, final Intent response, final String serviceId) {
        if (serviceId == null || !serviceId.equals(SERVICE_ID)) {
            mLogger.warning("serviceId is invalid. serviceId=" + serviceId);
            mLogger.exiting(this.getClass().getName(), "onGetZoomDiameter");
            MessageUtils.setEmptyServiceIdError(response);
            sendResponse(response);
            return true;
        }
        if (mAvailableApiList == null) {
            MessageUtils.setUnknownError(response);
            sendResponse(response);
            return true;
        }
        if (mAvailableApiList.indexOf("getEvent") == -1
                || mAvailableApiList.indexOf("actZoom") == -1) {
            MessageUtils.setNotSupportActionError(response);
            sendResponse(response);
            return true;
        }
        response.putExtra(DConnectMessage.EXTRA_RESULT, DConnectMessage.RESULT_OK);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                double zoomDiameterParam = 0;
                try {
                    JSONObject replyJson = mRemoteApi.getEvent(mWhileFetching);
                    if (isErrorReply(replyJson)) {
                        sendErrorResponse(request, response);
                    } else {
                        JSONArray resultsObj = replyJson.getJSONArray("result");
                        replyJson = resultsObj.getJSONObject(2);
                        zoomDiameterParam = (Double) Double.valueOf(replyJson.getString("zoomPosition"))
                                / (Double) VAL_TO_PERCENTAGE;
                        DecimalFormat decimalFormat = new DecimalFormat("0.0#");
                        zoomDiameterParam = Double.valueOf(decimalFormat.format(zoomDiameterParam));

                        response.putExtra(SonyCameraZoomProfile.PARAM_ZOOM_POSITION, zoomDiameterParam);
                        sendResponse(request, response);
                    }
                } catch (IOException e) {
                    sendErrorResponse(request, response);
                } catch (JSONException e) {
                    sendErrorResponse(request, response);
                }
            }
        });

        return false;
    }

    /**
     * 接続中のカメラの利用可能Remote APIリストをStringで入手.
     * 
     * @return result Available API List
     */
    private String getAvailableApi() {
        String result = null;
        try {
            JSONObject replyJson = mRemoteApi.getAvailableApiList();
            if (replyJson != null) {
                JSONArray resultsObj = replyJson.getJSONArray("result");
                result = resultsObj.toString();
            }
        } catch (IOException e) {
            mLogger.warning("IOException in availableApiCheck.");
            e.printStackTrace();
        } catch (JSONException e) {
            mLogger.warning("JSONException in availableApiCheck.");
            e.printStackTrace();
        }
        return result;
    }
}
