/*
 ThetaApiClient
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta;

import android.net.wifi.WifiInfo;

import com.theta360.lib.PtpipInitiator;
import com.theta360.lib.ThetaException;
import com.theta360.lib.ptpip.entity.ObjectHandles;
import com.theta360.lib.ptpip.entity.ObjectInfo;
import com.theta360.lib.ptpip.entity.PtpObject;
import com.theta360.lib.ptpip.eventlistener.PtpipEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Theta API Client.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaApiClient {

    private static final String IP_ADDRESS = "192.168.1.1";

    private static final String SERVICE_ID = "theta";

    private static final String MIMETYPE_PHOTO = "image/jpeg";

    private PtpipInitiator getInitiator() throws ThetaException, IOException {
        return new PtpipInitiator(IP_ADDRESS);
    }

    private final Logger mLogger = Logger.getLogger("theta.dplugin");

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private final ThetaApi mThetaApi = new ThetaApi() {

        @Override
        public double getBatteryLevel() throws ThetaException, IOException {
            return getInitiator().getBatteryLevel().getValue() / 100d;
        }

        @Override
        public void takePhoto(final ThetaPhotoEventListener listener) throws ThetaException, IOException {
            final CountDownLatch lockObj = new CountDownLatch(1);
            final PtpipInitiator initiator = getInitiator();
            final ThetaPhoto[] photo = new ThetaPhoto[1];
            if (BuildConfig.DEBUG) {
                initiator.setAudioVolume(0); // Mute the sound of shutter.
            }
            if (initiator.getStillCaptureMode() == PtpipInitiator.DEVICE_PROP_VALUE_UNDEFINED_CAPTURE_MODE) {
                throw new IllegalStateException("Theta's current mode is video mode.");
            }
            initiator.setStillCaptureMode(PtpipInitiator.DEVICE_PROP_VALUE_SINGLE_CAPTURE_MODE);
            initiator.initiateCapture(new PtpipEventListener() {
                @Override
                public void onObjectAdded(final int handle) {
                    try {
                        int w = RecorderInfo.PHOTO.mImageWidth;
                        int h = RecorderInfo.PHOTO.mImageHeight;
                        PtpObject obj = initiator.getResizedImageObject(handle, w, h);
                        byte[] data = obj.getDataObject();
                        ObjectInfo info = initiator.getObjectInfo(handle);
                        photo[0] = new ThetaPhoto(data, info.getFilename(), MIMETYPE_PHOTO, SERVICE_ID);
                    } catch (ThetaException e) {
                        e.printStackTrace();
                    }
                    lockObj.countDown();
                }
            });
            try {
                lockObj.await();
                if (photo[0] != null) {
                    listener.onPhoto(photo[0]);
                } else {
                    listener.onError();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean startVideoRecording() throws ThetaException, IOException {
            PtpipInitiator initiator = getInitiator();
            final int mode = initiator.getStillCaptureMode();
            if (mode != PtpipInitiator.DEVICE_PROP_VALUE_UNDEFINED_CAPTURE_MODE) {
                throw new IllegalStateException("Theta's current mode is not video mode.");
            }
            final short status = initiator.getCaptureStatus();
            if (status == PtpipInitiator.DEVICE_PROP_VALUE_CAPTURE_STATUS_CONTINUOUS_SHOOTING_RUNNING) {
                return false;
            }
            if (BuildConfig.DEBUG) {
                initiator.setAudioVolume(0); // Mute the sound of shutter.
            }
            initiator.initiateOpenCapture();
            return true;
        }

        @Override
        public boolean stopVideoRecording() throws ThetaException, IOException {
            PtpipInitiator initiator = getInitiator();
            final short status = initiator.getCaptureStatus();
            if (status == PtpipInitiator.DEVICE_PROP_VALUE_CAPTURE_STATUS_WAIT) {
                return false;
            }
            initiator.terminateOpenCapture();
            return true;
        }

        @Override
        public List<ThetaFileInfo> getFileInfoListFromDefaultStorage() throws ThetaException, IOException {
            List<ThetaFileInfo> result = new ArrayList<ThetaFileInfo>();

            PtpipInitiator initiator = getInitiator();
            ObjectHandles objectHandles = initiator.getObjectHandles(
                PtpipInitiator.PARAMETER_VALUE_DEFAULT,
                PtpipInitiator.PARAMETER_VALUE_DEFAULT,
                PtpipInitiator.PARAMETER_VALUE_DEFAULT);

            for (int i = 0; i < objectHandles.size(); i++) {
                int objectHandle = objectHandles.getObjectHandle(i);
                ObjectInfo objectInfo = initiator.getObjectInfo(objectHandle);
                String name = objectInfo.getFilename();
                String mimeType = MIMETYPE_PHOTO;
                String date = objectInfo.getCaptureDate();
                int size = objectInfo.getObjectCompressedSize();
                result.add(new ThetaFileInfo(name, mimeType, date, size, objectHandle));
            }

            return result;
        }

        @Override
        public byte[] getFile(final ThetaFileInfo info) throws ThetaException, IOException {
            PtpipInitiator initiator = getInitiator();
            byte[] data = initiator.getObject(info.mHandle);
            return data;
        }

        @Override
        public short getRecordingStatus() throws ThetaException, IOException {
            PtpipInitiator initiator = getInitiator();
            return initiator.getCaptureStatus();
        }

        @Override
        public boolean removeFileFromDefaultStorage(final String filename)
            throws ThetaException, IOException {
            PtpipInitiator initiator = getInitiator();
            ObjectHandles objectHandles = initiator.getObjectHandles(
                PtpipInitiator.PARAMETER_VALUE_DEFAULT,
                PtpipInitiator.PARAMETER_VALUE_DEFAULT,
                PtpipInitiator.PARAMETER_VALUE_DEFAULT);

            for (int i = 0; i < objectHandles.size(); i++) {
                int objectHandle = objectHandles.getObjectHandle(i);
                ObjectInfo objectInfo = initiator.getObjectInfo(objectHandle);
                String name = objectInfo.getFilename();
                if (name.equals(filename)) {
                    initiator.deleteObject(
                        objectHandle,
                        PtpipInitiator.PARAMETER_VALUE_DEFAULT);
                    return true;
                }
            }
            return false;
        }
    };

    private ThetaDeviceInfo mDeviceInfo;

    /**
     * Obtains the information of THETA which will be connected.
     *
     * @return the information of THETA which will be connected.
     */
    public synchronized ThetaDeviceInfo getDevice() {
        return mDeviceInfo;
    }

    /**
     * Obtains the information of THETA which will be connected.
     *
     * @param serviceId the ID of THETA
     * @return the information of THETA which will be connected.
     *         <code>null</code> is returned if the specified THETA is not set.
     */
    public synchronized ThetaDeviceInfo getDevice(final String serviceId) {
        if (mDeviceInfo == null) {
            return null;
        }
        if (!mDeviceInfo.mServiceId.equals(serviceId)) {
            return null;
        }
        return mDeviceInfo;
    }

    /**
     * Checks the specified THETA is set to this client.
     *
     * @param serviceId the ID of THETA
     * @return <code>true</code> if the specified THETA is set to this client, otherwise <code>false</code>
     */
    public boolean hasDevice(final String serviceId) {
        return getDevice(serviceId) != null;
    }

    public void fetchDevice(final WifiInfo wifiInfo) {
        final Object lockObj = this;
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    RecorderInfo recorderInfo;
                    short mode = getInitiator().getStillCaptureMode();
                    if (mode == PtpipInitiator.DEVICE_PROP_VALUE_UNDEFINED_CAPTURE_MODE) {
                        recorderInfo = RecorderInfo.VIDEO;
                    } else {
                        recorderInfo = RecorderInfo.PHOTO;
                    }
                    synchronized (lockObj) {
                        mDeviceInfo = new ThetaDeviceInfo(wifiInfo, recorderInfo);
                    }
                    PtpipInitiator.close();
                } catch (ThetaException e) {
                    // Nothing to do.
                } catch (IOException e) {
                    // Nothing to do.
                }
            }
        });
    }

    /**
     * Disposes the information of THETA.
     * <p>
     * Call this method when WiFi is disabled, or other WiFi access point is connected.
     * </p>
     */
    public synchronized void disposeDevice() {
        mDeviceInfo = null;
    }

    /**
     * Requests to execute the specified task.
     * <p>
     * {@link ThetaApiTask} will be executed on another thread.
     * </p>
     * @param task THETA API Task
     */
    public void execute(final ThetaApiTask task) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                task.run(mThetaApi);

                try {
                    PtpipInitiator.close();
                } catch (ThetaException e) {
                    // Nothing to do.
                }
            }
        });
    }

}
