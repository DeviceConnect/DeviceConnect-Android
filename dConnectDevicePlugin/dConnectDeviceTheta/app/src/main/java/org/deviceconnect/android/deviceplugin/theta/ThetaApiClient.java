package org.deviceconnect.android.deviceplugin.theta;

import android.util.Log;

import com.theta360.lib.PtpipInitiator;
import com.theta360.lib.ThetaException;
import com.theta360.lib.ptpip.entity.ObjectHandles;
import com.theta360.lib.ptpip.entity.ObjectInfo;
import com.theta360.lib.ptpip.entity.StorageIds;
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
                initiator.setAudioVolume(1); // Mute the sound of shutter.
            }
            if (initiator.getStillCaptureMode() == PtpipInitiator.DEVICE_PROP_VALUE_UNDEFINED_CAPTURE_MODE) {
                throw new IllegalStateException("Theta's current mode is video mode.");
            }
            initiator.setStillCaptureMode(PtpipInitiator.DEVICE_PROP_VALUE_SINGLE_CAPTURE_MODE);
            initiator.initiateCapture(new PtpipEventListener() {
                @Override
                public void onObjectAdded(final int handle) {
                    try {
                        byte[] data = initiator.getObject(handle);
                        ObjectInfo info = initiator.getObjectInfo(handle);
                        photo[0] = new ThetaPhoto(data, SERVICE_ID, info.getFilename(), MIMETYPE_PHOTO);
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
        public void startVideoRecording() throws ThetaException, IOException {
            PtpipInitiator initiator = getInitiator();
            final int mode = initiator.getStillCaptureMode();
            if (mode != PtpipInitiator.DEVICE_PROP_VALUE_UNDEFINED_CAPTURE_MODE) {
                throw new IllegalStateException("Theta's current mode is not video mode.");
            }
            if (BuildConfig.DEBUG) {
                initiator.setAudioVolume(1); // Mute the sound of shutter.
            }
            initiator.initiateOpenCapture();
        }

        @Override
        public void stopVideoRecording() throws ThetaException, IOException {
            PtpipInitiator initiator = getInitiator();
            initiator.terminateOpenCapture();
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
                int size = objectInfo.getObjectCompressedSize();
                result.add(new ThetaFileInfo(name, mimeType, size, objectHandle));
            }

            return result;
        }

        @Override
        public byte[] getFile(final ThetaFileInfo info) throws ThetaException, IOException {
            PtpipInitiator initiator = getInitiator();
            byte[] data = initiator.getObject(info.mHandle);
            return data;
        }
    };

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
