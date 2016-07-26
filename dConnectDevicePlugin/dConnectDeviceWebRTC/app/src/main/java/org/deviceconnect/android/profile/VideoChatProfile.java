/*
 VideoChatProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

/**
 * VideoChat Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class VideoChatProfile extends DConnectProfile implements VideoChatProfileConstants {

    @Override
    public String getProfileName() {
        return PROFILE_NAME;
    }

}
