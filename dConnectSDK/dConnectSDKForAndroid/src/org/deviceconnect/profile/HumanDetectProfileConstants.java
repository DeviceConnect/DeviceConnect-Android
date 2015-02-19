/*
 HumanDetectProfileConstants.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.profile;



/**
 * Human Detect Profile API constants.<br/>
 * Define the parameter name, interface name, attribute name, profile name of HumanDetectAPI .
 * 
 * @author NTT DOCOMO, INC.
 */
public interface HumanDetectProfileConstants extends DConnectProfileConstants {

    /**
     * HVC device plugin name prefix.
     */
    String DEVICE_NAME_PREFIX = "OMRON_HVC.*|omron_hvc.*";
    
    
    
    /**
     * profile nme: {@value} .
     */
    String PROFILE_NAME = "humandetect";

    /**
     * attribute: {@value} .
     */
    String ATTRIBUTE_SETTING = "setting";

    /**
     * attribute: {@value} .
     */
    String ATTRIBUTE_DETECTION = "detection";

    /**
     * path: {@value}.
     */
    String PATH_PROFILE = PATH_ROOT + SEPARATOR + PROFILE_NAME;

    /**
     * path: {@value} .
     */
    String PATH_SETTING = PATH_PROFILE + SEPARATOR + ATTRIBUTE_SETTING;
    
    /**
     * path: {@value} .
     */
    String PATH_DETECTION = PATH_PROFILE + SEPARATOR + ATTRIBUTE_DETECTION;
    
    /** 
     * parameter: {@value} .
     */
    String PARAM_USE_FUNC = "useFunc";
}
