package org.deviceconnect.android.deviceplugin.fabo.setting;

import android.app.Activity;
import android.content.Intent;

import org.deviceconnect.android.deviceplugin.fabo.FaBoDeviceService;
import org.deviceconnect.android.deviceplugin.fabo.service.virtual.VirtualService;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.android.ui.activity.DConnectServiceListActivity;

/**
 * FaBoプラグインが管理するサービスのリストを表示するActivity.
 */
public class FaBoServiceListActivity extends DConnectServiceListActivity {
    @Override
    protected Class<? extends DConnectMessageService> getMessageServiceClass() {
        return FaBoDeviceService.class;
    }

    @Override
    protected Class<? extends Activity> getSettingManualActivityClass() {
        return FaBoVirtualServiceActivity.class;
    }

    @Override
    protected boolean enablesItemClick() {
        return true;
    }

    @Override
    protected void onItemClick(final DConnectService service) {
        if (service instanceof VirtualService) {
            openVirtualServiceActivity((VirtualService) service);
        }
    }

    @Override
    public void onServiceRemoved(DConnectService service) {
        super.onServiceRemoved(service);

        if (service instanceof VirtualService) {
            FaBoDeviceService faBo = (FaBoDeviceService) getMessageService();
            faBo.removeServiceData(((VirtualService) service).getServiceData());
        }
    }

    /**
     * VirtualServiceActivityを開きます.
     * @param service 仮想サービス
     */
    private void openVirtualServiceActivity(final VirtualService service) {
        Intent intent = new Intent();
        intent.setClass(this, FaBoVirtualServiceActivity.class);
        intent.putExtra("service", service.getServiceData());
        startActivity(intent);
    }
}
