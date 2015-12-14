/*
DConnectUtil
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
*/

package org.deviceconnect.android.deviceplugin.sonycamera.utils;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.basic.message.DConnectResponseMessage;
import org.deviceconnect.message.client.DConnectClient;
import org.deviceconnect.message.http.impl.client.HttpDConnectClient;
import org.deviceconnect.message.http.impl.factory.HttpMessageFactory;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;

import android.os.AsyncTask;

/**
 * ユーティリティクラス.
 * @author NTT DOCOMO, INC.
 */
public final class DConnectUtil {
    /** SonyCameraのWiFiのプレフィックス. */
    private static final String WIFI_PREFIX = "DIRECT-";
    /**
     * Camera Remote APIに対応したWiFiのSSIDのサフィックスを定義.
     */
    public static final String[] CAMERA_SUFFIX = {"HDR-AS100", "ILCE-6000", "DSC-HC60V", "DSC-HX400", "ILCE-5000",
            "DSC-QX10", "DSC-QX100", "HDR-AS15", "HDR-AS30", "HDR-MV1", "NEX-5R", "NEX-5T", "NEX-6", "ILCE-7R/B",
            "ILCE-7/B" };

    /** dConnectManagerのURI. */
    private static final String BASE_URI = "http://localhost:4035";

    /** Service Discovery ProfileのURI. */
    private static final String DISCOVERY_URI = BASE_URI + "/" + ServiceDiscoveryProfileConstants.PROFILE_NAME;

    /**
     * コンストラクタ. ユーティリティクラスなのでprivateにしておく。
     */
    private DConnectUtil() {
    }

    /**
     * 指定されたSSIDがSonyCameraデバイスのWifiのSSIDかチェックする.
     * 
     * @param ssid SSID
     * @return SonyCameraデバイスのSSIDの場合はtrue、それ以外はfalse
     */
    public static boolean checkSSID(final String ssid) {
        if (ssid == null) {
            return false;
        }
        String id = ssid.replace("\"", "");
        if (id.startsWith(WIFI_PREFIX)) {
            for (int i = 0; i < CAMERA_SUFFIX.length; i++) {
                if (id.indexOf(CAMERA_SUFFIX[i]) > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 非同期にデバイスを探索する.
     * 
     * @param listener 結果を通知するリスナー
     */
    public static void asyncSearchDevice(final DConnectMessageHandler listener) {
        AsyncTask<Void, Void, DConnectMessage> task = new AsyncTask<Void, Void, DConnectMessage>() {
            @Override
            protected DConnectMessage doInBackground(final Void... params) {
                try {
                    DConnectClient client = new HttpDConnectClient();
                    HttpGet request = new HttpGet(DISCOVERY_URI);
                    HttpResponse response = client.execute(request);
                    return (new HttpMessageFactory()).newDConnectMessage(response);
                } catch (IOException e) {
                    return new DConnectResponseMessage(DConnectMessage.RESULT_ERROR);
                }
            }

            @Override
            protected void onPostExecute(final DConnectMessage message) {
                if (listener != null) {
                    listener.handleMessage(message);
                }
            }
        };
        task.execute();
    }
}
