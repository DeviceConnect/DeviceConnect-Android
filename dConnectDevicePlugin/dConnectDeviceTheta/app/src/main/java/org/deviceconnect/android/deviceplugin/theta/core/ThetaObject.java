package org.deviceconnect.android.deviceplugin.theta.core;

/**
 * THETA Object.
 */
public interface ThetaObject {

    /**
     * Fetches the specified data from a THETA device.
     *
     * @param type type of data
     * @throws ThetaDeviceException if the API execution is failed.
     */
    void fetch(DataType type) throws ThetaDeviceException;

    /**
     * Checks whether the specified data is fetched from a THETA device or not.
     *
     * @param type type of data
     * @return <code>true</code> if the specified data is fetched from a THETA device.
     *      Otherwise <code>false</code>.
     */
    boolean isFetched(DataType type);

    /**
     * Removes this object from the storage of THETA device.
     *
     * @throws ThetaDeviceException if the API execution is failed.
     */
    void remove() throws ThetaDeviceException;

    /**
     * Removes fetched data from cache in an instance of this class.
     *
     * @param type type of data
     */
    void clear(DataType type);

    /**
     * Gets the MIME-Type of main data.
     *
     * @return the MIME-Type of main data
     */
    String getMimeType();

    /**
     * Gets the MIME-Type of main data.
     *
     * @return the MIME-Type of main data
     */
    Boolean isImage();

    /**
     * Gets the object creation date and time as {@link String}.
     *
     * <p>
     * Format: yyyy/MM/dd HH:mm:ss
     * </p>
     *
     * @return a string which indicates object creation date and time
     */
    String getCreationTime();

    /**
     * Gets the object creation date and time as Unix Time.
     *
     * @return unix time which indicates object creation date and time
     */
    long getCreationTimeWithUnixTime();

    /**
     * Gets the filename of main data.
     *
     * @return the filename of main data
     */
    String getFileName();

    /**
     * Gets the width of resolution.
     *
     * @return the width of resolution
     */
    Integer getWidth();

    /**
     * Gets the height of resolution.
     *
     * @return the height of resolution
     */
    Integer getHeight();

    /**
     * Gets the thumbnail of main data.
     *
     * <p>
     * <code>null</code> will be returned if you have not call {@link #fetch(DataType)}
     * with {@link DataType#THUMBNAIL}.
     * </p>
     *
     * @return the MIME-Type of main data
     */
    byte[] getThumbnailData();

    /**
     * Gets main data.
     *
     * <p>
     * <code>null</code> will be returned if you have not call {@link #fetch(DataType)}
     * with {@link DataType#MAIN}.
     * </p>
     *
     * @return the MIME-Type of main data
     */
    byte[] getMainData();

    /**
     * Type of data to be fetched from a THETA device.
     */
    enum DataType {

        /**
         * Thumbnail of main data.
         */
        THUMBNAIL,

        /**
         * Main data.
         */
        MAIN

    }

}
