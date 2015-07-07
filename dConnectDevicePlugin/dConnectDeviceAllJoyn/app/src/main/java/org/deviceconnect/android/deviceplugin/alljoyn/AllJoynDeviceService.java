package org.deviceconnect.android.deviceplugin.alljoyn;

import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.alljoyn.bus.AboutListener;
import org.alljoyn.bus.AboutObjectDescription;
import org.alljoyn.bus.BusAttachment;
import org.alljoyn.bus.Mutable;
import org.alljoyn.bus.SessionListener;
import org.alljoyn.bus.SessionOpts;
import org.alljoyn.bus.Status;
import org.alljoyn.bus.Variant;
import org.allseen.lsf.helper.facade.LightingDirector;
import org.allseen.lsf.helper.manager.LightingSystemQueue;
import org.deviceconnect.android.deviceplugin.alljoyn.profile.AllJoynServiceDiscoveryProfile;
import org.deviceconnect.android.deviceplugin.alljoyn.profile.AllJoynSystemProfile;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.ServiceInformationProfile;
import org.deviceconnect.android.profile.SystemProfile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AllJoynデバイスプラグインサービス。
 *
 * @author NTT DOCOMO, INC.
 */
public class AllJoynDeviceService extends DConnectMessageService
//        implements AboutListener
{

    @Override
    public void onCreate() {
        super.onCreate();

        Debug.waitForDebugger();

        Log.d("SHIGSHIG", "debug");
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new AllJoynSystemProfile();
    }

    @Override
    protected ServiceInformationProfile getServiceInformationProfile() {
        return new ServiceInformationProfile(this) {
        };
    }

    @Override
    protected ServiceDiscoveryProfile getServiceDiscoveryProfile() {
        return new AllJoynServiceDiscoveryProfile(this);
    }

}
