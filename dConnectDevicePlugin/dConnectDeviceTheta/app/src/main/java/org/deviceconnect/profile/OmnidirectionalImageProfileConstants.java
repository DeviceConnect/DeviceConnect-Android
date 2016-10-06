/*
 OmnidirectionalImageProfileConstants.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.profile;

/**
 * Constants for Omnidirectional Image API.<br/>
 * Define parameter name, interface name, attribute and profile name for omnidirectional image
 * profile API.
 *
 * @author NTT DOCOMO, INC.
 */
public interface OmnidirectionalImageProfileConstants extends DConnectProfileConstants {

    /**
     * Profile name: {@value} .
     */
    String PROFILE_NAME = "omnidirectionalImage";

    /**
     * Interface name: {@value} .
     */
    String INTERFACE_ROI = "roi";

    /**
     * Attribute: {@value} .
     */
    String ATTRIBUTE_ROI = "roi";

    /**
     * Attribute: {@value} .
     */
    String ATTRIBUTE_SETTINGS = "settings";

    /**
     * Path: {@value} .
     */
    String PATH_PROFILE = PATH_ROOT + SEPARATOR + PROFILE_NAME;

    /**
     * Path: {@value} .
     */
    String PATH_ROI_VIEW = PATH_PROFILE + SEPARATOR + ATTRIBUTE_ROI;

    /**
     * Path: {@value} .
     */
    String PATH_ROI_VIEW_SETTINGS = PATH_PROFILE + SEPARATOR + INTERFACE_ROI + SEPARATOR
        + ATTRIBUTE_SETTINGS;

    /**
     * Parameter: {@value} .
     */
    String PARAM_SOURCE = "source";

    /**
     * Parameter: {@value} .
     */
    String PARAM_X = "x";

    /**
     * Parameter: {@value} .
     */
    String PARAM_Y = "y";

    /**
     * Parameter: {@value} .
     */
    String PARAM_Z = "z";

    /**
     * Parameter: {@value} .
     */
    String PARAM_ROLL = "roll";

    /**
     * Parameter: {@value} .
     */
    String PARAM_PITCH = "pitch";

    /**
     * Parameter: {@value} .
     */
    String PARAM_YAW = "yaw";

    /**
     * Parameter: {@value} .
     */
    String PARAM_FOV = "fov";

    /**
     * Parameter: {@value} .
     */
    String PARAM_SPHERE_SIZE = "sphereSize";

    /**
     * Parameter: {@value} .
     */
    String PARAM_WIDTH = "width";

    /**
     * Parameter: {@value} .
     */
    String PARAM_HEIGHT = "height";

    /**
     * Parameter: {@value} .
     */
    String PARAM_STEREO = "stereo";

    /**
     * Parameter: {@value} .
     */
    String PARAM_VR = "vr";

    /**
     * Parameter: {@value} .
     */
    String PARAM_OUTPUT = "output";

}
