package org.deviceconnect.android.deviceplugin.theta;

import com.theta360.lib.PtpipInitiator;
import com.theta360.lib.ThetaException;
import com.theta360.lib.ptpip.entity.ObjectInfo;
import com.theta360.lib.ptpip.eventlistener.PtpipEventListener;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    };

    public void execute(final ThetaApiTask task) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                task.run(mThetaApi);
            }
        });
    }

}
