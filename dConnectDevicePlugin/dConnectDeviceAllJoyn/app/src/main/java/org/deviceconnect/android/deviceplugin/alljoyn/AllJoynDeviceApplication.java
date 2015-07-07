package org.deviceconnect.android.deviceplugin.alljoyn;

import android.app.Application;
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
import org.allseen.LSF.helper.LightingDirector;
import org.allseen.lsf.helper.listener.ControllerErrorEvent;
import org.allseen.lsf.helper.listener.ControllerListener;
import org.allseen.lsf.helper.manager.LightingSystemQueue;
import org.allseen.lsf.helper.model.ControllerDataModel;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * AllJoynデバイスプラグインアプリケーショ。
 * 大域コンテキストを管理する。
 *
 * @author NTT DOCOMO, INC.
 */
public class AllJoynDeviceApplication extends Application {

    // AllJoyネイティブライブラリを読み込む
    static {
        System.loadLibrary("gnustl_shared");
        System.loadLibrary("alljoyn_java");
        System.loadLibrary("alljoyn_lsf_java");
    }

    private static final String SERVICE_NAME = "DConnectAllJoyn";

    // TODO: 低レイヤーのBusAttachmenを用いた実装へ移行し、高レイヤーのLightingDirectorから脱却する。
    private LightingDirector mLightingDirector;
    private BusAttachment mBus;

    private final Object mIsReadyLock = new Object();
    private CountDownLatch mIsReady = new CountDownLatch(1);
    private LightingSystemQueue mQueue;
    private AboutListener mAboutListener;
    private List<ControllerDataModel> mLightControllers =
            Collections.synchronizedList(new LinkedList<ControllerDataModel>());

    @Override
    public void onCreate() {
        super.onCreate();

//        Debug.waitForDebugger();

        startLightClient();
    }

    private void startLightClient() {
        if (mLightingDirector == null) {
            mQueue = new LightingSystemQueue() {
                Handler handler = new Handler(Looper.getMainLooper());

                @Override
                public void post(Runnable r) {
                    handler.post(r);
                }

                @Override
                public void postDelayed(Runnable r, int delay) {
                    handler.postDelayed(r, delay);
                }
            };
            mLightingDirector = new LightingDirector(mQueue);
//            mLightingDirector.postOnNextControllerConnection(new Runnable() {
//                @Override
//                public void run() {
//
//                }
//            }, 5000);
            mLightingDirector.getLightingManager().getControllerManager().addListener(new ControllerListener() {
                @Override
                public void onLeaderModelChange(final ControllerDataModel controllerDataModel) {
                    Log.i("SHIGSHIG", controllerDataModel.toString());

                    if (!mLightControllers.contains(controllerDataModel)) {
                        mLightControllers.add(controllerDataModel);
                    }

                    if (mBus == null) {
                        mBus = mLightingDirector.getBusAttachment();
                        if (mBus == null) {
                            return;
                        }

                        synchronized (mIsReadyLock) {
                            AllJoynDeviceApplication.this.mIsReady.countDown();
                        }

                        if (mAboutListener == null) {
                            mAboutListener = new AboutListener() {
                                @Override
                                public void announced(String busName, int version, short port,
                                                      AboutObjectDescription[] aboutObjectDescriptions, Map<String, Variant> map) {
                                    Log.d("SHIGSHIG", "hello");

//                                    for (AboutObjectDescription dscr : aboutObjectDescriptions) {
//                                        dscr.interfaces
//                                    }
                                    Mutable.IntegerValue sessionID = new Mutable.IntegerValue();
                                    Status status = mBus.joinSession(busName, port, sessionID,
                                            new SessionOpts(), new SessionListener() {
                                                public void sessionLost(int sessionId, int reason) {
                                                }

                                                public void sessionMemberAdded(int sessionId, String uniqueName) {
                                                }

                                                public void sessionMemberRemoved(int sessionId, String uniqueName) {
                                                }
                                            });
                                    Log.i("SHIGSHIG", status.toString());
                                }
                            };
                            mBus.registerAboutListener(mAboutListener);
                        }

                        mQueue.post(new Runnable() {
                            @Override
                            public void run() {
                                // To query per-API availability info in a context of
                                // DeviceConnect, query each AllJoyn interface separately.
//                                mBus.whoImplements(new String[]{"org.allseen.LSF.LampDetails"});
//                                mBus.whoImplements(new String[]{"org.allseen.LSF.LampParameters"});
//                                mBus.whoImplements(new String[]{"org.allseen.LSF.LampService"});
//                                mBus.whoImplements(new String[]{"org.allseen.LSF.LampState"});

                                mBus.whoImplements(new String[]{"org.allseen.LSF.ControllerService"});
                                mBus.whoImplements(new String[]{"org.allseen.LSF.ControllerService.Lamp"});
                                mBus.whoImplements(new String[]{"org.allseen.LSF.ControllerService.LampGroup"});
                            }
                        });
                    }
                }

                @Override
                public void onControllerErrors(ControllerErrorEvent controllerErrorEvent) {

                }
            });
        } else {
            stopLightClient();
        }
        mLightingDirector.start(SERVICE_NAME);
    }

    private void stopLightClient() {
        synchronized (mIsReadyLock) {
            mIsReady = new CountDownLatch(1);
        }
        mLightingDirector.stop();
    }

    /**
     * デバイスプラグインの準備が整っているか否かを返す。
     *
     * @param timeoutMillisec タイムアウト時間（ミリ秒）
     * @return 準備が整っているなら<code>true</code>、さもなくば<code>false</code。
     */
    public boolean isReady(long timeoutMillisec) {
        synchronized (mIsReadyLock) {
            boolean result = false;
            try {
                result = mIsReady.await(timeoutMillisec, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ignore) {
            }
            return result;
        }
    }

    public LightingDirector getLightingDirector() {
        return mLightingDirector;
    }

    public List<ControllerDataModel> getDiscoveredControllers() {
        return mLightControllers;
    }

}
