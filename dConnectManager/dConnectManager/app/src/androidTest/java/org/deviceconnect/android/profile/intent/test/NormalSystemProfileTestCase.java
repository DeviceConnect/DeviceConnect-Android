/*
 NormalSystemProfileTestCase.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.intent.test;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.test.runner.AndroidJUnit4;

import org.deviceconnect.android.test.plugin.profile.TestSystemProfileConstants;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.deviceconnect.profile.SystemProfileConstants;
import org.junit.Test;
import org.junit.runner.RunWith;



/**
 * Network Service Discoveryプロファイルの正常系テスト.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class NormalSystemProfileTestCase extends IntentDConnectTestCase
    implements TestSystemProfileConstants {

    /** テスト用デバイスプラグインID. */
    private String mTestPluginID;

    /**
     * スマートフォンのシステムプロファイルを取得する.
     * <pre>
     * 【Intent通信】
     * Action: GET
     * Extra: 
     *     profile=system
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * </pre>
     */
    @Test
    public void testGetSystem() {
        Intent request = new Intent(IntentDConnectMessage.ACTION_GET);
        request.putExtra(DConnectMessage.EXTRA_PROFILE, SystemProfileConstants.PROFILE_NAME);
        Intent response = sendRequest(request);

        assertResultOK(response);
        assertEquals(VERSION,
                response.getStringExtra(SystemProfileConstants.PARAM_VERSION));
        String[] supports = response.getStringArrayExtra(SystemProfileConstants.PARAM_SUPPORTS);
        assertNotNull(supports);
        Parcelable[] plugins = response.getParcelableArrayExtra(SystemProfileConstants.PARAM_PLUGINS);
        assertNotNull(plugins);
        Bundle testPlugin = null;
        for (int i = 0; i < plugins.length; i++) {
            Bundle plugin = (Bundle) plugins[i];
            if ("Device Connect Device Plugin for Test".equals(plugin.getString(SystemProfileConstants.PARAM_NAME))) {
                testPlugin = plugin;
                break;
            }
        }
        assertNotNull(testPlugin);
        String id = testPlugin.getString(SystemProfileConstants.PARAM_ID);
        assertNotNull(id);
        mTestPluginID = id;
        assertNotNull(testPlugin.getString(SystemProfileConstants.PARAM_NAME));
    }

    /**
     * デバイスプラグインの機能を有効にするテストを行う.
     * <pre>
     * 【Intent通信】
     * Action: PUT
     * Extra: 
     *     profile=system
     *     interface=device
     *     attribute=wakeup
     *     sessionKey=xxxx
     * </pre>
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・versionに"2.0.0"が返ってくること。
     * </pre>
     */
    @Test
    public void testPutSystemWakeup() {
        testGetSystem();
        Intent request = new Intent(IntentDConnectMessage.ACTION_PUT);
        request.putExtra(SystemProfileConstants.PARAM_PLUGIN_ID, mTestPluginID);
        request.putExtra(DConnectMessage.EXTRA_PROFILE, SystemProfileConstants.PROFILE_NAME);
        request.putExtra(DConnectMessage.EXTRA_INTERFACE, SystemProfileConstants.INTERFACE_DEVICE);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, SystemProfileConstants.ATTRIBUTE_WAKEUP);
        Intent response = sendRequest(request);
        assertResultOK(response);
    }

}
