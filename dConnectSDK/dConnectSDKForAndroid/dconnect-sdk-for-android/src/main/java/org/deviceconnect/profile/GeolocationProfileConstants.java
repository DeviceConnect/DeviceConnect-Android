/*
 GeolocationProfileConstants.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.profile;

/**
 * Constants for Geolocation Profile API.<br>
 * Define parameter name, interface name, attribute and profile name for
 * geolocation profile API.
 *
 * @deprecated swagger定義ファイルで定数を管理することになったので、このクラスは使用しないこととする。
 * @author NTT DOCOMO, INC.
 */
public interface GeolocationProfileConstants extends DConnectProfileConstants {

    /**
     * Profile name: {@value} .
     */
    String PROFILE_NAME = "geolocation";

    /**
     * Attribute: {@value} .
     */
    String ATTRIBUTE_CURRENT_POSITION = "currentPosition";

    /**
     * Attribute: {@value} .
     */
    String ATTRIBUTE_ON_WATCH_POSITION = "onWatchPosition";

    /**
     * Path: {@value} .
     */
    String PATH_PROFILE = PATH_ROOT + SEPARATOR + PROFILE_NAME;

    /**
     * Path: {@value} .
     */
    String PATH_GET_CURRENT_POSITION = PATH_ROOT + SEPARATOR + ATTRIBUTE_CURRENT_POSITION;

    /**
     * Path: {@value} .
     */
    String PATH_WATCH_POSITION = PATH_ROOT + SEPARATOR + ATTRIBUTE_ON_WATCH_POSITION;

    /**
     * Parameter: {@value} .
     */
    String PARAM_HIGH_ACCURACY = "highAccuracy";

    /**
     * Parameter: {@value} .
     */
    String PARAM_MAXIMUM_AGE = "maximumAge";

    /**
     * Parameter: {@value} .
     */
    String PARAM_INTERVAL = "interval";

    /**
     * Parameter: {@value} .
     */
    String PARAM_POSITION = "position";

    /**
     * Parameter: {@value} .
     */
    String PARAM_COORDINATES = "coordinates";

    /**
     * Parameter: {@value} .
     */
    String PARAM_LATITUDE = "latitude";

    /**
     * Parameter: {@value} .
     */
    String PARAM_LONGITUDE = "longitude";

    /**
     * Parameter: {@value} .
     */
    String PARAM_ALTITUDE = "altitude";

    /**
     * Parameter: {@value} .
     */
    String PARAM_ACCURACY = "accuracy";

    /**
     * Parameter: {@value} .
     */
    String PARAM_ALTITUDE_ACCURACY = "altitudeAccuracy";

    /**
     * Parameter: {@value} .
     */
    String PARAM_HEADING = "heading";

    /**
     * Parameter: {@value} .
     */
    String PARAM_SPEED = "speed";

    /**
     * Parameter: {@value} .
     */
    String PARAM_TIME_STAMP = "timeStamp";

    /**
     * Parameter: {@value} .
     */
    String PARAM_TIME_STAMP_STRING = "timeStampString";

    /**
     * Parameter: false.
     */
    Boolean DEFAULT_HIGH_ACCURACY = false;

    /**
     * Parameter: {@value} .
     */
    double DEFAULT_MAXIMUM_AGE = 0;

    /**
     * Parameter: {@value} .
     */
    double DEFAULT_INTERVAL = 1000;
}
