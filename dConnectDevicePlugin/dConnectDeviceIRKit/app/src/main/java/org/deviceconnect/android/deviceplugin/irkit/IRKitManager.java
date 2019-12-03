/*
 IRKitManager.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.irkit;

import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.telephony.TelephonyManager;
import android.util.Log;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

/**
 * IRKitの操作をするクラス.
 * @author NTT DOCOMO, INC.
 */
public enum IRKitManager {
    /**
     * シングルトンなIRKitManagerのインスタンス.
     */
    INSTANCE;

    /**
     * タグ名.
     */
    private static final String TAG = "IRKit";
    /**
     * Httpリクエスト ステータスコード 200.
     */
    private static final int STATUS_CODE_OK = 200;

    /**
     * IP解決のタイムアウト {@value} ミリ秒.
     */
    private static final int RESOLVE_TIMEOUT = 500;

    /**
     * HTTPリクエストのタイムアウト {@value} ミリ秒.
     */
    private static final int HTTP_REQUEST_TIMEOUT = 5000;

    /**
     * HTTPリクエストのタイムアウト {@value} ミリ秒.
     */
    private static final int HTTP_REQUEST_LONG_TIMEOUT = 30000;

    /**
     * パスワードの最大長.
     */
    private static final int MAX_PASSWORD_LENGTH = 63;

    /**
     * SSIDの最大長.
     */
    private static final int MAX_SSID_LENGTH = 32;

    /**
     * デバイスキーの最大長.
     */
    private static final int MAX_DEVICE_KEY_LENGTH = 32;
    /**
     * CRC8の初期値.
     */
    private static final byte CRC8INIT = 0x00;
    /**
     * CRC8のポリシー値.
     */
    private static final byte CRC8POLY = 0x31; // = X^8+X^5+X^4+X^0
    /**
     * IRKitのサービスタイプ.
     */
    private static final String SERVICE_TYPE = "_irkit._tcp.local.";

    /**
     * IRKit AIPサーバーのホスト.
     */
    private static final String INTERNET_HOST = "api.getirkit.com";

    /**
     * IRKitをWiFiスポットにした場合のホスト.
     */
    public static final String DEVICE_HOST = "192.168.1.1";

    /**
     * マルチキャスト用のタグ.
     */
    private static final String MULTI_CAST_LOCK_TAG = "org.deviceconnect.android.deviceplugin.irkit";

    /**
     * 16進数変換用コード.
     */
    private static final char[] HEX_CODE = "0123456789ABCDEF".toCharArray();

    /**
     * 日本以外の国リスト. regdomainの決定に利用する。
     */
    private static final String[] NOT_JP_COUNTRIES = {"CA", "MX", "US", "AU", "HK", "IN", "MY", "NZ", "PH", "TW",
            "RU", "AR", "BR", "CL", "CO", "CR", "DO", "DM", "EC", "PA", "PY", "PE", "PR", "VE" };

    /** スリープ時間を定義. */
    private static final long SLEEP_TIME = 1000;

    /** 3.0用ヘッダー名. */
    private static final String X_REQUESTED_WITH_HEADER_NAME = "X-Requested-With";
    /** 3.0用ヘッダー値. */
    private static final String X_REQUESTED_WITH_HEADER_VALUE = "IRKit Device Plug-in";

    /**
     * 検知リスナー.
     */
    private DetectionListener mDetectionListener;

    /**
     * DNSクラス.
     */
    private JmDNS mDNS;

    /**
     * サービス検知リスナー.
     */
    private ServiceListener mServiceListener;

    /**
     * IPのint値.
     */
    private int mIpValue;

    /**
     * 検知中フラグ.
     */
    private boolean mIsDetecting;

    /**
     * apikey.
     */
    private String mAPIKey;

    /**
     * regDomainを決定するためのキャリアの国コード.
     */
    private String mCountryCode;
    
    /** 
     * マルチキャストロック.
     */
    private MulticastLock mMultiLock;
    
    /** 
     * 検知したサービス一覧.
     */
    private ConcurrentHashMap<String, IRKitDevice> mServices;
    
    /** 
     * 消失検出ハンドラ.
     */
    private ServiceRemovingDiscoveryHandler mRemoveHandler;
    
    /**
     * 実行用スレッド管理クラス.
     */
    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    /**
     * WiFiのセキュリティタイプ.
     */
    public enum WiFiSecurityType {

        /**
         * 無し.
         */
        NONE(0),

        /**
         * WEP.
         */
        WEP(2),

        /**
         * WPA2.
         */
        WPA2(8);

        /**
         * コード.
         */
        int mCode;

        /**
         * 指定したコードを持つセキュリティタイプを定義する.
         * 
         * @param code コード
         */
        private WiFiSecurityType(final int code) {
            mCode = code;
        }
    }

    /**
     * IRKitManagerのインスタンスを生成する.
     */
    private IRKitManager() {
        mServiceListener = new ServiceListenerImpl();
        mServices = new ConcurrentHashMap<String, IRKitDevice>();
    }

    /**
     * IRkitデバイスのリストを返す.
     */
    public ConcurrentHashMap<String, IRKitDevice> getIRKitDevices() {
        return mServices;
    }
    /**
     * 指定されたHostとPathへのGETリクエストを生成する.
     * 
     * @param host ホスト名
     * @param path パス
     * @return HttpURLConnectionのインスタンス
     */
    private HttpURLConnection createGetRequest(final String host, final String path) throws IOException {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "http://" + host + path);
        }
        URL url = new URL("http://" + host + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty(X_REQUESTED_WITH_HEADER_NAME, X_REQUESTED_WITH_HEADER_VALUE);
        conn.setReadTimeout(HTTP_REQUEST_TIMEOUT);
        conn.setConnectTimeout(HTTP_REQUEST_TIMEOUT);
        conn.setUseCaches(false);
        return conn;
    }

    /**
     * 指定されたHostとPathへのPOSTリクエストを生成する.
     * 
     * @param host ホスト名
     * @param path パス
     * @return HttpURLConnectionのインスタンス
     */
    private HttpURLConnection createPostRequest(final String host, final String path) throws IOException {
        URL url = new URL("http://" + host + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty(X_REQUESTED_WITH_HEADER_NAME, X_REQUESTED_WITH_HEADER_VALUE);
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        conn.setReadTimeout(HTTP_REQUEST_LONG_TIMEOUT);
        conn.setConnectTimeout(HTTP_REQUEST_LONG_TIMEOUT);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches(false);
        return conn;
    }

    /**
     * リクエストを実行する.
     * @param conn HttpURLConnection
     * @return 実行結果のBody
     */
    private String executeRequest(final HttpURLConnection conn) {
        return  executeRequest(conn, "");
    }
    /**
     * リクエストを実行する.
     * 
     * @param conn リクエスト
     * @param bodyData 送信するボディデータ
     * @return レスポンスボディ。失敗、無い場合はnullを返す。
     */
    private String executeRequest(final HttpURLConnection conn, final String bodyData) {
        String body = null;
        InputStream in = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            if (bodyData != null && bodyData.length() > 0) {
                OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
                out.write(bodyData);
                out.flush();
                out.close();
            }
            conn.connect();
            int resp = conn.getResponseCode();
            if (resp == 200) {
                in = conn.getInputStream();
                int len;
                byte[] buf = new byte[4096];
                while ((len = in.read(buf)) > 0) {
                    baos.write(buf, 0, len);
                }
                in.close();
            } else {
                in = conn.getErrorStream();
                int len;
                byte[] buf = new byte[4096];
                while ((len = in.read(buf)) > 0) {
                    baos.write(buf, 0, len);
                }
                in.close();
            }
            body = new String(baos.toByteArray(), "UTF-8");
        } catch (IOException e) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "IOException", e);
            }
            body = null;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch(IOException e) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "Http Request Error", e);
                }
            }
            conn.disconnect();
        }
        return body;
    }

    /**
     * リクエストを実行する.
     * @param conn HttpURLConnection
     * @param keyValues Query
     * @return Bodyの中身
     * @throws IOException　Stream上のエラー
     */
    private String executeRequest(final HttpURLConnection conn, final Map<String, String> keyValues) throws IOException {
        StringBuffer body = new StringBuffer();
        if (keyValues.size() > 0) {
            Uri.Builder builder = new Uri.Builder();
            Set<String> keys = keyValues.keySet();
            for (String key : keys) {
                builder.appendQueryParameter(key , keyValues.get(key));
            }
            String join = builder.build().getEncodedQuery();
            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
            out.write(join);
            out.flush();
            out.close();
            conn.connect();
            String st = null;
            InputStream in = null;
            try {
                in = conn.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                while ((st = br.readLine()) != null) {
                    body.append(st);
                }
            } catch (IOException e) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "Http Request Error", e);
                }
                body = null;
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch(IOException e) {
                    if (BuildConfig.DEBUG) {
                        Log.e(TAG, "Http Request Error", e);
                    }
                }
                conn.disconnect();

            }
        }
        return body.toString();
    }

    /**
     * WiFi接続時のIPアドレスを解析する.
     * 
     * @param ipValue IPアドレスのint値
     * @return InetAddressのインスタンス。失敗した場合はnullを返す。
     */
    private InetAddress parseIPAddress(final int ipValue) {
        byte[] byteaddr = new byte[] {(byte) (ipValue & 0xff), (byte) (ipValue >> 8 & 0xff),
                (byte) (ipValue >> 16 & 0xff), (byte) (ipValue >> 24 & 0xff)};
        try {
            return InetAddress.getByAddress(byteaddr);
        } catch (UnknownHostException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            // IPが取れなければ検知不可能として処理させる。
            return null;
        }
    }

    /**
     * 文字列を16進数に変換する.
     * 
     * @param ori 変換元の文字列
     * @param maxLength 変換する最大長
     * @return 返還後の文字列
     */
    private String toHex(final String ori, final int maxLength) {

        String tmp = ori;
        if (tmp.length() > maxLength) {
            tmp = tmp.substring(0, maxLength);
        }

        byte[] data = tmp.getBytes();
        StringBuilder sb = new StringBuilder(data.length * 2);
        for (byte b : data) {
            sb.append(HEX_CODE[(b >> 4) & 0xF]);
            sb.append(HEX_CODE[(b & 0xF)]);
        }

        return sb.toString();
    }

    /**
     * CRC8変換.
     * 
     * @param data 変換元データ
     * @param size データサイズ
     * @return 変換したデータ
     */
    private byte crc8(byte[] data, int size) {
        return crc8(data, size, CRC8INIT);
    }

    /**
     * CRC8変換
     * @param data 変換データ
     * @param size データサイズ
     * @param crcinit CRC初期値
     * @return CRC8のバイトデータ
     */
    private byte crc8(byte[] data, int size, byte crcinit) {
        byte crc = crcinit;
        int dataLength = data.length;
        for (int i = 0; i < size; i++) {
            if (i < dataLength) {
                crc ^= data[i];
            }
            for (int j = 0; j < 8; j++) {
                if ((crc & 0x80) != 0x00) {
                    crc = (byte) ((crc << 1) ^ CRC8POLY);
                } else {
                    crc <<= 1;
                }
            }
        }
        return crc;
    }

    /**
     * バイトデータを文字列に変換する.
     * @param bytes バイトデータ
     * @return バイトデータ文字列
     */
    private String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append( String.format(Locale.US, "%02x", b) );
        }
        return sb.toString().toUpperCase();
    }
    /**
     * WiFiへの接続情報をシリアライズするためにbyte配列に変換する.
     * 
     * @param type セキュリティタイプ
     * @param ssid SSID
     * @param password パスワード
     * @param deviceKey デバイスキー
     * @return byte配列
     */
    private String toCRC(final WiFiSecurityType type, final String ssid, final String password,
                         final String deviceKey) {

        byte[] ssidBytes;
        byte[] passwordBytes;
        byte[] deviceKeyBytes;
        try {
            ssidBytes = ssid.getBytes("UTF-8");
            passwordBytes = password.getBytes("UTF-8");
            deviceKeyBytes = deviceKey.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        byte crc = crc8(new byte[]{ (byte) type.mCode }, 1);
        crc = crc8(ssidBytes, 33, crc);
        crc = crc8(passwordBytes, 64, crc);
        crc = crc8(new byte[]{ 1 }, 1, crc); // wifi_is_set == true
        crc = crc8(new byte[]{ 0 }, 1, crc); // wifi_was_valid == false
        crc = crc8(deviceKeyBytes, 33, crc);
        String crcString = String.format(Locale.US, "%02x", crc).toUpperCase();

        return crcString;
    }

    /**
     * 文字列を指定された長さのbyte配列に変換する.
     * 
     * @param str 文字列
     * @param length 長さ
     * @return byte配列
     */
    private byte[] toByte(final String str, final int length) {

        byte[] res = new byte[length];
        byte[] data = str.getBytes();

        for (int i = 0; i < length; i++) {
            if (i == data.length) {
                break;
            }
            res[i] = data[i];
        }

        return res;
    }

    /**
     * サービスを登録する.
     * 
     * @param device サービス
     */
    private void addService(final IRKitDevice device) {
        mServices.put(device.getName(), device);
    }
    
    /**
     * サービスを削除する.
     * 
     * @param device サービス
     */
    private synchronized void removeService(final IRKitDevice device) {
        
        if (!isDetecting()) {
            return;
        }
        
        mServices.remove(device.getName());
        
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Lost Device : " + device);
        }
        
        if (mDetectionListener != null) {
            mDetectionListener.onLostDevice(device);
        }
    }
    
    /**
     * regdomainを取得する.
     * 
     * @return regdomain
     */
    private String getRegDomain() {

        String regdomain = null;

        if (mCountryCode == null) {
            Locale locale = Locale.getDefault();
            mCountryCode = locale.getCountry();
        }

        if ("JP".equals(mCountryCode)) {
            regdomain = "2";
        } else {

            for (String code : NOT_JP_COUNTRIES) {
                if (code.equals(mCountryCode)) {
                    regdomain = "0";
                    break;
                }
            }

            if (regdomain == null) {
                regdomain = "1";
            }
        }
        return regdomain;
    }
    /**
     * 初期化を実行する.
     * 
     * @param context コンテキストオブジェクト。コンテキストは保持されない。
     */
    public void init(final ContextWrapper context) {
        mAPIKey = context.getString(R.string.apikey);

        TelephonyManager tm = getTelephonyManager(context);
        mCountryCode = tm.getSimCountryIso().toUpperCase(Locale.ENGLISH);
    }

    /**
     * 端末検知リスナーを設定する.
     * 
     * @param listener DetectionListenerのインスタンス
     */
    public synchronized void setDetectionListener(final DetectionListener listener) {
        mDetectionListener = listener;
    }

    /**
     * 検知中かどうか.
     * 
     * @return 検知中はtrue、その他はfalse
     */
    public synchronized boolean isDetecting() {
        return mIsDetecting;
    }

    /**
     * 端末検知を開始する.
     * 
     * @param context コンテキスト
     */
    public synchronized void startDetection(final ContextWrapper context) {
        
        if (isDetecting()) {
            return;
        }
        
        mIsDetecting = true;
        init(context);
        
        WifiManager wifi = getWifiManager(context);
        mMultiLock = wifi.createMulticastLock(MULTI_CAST_LOCK_TAG);
        mMultiLock.setReferenceCounted(true);
        mMultiLock.acquire();

        WifiInfo info = wifi.getConnectionInfo();
        if (info != null) {
            mIpValue = info.getIpAddress();
        } else {
            mIpValue = 0;
        }
        
        new Thread(new Runnable() {

            @Override
            public void run() {
                synchronized (INSTANCE) {
                    try {
                        if (mDNS != null || mIpValue == 0) {
                            mIsDetecting = false;
                            return;
                        }
                        InetAddress ia = parseIPAddress(mIpValue);
                        if (ia == null) {
                            mIsDetecting = false;
                            return;
                        }

                        mDNS = JmDNS.create(ia);
                        mDNS.addServiceListener(SERVICE_TYPE, mServiceListener);
                        mIsDetecting = true;
                        
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "start detection.");
                        }
                        
                    } catch (IOException e) {
                        if (BuildConfig.DEBUG) {
                            e.printStackTrace();
                        }
                        mIsDetecting = false;
                        mDNS = null;
                    }
                }
            }
        }).start();
    }

    /**
     * 端末検知を終了する.
     */
    public synchronized void stopDetection() {
        if (mDNS != null) {
            mRemoveHandler = null;
            mIsDetecting = false;
            mServices.clear();
            mDNS.removeServiceListener(SERVICE_TYPE, mServiceListener);
            try {
                mDNS.close();
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "close detection.");
                }
            } catch (IOException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            } finally {
                // クローズできなかった場合は特に復旧できなさそうなので参照だけきっておく。
                mDNS = null;
            }
        }
        
        if (mMultiLock != null && mMultiLock.isHeld()) {
            mMultiLock.release();
            mMultiLock = null;
        }
    }

    /**
     * 指定したIPに紐づくIRKitから赤外線データを取得する.
     * 
     * @param ip IRKitのIPアドレス
     * @param callback 処理結果を受けるコールバック
     */
    public void fetchMessage(final String ip, final GetMessageCallback callback) {
        mExecutor.execute(new Runnable() {

            @Override
            public void run() {
                HttpURLConnection req = null;
                try {
                    req = createGetRequest(ip, "/messages");
                    String message = executeRequest(req);
                    callback.onGetMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // 連続でIRKitに通信を行わないようにするためのスリープ
                // IRKitに連続で通信を行うと正常に動作しないことがあるため
                try {
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }



    /**
     * 赤外線データを送信する.
     * 
     * @param ip IRKitのIP
     * @param message 赤外線データ
     * @param callback 処理結果の通知を受けるコールバック
     */
    public void sendMessage(final String ip, final String message, final PostMessageCallback callback) {
        mExecutor.execute(new Runnable() {

            @Override
            public void run() {
                HttpURLConnection req = null;
                boolean result = false;
                try {
                    req = createPostRequest(ip, "/messages");
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "ip=" + ip + " post message : " + message);
                    }
                    req.setRequestProperty("Content-Length", String.valueOf(message.length()));
                    OutputStreamWriter out = new OutputStreamWriter(req.getOutputStream());
                    out.write(message);
                    out.flush();
                    req.connect();
                    out.close();
                    int status = req.getResponseCode();
                    if (status == STATUS_CODE_OK) {
                        result = true;
                    }

                    // 連続でIRKitに通信を行わないようにするためのスリープ
                    // IRKitに連続で通信を行うと正常に動作しないことがあるため
                    try {
                        Thread.sleep(SLEEP_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    callback.onPostMessage(result);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (req != null) {
                        req.disconnect();
                    }
                    callback.onPostMessage(result);
                }

            }
        });
    }

    /**
     * clientkeyを取得する.
     * 
     * @param callback コールバック
     */
    public void fetchClientKey(final GetClientKeyCallback callback) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                String clientKey = null;
                try {
                    Map<String, String> params = new HashMap<>();
                    params.put("apikey", mAPIKey);
                    HttpURLConnection req = createPostRequest(INTERNET_HOST, "/1/clients");
                    req.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                    String body = executeRequest(req, params);
                    JSONObject json = new JSONObject(body);
                    clientKey = json.getString("clientkey");
                } catch (IOException e) {
                    if (BuildConfig.DEBUG) {
                        e.printStackTrace();
                    }
                    clientKey = null;
                } catch (JSONException e) {
                    if (BuildConfig.DEBUG) {
                        e.printStackTrace();
                    }
                    clientKey = null;
                }

                callback.onGetClientKey(clientKey);

                // 連続でIRKitに通信を行わないようにするためのスリープ
                try {
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 新規にデバイスを生成する.
     * 
     * @param clientKey clientkey
     * @param callback コールバック
     */
    public void createNewDevice(final String clientKey, final GetNewDeviceCallback callback) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                String deviceKey = null;
                String serviceId = null;
                try {
                    Map<String, String> params = new HashMap<>();
                    params.put("clientkey", clientKey);
                    HttpURLConnection req = createPostRequest(INTERNET_HOST, "/1/devices");
                    req.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                    String body = executeRequest(req, params);
                    JSONObject json = new JSONObject(body);
                    serviceId = json.getString("deviceid");
                    deviceKey = json.getString("devicekey");
                } catch (IOException e) {
                    if (BuildConfig.DEBUG) {
                        e.printStackTrace();
                    }
                    serviceId = null;
                    deviceKey = null;
                } catch (JSONException e) {
                    if (BuildConfig.DEBUG) {
                        e.printStackTrace();
                    }
                    serviceId = null;
                    deviceKey = null;
                }
                callback.onGetDevice(serviceId, deviceKey);
            }
        }).start();
    }

    /**
     * IRKitをWiFiへ接続させる.
     * 
     * @param ssid 接続させるWiFiのSSID
     * @param password パスワード
     * @param type セキュリティタイプ
     * @param deviceKey デバイスキー
     * @param callback コールバック
     */
    public void connectIRKitToWiFi(final String ssid, final String password, final WiFiSecurityType type,
            final String deviceKey, final IRKitConnectionCallback callback) {
        new Thread(() -> {
            String ssidHex = toHex(ssid, MAX_SSID_LENGTH + 1);
            String tmpPassword = password;
            if (type == WiFiSecurityType.WEP && (password.length() == 5 || password.length() == 13)) {
                tmpPassword = toHex(password, MAX_PASSWORD_LENGTH);
            }
            String passHex = toHex(tmpPassword, MAX_PASSWORD_LENGTH);
            String crcHex = toCRC(type, ssid, tmpPassword, deviceKey);
            String regdomain = getRegDomain();
            String postData = String.format(Locale.ENGLISH, "%d/%s/%s/%s/%s//////%s", type.mCode, ssidHex, passHex,
                    deviceKey, regdomain, crcHex).toUpperCase(Locale.ENGLISH);

            HttpURLConnection req = null;
            boolean result = false;
            try {
                req = createPostRequest(DEVICE_HOST, "/wifi");
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "body : " + postData);
                }
                req.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                executeRequest(req, postData);
                int status = req.getResponseCode();
                if (status == STATUS_CODE_OK) {
                    result = true;
                }
            } catch (IOException e) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "disconnect:" + result, e);
                }
            } finally {
                if (req != null) {
                    req.disconnect();
                }
                callback.onConnectedToWiFi(result);
            }
        }).start();
    }
    
    /**
     * 指定されたIPのデバイスがIRKitかをチェックする.
     * 
     * @param ip IPアドレス
     * @param callback コールバック
     */
    public void checkIfTargetIsIRKit(final String ip, final CheckingIRKitCallback callback) {
        new Thread(() -> {
            boolean isIRKit = false;
            HttpURLConnection req = null;
            try {
                req = createGetRequest(ip, "/messages");
                executeRequest(req);
                Map<String, List<String>> headers = req.getHeaderFields();
                for (String h : headers.keySet()) {
                    if (h == null) {
                        continue;
                    }
                    if (h.equals("Server")) {
                        for (String v : headers.get(h)) {
                            if (v.contains(TAG)) {
                                isIRKit = true;
                                break;
                            }
                        }
                        if (isIRKit) {
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "disconnect:" + isIRKit, e);

                }
            } finally {
                if (req != null) {
                    req.disconnect();
                }
            }

            callback.onChecked(isIRKit);
        }).start();
    }
    
    /**
     * IRKitがインターネットに接続したかをチェックする.
     * 
     * @param clientKey クライアントキー
     * @param serviceId サービスID
     * @param callback コールバック
     */
    public void checkIfIRKitIsConnectedToInternet(final String clientKey, final String serviceId, 
            final IRKitConnectionCheckingCallback callback) {
        new Thread(() -> {
            String hostName = null;
            try {
                Map<String, String> params = new HashMap<>();
                params.put("clientkey", clientKey);
                params.put("deviceid", serviceId);
                HttpURLConnection req = createPostRequest(INTERNET_HOST, "/1/door");
                req.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String body = executeRequest(req, params);
                JSONObject json = new JSONObject(body);
                hostName = json.getString("hostname");

            } catch (IOException e) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "disconnect:" + (hostName != null), e);
                }
                hostName = null;
            } catch (JSONException e) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "disconnect:" + (hostName != null), e);
                }
                hostName = null;
            }
            callback.onConnectedToInternet(hostName != null);
        }).start();
    }

    private TelephonyManager getTelephonyManager(final Context context) {
        return (TelephonyManager) context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
    }

    private WifiManager getWifiManager(final Context context) {
        return (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    /**
     * 検知の通知を受けるリスナー.
     */
    public interface DetectionListener {

        /**
         * デバイスを検知したことを通知する.
         * 
         * @param device 検知したIRKitのデバイス
         */
        void onFoundDevice(IRKitDevice device);

        /**
         * デバイスが消失したことを通知する.
         * 
         * @param device 消失したIRKitのデバイス
         */
        void onLostDevice(IRKitDevice device);
    }

    /**
     * 赤外線データの取得リクエストコールバック.
     */
    public interface GetMessageCallback {

        /**
         * 赤外線データを受け取ったことを通知する.
         * 
         * @param message 赤外線データ。取れなかった場合はnull。
         */
        void onGetMessage(String message);
    }

    /**
     * 赤外線データの送信リクエストコールバック.
     */
    public interface PostMessageCallback {
        /**
         * 送信が完了したことを通知する.
         * 
         * @param result 成功した場合true、その他はfalseを返す。
         */
        void onPostMessage(boolean result);
    }

    /**
     * clientkeyの取得リクエストコールバック.
     */
    public interface GetClientKeyCallback {

        /**
         * clientkeyを取得したことを通知する.
         * 
         * @param clientKey クライアントキー。取得できなかった場合はnullを返す。
         */
        void onGetClientKey(String clientKey);
    }

    /**
     * 新規デバイスの取得リクエストコールバック.
     */
    public interface GetNewDeviceCallback {

        /**
         * 新規デバイスが生成できたことを通知する.
         * 
         * @param serviceId サービスID
         * @param deviceKey デバイスキー
         */
        void onGetDevice(String serviceId, String deviceKey);
    }

    /**
     * IRKitのWiFiへの接続処理完了コールバック.
     */
    public interface IRKitConnectionCallback {

        /**
         * WiFiへの接続処理が完了したことを通知する.
         * 
         * @param isConnect 接続成功ならtrue、その他はfalseを返す。
         */
        void onConnectedToWiFi(boolean isConnect);
    }
    
    /**
     * IRKitのインターネットへの接続確認完了コールバック.
     */
    public interface IRKitConnectionCheckingCallback {

        /**
         * インターネットへの接続確認が完了したことを通知する.
         * 
         * @param isConnect 接続成功ならtrue、その他はfalseを返す。
         */
        void onConnectedToInternet(boolean isConnect);
    }
    
    /**
     * IRKitかどうかのチェック完了コールバック.
     */
    public interface CheckingIRKitCallback {

        /**
         * IRKitかどうかの判断が出来たことを通知する.
         * 
         * @param isIRKit IRKitならtrue、その他はfalseを返す。
         */
        void onChecked(boolean isIRKit);
    }

    /**
     * サービス検知のリスナー実装.
     */
    private class ServiceListenerImpl implements ServiceListener {

        @Override
        public void serviceAdded(final ServiceEvent event) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "serviceAdded");
            }
            synchronized (INSTANCE) {
                if (mDetectionListener != null) {
                    mDNS.requestServiceInfo(SERVICE_TYPE, event.getName(), RESOLVE_TIMEOUT);
                }
            }
        }

        @Override
        public void serviceRemoved(final ServiceEvent event) {
            // do-nothing.
        }

        @Override
        public void serviceResolved(final ServiceEvent event) {
            ServiceInfo info = event.getInfo();
            String ip = null;
            
            Inet4Address[] ipv4 = info.getInet4Addresses();
            if (ipv4 != null && ipv4.length != 0) {
                ip = ipv4[0].toString();
            } else {
                Inet6Address[] ipv6 = info.getInet6Addresses();
                if (ipv6 != null && ipv6.length != 0) {
                    ip = ipv6[0].toString();
                }
            }

            if (ip == null) {
                // IPが解決できない場合は通知しない。
                return;
            }

            IRKitDevice device = new IRKitDevice();
            device.setName(info.getName().toUpperCase(Locale.ENGLISH));
            ip = ip.replace("/", ""); // /が前に入っているので削除する.
            device.setIp(ip);

            synchronized (INSTANCE) {
                addService(device);
                
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "serviceResolved ip=" + ip);
                    Log.d(TAG, "device=" + device);
                    Log.d(TAG, "devicename=" + device.getName());
                }
                
                if (mRemoveHandler == null) {
                    mRemoveHandler = new ServiceRemovingDiscoveryHandler();
                    mRemoveHandler.start();
                } else {
                    mRemoveHandler.refresh();
                }
                mDetectionListener.onFoundDevice(device);
            }
        }
    }
    
    /**
     * サービス消失検知ハンドラ.
     */
    private class ServiceRemovingDiscoveryHandler extends Thread {
        
        /** 
         * インターバル最大値. (ミリ秒)
         */
        private static final long MAX_INTERVAL = 512 * 1000;

        /**
         * インターバルの初期値. (秒)
         */
        private static final long INITIAL_INTERVAL = 60;

        /** 
         * インターバル. (秒)
         */
        private long mNextInterval = INITIAL_INTERVAL;
        
        /** 
         * 実際の待ち時間. (ミリ秒)
         */
        private long mDelay;

        /**
         * カウンタ.
         */
        private int mCount;
        
        /** 
         * 削除リスト.
         */
        private ArrayList<IRKitDevice> mRemoveList = new ArrayList<IRKitDevice>();
        
        /**
         * インスタンスの生成.
         */
        public ServiceRemovingDiscoveryHandler() {
        }
        
        /**
         * 検索インターバルをリフレッシュする.
         */
        public synchronized void refresh() {
            interrupt();
            mCount = 0;
            mDelay = 0;
            mNextInterval = INITIAL_INTERVAL;
        }
        
        @Override
        public void run() {
            super.run();
            
            while (true) {
                
                if (!isDetecting()) {
                    break;
                }
                
                long pt = checkConnection();
                
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Check Time : " + pt);
                }
                
                synchronized (this) {

                    // NOTE: 最初の約8分間は一定の短いインターバルで生存確認を行う.
                    // それ以降は最大値に達するまで生存確認するごとにインターバルを
                    // 2倍にのばしていく.
                    if (mCount < 64) {
                        mCount++;
                    } else {
                        mNextInterval <<= 1;
                    }

                    mDelay = (mNextInterval * 1000) - pt;
                    if (mDelay < 0) {
                        mDelay = 0;
                    } else if (mDelay > MAX_INTERVAL) {
                        mDelay = MAX_INTERVAL;
                        mNextInterval = MAX_INTERVAL;
                    }
                }
                
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Start remove checking after " + mDelay + " ms.");
                }
                
                try {
                    if (isInterrupted()) {
                        throw new InterruptedException();
                    }
                    sleep(mDelay);
                } catch (InterruptedException e) {
                    if (BuildConfig.DEBUG) {
                        e.printStackTrace();
                    }
                    mNextInterval = INITIAL_INTERVAL;
                }
            }
        }

        
        /**
         * IRKitの接続チェックをする.
         * 
         * @return 実行時間
         */
        private long checkConnection() {
            final CountDownLatch lock;
            mRemoveList.clear();
            long start = System.currentTimeMillis();
            synchronized (mServices) {
                final int max = mServices.size();
                lock = new CountDownLatch(max);
                for (final IRKitDevice device : mServices.values()) {
                    checkIfTargetIsIRKit(device.getIp(), new CheckingIRKitCallback() {
                        @Override
                        public void onChecked(final boolean isIRKit) {
                            synchronized (mRemoveList) {
                                
                                if (!isIRKit) {
                                    mRemoveList.add(device);
                                }
                                
                                lock.countDown();
                            }
                        }
                    });
                }
            }
            
            try {
                lock.await();
            } catch (InterruptedException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            } finally {
                for (IRKitDevice device : mRemoveList) {
                    removeService(device);
                }
            }
            
            return System.currentTimeMillis() - start;
        }
    }
}
