/*
 TouchProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.profile.TouchProfileConstants;

import android.content.Intent;
import android.os.Bundle;

/**
 * Touch Profile.
 * 
 * <p>
 * API that provides a smart device touch operation function.<br>
 * Device plug-in that provides a touch operation function by extending this
 * class, and implements the corresponding API that.<br>
 * </p>
 * 
 * <h1>API provides methods</h1>
 * <p>
 * The request to each API of Touch Profile, following callback method group is
 * automatically invoked.<br>
 * Subclass is to implement the functionality by overriding the method for the
 * API provided by the device plug-in from the following methods group.<br>
 * Features that are not overridden automatically return the response as
 * non-compliant API.
 * </p>
 * <ul>
 * <li>Touch API [GET] :
 * {@link TouchProfile#onGetOnTouch(Intent, Intent, String)}</li>
 * <li>Touch Start API [GET] :
 * {@link TouchProfile#onGetOnTouchStart(Intent, Intent, String)}</li>
 * <li>Touch End API [GET] :
 * {@link TouchProfile#onGetOnTouchEnd(Intent, Intent, String)}</li>
 * <li>Double Tap API [GET] :
 * {@link TouchProfile#onGetOnDoubleTap(Intent, Intent, String)}</li>
 * <li>Touch Move API [GET] :
 * {@link TouchProfile#onGetOnTouchMove(Intent, Intent, String)}</li>
 * <li>Touch Cancel API [GET] :
 * {@link TouchProfile#onGetOnTouchCancel(Intent, Intent, String)}</li>
 * <li>Touch Event API [Register] :
 * {@link TouchProfile#onPutOnTouch(Intent, Intent, String, String)}</li>
 * <li>Touch API [Unregister] :
 * {@link TouchProfile#onDeleteOnTouch(Intent, Intent, String, String)}</li>
 * <li>Touch Start Event API [Register] :
 * {@link TouchProfile#onPutOnTouchStart(Intent, Intent, String, String)}</li>
 * <li>Touch Start API [Unregister] :
 * {@link TouchProfile#onDeleteOnTouchStart(Intent, Intent, String, String)}</li>
 * <li>Touch End Event API [Register] :
 * {@link TouchProfile#onPutOnTouchEnd(Intent, Intent, String, String)}</li>
 * <li>Touch End API [Unregister] :
 * {@link TouchProfile#onDeleteOnTouchEnd(Intent, Intent, String, String)}</li>
 * <li>Double Tap Event API [Register] :
 * {@link TouchProfile#onPutOnDoubleTap(Intent, Intent, String, String)}</li>
 * <li>Double Tap API [Unregister] :
 * {@link TouchProfile#onDeleteOnDoubleTap(Intent, Intent, String, String)}</li>
 * <li>Touch Move Event API [Register] :
 * {@link TouchProfile#onPutOnTouchMove(Intent, Intent, String, String)}</li>
 * <li>Touch Move API [Unregister] :
 * {@link TouchProfile#onDeleteOnTouchMove(Intent, Intent, String, String)}</li>
 * <li>Touch Cancel Event API [Register] :
 * {@link TouchProfile#onPutOnTouchCancel(Intent, Intent, String, String)}</li>
 * <li>Touch Cancel API [Unregister] :
 * {@link TouchProfile#onDeleteOnTouchCancel(Intent, Intent, String, String)}</li>
 * </ul>
 * 
 * @author NTT DOCOMO, INC.
 */
public class TouchProfile extends DConnectProfile implements TouchProfileConstants {

    @Override
    public final String getProfileName() {
        return PROFILE_NAME;
    }

    // ------------------------------------
    // Message setter method group
    // ------------------------------------

    /**
     * Set touch information to touch object.
     * 
     * @param touchobject touch object.
     * @param touch touch information.
     */
    public static void setTouch(final Bundle touchobject, final Bundle touch) {
        touchobject.putBundle(PARAM_TOUCH, touch);
    }

    /**
     * Set touch coordinate information to Touch information.
     * 
     * @param touch touch information.
     * @param touches touch coordinate information.
     */
    public static void setTouches(final Bundle touch, final Bundle touches) {
        touch.putBundle(PARAM_TOUCHES, touches);
    }

    /**
     * Set the identification number to touch coordinate information.
     * 
     * @param touches touch coordinate information.
     * @param id identification number.
     */
    public static void setId(final Bundle touches, final int id) {
        touches.putInt(PARAM_ID, id);
    }

    /**
     * Set the X coordinates to touch coordinate information.
     * 
     * @param touches touch coordinate information.
     * @param x X coordinate.
     */
    public static void setX(final Bundle touches, final double x) {
        touches.putDouble(PARAM_X, x);
    }

    /**
     * Set the Y coordinates to touch coordinate information.
     * 
     * @param touches touch coordinate information.
     * @param y Y coordinate.
     */
    public static void setY(final Bundle touches, final double y) {
        touches.putDouble(PARAM_Y, y);
    }
}
