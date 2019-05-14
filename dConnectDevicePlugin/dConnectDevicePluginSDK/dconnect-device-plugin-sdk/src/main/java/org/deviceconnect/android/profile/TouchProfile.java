/*
 TouchProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.os.Bundle;

import org.deviceconnect.profile.TouchProfileConstants;

/**
 * Touch Profile.
 * 
 * <p>
 * API that provides a smart device touch operation function.<br>
 * Device plug-in that provides a touch operation function by extending this
 * class, and implements the corresponding API that.<br>
 * </p>
 *
 * @deprecated
 *  swagger定義ファイルで定数を管理することになったので、このクラスは使用しないこととする。
 *  プロファイルを実装する際は本クラスではなく、{@link DConnectProfile} クラスを継承すること。
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
