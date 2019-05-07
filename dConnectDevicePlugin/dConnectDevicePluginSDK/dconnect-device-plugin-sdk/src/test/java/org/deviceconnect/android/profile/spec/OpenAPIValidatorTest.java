/*
 OpenAPIValidatorTest.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec;

import android.content.Intent;

import org.deviceconnect.android.PluginSDKTestRunner;
import org.deviceconnect.android.profile.spec.models.DataFormat;
import org.deviceconnect.android.profile.spec.models.DataType;
import org.deviceconnect.android.profile.spec.models.Items;
import org.deviceconnect.android.profile.spec.models.Operation;
import org.deviceconnect.android.profile.spec.models.Path;
import org.deviceconnect.android.profile.spec.models.Paths;
import org.deviceconnect.android.profile.spec.models.Swagger;
import org.deviceconnect.android.profile.spec.models.parameters.QueryParameter;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * OpenAPIValidator のテスト.
 */
@RunWith(Enclosed.class)
public class OpenAPIValidatorTest {

    /**
     * 共通テスト.
     */
    @RunWith(PluginSDKTestRunner.class)
    public static class CommonTest {

        /**
         * Property#setType(DataType) に null を指定して OpenAPIValidator#validte(Property, Object) を呼び出す。
         * <pre>
         * 【期待する動作】
         * ・パラメータ定義のフォーマットエラーなので、妥当ということで true が返ることこと。
         * </pre>
         */
        @Test
        public void testNotSetType() {
            boolean result;

            QueryParameter p = new QueryParameter();

            result = OpenAPIValidator.validate(p, 0);
            assertThat(result, is(true));
        }

        /**
         * Property#setRequired(Boolean) に true を指定して、
         * OpenAPIValidator#validte(Property, Object) の第2引数に null
         * を指定して呼び出す。
         * <pre>
         * 【期待する動作】
         * ・パラメータが必須なので、値が未設定の場合には不適切ということで false が返ることこと。
         * </pre>
         */
        @Test
        public void testRequiredWithTrue() {
            boolean result;

            QueryParameter p = new QueryParameter();
            p.setType(DataType.INTEGER);
            p.setRequired(true);

            result = OpenAPIValidator.validate(p, null);
            assertThat(result, is(false));
        }

        /**
         * Property#setRequired(Boolean) に false を指定して、
         * OpenAPIValidator#validte(Property, Object) の第2引数に null
         * を指定して呼び出す。
         * <pre>
         * 【期待する動作】
         * ・パラメータが必須ではないので、値が未設定の場合でも妥当ということで true が返ることこと。
         * </pre>
         */
        @Test
        public void testRequiredWithFalse() {
            boolean result;

            QueryParameter p = new QueryParameter();
            p.setType(DataType.INTEGER);
            p.setRequired(false);

            result = OpenAPIValidator.validate(p, null);
            assertThat(result, is(true));
        }

        /**
         * Swagger#addParameter(String, Parameter) に Parameter を指定して呼び出す。
         * <pre>
         * 【期待する動作】
         * ・Swagger#addParameter(String, Parameter) の設定で反映されること。
         * </pre>
         */
        @Test
        public void testParametersWithRoot() {
            boolean result;

            QueryParameter p1 = new QueryParameter();
            p1.setName("serviceId");
            p1.setType(DataType.STRING);
            p1.setRequired(true);
            p1.setMaxLength(10);

            Paths paths = new Paths();
            paths.setPaths(new HashMap<>());

            Swagger swagger = new Swagger();
            swagger.addParameter("serviceId", p1);
            swagger.setPaths(paths);

            Intent request = new Intent();
            request.setAction(IntentDConnectMessage.ACTION_GET);
            request.putExtra("profile", "a0");
            request.putExtra("serviceId", "012345678");

            result = OpenAPIValidator.validate(swagger, request);
            assertThat(result, is(true));

            request.putExtra("serviceId", "01234567890");

            result = OpenAPIValidator.validate(swagger, request);
            assertThat(result, is(false));

            request.putExtra("serviceId", 1);

            result = OpenAPIValidator.validate(swagger, request);
            assertThat(result, is(false));
        }

        /**
         * Path#addParameter(String, Parameter) に Parameter を指定して呼び出す。
         * <pre>
         * 【期待する動作】
         * ・Path#addParameter(String, Parameter) の設定で反映されること。
         * </pre>
         */
        @Test
        public void testParametersWithPath() {
            boolean result;

            QueryParameter p1 = new QueryParameter();
            p1.setName("serviceId");
            p1.setType(DataType.STRING);
            p1.setRequired(true);

            Path path = new Path();
            path.addParameter(p1);

            Paths paths = new Paths();
            paths.addPath("/a0", path);

            Swagger swagger = new Swagger();
            swagger.setPaths(paths);

            Intent request = new Intent();
            request.setAction(IntentDConnectMessage.ACTION_GET);
            request.putExtra("profile", "a0");
            request.putExtra("serviceId", "aa");

            result = OpenAPIValidator.validate(swagger, request);
            assertThat(result, is(true));

            request.putExtra("serviceId", "01234567890");

            result = OpenAPIValidator.validate(swagger, request);
            assertThat(result, is(true));

            request.putExtra("serviceId", 1);

            result = OpenAPIValidator.validate(swagger, request);
            assertThat(result, is(false));
        }

        /**
         * Operation#addParameter(String, Parameter) に Parameter を指定して呼び出す。
         * <pre>
         * 【期待する動作】
         * ・Operation#addParameter(String, Parameter) の設定で反映されること。
         * </pre>
         */
        @Test
        public void testParametersWithOperation() {
            boolean result;

            QueryParameter p1 = new QueryParameter();
            p1.setName("serviceId");
            p1.setType(DataType.STRING);
            p1.setRequired(true);

            Operation operation = new Operation();
            operation.addParameter(p1);

            Path path = new Path();
            path.setGet(operation);

            Paths paths = new Paths();
            paths.addPath("/a0", path);

            Swagger swagger = new Swagger();
            swagger.setPaths(paths);

            Intent request = new Intent();
            request.setAction(IntentDConnectMessage.ACTION_GET);
            request.putExtra("profile", "a0");
            request.putExtra("serviceId", "aa");

            result = OpenAPIValidator.validate(swagger, request);
            assertThat(result, is(true));

            request.putExtra("serviceId", 1);

            result = OpenAPIValidator.validate(swagger, request);
            assertThat(result, is(false));
        }


        /**
         * Swagger#addParameter(String, Parameter)、Path#addParameter(String, Parameter)、
         * Operation#addParameter(String, Parameter) に Parameter を指定して呼び出す。
         * <pre>
         * 【期待する動作】
         * ・Parameter の優先順位が Operation > Path > Swagger になっていること。
         * </pre>
         */
        @Test
        public void testParametersWithOverWrite() {
            boolean result1;
            boolean result2;
            boolean result3;

            QueryParameter operationParameter = new QueryParameter();
            operationParameter.setName("serviceId");
            operationParameter.setType(DataType.STRING);
            operationParameter.setMaxLength(15);
            operationParameter.setRequired(true);

            QueryParameter pathParameter = new QueryParameter();
            pathParameter.setName("serviceId");
            pathParameter.setType(DataType.STRING);
            pathParameter.setMaxLength(10);
            pathParameter.setRequired(true);

            QueryParameter rootParameter = new QueryParameter();
            rootParameter.setName("serviceId");
            rootParameter.setType(DataType.STRING);
            rootParameter.setMaxLength(5);
            rootParameter.setRequired(true);

            Operation operation = new Operation();

            Path path = new Path();
            path.setGet(operation);

            Paths paths = new Paths();
            paths.addPath("/a0", path);

            Swagger swagger = new Swagger();
            swagger.setPaths(paths);
            swagger.addParameter("serviceId", rootParameter);

            Intent request1 = new Intent();
            request1.setAction(IntentDConnectMessage.ACTION_GET);
            request1.putExtra("profile", "a0");
            request1.putExtra("serviceId", "01234567890123");

            Intent request2 = new Intent();
            request2.setAction(IntentDConnectMessage.ACTION_GET);
            request2.putExtra("profile", "a0");
            request2.putExtra("serviceId", "012345678");

            Intent request3 = new Intent();
            request3.setAction(IntentDConnectMessage.ACTION_GET);
            request3.putExtra("profile", "a0");
            request3.putExtra("serviceId", "0123");

            result1 = OpenAPIValidator.validate(swagger, request1);
            result2 = OpenAPIValidator.validate(swagger, request2);
            result3 = OpenAPIValidator.validate(swagger, request3);
            assertThat(result1, is(false));
            assertThat(result2, is(false));
            assertThat(result3, is(true));

            path.addParameter(pathParameter);

            result1 = OpenAPIValidator.validate(swagger, request1);
            result2 = OpenAPIValidator.validate(swagger, request2);
            result3 = OpenAPIValidator.validate(swagger, request3);
            assertThat(result1, is(false));
            assertThat(result2, is(true));
            assertThat(result3, is(true));

            operation.addParameter(operationParameter);

            result1 = OpenAPIValidator.validate(swagger, request1);
            result2 = OpenAPIValidator.validate(swagger, request2);
            result3 = OpenAPIValidator.validate(swagger, request3);
            assertThat(result1, is(true));
            assertThat(result2, is(true));
            assertThat(result3, is(true));
        }
    }

    /**
     * 整数値のパラメータテスト.
     */
    @RunWith(PluginSDKTestRunner.class)
    public static class IntegerTest {
        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.INTEGER を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・パラメータが整数値の場合は true が返ること。
         * ・パラメータが整数値以外の場合は false が返ること。
         * </pre>
         */
        @Test
        public void test() {
            boolean result;

            QueryParameter p = new QueryParameter();
            p.setType(DataType.INTEGER);

            result = OpenAPIValidator.validate(p, 0);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, 10);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, -10);
            assertThat(result, is(true));

            // 文字列でも数値に変換できる場合は、数値として扱う
            result = OpenAPIValidator.validate(p, "11");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "-11");
            assertThat(result, is(true));

            // 実数は、整数に入らないので false
            result = OpenAPIValidator.validate(p, 0.0f);
            assertThat(result, is(false));

            result = OpenAPIValidator.validate(p, 5.5f);
            assertThat(result, is(false));

            result = OpenAPIValidator.validate(p, -5.5f);
            assertThat(result, is(false));

            result = OpenAPIValidator.validate(p, "5.5f");
            assertThat(result, is(false));

            result = OpenAPIValidator.validate(p, "-5.5f");
            assertThat(result, is(false));

            result = OpenAPIValidator.validate(p, "test");
            assertThat(result, is(false));
        }

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.INTEGER を指定
         * ・Property#setMaximum(Number) に 10 を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・パラメータが10未満の場合は true が返ること。
         * ・パラメータが10以上の場合は false が返ること。
         * </pre>
         */
        @Test
        public void testMaximum() {
            boolean result;

            QueryParameter p = new QueryParameter();
            p.setType(DataType.INTEGER);
            p.setMaximum(10);

            result = OpenAPIValidator.validate(p, 5);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, 100);
            assertThat(result, is(false));
        }

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.INTEGER を指定
         * ・Property#setMaximum(Number) に 10 を指定
         * ・Property#setExclusiveMaximum(Boolean) に true を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・パラメータが10未満の場合は true が返ること。
         * ・パラメータが10の場合は false が返ること。
         * ・パラメータが10以上の場合は false が返ること。
         * </pre>
         */
        @Test
        public void testExclusiveMaximumWithTrue() {
            boolean result;

            QueryParameter p = new QueryParameter();
            p.setType(DataType.INTEGER);
            p.setMaximum(10);
            p.setExclusiveMaximum(true);

            result = OpenAPIValidator.validate(p, 9);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, 10);
            assertThat(result, is(false));

            result = OpenAPIValidator.validate(p, 11);
            assertThat(result, is(false));
        }

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.INTEGER を指定
         * ・Property#setMaximum(Integer) に 10 を指定
         * ・Property#setExclusiveMaximum(Boolean) に false を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・パラメータが10未満の場合は true が返ること。
         * ・パラメータが10の場合は true が返ること。
         * ・パラメータが10以上の場合は false が返ること。
         * </pre>
         */
        @Test
        public void testExclusiveMaximumWithFalse() {
            boolean result;

            QueryParameter p = new QueryParameter();
            p.setType(DataType.INTEGER);
            p.setMaximum(10);
            p.setExclusiveMaximum(false);

            result = OpenAPIValidator.validate(p, 9);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, 10);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, 11);
            assertThat(result, is(false));
        }

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.INTEGER を指定
         * ・Property#setMinimum(Integer) に 10 を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・パラメータが10未満の場合は false が返ること。
         * ・パラメータが10の場合は true が返ること。
         * ・パラメータが10以上の場合は true が返ること。
         * </pre>
         */
        @Test
        public void testMinimum() {
            boolean result;

            QueryParameter p = new QueryParameter();
            p.setType(DataType.INTEGER);
            p.setMinimum(10);

            result = OpenAPIValidator.validate(p, 9);
            assertThat(result, is(false));

            result = OpenAPIValidator.validate(p, 10);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, 11);
            assertThat(result, is(true));
        }

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.INTEGER を指定
         * ・Property#setMinimum(Integer) に 10 を指定
         * ・Property#setExclusiveMinimum(Boolean) に true を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・パラメータが10未満の場合は false が返ること。
         * ・パラメータが10の場合は false が返ること。
         * ・パラメータが10以上の場合は true が返ること。
         * </pre>
         */
        @Test
        public void testExclusiveMinimumWithTrue() {
            boolean result;

            QueryParameter p = new QueryParameter();
            p.setType(DataType.INTEGER);
            p.setMinimum(10);
            p.setExclusiveMinimum(true);

            result = OpenAPIValidator.validate(p, 9);
            assertThat(result, is(false));

            result = OpenAPIValidator.validate(p, 10);
            assertThat(result, is(false));

            result = OpenAPIValidator.validate(p, 11);
            assertThat(result, is(true));
        }

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.INTEGER を指定
         * ・Property#setMinimum(Integer) に 10 を指定
         * ・Property#setExclusiveMinimum(Boolean) に false を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・パラメータが10未満の場合は false が返ること。
         * ・パラメータが10の場合は true が返ること。
         * ・パラメータが10以上の場合は true が返ること。
         * </pre>
         */
        @Test
        public void testExclusiveMinimumWithFalse() {
            boolean result;
            QueryParameter p = new QueryParameter();
            p.setType(DataType.INTEGER);
            p.setMinimum(10);
            p.setExclusiveMinimum(false);

            result = OpenAPIValidator.validate(p, 9);
            assertThat(result, is(false));

            result = OpenAPIValidator.validate(p, 10);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, 11);
            assertThat(result, is(true));
        }

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.INTEGER を指定
         * ・Property#setEnum(List) に 1,2,3 を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・パラメータが1,2,3の場合は true が返ること。
         * ・パラメータが1,2,3以外の場合は false が返ること。
         * </pre>
         */
        @Test
        public void testEnum() {
            boolean result;

            List<Object> enums = new ArrayList<>();
            enums.add(1);
            enums.add(2);
            enums.add(3);

            QueryParameter p = new QueryParameter();
            p.setType(DataType.INTEGER);
            p.setEnum(enums);

            result = OpenAPIValidator.validate(p, 1);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, 2);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, 4);
            assertThat(result, is(false));

            result = OpenAPIValidator.validate(p, 5);
            assertThat(result, is(false));

            result = OpenAPIValidator.validate(p, 6);
            assertThat(result, is(false));
        }

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.INTEGER を指定
         * ・Property#setFormat(DataFormat) に DataFormat.INT32 を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・パラメータが整数値の場合は true が返ること。
         * </pre>
         */
        @Test
        public void testFormatInt32() {
            boolean result;

            QueryParameter p = new QueryParameter();
            p.setType(DataType.INTEGER);
            p.setFormat(DataFormat.INT32);

            result = OpenAPIValidator.validate(p, -1);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, 0);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, 1);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, 10);
            assertThat(result, is(true));
        }

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.INTEGER を指定
         * ・Property#setFormat(DataFormat) に DataFormat.INT64 を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・パラメータが整数値の場合は true が返ること。
         * </pre>
         */
        @Test
        public void testFormatInt64() {
            boolean result;

            QueryParameter p = new QueryParameter();
            p.setType(DataType.INTEGER);
            p.setFormat(DataFormat.INT64);

            result = OpenAPIValidator.validate(p, -1);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, 0);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, 1);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, 10);
            assertThat(result, is(true));
        }

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.INTEGER を指定
         * ・Property#setFormat(DataFormat) に DataFormat.DOUBLE を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・常に true が返ること。
         * ・プロファイル定義ファイルのエラーなので、妥当性としては true を返却すること。
         * </pre>
         */
        @Test
        public void testInvalidFormat() {
            boolean result;

            QueryParameter p = new QueryParameter();
            p.setType(DataType.INTEGER);
            p.setFormat(DataFormat.DOUBLE);

            result = OpenAPIValidator.validate(p, 2);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, 10);
            assertThat(result, is(true));
        }
    }

    /**
     * 実数値のパラメータテスト.
     */
    @RunWith(PluginSDKTestRunner.class)
    public static class NumberTest {
        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.NUMBER を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・パラメータが実数値の場合は true が返ること。
         * ・パラメータが実数値以外の場合は false が返ること。
         * </pre>
         */
        @Test
        public void test() {
            boolean result;

            QueryParameter p = new QueryParameter();
            p.setType(DataType.NUMBER);

            result = OpenAPIValidator.validate(p, 0.0f);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, 5.5f);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, -5.5f);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "5.5f");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "-5.5f");
            assertThat(result, is(true));

            // 実数なので、整数も含むので true
            result = OpenAPIValidator.validate(p, 0);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, 10);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, -10);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "10");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "-10");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "test");
            assertThat(result, is(false));
        }

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.NUMBER を指定
         * ・Property#setMaximum(Number) に 5.5f を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・パラメータが5.5未満の場合は true が返ること。
         * ・パラメータが5.5以上の場合は false が返ること。
         * </pre>
         */
        @Test
        public void testMaximum() {
            boolean result;

            QueryParameter p = new QueryParameter();
            p.setType(DataType.NUMBER);
            p.setMaximum(5.5f);

            result = OpenAPIValidator.validate(p, 5.4f);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, 5.5f);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, 5.6f);
            assertThat(result, is(false));
        }

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.NUMBER を指定
         * ・Property#setMaximum(Number) に 5.5f を指定
         * ・Property#setExclusiveMaximum(Boolean) に true を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・パラメータが5.5未満の場合は true が返ること。
         * ・パラメータが5.5以上の場合は false が返ること。
         * </pre>
         */
        @Test
        public void testExclusiveMaximumWithTrue() {
            boolean result;

            QueryParameter p = new QueryParameter();
            p.setType(DataType.NUMBER);
            p.setMaximum(5.5f);
            p.setExclusiveMaximum(true);

            result = OpenAPIValidator.validate(p, 5.4f);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, 5.5f);
            assertThat(result, is(false));

            result = OpenAPIValidator.validate(p, 5.6f);
            assertThat(result, is(false));
        }

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.NUMBER を指定
         * ・Property#setMaximum(Number) に 5.5f を指定
         * ・Property#setExclusiveMaximum(Boolean) に false を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・パラメータが5.5未満の場合は false が返ること。
         * ・パラメータが5.5の場合は true が返ること。
         * ・パラメータが5.5以上の場合は true が返ること。
         * </pre>
         */
        @Test
        public void testExclusiveMaximumWithFalse() {
            boolean result;

            QueryParameter p = new QueryParameter();
            p.setType(DataType.NUMBER);
            p.setMaximum(5.5f);
            p.setExclusiveMaximum(false);

            result = OpenAPIValidator.validate(p, 5.4f);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, 5.5f);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, 5.6f);
            assertThat(result, is(false));
        }

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.NUMBER を指定
         * ・Property#setMinimum(Number) に 5.5f を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・パラメータが5.5未満の場合は false が返ること。
         * ・パラメータが5.5の場合は false が返ること。
         * ・パラメータが5.5以上の場合は true が返ること。
         * </pre>
         */
        @Test
        public void testMinimum() {
            boolean result;

            QueryParameter p = new QueryParameter();
            p.setType(DataType.NUMBER);
            p.setMinimum(5.5f);

            result = OpenAPIValidator.validate(p, 5.4f);
            assertThat(result, is(false));

            result = OpenAPIValidator.validate(p, 5.5f);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, 5.6f);
            assertThat(result, is(true));
        }

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.NUMBER を指定
         * ・Property#setMinimum(Number) に 5.5f を指定
         * ・Property#setExclusiveMinimum(Number) に true を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・パラメータが5.5未満の場合は false が返ること。
         * ・パラメータが5.5の場合は false が返ること。
         * ・パラメータが5.5以上の場合は true が返ること。
         * </pre>
         */
        @Test
        public void testExclusiveMinimumWithTrue() {
            boolean result;

            QueryParameter p = new QueryParameter();
            p.setType(DataType.NUMBER);
            p.setMinimum(5.5f);
            p.setExclusiveMinimum(true);

            result = OpenAPIValidator.validate(p, 5.4f);
            assertThat(result, is(false));

            result = OpenAPIValidator.validate(p, 5.5f);
            assertThat(result, is(false));

            result = OpenAPIValidator.validate(p, 5.6f);
            assertThat(result, is(true));
        }

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.NUMBER を指定
         * ・Property#setMinimum(Number) に 5.5f を指定
         * ・Property#setExclusiveMinimum(Number) に true を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・パラメータが5.5未満の場合は false が返ること。
         * ・パラメータが5.5の場合は true が返ること。
         * ・パラメータが5.5以上の場合は true が返ること。
         * </pre>
         */
        @Test
        public void testExclusiveMinimumWithFalse() {
            boolean result;

            QueryParameter p = new QueryParameter();
            p.setType(DataType.NUMBER);
            p.setMinimum(5.5f);
            p.setExclusiveMinimum(false);

            result = OpenAPIValidator.validate(p, 5.4f);
            assertThat(result, is(false));

            result = OpenAPIValidator.validate(p, 5.5f);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, 5.6f);
            assertThat(result, is(true));
        }

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.NUMBER を指定
         * ・Property#setEnum(List) に 1.5f,2.5f,3.5f を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・パラメータが 1.5f,2.5f,3.5fの場合は true が返ること。
         * ・パラメータが 1.5f,2.5f,3.5f以外の場合は false が返ること。
         * </pre>
         */
        @Test
        public void testEnum() {
            boolean result;

            List<Object> enums = new ArrayList<>();
            enums.add(1.5f);
            enums.add(2.5f);
            enums.add(3.5f);

            QueryParameter p = new QueryParameter();
            p.setType(DataType.NUMBER);
            p.setEnum(enums);

            result = OpenAPIValidator.validate(p, 1.5f);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, 2.5f);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, 3.5f);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, 1.4f);
            assertThat(result, is(false));

            result = OpenAPIValidator.validate(p, 1.6f);
            assertThat(result, is(false));
        }

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.NUMBER を指定
         * ・Property#setFormat(DataFormat) に DataFormat.FLOAT を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・パラメータが実数の場合は true が返ること。
         * </pre>
         */
        @Test
        public void testFormatFloat() {
            boolean result;

            QueryParameter p = new QueryParameter();
            p.setType(DataType.NUMBER);
            p.setFormat(DataFormat.FLOAT);

            result = OpenAPIValidator.validate(p, 1.5f);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, 10);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "1.4f");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "test");
            assertThat(result, is(false));
        }

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.NUMBER を指定
         * ・Property#setFormat(DataFormat) に DataFormat.DOUBLE を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・パラメータが実数の場合は true が返ること。
         * </pre>
         */
        @Test
        public void testFormatDouble() {
            boolean result;

            QueryParameter p = new QueryParameter();
            p.setType(DataType.NUMBER);
            p.setFormat(DataFormat.DOUBLE);

            result = OpenAPIValidator.validate(p, 1.5f);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, 10);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "1.4f");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "test");
            assertThat(result, is(false));
        }

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.NUMBER を指定
         * ・Property#setFormat(DataFormat) に DataFormat.INT32 を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・常に true が返ること。
         * ・プロファイル定義ファイルのエラーなので、妥当性としては true を返却すること。
         * </pre>
         */
        @Test
        public void testInvalidFormat() {
            boolean result;

            QueryParameter p = new QueryParameter();
            p.setType(DataType.NUMBER);
            p.setFormat(DataFormat.INT32);

            result = OpenAPIValidator.validate(p, 2);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, 10);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "test");
            assertThat(result, is(false));
        }
    }

    /**
     * 文字列のパラメータテスト.
     */
    @RunWith(PluginSDKTestRunner.class)
    public static class StringTest {

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.STRING を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・パラメータが文字列の場合は true が返ること。
         * ・パラメータが文字列以外の場合は false が返ること。
         * </pre>
         */
        @Test
        public void test() {
            boolean result;

            QueryParameter p = new QueryParameter();
            p.setType(DataType.STRING);

            result = OpenAPIValidator.validate(p, "");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "text");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "5.5f");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, 1);
            assertThat(result, is(false));

            result = OpenAPIValidator.validate(p, 1.5f);
            assertThat(result, is(false));
        }

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.STRING を指定
         * ・Property#setMaxLength(Integer) に 10 を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・パラメータが文字列数が10以下の場合は true が返ること。
         * ・パラメータが文字列数が10以上の場合は false が返ること。
         * </pre>
         */
        @Test
        public void testMaxLength() {
            boolean result;

            QueryParameter p = new QueryParameter();
            p.setType(DataType.STRING);
            p.setMaxLength(10);

            result = OpenAPIValidator.validate(p, "");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "012345678");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "0123456789");
            assertThat(result, is(false));

            result = OpenAPIValidator.validate(p, "01234567890");
            assertThat(result, is(false));
        }

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.STRING を指定
         * ・Property#setMinLength(Integer) に 10 を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・パラメータが文字列数が10以下の場合は false が返ること。
         * ・パラメータが文字列数が10以上の場合は true が返ること。
         * </pre>
         */
        @Test
        public void testMinLength() {
            boolean result;

            QueryParameter p = new QueryParameter();
            p.setType(DataType.STRING);
            p.setMinLength(10);

            result = OpenAPIValidator.validate(p, "");
            assertThat(result, is(false));

            result = OpenAPIValidator.validate(p, "0123456789");
            assertThat(result, is(false));

            result = OpenAPIValidator.validate(p, "01234567890");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "012345678901");
            assertThat(result, is(true));
        }

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.STRING を指定
         * ・Property#setPattern(Integer) に [a-z]+ を指定 (小文字の英字のみ)
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・パラメータが文字列が小文字の英字の場合は true が返ること。
         * ・パラメータが文字列が小文字の英字以外の場合は false が返ること。
         * </pre>
         */
        @Test
        public void testPattern() {
            boolean result;

            QueryParameter p = new QueryParameter();
            p.setType(DataType.STRING);
            p.setPattern("[a-z]+");

            result = OpenAPIValidator.validate(p, "abcdefg");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "ABCDEFG");
            assertThat(result, is(false));

            result = OpenAPIValidator.validate(p, "01234567890");
            assertThat(result, is(false));
        }

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Propertyの設定】
         * ・Property#setEnum(DataType) に DataType.STRING を指定
         * ・Property#setEnum(List) に a,b,c を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・パラメータが文字列がa,b,cの場合は true が返ること。
         * ・パラメータが文字列がa,b,c以外の場合は false が返ること。
         * </pre>
         */
        @Test
        public void testEnum() {
            boolean result;

            List<Object> enums = new ArrayList<>();
            enums.add("a");
            enums.add("b");
            enums.add("c");

            QueryParameter p = new QueryParameter();
            p.setType(DataType.STRING);
            p.setEnum(enums);

            result = OpenAPIValidator.validate(p, "a");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "b");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "c");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "d");
            assertThat(result, is(false));

            result = OpenAPIValidator.validate(p, "no-define");
            assertThat(result, is(false));

            result = OpenAPIValidator.validate(p, 1);
            assertThat(result, is(false));
        }

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Propertyの設定】
         * ・Property#setEnum(DataType) に DataType.STRING を指定
         * ・Property#setFormat(DataFormat) に DataFormat.RGB を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・パラメータが文字列がRGBの場合は true が返ること。
         * ・パラメータが文字列がRGB以外の場合は false が返ること。
         * </pre>
         */
        @Test
        public void testFormatRGB() {
            boolean result;

            QueryParameter p = new QueryParameter();
            p.setType(DataType.STRING);
            p.setFormat(DataFormat.RGB);

            result = OpenAPIValidator.validate(p, "aabbcc");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "AABBCC");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "0011CC");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "ABC");
            assertThat(result, is(false));

            result = OpenAPIValidator.validate(p, "GGGGGG");
            assertThat(result, is(false));

            result = OpenAPIValidator.validate(p, "no-define");
            assertThat(result, is(false));

            result = OpenAPIValidator.validate(p, 123);
            assertThat(result, is(false));

            result = OpenAPIValidator.validate(p, "");
            assertThat(result, is(false));
        }

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Propertyの設定】
         * ・Property#setEnum(DataType) に DataType.STRING を指定
         * ・Property#setFormat(DataFormat) に DataFormat.INT32 を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・常に true が返ること。
         * ・プロファイル定義ファイルのエラーなので、妥当性としては true を返却すること。
         * </pre>
         */
        @Test
        public void testFormatInt32() {
            boolean result;

            QueryParameter p = new QueryParameter();
            p.setType(DataType.STRING);
            p.setFormat(DataFormat.INT32);

            result = OpenAPIValidator.validate(p, "");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "test");
            assertThat(result, is(true));
        }
    }

    /**
     * 配列のパラメータテスト.
     */
    @RunWith(PluginSDKTestRunner.class)
    public static class ArrayTest {
        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Itemsの設定】
         * ・Property#setType(DataType) に DataType.INTEGER を指定
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.ARRAY を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・パラメータの配列に整数値が格納されている場合は true が返ること。
         * ・パラメータの配列に整数値以外が格納されている場合は false が返ること。
         * </pre>
         */
        @Test
        public void test() {
            boolean result;

            Items items = new Items();
            items.setType(DataType.INTEGER);

            QueryParameter p = new QueryParameter();
            p.setType(DataType.ARRAY);
            p.setItems(items);

            result = OpenAPIValidator.validate(p, 10);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "10,123,1");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "a,b,c,d");
            assertThat(result, is(false));
        }

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Itemsの設定】
         * ・Property#setType(DataType) に DataType.NUMBER を指定
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.ARRAY を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・パラメータの配列に実数値が格納されている場合は true が返ること。
         * ・パラメータの配列に実数値以外が格納されている場合は false が返ること。
         * </pre>
         */
        @Test
        public void testNumber() {
            boolean result;

            Items items = new Items();
            items.setType(DataType.NUMBER);

            QueryParameter p = new QueryParameter();
            p.setType(DataType.ARRAY);
            p.setItems(items);

            result = OpenAPIValidator.validate(p, 10.5);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "10.5,123.1,1.2");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "a,b,c,d");
            assertThat(result, is(false));
        }

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Itemsの設定】
         * ・Property#setType(DataType) に DataType.STRING を指定
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.ARRAY を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・パラメータの配列に文字列が格納されている場合は true が返ること。
         * ・パラメータの配列に文字列以外が格納されている場合は false が返ること。
         * </pre>
         */
        @Test
        public void testString() {
            boolean result;

            Items items = new Items();
            items.setType(DataType.STRING);

            QueryParameter p = new QueryParameter();
            p.setType(DataType.ARRAY);
            p.setItems(items);

            result = OpenAPIValidator.validate(p, "text");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "text,abc,1,2,3");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, 10);
            assertThat(result, is(true));
        }

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Itemsの設定】
         * ・Property#setType(DataType) に DataType.INTEGER を指定
         * ・Property#setMaximum(Number) に 10 を指定
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.ARRAY を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・パラメータの配列に格納されている数値が10以下の場合は true が返ること。
         * ・パラメータの配列に格納されている数値が10以上の場合は false が返ること。
         * </pre>
         */
        @Test
        public void testMaximum() {
            boolean result;

            Items items = new Items();
            items.setType(DataType.INTEGER);
            items.setMaximum(10);

            QueryParameter p = new QueryParameter();
            p.setType(DataType.ARRAY);
            p.setItems(items);

            result = OpenAPIValidator.validate(p, 10);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "1,2,3,4,5,6,7,8,9");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "9,10,11");
            assertThat(result, is(false));

            result = OpenAPIValidator.validate(p, "a,b,c,d");
            assertThat(result, is(false));
        }

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Itemsの設定】
         * ・Property#setType(DataType) に DataType.INTEGER を指定
         * ・Property#setMinimum(Number) に 10 を指定
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.ARRAY を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・パラメータの配列に格納されている数値が10以上の場合は true が返ること。
         * ・パラメータの配列に格納されている数値が10以下の場合は false が返ること。
         * </pre>
         */
        @Test
        public void testMinimum() {
            boolean result;

            Items items = new Items();
            items.setType(DataType.INTEGER);
            items.setMinimum(10);

            QueryParameter p = new QueryParameter();
            p.setType(DataType.ARRAY);
            p.setItems(items);

            result = OpenAPIValidator.validate(p, 11);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "11,12,13,14");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "9,10,11");
            assertThat(result, is(false));

            result = OpenAPIValidator.validate(p, "a,b,c,d");
            assertThat(result, is(false));
        }

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Itemsの設定】
         * ・Property#setType(DataType) に DataType.INTEGER を指定
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.ARRAY を指定
         * ・Property#setAllowEmptyValue(Boolean) に true を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・パラメータの配列が空の場合は true が返ること。
         * </pre>
         */
        @Test
        public void testAllowEmptyValueWithTrue() {
            boolean result;

            Items items = new Items();
            items.setType(DataType.INTEGER);

            QueryParameter p = new QueryParameter();
            p.setType(DataType.ARRAY);
            p.setItems(items);
            p.setAllowEmptyValue(true);

            result = OpenAPIValidator.validate(p, "");
            assertThat(result, is(true));
        }

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Itemsの設定】
         * ・Property#setType(DataType) に DataType.INTEGER を指定
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.ARRAY を指定
         * ・Property#setAllowEmptyValue(Boolean) に false を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・パラメータの配列が空の場合は false が返ること。
         * </pre>
         */
        @Test
        public void testAllowEmptyValueWithFalse() {
            boolean result;

            Items items = new Items();
            items.setType(DataType.INTEGER);

            QueryParameter p = new QueryParameter();
            p.setType(DataType.ARRAY);
            p.setItems(items);
            p.setAllowEmptyValue(false);

            result = OpenAPIValidator.validate(p, "");
            assertThat(result, is(false));
        }

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Itemsの設定】
         * ・Property#setType(DataType) に DataType.INTEGER を指定
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.ARRAY を指定
         * ・Property#setMaxItems(Integer) に 5 を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・パラメータの配列のサイズが 5 以下の場合は true が返ること。
         * ・パラメータの配列のサイズが 5 以上の場合は false が返ること。
         * </pre>
         */
        @Test
        public void testMaxItems() {
            boolean result;

            Items items = new Items();
            items.setType(DataType.INTEGER);

            QueryParameter p = new QueryParameter();
            p.setType(DataType.ARRAY);
            p.setItems(items);
            p.setMaxItems(5);

            result = OpenAPIValidator.validate(p, "10,123,1,12");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "10,123,1,12,32");
            assertThat(result, is(false));
        }

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Itemsの設定】
         * ・Property#setType(DataType) に DataType.INTEGER を指定
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.ARRAY を指定
         * ・Property#setMinItems(Integer) に 5 を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・パラメータの配列のサイズが 5 以上の場合は true が返ること。
         * ・パラメータの配列のサイズが 5 以下の場合は false が返ること。
         * </pre>
         */
        @Test
        public void testMinItems() {
            boolean result;

            Items items = new Items();
            items.setType(DataType.INTEGER);

            QueryParameter p = new QueryParameter();
            p.setType(DataType.ARRAY);
            p.setItems(items);
            p.setMinItems(5);

            result = OpenAPIValidator.validate(p, "10,123,1,12");
            assertThat(result, is(false));

            result = OpenAPIValidator.validate(p, "10,123,1,12,32");
            assertThat(result, is(true));
        }

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Itemsの設定】
         * ・Property#setType(DataType) に DataType.INTEGER を指定
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.ARRAY を指定
         * ・Property#setMinItems(Integer) に 5 を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・パラメータの配列の要素に整数の場合は true が返ること。
         * ・パラメータの配列の要素に文字列の場合は false が返ること。
         * </pre>
         */
        @Test
        public void testArrayString() {
            boolean result;

            Items items = new Items();
            items.setType(DataType.INTEGER);

            QueryParameter p = new QueryParameter();
            p.setType(DataType.ARRAY);
            p.setItems(items);

            result = OpenAPIValidator.validate(p, "1,2,3");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "a,b,c,d");
            assertThat(result, is(false));
        }

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Itemsの設定】
         * ・Property#setType(DataType) に DataType.INTEGER を指定
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.ARRAY を指定
         * ・Property#setUniqueItems(Boolean) に true を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・パラメータの配列の要素に同じ値が無いの場合は true が返ること。
         * ・パラメータの配列の要素に同じ値が有るの場合は false が返ること。
         * </pre>
         */
        @Test
        public void testUniqueItemsWithTrue() {
            boolean result;

            Items items = new Items();
            items.setType(DataType.INTEGER);

            QueryParameter p = new QueryParameter();
            p.setType(DataType.ARRAY);
            p.setItems(items);
            p.setUniqueItems(true);

            result = OpenAPIValidator.validate(p, "1,2,3");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "1,1,3");
            assertThat(result, is(false));
        }

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Itemsの設定】
         * ・Property#setType(DataType) に DataType.INTEGER を指定
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.ARRAY を指定
         * ・Property#setUniqueItems(Boolean) に false を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・パラメータの配列の要素に同じ値が無いの場合は true が返ること。
         * ・パラメータの配列の要素に同じ値が有るの場合は true が返ること。
         * </pre>
         */
        @Test
        public void testUniqueItemsWithFalse() {
            boolean result;

            Items items = new Items();
            items.setType(DataType.INTEGER);

            QueryParameter p = new QueryParameter();
            p.setType(DataType.ARRAY);
            p.setItems(items);
            p.setUniqueItems(false);

            result = OpenAPIValidator.validate(p, "1,2,3");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "1,1,3");
            assertThat(result, is(true));
        }

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Itemsの設定】
         * ・Property#setType(DataType) に DataType.INTEGER を指定
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.ARRAY を指定
         * ・Property#setCollectionFormat(String) に csv を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・パラメータの配列のフォーマットがカンマ区切りの場合は true が返ること。
         * ・パラメータの配列のフォーマットがカンマ区切り以外の場合は false が返ること。
         * </pre>
         */
        @Test
        public void testCollectionFormatWithCSV() {
            boolean result;

            Items items = new Items();
            items.setType(DataType.INTEGER);

            QueryParameter p = new QueryParameter();
            p.setType(DataType.ARRAY);
            p.setItems(items);
            p.setCollectionFormat("csv");

            result = OpenAPIValidator.validate(p, "1,2,3");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "1\t2\t3");
            assertThat(result, is(false));
        }

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Itemsの設定】
         * ・Property#setType(DataType) に DataType.INTEGER を指定
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.ARRAY を指定
         * ・Property#setCollectionFormat(String) に ssv を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・パラメータの配列のフォーマットがスペース区切りの場合は true が返ること。
         * ・パラメータの配列のフォーマットがスペース区切り以外の場合は false が返ること。
         * </pre>
         */
        @Test
        public void testCollectionFormatWithSSV() {
            boolean result;

            Items items = new Items();
            items.setType(DataType.INTEGER);

            QueryParameter p = new QueryParameter();
            p.setType(DataType.ARRAY);
            p.setItems(items);
            p.setCollectionFormat("ssv");

            result = OpenAPIValidator.validate(p, "1 2 3");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "1,2,3");
            assertThat(result, is(false));
        }

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Itemsの設定】
         * ・Property#setType(DataType) に DataType.INTEGER を指定
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.ARRAY を指定
         * ・Property#setCollectionFormat(String) に tsv を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・パラメータの配列のフォーマットがタブ区切りの場合は true が返ること。
         * ・パラメータの配列のフォーマットがタブ区切り以外の場合は false が返ること。
         * </pre>
         */
        @Test
        public void testCollectionFormatWithTSV() {
            boolean result;

            Items items = new Items();
            items.setType(DataType.INTEGER);

            QueryParameter p = new QueryParameter();
            p.setType(DataType.ARRAY);
            p.setItems(items);
            p.setCollectionFormat("tsv");

            result = OpenAPIValidator.validate(p, "1\t2\t3");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "1,2,3");
            assertThat(result, is(false));
        }

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Itemsの設定】
         * ・Property#setType(DataType) に DataType.INTEGER を指定
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.ARRAY を指定
         * ・Property#setCollectionFormat(String) に pipes を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・パラメータの配列のフォーマットがパイプ区切りの場合は true が返ること。
         * ・パラメータの配列のフォーマットがパイプ区切り以外の場合は false が返ること。
         * </pre>
         */
        @Test
        public void testCollectionFormatWithPIPES() {
            boolean result;

            Items items = new Items();
            items.setType(DataType.INTEGER);

            QueryParameter p = new QueryParameter();
            p.setType(DataType.ARRAY);
            p.setItems(items);
            p.setCollectionFormat("pipes");

            result = OpenAPIValidator.validate(p, "1|2|3");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "1,2,3");
            assertThat(result, is(false));
        }

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Itemsの設定】
         * ・Property#setType(DataType) に DataType.INTEGER を指定
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.ARRAY を指定
         * ・Property#setCollectionFormat(String) に csv を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・パラメータの配列のフォーマットが不正なの場合は false が返ること。
         * </pre>
         */
        @Test
        public void testInvalidFormat() {
            boolean result;

            Items items = new Items();
            items.setType(DataType.INTEGER);

            QueryParameter p = new QueryParameter();
            p.setType(DataType.ARRAY);
            p.setItems(items);

            result = OpenAPIValidator.validate(p, "1,,2,3");
            assertThat(result, is(false));
        }

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Itemsの設定】
         * ・無し
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.ARRAY を指定
         * ・Property#setCollectionFormat(String) に csv を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・配列の場合に items が指定されないのは、定義ファイルのエラーなので、妥当性は true を返す。
         * </pre>
         */
        @Test
        public void testNotSetItems() {
            boolean result;

            QueryParameter p = new QueryParameter();
            p.setType(DataType.ARRAY);

            result = OpenAPIValidator.validate(p, 10);
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "10,123,1");
            assertThat(result, is(true));

            result = OpenAPIValidator.validate(p, "text");
            assertThat(result, is(true));
        }

        /**
         * 以下の設定で、OpenAPIValidator#validte(Property, Object) の第2引数に値を指定して呼び出す。
         * <pre>
         * 【Itemsの設定】
         * ・無し
         * 【Propertyの設定】
         * ・Property#setType(DataType) に DataType.ARRAY を指定
         * </pre>
         * <pre>
         * 【期待する動作】
         * ・値が必須でないので、trueを返すこと。
         * </pre>
         */
        @Test
        public void testNull() {
            boolean result;

            Items items = new Items();
            items.setType(DataType.INTEGER);

            QueryParameter p = new QueryParameter();
            p.setType(DataType.ARRAY);
            p.setItems(items);

            result = OpenAPIValidator.validate(p, null);
            assertThat(result, is(true));
        }
    }
}
