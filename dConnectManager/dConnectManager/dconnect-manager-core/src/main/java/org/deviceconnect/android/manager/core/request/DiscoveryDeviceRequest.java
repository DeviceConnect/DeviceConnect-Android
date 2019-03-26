/*
 DiscoveryDeviceRequest.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.core.request;

import android.content.Intent;

import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;

import java.util.List;

/**
 * デバイスが発見されてdConnectManagerに通知があった場合にClientやAccessTokenを作成するためのリクエスト.
 * @author NTT DOCOMO, INC.
 */
public class DiscoveryDeviceRequest extends LocalOAuthRequest {
    /** Local OAuth認可後に送信するイベントデータ. */
    private Intent mEvent;

    /**
     * Local OAuth認可後に送信するイベントを設定する.
     * @param event イベント
     */
    public void setEvent(final Intent event) {
        mEvent = event;
    }

    @Override
    protected void executeRequest(final String accessToken) {
        List<Event> evts = EventManager.INSTANCE.getEventList(
                ServiceDiscoveryProfileConstants.PROFILE_NAME,
                ServiceDiscoveryProfileConstants.ATTRIBUTE_ON_SERVICE_CHANGE);
        for (int i = 0; i < evts.size(); i++) {
            Event evt = evts.get(i);
//            ((DConnectService) getContext()).sendEvent(evt.getReceiverName(), mEvent);
        }
    }
}
