/*
 GeolocationProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.profile.GeolocationProfileConstants;

/**
 * Geolocation Profile.
 *
 * <p>
 * API that provides a smart device geolocation operation function.<br>
 * Device plug-in that provides a geolocation operation function by extending this
 * class, and implements the corresponding API that.<br>
 * </p>
 *
 * @deprecated
 *  swagger定義ファイルで定数を管理することになったので、このクラスは使用しないこととする。
 *  プロファイルを実装する際は本クラスではなく、{@link DConnectProfile} クラスを継承すること。
 *
 * @author NTT DOCOMO, INC.
 */
public class GeolocationProfile extends DConnectProfile implements GeolocationProfileConstants {

    @Override
    public final String getProfileName() {
        return PROFILE_NAME;
    }

    /**
     * Set latitude to coordinates information.
     *
     * @param coordinates location information.
     * @param latitude latitude.
     */
    public static void setLatitude(final Bundle coordinates, final double latitude) {
        coordinates.putDouble(PARAM_LATITUDE, latitude);
    }

    /**
     * Set longitude to coordinates information.
     *
     * @param coordinates coordinates information.
     * @param longitude longitude.
     */
    public static void setLongitude(final Bundle coordinates, final double longitude) {
        coordinates.putDouble(PARAM_LONGITUDE, longitude);
    }

    /**
     * Set altitude to coordinates information.
     *
     * @param coordinates coordinates information.
     * @param altitude altitude.
     */
    public static void setAltitude(final Bundle coordinates, final double altitude) {
        coordinates.putDouble(PARAM_ALTITUDE, altitude);
    }

    /**
     * Set accuracy to coordinates information.
     *
     * @param coordinates coordinates information.
     * @param accuracy accuracy.
     */
    public static void setAccuracy(final Bundle coordinates, final double accuracy) {
        coordinates.putDouble(PARAM_ACCURACY, accuracy);
    }

    /**
     * Set altitudeAccuracy to coordinates information.
     *
     * @param coordinates coordinates information.
     * @param altitudeAccuracy altitudeAccuracy.
     */
    public static void setAltitudeAccuracy(final Bundle coordinates, final double altitudeAccuracy) {
        coordinates.putDouble(PARAM_ALTITUDE_ACCURACY, altitudeAccuracy);
    }

    /**
     * Set heading to coordinates information.
     *
     * @param coordinates coordinates information.
     * @param heading heading.
     */
    public static void setHeading(final Bundle coordinates, final double heading) {
        coordinates.putDouble(PARAM_HEADING, heading);
    }

    /**
     * Set speed to coordinates information.
     *
     * @param coordinates coordinates information.
     * @param speed speed.
     */
    public static void setSpeed(final Bundle coordinates, final double speed) {
        coordinates.putDouble(PARAM_SPEED, speed);
    }

    /**
     * Set coordinate information to coordinates object.
     *
     * @param coordinates coordinate object
     * @param location location information
     */
    public static void setCoordinates(final Bundle coordinates, final Bundle location) {
        coordinates.putBundle(PARAM_COORDINATES, location);
    }

    /**
     * Set timeStamp to position object.
     *
     * @param position position object.
     * @param timeStamp timeStamp.
     */
    public static void setTimeStamp(final Bundle position, final long timeStamp) {
        position.putLong(PARAM_TIME_STAMP, timeStamp);
    }

    /**
     * Set timeStampString to position object.
     *
     * @param position position object.
     * @param timeStampString timeStampString.
     */
    public static void setTimeStampString(final Bundle position, final String timeStampString) {
        position.putString(PARAM_TIME_STAMP_STRING, timeStampString);
    }

    /**
     * Set position information to position object.
     *
     * @param position position object
     * @param positionInfo position information
     */
    public static void setPosition(final Bundle position, final Bundle positionInfo) {
        position.putBundle(PARAM_POSITION, positionInfo);
    }

    /**
     * Get high accuracy parameter from request parameter.
     *
     * @param request request parameter.
     * @return true or flase.
     */
    public Boolean getHighAccuracy(final Intent request) {
        Boolean accurary = parseBoolean(request, PARAM_HIGH_ACCURACY);
        if (accurary == null) {
            return DEFAULT_HIGH_ACCURACY;
        } else {
            return accurary;
        }
    }

    /**
     * Get maximum age parameter from request parameter.
     *
     * @param request request parameter.
     * @return maximum age value.
     */
    public static double getMaximumAge(final Intent request) {
        Double maximumAge = parseDouble(request, PARAM_MAXIMUM_AGE);
        if (maximumAge == null) {
            return DEFAULT_MAXIMUM_AGE;
        } else {
            return maximumAge;
        }
    }

    /**
     * Get interval parameter from request parameter.
     *
     * @param request request parameter.
     * @return interval value.
     */
    public static double getInterval(final Intent request) {
        Double interval = parseDouble(request, PARAM_INTERVAL);
        if (interval == null) {
            return DEFAULT_INTERVAL;
        } else {
            return interval;
        }
    }
}
