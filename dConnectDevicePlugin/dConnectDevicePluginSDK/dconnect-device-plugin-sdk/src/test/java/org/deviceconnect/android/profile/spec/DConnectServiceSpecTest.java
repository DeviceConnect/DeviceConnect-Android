/*
 DConnectServiceSpecTest.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

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


    /**
     * OpenAPIParser#parse(String) に testEnum.json を渡して解析を行う。
     * 
     * parameter の type と異なる enum の宣言が存在する testEnum.json をよみこむ。
     * <pre>
     * 【期待する動作】
     * ・Swagger オブジェクトが取得できること。
     * ・parameter の enum が取得できること。
     * </pre>
     */
    @Test
    public void testEnum() throws JSONException {
        DevicePluginContext pluginContext = Mockito.mock(DevicePluginContext.class);

        String jsonString = FileLoader.readString("parser/testEnum.json");

        DConnectServiceSpec spec = new DConnectServiceSpec(pluginContext);
        spec.addProfileSpec("testEnum", jsonString);

        Bundle swagger = spec.findProfileSpec("testEnum").toBundle();
        assertThat(swagger, is(notNullValue()));

        Bundle paths = swagger.getBundle("paths");
        assertThat(paths, is(notNullValue()));
        assertThat(paths.size(), is(1));

        Bundle a0 = paths.getBundle("/a0");
        assertThat(a0, is(notNullValue()));

        Bundle a0Get = a0.getBundle("get");
        assertThat(a0Get, is(notNullValue()));

        Parcelable[] parameters = a0Get.getParcelableArray("parameters");
        assertThat(parameters, is(notNullValue()));
        assertThat(parameters.length, is(7));

        Bundle stringInt = (Bundle) parameters[1];
        assertThat(stringInt.getString("type"), is("string"));
        String[] stringIntEnum = stringInt.getStringArray("enum");
        assertThat(stringIntEnum, is(notNullValue()));
        assertThat(stringIntEnum[0], is("1"));
        assertThat(stringIntEnum[1], is("2"));
        assertThat(stringIntEnum[2], is("3"));
        assertThat(stringIntEnum[3], is("4"));

        Bundle stringNumber = (Bundle) parameters[2];
        assertThat(stringNumber.getString("type"), is("string"));
        String[] stringNumberEnum = stringNumber.getStringArray("enum");
        assertThat(stringNumberEnum, is(notNullValue()));
        assertThat(stringNumberEnum[0], is("1.1"));
        assertThat(stringNumberEnum[1], is("2.2"));
        assertThat(stringNumberEnum[2], is("3.3"));
        assertThat(stringNumberEnum[3], is("4.4"));

        Bundle intString = (Bundle) parameters[3];
        assertThat(intString.getString("type"), is("integer"));
        assertThat(intString.getString("format"), is("int32"));
        int[] intStringEnum = intString.getIntArray("enum");
        assertThat(intStringEnum, is(notNullValue()));
        assertThat(intStringEnum[0], is(1));
        assertThat(intStringEnum[1], is(2));
        assertThat(intStringEnum[2], is(3));
        assertThat(intStringEnum[3], is(4));

        Bundle longString = (Bundle) parameters[4];
        assertThat(longString.getString("type"), is("integer"));
        assertThat(longString.getString("format"), is("int64"));
        long[] longStringEnum = longString.getLongArray("enum");
        assertThat(longStringEnum, is(notNullValue()));
        assertThat(longStringEnum[0], is(1L));
        assertThat(longStringEnum[1], is(2L));
        assertThat(longStringEnum[2], is(3L));
        assertThat(longStringEnum[3], is(4L));

        Bundle floatString = (Bundle) parameters[5];
        assertThat(floatString.getString("type"), is("number"));
        assertThat(floatString.getString("format"), is("float"));
        float[] floatStringEnum = floatString.getFloatArray("enum");
        assertThat(floatStringEnum, is(notNullValue()));
        assertThat(floatStringEnum[0], is(1.1F));
        assertThat(floatStringEnum[1], is(2.2F));
        assertThat(floatStringEnum[2], is(3.3F));
        assertThat(floatStringEnum[3], is(4.4F));

        Bundle doubleString = (Bundle) parameters[6];
        assertThat(doubleString.getString("type"), is("number"));
        assertThat(doubleString.getString("format"), is("double"));
        double[] doubleStringEnum = doubleString.getDoubleArray("enum");
        assertThat(doubleStringEnum, is(notNullValue()));
        assertThat(doubleStringEnum[0], is(1.1D));
        assertThat(doubleStringEnum[1], is(2.2D));
        assertThat(doubleStringEnum[2], is(3.3D));
        assertThat(doubleStringEnum[3], is(4.4D));
    }
}
