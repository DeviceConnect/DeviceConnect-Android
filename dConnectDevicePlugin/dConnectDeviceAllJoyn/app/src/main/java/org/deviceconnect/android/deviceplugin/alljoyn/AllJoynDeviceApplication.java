package org.deviceconnect.android.deviceplugin.alljoyn;

import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import org.alljoyn.about.AboutService;
import org.alljoyn.about.AboutServiceImpl;
import org.alljoyn.bus.BusAttachment;
import org.alljoyn.bus.Mutable;
import org.alljoyn.bus.OnPingListener;
import org.alljoyn.bus.ProxyBusObject;
import org.alljoyn.bus.SessionListener;
import org.alljoyn.bus.SessionOpts;
import org.alljoyn.bus.Status;
import org.alljoyn.bus.Variant;
import org.alljoyn.bus.alljoyn.DaemonInit;
import org.alljoyn.services.common.AnnouncementHandler;
import org.alljoyn.services.common.BusObjectDescription;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AllJoynデバイスプラグインアプリケーショ。
 * 大域コンテキストを管理する。
 *
 * @author NTT DOCOMO, INC.
 */
public class AllJoynDeviceApplication extends Application {

    private static final String SERVICE_NAME = "DConnectAllJoyn";

    public static final String[] SINGLE_LAMP_INTERFACE_SET =
            new String[]{
                    // AllJoyn Lighting service framework, Lamp service
                    "org.allseen.LSF.LampDetails"
                    , "org.allseen.LSF.LampParameters"
                    , "org.allseen.LSF.LampService"
                    , "org.allseen.LSF.LampState"
            };
    public static final String[] LAMP_CONTROLLER_INTERFACE_SET =
            new String[]{
//                    // AllJoyn Lighting service framework, Controller Service
                    "org.allseen.LSF.ControllerService"
                    , "org.allseen.LSF.ControllerService.Lamp"
//                    , "org.allseen.LSF.ControllerService.LampGroup"
//                    , "org.allseen.LSF.ControllerService.Preset"
//                    , "org.allseen.LSF.ControllerService.Scene"
//                    , "org.allseen.LSF.ControllerService.MasterScene"
//                    , "org.allseen.LeaderElectionAndStateSync"
            };
    public static final String[][] SUPPORTED_INTERFACE_SETS = new String[][]{
            SINGLE_LAMP_INTERFACE_SET
            , LAMP_CONTROLLER_INTERFACE_SET
    };

    public static final int RESULT_OK = -1;
    public static final int RESULT_FAILED = 0;

    public static final String PARAM_RESULT_RECEIVER = "PARAM_RESULT_RECEIVER";
    public static final String PARAM_BUS_NAME = "PARAM_BUS_NAME";
    public static final String PARAM_PORT = "PARAM_PORT";
    public static final String PARAM_SESSION_ID = "PARAM_SESSION_ID";

    public static final int MSG_TYPE_INIT = 0;
    public static final int MSG_TYPE_DISCOVER = 1;
    public static final int MSG_TYPE_DESTROY = 2;
    public static final int MSG_TYPE_JOIN_SESSION = 3;
    public static final int MSG_TYPE_LEAVE_SESSION = 4;
    public static final int MSG_TYPE_PING = 5;

    static {
        // Load AllJoyn native libraries.
        System.loadLibrary("alljoyn_java");
    }

    private AllJoynHandler mAllJoynHandler;

    // TODO: 到達不可のリモートバス（サービス）を削除する機構。
    private Map<String, AllJoynServiceEntity> mAllJoynServiceEntities =
            Collections.synchronizedMap(new LinkedHashMap<String, AllJoynServiceEntity>());

    @Override
    public void onCreate() {
        super.onCreate();

        startLightClient();
    }

    public void startLightClient() {
        Log.d(getClass().getSimpleName(), "startLightClient");
        if (mAllJoynHandler == null) {
            HandlerThread busThread = new HandlerThread("AllJoynHandler");
            busThread.start();
            mAllJoynHandler = new AllJoynHandler(busThread.getLooper());
        }
        final Message msg = new Message();
        msg.what = MSG_TYPE_INIT;
        Bundle data = new Bundle();
        data.putParcelable(PARAM_RESULT_RECEIVER, new ResultReceiver() {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == RESULT_FAILED) {
                    Log.w(AllJoynDeviceApplication.class.getSimpleName(),
                            "AllJoyn init failed, retrying...");
                    // Resend
                    mAllJoynHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mAllJoynHandler.sendMessage(msg);
                        }
                    }, 5000);
                }
            }
        });
        msg.obj = data;
        mAllJoynHandler.sendMessage(msg);
    }

    public void performDiscovery() {
        final Message msg = new Message();
        msg.what = MSG_TYPE_DISCOVER;
        Bundle data = new Bundle();
        data.putParcelable(PARAM_RESULT_RECEIVER, new ResultReceiver() {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
            }
        });
        msg.obj = data;
        mAllJoynHandler.sendMessage(msg);
    }

    public Map<String, AllJoynServiceEntity> getDiscoveredAlljoynServices() {
        return new LinkedHashMap<>(mAllJoynServiceEntities);
    }

    public void joinSession(@NonNull String busName, short port,
                            @NonNull ResultReceiver resultReceiver) {
        final Message msg = new Message();
        msg.what = MSG_TYPE_JOIN_SESSION;
        Bundle data = new Bundle();
        data.putString(PARAM_BUS_NAME, busName);
        data.putShort(PARAM_PORT, port);
        data.putParcelable(PARAM_RESULT_RECEIVER, resultReceiver);
        msg.obj = data;
        mAllJoynHandler.sendMessage(msg);
    }

    public void leaveSession(int sessionId, @NonNull ResultReceiver resultReceiver) {
        final Message msg = new Message();
        msg.what = MSG_TYPE_LEAVE_SESSION;
        Bundle data = new Bundle();
        data.putInt(PARAM_SESSION_ID, sessionId);
        data.putParcelable(PARAM_RESULT_RECEIVER, resultReceiver);
        msg.obj = data;
        mAllJoynHandler.sendMessage(msg);
    }

    /**
     * Obtain a proxy to an AllJoyn interface on a service.
     * Through this proxy, properties, methods and signals of the service are accessed.
     *
     * @param serviceId  service ID
     * @param ifaceClass AllJoyn interface class
     * @param <T>        AllJoyn interface
     * @return a concrete AllJoyn object
     */
    public <T> T getInterface(@NonNull String serviceId, int sessionId, @NonNull Class<T> ifaceClass) {
        AllJoynServiceEntity service = mAllJoynServiceEntities.get(serviceId);
        if (service == null || service.proxyObjects == null) {
            return null;
        }

        // FIXME: 特定のAllJoyn I/Fを備えたBusオブジェクトが1サービスに複数あった場合の対処。
        // 当面、最初のオブジェクトのI/Fを返す。オブジェクト毎にサービスIDを発行し、別々に取り扱えるようにすべき？
        for (BusObjectDescription proxyObject : service.proxyObjects) {
            for (String iface : proxyObject.interfaces) {
                if (ifaceClass.getCanonicalName().equals(iface)) {
                    return mAllJoynHandler.getInterface(service.busName, proxyObject.path,
                            sessionId, ifaceClass);
                }
            }
        }
        return null;
    }

    /**
     * AllJoyn-related process handler.
     * Access to AllJoyn SDK goes through an instance of this class for thread safety.
     */
    // TODO: 非推奨のorg.alljoyn.about周辺のAPIから新しいorg.alljoyn.bus周辺のAPIへ移行する。
    private class AllJoynHandler extends Handler implements AnnouncementHandler {

        private static final int PING_TIMEOUT = 5000;
        private static final int PING_INTERVAL = 20000;
        private static final int DISCOVER_INTERVAL = 30000;

        private BusAttachment mBus;
        private AboutService mAboutService;
        private ScheduledExecutorService mPingTimer;
        private boolean mFirstTime = true;
        private ScheduledExecutorService mDiscoverTimer;

        public AllJoynHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.obj == null || !(msg.obj instanceof Bundle)) {
                return;
            }
            Bundle data = (Bundle) msg.obj;
            ResultReceiver resultReceiver = data.getParcelable(PARAM_RESULT_RECEIVER);
            if (resultReceiver == null) {
                return;
            }

            switch (msg.what) {
                case MSG_TYPE_INIT:
                    doInitAllJoynContext(resultReceiver);
                    break;
                case MSG_TYPE_DISCOVER:
                    doDiscover(resultReceiver);
                    break;
                case MSG_TYPE_DESTROY:
                    doDestroyAllJoynContext(resultReceiver);
                case MSG_TYPE_JOIN_SESSION: {
                    String sessionhostBusName = data.getString(PARAM_BUS_NAME);
                    if (sessionhostBusName == null) {
                        resultReceiver.send(RESULT_FAILED, null);
                        return;
                    }
                    if (!data.containsKey(PARAM_PORT)) {
                        resultReceiver.send(RESULT_FAILED, null);
                        return;
                    }
                    short sessionPort = data.getShort(PARAM_PORT);
                    doJoinSession(sessionhostBusName, sessionPort, resultReceiver);
                    break;
                }
                case MSG_TYPE_LEAVE_SESSION: {
                    if (!data.containsKey(PARAM_SESSION_ID)) {
                        resultReceiver.send(RESULT_FAILED, null);
                        return;
                    }
                    int sessionId = data.getInt(PARAM_SESSION_ID);
                    doLeaveSession(sessionId, resultReceiver);
                    break;
                }
                case MSG_TYPE_PING: {
                    String busName = data.getString(PARAM_BUS_NAME);
                    if (busName == null) {
                        resultReceiver.send(RESULT_FAILED, null);
                        return;
                    }
                    doPing(busName, resultReceiver);
                    break;
                }
            }
        }

        private boolean isSupported(@NonNull BusObjectDescription[] busObjects) {
            List<String> interfaces = new LinkedList<>();
            for (BusObjectDescription busObject : busObjects) {
                Collections.addAll(interfaces, busObject.interfaces);
            }

            // Each supported AllJoyn interface set represents a collection of required AllJoyn
            // interfaces to realize a certain DeviceConnect profile (e.g. AllJoyn Lamp service
            // interfaces are required for the DeviceConnect Light profile).
            // If AllJoyn bus object in question contains any of supported interface sets, then
            // assumedly this bus object is able to become a DeviceConect service.
            for (String[] supportedInterfaceSet : SUPPORTED_INTERFACE_SETS) {
                if (interfaces.containsAll(Arrays.asList(supportedInterfaceSet))) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void onAnnouncement(String busName, short port,
                                   BusObjectDescription[] busObjects,
                                   Map<String, Variant> aboutMap) {
            AllJoynServiceEntity service =
                    new AllJoynServiceEntity(busName, port, aboutMap, busObjects);

            Log.i(AllJoynHandler.this.getClass().getSimpleName(),
                    "Service found: " + service.serviceName);

            if (!isSupported(busObjects)) {
                Log.i(AllJoynHandler.this.getClass().getSimpleName(),
                        "Required I/Fs are missing. Ignoring \"" + service.serviceName + "\"");
                return;
            }

            mAllJoynServiceEntities.put(busName, service);
        }

        @Override
        public void onDeviceLost(String busName) {
            // Remove the service
            Log.d(AllJoynHandler.this.getClass().getSimpleName(),
                    "onDeviceLost received: device with busName: " + busName + " was lost");
            mAllJoynServiceEntities.remove(busName);
        }

        /**
         * Obtain a proxy to an AllJoyn interface on a service.
         * Through this proxy, properties, methods and signals of the service are accessed.
         *
         * @param busName    messaging bus name
         * @param objPath    object path
         * @param sessionId  session ID
         * @param ifaceClass AllJoyn interface class
         * @param <T>        AllJoyn interface
         * @return a concrete AllJoyn object
         */
        public <T> T getInterface(@NonNull String busName, @NonNull String objPath,
                                  int sessionId, @NonNull Class<T> ifaceClass) {
            ProxyBusObject proxyObj =
                    mBus.getProxyBusObject(busName, objPath, sessionId, new Class[]{ifaceClass});
            if (proxyObj == null) {
                return null;
            }
            return proxyObj.getInterface(ifaceClass);
        }

        /**
         * Initialize an AllJoyn context (message bus).
         *
         * @param resultReceiver
         */
        private void doInitAllJoynContext(@NonNull ResultReceiver resultReceiver) {
            Log.d(AllJoynHandler.this.getClass().getSimpleName(), "init");

            if (mBus == null) {
                DaemonInit.PrepareDaemon(getApplicationContext());
                mBus = new BusAttachment(getPackageName(), BusAttachment.RemoteMessage.Receive);

                Status status = mBus.connect();
                if (status != Status.OK) {
                    mBus = null;
                    resultReceiver.send(RESULT_FAILED, null);
                    return;
                }

                try {
                    mAboutService = AboutServiceImpl.getInstance();
                    mAboutService.startAboutClient(mBus);
                    mAboutService.addAnnouncementHandler(this, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                mPingTimer = Executors.newScheduledThreadPool(3);
                final ResultReceiver pingResultReceiver = new ResultReceiver() {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        if (resultData == null || !resultData.containsKey(PARAM_BUS_NAME)) {
                            Log.e(AllJoynHandler.this.getClass().getSimpleName(),
                                    "Logical error: ping result must be present.");
                            return;
                        }
                        String busName = resultData.getString(PARAM_BUS_NAME);
                        if (resultCode != RESULT_OK) {
                            Log.i(AllJoynHandler.this.getClass().getSimpleName(),
                                    "No ping from the service with bus name \"" + busName +
                                            "\". Removing it from discovered services...");
                            mAllJoynServiceEntities.remove(busName);
                        }
                    }
                };
                mPingTimer.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(AllJoynHandler.this.getClass().getSimpleName(),
                                "Sending pings to discovered services...");

                        for (AllJoynServiceEntity serviceEntity : mAllJoynServiceEntities.values()) {
                            Message msg = new Message();
                            msg.what = MSG_TYPE_PING;
                            Bundle data = new Bundle();
                            data.putString(PARAM_BUS_NAME, serviceEntity.busName);
                            data.putParcelable(PARAM_RESULT_RECEIVER, pingResultReceiver);
                            msg.obj = data;
                            AllJoynHandler.this.sendMessage(msg);
                        }
                    }
                }, 0, PING_INTERVAL, TimeUnit.MILLISECONDS);

                mDiscoverTimer = Executors.newSingleThreadScheduledExecutor();
                mDiscoverTimer.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        // FIXME: ***** HACK ***** Needs clean up or a better solution.
                        // About announcements of services that were powered off or nonresponsive to
                        // pings, somehow return as results of discovery (cached?).
                        // By initializing AboutService, announcement cache is cleared.
                        if (mAboutService != null) {
                            try {
                                mAboutService.stopAboutClient();
                                mAboutService = AboutServiceImpl.getInstance();
                                mAboutService.startAboutClient(mBus);
                                mAboutService.addAnnouncementHandler(AllJoynHandler.this, null);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        performDiscovery();
                    }
                }, 0, DISCOVER_INTERVAL, TimeUnit.MILLISECONDS);
            }
            resultReceiver.send(RESULT_OK, null);
        }

        /**
         * @param resultReceiver
         */
        private void doDestroyAllJoynContext(@NonNull ResultReceiver resultReceiver) {
            try {
                if (mPingTimer != null) {
                    mPingTimer.shutdownNow();
                    mPingTimer = null;
                }
                if (mAboutService != null) {
                    mAboutService.stopAboutClient();
                    mAboutService = null;
                }
                if (mBus != null) {
                    mBus.disconnect();
                    mBus = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                resultReceiver.send(RESULT_FAILED, null);
                return;
            }
            resultReceiver.send(RESULT_OK, null);
        }

        /**
         * Discover AllJoyn services with DeviceConnect-compatible interfaces.
         *
         * @param resultReceiver
         */
        private void doDiscover(@NonNull ResultReceiver resultReceiver) {
            Log.d(AllJoynHandler.this.getClass().getSimpleName(), "discover");

            if (!mFirstTime) {
                for (String[] ifaceSet : SUPPORTED_INTERFACE_SETS) {
                    for (String iface : ifaceSet) {
                        mBus.cancelWhoImplements(new String[]{iface});
                    }
                }
            } else {
                mFirstTime = false;
            }

            // To realize fine-grained API availability for DeviceConnect,
            // query each AllJoyn interface separately.
            for (String[] ifaceSet : SUPPORTED_INTERFACE_SETS) {
                for (String iface : ifaceSet) {
                    mBus.whoImplements(new String[]{iface});
                }
            }
        }

        /**
         * Join a messaging session hosted by a service.
         * Messaging session is specified by its hosting bus name and port.
         *
         * @param sessionHostBusName
         * @param sessionPort
         * @param resultReceiver
         */
        private void doJoinSession(@NonNull String sessionHostBusName, short sessionPort,
                                   @NonNull ResultReceiver resultReceiver) {
            Log.d(AllJoynHandler.this.getClass().getSimpleName(), "joinSession");

            SessionOpts sessionOpts = new SessionOpts();
            Mutable.IntegerValue sessionId = new Mutable.IntegerValue();

            Status status = mBus.joinSession(sessionHostBusName, sessionPort,
                    sessionId, sessionOpts, new SessionListener() {
                        @Override
                        public void sessionLost(int sessionId, int reason) {
                        }

                        @Override
                        public void sessionMemberAdded(int sessionId, String uniqueName) {
                        }

                        @Override
                        public void sessionMemberRemoved(int sessionId, String uniqueName) {
                        }
                    });

            if (status == Status.OK) {
                Bundle resultData = new Bundle();
                resultData.putInt(PARAM_SESSION_ID, sessionId.value);
                resultReceiver.send(RESULT_OK, resultData);
            } else {
                resultReceiver.send(RESULT_FAILED, null);
            }
        }

        private void doLeaveSession(int sessionId, @NonNull ResultReceiver resultReceiver) {
            Log.d(AllJoynHandler.this.getClass().getSimpleName(), "leaveSession");

            Status status = mBus.leaveSession(sessionId);
            if (status == Status.OK) {
                resultReceiver.send(RESULT_OK, null);
            } else {
                resultReceiver.send(RESULT_FAILED, null);
            }
        }

        private void doPing(final String busName, @NonNull final ResultReceiver resultReceiver) {
            Log.d(AllJoynHandler.this.getClass().getSimpleName(), "ping the service with bus name \"" + busName + "\"");

            final AtomicBoolean finished = new AtomicBoolean(false);
            final Bundle data = new Bundle();
            data.putString(PARAM_BUS_NAME, busName);
            Status pingStatus = mBus.ping(busName, PING_TIMEOUT, new OnPingListener() {
                @Override
                public void onPing(Status status, Object context) {
                    synchronized (finished) {
                        if (!finished.get()) {
                            if (status == Status.OK) {
                                resultReceiver.send(RESULT_OK, data);
                            } else {
                                resultReceiver.send(RESULT_FAILED, data);
                            }
                            finished.set(true);
                        }
                    }
                }
            }, null);
            if (pingStatus != Status.OK) {
                synchronized (finished) {
                    if (!finished.get()) {
                        resultReceiver.send(RESULT_FAILED, data);
                        finished.set(true);
                    }
                }
            }
        }

    }

    public class ResultReceiver extends android.os.ResultReceiver {
        public ResultReceiver() {
            super(mAllJoynHandler);
        }
    }

}
