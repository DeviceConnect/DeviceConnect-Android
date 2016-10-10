/*
 AccelerationData
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hitoe.data;

import android.os.Bundle;

import org.deviceconnect.android.profile.DeviceOrientationProfile;

/**
 * This class is information of Acceleration.
 * @author NTT DOCOMO, INC.
 */
public class AccelerationData {
    /** Acceleration X. */
    private double mAccelX;
    /** Acceleration Y. */
    private double mAccelY;
    /** Acceleration Z. */
    private double mAccelZ;
    /** Acceleration Gravity X. */
    private double mGravityX;
    /** Acceleration Gravity Y. */
    private double mGravityY;
    /** Acceleration Gravity Z. */
    private double mGravityZ;
    /** Gyro X. */
    private double mGyroAlpha;
    /** Gyro Y. */
    private double mGyroBeta;
    /** Gyro Z. */
    private double mGyroGamma;
    /** TimeStamp. */
    private long mInterval;

    /**
     * Constructor.
     */
    public AccelerationData() {
        mInterval = 0;
    }


    /**
     * Get Acceleration X.
     * @return Acceleration X
     */
    public double getAccelX() {
        return mAccelX;
    }

    /**
     * Set Acceleration X.
     * @param accelX Acceleration X
     */
    public void setAccelX(final double accelX) {
        mAccelX = accelX;
    }

    /**
     * Get Acceleration Y.
     * @return Acceleration Y
     */
    public double getAccelY() {
        return mAccelY;
    }

    /**
     * Set Acceleration Y.
     * @param accelY Acceleration Y
     */
    public void setAccelY(final double accelY) {
        mAccelY = accelY;
    }

    /**
     * Get Acceleration Z.
     * @return Acceleration Z
     */
    public double getAccelZ() {
        return mAccelZ;
    }

    /**
     * Set Acceleration Z.
     * @param accelZ Acceleration Z
     */
    public void setAccelZ(final double accelZ) {
        mAccelZ = accelZ;
    }

    /**
     * Get Acceleration Gravity X.
     * @return Acceleration Gravity X
     */
    public double getGravityX() {
        return mGravityX;
    }

    /**
     * Set Acceleration Gravity X.
     * @param gravityX Acceleration gravity X
     */
    public void setGravityX(final double gravityX) {
        mGravityX = gravityX;
    }

    /**
     * Get Acceleration Gravity Y.
     * @return Acceleration Gravity Y
     */
    public double getGravityY() {
        return mGravityY;
    }

    /**
     * Set Acceleration Gravity Y.
     * @param gravityY Acceleration Gravity Y
     */
    public void setGravityY(final double gravityY) {
        mGravityY = gravityY;
    }

    /**
     * Get Acceleration Gravity Z.
     * @return Acceleration Gravity Z
     */
    public double getGravityZ() {
        return mGravityZ;
    }

    /**
     * Set Acceleration Gravity Z.
     * @param gravityZ Acceleration Gravity Z
     */
    public void setGravityZ(final double gravityZ) {
        mGravityZ = gravityZ;
    }

    /**
     * Get Gyro Alpha.
     * @return Gyro Alpha
     */
    public double getGyroAlpha() {
        return mGyroAlpha;
    }

    /**
     * Set Gyro alpha.
     * @param gyroAlpha Gyro Alpha
     */
    public void setGyroAlpha(final double gyroAlpha) {
        mGyroAlpha = gyroAlpha;
    }

    /**
     * Get Gyro Beta.
     * @return Gyro Beta
     */
    public double getGyroBeta() {
        return mGyroBeta;
    }

    /**
     * Set Gyro Beta.
     * @param gyroBeta Gyro Beta
     */
    public void setGyroBeta(final double gyroBeta) {
        mGyroBeta = gyroBeta;
    }

    /**
     * Get Gyro Gamma.
     * @return Gyro Gamma
     */
    public double getGyroGamma() {
        return mGyroGamma;
    }

    /**
     * Set Gyro Gamma.
     * @param gyroGamma Gyro Gamma
     */
    public void setGyroGamma(final double gyroGamma) {
        mGyroGamma = gyroGamma;
    }

    /**
     * Get TimeStamp.
     * @return timestamp
     */
    public long getTimeStamp() {
        return mInterval;
    }

    /**
     * Set TimeStamp.
     * @param timeStamp timestamp
     */
    public void setTimeStamp(final long timeStamp) {
        mInterval = timeStamp;
    }

    /**
     * To Bundle.
     * @return bundle
     */
    public Bundle toBundle() {
        Bundle orientation = new Bundle();

        Bundle ag = new Bundle();
        DeviceOrientationProfile.setX(ag, mAccelX * 9.81);
        DeviceOrientationProfile.setY(ag, mAccelY * 9.81);
        DeviceOrientationProfile.setZ(ag, mAccelZ * 9.81);

        DeviceOrientationProfile.setAcceleration(orientation, ag);
        DeviceOrientationProfile.setInterval(orientation, mInterval);
        return orientation;
    }
}
