package org.deviceconnect.android.deviceplugin.theta.fragment;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.provider.Settings;

import org.deviceconnect.android.deviceplugin.theta.R;
import org.deviceconnect.android.deviceplugin.theta.ThetaDeviceService;

@TargetApi(Build.VERSION_CODES.Q)
public class ConfirmationFragmentForApi29 extends BaseConfirmationFragment {

    @Override
    protected void connectWifi(final ScanResult result) {
        Intent intent = new Intent(getContext(), ThetaDeviceService.class);
        intent.setAction(ThetaDeviceService.ACTION_CONNECT_WIFI);
        intent.putExtra(ThetaDeviceService.EXTRA_SCAN_RESULT, result);
        getContext().startService(intent);
    }

    @Override
    protected void onWifiDisabled() {
        mLogger.info("ConfirmationFragmentForApi29.onWifiDisabled");
        // API レベル 29 以降は Wi-Fi の ON/OFF をアプリで制御不能になったため、端末標準の設定画面へ誘導
        ThetaDialogFragment.showConfirmAlert(getActivity(),
                getString(R.string.wifi_setting),
                getString(R.string.wifi_setting_prompt),
                getString(R.string.open_wifi_setting),
                (dialog, which) -> openWifiSetting());
    }

    private void openWifiSetting() {
        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        startActivity(intent);
    }

}
