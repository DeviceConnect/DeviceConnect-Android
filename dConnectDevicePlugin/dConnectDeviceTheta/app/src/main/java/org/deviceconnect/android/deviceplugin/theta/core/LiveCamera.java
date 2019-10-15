package org.deviceconnect.android.deviceplugin.theta.core;


import java.io.IOException;
import java.io.InputStream;

/**
 * Live Camera.
 */
public interface LiveCamera {

    /**
     * Gets an input stream of Live Stream.
     *
     * <p>
     * Format: MotionJPEG
     * </p>
     *
     * @return an input stream of Live Stream
     * @throws IOException if the API execution is failed.
     */
    InputStream getLiveStream() throws IOException;

}
