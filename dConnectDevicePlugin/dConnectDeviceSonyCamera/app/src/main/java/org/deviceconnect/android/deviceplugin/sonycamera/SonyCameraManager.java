/*
SonyCameraManager
Copyright (c) 2017 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sonycamera;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.example.sony.cameraremote.ServerDevice;
import com.example.sony.cameraremote.SimpleCameraEventObserver;
import com.example.sony.cameraremote.SimpleRemoteApi;
import com.example.sony.cameraremote.SimpleSsdpClient;

import org.deviceconnect.android.deviceplugin.sonycamera.service.SonyCameraService;
import org.deviceconnect.android.deviceplugin.sonycamera.utils.SonyCameraPreview;
import org.deviceconnect.android.deviceplugin.sonycamera.utils.SonyCameraUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.deviceconnect.android.deviceplugin.sonycamera.utils.SonyCameraUtil.convertCameraState;
import static org.deviceconnect.android.deviceplugin.sonycamera.utils.SonyCameraUtil.isErrorReply;
import static org.deviceconnect.android.deviceplugin.sonycamera.utils.SonyCameraUtil.pixelValueCalculate;

/**
 * Sonyカメラを制御するためのクラス.
 * @author NTT DOCOMO, INC.
 */
public class SonyCameraManager {

    /**
     * リトライ回数.
     */
    private static final int MAX_RETRY_COUNT = 3;

    /**
     * 待機時間.
     */
    private static final int WAIT_TIME = 100;

    /**
     * パーセント表示変換用.
     */
    private static final double VAL_TO_PERCENTAGE = 100.0;

    /**
     * SonyCameraデバイスをDB管理するクラス.
     */
    private SonyCameraDBHelper mDBHelper;

    /**
     * コンテキスト.
     */
    private Context mContext;

    /**
     * SonyCameraとの接続管理クライアント.
     */
    private SimpleSsdpClient mSsdpClient;

    /**
     * SonyCameraのリモートAPI.
     */
    private SimpleRemoteApi mRemoteApi;

    /**
     * SonyCameraのイベント監視クラス.
     */
    private SimpleCameraEventObserver mEventObserver;

    /**
     * 接続中カメラが利用できるRemote API一覧.
     */
    private String mAvailableApiList = "";

    /**
     * SonyCameraデバイス一覧.
     */
    private final List<SonyCameraService> mSonyCameraServices;

    /**
     * 接続リトライカウント.
     */
    private int mRetryCount;

    /**
     * プレビューを管理するクラス.
     */
    private SonyCameraPreview mCameraPreview;

    /**
     * executorインスタンス.
     */
    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    /**
     * 写真撮影監視リスナー.
     */
    private OnSonyCamera2Listener mOnSonyCamera2Listener;

    /**
     * 現在接続中のWiFiのSSIDを保持します.
     */
    private String mSSID;

    /**
     * コンストラクタ.
     * @param context コンテキスト
     */
    SonyCameraManager(final Context context) {
        mContext = context;
        mDBHelper = new SonyCameraDBHelper(context);
        mSonyCameraServices = mDBHelper.getSonyCameraServices();
        mSsdpClient = new SimpleSsdpClient();
    }

    /**
     * 撮影イベントリスナーを設定します.
     * @param listener リスナー
     */
    void setOnSonyCamera2Listener(final OnSonyCamera2Listener listener) {
        mOnSonyCamera2Listener = listener;
    }

    /**
     * 接続中のSonyCameraのサービスIDを取得します.
     * @return サービスID
     */
    String getServiceId() {
        return mSSID;
    }

    /**
     * 指定されたサービスIDに接続されているか確認を行う.
     * @param serviceId サービスID
     * @return 接続されている場合はtrue、それ以外はfalse
     */
    public boolean isConnectedService(final String serviceId) {
        String ssid = getSSID();
        return mRemoteApi != null && ssid != null && ssid.equals(serviceId);
    }

    /**
     * Sonyカメラの撮影中か確認を行う.
     * @return 撮影中の場合はtrue、それ以外がはfalse
     */
    public boolean isRecording() {
        if (mEventObserver != null) {
            SonyCameraUtil.SonyCameraStatus status = SonyCameraUtil.SonyCameraStatus.getStatus(mEventObserver.getCameraStatus());
            SonyCameraUtil.SonyCameraMode mode = SonyCameraUtil.SonyCameraMode.getMode(mEventObserver.getShootMode());
            if (status == SonyCameraUtil.SonyCameraStatus.Recording && mode == SonyCameraUtil.SonyCameraMode.Movie) {
                return true;
            }
        }
        return false;
    }

    /**
     * ズームに対応しているか確認を行います.
     * @return 対応している場合はtrue、それ以外はfalse
     */
    public boolean isSupportedZoom() {
        return mAvailableApiList != null && mAvailableApiList.contains("actZoom");
    }

    /**
     * プレビューを撮影中か確認を行う.
     * @return 撮影中の場合はtrue、それ以外はfalse
     */
    public boolean isPreview() {
        return mCameraPreview != null && mCameraPreview.isPreview();
    }

    /**
     * Sonyカメラサービスのリストを取得します.
     * @return Sonyカメラサービスのリスト
     */
    List<SonyCameraService> getSonyCameraServices() {
        return mSonyCameraServices;
    }

    /**
     * Sonyカメラに接続を行います.
     */
    void connectSonyCamera() {
        final AtomicBoolean found = new AtomicBoolean();

        if (mSSID != null && mRemoteApi != null && mSSID.equals(getSSID())) {
            // すでに同じSonyカメラに接続されていた場合
            return;
        }

        mSsdpClient.search(new SimpleSsdpClient.SearchResultHandler() {
            @Override
            public void onDeviceFound(final ServerDevice device) {
                found.set(true);

                SonyCameraService s = foundSonyCamera();
                if (s != null) {
                    s.setOnline(true);
                }

                createSonyCameraSDK(device);
            }

            @Override
            public void onFinished() {
                if (!found.get()) {
                    // 見つからない場合には、何回か確認を行う
                    mRetryCount++;
                    if (mRetryCount < MAX_RETRY_COUNT) {
                        mExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(WAIT_TIME);
                                } catch (InterruptedException e) {
                                    // do nothing.
                                }
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
            }
        });
    }

    /**
     * Sonyカメラから切断します.
     */
    void disconnectSonyCamera() {
        deleteSonyCameraSDK();
    }

    /**
     * Sonyカメラサービスを管理から削除します.
     * @param service 削除するSonyカメラサービス
     */
    public void removeSonyCameraService(final SonyCameraService service) {
        mDBHelper.removeSonyCameraService(service);
    }

    /**
     * Sonyカメラで動作しているプレビューやレコーディングを停止します.
     */
    void resetSonyCamera() {
        if (mEventObserver != null) {
            // レコーディングの停止
            if (isRecording()) {
                stopMovieRec(new OnSonyCameraListener() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError() {
                    }
                });
            }

            // プレビューの停止
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    while (isRecording()) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    stopPreview();
                }
            });
        }
    }

    /**
     * Sonyカメラの状態を取得します.
     * @param listener Sonyカメラの状態を通知するリスナー
     */
    public void getCameraState(final OnCameraStateListener listener) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject replyJson = mRemoteApi.getEvent(false);
                    if (isErrorReply(replyJson)) {
                        listener.onError();
                    } else {
                        JSONArray resultObject = replyJson.getJSONArray("result");
                        replyJson = resultObject.getJSONObject(1);
                        String status = replyJson.getString("cameraStatus");
                        if (status != null) {
                            String state = convertCameraState(status);
                            int[] size = getCameraSize();
                            listener.onState(state, size);
                        } else {
                            listener.onError();
                        }
                    }
                } catch (Exception e) {
                    listener.onError();
                }
            }
        });
    }

    /**
     * 接続されているSonyCameraに写真撮影を要求します.
     *
     * @param listener 写真撮影の結果を通知するリスナー
     */
    public void takePicture(final OnTakePictureListener listener) {
        SonyCameraUtil.SonyCameraMode mode = SonyCameraUtil.SonyCameraMode.getMode(mEventObserver.getShootMode());

        if (mode == SonyCameraUtil.SonyCameraMode.Picture) {
            takePictureInternal(listener);
        } else {
            setShootMode(SonyCameraUtil.SonyCameraMode.Picture, new OnSonyCameraListener() {
                @Override
                public void onSuccess() {
                    takePictureInternal(listener);
                }

                @Override
                public void onError() {
                    listener.onError();
                }
            });
        }
    }

    /**
     * 接続されているSonyCameraにプレビュー開始を要求します.
     *
     * @param listener プレビュー開始の結果を通知するリスナー
     */
    public void startPreview(final SonyCameraPreview.OnPreviewListener listener) {
        startPreview(100, listener);
    }

    /**
     * 接続されているSonyCameraにプレビュー開始を要求します.
     *
     * @param timeSlice タイムスライス(ms)
     * @param listener プレビュー開始の結果を通知するリスナー
     */
    public void startPreview(final int timeSlice, final SonyCameraPreview.OnPreviewListener listener) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (isPreview()) {
                    listener.onPreviewServer(mCameraPreview.getPreviewUrl());
                } else {
                    if (mCameraPreview != null) {
                        mCameraPreview.stopPreview();
                        mCameraPreview = null;
                    }

                    mCameraPreview = new SonyCameraPreview(mRemoteApi);
                    mCameraPreview.setOnPreviewListener(listener);
                    mCameraPreview.setTimeSlice(timeSlice);
                    mCameraPreview.startPreview();
                }
            }
        });
    }

    /**
     * 接続されているSonyCameraにプレビュー停止を要求します.
     */
    public void stopPreview() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mCameraPreview != null) {
                    mCameraPreview.stopPreview();
                    mCameraPreview = null;
                }
            }
        });
    }

    /**
     * 接続されているSonyCameraに動画撮影開始を要求します.
     *
     * @param listener 動画撮影開始の結果を通知するリスナー
     */
    public void startMovieRec(final OnSonyCameraListener listener) {
        SonyCameraUtil.SonyCameraMode mode = SonyCameraUtil.SonyCameraMode.getMode(mEventObserver.getShootMode());

        if (mode == SonyCameraUtil.SonyCameraMode.Movie) {
            startMovieRecInternal(listener);
        } else {
            setShootMode(SonyCameraUtil.SonyCameraMode.Movie, new OnSonyCameraListener() {
                @Override
                public void onSuccess() {
                    startMovieRecInternal(listener);
                }

                @Override
                public void onError() {
                    listener.onError();
                }
            });
        }
    }

    /**
     * 接続されているSonyCameraに動画撮影停止を要求します.
     *
     * @param listener 動画撮影停止の結果を通知するリスナー
     */
    public void stopMovieRec(final OnSonyCameraListener listener) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject replyJson = mRemoteApi.stopMovieRec();
                    if (isErrorReply(replyJson)) {
                        listener.onError();
                    } else {
                        JSONArray resultsObj = replyJson.getJSONArray("result");
                        String thumbnailUrl = resultsObj.getString(0);
                        if (thumbnailUrl != null) {
                            listener.onSuccess();
                        } else {
                            listener.onError();
                        }
                    }
                } catch (Exception e) {
                    listener.onError();
                }
            }
        });
    }

    /**
     * 接続されているSonyCameraにズームを要求します.
     * @param direction ズームイン・ズームアウト
     * @param movement ズームする単位
     * @param listener ズーム結果を通知するリスナー
     */
    public void setZoom(final String direction, final String movement, final OnSonyCameraListener listener) {
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
                        listener.onError();
                    } else {
                        JSONArray resultsObj = replyJson.getJSONArray("result");
                        if (resultsObj != null) {
                            listener.onSuccess();
                        } else {
                            listener.onError();
                        }
                    }
                } catch (Exception e) {
                    listener.onError();
                }
            }
        });
    }

    /**
     * 接続されているSonyCameraにズームの値取得を要求します.
     * @param listener ズームの値取得の結果を通知するリスナー
     */
    public void getZoom(final OnZoomListener listener) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                double zoomDiameterParam;
                try {
                    JSONObject replyJson = mRemoteApi.getEvent(true);
                    if (isErrorReply(replyJson)) {
                        listener.onError();
                    } else {
                        JSONArray resultsObj = replyJson.getJSONArray("result");
                        replyJson = resultsObj.getJSONObject(2);
                        zoomDiameterParam = Double.valueOf(replyJson.getString("zoomPosition"))
                                / (Double) VAL_TO_PERCENTAGE;
                        DecimalFormat decimalFormat = new DecimalFormat("0.0#");
                        zoomDiameterParam = Double.valueOf(decimalFormat.format(zoomDiameterParam));

                        listener.onZoom(zoomDiameterParam);

                    }
                } catch (Exception e) {
                    listener.onError();
                }
            }
        });
    }

    /**
     * 接続されているSonyCameraに写真撮影を要求します.
     *
     * @param listener 写真撮影の結果を通知するリスナー
     */
    private void takePictureInternal(final OnTakePictureListener listener) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject replyJson = mRemoteApi.actTakePicture();
                    if (isErrorReply(replyJson)) {
                        listener.onError();
                    } else {
                        JSONArray resultsObj = replyJson.getJSONArray("result");
                        JSONArray imageUrlsObj = resultsObj.getJSONArray(0);
                        String postImageUrl = null;
                        if (1 <= imageUrlsObj.length()) {
                            postImageUrl = imageUrlsObj.getString(0);
                        }
                        if (postImageUrl == null) {
                            listener.onError();
                        } else {
                            listener.onSuccess(postImageUrl);
                        }
                    }
                } catch (Exception e) {
                    listener.onError();
                }
            }
        });
    }

    /**
     * 接続されているSonyCameraに動画撮影開始を要求します.
     *
     * @param listener 動画撮影開始の結果を通知するリスナー
     */
    private void startMovieRecInternal(final OnSonyCameraListener listener) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject replyJson = mRemoteApi.startMovieRec();
                    if (isErrorReply(replyJson)) {
                        listener.onError();
                    } else {
                        JSONArray resultsObj = replyJson.getJSONArray("result");
                        int resultCode = resultsObj.getInt(0);
                        if (resultCode == 0) {
                            listener.onSuccess();
                        } else {
                            listener.onError();
                        }
                    }
                } catch (Exception e) {
                    listener.onError();
                }
            }
        });
    }

    /**
     * SonyCameraの撮影モードを切り替えます.
     *
     * @param mode モード
     */
    private void setShootMode(final SonyCameraUtil.SonyCameraMode mode, final OnSonyCameraListener listener) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject replyJson = mRemoteApi.setShootMode(mode.getValue());
                    if (isErrorReply(replyJson)) {
                        listener.onError();
                    } else {
                        JSONArray resultsObj = replyJson.getJSONArray("result");
                        int resultCode = resultsObj.getInt(0);
                        if (resultCode == 0) {
                            listener.onSuccess();
                        } else {
                            listener.onError();
                        }
                    }
                } catch (Exception e) {
                    listener.onError();
                }
            }
        });
    }

    /**
     * WifiManagerを取得する.
     *
     * @return WifiManagerのインスタンス
     */
    private WifiManager getWifiManager() {
        return (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    /**
     * 全てのSonyCameraのサービスの状態をWiFiのssidに合わせて変更します.
     */
    private void setOnlineStatus() {
        String ssid = getSSID();
        for (SonyCameraService service : mSonyCameraServices) {
            service.setOnline(service.getId().equals(ssid));
        }
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
        startEventObserver();
    }

    /**
     * SonyCameraデバイスSDKを破棄する.
     */
    private void deleteSonyCameraSDK() {
        setOnlineStatus();

        stopPreview();
        stopEventObserver();

        if (mRemoteApi != null) {
            mRemoteApi = null;
        }
        mRetryCount = 0;

        mSSID = null;
    }

    /**
     * SonyCameraデバイスからのイベントを待つスレッドを作成します.
     */
    private void startEventObserver() {
        if (mEventObserver == null || !mEventObserver.isStarted()) {
            mEventObserver = new SimpleCameraEventObserver(mContext, mRemoteApi);
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
                    if (mOnSonyCamera2Listener != null) {
                        mOnSonyCamera2Listener.onTakePicture(postImageUrl);
                    }
                }
            });
            mEventObserver.start();
        }
    }

    /**
     * SonyCameraデバイスからのイベントを待つスレッドを破棄します.
     */
    private void stopEventObserver() {
        if (mEventObserver != null) {
            mEventObserver.stop();
            mEventObserver = null;
        }
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
        } catch (Exception e) {
            // do nothing
        }
        return result;
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
            int width;
            int height;
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
                return s;
            }
        }
        return null;
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
     * Sonyカメラに対応するサービスを取得します.
     * @return SonyCameraサービス
     */
    private SonyCameraService foundSonyCamera() {
        mSSID = getSSID();
        if (mSSID == null) {
            return null;
        }

        for (SonyCameraService service : mSonyCameraServices) {
            if (service.getId().equals(mSSID)) {
                return service;
            }
        }

        // リストにない場合には、新規のデバイスなので登録
        SonyCameraService service = new SonyCameraService(mSSID);
        service.setName(mSSID);

        mSonyCameraServices.add(service);
        mDBHelper.addSonyCameraService(service);

        mOnSonyCamera2Listener.onAdded(service);

        return service;
    }

    /**
     * 接続中のWiFiのSSIDを取得します.
     * @return SSID
     */
    private String getSSID() {
        WifiManager wifiMgr = getWifiManager();
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        return SonyCameraUtil.ssid(wifiInfo.getSSID());
    }

    /**
     * Sonyカメラからの撮影結果を通知するためのリスナー.
     */
    public interface OnSonyCamera2Listener {
        void onTakePicture(final String postImageUrl);
        void onAdded(final SonyCameraService service);
        void onError();
    }

    /**
     * Sonyカメラからの撮影結果を通知するためのリスナー.
     */
    public interface OnTakePictureListener {
        void onSuccess(final String postImageUrl);

        void onError();
    }

    /**
     * Sonyカメラからの操作結果を通知するためのリスナー.
     */
    public interface OnSonyCameraListener {
        void onSuccess();

        void onError();
    }

    /**
     * Sonyカメラの状態を通知するためのリスナー.
     */
    public interface OnCameraStateListener {
        void onState(String state, int[] size);

        void onError();
    }

    /**
     * Sonyカメラのズームを通知するためのリスナー.
     */
    public interface OnZoomListener {
        void onZoom(double zoom);

        void onError();
    }
}
