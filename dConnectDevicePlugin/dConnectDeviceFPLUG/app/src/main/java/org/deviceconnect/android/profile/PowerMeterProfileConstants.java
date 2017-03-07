/*
 PowerMeterProfileConstants.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
*/
package org.deviceconnect.android.profile;

import org.deviceconnect.profile.DConnectProfileConstants;

/**
 * Constants for PowerMeter profile.
 *
 * @author NTT DOCOMO, INC.
 */
public interface PowerMeterProfileConstants extends DConnectProfileConstants {

    /**
     * profile name.
     */
    String PROFILE_NAME = "powerMeter";

    /**
     * attribute : {@value} .
     */
    String ATTR_INTEGRATEDPOWER = "integratedPower";


    /**
     * attribute : {@value} .
     */
    String ATTR_INSTANTANEOUSPOWER = "instantaneousPower";

    /**
     * parameter : {@value} .
     */
    String PARAM_POWERSTATUS = "powerstatus";

    /**
     * parameter : {@value} .
     */
    String PARAM_DATE = "date";

    /**
     * parameter : {@value} .
     */
    String PARAM_INTEGRATEDPOWER = "integratedpower";

    /**
     * parameter : {@value} .
     */
    String PARAM_INSTANTANEOUSPOWER = "instantaneouspower";

}
