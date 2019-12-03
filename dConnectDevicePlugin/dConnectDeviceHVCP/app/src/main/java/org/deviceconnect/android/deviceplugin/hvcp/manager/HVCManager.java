package org.deviceconnect.android.deviceplugin.hvcp.manager;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.NonNull;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import org.deviceconnect.android.deviceplugin.hvcp.BuildConfig;
import org.deviceconnect.android.deviceplugin.hvcp.manager.data.HVCCameraInfo;
import org.deviceconnect.android.deviceplugin.hvcp.manager.data.HumanDetectKind;
import org.deviceconnect.android.deviceplugin.hvcp.manager.data.OkaoResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


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
    /** Okao Execute Command. */
    private static final String OKAO_EXECUTE = "FE040300FF0100";

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



    /** HVC-P body detect threshold initial value. */
    private static final int HVC_P_BODY_DETECT_THRESHOLD = 500; //1〜1000
    /** HVC-P hand detect threshold initial value. */
    private static final int HVC_P_HAND_DETECT_THRESHOLD = 500; //1〜1000
    /** HVC-P Pet detect threshold initial value. */
    private static final int HVC_P_PET_DETECT_THRESHOLD = 500; //1〜1000
    /** HVC-P Face detect threshold initial value. */
    private static final int HVC_P_FACE_DETECT_THRESHOLD = 500; //1〜1000
    /** HVC-P Face Recognition threshold initial value. */
    private static final int HVC_P_RECOGNITION_THRESHOLD = 500; //0〜1000
    /** Threshold min. body, hand, pet, face. */
    public static final int HVC_P_MIN_THRESHOLD = 1;
    /** Threshold min. recognition.*/
    public static final int HVC_P_MIN_RECOGNITION_THRESHOLD = 0;
    /** Threshold max.*/
    public static final int HVC_P_MAX_THRESHOLD = 1000;

    /** Confidence max.*/
    public static final int HVC_P_MAX_CONFIDENCE = 1000;
    /** Blink max.*/
    public static final int HVC_P_MAX_BLINK = 1000;


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
     * HVC-P detect camera width[pixels].
     */
    public static final int HVC_P_CAMERA_WIDTH = 640;

    /**
     * HVC-P detect camera height[pixels].
     */
    public static final int HVC_P_CAMERA_HEIGHT = 480;
    /** HVC-P body min size. */
    private static final int HVC_P_BODY_MIN_SIZE = 30; //20〜8192
    /** HVC-P body max size. */
    private static final int HVC_P_BODY_MAX_SIZE = 8192;
    /** HVC-P hand min size. */
    private static final int HVC_P_HAND_MIN_SIZE = 40;
    /** HVC-P hand max size. */
    private static final int HVC_P_HAND_MAX_SIZE = 8192;
    /** HVC-P pet min size. */
    private static final int HVC_P_PET_MIN_SIZE = 40;
    /** HVC-P pet max size. */
    private static final int HVC_P_PET_MAX_SIZE = 8192;
    /** HVC-P face min size. */
    private static final int HVC_P_FACE_MIN_SIZE = 64;
    /** HVC-P face max size. */
    private static final int HVC_P_FACE_MAX_SIZE = 8192;
    /** HVC-P min size. */
    public static final int HVC_P_MIN_SIZE = 20;
    /** HVC-P max size. */
    public static final int HVC_P_MAX_SIZE = 8192;
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
     * USB Manager.
     */
    private UsbManager mUsbManager;

    /**
     * USB Driver.
     */
    private UsbSerialPort mUsbDriver;

    /**
     * No Command.
     */
    private final int CMD_UNKNOWN = -1;
    /**
     * Get Version.
     */
    private final int CMD_VERSION = 1;
    /**
     * Execute Okao.
     */
    private final int CMD_OKAO_EXECUTE = 2;
    /**
     * Set Threshold.
     */
    private final int CMD_SET_THRESHOLD = 3;
    /**
     * Set Size.
     */
    private final int CMD_SET_SIZE = 4;
    /**
     * Command mType.
     */
    private int mType = 0;
    /**
     * Now interval.
     */
    private Long mNowInterval = PARAM_INTERVAL_MIN;
    /**
     * HVC Cameras.
     */
    private ConcurrentHashMap<String, HVCCameraInfo> mServices;
    /** Event List. */
    private List<String> mEventList;
    /** One shot response list. */
    private List<String> mOneShotList;

    /** Timer Handler. */
    private Handler mTimer;

    public static final String ACTION_USB_PERMISSION_BASE = "com.serenegiant.USB_PERMISSION.";
    public final String ACTION_USB_PERMISSION = ACTION_USB_PERMISSION_BASE + hashCode();

    private PendingIntent mPermissionIntent;


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            String action = intent.getAction();
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "action:" + action);
            }
            UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                HVCCameraInfo camera = new HVCCameraInfo("" + device.getDeviceId() + "_" + device.getProductId() + "_" + device.getVendorId(),
                                "HVC-P:" + "" + device.getDeviceId() + "_" + device.getProductId() + "_" + device.getVendorId());
                mServices.put(camera.getID(), camera);

                // デバイスとの接続完了を通知.
                notifyOnConnected(camera);

                HVCManager.INSTANCE.init(context);
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "device:true:" + device.getDeviceId());
                }
            } else {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "device:false");
                }
            }

        }
    };

    private final List<ConnectionListener> mConnectionListeners = new ArrayList<ConnectionListener>();

    /**
     * Constructor.
     */
    private HVCManager() {
        mServices = new ConcurrentHashMap<String, HVCCameraInfo>();
        mEventList = new ArrayList<String>();
        mOneShotList = new ArrayList<String>();

        mTimer = new Handler();
        mType = CMD_UNKNOWN;
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
        mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        context.registerReceiver(mReceiver, filter);
    }

    /**
     * Unregister Intent-Filter.
     * @param context Context
     */
    public void destroyFilter(final Context context) {
        context.unregisterReceiver(mReceiver);
    }
    /**
     * Add HVC-P Device.
     * @param device device
     */
    public void addUSBDevice(final UsbDevice device) {
        if (!mUsbManager.hasPermission(device)) {
            mUsbManager.requestPermission(device, mPermissionIntent);
        }
    }

    /**
     * Delete HVC-P Device.
     * @param device device
     */
    public void removeUSBDevice(final UsbDevice device) {
        HVCCameraInfo camera = mServices.remove("" + device.getDeviceId() + "_" + device.getProductId() + "_" + device.getVendorId());
        if (camera != null) {
            // デバイスとの接続切断を通知.
            notifyOnDisconnected(camera);
        }
    }

    /**
     * Add Body Detect Event Listener.
     * @param serviceId ServiceID
     * @param l Listener
     * @param interval Interval
     */
    public synchronized void addBodyDetectEventListener(final String serviceId,
                                           final HVCCameraInfo.OnBodyEventListener l,
                                           final Long interval) {
        if (!mEventList.contains(serviceId)) {
            mEventList.add(serviceId);
        }
        HVCCameraInfo camera = mServices.get(serviceId);
        if (camera == null) {
            return;
        }
        camera.setBodyEvent(l);
        mType = CMD_OKAO_EXECUTE;
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "register body event ");
        }
        sendCommand(OKAO_EXECUTE, interval);
    }

    /**
     * Add Hand Detect Event Listener.
     * @param serviceId Service ID
     * @param l Listener
     * @param interval Interval
     */
    public synchronized void addHandDetectEventListener(final String serviceId,
                                           final HVCCameraInfo.OnHandEventListener l,
                                           final Long interval) {
        if (!mEventList.contains(serviceId)) {
            mEventList.add(serviceId);
        }
        HVCCameraInfo camera = mServices.get(serviceId);
        if (camera == null) {
            return;
        }
        camera.setHandEvent(l);
        mType = CMD_OKAO_EXECUTE;
        sendCommand(OKAO_EXECUTE, interval);
    }

    /**
     * Add Face Detect Event Listener.
     * @param serviceId ServiceID
     * @param l Listener
     * @param options Options
     * @param interval Interval
     */
    public synchronized void addFaceDetectEventListener(final String serviceId,
                                           final HVCCameraInfo.OnFaceEventListener l,
                                           final List<String> options,
                                           final Long interval) {
        if (!mEventList.contains(serviceId)) {
            mEventList.add(serviceId);
        }

        HVCCameraInfo camera = mServices.get(serviceId);
        if (camera == null) {
            return;
        }
        camera.setOptions(options);
        camera.setFaceEvent(l);
        mType = CMD_OKAO_EXECUTE;
        sendCommand(OKAO_EXECUTE, interval);
    }

    /**
     * Okao Execute.
     * @param serviceId Service ID
     * @param kind Detect kind
     * @param l One shot listener
     */
    public synchronized void execute(final String serviceId,
                        final HumanDetectKind kind,
                        final HVCCameraInfo.OneShotOkaoResultResoponseListener l) {
        mOneShotList.add(serviceId);

        HVCCameraInfo camera = mServices.get(serviceId);
        if (camera == null) {
            return;
        }
        switch (kind) {
            case BODY:
                camera.setBodyGet(l);
                break;
            case HAND:
                camera.setHandGet(l);
                break;
            case FACE:
                camera.setFaceGet(l);
                break;
            case HUMAN:
                camera.setBodyGet(l);
                camera.setHandGet(l);
                camera.setFaceGet(l);
                break;
            default:
        }
        mType = CMD_OKAO_EXECUTE;
        sendCommand(OKAO_EXECUTE, 1L);
    }

    /**
     * Set Okao Execute's Threshold.
     * @param body body's threshold
     * @param hand hand's threshold
     * @param face face's threshold
     * @param serviceId Service ID
     * @param l One Shot SetParameterResoponseListener
     */
    public synchronized void setThreshold(final Double body,
                                          final Double hand,
                                          final Double face,
                                          final String serviceId,
                                          final HVCCameraInfo.OneShotSetParameterResoponseListener l) {
        int b = HVC_P_BODY_DETECT_THRESHOLD;
        int h = HVC_P_HAND_DETECT_THRESHOLD;
        int p = HVC_P_PET_DETECT_THRESHOLD;
        int f = HVC_P_FACE_DETECT_THRESHOLD;
        int r = HVC_P_RECOGNITION_THRESHOLD;
        if (body != null) {
            b = body.intValue() * 1000;
        }
        if (hand != null) {
            h = hand.intValue() * 1000;
        }
        if (face != null) {
            f = face.intValue() * 1000;
        }
        StringBuffer cmdThreshold = new StringBuffer();
        cmdThreshold.append("FE050800").append(swapLSBandMSB(b)).append(swapLSBandMSB(h))
                .append(swapLSBandMSB(f)).append("F401");
        mOneShotList.add(serviceId);
        HVCCameraInfo camera = mServices.get(serviceId);
        if (camera == null) {
            return;
        }
        camera.setThresholdSet(l);
        mType = CMD_SET_THRESHOLD;
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Set threshold cmd:" + cmdThreshold.toString());
        }
        sendCommand(cmdThreshold.toString(), 1L);

    }

    /**
     * Set Okao Execute's Min Max Size.
     * @param bodyMin body's min size
     * @param bodyMax body's max size
     * @param handMin hand's min size
     * @param handMax hand's max size
     * @param faceMin face's min size
     * @param faceMax face's max size
     * @param serviceId Service ID
     * @param l One Shot SetParameterResoponseListener
     */
    public synchronized void setMinMaxSize(final Double bodyMin, final Double bodyMax,
                                           final Double handMin, final Double handMax,
                                           final Double faceMin, final Double faceMax,
                                           final String serviceId,
                                           final HVCCameraInfo.OneShotSetParameterResoponseListener l) {
        int bMin = HVC_P_BODY_MIN_SIZE;
        int bMax = HVC_P_BODY_MAX_SIZE;
        int hMin = HVC_P_HAND_MIN_SIZE;
        int hMax = HVC_P_HAND_MAX_SIZE;
        int fMin = HVC_P_FACE_MIN_SIZE;
        int fMax = HVC_P_FACE_MAX_SIZE;
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
        if (faceMin != null) {
            fMin = faceMin.intValue() * 8192;
        }
        if (faceMax != null) {
            fMax = faceMax.intValue() * 8192;
        }
        StringBuffer cmdThreshold = new StringBuffer();
        cmdThreshold.append("FE070C00").append(swapLSBandMSB(bMin)).append(swapLSBandMSB(bMax))
                .append(swapLSBandMSB(hMin)).append(swapLSBandMSB(hMax))
                .append(swapLSBandMSB(fMin)).append(swapLSBandMSB(fMax));
        mOneShotList.add(serviceId);
        HVCCameraInfo camera = mServices.get(serviceId);
        if (camera == null) {
            return;
        }
        camera.setSizeSet(l);
        mType = CMD_SET_SIZE;
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Set size cmd:" + cmdThreshold.toString());
        }
        sendCommand(cmdThreshold.toString(), 50L);

    }
    /**
     * Remove Body Detect Event Listener.
     * @param serviceId serviceId
     */
    public synchronized void removeBodyDetectEventListener(final String serviceId) {
        HVCCameraInfo camera = mServices.get(serviceId);
        if (camera == null) {
            return;
        }
        camera.setBodyEvent(null);
        removeEventList(serviceId, camera);
    }

    /**
     * Remove Hand Detect Event Listener
     * @param serviceId Service ID
     */
    public synchronized void removeHandDetectEventListener(final String serviceId) {
        HVCCameraInfo camera = mServices.get(serviceId);
        if (camera == null) {
            return;
        }
        camera.setHandEvent(null);
        removeEventList(serviceId, camera);
    }

    /**
     * Remove Face Detect Event Listener.
     * @param serviceId Service ID
     */
    public synchronized void removeFaceDetectEventListener(final String serviceId) {
        HVCCameraInfo camera = mServices.get(serviceId);
        if (camera == null) {
            return;
        }
        camera.setFaceEvent(null);
        removeEventList(serviceId, camera);
    }

    /**
     * Remove Face Recognize Event Listener.
     * @param serviceId ServiceID
     */
    public synchronized void removeFaceRecognizeEventListener(final String serviceId) {
        HVCCameraInfo camera = mServices.get(serviceId);
        if (camera == null) {
            return;
        }
        camera.setFaceRecognizeEvent(null);
        removeEventList(serviceId, camera);
    }

    /**
     * Remove all event listener.
     */
    public void removeAllEventListener() {
        for (String key : mServices.keySet()) {
            HVCCameraInfo camera = mServices.get(key);
            camera.setBodyEvent(null);
            camera.setFaceEvent(null);
            camera.setFaceRecognizeEvent(null);
            camera.setHandEvent(null);
            mEventList.remove(camera.getID());
        }
    }

    /**
     * Start USB binary read thread.
     */
    private synchronized void startReadThread(final String stCommand, final Long interval) {
        mTimer.removeCallbacksAndMessages(null);
        mNowInterval = interval;
        mTimer.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    byte buf[] = new byte[512];
                    int num = mUsbDriver.read(buf, 1000);
                    if (num <= 0) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        int offset = 0;
                        while (buf[offset] != (byte) 0xFE) {
                            offset++;
                            if (offset >= buf.length - 1) {
                                mTimer.postDelayed(this, mNowInterval);
                                return;
                            }
                        }
                        offset++;
                        if (buf[offset] == 0x00) {
                            // 成功
                            baos.write(buf, offset, num - offset);
                        } else {
                            mTimer.postDelayed(this, mNowInterval);
                            return;
                        }
                        offset++;
                        int dataLength = ((buf[offset+3] & 0xFF) << 24) | ((buf[offset+2] & 0xFF) << 16) |
                                ((buf[offset+1] & 0xFF) << 8) | (buf[offset] & 0xFF);
                        offset += 4;
                        int remaining = dataLength - (num - offset);
                        while (remaining > 0) {
                            int len = mUsbDriver.read(buf, 1000);
                            baos.write(buf, 0, len);
                            remaining -= len;
                        }

                        buf = baos.toByteArray();
                    }
                    for (String key : mServices.keySet()) {
                        HVCCameraInfo camera = mServices.get(key);
                        if (mType == CMD_OKAO_EXECUTE) {
                            if (buf[0] == (byte) 0xfe) {
                                OkaoResult result = parseOkaoResult(buf);
                                if (camera.getBodyEvent() != null) {
                                    camera.getBodyEvent().onNotifyForBodyDetectResult(key, result);
                                }

                                if (camera.getHandEvent() != null) {
                                    camera.getHandEvent().onNotifyForHandDetectResult(key, result);
                                }

                                if (camera.getFaceEvent() != null) {
                                    camera.getFaceEvent().onNotifyForFaceDetectResult(key, result);
                                }

                                if (camera.getBodyGet() != null) {
                                    camera.getBodyGet().onResponse(key, result);
                                    camera.setBodyGet(null);
                                    mOneShotList.remove(key);
                                }
                                if (camera.getHandGet() != null) {
                                    camera.getHandGet().onResponse(key, result);
                                    camera.setHandGet(null);
                                    mOneShotList.remove(key);
                                }
                                if (camera.getFaceGet() != null) {
                                    camera.getFaceGet().onResponse(key, result);
                                    camera.setFaceGet(null);
                                    mOneShotList.remove(key);
                                }
                            }
                        } else if (mType == CMD_SET_THRESHOLD) {
                            if (buf[0] == (byte) 0xfe && camera.getThresholdSet() != null) {
                                camera.getThresholdSet().onResponse(buf[1]);
                                camera.setThresholdSet(null);
                                mOneShotList.remove(key);
                                if (BuildConfig.DEBUG) {
                                    Log.d(TAG, "SET Threshold response");
                                }
                            } else {
                                if (BuildConfig.DEBUG) {
                                    Log.d(TAG, "SET Threshold no response");
                                }
                                camera.getThresholdSet().onResponse(buf[1]);
                                camera.setThresholdSet(null);
                                mOneShotList.remove(key);
                            }
                        } else if (mType == CMD_SET_SIZE) {
                            if (buf[0] == (byte) 0xfe && camera.getSizeSet() != null) {
                                camera.getSizeSet().onResponse(buf[1]);
                                camera.setSizeSet(null);
                                mOneShotList.remove(key);
                                if (BuildConfig.DEBUG) {
                                    Log.d(TAG, "SET size  response");
                                }
                            } else {
                                if (BuildConfig.DEBUG) {
                                    Log.d(TAG, "SET size no response");
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    if (BuildConfig.DEBUG) {
                        e.printStackTrace();
                    }
                    mEventList.clear();
                    mOneShotList.clear();
                    return;
                }
                if (mEventList.size() > 0 || mOneShotList.size() > 0) {

                    if (mType == CMD_OKAO_EXECUTE) {
                        byte send[] = hex2bin(OKAO_EXECUTE);
                        try {
                            mUsbDriver.write(send, 1000);
                        } catch (IOException e) {
                            if (BuildConfig.DEBUG) {
                                Log.e(TAG, "", e);
                            }
                        }
                    }
                    mTimer.postDelayed(this, mNowInterval);
                }
            }
        }, interval);
    }

    @NonNull
    private OkaoResult parseOkaoResult(final byte[] buf) {
        OkaoResult result = new OkaoResult();
        int header = 2 + buf[2] + 4;//(strBuffer.indexOf("a0007800")+8)/2;//160x120
        if (buf[2] > 8) {
            result.setNumberOfBody(buf[6]);
            result.setNumberOfHand(buf[7]);
            result.setNumberOfFace(buf[8]);
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "binary:" + bin2hex(buf));
                Log.d(TAG, "body:" + result.getNumberOfBody());
                Log.d(TAG, "hand:" + result.getNumberOfHand());
                Log.d(TAG, "face:" + result.getNumberOfFace());
            }
            for (int i = 0; i < result.getNumberOfBody(); i++) {
                result.getBodyX()[i] = (buf[10 + 8 * i] & 0xff) + ((buf[10 + 8 * i + 1] & 0xff) << 8);
                result.getBodyY()[i] = (buf[10 + 8 * i + 2] & 0xff) + ((buf[10 + 8 * i + 3] & 0xff) << 8);
                result.getBodySize()[i] = (buf[10 + 8 * i + 4] & 0xff) + ((buf[10 + 8 * i + 5] & 0xff) << 8);
                result.getBodyDetectConfidence()[i] = (buf[10 + 8 * i + 6] & 0xff) + ((buf[10 + 8 * i + 7] & 0xff) << 8);
                if (BuildConfig.DEBUG) {
                    String bodyDebug = "[body" + i + "]"
                            + "| x:" + result.getBodyX()[i]
                            + "| y:" + result.getBodyY()[i]
                            + "| size:" + result.getBodySize()[i]
                            + "| Confidence:" + result.getBodyDetectConfidence()[i] + "\n";
                    Log.d(TAG, bodyDebug);
                }
            }

            for (int i = 0; i < result.getNumberOfHand(); i++) {
                result.getHandX()[i] = (buf[10 + 8 * (result.getNumberOfBody() + i)] & 0xff) + ((buf[10 + 8 * (result.getNumberOfBody() + i) + 1] & 0xff) << 8);
                result.getHandY()[i] = (buf[10 + 8 * (result.getNumberOfBody() + i) + 2] & 0xff) + ((buf[10 + 8 * (result.getNumberOfBody() + i) + 3] & 0xff) << 8);
                result.getHandSize()[i] = (buf[10 + 8 * (result.getNumberOfBody() + i) + 4] & 0xff) + ((buf[10 + 8 * (result.getNumberOfBody() + i) + 5] & 0xff) << 8);
                result.getHandDetectConfidence()[i] = (buf[10 + 8 * (result.getNumberOfBody() + i) + 6] & 0xff) + ((buf[10 + 8 * (result.getNumberOfBody() + i) + 7] & 0xff) << 8);
                if (BuildConfig.DEBUG) {
                    String handDebug = "[hand" + i + "]"
                            + "| x:" + result.getHandX()[i]
                            + "| y:" + result.getHandY()[i]
                            + "| size:" + result.getHandSize()[i]
                            + "| Confidence:" + result.getHandDetectConfidence()[i] + "\n";
                    Log.d(TAG, handDebug);
                }
            }

            for (int i = 0; i < result.getNumberOfFace(); i++) {
                result.getFaceX()[i] = (long) ((buf[10 + 8 * (result.getNumberOfBody() + result.getNumberOfHand()) + 34 * i] & 0xff)
                        | ((buf[10 + 8 * (result.getNumberOfBody() + result.getNumberOfHand())+ 34 * i + 1] & 0xff) << 8)) & 0xffff;
                result.getFaceY()[i] = (long) ((buf[10 + 8 * (result.getNumberOfBody() + result.getNumberOfHand()) + 34 * i + 2] & 0xff)
                        | ((buf[10 + 8 * (result.getNumberOfBody() + result.getNumberOfHand()) + 34 * i + 3] & 0xff) << 8)) & 0xffff;
                result.getFaceSize()[i] = (long) ((buf[10 + 8 * (result.getNumberOfBody() + result.getNumberOfHand()) + 34 * i + 4] & 0xff)
                        | ((buf[10 + 8 * (result.getNumberOfBody() + result.getNumberOfHand()) + 34 * i + 5] & 0xff) << 8)) & 0xffff;
                result.getFaceDetectConfidence()[i] = (long) ((buf[10 + 8 * (result.getNumberOfBody() + result.getNumberOfHand())+ 34 * i + 6] & 0xff)
                        | ((buf[10 + 8 * (result.getNumberOfBody() + result.getNumberOfHand())+ 34 * i + 7] & 0xff) << 8)) & 0xffff;

                result.getFaceDirectionLR()[i] = (long)  (buf[10 + 8 * (result.getNumberOfBody() + result.getNumberOfHand())+ 34 * i + 8] & 0xff)
                        + ((buf[10 + 8 * (result.getNumberOfBody() + result.getNumberOfHand()) + 34 * i + 9] & 0xff) << 8);
                result.getFaceDirectionUD()[i] = (long)  (buf[10 + 8 * (result.getNumberOfBody() + result.getNumberOfHand())+ 34 * i + 10] & 0xff)
                        + ((buf[10 + 8 * (result.getNumberOfBody() + result.getNumberOfHand())+ 34 * i + 8] & 0xff) << 8);
                result.getFaceDirectionSlope()[i] = (long)  (buf[10 + 8 * (result.getNumberOfBody() + result.getNumberOfHand())+ 34 * i + 12] & 0xff)
                        + ((buf[10 + 8 * (result.getNumberOfBody() + result.getNumberOfHand())+ 34 * i + 13] & 0xff) << 8);
                result.getFaceDirectionConfidence()[i] = (long) (buf[10 + 8 * (result.getNumberOfBody() + result.getNumberOfHand())+ 34 * i + 14] & 0xff)
                        + ((buf[10 + 8 * (result.getNumberOfBody() + result.getNumberOfHand())+ 34 * i + 15] & 0xff) << 8);

                result.getAge()[i] = (long) (buf[10 + 8 * (result.getNumberOfBody() + result.getNumberOfHand())+ 34 * i + 16] & 0xff);
                result.getAgeConfidence()[i] = (long) (buf[10 + 8 * (result.getNumberOfBody() + result.getNumberOfHand())+ 34 * i + 17] & 0xff)
                        + ((buf[10 + 8 * (result.getNumberOfBody() + result.getNumberOfHand())+ 34 * i + 18] & 0xff) << 8);

                result.getGender()[i] = (long) (buf[10 + 8 * (result.getNumberOfBody() + result.getNumberOfHand()) + 34 * i + 19] & 0xff);
                result.getGenderConfidence()[i] = (long) (buf[10 + 8 * (result.getNumberOfBody() + result.getNumberOfHand()) + 34 * i + 20] & 0xff)
                        + ((buf[10 + 8 * (result.getNumberOfBody() + result.getNumberOfHand()) + 34 * i + 21] & 0xff) << 8);

                result.getGazeLR()[i] = (long) (buf[10 + 8 * (result.getNumberOfBody() + result.getNumberOfHand())+ 34 * i + 22] & 0xff);
                result.getGazeUD()[i] = (long) (buf[10 + 8 * (result.getNumberOfBody() + result.getNumberOfHand())+ 34 * i + 23] & 0xff);
                result.getBlinkLeft()[i] = (long) (buf[10 + 8 * (result.getNumberOfBody() + result.getNumberOfHand())+ 34 * i + 24] & 0xff)
                        + ((buf[10 + 8 * (result.getNumberOfBody() + result.getNumberOfHand()) + 34 * i + 25] & 0xff) << 8);
                result.getBlinkRight()[i] = (long) (buf[10 + 8 * (result.getNumberOfBody() + result.getNumberOfHand()) + 34 * i + 26] & 0xff)
                        + ((buf[10 + 8 * (result.getNumberOfBody() + result.getNumberOfHand())+ 34 * i + 27] & 0xff) << 8);

                result.getExpressionUnknown()[i] = (long) (buf[10 + 8 * (result.getNumberOfBody() + result.getNumberOfHand())+ 34 * i + 28] & 0xff);
                result.getExpressionSmile()[i] = (long) (buf[10 + 8 * (result.getNumberOfBody() + result.getNumberOfHand()) + 34 * i + 29] & 0xff);
                result.getExpressionSurprise()[i] = (long) (buf[10 + 8 * (result.getNumberOfBody() + result.getNumberOfHand()) + 34 * i + 30] & 0xff);
                result.getExpressionMad()[i] = (long) (buf[10 + 8 * (result.getNumberOfBody() + result.getNumberOfHand()) + 34 * i + 31] & 0xff);
                result.getExpressionSad()[i] = (long) (buf[10 + 8 * (result.getNumberOfBody() + result.getNumberOfHand()) + 34 * i + 32] & 0xff);
                result.getExpressionConfidence()[i] = (long) (buf[10 + 8 * (result.getNumberOfBody() + result.getNumberOfHand()) + 34 * i + 33] & 0xff);
                if (BuildConfig.DEBUG) {
                    String faceDebug =
                            "[face" + i + "]"
                            + "| x:" + result.getFaceX()[i]
                            + "| y:" + result.getFaceY()[i]
                            + "| size:" + result.getFaceSize()[i]
                            + "| Confidence:" + result.getFaceDirectionConfidence()[i] + "\n";
                    Log.d(TAG, faceDebug);
                }
            }

        }
        return result;
    }

    /**
     * Send Command.
     *
     * @param stCommand
     *            Command String Example) FF00AE8
     * @param interval Interval
     */
    private void sendCommand(final String stCommand, final Long interval) {
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);
        if (availableDrivers.isEmpty()) {
            return;
        }

        UsbSerialDriver driver = availableDrivers.get(0);
        UsbDeviceConnection connection = mUsbManager.openDevice(driver.getDevice());
        if (connection == null) {
            // You probably need to call UsbManager.requestPermission(driver.getDevice(), ..)
            return;
        }
        mUsbDriver = driver.getPorts().get(0);

        if (mUsbDriver != null) {
            try {
                mUsbDriver.open(connection);

                mUsbDriver.setParameters(921600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

                startReadThread(stCommand, interval);
                byte send[] = hex2bin(stCommand);
                mUsbDriver.write(send, send.length);


            } catch (IOException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
        }
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
     * Convert Byte as it is to the string.
     * @param data array of byte Example) fee0 <- byte
     * @return string of remains of the value of the byte Example) fee0 <- String
     */
    private  String bin2hex(final byte[] data) {
        StringBuffer sb = new StringBuffer();
        for (byte b : data) {
            String s = Integer.toHexString(0xff & b);
            if (s.length() == 1) {
                sb.append("0");
            }
            sb.append(s);
        }
        return sb.toString();
    }

    /**
     * Convert string as it is to the byte.
     * @param hex hex <- String
     * @return Converted byte array of values hex <- String
     */
    private byte[] hex2bin(final String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int index = 0; index < bytes.length; index++) {
            bytes[index] = (byte) Integer.parseInt(
                    hex.substring(index * 2, (index + 1) * 2), 16);
        }
        return bytes;
    }

    /**
     * Swap LSB and MSB.
     * @param parameter hex
     * @return swap string
     */
    private String swapLSBandMSB(final int parameter) {
        String size = String.format("%04X", parameter & 0xFFFF);
        return size.substring(2, size.length()) + size.substring(0, 2);
    }

    public void addConnectionListener(final ConnectionListener listener) {
        synchronized (mConnectionListeners) {
            if (!mConnectionListeners.contains(listener)) {
                mConnectionListeners.add(listener);
            }
        }
    }

    public void removeConnectionListener(final ConnectionListener listener) {
        synchronized (mConnectionListeners) {
            for (Iterator<ConnectionListener> it = mConnectionListeners.iterator(); ; it.hasNext()) {
                ConnectionListener l = it.next();
                if (l == listener) {
                    it.remove();
                    break;
                }
            }
        }
    }

    private void notifyOnConnected(final HVCCameraInfo cameraInfo) {
        synchronized (mConnectionListeners) {
            for (Iterator<ConnectionListener> it = mConnectionListeners.iterator(); it.hasNext(); ) {
                ConnectionListener listener = it.next();
                listener.onConnected(cameraInfo);
            }
        }
    }

    private void notifyOnDisconnected(final HVCCameraInfo cameraInfo) {
        synchronized (mConnectionListeners) {
            for (Iterator<ConnectionListener> it = mConnectionListeners.iterator(); it.hasNext(); ) {
                ConnectionListener listener = it.next();
                listener.onDisconnected(cameraInfo);
            }
        }
    }

    public interface ConnectionListener {

        void onConnected(HVCCameraInfo camera);

        void onDisconnected(HVCCameraInfo camera);

    }
}
