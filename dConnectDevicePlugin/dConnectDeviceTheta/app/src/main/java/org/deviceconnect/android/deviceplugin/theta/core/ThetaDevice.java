package org.deviceconnect.android.deviceplugin.theta.core;


import org.deviceconnect.android.deviceplugin.theta.core.preview.PreviewServer;
import org.deviceconnect.android.deviceplugin.theta.core.preview.PreviewServerProvider;

import java.util.List;

/**
 * THETA Device.
 * <p>
 * Provides APIs to access features of THETA Device. These APIs return a value synchronously.
 * </p>
 */
public interface ThetaDevice extends LiveCamera {

    /**
     * Gets the identifier of this THETA device.
     *
     * @return the identifier of this THETA device
     */
    String getId();

    /**
     * Gets the name of this THETA device.
     *
     * @return the name of this THETA device
     */
    String getName();

    /**
     * Gets the model of this THETA device.
     *
     * @return model of this THETA device
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
     * If the binary data is needed, please call {@link ThetaObject#fetch(ThetaObject.DataType)} } to obtain the
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
     * If THETA device has no object in the specified range, this method returns a 0-length list.
     * </p>
     *
     * <p>
     * NOTE: An instance of {@link ThetaObject} which is returned by this method is not fetched.
     * If the binary data is needed, please call {@link ThetaObject#fetch(ThetaObject.DataType)} to obtain the
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
     * If the binary data is needed, please call {@link ThetaObject#fetch(ThetaObject.DataType)} to obtain the
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
     * @throws ThetaDeviceException if the API execution is failed.
     */
    void stopVideoRecording() throws ThetaDeviceException;

    /**
     * Return maximum length of video recording as milliseconds.
     *
     * @return Maximum length of video recording as milliseconds
     */
    long getMaxVideoLength();

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
     * Gets current shooting mode.
     *
     * @return current shooting mode
     * @throws ThetaDeviceException if the API execution is failed.
     */
    ShootingMode getShootingMode() throws ThetaDeviceException;

    /**
     * Changes shooting mode.
     *
     * @param mode shooting mode
     * @throws ThetaDeviceException if the API execution is failed.
     * @see ShootingMode
     */
    void changeShootingMode(ShootingMode mode) throws ThetaDeviceException;

    /**
     * Gets the recorder information of this THETA device.
     *
     * @return the recorder information of this THETA device
     * @throws ThetaDeviceException if the API execution is failed.
     */
    Recorder getRecorder() throws ThetaDeviceException;
    /**
     * プレビュー配信サーバの管理クラスを取得します.
     *
     * @return プレビュー配信サーバ
     */
    PreviewServerProvider getServerProvider();
    /**
     * プレビュー配信サーバを起動します.
     *
     * @return 起動したプレビュー配信サーバのリスト
     */
    List<PreviewServer> startPreviews();

    /**
     * プレビュー配信サーバを停止します.
     */
    void stopPreviews();
    /**
     * Release objects or any other resources.
     */
    void destroy();

    /**
     * Shooting mode.
     */
    enum ShootingMode {

        /**
         * Image shooting mode (one-shot).
         */
        IMAGE,

        /**
         * Image shooting mode (interval).
         */
        IMAGE_INTERVAL,

        /**
         * Video shooting mode.
         */
        VIDEO,

        /**
         * Live streaming mode.
         */
        LIVE_STREAMING,

        /**
         * Unknown mode.
         */
        UNKNOWN

    }

    interface Recorder {

        String getId();

        String getName();

        String getMimeType();

        int getImageWidth();

        int getImageHeight();

        int getPreviewWidth();

        int getPreviewHeight();

        double getPreviewMaxFrameRate();

        boolean supportsPreview();

        boolean supportsVideoRecording();

        boolean supportsPhoto();

        RecorderState getState() throws ThetaDeviceException;

    }

    enum RecorderState {

        INACTIVE,

        RECORDING,

        UNKNOWN

    }

}
