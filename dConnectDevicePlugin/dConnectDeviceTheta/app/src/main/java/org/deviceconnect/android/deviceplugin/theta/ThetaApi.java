/*
 ThetaApi
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta;

import com.theta360.lib.ThetaException;

import java.io.IOException;
import java.util.List;

/**
 * Theta API.
 *
 * @author NTT DOCOMO, INC.
 */
public interface ThetaApi {

    /**
     * Obtains the current battery level.
     *
     * @return the current battery level. The range is 0 .. 1.0.
     *         The value of 0 indicates the battery is empty.
     *         The value of 1.0 indicates the battery is full.
     * @throws ThetaException if an error is occurred when this API is executed on THETA
     * @throws IOException if a communication between THETA and Android device is failed
     */
    double getBatteryLevel() throws ThetaException, IOException;

    /**
     * Takes a photo.
     *
     * @param listener an instance of {@link ThetaPhotoEventListener}
     * @throws ThetaException if an error is occurred when this API is executed on THETA
     * @throws IOException if a communication between THETA and Android device is failed
     */
    void takePhoto(ThetaPhotoEventListener listener) throws ThetaException, IOException;

    /**
     * Requests to start video recording.
     *
     * @throws ThetaException if an error is occurred when this API is executed on THETA
     * @throws IOException if a communication between THETA and Android device is failed
     */
    void startVideoRecording() throws ThetaException, IOException;

    /**
     * Requests to stop video recording.
     *
     * @throws ThetaException if an error is occurred when this API is executed on THETA
     * @throws IOException if a communication between THETA and Android device is failed
     */
    void stopVideoRecording() throws ThetaException, IOException;

    /**
     * Obtains the list of file information in the default storage of THETA.
     *
     * @return the list of file information in the default storage of THETA.
     * @throws ThetaException if an error is occurred when this API is executed on THETA
     * @throws IOException if a communication between THETA and Android device is failed
     */
    List<ThetaFileInfo> getFileInfoListFromDefaultStorage() throws ThetaException, IOException;

    /**
     * Obtains the binary data of the specified file in the default storage of THETA.
     *
     * NOTE: {@link java.lang.OutOfMemoryError} will occur if too many bytes of data (e.g. video) is requested.
     *
     * @param info the information of file
     * @return the binary data of the specified file
     * @throws ThetaException if an error is occurred when this API is executed on THETA
     * @throws IOException if a communication between THETA and Android device is failed
     */
    byte[] getFile(ThetaFileInfo info) throws ThetaException, IOException;
}
