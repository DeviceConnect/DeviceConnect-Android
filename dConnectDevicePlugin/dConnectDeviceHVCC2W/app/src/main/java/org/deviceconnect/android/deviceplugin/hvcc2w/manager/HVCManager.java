package org.deviceconnect.android.deviceplugin.hvcc2w.manager;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;

import org.deviceconnect.android.deviceplugin.hvcc2w.BuildConfig;
import org.deviceconnect.android.deviceplugin.hvcc2w.R;
import org.deviceconnect.android.deviceplugin.hvcc2w.manager.data.FaceRecognitionDataModel;
import org.deviceconnect.android.deviceplugin.hvcc2w.manager.data.FaceRecognitionObject;
import org.deviceconnect.android.deviceplugin.hvcc2w.manager.data.HVCCameraInfo;
import org.deviceconnect.android.deviceplugin.hvcc2w.manager.data.HumanDetectKind;
import org.deviceconnect.android.deviceplugin.hvcc2w.manager.data.UserDataModel;
import org.deviceconnect.android.deviceplugin.hvcc2w.manager.data.UserDataObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.HttpsURLConnection;

import jp.co.omron.hvcw.ErrorCodes;
import jp.co.omron.hvcw.FileInfo;
import jp.co.omron.hvcw.HvcwApi;
import jp.co.omron.hvcw.Int;
import jp.co.omron.hvcw.OkaoResult;
import jp.co.omron.hvcw.ResultAge;
import jp.co.omron.hvcw.ResultDetection;
import jp.co.omron.hvcw.ResultDirection;
import jp.co.omron.hvcw.ResultFace;
import jp.co.omron.hvcw.ResultGender;
import jp.co.omron.hvcw.ResultRecognition;

/**
 * HVC Manager.
 * @author NTT DOCOMO, INC.
 */

public enum HVCManager {
    /**
     * Singleton instance.
     */
    INSTANCE;



    /** TAG. */
    private static final String TAG = "HVCManager";
    /** Based request URL of the Web API. */
    private static final String HVC_SERVICE_URL = "https://developer.hvc.omron.com/c2w";

    /** User Signup URL.*/
    private static final String HVC_SIGN_UP_URL = HVC_SERVICE_URL + "/api/v1/signup.php";

    /** User Login URL. */
    private static final String HVC_LOGIN_URL = HVC_SERVICE_URL + "/api/v1/login.php";

    /** User Logout URL. */
    private static final String HVC_LOGOUT_URL = HVC_SERVICE_URL + "/api/v1/logout.php";

    /** Get CameraList URL.*/
    private static final String HVC_GET_CAMERA_URL = HVC_SERVICE_URL + "/api/v1/getCameraList.php";

    /** Network Setting file name. */
    private static final String HVC_NETWORK_SETTING = "/network_setting.pcm";

    /** Option parameter:{@value}. */
    public static final String PARAM_OPTIONS_EYE = "eye";
    /** Option parameter:{@value}. */
    public static final String PARAM_OPTIONS_NOSE = "nose";
    /** Option parameter:{@value}. */
    public static final String PARAM_OPTIONS_MOUTH = "mouth";
    /** Option parameter:{@value}. */
    public static final String PARAM_OPTIONS_BLINK = "blink";
    /** Option parameter:{@value}. */
    public static final String PARAM_OPTIONS_AGE = "age";
    /** Option parameter:{@value}. */
    public static final String PARAM_OPTIONS_GENDER = "gender";
    /** Option parameter:{@value}. */
    public static final String PARAM_OPTIONS_FACE_DIRECTION = "faceDirection";
    /** Option parameter:{@value}. */
    public static final String PARAM_OPTIONS_GAZE = "gaze";
    /** Option parameter:{@value}. */
    public static final String PARAM_OPTIONS_EXPRESSION = "expression";



    /** HVC-C2W body detect threshold initial value. */
    private static final int HVC_C2W_BODY_DETECT_THRESHOLD = 500; //1〜1000
    /** HVC-C2W hand detect threshold initial value. */
    private static final int HVC_C2W_HAND_DETECT_THRESHOLD = 500; //1〜1000
    /** HVC-C2W Pet detect threshold initial value. */
    private static final int HVC_C2W_PET_DETECT_THRESHOLD = 500; //1〜1000
    /** HVC-C2W Face detect threshold initial value. */
    private static final int HVC_C2W_FACE_DETECT_THRESHOLD = 500; //1〜1000
    /** HVC-C2W Face Recognition threshold initial value. */
    private static final int HVC_C2W_RECOGNITION_THRESHOLD = 500; //0〜1000
    /** Threshold min. body, hand, pet, face. */
    public static final int HVC_C2W_MIN_THRESHOLD = 1;
    /** Threshold min. recognition.*/
    public static final int HVC_C2W_MIN_RECOGNITION_THRESHOLD = 0;
    /** Threshold max.*/
    public static final int HVC_C2W_MAX_THRESHOLD = 1000;

    /** Confidence max.*/
    public static final double HVC_C2W_MAX_CONFIDENCE = 1000.0;
    /** Blink max.*/
    public static final int HVC_C2W_MAX_BLINK = 1000;


    /**
     * Neutral<br>
     * Expression Estimation result value<br>
     */
    public static final int HVC_EX_NEUTRAL = 0;
    /**
     * Happiness<br>
     * Expression Estimation result value<br>
     */
    public static final int HVC_EX_HAPPINESS = 1;
    /**
     * Surprise<br>
     * Expression Estimation result value<br>
     */
    public static final int HVC_EX_SURPRISE = 2;
    /**
     * Anger<br>
     * Expression Estimation result value<br>
     */
    public static final int HVC_EX_ANGER = 3;
    /**
     * Sadness<br>
     * Expression Estimation result value<br>
     */
    public static final int HVC_EX_SADNESS = 4;
    /**
     * HVC-C expression unknown.
     */
    public static final String EXPRESSION_UNKNOWN = "unknown";

    /**
     * HVC-C expression smile.
     */
    public static final String EXPRESSION_SMILE = "smile";

    /**
     * HVC-C expression surprise.
     */
    public static final String EXPRESSION_SURPRISE = "surprise";

    /**
     * HVC-C expression mad.
     */
    public static final String EXPRESSION_MAD = "mad";

    /**
     * HVC-C expression sad.
     */
    public static final String EXPRESSION_SAD = "sad";
    /**
     * HVC-C expression score max.
     */
    public static final int EXPRESSION_SCORE_MAX = 100;
    /**
     * HVC-C2W detect camera width[pixels].
     */
    public static final int HVC_C2W_CAMERA_WIDTH = 1920;

    /**
     * HVC-C2W detect camera height[pixels].
     */
    public static final int HVC_C2W_CAMERA_HEIGHT = 1080;
    /** HVC-C2W body min size. */
    private static final int HVC_C2W_BODY_MIN_SIZE = 30; //20〜8192
    /** HVC-C2W body max size. */
    private static final int HVC_C2W_BODY_MAX_SIZE = 8192;
    /** HVC-C2W hand min size. */
    private static final int HVC_C2W_HAND_MIN_SIZE = 40;
    /** HVC-C2W hand max size. */
    private static final int HVC_C2W_HAND_MAX_SIZE = 8192;
    /** HVC-C2W pet min size. */
    private static final int HVC_C2W_PET_MIN_SIZE = 40;
    /** HVC-C2W pet max size. */
    private static final int HVC_C2W_PET_MAX_SIZE = 8192;
    /** HVC-C2W face min size. */
    private static final int HVC_C2W_FACE_MIN_SIZE = 64;
    /** HVC-C2W face max size. */
    private static final int HVC_C2W_FACE_MAX_SIZE = 8192;
    /** HVC-C2W min size. */
    public static final int HVC_C2W_MIN_SIZE = 20;
    /** HVC-C2W max size. */
    public static final int HVC_C2W_MAX_SIZE = 8192;
    /**
     * HVC interval parameter minimum value.
     */
    public static final long PARAM_INTERVAL_MIN = 3 * 1000;

    /**
     * HVC interval parameter maximum value.
     */
    public static final long PARAM_INTERVAL_MAX = 60 * 1000;
    /**
     * Male<br>
     * Gender Estimation result value<br>
     */
    public static final int HVC_GEN_MALE = 1;
    /**
     * Female<br>
     * Gender Estimation result value<br>
     */
    public static final int HVC_GEN_FEMALE = 0;
    /**
     * API KEY.
     */
    private String mAPIKey;
    /**
     * APP ID.
     */
    private String mAppId;
    /**
     * HVC Cameras.
     */
    private ConcurrentHashMap<String, HVCCameraInfo> mServices;
    /** Event List. */
    private List<String> mEventList;
    /** HVC SDK Handle. */
    private HvcwApi mApi;
    /** Body Timer Handler. */
    private Handler mBodyTimer;

    /** Hand Timer Handler. */
    private Handler mHandTimer;
    /** Face Timer Handler. */
    private Handler mFaceTimer;
    /**
     * POST Request's or Manager's Listener.
     */
    public interface ResponseListener {
        /**
         * Request Receiver.
         * @param json response json
         */
        void onReceived(final String json);
    }


    /**
     * Load .so libraries.
     */
    static {
        System.loadLibrary("openh264");
        System.loadLibrary("ffmpeg");
        System.loadLibrary("ldpc");
        System.loadLibrary("IOTCAPIs");
        System.loadLibrary("RDTAPIs");
        System.loadLibrary("c2w");
        System.loadLibrary("HvcOi");
        System.loadLibrary("HVCW");
    }
    /**
     * Constructor.
     */
    private HVCManager() {
        mServices = new ConcurrentHashMap<String, HVCCameraInfo>();
        mEventList = new ArrayList<String>();
        mBodyTimer = new Handler();
        mHandTimer = new Handler();
        mFaceTimer = new Handler();
    }
    /**
     * Return HVC Cameras.
     * @return HVC Cameras List
     */
    public ConcurrentHashMap<String, HVCCameraInfo> getHVCDevices() {
        return mServices;
    }

    /**
     * Initialize.
     *
     * @param context Context。
     */
    public void init(final Context context) {
        mAPIKey = context.getString(R.string.api_key);
        mAppId = context.getString(R.string.app_id);
        if (mApi != null) {
            mApi.deleteHandle();
        }
        mApi = HvcwApi.createHandle();

    }

    /**
     * Add Body Detect Event Listener.
     * @param serviceId ServiceID
     * @param l Listener
     */
    public void addBodyDetectEventListener(final String serviceId,
                                           final HVCCameraInfo.OnBodyEventListener l) {
        if (!mEventList.contains(serviceId)) {
            mEventList.add(serviceId);
        }
        HVCCameraInfo camera = mServices.get(serviceId);
        camera.setBodyEvent(l);
    }

    /**
     * Add Hand Detect Event Listener.
     * @param serviceId Service ID
     * @param l Listener
     */
    public void addHandDetectEventListener(final String serviceId,
                                           final HVCCameraInfo.OnHandEventListener l) {
        if (!mEventList.contains(serviceId)) {
            mEventList.add(serviceId);
        }
        HVCCameraInfo camera = mServices.get(serviceId);
        camera.setHandEvent(l);
    }

    /**
     * Add Face Detect Event Listener.
     * @param serviceId ServiceID
     * @param l Listener
     * @param options Options
     */
    public void addFaceDetectEventListener(final String serviceId,
                                           final HVCCameraInfo.OnFaceEventListener l,
                                           final List<String> options) {
        if (!mEventList.contains(serviceId)) {
            mEventList.add(serviceId);
        }

        HVCCameraInfo camera = mServices.get(serviceId);
        camera.setOptions(options);
        camera.setFaceEvent(l);
    }

    /**
     * Add Face Recognize Event Listener.
     * @param serviceId ServiceID
     * @param l Listener
     * @param options Options
     */
    public void addFaceRecognizeEventListener(final String serviceId,
                                              final HVCCameraInfo.OnFaceRecognizeEventListener l,
                                              final List<String> options) {
        if (!mEventList.contains(serviceId)) {
            mEventList.add(serviceId);
        }
        HVCCameraInfo camera = mServices.get(serviceId);
        camera.setOptions(options);
        camera.setFaceRecognizeEvent(l);
    }

    /**
     * Check Remove Event list.
     * @param serviceId ServiceID
     * @param camera Camera Info
     *
     */
    private void removeEventList(final String serviceId, final HVCCameraInfo camera) {
        if (mEventList.contains(serviceId) && camera.getBodyEvent() == null
                && camera.getHandEvent() == null && camera.getFaceEvent() == null
                && camera.getFaceRecognizeEvent() == null) {
            mEventList.remove(camera.getID());
        }
    }

    /**
     * Start Face Detection Timer.
     * @param interval Timer's interval
     */
    private void startFaceTimer(final Long interval) {
        mFaceTimer.postDelayed(new Runnable() {
            @Override
            public void run() {
                for (String key : mServices.keySet()) {
                    HVCCameraInfo camera = mServices.get(key);
                    if (camera == null) {
                        return;
                    }
                    OkaoResult result = HVCManager.INSTANCE.execute();

                    if (camera.getFaceEvent() != null) {
                        camera.getFaceEvent().onNotifyForFaceDetectResult(key, result);
                    }
                }
                if (mEventList.size() > 0) {
                    mFaceTimer.postDelayed(this, interval);
                }
            }
        }, interval);
    }

    /**
     * Start Hand Detection Timer.
     * @param interval Timer's interval
     */
    private void startHandTimer(final Long interval) {
        mHandTimer.postDelayed(new Runnable() {
            @Override
            public void run() {
                for (String key : mServices.keySet()) {
                    HVCCameraInfo camera = mServices.get(key);
                    if (camera == null) {
                        return;
                    }
                    OkaoResult result = HVCManager.INSTANCE.execute();

                    if (camera.getHandEvent() != null) {
                        camera.getHandEvent().onNotifyForHandDetectResult(key, result);
                    }
                }
                if (mEventList.size() > 0) {
                    mHandTimer.postDelayed(this, interval);
                }
            }
        }, interval);
    }

    /**
     * Start Body Detection Timer.
     * @param interval Timer's interval
     */
    private void startBodyTimer(final Long interval) {
        mBodyTimer.postDelayed(new Runnable() {
            @Override
            public void run() {
                for (String key : mServices.keySet()) {
                    HVCCameraInfo camera = mServices.get(key);
                    if (camera == null) {
                        return;
                    }
                    OkaoResult result = HVCManager.INSTANCE.execute();

                    if (camera.getBodyEvent() != null) {
                        camera.getBodyEvent().onNotifyForBodyDetectResult(key, result);
                    }

                }
                if (mEventList.size() > 0) {
                    mBodyTimer.postDelayed(this, interval);
                }
            }
        }, interval);
    }

    /**
     * Remove Body Detect Event Listener.
     * @param serviceId serviceId
     */
    public void removeBodyDetectEventListener(final String serviceId) {
        HVCCameraInfo camera = mServices.get(serviceId);
        if (camera != null) {
            camera.setBodyEvent(null);
            removeEventList(serviceId, camera);
        }
    }

    /**
     * Remove Hand Detect Event Listener
     * @param serviceId Service ID
     */
    public void removeHandDetectEventListener(final String serviceId) {
        HVCCameraInfo camera = mServices.get(serviceId);
        if (camera != null) {
            camera.setHandEvent(null);
            removeEventList(serviceId, camera);
        }
    }

    /**
     * Remove Face Detect Event Listener.
     * @param serviceId Service ID
     */
    public void removeFaceDetectEventListener(final String serviceId) {
        HVCCameraInfo camera = mServices.get(serviceId);
        if (camera != null) {
            camera.setFaceEvent(null);
            removeEventList(serviceId, camera);
        }
    }

    /**
     * Remove Face Recognize Event Listener.
     * @param serviceId ServiceID
     */
    public void removeFaceRecognizeEventListener(final String serviceId) {
        HVCCameraInfo camera = mServices.get(serviceId);
        if (camera != null) {
            camera.setFaceRecognizeEvent(null);
            removeEventList(serviceId, camera);
        }
    }

    /**
     * Remove all event listener.
     */
    public void removeAllEventListener() {
        for (String key : mServices.keySet()) {
            HVCCameraInfo camera = mServices.get(key);
            if (camera != null) {
                camera.setBodyEvent(null);
                camera.setFaceEvent(null);
                camera.setFaceRecognizeEvent(null);
                camera.setHandEvent(null);
                mEventList.remove(camera.getID());
            }
        }
    }

    /**
     * Start Event Timer.
     * @param kind HumanDetect kind
     * @param interval Interval
     */
    public void startEventTimer(final HumanDetectKind kind, final Long interval) {
        switch (kind) {
            case BODY:
                startBodyTimer(interval);
                break;
            case HAND:
                startHandTimer(interval);
                break;
            case FACE:
                startFaceTimer(interval);
                break;
            case HUMAN:
                startBodyTimer(interval);
                startHandTimer(interval);
                startFaceTimer(interval);
                break;
            default:

        }

    }




    /**
     * Stop Event Timer.
     */
    public void stopEventTimer(final HumanDetectKind kind) {
        switch(kind) {
            case BODY:
                mBodyTimer.removeCallbacksAndMessages(null);
                break;
            case HAND:
                mHandTimer.removeCallbacksAndMessages(null);
                break;
            case FACE:
                mFaceTimer.removeCallbacksAndMessages(null);
                break;
            case HUMAN:
                mBodyTimer.removeCallbacksAndMessages(null);
                mHandTimer.removeCallbacksAndMessages(null);
                mFaceTimer.removeCallbacksAndMessages(null);
            default:
        }
    }


    /**
     * Sign Up HVC Service.
     * @param mailAddress user's mail address
     * @param l manager's response listener
     */
    public void signup(final String mailAddress, final ResponseListener l) {
        String apiKey = "apiKey=" + mAPIKey;
        String email = "email=" + mailAddress;
        String params = apiKey + "&" + email;
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "url:" + HVC_SIGN_UP_URL);
            Log.d(TAG, "params:" + params);
        }
        PostMessageTask task = new PostMessageTask(new ResponseListener() {
            public void onReceived(String json) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "result:" + json);
                }
                if (l != null) {
                    l.onReceived(json);
                }
            }
        });

        task.execute(HVC_SIGN_UP_URL, params);
    }

    /**
     * Login HVC Server.
     * @param context Context
     * @param mailAddress Mail Address
     * @param password Password
     * @param l Manager's response Listener
     */
    public void login(final Context context, final String mailAddress, final String password, final ResponseListener l) {
        String apiKey = "apiKey=" + mAPIKey;
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String deviceId = "deviceId=" + wifiInfo.getMacAddress();
        String osType = "osType=" + "1";
        String email = "email=" + mailAddress;
        String pass = "password=" + password;
        String params = apiKey + "&" + deviceId + "&" + osType + "&" + "&" + email + "&" + pass;
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "url:" + HVC_LOGIN_URL);
            Log.d(TAG, "params:" + params);
        }
        PostMessageTask task = new PostMessageTask(new ResponseListener() {
            public void onReceived(String json) {
                if (l != null) {
                    l.onReceived(json);
                }
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "json:" + json);
                }
                if (json != null) {
                    try {
                        JSONObject root = new JSONObject(json);
                        JSONObject result = root.getJSONObject("result");
                        String code = result.getString("code");
                        String msg = result.getString("msg");
                        String accessToken = "";
                        int expiresIn = -1;
                        if (msg.equals("success")) {
                            JSONObject access = root.getJSONObject("access");
                            accessToken = access.getString("token");
                            expiresIn = access.getInt("expiresIn");
                        }
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, String.format("response=%s(%s)", code, msg));
                            Log.d(TAG, String.format("token=\"%s\"", accessToken));
                            Log.d(TAG, String.format("expiresIn=%d", expiresIn));
                        }
                        UserDataObject object = new UserDataModel(mailAddress, password, accessToken);
                        HVCStorage.INSTANCE.registerUserData(object);
                    } catch (JSONException e) {
                        if (BuildConfig.DEBUG) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        task.execute(HVC_LOGIN_URL, params);
    }

    /**
     * Logout HVC Server.
     * @param l Manager's response Listener
     */
    public void logout(final ResponseListener l) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "url:" + HVC_LOGOUT_URL);
        }
        final List<UserDataObject> lists = HVCStorage.INSTANCE.getUserDatas(null);
        if (lists.size() == 0) {
            if (l != null ){
                l.onReceived(null);
            }
            return;
        }
        PostMessageTask task = new PostMessageTask(new ResponseListener() {
            public void onReceived(String json) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "json:" + json);
                }
                HVCStorage.INSTANCE.removeUserData(lists.get(0).getEmail());
                mServices.clear();
                mEventList.clear();
                if (l != null){
                    l.onReceived(json);
                }

            }
        });

        task.execute(HVC_LOGOUT_URL, "", lists.get(0).getAccessToken());
    }


    /**
     * Play Connected Sound.
     * @param context Context
     * @param ssid SSID
     * @param password SSID's Password
     */
    public void playConnectSound(final Context context, final String ssid, final String password) {
        final String fileName = context.getApplicationContext().getFilesDir() + "/network_setting.pcm";

        List<UserDataObject> lists = HVCStorage.INSTANCE.getUserDatas(null);
        if (lists.size() == 0) {
            return;
        }
        int ret = mApi.generateDataSoundFile(fileName, ssid, password, lists.get(0).getAccessToken());
        if (ret == ErrorCodes.HVCW_SUCCESS) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "play:" + fileName);
                    }
                    File networkSettingFile = new File(fileName);
                    if (networkSettingFile == null) {
                        return;
                    }

                    byte[] byteData = new byte[(int) networkSettingFile.length()];
                    FileInputStream fis;
                    try {
                        fis = new FileInputStream(networkSettingFile);
                        fis.read(byteData);
                        fis.close();
                    } catch (FileNotFoundException e) {
                        if (BuildConfig.DEBUG) {
                            e.printStackTrace();
                        }
                        return;
                    } catch (IOException e) {
                        if (BuildConfig.DEBUG) {
                            e.printStackTrace();
                        }
                        return;
                    }

                    int audioBuffSize = AudioTrack.getMinBufferSize(
                            8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

                    AudioTrack audio = new AudioTrack(AudioManager.STREAM_MUSIC,
                            8000,
                            AudioFormat.CHANNEL_OUT_MONO,
                            AudioFormat.ENCODING_PCM_16BIT,
                            audioBuffSize,
                            AudioTrack.MODE_STREAM);
                    audio.play();
                    audio.write(byteData, 0, byteData.length);
                }
            }).start();
        }
    }

    /**
     * Get Camera List.
     * @param l Manager's response listener
     */
    public void getCameraList(final ResponseListener l) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "url:" + HVC_GET_CAMERA_URL);
        }
        List<UserDataObject> lists = HVCStorage.INSTANCE.getUserDatas(null);
        if (lists.size() == 0) {
            if (l != null) {
                l.onReceived(null);
            }
            return;
        }
        PostMessageTask task = new PostMessageTask(new ResponseListener() {
            public void onReceived(String json) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "json:" + json);
                }
                if (json != null) {

                    try {
                        JSONObject root = new JSONObject(json);
                        JSONObject result = root.getJSONObject("result");
                        String code = result.getString("code");
                        String msg = result.getString("msg");
                        if (msg.equals("success")) {
                            mServices.clear();
                            JSONArray array = root.getJSONArray("cameraList");
                            for (int i = 0; i < array.length(); ++i) {
                                JSONObject obj =array.getJSONObject(i);
                                String id = obj.getString("cameraId");
                                String name = obj.getString("cameraName");
                                String macAddress = obj.getString("cameraMacAddr");
                                String appId = obj.getString("appId");
                                int ownerType = obj.getInt("ownerType");
                                String ownerEmail = obj.getString("ownerEmail");
                                HVCCameraInfo ci = new HVCCameraInfo(id, name, macAddress, appId, ownerType, ownerEmail);

                                mServices.put(id, ci);
                                if (BuildConfig.DEBUG) {
                                    Log.d(TAG, String.format("camera[%d] name=\"%s\",id=\"%s\"", i, name, id));
                                }
                            }
                        }
                    } catch (JSONException e) {
                        if (BuildConfig.DEBUG) {
                            e.printStackTrace();
                        }
                    }
                }
                if (l != null) {
                    l.onReceived(json);
                }
            }
        });

        task.execute(HVC_GET_CAMERA_URL, "", lists.get(0).getAccessToken());
    }

    /**
     * Set HVC Camera.
     * @param cameraId CameraID(ServiceId)
     * @param l manager's response listener
     */
    public void setCamera(final String cameraId, final ResponseListener l) {
        final List<UserDataObject> lists = HVCStorage.INSTANCE.getUserDatas(null);
        if (lists.size() == 0) {
            if (l != null) {
                l.onReceived(null);
            }
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "connect:" + cameraId);
                }
                int ret = mApi.connect(cameraId, lists.get(0).getAccessToken());
                Int returnStatus = new Int();
                if (ret == ErrorCodes.HVCW_SUCCESS) {
                    int appId = 100;
                    try {
                        appId = Integer.parseInt(mAppId);
                    } catch (NumberFormatException e) {
                        if (BuildConfig.DEBUG) {
                            e.printStackTrace();
                        }
                    }
                    ret = mApi.setAppID(appId, returnStatus);
                }

                final String msg = String.format("errorCode=%d,returnStatus=%#x", ret, returnStatus.getIntValue());
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, msg);
                }
                if (l != null) {
                    l.onReceived("{\"result\":" + ret + "}");
                }
            }
        }).start();
    }


    /**
     * Register Album.
     * @param name Face Recognition name
     * @param userId Face Recognition UserId
     * @param dataId Face Recognition DataId
     * @param l Manager's Response Listener
     */
    public void registerAlbum(final String name, final String serviceId, final int userId, final int dataId, final ResponseListener l) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ResultDetection rd = new ResultDetection();
                FileInfo fi = new FileInfo();
                Int returnStatus = new Int();

                int ret = mApi.albumRegister(userId, dataId, rd, fi, returnStatus);

                StringBuilder sb = new StringBuilder();
                if (ret == ErrorCodes.HVCW_SUCCESS) {
                    sb.append(String.format("errorCode=%d,returnStatus=%#x\n", ret, returnStatus.getIntValue()));

                    // 顔検出結果
                    sb.append(String.format("register album x=%d,y=%d,size=%d,confidence=%d",
                            rd.getCenter().getX(),
                            rd.getCenter().getY(),
                            rd.getSize(),
                            rd.getConfidence()));
                    FaceRecognitionObject faceRecognition = new FaceRecognitionDataModel(name, serviceId, userId, dataId);
                    HVCStorage.INSTANCE.registerFaceRecognitionData(faceRecognition);
                } else {
                    sb.append(String.format("errorCode=%d,returnStatus=%#x", ret, returnStatus.getIntValue()));
                }

                final String msg = sb.toString();
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, msg);
                }
                if (l != null) {
                    l.onReceived("{\"result\":" + ret + "}");
                }
            }
        }).start();
    }


    /**
     * Delete Album.
     * @param name name
     * @param l Manager's response listener
     */
    public void deleteAlbum(final String name, final ResponseListener l) {
        final List<FaceRecognitionObject> lists = HVCStorage.INSTANCE.getFaceRecognitionDatas(name);
        if (lists.size() == 0) {
            if (l != null) {
                l.onReceived(null);
            }
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                Int returnStatus = new Int();
                // アルバムデータ削除
                int ret = mApi.albumDeleteData(lists.get(0).getUserId(),
                                                lists.get(0).getDataId(), returnStatus);

                final String msg = String.format("errorCode=%d,returnStatus=%#x", ret, returnStatus.getIntValue());
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "result:" + msg);
                }
                if (l != null) {
                    l.onReceived("{\"result\":" + ret + "}");
                }
            }
        }).start();
    }

    /**
     * Face Detect & Face Recognize Execute.
     * @return Okao Result
     */
    public OkaoResult execute() {
        int useFunction[] = {1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1};
        OkaoResult result = new OkaoResult();
        Int returnStatus = new Int();
        // 実行
        int ret = mApi.okaoExecute(useFunction, result, returnStatus);

        StringBuilder sb = new StringBuilder();
        if (ret == ErrorCodes.HVCW_SUCCESS) {
            sb.append(String.format("errorCode=%d,returnStatus=%#x\n", ret, returnStatus.getIntValue()));
            // 検出数
            int count = result.getResultFaces().getCount();
            ResultFace[] rf = result.getResultFaces().getResultFace();
            sb.append(String.format("faceCount=%d", count));
            for (int i = 0; i < count; ++i) {

                sb.append(String.format("\nface[%d] x=%d,y=%d,size=%d,confidence=%d", i,
                        rf[i].getCenter().getX(),
                        rf[i].getCenter().getY(),
                        rf[i].getSize(),
                        rf[i].getConfidence()));
                ResultDirection rd = rf[i].getDirection();
                sb.append(String.format("\nface[%d] leftRight=%d,upDown=%d,roll=%d", i,
                        rd.getLR(),
                        rd.getUD(),
                        rd.getRoll()));
                ResultAge ra = rf[i].getAge();
                sb.append(String.format("\nface[%d] age=%d,confidence=%d", i,
                        ra.getAge(),
                        ra.getConfidence()));
                ResultGender rg = rf[i].getGender();
                sb.append(String.format("\nface[%d] gender=%d,confidence=%d", i,
                        rg.getGender(),
                        rg.getConfidence()));
                ResultRecognition rr = rf[i].getRecognition();

                String recg;
                switch (rr.getUID()) {
                    case -128:
                        recg = "not recognized";
                        break;
                    case -127:
                        recg = "album is not registered";
                        break;
                    case -1:
                        recg = String.format("match=×,score=%d", rr.getScore());
                        break;
                    default:
                        recg = String.format("match=○,score=%d", rr.getScore());
                }
                sb.append(String.format("\nface[%d] %s", i, recg));
            }
        } else {
            sb.append(String.format("errorCode=%d,returnStatus=%#x", ret, returnStatus.getIntValue()));
        }

        final String msg = sb.toString();
        if (BuildConfig.DEBUG) {
            Log.d(TAG, msg);
        }
        return result;
    }

    /**
     * Set Okao Execute's Threshold.
     * @param body body's threshold
     * @param hand hand's threshold
     * @param pet pet's threshold
     * @param face face's threshold
     * @param recognition recognition's threshold
     * @return set result
     */
    public int setThreshold(final Double body, final Double hand, final Double pet, final Double face,
                                final Double recognition) {
        int b = HVC_C2W_BODY_DETECT_THRESHOLD;
        int h = HVC_C2W_HAND_DETECT_THRESHOLD;
        int p = HVC_C2W_PET_DETECT_THRESHOLD;
        int f = HVC_C2W_FACE_DETECT_THRESHOLD;
        int r = HVC_C2W_RECOGNITION_THRESHOLD;
        if (body != null) {
            b = body.intValue() * 1000;
        }
        if (hand != null) {
            h = hand.intValue() * 1000;
        }
        if (pet != null) {
            p = pet.intValue() * 1000;
        }
        if (face != null) {
            f = face.intValue() * 1000;
        }
        if (recognition != null) {
            r = recognition.intValue() * 1000;
        }
        Int ret = new Int();
        mApi.setThreshold(b, h, p, f, r, ret);

        return ret.getIntValue();
    }

    /**
     * Set Okao Execute's Min Max Size.
     * @param bodyMin body's min size
     * @param bodyMax body's max size
     * @param handMin hand's min size
     * @param handMax hand's max size
     * @param petMin pet's min size
     * @param petMax pet's max size
     * @param faceMin face's min size
     * @param faceMax face's max size
     * @return set result
     */
    public int setMinMaxSize(final Double bodyMin, final Double bodyMax,
                             final Double handMin, final Double handMax,
                             final Double petMin, final Double petMax,
                             final Double faceMin, final Double faceMax) {
        int bMin = HVC_C2W_BODY_MIN_SIZE;
        int bMax = HVC_C2W_BODY_MAX_SIZE;
        int hMin = HVC_C2W_HAND_MIN_SIZE;
        int hMax = HVC_C2W_HAND_MAX_SIZE;
        int pMin = HVC_C2W_PET_MIN_SIZE;
        int pMax = HVC_C2W_PET_MAX_SIZE;
        int fMin = HVC_C2W_FACE_MIN_SIZE;
        int fMax = HVC_C2W_FACE_MAX_SIZE;
        if (bodyMin != null) {
            bMin = bodyMin.intValue() * 8192;
        }
        if (bodyMax != null) {
            bMax = bodyMax.intValue() * 8192;
        }
        if (handMin != null) {
            hMin = handMin.intValue() * 8192;
        }
        if (handMax != null) {
            hMax = handMax.intValue() * 8192;
        }
        if (petMin != null) {
            pMin = petMin.intValue() * 8192;
        }
        if (petMax != null) {
            pMax = petMax.intValue() * 8192;
        }
        if (faceMin != null) {
            fMin = faceMin.intValue() * 8192;
        }
        if (faceMax != null) {
            fMax = faceMax.intValue() * 8192;
        }
        Int ret = new Int();
        mApi.setSizeRange(bMin, bMax, hMin, hMax, pMin, pMax, fMin, fMax, ret);

        return ret.getIntValue();
    }

    /**
     * convert to normalize expression value.
     * @param hvcExpression expression(HVC value)
     * @return normalize expression value.
     */
    public static String convertToNormalizeExpression(final int hvcExpression) {

        String normalizeExpression = EXPRESSION_UNKNOWN;

        SparseArray<String> map = new SparseArray<>();
        map.put(HVC_EX_NEUTRAL, EXPRESSION_UNKNOWN);
        map.put(HVC_EX_HAPPINESS, EXPRESSION_SMILE);
        map.put(HVC_EX_SURPRISE, EXPRESSION_SURPRISE);
        map.put(HVC_EX_ANGER, EXPRESSION_MAD);
        map.put(HVC_EX_SADNESS, EXPRESSION_SAD);

        String exp = map.get(hvcExpression);
        if (exp != null) {
            normalizeExpression = exp;
        } else {
            normalizeExpression = EXPRESSION_UNKNOWN;
        }

        return normalizeExpression;
    }
    /**
     * Post Request's Listener Class.
     */
    public class PostMessageTask extends AsyncTask<String, Void, String> {

        /** Post Request's Listener. */
        private ResponseListener listener = null;

        /**
         * Constructor.
         * @param listener Post Request's Listener
         */
        public PostMessageTask(ResponseListener listener) {
            this.listener = listener;
        }

        @Override
        protected String doInBackground(String... params) {
            HttpsURLConnection conn = null;
            String json = null;
            try {
                URL url;
                if (params[1].isEmpty()) {
                    url = new URL(params[0]);
                } else {
                    url = new URL(params[0] + "?" + params[1]);
                }
                conn = (HttpsURLConnection)url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                if (params.length == 3) {
                    // アクセストークンが必要なリクエストの場合
                    conn.setRequestProperty("Authorization", "Bearer " + params[2]);
                }
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // POST
                conn.connect();

                // レスポンス受信
                if (conn.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                    StringBuilder sb = new StringBuilder();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    json = sb.toString();
                }
            } catch(MalformedURLException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            } catch(IOException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            } finally {
                if(conn != null) {
                    conn.disconnect();
                }
            }

            return json;
        }

        @Override
        protected void onPostExecute(String param) {
            listener.onReceived(param);
        }
    }
}
