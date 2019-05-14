/*
 KeyEventProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.os.Bundle;

import org.deviceconnect.profile.KeyEventProfileConstants;

/**
 * Key Event Profile.
 * 
 * <p>
 * API that provides a smart device key event operation function.<br>
 * Device plug-in that provides a button operation function by extending this
 * class, and implements the corresponding API that.<br>
 * </p>
 *
 * @deprecated
 *  swagger定義ファイルで定数を管理することになったので、このクラスは使用しないこととする。
 *  プロファイルを実装する際は本クラスではなく、{@link DConnectProfile} クラスを継承すること。
 *
 * @author NTT DOCOMO, INC.
 */
public class KeyEventProfile extends DConnectProfile implements KeyEventProfileConstants {

    @Override
    public final String getProfileName() {
        return PROFILE_NAME;
    }

    // ------------------------------------
    // Message setter method group
    // ------------------------------------

    /**
     * Set key event information to key event object.
     * 
     * @param keyeventobject key event object
     * @param keyevent key event information
     */
    public static void setKeyevent(final Bundle keyeventobject, final Bundle keyevent) {
        keyeventobject.putBundle(PARAM_KEYEVENT, keyevent);
    }

    /**
     * Set the identification number to key event information.
     * 
     * @param keyevent key event information
     * @param id identification number
     */
    public static void setId(final Bundle keyevent, final int id) {
        keyevent.putInt(PARAM_ID, id);
    }

    /**
     * Set key configure to key event information.
     * 
     * @param keyevent key event information
     * @param config key configure
     */
    public static void setConfig(final Bundle keyevent, final String config) {
        keyevent.putString(PARAM_CONFIG, config);
    }

}
