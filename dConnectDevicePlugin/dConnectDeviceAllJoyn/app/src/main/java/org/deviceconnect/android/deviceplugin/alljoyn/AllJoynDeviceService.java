package org.deviceconnect.android.deviceplugin.alljoyn;

import android.util.Log;

import org.deviceconnect.android.deviceplugin.alljoyn.profile.AllJoynSystemProfile;
import org.deviceconnect.android.deviceplugin.alljoyn.service.AllJoynService;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.service.DConnectService;

/**
 * Device Connect device plug-in for AllJoyn.
 *
 * @author NTT DOCOMO, INC.
 */
public class AllJoynDeviceService extends DConnectMessageService
    implements AllJoynDeviceApplication.ConnectionListener {

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Log.i(getClass().getSimpleName(), "started");
        }

        ((AllJoynDeviceApplication) getApplication()).setConnectionListener(this);
    }

    @Override
    protected void onManagerUninstalled() {
        // Managerアンインストール検知時の処理
        if (BuildConfig.DEBUG) {
            Log.i(getClass().getSimpleName(),"Plug-in : onManagerUninstalled");
        }
    }

    @Override
    protected void onManagerTerminated() {
        // Manager正常終了通知受信時の処理
        if (BuildConfig.DEBUG) {
            Log.i(getClass().getSimpleName(),"Plug-in : onManagerTerminated");
        }
    }

    @Override
    protected void onDevicePluginReset() {
        // Device Plug-inへのReset要求受信時の処理
        if (BuildConfig.DEBUG) {
            Log.i(getClass().getSimpleName(),"Plug-in : onDevicePluginReset");
        }
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new AllJoynSystemProfile();
    }

    @Override
    public void onConnect(final AllJoynServiceEntity entity) {
        DConnectService service = new AllJoynService(entity);
        service.setOnline(true);
        getServiceProvider().addService(service);
    }

    @Override
    public void onDisconnect(final AllJoynServiceEntity entity) {
        DConnectService service = getServiceProvider().getService(entity.appId);
        if (service != null) {
            service.setOnline(false);
        }
    }
}
