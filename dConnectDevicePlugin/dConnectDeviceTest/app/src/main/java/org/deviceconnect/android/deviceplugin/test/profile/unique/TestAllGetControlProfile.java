/*
 TestUniqueProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.test.profile.unique;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.test.UnitTestDeviceService;
import org.deviceconnect.android.deviceplugin.test.profile.Util;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * DeviceConnectのURLをGETで操作する機能のテスト用プロファイル.
 * @author NTT DOCOMO, INC.
 */
public class TestAllGetControlProfile extends DConnectProfile {

    /**
     * プロファイル名: {@value} .
     */
    public static final String PROFILE_NAME = "allGetControl";

    /**
     * インターフェース名: {@value} .
     */
    public static final String INTERFACE_TEST = "test";

    /**
     * 属性名: {@value} .
     */
    public static final String ATTRIBUTE_PING = "ping";

    /**
     * パラメータ: {@value}.
     */
    public static final String PARAM_KEY = "key";

    /**
     * テスト値: {@value}.
     */
    public static final String VALUE_KEY_PROFILE = "PROFILE_OK";
    /**
     * テスト値: {@value}.
     */
    public static final String VALUE_KEY_INTERFACE = "INTERFACE_OK";
    /**
     * テスト値: {@value}.
     */
    public static final String VALUE_KEY_ATTRIBUTE = "ATTRIBUTE_OK";

    @Override
    public String getProfileName() {
        return PROFILE_NAME;
    }

    @Override
    public boolean onRequest(final Intent request, final Intent response) {
        final String inter = getInterface(request);
        final String attribute = getAttribute(request);
        final String action = request.getAction();
        if (inter == null && attribute == null) {
            if (IntentDConnectMessage.ACTION_GET.equals(action)
                || IntentDConnectMessage.ACTION_POST.equals(action)
                || IntentDConnectMessage.ACTION_PUT.equals(action)
                || IntentDConnectMessage.ACTION_DELETE.equals(action)) {

                response.putExtra(PARAM_KEY, VALUE_KEY_PROFILE);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setUnknownAttributeError(response);
            }
        } else if (inter == null && ATTRIBUTE_PING.equals(attribute)) {
            if (IntentDConnectMessage.ACTION_GET.equals(action)
                    || IntentDConnectMessage.ACTION_POST.equals(action)
                    || IntentDConnectMessage.ACTION_PUT.equals(action)
                    || IntentDConnectMessage.ACTION_DELETE.equals(action)) {

                response.putExtra(PARAM_KEY, VALUE_KEY_ATTRIBUTE);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setUnknownAttributeError(response);
            }
        } else if (INTERFACE_TEST.equals(inter) && ATTRIBUTE_PING.equals(attribute)) {
            if (IntentDConnectMessage.ACTION_GET.equals(action)
                    || IntentDConnectMessage.ACTION_POST.equals(action)
                    || IntentDConnectMessage.ACTION_PUT.equals(action)
                    || IntentDConnectMessage.ACTION_DELETE.equals(action)) {

                response.putExtra(PARAM_KEY, VALUE_KEY_INTERFACE);
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setUnknownAttributeError(response);
            }
        } else {
            MessageUtils.setUnknownAttributeError(response);
        }
        return true;
    }


}
