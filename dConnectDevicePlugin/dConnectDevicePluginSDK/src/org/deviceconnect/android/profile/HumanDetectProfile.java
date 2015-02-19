/*
 HumanDetectProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.profile.HumanDetectProfileConstants;

import android.content.Intent;

/**
 * Human Detect Profile.
 * 
 * <p>
 * API that provides Setting, the Detection feature for Human Detect Device.<br/>
 * 
 * DevicePlugin that provides a HumanDetect operation function of for smart device inherits an equivalent class, and implements the corresponding API thing. <br/>
 * </p>
 * 
 * <h1>API provides methods</h1>
 * <p>
 * For requests to each API of HumanDetectProfile, following callback method group is automatically invoked.<br/>
 * Subclasses override the methods for API provided by the DevicePlugin from the following methods group, to implement the functionality that.<br/>
 * Features that are not overridden automatically return the response as non-compliant API.
 * </p>
 * <ul>
 * <li>Human Detect API [POST] :
 * {@link HumanDetectProfile#detection(Intent, Intent, String, Byte[], double, double, String)}</li>
 * </ul>
 * @author NTT DOCOMO, INC.
 */
public abstract class HumanDetectProfile extends DConnectProfile implements HumanDetectProfileConstants {

	/**
	 * Constructor.
     */
    public HumanDetectProfile() {
    }

    @Override
    public final String getProfileName() {
        return PROFILE_NAME;
    }

    @Override
    protected boolean onPostRequest(final Intent request, final Intent response) {
        String attribute = getAttribute(request);
        boolean result = true;

        if (ATTRIBUTE_DETECTION.equals(attribute)) {
            String serviceId = getServiceID(request);
            int useFunc = getUseFunc(request);
            result = onPostDetection(request, response, serviceId, useFunc);
        } else {
            MessageUtils.setUnknownAttributeError(response);
        }

        return result;
    }

    /**
     * detection attribute request handler.<br/>
     * And ask the human body detection, and the result is stored in the response parameters.
     * If the response parameter is ready, please return true.
     * If you are not ready, please return false to start the process in the thread.
     * Once the thread is complete, send the response parameters.
     * @param request request parameter
     * @param response response parameter.
     * @param serviceId serviceID
     * @param useFunc use function flag.
     * @return send response flag.(true:sent / unsent (Send after the thread has been completed))
     */
    protected boolean onPostDetection(final Intent request, final Intent response, final String serviceId, final int useFunc) {
        setUnsupportedError(response);
        return true;
    }

    // ------------------------------------
    // Getter methods.
    // ------------------------------------
    
    /**
     * get use funcition flag from request.
     * 
     * @param request request parameter.
     * @return use funcition flag. if nothing, zero.
     */
    public static int getUseFunc(final Intent request) {
        return request.getIntExtra(PARAM_USE_FUNC, 0);
    }
}
