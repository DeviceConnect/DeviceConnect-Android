package org.deviceconnect.android.deviceplugin.hogp;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.hogp.profiles.HOGPHogpProfile;
import org.deviceconnect.android.deviceplugin.hogp.profiles.HOGPSystemProfile;
import org.deviceconnect.android.deviceplugin.hogp.server.HOGPServer;
import org.deviceconnect.android.deviceplugin.hogp.server.MouseHOGPServer;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants.NetworkType;


public class HOGPMessageService extends DConnectMessageService {

    private HOGPServer mHOGPServer;

    @Override
    public void onCreate() {
        super.onCreate();

        DConnectService service = new DConnectService("hogp_service_id");
        service.setName("dConnectDeviceHOGP Service");
        service.setOnline(false);
        service.setNetworkType(NetworkType.BLE);
        service.addProfile(new HOGPHogpProfile());
        getServiceProvider().addService(service);
    }

    @Override
    public void onDestroy() {

        Log.d("ABC", "onDestroy");

        stopHOGPServer();

        super.onDestroy();
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new HOGPSystemProfile();
    }

    @Override
    protected void onManagerUninstalled() {
        // TODO Device Connect Managerアンインストール時に実行したい処理. 実装は任意.
    }

    @Override
    protected void onManagerTerminated() {
        // TODO Device Connect Manager停止時に実行したい処理. 実装は任意.
    }

    @Override
    protected void onManagerEventTransmitDisconnected(final String origin) {
        // TODO アプリとのWebSocket接続が切断された時に実行したい処理. 実装は任意.
    }

    @Override
    protected void onDevicePluginReset() {
        // TODO Device Connect Managerの設定画面上で「プラグイン再起動」を要求された場合の処理. 実装は任意.
    }

    /**
     * HOGPサーバを取得します.
     * <p>
     * HOGPサーバが開始されていない場合にはnullを返却します。
     * </p>
     * @return HOGPサーバ
     */
    public synchronized HOGPServer getHOGPServer() {
        return mHOGPServer;
    }

    /**
     * HOGPサーバを起動します.
     */
    public synchronized void startHOGPServer() {
        if (mHOGPServer != null) {
            return;
        }

        mHOGPServer = new MouseHOGPServer(this);
        mHOGPServer.setOnHOGPServerListener(new HOGPServer.OnHOGPServerListener() {
            @Override
            public void onConnected(final BluetoothDevice device) {
            }

            @Override
            public void onDisconnected(final BluetoothDevice device) {
            }
        });
        mHOGPServer.start();
    }

    /**
     * HOGPサーバを停止します.
     */
    public synchronized void stopHOGPServer() {
        if (mHOGPServer != null) {
            mHOGPServer.stop();
            mHOGPServer = null;
        }
    }
}