/*
 AvailabilityProfileConstants.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.profile;

/**
 * Constants for Availability Profile API.<br/>
 * Define profile name for Availability Profile API.
 * 
 * @author NTT DOCOMO, INC.
 */
public interface AvailabilityProfileConstants extends DConnectProfileConstants {

    /**
     * Profile name: {@value} .
     */
    String PROFILE_NAME = "availability";

    /**
     * Path: {@value}.
     */
    String PATH_PROFILE = PATH_ROOT + SEPARATOR + PROFILE_NAME;

}
