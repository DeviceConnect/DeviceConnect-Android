/*
 DConnectServiceSpecTest.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec;

import android.content.Intent;

import org.deviceconnect.android.PluginSDKTestRunner;
import org.deviceconnect.android.message.DevicePluginContext;
import org.deviceconnect.android.profile.spec.models.Method;
import org.deviceconnect.android.profile.spec.models.Operation;
import org.deviceconnect.android.profile.spec.models.Path;
import org.deviceconnect.android.profile.spec.models.Swagger;
import org.deviceconnect.android.utils.FileLoader;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(PluginSDKTestRunner.class)
public class DConnectServiceSpecTest {

    /**
     * DConnectServiceSpec#findProfileSpec(String) から Operation を取得する。
     * <pre>
     * 【期待する動作】
     * ・Swagger オブジェクトが取得できること。
     * </pre>
     */
    @Test
    public void testFindSwagger() throws JSONException {
        DevicePluginContext pluginContext = Mockito.mock(DevicePluginContext.class);

        String jsonString = FileLoader.readString("testProfile.json");

        DConnectServiceSpec spec = new DConnectServiceSpec(pluginContext);
        spec.addProfileSpec("testProfile", jsonString);

        Swagger swagger = spec.findProfileSpec("testProfile");
        assertThat(swagger, is(notNullValue()));
    }

    /**
     * DConnectServiceSpec#findProfileSpec(String) の引数に null を指定して Operation を取得する。
     * <pre>
     * 【期待する動作】
     * ・Swagger オブジェクトが取得できないこと。
     * </pre>
     */
    @Test
    public void testFindSwaggerNull() throws JSONException {
        DevicePluginContext pluginContext = Mockito.mock(DevicePluginContext.class);

        String jsonString = FileLoader.readString("testProfile.json");

        DConnectServiceSpec spec = new DConnectServiceSpec(pluginContext);
        spec.addProfileSpec("testProfile", jsonString);

        Swagger swagger = spec.findProfileSpec(null);
        assertThat(swagger, is(nullValue()));
    }

    /**
     * DConnectServiceSpec#findProfileSpec(String) に存在しないプロファイル名を指定して Operation を取得する。
     * <pre>
     * 【期待する動作】
     * ・Swagger オブジェクトが取得できないこと。
     * </pre>
     */
    @Test
    public void testFindSwaggerNotExist() throws JSONException {
        DevicePluginContext pluginContext = Mockito.mock(DevicePluginContext.class);

        String jsonString = FileLoader.readString("testProfile.json");

        DConnectServiceSpec spec = new DConnectServiceSpec(pluginContext);
        spec.addProfileSpec("testProfile", jsonString);

        Swagger swagger = spec.findProfileSpec("aaa");
        assertThat(swagger, is(nullValue()));
    }

    /**
     * DConnectServiceSpec#findOperationSpec(Method, String) から Operation を取得する。
     * <pre>
     * 【期待する動作】
     * ・Operation オブジェクトが取得できること。
     * </pre>
     */
    @Test
    public void testFindProfileSpec() throws JSONException {
        DevicePluginContext pluginContext = Mockito.mock(DevicePluginContext.class);

        String jsonString = FileLoader.readString("testProfile.json");

        DConnectServiceSpec spec = new DConnectServiceSpec(pluginContext);
        spec.addProfileSpec("testProfile", jsonString);

        Operation operation = spec.findOperationSpec(Method.GET, "/gotapi/testProfile");
        assertThat(operation, is(notNullValue()));

        operation = spec.findOperationSpec(Method.GET, "/gotapi/testProfile/");
        assertThat(operation, is(notNullValue()));

        operation = spec.findOperationSpec(Method.GET, "/gotapi/testProfile/a0");
        assertThat(operation, is(notNullValue()));

        operation = spec.findOperationSpec(Method.GET, "/gotapi/testProfile/a0/");
        assertThat(operation, is(notNullValue()));

        operation = spec.findOperationSpec(Method.GET, "/gotapi/testProfile/a1");
        assertThat(operation, is(notNullValue()));

        operation = spec.findOperationSpec(Method.GET, "/gotapi/testProfile/a1/");
        assertThat(operation, is(notNullValue()));
    }

    /**
     * DConnectServiceSpec#findOperationSpec(Intent) から Operation を取得する。
     * <pre>
     * 【期待する動作】
     * ・Operation オブジェクトが取得できること。
     * </pre>
     */
    @Test
    public void testFindProfileSpecFromIntent() throws JSONException {
        DevicePluginContext pluginContext = Mockito.mock(DevicePluginContext.class);

        String jsonString = FileLoader.readString("testProfile.json");

        DConnectServiceSpec spec = new DConnectServiceSpec(pluginContext);
        spec.addProfileSpec("testProfile", jsonString);

        Intent request = new Intent(IntentDConnectMessage.ACTION_GET);
        request.putExtra("api", "gotapi");
        request.putExtra("profile", "testProfile");

        Operation operation = spec.findOperationSpec(request);
        assertThat(operation, is(notNullValue()));

        request = new Intent(IntentDConnectMessage.ACTION_GET);
        request.putExtra("api", "gotapi");
        request.putExtra("profile", "testProfile");
        request.putExtra("attribute", "a0");

        operation = spec.findOperationSpec(request);
        assertThat(operation, is(notNullValue()));
    }

    /**
     * DConnectServiceSpec#findOperationSpec(Method, String) の引数に null を指定して Operation を取得する。
     * <pre>
     * 【期待する動作】
     * ・Operation オブジェクトが取得できないこと。
     * </pre>
     */
    @Test
    public void testFindProfileSpecNull() throws JSONException {
        DevicePluginContext pluginContext = Mockito.mock(DevicePluginContext.class);

        String jsonString = FileLoader.readString("testProfile.json");

        DConnectServiceSpec spec = new DConnectServiceSpec(pluginContext);
        spec.addProfileSpec("testProfile", jsonString);

        Operation operation = spec.findOperationSpec(Method.GET, null);
        assertThat(operation, is(nullValue()));

        operation = spec.findOperationSpec(null, "/gotapi/testProfile");
        assertThat(operation, is(nullValue()));
    }

    /**
     * DConnectServiceSpec#findOperationSpec(Method, String) に存在しない Method やパスを指定して Operation を取得する。
     * <pre>
     * 【期待する動作】
     * ・Operation オブジェクトが取得できないこと。
     * </pre>
     */
    @Test
    public void testFindProfileSpecNotExist() throws JSONException {
        DevicePluginContext pluginContext = Mockito.mock(DevicePluginContext.class);

        String jsonString = FileLoader.readString("testProfile.json");

        DConnectServiceSpec spec = new DConnectServiceSpec(pluginContext);
        spec.addProfileSpec("testProfile", jsonString);

        Operation operation = spec.findOperationSpec(Method.GET, "/gotapi/testProfile/aaa");
        assertThat(operation, is(nullValue()));

        operation = spec.findOperationSpec(Method.PUT, "/gotapi/testProfile");
        assertThat(operation, is(nullValue()));
    }

    /**
     * DConnectServiceSpec#findOperationSpec(Intent) に存在しない Method やパスを指定して Operation を取得する。
     * <pre>
     * 【期待する動作】
     * ・Operation オブジェクトが取得できないこと。
     * </pre>
     */
    @Test
    public void testFindProfileSpecNotExistFromIntent() throws JSONException {
        DevicePluginContext pluginContext = Mockito.mock(DevicePluginContext.class);

        String jsonString = FileLoader.readString("testProfile.json");

        DConnectServiceSpec spec = new DConnectServiceSpec(pluginContext);
        spec.addProfileSpec("testProfile", jsonString);

        Intent request = new Intent(IntentDConnectMessage.ACTION_GET);
        request.putExtra("api", "gotapi");
        request.putExtra("profile", "testProfile");
        request.putExtra("attribute", "aaa");

        Operation operation = spec.findOperationSpec(request);
        assertThat(operation, is(nullValue()));

        request = new Intent(IntentDConnectMessage.ACTION_PUT);
        request.putExtra("api", "gotapi");
        request.putExtra("profile", "testProfile");
        request.putExtra("attribute", "a0");

        operation = spec.findOperationSpec(request);
        assertThat(operation, is(nullValue()));

        request = new Intent();
        request.putExtra("api", "gotapi");
        request.putExtra("profile", "testProfile");
        request.putExtra("attribute", "a0");

        operation = spec.findOperationSpec(request);
        assertThat(operation, is(nullValue()));
    }

    /**
     * DConnectServiceSpec#findOperationSpec(Intent) に null を指定して Operation を取得する。
     * <pre>
     * 【期待する動作】
     * ・Operation オブジェクトが取得できないこと。
     * </pre>
     */
    @Test
    public void testFindProfileSpecNullFromIntent() throws JSONException {
        DevicePluginContext pluginContext = Mockito.mock(DevicePluginContext.class);

        String jsonString = FileLoader.readString("testProfile.json");

        DConnectServiceSpec spec = new DConnectServiceSpec(pluginContext);
        spec.addProfileSpec("testProfile", jsonString);

        Operation operation = spec.findOperationSpec(null);
        assertThat(operation, is(nullValue()));
    }

    /**
     * DConnectServiceSpec#findPathSpec(String) から Operation を取得する。
     * <pre>
     * 【期待する動作】
     * ・Path オブジェクトが取得できること。
     * </pre>
     */
    @Test
    public void testFindPathSpec() throws JSONException {
        DevicePluginContext pluginContext = Mockito.mock(DevicePluginContext.class);

        String jsonString = FileLoader.readString("testProfile.json");

        DConnectServiceSpec spec = new DConnectServiceSpec(pluginContext);
        spec.addProfileSpec("testProfile", jsonString);

        Path path = spec.findPathSpec("/gotapi/testProfile");
        assertThat(path, is(notNullValue()));
    }

    /**
     * DConnectServiceSpec#findPathSpec(Intent) から Operation を取得する。
     * <pre>
     * 【期待する動作】
     * ・Path オブジェクトが取得できること。
     * </pre>
     */
    @Test
    public void testFindPathSpecFromIntent() throws JSONException {
        DevicePluginContext pluginContext = Mockito.mock(DevicePluginContext.class);

        String jsonString = FileLoader.readString("testProfile.json");

        DConnectServiceSpec spec = new DConnectServiceSpec(pluginContext);
        spec.addProfileSpec("testProfile", jsonString);

        Intent request = new Intent(IntentDConnectMessage.ACTION_GET);
        request.putExtra("api", "gotapi");
        request.putExtra("profile", "testProfile");

        Path path = spec.findPathSpec(request);
        assertThat(path, is(notNullValue()));
    }

    /**
     * DConnectServiceSpec#findPathSpec(String) に null を指定して Operation を取得する。
     * <pre>
     * 【期待する動作】
     * ・Path オブジェクトが取得できないこと。
     * </pre>
     */
    @Test
    public void testFindPathSpecNull() throws JSONException {
        DevicePluginContext pluginContext = Mockito.mock(DevicePluginContext.class);

        String jsonString = FileLoader.readString("testProfile.json");

        DConnectServiceSpec spec = new DConnectServiceSpec(pluginContext);
        spec.addProfileSpec("testProfile", jsonString);

        Path path = spec.findPathSpec((String) null);
        assertThat(path, is(nullValue()));

        path = spec.findPathSpec((Intent) null);
        assertThat(path, is(nullValue()));
    }

    /**
     * DConnectServiceSpec#findPathSpec(String) に存在しないプロファイル名を指定して Operation を取得する。
     * <pre>
     * 【期待する動作】
     * ・Path オブジェクトが取得できないこと。
     * </pre>
     */
    @Test
    public void testFindPathSpecNotExist() throws JSONException {
        DevicePluginContext pluginContext = Mockito.mock(DevicePluginContext.class);

        String jsonString = FileLoader.readString("testProfile.json");

        DConnectServiceSpec spec = new DConnectServiceSpec(pluginContext);
        spec.addProfileSpec("testProfile", jsonString);

        Path path = spec.findPathSpec("/gotapi/abcProfile");
        assertThat(path, is(nullValue()));
    }
}
