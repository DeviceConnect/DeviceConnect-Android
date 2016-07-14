/*
 MessageHookProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import org.deviceconnect.profile.MessageHookProfileConstants;

/**
 * MessageHook Profile.
 * @author NTT DOCOMO, INC.
 */
public class MessageHookProfile extends DConnectProfile implements MessageHookProfileConstants {

    @Override
    public final String getProfileName() {
        return PROFILE_NAME;
    }

}
