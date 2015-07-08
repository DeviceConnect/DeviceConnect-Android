package org.deviceconnect.android.deviceplugin.theta;

import com.theta360.lib.PtpipInitiator;
import com.theta360.lib.ThetaException;


import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Theta API Client.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaApiClient {

    private static final String IP_ADDRESS = "192.168.1.1";

    private PtpipInitiator getInitiator() throws ThetaException, IOException {
        return new PtpipInitiator(IP_ADDRESS);
    }

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private final ThetaApi mThetaApi = new ThetaApi() {

        @Override
        public double getBatteryLevel() throws ThetaException, IOException {
            try {
                return getInitiator().getBatteryLevel().getValue() / 100d;
            } finally {
                PtpipInitiator.close();
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
