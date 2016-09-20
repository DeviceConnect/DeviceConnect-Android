/*
 FPLUGDeviceService.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.fplug;

import android.util.Log;

import org.deviceconnect.android.deviceplugin.fplug.fplug.FPLUGController;
import org.deviceconnect.android.deviceplugin.fplug.profile.FPLUGSystemProfile;
import org.deviceconnect.android.deviceplugin.fplug.service.FPLUGService;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.MemoryCacheController;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.service.DConnectService;

/**
 * F-PLUG device plug-in.
 *
 * @author NTT DOCOMO, INC.
 */
public class FPLUGDeviceService extends DConnectMessageService
    implements FPLUGApplication.ControllerListener {

    @Override
    public void onCreate() {
        super.onCreate();
        EventManager.INSTANCE.setController(new MemoryCacheController());

        FPLUGApplication app = (FPLUGApplication) getApplication();
        app.setControllerListener(this);
    }

    @Override
    protected void onManagerUninstalled() {
        // Managerアンインストール検知時の処理
        if (BuildConfig.DEBUG) {
            Log.i("fplug.dplugin", "Plug-in : onManagerUninstalled");
        }
    }

    @Override
    protected void onManagerTerminated() {
        // Manager正常終了通知受信時の処理
        if (BuildConfig.DEBUG) {
            Log.i("fplug.dplugin", "Plug-in : onManagerTerminated");
        }
    }

    @Override
    protected void onDevicePluginReset() {
        // Device Plug-inへのReset要求受信時の処理
        if (BuildConfig.DEBUG) {
            Log.i("fplug.dplugin", "Plug-in : onDevicePluginReset");
        }
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new FPLUGSystemProfile();
    }

    @Override
    public void onAdded(final FPLUGController controller) {
        addService(controller);
    }

    @Override
    public void onConnected(final FPLUGController controller) {
        getService(controller).setOnline(true);
    }

    @Override
    public void onDisconnected(final FPLUGController controller) {
        DConnectService service = getServiceProvider().getService(controller.getAddress());
        if (service != null) {
            service.setOnline(false);
        }
    }

    private DConnectService addService(final FPLUGController controller) {
        DConnectService service = new FPLUGService(controller.getAddress());
        getServiceProvider().addService(service);
        return service;
    }

    private DConnectService getService(final FPLUGController controller) {
        DConnectService service = getServiceProvider().getService(controller.getAddress());
        if (service == null) {
            service = addService(controller);
        }
        return service;
    }
}
