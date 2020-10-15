/*
 ThetaDeviceService
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import com.theta360.lib.PtpipInitiator;
import com.theta360.lib.ThetaException;

import org.deviceconnect.android.deviceplugin.theta.core.ThetaDevice;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceClient;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceEventListener;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDeviceManager;
import org.deviceconnect.android.deviceplugin.theta.profile.ThetaMediaStreamRecordingProfile;
import org.deviceconnect.android.deviceplugin.theta.profile.ThetaOmnidirectionalImageProfile;
import org.deviceconnect.android.deviceplugin.theta.profile.ThetaSystemProfile;
import org.deviceconnect.android.deviceplugin.theta.service.ThetaImageService;
import org.deviceconnect.android.deviceplugin.theta.service.ThetaService;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.OmnidirectionalImageProfile;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.provider.FileManager;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.android.ssl.EndPointKeyStoreManager;
import org.deviceconnect.android.ssl.KeyStoreCallback;
import org.deviceconnect.android.ssl.KeyStoreError;
import org.deviceconnect.android.ssl.KeyStoreManager;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;

/**
 * Theta Device Service.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaDeviceService extends DConnectMessageService implements ThetaDeviceEventListener {

    public static final String ACTION_CONNECT_WIFI = "action.CONNECT_WIFI";

    public static final String EXTRA_SCAN_RESULT = "scanResult";

    /** ロガー. */
    private final Logger mLogger = Logger.getLogger("theta.dplugin");
    private static final String TYPE_NONE = "none";
    private ThetaDeviceManager mDeviceMgr;
    private ThetaDeviceClient mClient;
    private FileManager mFileMgr;
    private ThetaMediaStreamRecordingProfile mThetaMediaStreamRecording;
    private SSLContext mSSLContext;
    /**
     * SSLContext を提供するインターフェース.
     */
    public interface SSLContextCallback {
        void onGet(SSLContext context);
        void onError();
    }

    public void getSSLContext(final SSLContextCallback callback) {
        final SSLContext sslContext = mSSLContext;
        if (sslContext != null) {
            mLogger.log(Level.INFO, "getSSLContext: requestKeyStore: onSuccess: Already created SSL Context: " + sslContext);
            callback.onGet(sslContext);
        } else {
            requestKeyStore(getIPAddress(this), new KeyStoreCallback() {
                public void onSuccess(final KeyStore keyStore, final Certificate certificate, final Certificate certificate1) {
                    try {
                        mLogger.log(Level.INFO, "getSSLContext: requestKeyStore: onSuccess: Creating SSL Context...");
                        mSSLContext = createSSLContext(keyStore, "0000");
                        mLogger.log(Level.INFO, "getSSLContext: requestKeyStore: onSuccess: Created SSL Context: " + mSSLContext);
                        callback.onGet(mSSLContext);
                    } catch (GeneralSecurityException e) {
                        mLogger.log(Level.WARNING, "getSSLContext: requestKeyStore: onSuccess: Failed to create SSL Context", e);
                        callback.onError();
                    }
                }

                public void onError(final KeyStoreError keyStoreError) {
                    mLogger.warning("getSSLContext: requestKeyStore: onError: error = " + keyStoreError);
                    callback.onError();
                }
            });
        }
    }

    @Override
    protected boolean usesAutoCertificateRequest() {
        return true;
    }

    @Override
    protected void onKeyStoreUpdated(final KeyStore keyStore, final Certificate cert, final Certificate rootCert) {
        try {
            if (keyStore == null) {
                return;
            }
            mSSLContext = createSSLContext(keyStore, "0000");
        } catch (GeneralSecurityException e) {
            mLogger.log(Level.SEVERE, "Failed to update keystore", e);
        }
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        int result = super.onStartCommand(intent, flags, startId);
        if (intent != null && ACTION_CONNECT_WIFI.equals(intent.getAction())) {
            ScanResult scanResult = intent.getParcelableExtra(EXTRA_SCAN_RESULT);
            connectWifi(scanResult);
        }
        return result;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ThetaDeviceApplication app = (ThetaDeviceApplication) getApplication();
        mDeviceMgr = app.getDeviceManager();
        mDeviceMgr.registerDeviceEventListener(this);
        mDeviceMgr.startDeviceDetection();
        mClient = new ThetaDeviceClient(mDeviceMgr);
        mFileMgr = new FileManager(this);

        EventManager.INSTANCE.setController(new MemoryCacheController());

        getServiceProvider().addService(new ThetaImageService(app.getHeadTracker()));
    }

    @Override
    public void onDestroy() {
        mDeviceMgr.dispose();
        mDeviceMgr.unregisterDeviceEventListener(this);
        new Thread(() -> {
            try {
                PtpipInitiator.close();
            } catch (ThetaException e) {
                // Nothing to do.
            }
        }).start();
        super.onDestroy();
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new ThetaSystemProfile();
    }

    @Override
    public void onConnected(final ThetaDevice device) {
        DConnectService service = getServiceProvider().getService(device.getId());
        if (service == null) {
            service = new ThetaService(device, mClient, mFileMgr);
            getServiceProvider().addService(service);
            mThetaMediaStreamRecording = (ThetaMediaStreamRecordingProfile)service.getProfile(ThetaMediaStreamRecordingProfile.PROFILE_NAME);
        }
        service.setOnline(true);
    }

    @Override
    public void onDisconnected(final ThetaDevice device) {
        if (getServiceProvider().hasService(device.getId())) {
            DConnectService service = getServiceProvider().getService(device.getId());
            service.setOnline(false);
        }
    }

    @Override
    protected void onManagerUninstalled() {
        // Managerアンインストール検知時の処理。
        if (BuildConfig.DEBUG) {
            mLogger.info("Plug-in : onManagerUninstalled");
        }
    }

    @Override
    protected void onManagerTerminated() {
        // Manager正常終了通知受信時の処理。
        if (BuildConfig.DEBUG) {
            mLogger.info("Plug-in : onManagerTerminated");
        }
    }

    @Override
    protected void onManagerEventTransmitDisconnected(String sessionKey) {
        // ManagerのEvent送信経路切断通知受信時の処理。
        if (BuildConfig.DEBUG) {
            mLogger.info("Plug-in : onManagerEventTransmitDisconnected");
        }
        if (sessionKey != null) {
            EventManager.INSTANCE.removeEvents(sessionKey);
        } else {
            EventManager.INSTANCE.removeAll();
        }
    }

    @Override
    protected void onDevicePluginReset() {
        // Device Plug-inへのReset要求受信時の処理。
        if (BuildConfig.DEBUG) {
            mLogger.info("Plug-in : onDevicePluginReset");
        }
        resetPluginResource();
    }

    /**
     * リソースリセット処理.
     */
    private void resetPluginResource() {
        /* 全イベント削除. */
        EventManager.INSTANCE.removeAll();

        /* 記録処理・プレビュー停止 */

        if (mThetaMediaStreamRecording != null) {
            mThetaMediaStreamRecording.forcedStopRecording();
        }

        List<ThetaOmnidirectionalImageProfile> omnidirectionalImageProfiles = new ArrayList<>();
        for (DConnectService service : getServiceProvider().getServiceList()) {
            ThetaOmnidirectionalImageProfile profile = (ThetaOmnidirectionalImageProfile) service.getProfile(OmnidirectionalImageProfile.PROFILE_NAME);
            if (profile != null && !omnidirectionalImageProfiles.contains(profile)) {
                omnidirectionalImageProfiles.add(profile);
            }
        }
        for (ThetaOmnidirectionalImageProfile profile : omnidirectionalImageProfiles) {
            profile.forceStopPreview();
        }
    }

    @TargetApi(Build.VERSION_CODES.Q)
    private void connectWifi(final ScanResult result) {
        ThetaDeviceManager deviceManager = ((ThetaDeviceApplication) getApplication()).getDeviceManager();
        deviceManager.requestNetwork(result.SSID);
    }
    /**
     * Gets the ip address.
     *
     * @param context Context of application
     * @return Returns ip address
     */
    private static String getIPAddress(final Context context) {
        Context appContext = context.getApplicationContext();
        WifiManager wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
        ConnectivityManager cManager = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = cManager.getActiveNetworkInfo();
        String en0Ip = null;
        if (network != null) {
            switch (network.getType()) {
                case ConnectivityManager.TYPE_ETHERNET:
                    try {
                        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                            NetworkInterface intf = en.nextElement();
                            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                                InetAddress inetAddress = enumIpAddr.nextElement();
                                if (inetAddress instanceof Inet4Address
                                        && !inetAddress.getHostAddress().equals("127.0.0.1")) {
                                    en0Ip = inetAddress.getHostAddress();
                                    break;
                                }
                            }
                        }
                    } catch (SocketException e) {
                        Log.e("Host", "Get Ethernet IP Error", e);
                    }
            }
        }

        if (en0Ip != null) {
            return en0Ip;
        } else {
            int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
            return String.format(Locale.getDefault(), "%d.%d.%d.%d",
                    (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                    (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
        }
    }
}
