/*
 HvcHumanDetectProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.profile;

import org.deviceconnect.android.profile.HumanDetectProfile;

import android.content.Intent;

/**
 * HVC HumanDetectProfile.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HvcHumanDetectProfile extends HumanDetectProfile {
    @Override
    protected boolean onPostDetection(final Intent request, final Intent response, final String serviceId,
            final int useFunc) {
        // TODO: 実装する
        return super.onPostDetection(request, response, serviceId, useFunc);
    }
}
