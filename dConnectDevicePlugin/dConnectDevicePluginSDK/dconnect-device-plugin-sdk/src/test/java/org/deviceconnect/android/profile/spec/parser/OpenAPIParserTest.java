/*
 OpenAPIParserTest.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec.parser;

import org.deviceconnect.android.PluginSDKTestRunner;
import org.deviceconnect.android.profile.spec.models.DataFormat;
import org.deviceconnect.android.profile.spec.models.DataType;
import org.deviceconnect.android.profile.spec.models.Example;
import org.deviceconnect.android.profile.spec.models.Info;
import org.deviceconnect.android.profile.spec.models.Path;
import org.deviceconnect.android.profile.spec.models.Swagger;
import org.deviceconnect.android.profile.spec.models.XEvent;
import org.deviceconnect.android.profile.spec.models.XType;
import org.deviceconnect.android.profile.spec.models.parameters.AbstractParameter;
import org.deviceconnect.android.profile.spec.models.parameters.Parameter;
import org.deviceconnect.android.utils.FileLoader;
import org.deviceconnect.utils.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;

@RunWith(PluginSDKTestRunner.class)
public class OpenAPIParserTest {

    private void printSwagger(Swagger swagger) throws JSONException {
        JSONObject o = JSONUtils.convertBundleToJSON(swagger.toBundle());
        System.out.println();
        System.out.println(" " + o.toString(2));
        System.out.println();
    }

    /**
     * OpenAPIParser#parse(String) に testProfile.json を渡して解析を行う。
     * <pre>
     * 【期待する動作】
     * ・Swagger オブジェクトが取得できること。
     * ・Swagger オブジェクトに /a0 と /a1 が存在すること。
     * </pre>
     */
    @Test
    public void test() throws JSONException {
        String jsonString = FileLoader.readString("testProfile.json");

        Swagger swagger = OpenAPIParser.parse(jsonString);
        printSwagger(swagger);

        assertThat(swagger, is(notNullValue()));
        assertThat(swagger.getSwagger(), is("2.0"));
        assertThat(swagger.getBasePath(), is("/gotapi/testProfile"));
        assertThat(swagger.getPaths(), is(notNullValue()));
        assertThat(swagger.getProduces(), hasItems("application/json", "text/html"));
        assertThat(swagger.getConsumes(), hasItems("application/json"));

        Info info = swagger.getInfo();
        assertThat(info.getTitle(), is("Test Profile"));
        assertThat(info.getVersion(), is("1.0"));
        assertThat(info.getDescription(), is("Test Description"));

        Path a0 = swagger.getPaths().getPath("/a0");
        assertThat(a0, is(notNullValue()));
        assertThat(a0.getGet().getSummary(), is("test path a0"));
        assertThat(a0.getGet().getDescription(), is("test path a0"));
        assertThat(a0.getGet().getOperationId(), is("a0Get"));

        List<Parameter> parameters = a0.getGet().getParameters();
        assertThat(parameters, is(notNullValue()));
        assertThat(parameters.get(0).getName(), is("serviceId"));
        assertThat(parameters.get(0).getDescription(), is("serviceId"));
        assertThat(parameters.get(0).getIn().getName(), is("query"));

        Path a1 = swagger.getPaths().getPath("/a1");
        assertThat(a1, is(notNullValue()));
        assertThat(a1.getGet().getSummary(), is("test path a1"));
        assertThat(a1.getGet().getDescription(), is("test path a1"));
        assertThat(a1.getGet().getOperationId(), is(nullValue()));
    }

    /**
     * OpenAPIParser#parse(String) に testVendorExtension.json を渡して解析を行う。
     * <pre>
     * 【期待する動作】
     * ・Swagger オブジェクトが取得できること。
     * ・Vendor Extension のデータが取得できること。
     * </pre>
     */
    @Test
    public void testVendorExtension() throws JSONException {
        String jsonString = FileLoader.readString("parser/testVendorExtension.json");

        Swagger swagger = OpenAPIParser.parse(jsonString);
        printSwagger(swagger);

        assertThat(swagger, is(notNullValue()));
        assertThat(swagger.getSwagger(), is("2.0"));
        assertThat(swagger.getBasePath(), is("/gotapi/testProfile"));
        assertThat(swagger.getPaths(), is(notNullValue()));
        assertThat(swagger.getProduces(), hasItems("application/json", "text/html"));
        assertThat(swagger.getConsumes(), hasItems("application/json"));

        Info info = swagger.getInfo();
        assertThat(info.getTitle(), is("Test Profile"));
        assertThat(info.getVersion(), is("1.0"));
        assertThat(info.getDescription(), is("Test Description"));

        Path a0 = swagger.getPaths().getPath("/a0");
        assertThat(a0, is(notNullValue()));
        assertThat(a0.getGet().getSummary(), is("test path a0"));
        assertThat(a0.getGet().getDescription(), is("test path a0"));
        assertThat(a0.getGet().getOperationId(), is("a0Get"));

        List<Parameter> parameters = a0.getGet().getParameters();
        assertThat(parameters, is(notNullValue()));
        assertThat(parameters.get(0).getName(), is("serviceId"));
        assertThat(parameters.get(0).getDescription(), is("serviceId"));
        assertThat(parameters.get(0).getIn().getName(), is("query"));

        XType xType = a0.getPut().getXType();
        assertThat(xType, is(notNullValue()));
        assertThat(xType, is(XType.EVENT));

        XEvent xEvent = a0.getPut().getXEvent();
        assertThat(xEvent, is(notNullValue()));
        assertThat(xEvent.getSchema(), is(notNullValue()));
        assertThat(xEvent.getSchema().getReference(), is(notNullValue()));
        assertThat(xEvent.getSchema().getReference(), is("#/definitions/CommonEvent"));
        assertThat(xEvent.getExamples(), is(notNullValue()));

        Example example = xEvent.getExamples().get("application/json");
        assertThat(example, is(notNullValue()));
        assertThat(example.getExample(), is(notNullValue()));
        assertThat(example.getExample().getString("serviceId"), is("Test.exampleId.localhost.deviceconnect.org"));
        assertThat(example.getExample().getString("profile"), is("testProfile"));
        assertThat(example.getExample().getString("attribute"), is("a0"));

        Path a1 = swagger.getPaths().getPath("/a1");
        assertThat(a1, is(notNullValue()));
        assertThat(a1.getGet().getSummary(), is("test path a1"));
        assertThat(a1.getGet().getDescription(), is("test path a1"));
        assertThat(a1.getGet().getOperationId(), is(nullValue()));
    }

    /**
     * OpenAPIParser#parse(String) に testInvalidJson.json を渡して解析を行う。
     * <pre>
     * 【期待する動作】
     * ・JSONException が発生すること。
     * </pre>
     */
    @Test(expected = JSONException.class)
    public void testInvalidJson() throws JSONException {
        String jsonString = FileLoader.readString("parser/testInvalidJson.json");
        OpenAPIParser.parse(jsonString);
    }

    /**
     * OpenAPIParser#parse(String) に文字列を渡して解析を行う。
     * <pre>
     * 【期待する動作】
     * ・JSONException が発生すること。
     * </pre>
     */
    @Test(expected = JSONException.class)
    public void testString() throws JSONException {
        String jsonString = "test";
        OpenAPIParser.parse(jsonString);
    }

    /**
     * OpenAPIParser#parse(String) に null を渡して解析を行う。
     * <pre>
     * 【期待する動作】
     * ・NullPointerException が発生すること。
     * </pre>
     */
    @Test(expected = NullPointerException.class)
    public void testNull() throws JSONException {
        OpenAPIParser.parse(null);
    }

    /**
     * OpenAPIParser#parse(String) に null を渡して解析を行う。
     * <pre>
     * 【期待する動作】
     * ・IllegalArgumentException が発生すること。
     * </pre>
     */
    @Test(expected = IllegalArgumentException.class)
    public void testEmpty() throws JSONException {
        String jsonString = "{}";
        OpenAPIParser.parse(jsonString);
    }

    /**
     * OpenAPIParser#parse(String) に testMinimumJson.json を渡して解析を行う。
     * <pre>
     * 【期待する動作】
     * ・Swagger オブジェクトが取得できること。
     * </pre>
     */
    @Test
    public void testMinimumJson() throws JSONException {
        String jsonString = FileLoader.readString("parser/testMinimumJson.json");

        Swagger swagger = OpenAPIParser.parse(jsonString);
        printSwagger(swagger);

        assertThat(swagger, is(notNullValue()));
        assertThat(swagger.getSwagger(), is("2.0"));
        assertThat(swagger.getBasePath(), is("/minimumJson/test"));
        assertThat(swagger.getPaths(), is(notNullValue()));

        Info info = swagger.getInfo();
        assertThat(info.getTitle(), is("Minimum JSON Test Profile"));
        assertThat(info.getVersion(), is("1.0"));
        assertThat(info.getDescription(), is("Minimum JSON Test Description"));

        assertThat(swagger.getPaths(), is(notNullValue()));
        assertThat(swagger.getPaths().getPaths(), is(nullValue()));
    }

    /**
     * OpenAPIParser#parse(String) に testParameterSpec.json を渡して解析を行う。
     * <pre>
     * 【期待する動作】
     * ・Swagger オブジェクトが取得できること。
     * ・parameters の値が取得できること。
     * </pre>
     */
    @Test
    public void testParameterSpec() throws JSONException {
        String jsonString = FileLoader.readString("parser/testParameterSpec.json");

        Swagger swagger = OpenAPIParser.parse(jsonString);
        printSwagger(swagger);

        assertThat(swagger, is(notNullValue()));
        assertThat(swagger.getSwagger(), is("2.0"));
        assertThat(swagger.getBasePath(), is("/parameter/test"));
        assertThat(swagger.getPaths(), is(notNullValue()));

        Info info = swagger.getInfo();
        assertThat(info.getTitle(), is("Parameter Test Profile"));
        assertThat(info.getVersion(), is("1.0"));
        assertThat(info.getDescription(), is("Parameter Test Description"));

        Path a0 = swagger.getPaths().getPath("/");
        assertThat(a0, is(notNullValue()));

        List<Parameter> parameters = a0.getGet().getParameters();
        assertThat(parameters, is(notNullValue()));
        assertThat(parameters.size(), is(24));

        assertThat(parameters.get(0).getName(), is("booleanParam"));
        assertThat(parameters.get(0).getIn().getName(), is("query"));
        assertThat(parameters.get(0).isRequired(), is(true));
        assertThat(((AbstractParameter) parameters.get(0)).getType(), is(DataType.BOOLEAN));

        assertThat(parameters.get(1).getName(), is("booleanParamWithEnum"));
        assertThat(parameters.get(1).getIn().getName(), is("query"));
        assertThat(parameters.get(1).isRequired(), is(true));
        assertThat(((AbstractParameter) parameters.get(1)).getType(), is(DataType.BOOLEAN));
        assertThat(((AbstractParameter) parameters.get(1)).getEnum(), is(notNullValue()));
        assertThat(((AbstractParameter) parameters.get(1)).getEnum().size(), is(2));
        assertThat(((AbstractParameter) parameters.get(1)).getEnum().get(0), is(true));
        assertThat(((AbstractParameter) parameters.get(1)).getEnum().get(1), is(false));

        assertThat(parameters.get(2).getName(), is("integerParam"));
        assertThat(parameters.get(2).getIn().getName(), is("query"));
        assertThat(parameters.get(2).isRequired(), is(true));
        assertThat(((AbstractParameter) parameters.get(2)).getType(), is(DataType.INTEGER));

        assertThat(parameters.get(3).getName(), is("integerParamWithEnum"));
        assertThat(parameters.get(3).getIn().getName(), is("query"));
        assertThat(parameters.get(3).isRequired(), is(true));
        assertThat(((AbstractParameter) parameters.get(3)).getType(), is(DataType.INTEGER));
        assertThat(((AbstractParameter) parameters.get(3)).getEnum(), is(notNullValue()));
        assertThat(((AbstractParameter) parameters.get(3)).getEnum().size(), is(2));
        assertThat(((AbstractParameter) parameters.get(3)).getEnum().get(0), is(0));
        assertThat(((AbstractParameter) parameters.get(3)).getEnum().get(1), is(1));

        assertThat(parameters.get(4).getName(), is("integerParamWithRange"));
        assertThat(parameters.get(4).getIn().getName(), is("query"));
        assertThat(parameters.get(4).isRequired(), is(true));
        assertThat(((AbstractParameter) parameters.get(4)).getType(), is(DataType.INTEGER));
        assertThat(((AbstractParameter) parameters.get(4)).getMaximum(), is(1));
        assertThat(((AbstractParameter) parameters.get(4)).getMinimum(), is(0));
        assertThat(((AbstractParameter) parameters.get(4)).isExclusiveMaximum(), is(true));
        assertThat(((AbstractParameter) parameters.get(4)).isExclusiveMinimum(), is(true));

        assertThat(parameters.get(5).getName(), is("integerParamFormatInt32"));
        assertThat(parameters.get(5).getIn().getName(), is("query"));
        assertThat(parameters.get(5).isRequired(), is(true));
        assertThat(((AbstractParameter) parameters.get(5)).getType(), is(DataType.INTEGER));
        assertThat(((AbstractParameter) parameters.get(5)).getFormat(), is(DataFormat.INT32));

        assertThat(parameters.get(6).getName(), is("integerParamFormatInt64"));
        assertThat(parameters.get(6).getIn().getName(), is("query"));
        assertThat(parameters.get(6).isRequired(), is(true));
        assertThat(((AbstractParameter) parameters.get(6)).getType(), is(DataType.INTEGER));
        assertThat(((AbstractParameter) parameters.get(6)).getFormat(), is(DataFormat.INT64));

        assertThat(parameters.get(7).getName(), is("numberParam"));
        assertThat(parameters.get(7).getIn().getName(), is("query"));
        assertThat(parameters.get(7).isRequired(), is(true));
        assertThat(((AbstractParameter) parameters.get(7)).getType(), is(DataType.NUMBER));

        assertThat(parameters.get(8).getName(), is("numberParamWithEnum"));
        assertThat(parameters.get(8).getIn().getName(), is("query"));
        assertThat(parameters.get(8).isRequired(), is(true));
        assertThat(((AbstractParameter) parameters.get(8)).getType(), is(DataType.NUMBER));
        assertThat(((AbstractParameter) parameters.get(8)).getEnum(), is(notNullValue()));
        assertThat(((AbstractParameter) parameters.get(8)).getEnum().size(), is(2));
        assertThat(((AbstractParameter) parameters.get(8)).getEnum().get(0), is(0.5));
        assertThat(((AbstractParameter) parameters.get(8)).getEnum().get(1), is(1.5));

        assertThat(parameters.get(9).getName(), is("numberParamWithRange"));
        assertThat(parameters.get(9).getIn().getName(), is("query"));
        assertThat(parameters.get(9).isRequired(), is(true));
        assertThat(((AbstractParameter) parameters.get(9)).getType(), is(DataType.NUMBER));
        assertThat(((AbstractParameter) parameters.get(9)).getMaximum(), is(1.5));
        assertThat(((AbstractParameter) parameters.get(9)).getMinimum(), is(0.5));
        assertThat(((AbstractParameter) parameters.get(9)).isExclusiveMaximum(), is(true));
        assertThat(((AbstractParameter) parameters.get(9)).isExclusiveMinimum(), is(true));

        assertThat(parameters.get(10).getName(), is("numberParamFormatFloat"));
        assertThat(parameters.get(10).getIn().getName(), is("query"));
        assertThat(parameters.get(10).isRequired(), is(true));
        assertThat(((AbstractParameter) parameters.get(10)).getType(), is(DataType.NUMBER));
        assertThat(((AbstractParameter) parameters.get(10)).getFormat(), is(DataFormat.FLOAT));

        assertThat(parameters.get(11).getName(), is("numberParamFormatDouble"));
        assertThat(parameters.get(11).getIn().getName(), is("query"));
        assertThat(parameters.get(11).isRequired(), is(true));
        assertThat(((AbstractParameter) parameters.get(11)).getType(), is(DataType.NUMBER));
        assertThat(((AbstractParameter) parameters.get(11)).getFormat(), is(DataFormat.DOUBLE));

        assertThat(parameters.get(12).getName(), is("stringParam"));
        assertThat(parameters.get(12).getIn().getName(), is("query"));
        assertThat(parameters.get(12).isRequired(), is(true));
        assertThat(((AbstractParameter) parameters.get(12)).getType(), is(DataType.STRING));

        assertThat(parameters.get(13).getName(), is("stringParamWithEnum"));
        assertThat(parameters.get(13).getIn().getName(), is("query"));
        assertThat(parameters.get(13).isRequired(), is(true));
        assertThat(((AbstractParameter) parameters.get(13)).getType(), is(DataType.STRING));
        assertThat(((AbstractParameter) parameters.get(13)).getEnum(), is(notNullValue()));
        assertThat(((AbstractParameter) parameters.get(13)).getEnum().size(), is(3));
        assertThat(((AbstractParameter) parameters.get(13)).getEnum().get(0), is("a"));
        assertThat(((AbstractParameter) parameters.get(13)).getEnum().get(1), is("b"));
        assertThat(((AbstractParameter) parameters.get(13)).getEnum().get(2), is("c"));

        assertThat(parameters.get(14).getName(), is("stringParamWithRange"));
        assertThat(parameters.get(14).getIn().getName(), is("query"));
        assertThat(parameters.get(14).isRequired(), is(true));
        assertThat(((AbstractParameter) parameters.get(14)).getType(), is(DataType.STRING));
        assertThat(((AbstractParameter) parameters.get(14)).getMaxLength(), is(1));
        assertThat(((AbstractParameter) parameters.get(14)).getMinLength(), is(0));

        assertThat(parameters.get(15).getName(), is("stringParamFormatText"));
        assertThat(parameters.get(15).getIn().getName(), is("query"));
        assertThat(parameters.get(15).isRequired(), is(true));
        assertThat(((AbstractParameter) parameters.get(15)).getType(), is(DataType.STRING));
        assertThat(((AbstractParameter) parameters.get(15)).getFormat(), is(DataFormat.TEXT));

        assertThat(parameters.get(16).getName(), is("stringParamFormatByte"));
        assertThat(parameters.get(16).getIn().getName(), is("query"));
        assertThat(parameters.get(16).isRequired(), is(true));
        assertThat(((AbstractParameter) parameters.get(16)).getType(), is(DataType.STRING));
        assertThat(((AbstractParameter) parameters.get(16)).getFormat(), is(DataFormat.BYTE));

        assertThat(parameters.get(17).getName(), is("stringParamFormatBinary"));
        assertThat(parameters.get(17).getIn().getName(), is("query"));
        assertThat(parameters.get(17).isRequired(), is(true));
        assertThat(((AbstractParameter) parameters.get(17)).getType(), is(DataType.STRING));
        assertThat(((AbstractParameter) parameters.get(17)).getFormat(), is(DataFormat.BINARY));

        assertThat(parameters.get(18).getName(), is("stringParamFormatDate"));
        assertThat(parameters.get(18).getIn().getName(), is("query"));
        assertThat(parameters.get(18).isRequired(), is(true));
        assertThat(((AbstractParameter) parameters.get(18)).getType(), is(DataType.STRING));
        assertThat(((AbstractParameter) parameters.get(18)).getFormat(), is(DataFormat.DATE));

        assertThat(parameters.get(19).getName(), is("stringParamFormatDateTime"));
        assertThat(parameters.get(19).getIn().getName(), is("query"));
        assertThat(parameters.get(19).isRequired(), is(true));
        assertThat(((AbstractParameter) parameters.get(19)).getType(), is(DataType.STRING));
        assertThat(((AbstractParameter) parameters.get(19)).getFormat(), is(DataFormat.DATE_TIME));

        assertThat(parameters.get(20).getName(), is("stringParamFormatPassword"));
        assertThat(parameters.get(20).getIn().getName(), is("query"));
        assertThat(parameters.get(20).isRequired(), is(true));
        assertThat(((AbstractParameter) parameters.get(20)).getType(), is(DataType.STRING));
        assertThat(((AbstractParameter) parameters.get(20)).getFormat(), is(DataFormat.PASSWORD));

        assertThat(parameters.get(21).getName(), is("stringParamFormatRGB"));
        assertThat(parameters.get(21).getIn().getName(), is("query"));
        assertThat(parameters.get(21).isRequired(), is(true));
        assertThat(((AbstractParameter) parameters.get(21)).getType(), is(DataType.STRING));
        assertThat(((AbstractParameter) parameters.get(21)).getFormat(), is(DataFormat.RGB));

        assertThat(parameters.get(22).getName(), is("arrayParam"));
        assertThat(parameters.get(22).getIn().getName(), is("query"));
        assertThat(parameters.get(22).isRequired(), is(true));
        assertThat(((AbstractParameter) parameters.get(22)).getType(), is(DataType.ARRAY));

        assertThat(parameters.get(23).getName(), is("arrayParamWithInteger"));
        assertThat(parameters.get(23).getIn().getName(), is("query"));
        assertThat(parameters.get(23).isRequired(), is(true));
        assertThat(((AbstractParameter) parameters.get(23)).getType(), is(DataType.ARRAY));
        assertThat(((AbstractParameter) parameters.get(23)).getItems(), is(notNullValue()));
        assertThat(((AbstractParameter) parameters.get(23)).getItems().getType(), is(DataType.INTEGER));
    }
}
