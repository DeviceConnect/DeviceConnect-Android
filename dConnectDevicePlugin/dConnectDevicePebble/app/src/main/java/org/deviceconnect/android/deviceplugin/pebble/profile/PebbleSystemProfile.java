/*
 PebbleSystemProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.pebble.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.getpebble.android.kit.util.PebbleDictionary;

import org.deviceconnect.android.deviceplugin.pebble.PebbleDeviceService;
import org.deviceconnect.android.deviceplugin.pebble.setting.PebbleServiceListActivity;
import org.deviceconnect.android.deviceplugin.pebble.util.PebbleManager;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import java.util.logging.Logger;

/**
 * Pebbleデバイスプラグイン, System プロファイル.
 * @author NTT DOCOMO, INC.
 */
public class PebbleSystemProfile extends SystemProfile {
    /** debug log. */
    private Logger mLogger = Logger.getLogger("Pebble");

    private final DConnectApi mDeleteEventsApi = new DeleteApi() {
        @Override
        public String getAttribute() {
            return ATTRIBUTE_EVENTS;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            PebbleManager mgr = ((PebbleDeviceService) getContext()).getPebbleManager();
            mLogger.fine("onDeleteEvents delete system");
            // PebbleにEVENT解除依頼を送る
            sendDeleteEvent(PebbleManager.PROFILE_SYSTEM, PebbleManager.SYSTEM_ATTRIBUTE_EVENTS, mgr);
            // ここでイベントの解除をする
            String origin = request.getStringExtra(IntentDConnectMessage.EXTRA_ORIGIN);
            boolean isSuccess = EventManager.INSTANCE.removeEvents(origin);
            if (isSuccess) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setUnknownError(response, "Failed to remove events for origin = " + origin);
            }
            return true;
        }
    };

    public PebbleSystemProfile() {
        addApi(mDeleteEventsApi);
    }

    @Override
    protected Class<? extends Activity> getSettingPageActivity(final Intent request, final Bundle param) {
        return PebbleServiceListActivity.class;
    }

    /**
     * delete event を送る.
     * @param profile profile.
     * @param attribute attribute.
     * @param mgr PebbleManager
     */
    private void sendDeleteEvent(final int profile, final int attribute, final PebbleManager mgr) {
        PebbleDictionary dic = new PebbleDictionary(); 
        dic.addInt8(PebbleManager.KEY_PROFILE, (byte) profile);
        dic.addInt8(PebbleManager.KEY_ATTRIBUTE, (byte) attribute);
        dic.addInt8(PebbleManager.KEY_ACTION, (byte) PebbleManager.ACTION_DELETE);
        mgr.sendCommandToPebble(dic, null);
    }
}
