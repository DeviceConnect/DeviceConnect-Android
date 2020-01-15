package org.deviceconnect.android.deviceplugin.switchbot;

import android.util.Log;

import org.deviceconnect.android.deviceplugin.switchbot.device.SwitchBotDevice;
import org.deviceconnect.android.deviceplugin.switchbot.device.SwitchBotDeviceProvider;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.service.DConnectService;

import org.deviceconnect.android.deviceplugin.switchbot.profiles.SwitchBotButtonProfile;
import org.deviceconnect.android.deviceplugin.switchbot.profiles.SwitchBotSwitchProfile;
import org.deviceconnect.android.deviceplugin.switchbot.profiles.SwitchBotSystemProfile;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants.NetworkType;

import java.util.ArrayList;
import java.util.Locale;


public class SwitchBotMessageService extends DConnectMessageService {
    private static final String TAG = "SwitchBotMessageService";
    private static final Boolean DEBUG = BuildConfig.DEBUG;

    private ArrayList<SwitchBotDevice> switchBotDevices;
    private SwitchBotDeviceProvider switchBotDeviceProvider;

    @Override
    public void onCreate() {
        super.onCreate();

        if(DEBUG){
            Log.d(TAG, "onCreate()");
        }

        // TODO 以降の処理では常駐型のサービスを生成しています. 要件に適さない場合は修正してください.
        DConnectService service = new DConnectService("SwitchBot.Plugin");
        // TODO サービス名の設定
        service.setName("SwitchBot Device Plugin Service");
        // TODO サービスの使用可能フラグのハンドリング
        service.setOnline(true);
        // TODO ネットワークタイプの指定 (例: BLE, Wi-Fi)
        service.setNetworkType(NetworkType.UNKNOWN);
        service.addProfile(new SwitchBotButtonProfile(null));
        service.addProfile(new SwitchBotSwitchProfile(null));
        getServiceProvider().addService(service);

        switchBotDeviceProvider = new SwitchBotDeviceProvider(this);
        switchBotDevices = switchBotDeviceProvider.getDevices();
        for(SwitchBotDevice switchBotDevice : switchBotDevices) {
            createService(switchBotDevice);
        }
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new SwitchBotSystemProfile();
    }

    @Override
    protected void onManagerUninstalled() {
        // TODO Device Connect Managerアンインストール時に実行したい処理. 実装は任意.
    }

    @Override
    protected void onManagerTerminated() {
        // TODO Device Connect Manager停止時に実行したい処理. 実装は任意.
        for(SwitchBotDevice switchBotDevice : switchBotDevices) {
            switchBotDevice.disconnect();
        }
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
     * デバイス登録処理
     * @param switchBotDevice 対象デバイス
     * @return true : 登録成功, false : 登録失敗(デバイス名の重複)
     */
    public Boolean registerDevice(SwitchBotDevice switchBotDevice) {
        if (switchBotDeviceProvider.insert(switchBotDevice)) {
            switchBotDevices.add(switchBotDevice);
            createService(switchBotDevice);
            return true;
        } else {
            return false;
        }
    }

    /**
     * デバイス登録解除処理
     * @param switchBotDevices 対象デバイス
     */
    public void unregisterDevices(ArrayList<SwitchBotDevice> switchBotDevices) {
        if(DEBUG) {
            Log.d(TAG, "unregisterDevice()");
        }
        ArrayList<String> deviceNames = new ArrayList<>();
        for(SwitchBotDevice switchBotDevice : switchBotDevices) {
            if(DEBUG) {
                Log.d(TAG, "switchBotDevice : " + switchBotDevice);
                Log.d(TAG, "device name : " + switchBotDevice.getDeviceName());
                Log.d(TAG, "device address : " + switchBotDevice.getDeviceAddress());
                Log.d(TAG, "device mode : " + switchBotDevice.getDeviceMode());
            }
            getServiceProvider().removeService(makeServiceId(switchBotDevice));
            removeList(switchBotDevice);
        }
        switchBotDeviceProvider.delete(switchBotDevices);
    }

    /**
     * デバイスリスト削除処理
     * @param switchBotDevice 削除対象デバイス
     */
    private void removeList(SwitchBotDevice switchBotDevice) {
        for(int i = 0; i < switchBotDevices.size(); i++) {
            if(switchBotDevices.get(i).getDeviceName().equals(switchBotDevice.getDeviceName())) {
                switchBotDevices.remove(i);
                break;
            }
        }
    }

    /**
     * デバイス毎のサービス生成処理
     * @param switchBotDevice 対象デバイス
     */
    public void createService(SwitchBotDevice switchBotDevice) {
        if(DEBUG){
            Log.d(TAG, "createService()");
            Log.d(TAG, "device name : " + switchBotDevice.getDeviceName());
            Log.d(TAG, "device address : " + switchBotDevice.getDeviceAddress());
            Log.d(TAG, "device mode : " + switchBotDevice.getDeviceMode());
        }
        DConnectService service = new DConnectService(makeServiceId(switchBotDevice));
        // TODO サービス名の設定
        service.setName(makeServiceName(switchBotDevice));
        // TODO サービスの使用可能フラグのハンドリング
        service.setOnline(true);
        // TODO ネットワークタイプの指定 (例: BLE, Wi-Fi)
        service.setNetworkType(NetworkType.UNKNOWN);
        service.addProfile(new SwitchBotButtonProfile(switchBotDevice));
        service.addProfile(new SwitchBotSwitchProfile(switchBotDevice));
        getServiceProvider().addService(service);
        switchBotDevice.connect();
    }

    /**
     * サービスID生成処理
     * @param switchBotDevice 対象デバイス
     * @return サービスID
     */
    private String makeServiceId(final SwitchBotDevice switchBotDevice) {
        return String.format(Locale.US, "SwitchBot.Device.%s", switchBotDevice.getDeviceAddress());
    }

    /**
     * サービス名生成処理
     * @param switchBotDevice 対象デバイス
     * @return サービス名
     */
    private String makeServiceName(final SwitchBotDevice switchBotDevice) {
        return String.format(Locale.US, "%s(SwitchBotDevice)", switchBotDevice.getDeviceName());
    }

    /**
     * デバイス一覧取得処理
     * @return デバイス一覧
     */
    public ArrayList<SwitchBotDevice> getDeviceList() {
        return switchBotDevices;
    }

    public SwitchBotDevice getSwitchBotDeviceFromDeviceName(String deviceName) {
        if(DEBUG){
            Log.d(TAG, "getSwitchBotDeviceFromDeviceName()");
            Log.d(TAG, "deviceName : " + deviceName);
        }
        for(SwitchBotDevice switchBotDevice : switchBotDevices) {
            if(switchBotDevice.getDeviceName().equals(deviceName)) {
                return switchBotDevice;
            }
        }
        return null;
    }

    /**
     * デバイス情報更新処理
     * @param oldDevice 旧デバイス情報
     * @param newDevice 新デバイス情報
     * @return 処理結果。true:成功, false:失敗
     */
    public Boolean modifyDevice(SwitchBotDevice oldDevice, SwitchBotDevice newDevice) {
        if(DEBUG){
            Log.d(TAG,"modifyDevice()");
            Log.d(TAG,"device name(old) : " + oldDevice.getDeviceName());
            Log.d(TAG,"device name(new) : " + newDevice.getDeviceName());
            Log.d(TAG,"device mode(old) : " + oldDevice.getDeviceMode());
            Log.d(TAG,"device mode(new) : " + newDevice.getDeviceMode());
        }
        if(switchBotDeviceProvider.update(oldDevice, newDevice)){
            removeList(oldDevice);
            getServiceProvider().removeService(makeServiceName(oldDevice));
            switchBotDevices.add(newDevice);
            createService(newDevice);
            oldDevice.disconnect();
            if(oldDevice.getDeviceMode() != newDevice.getDeviceMode()) {
                newDevice.modeChange();
            }
            return true;
        }
        return false;
    }
}