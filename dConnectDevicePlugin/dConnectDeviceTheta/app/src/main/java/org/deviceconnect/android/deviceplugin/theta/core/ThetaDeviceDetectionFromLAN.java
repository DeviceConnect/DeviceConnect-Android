package org.deviceconnect.android.deviceplugin.theta.core;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class ThetaDeviceDetectionFromLAN extends AbstractThetaDeviceDetection {

    private static final String SERVICE_TYPE = "_osc._tcp";

    private final Logger mLogger = Logger.getLogger("theta.dplugin");

    private final List<ThetaDevice> mDetectedDevices = new ArrayList<>();

    private NsdManager mNsdManager;

    private boolean mIsStarted;

    private final NsdManager.DiscoveryListener mNsdDiscoveryListener = new NsdManager.DiscoveryListener() {
        @Override
        public void onStartDiscoveryFailed(final String serviceType, final int errorCode) {
            mLogger.warning("onStartDiscoveryFailed: serviceType=" + serviceType + ", errorCode=" + errorCode);
        }

        @Override
        public void onStopDiscoveryFailed(final String serviceType, final int errorCode) {
            mLogger.warning("onStopDiscoveryFailed: serviceType=" + serviceType + ", errorCode=" + errorCode);
        }

        @Override
        public void onDiscoveryStarted(final String serviceType) {
            mLogger.info("onDiscoveryStarted: serviceType=" + serviceType);
        }

        @Override
        public void onDiscoveryStopped(final String serviceType) {
            mLogger.info("onDiscoveryStopped: serviceType=" + serviceType);
        }

        @Override
        public void onServiceFound(final NsdServiceInfo serviceInfo) {
            mLogger.info("onServiceFound: serviceInfo=" + serviceInfo);
            if (serviceInfo != null) {
                NsdManager nsdManager = mNsdManager;
                if (nsdManager != null) {
                    nsdManager.resolveService(serviceInfo, mResolveListener);
                }
            }
        }

        @Override
        public void onServiceLost(final NsdServiceInfo serviceInfo) {
            mLogger.info("onServiceLost: serviceInfo=" + serviceInfo);

            ThetaDevice deleted = null;
            synchronized (mDetectedDevices) {
                for (Iterator<ThetaDevice> it = mDetectedDevices.iterator(); it.hasNext(); ) {
                    ThetaDevice device = it.next();
                    if (device.getName().equals(serviceInfo.getServiceName())) {
                        deleted = device;
                        it.remove();
                        break;
                    }
                }
            }
            if (deleted != null) {
                notifyOnThetaLost(deleted);
            }
        }
    };

    private final NsdManager.ResolveListener mResolveListener = new NsdManager.ResolveListener() {
        @Override
        public void onServiceResolved(final NsdServiceInfo serviceInfo) {
            mLogger.info("onServiceResolved: serviceInfo=" + serviceInfo);

            ThetaDevice device = ThetaDeviceFactory.createDeviceFromNsdServiceInfo(serviceInfo);
            if (device != null) {
                mLogger.info("Created THETA service: serviceInfo=" + serviceInfo);
                synchronized (mDetectedDevices) {
                    mDetectedDevices.add(device);
                }
                notifyOnThetaDetected(device);
            } else {
                mLogger.warning("Failed to create THETA service: serviceInfo=" + serviceInfo);
            }
        }

        @Override
        public void onResolveFailed(final NsdServiceInfo serviceInfo, final int errorCode) {
            mLogger.warning("onStopDiscoveryFailed: serviceInfo=" + serviceInfo + ", errorCode=" + errorCode);
        }
    };

    @Override
    public synchronized void start(final Context context) {
        if (mIsStarted) {
            return;
        }
        mIsStarted = true;
        mNsdManager = (NsdManager) context.getApplicationContext().getSystemService(Context.NSD_SERVICE);
        if (mNsdManager != null) {
            mNsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mNsdDiscoveryListener);
        }
    }

    @Override
    public synchronized void stop(final Context context) {
        if (!mIsStarted) {
            return;
        }
        mIsStarted = false;
        mNsdManager.stopServiceDiscovery(mNsdDiscoveryListener);
        mNsdManager = null;
    }

    @Override
    public List<ThetaDevice> getDetectedDevices() {
        synchronized (mDetectedDevices) {
            return new ArrayList<>(mDetectedDevices);
        }
    }
}
