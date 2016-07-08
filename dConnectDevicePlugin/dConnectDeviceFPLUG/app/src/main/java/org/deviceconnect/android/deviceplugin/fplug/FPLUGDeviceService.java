/*
 FPLUGDeviceService.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.fplug;

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
    protected SystemProfile getSystemProfile() {
        return new FPLUGSystemProfile();
    }

    @Override
    public void onAdded(final FPLUGController controller) {
        getServiceProvider().addService(new FPLUGService(controller.getAddress()));
    }

    @Override
    public void onConnected(final FPLUGController controller) {
        DConnectService service = getServiceProvider().getService(controller.getAddress());
        if (service != null) {
            service.setOnline(true);
        }
    }

    @Override
    public void onDisconnected(final FPLUGController controller) {
        DConnectService service = getServiceProvider().getService(controller.getAddress());
        if (service != null) {
            service.setOnline(false);
        }
    }
}
