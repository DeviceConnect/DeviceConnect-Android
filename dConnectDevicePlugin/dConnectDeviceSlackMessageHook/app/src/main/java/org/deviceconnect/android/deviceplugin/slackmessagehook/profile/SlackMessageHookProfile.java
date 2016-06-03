/*
 SlackMessageHookProfile.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.slackmessagehook.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.slackmessagehook.slack.SlackManager;
import org.deviceconnect.android.profile.MessageHookProfile;

/**
 * Bot プロファイルの実装クラス.
 * @author docomo
 */
public class SlackMessageHookProfile extends MessageHookProfile {

    @Override
    protected boolean onGetChannel(Intent request, Intent response, String serviceId) {
        return true;
    }

    @Override
    protected boolean onPostMessage(Intent request, Intent response, String serviceId, String channel, String text, String resource, String mimeType) {
        SlackManager.INSTANCE.sendMessage(text, channel);
        return true;
    }

    @Override
    protected boolean onPutOnMessageReceived(Intent request, Intent response, String serviceId, String sessionKey) {
        return super.onPutOnMessageReceived(request, response, serviceId, sessionKey);
    }

    @Override
    protected boolean onDeleteOnMessageReceived(Intent request, Intent response, String serviceId, String sessionKey) {
        return super.onDeleteOnMessageReceived(request, response, serviceId, sessionKey);
    }
}
