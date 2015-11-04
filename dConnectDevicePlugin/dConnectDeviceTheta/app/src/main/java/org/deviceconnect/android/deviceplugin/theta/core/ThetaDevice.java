package org.deviceconnect.android.deviceplugin.theta.core;


import java.util.List;

/**
 * THETA Device.
 * <p>
 * Provides APIs to access features of THETA Device. These APIs return a value synchronously.
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
     * <p>
     * If THETA device has no object, this method returns a 0-length list.
     * </p>
     *
     * <p>
     * NOTE: An instance of {@link ThetaObject} which is returned by this method is not fetched.
     * If the binary data is needed, please call {@link ThetaObject#fetch()} to obtain the
     * binary data from the THETA device.
     * </p>
     *
     * @return a list of objects stored in this THETA device
     * @throws ThetaDeviceException if the API execution is failed.
     */
    List<ThetaObject> fetchAllObjectList() throws ThetaDeviceException;

    /**
     * Fetches a list of objects stored in this THETA device.
     *
     * <p>
     * If THETA device has no objectin in the specified range, this method returns a 0-length list.
     * </p>
     *
     * <p>
     * NOTE: An instance of {@link ThetaObject} which is returned by this method is not fetched.
     * If the binary data is needed, please call {@link ThetaObject#fetch()} to obtain the
     * binary data from the THETA device.
     * </p>
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
     * <p>
     * NOTE: An instance of {@link ThetaObject} which is returned by this method is not fetched.
     * If the binary data is needed, please call {@link ThetaObject#fetch()} to obtain the
     * binary data from the THETA device.
     * </p>
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
     * <p>
     * NOTE: An instance of {@link ThetaObject} which is returned by this method is not fetched.
     * If the binary data is needed, please call {@link ThetaObject#fetch()} to obtain the
     * binary data from the THETA device.
     * </p>
     *
     * @return an instance of {@link ThetaObject} of the video
     * @throws ThetaDeviceException if the API execution is failed.
     */
    ThetaObject stopVideoRecording() throws ThetaDeviceException;

    /**
     * Gets the battery level of THETA device.
     *
     * <p>
     * Range: 0.0 <= level <= 1.0
     * </p>
     *
     * @return the battery level of THETA device
     * @throws ThetaDeviceException if the API execution is failed.
     */
    double getBatteryLevel() throws ThetaDeviceException;

    /**
     * Changes shooting mode.
     *
     * @param mode shooting mode
     * @throws ThetaDeviceException if the API execution is failed.
     * @see ShootingMode
     */
    void changeShootingMode(ShootingMode mode) throws ThetaDeviceException;

    /**
     * Shooting mode.
     */
    enum ShootingMode {

        /**
         * Image shooting mode.
         */
        IMAGE,

        /**
         * Video shooting mode.
         */
        VIDEO

    }

}
