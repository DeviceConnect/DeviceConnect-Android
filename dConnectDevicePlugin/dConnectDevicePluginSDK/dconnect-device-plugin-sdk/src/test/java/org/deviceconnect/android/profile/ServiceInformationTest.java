/*
 ServiceInformationTest.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import org.deviceconnect.android.PluginSDKTestRunner;
import org.deviceconnect.android.profile.spec.DConnectServiceSpec;
import org.deviceconnect.android.profile.spec.parser.OpenAPIParser;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.android.utils.FileLoader;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(PluginSDKTestRunner.class)
public class ServiceInformationTest {

    /**
     * ServiceInformation#onRequest(Intent, Intent) を呼び出すテスト。
     * <pre>
     * 【期待する動作】
     * ・resultに0が返ってくること。
     * ・resources/testProile.json で定義した仕様が取得されること。
     * </pre>
     */
    @Test
    public void testOnRequest() throws Exception {
        final String profileName = "testProfile";
        final String specName = profileName + ".json";
        final String version = "1.2.3";

        PackageInfo packageInfo = new PackageInfo();
        packageInfo.versionName = version;

        PackageManager packageManager = Mockito.mock(PackageManager.class);
        Mockito.when(packageManager.getPackageInfo(Mockito.anyString(), Mockito.anyInt())).thenReturn(packageInfo);

        Context context = Mockito.mock(Context.class);
        Mockito.when(context.getPackageName()).thenReturn("org.deviceconnect.android.test");
        Mockito.when(context.getPackageManager()).thenReturn(packageManager);

        DConnectServiceSpec pluginSpec = Mockito.mock(DConnectServiceSpec.class);
        Mockito.when(pluginSpec.findProfileSpec(Mockito.anyString()))
                .thenReturn(OpenAPIParser.parse(FileLoader.readString(specName)));

        DConnectService service = Mockito.spy(new DConnectService("serviceId"));
        service.addProfile(new DConnectProfile() {
            @Override
            public String getProfileName() {
                return profileName;
            }
        });
        Mockito.when(service.getServiceSpec()).thenReturn(pluginSpec);

        // リクエストとレスポンスの初期化
        Intent request = new Intent(IntentDConnectMessage.ACTION_GET);
        request.putExtra("profile", "serviceInformation");
        Intent response = new Intent();

        ServiceInformationProfile profile = (ServiceInformationProfile) service.getProfile("serviceInformation");
        profile.setContext(context);
        profile.setService(service);

        boolean isSync = profile.onRequest(request, response);

        assertThat(isSync, is(true));
        assertThat(response.getIntExtra("result", -1), is(0));
        assertThat(response.getStringExtra("version"), is(version));

        Bundle supportApis = response.getBundleExtra("supportApis");
        assertThat(supportApis, is(notNullValue()));

        Bundle swagger = supportApis.getBundle(profileName);
        assertThat(swagger, is(notNullValue()));
        assertThat(swagger.getString("swagger"), is("2.0"));
        assertThat(swagger.getString("basePath"), is("/gotapi/testProfile"));

        Bundle info = swagger.getBundle("info");
        assertThat(info, is(notNullValue()));
        assertThat(info.getString("title"), is(notNullValue()));
        assertThat(info.getString("version"), is(notNullValue()));
        assertThat(info.containsKey("description"), is(false));
        assertThat(swagger.getStringArray("consumes"), is(notNullValue()));

        Bundle paths = swagger.getBundle("paths");
        assertThat(paths, is(notNullValue()));
        assertThat(paths.size(), is(3));

        assertThat(swagger.containsKey("definitions"), is(false));
    }
}
