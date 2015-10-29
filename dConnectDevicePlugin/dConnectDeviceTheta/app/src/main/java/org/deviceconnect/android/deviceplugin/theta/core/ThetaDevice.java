package org.deviceconnect.android.deviceplugin.theta.core;


import java.util.List;

/**
 * THETA Device.
 * <p>
 * Provides APIs to access features of THETA Device.
 * </p>
 */
public interface ThetaDevice {

    /**
     * Gets model of this THETA device.
     *
     * @return model of this THETA Device
     */
    ThetaDeviceModel getModel();

    /**
     * Fetches a list of objects stored in this THETA device.
     *
     * @return a list of objects stored in this THETA device
     * @throws ThetaDeviceException if the API execution is failed.
     */
    List<ThetaObject> fetchAllObjectList() throws ThetaDeviceException;

    /**
     * Fetches a list of objects stored in this THETA device.
     *
     * @param offset the offset of the list
     * @param maxLength the maximum length of the list
     * @return a list of objects stored in this THETA device
     * @throws ThetaDeviceException if the API execution is failed.
     */
    List<ThetaObject> fetchObjectList(int offset, int maxLength) throws ThetaDeviceException;

    /**
     * Takes a picture by the camera of THETA device.
     *
     * @return an instance of {@link ThetaObject} of the picture
     * @throws ThetaDeviceException if the API execution is failed.
     */
    ThetaObject takePicture() throws ThetaDeviceException;

    /**
     * Starts video recording by the camera of THETA device.
     *
     * @see #stopVideoRecording()
     * @throws ThetaDeviceException if the API execution is failed.
     */
    void startVideoRecording() throws ThetaDeviceException;

    /**
     * Stops video recording explicitly.
     *
     * @throws ThetaDeviceException if the API execution is failed.
     */
    void stopVideoRecording() throws ThetaDeviceException;

    /**
     * Gets the battery level of THETA device.
     *
     * @return the battery level of THETA device
     * @throws ThetaDeviceException if the API execution is failed.
     */
    double getBatteryLevel() throws ThetaDeviceException;

}
