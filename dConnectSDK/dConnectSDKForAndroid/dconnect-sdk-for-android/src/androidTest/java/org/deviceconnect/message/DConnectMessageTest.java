/*
 DConnectMessageTest.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.message;

import android.content.Intent;
import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * DConnectMessageのテスト.
 *
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class DConnectMessageTest {

    /**
     * プリミティブ型の値が格納されているJSONの文字列からDConnectMessageを生成することを確認する。
     * <pre>
     * 【期待する動作】
     * ・BasicDConnectMessageのインスタンスが生成されること。
     * ・JSONの階層がBasicDConnectMessageに反映されていること。
     * </pre>
     */
    @Test
    public void BasicDConnectMessage_text_primitive() throws Exception {
        final String strKey = "strKey";
        final String strValue = "value";

        final String intKey = "intKey";
        final int intValue = Integer.MAX_VALUE;

        final String longKey = "longKey";
        final long longValue = Long.MAX_VALUE;

        final String floatKey = "floatKey";
        final float floatValue = 1.23f;

        final String doubleKey = "doubleKey";
        final double doubleValue = 3.21d;

        final String booleanKey = "booleanKey";
        final boolean booleanValue = true;

        final String json = "{" +
                "\"" + strKey + "\" : \"" + strValue + "\"," +
                "\"" + intKey + "\" : " + intValue + "," +
                "\"" + longKey + "\" : " + longValue + "," +
                "\"" + floatKey + "\" : " + floatValue + "," +
                "\"" + doubleKey + "\" : " + doubleValue + "," +
                "\"" + booleanKey + "\" : " + booleanValue +
                "}";

        DConnectMessage message = new BasicDConnectMessage(json);
        assertThat(message.getString(strKey), is(strValue));
        assertThat(message.getInt(intKey), is(intValue));
        assertThat(message.getLong(longKey), is(longValue));
        assertThat(message.getFloat(floatKey), is(floatValue));
        assertThat(message.getDouble(doubleKey), is(doubleValue));
        assertThat(message.getBoolean(booleanKey), is(booleanValue));
    }

    /**
     * 配列が格納されているJSONからDConnectMessageを生成することを確認する。
     * <pre>
     * 【期待する動作】
     * ・BasicDConnectMessageのインスタンスが生成されること。
     * ・プリミティブ型のリストが生成されていること。
     * </pre>
     */
    @Test
    public void BasicDConnectMessage_text_array() throws Exception {
        final String strArrayKey = "strArrayKey";
        final String[] strArrayValue = {"value1", "value2", "value3"};

        final String intArrayKey = "intArrayKey";
        final int[] intArrayValue = {0, Integer.MAX_VALUE, Integer.MIN_VALUE};

        final String longArrayKey = "longArrayKey";
        final long[] longArrayValue = {0L, Long.MAX_VALUE, Long.MIN_VALUE};

        final String doubleArrayKey = "doubleArrayKey";
        final double[] doubleArrayValue = {0d, Double.MAX_VALUE, Double.MIN_VALUE};

        final String booleanArrayKey = "booleanArrayKey";
        final boolean[] booleanArrayValue = {true, false, true};

        final String json = "{" +
                "\"" + strArrayKey + "\" : [\"" + strArrayValue[0] + "\",\"" + strArrayValue[1] + "\",\"" + strArrayValue[2] + "\"]," +
                "\"" + intArrayKey + "\" : [" + intArrayValue[0] + "," + intArrayValue[1] + "," + intArrayValue[2] + "]," +
                "\"" + longArrayKey + "\" : [" + longArrayValue[0] + "," + longArrayValue[1] + "," + longArrayValue[2] + "]," +
                "\"" + doubleArrayKey + "\" : [" + doubleArrayValue[0] + "," + doubleArrayValue[1] + "," + doubleArrayValue[2] + "]," +
                "\"" + booleanArrayKey + "\" : [" + booleanArrayValue[0] + "," + booleanArrayValue[1] + "," + booleanArrayValue[2] + "]" +
                "}";

        DConnectMessage message = new BasicDConnectMessage(json);
        assertThat(message.getList(strArrayKey), is(asList(strArrayValue)));
        assertThat(message.getList(intArrayKey), is(asList(intArrayValue)));
        List longList = message.getList(longArrayKey);
        for (int i = 0; i < longList.size(); i++) {
            Number a = (Number) longList.get(i);
            assertThat(a.longValue(), is(longArrayValue[i]));
        }
        List doubleList = message.getList(doubleArrayKey);
        for (int i = 0; i < doubleList.size(); i++) {
            Double a = (Double) doubleList.get(i);
            assertThat(a, closeTo(doubleArrayValue[i], 0.001d));
        }
        assertThat(message.getList(booleanArrayKey), is(asList(booleanArrayValue)));
    }

    /**
     * プリミティブ型の値が格納されているJSONからDConnectMessageを生成することを確認する。
     * <pre>
     * 【期待する動作】
     * ・BasicDConnectMessageのインスタンスが生成されること。
     * ・JSONの階層がBasicDConnectMessageに反映されていること。
     * </pre>
     */
    @Test
    public void BasicDConnectMessage_json_primitive() throws Exception {
        final String strKey = "strKey";
        final String strValue = "value";

        final String intKey = "intKey";
        final int intValue = Integer.MAX_VALUE;

        final String longKey = "longKey";
        final long longValue = Long.MAX_VALUE;

        final String floatKey = "floatKey";
        final float floatValue = 1.23f;

        final String doubleKey = "doubleKey";
        final double doubleValue = 3.21d;

        final String booleanKey = "booleanKey";
        final boolean booleanValue = true;

        JSONObject json = new JSONObject();
        json.put(strKey, strValue);
        json.put(intKey, intValue);
        json.put(longKey, longValue);
        json.put(floatKey, floatValue);
        json.put(doubleKey, doubleValue);
        json.put(booleanKey, booleanValue);

        DConnectMessage message = new BasicDConnectMessage(json);
        assertThat(message.getString(strKey), is(strValue));
        assertThat(message.getInt(intKey), is(intValue));
        assertThat(message.getLong(longKey), is(longValue));
        assertThat(message.getFloat(floatKey), is(floatValue));
        assertThat(message.getDouble(doubleKey), is(doubleValue));
        assertThat(message.getBoolean(booleanKey), is(booleanValue));
    }

    /**
     * 配列が格納されているJSONからDConnectMessageを生成することを確認する。
     * <pre>
     * 【期待する動作】
     * ・BasicDConnectMessageのインスタンスが生成されること。
     * ・プリミティブ型のリストが生成されていること。
     * </pre>
     */
    @Test
    public void BasicDConnectMessage_json_array() throws Exception {
        final String strArrayKey = "strArrayKey";
        final String[] strArrayValue = {"value1", "value2", "value3"};

        final String intArrayKey = "intArrayKey";
        final int[] intArrayValue = {0, Integer.MAX_VALUE, Integer.MIN_VALUE};

        final String longArrayKey = "longArrayKey";
        final long[] longArrayValue = {0l, Long.MAX_VALUE, Long.MIN_VALUE};

        final String floatArrayKey = "floatArrayKey";
        final float[] floatArrayValue = {0f, Float.MAX_VALUE, Float.MIN_VALUE};

        final String doubleArrayKey = "doubleArrayKey";
        final double[] doubleArrayValue = {0d, Double.MAX_VALUE, Double.MIN_VALUE};

        final String booleanArrayKey = "booleanArrayKey";
        final boolean[] booleanArrayValue = {true, false, true};

        JSONObject json = new JSONObject();
        json.put(strArrayKey, asArray(strArrayValue));
        json.put(intArrayKey, asArray(intArrayValue));
        json.put(longArrayKey, asArray(longArrayValue));
        json.put(floatArrayKey, asArray(floatArrayValue));
        json.put(doubleArrayKey, asArray(doubleArrayValue));
        json.put(booleanArrayKey, asArray(booleanArrayValue));

        DConnectMessage message = new BasicDConnectMessage(json);
        assertThat(message.getList(strArrayKey), is(asList(strArrayValue)));
        assertThat(message.getList(intArrayKey), is(asList(intArrayValue)));
        assertThat(message.getList(longArrayKey), is(asList(longArrayValue)));
        List floatList = message.getList(floatArrayKey);
        for (int i = 0; i < floatList.size(); i++) {
            Double a = (Double) floatList.get(i);
            assertThat(a, closeTo(floatArrayValue[i], 0.001d));
        }
        List doubleList = message.getList(doubleArrayKey);
        for (int i = 0; i < doubleList.size(); i++) {
            Double a = (Double) doubleList.get(i);
            assertThat(a, closeTo(doubleArrayValue[i], 0.001));
        }
        assertThat(message.getList(booleanArrayKey), is(asList(booleanArrayValue)));
    }

    /**
     * オブジェクトを含むJSONからDConnectMessageを生成することを確認する。
     * <pre>
     * 【期待する動作】
     * ・DConnectMessageのオブジェクト含まれたBasicDConnectMessageのインスタンスが生成されること。
     * </pre>
     */
    @Test
    public void BasicDConnectMessage_json_object() throws Exception {
        final String objectKey = "objectKey";

        final String strKey = "strKey";
        final String strValue = "value";

        final String intKey = "intKey";
        final int intValue = Integer.MAX_VALUE;

        final String longKey = "longKey";
        final long longValue = Long.MAX_VALUE;

        final String floatKey = "floatKey";
        final float floatValue = 1.23f;

        final String doubleKey = "doubleKey";
        final double doubleValue = 3.21d;

        final String booleanKey = "booleanKey";
        final boolean booleanValue = true;

        JSONObject object = new JSONObject();
        object.put(strKey, strValue);
        object.put(intKey, intValue);
        object.put(longKey, longValue);
        object.put(floatKey, floatValue);
        object.put(doubleKey, doubleValue);
        object.put(booleanKey, booleanValue);

        JSONObject json = new JSONObject();
        json.put(objectKey, object);

        DConnectMessage message = new BasicDConnectMessage(json);
        DConnectMessage obj = message.getMessage(objectKey);
        assertThat(obj.getString(strKey), is(strValue));
        assertThat(obj.getInt(intKey), is(intValue));
        assertThat(obj.getLong(longKey), is(longValue));
        assertThat(obj.getFloat(floatKey), is(floatValue));
        assertThat(obj.getDouble(doubleKey), is(doubleValue));
        assertThat(obj.getBoolean(booleanKey), is(booleanValue));
    }


    /**
     * 配列を持つオブジェクトを含むJSONからDConnectMessageを生成することを確認する。
     * <pre>
     * 【期待する動作】
     * ・BasicDConnectMessageのインスタンスが生成されること。
     * ・空の配列が生成されていること。
     * </pre>
     */
    @Test
    public void BasicDConnectMessage_json_array_in_object() throws Exception {
        final String objectKey = "objectKey";

        final String strArrayKey = "strArrayKey";
        final String[] strArrayValue = {"value1", "value2", "value3"};

        final String intArrayKey = "intArrayKey";
        final int[] intArrayValue = {0, Integer.MAX_VALUE, Integer.MIN_VALUE};

        final String longArrayKey = "longArrayKey";
        final long[] longArrayValue = {0l, Long.MAX_VALUE, Long.MIN_VALUE};

        final String floatArrayKey = "floatArrayKey";
        final float[] floatArrayValue = {0f, Float.MAX_VALUE, Float.MIN_VALUE};

        final String doubleArrayKey = "doubleArrayKey";
        final double[] doubleArrayValue = {0d, Double.MAX_VALUE, Double.MIN_VALUE};

        final String booleanArrayKey = "booleanArrayKey";
        final boolean[] booleanArrayValue = {true, false, true};

        JSONObject object = new JSONObject();
        object.put(strArrayKey, asArray(strArrayValue));
        object.put(intArrayKey, asArray(intArrayValue));
        object.put(longArrayKey, asArray(longArrayValue));
        object.put(floatArrayKey, asArray(floatArrayValue));
        object.put(doubleArrayKey, asArray(doubleArrayValue));
        object.put(booleanArrayKey, asArray(booleanArrayValue));

        JSONObject json = new JSONObject();
        json.put(objectKey, object);

        DConnectMessage message = new BasicDConnectMessage(json);
        DConnectMessage obj = message.getMessage(objectKey);
        assertThat(obj.getList(strArrayKey), is(asList(strArrayValue)));
        assertThat(obj.getList(intArrayKey), is(asList(intArrayValue)));
        assertThat(obj.getList(longArrayKey), is(asList(longArrayValue)));
        List floatList = obj.getList(floatArrayKey);
        for (int i = 0; i < floatList.size(); i++) {
            Double a = (Double) floatList.get(i);
            assertThat(a, closeTo(floatArrayValue[i], 0.001));
        }
        List doubleList = obj.getList(doubleArrayKey);
        for (int i = 0; i < doubleList.size(); i++) {
            Double a = (Double) doubleList.get(i);
            assertThat(a, closeTo(doubleArrayValue[i], 0.001d));
        }
        assertThat(obj.getList(booleanArrayKey), is(asList(booleanArrayValue)));
    }

    /**
     * 3階層のオブジェクトを含むJSONからDConnectMessageを生成することを確認する。
     * <pre>
     * 【期待する動作】
     * ・3階層になっているDConnectMessageを含むBasicDConnectMessageのインスタンスが生成されること。
     * </pre>
     */
    @Test
    public void BasicDConnectMessage_json_object_recursive() throws Exception {
        final String objectKey1 = "objectKey1";
        final String objectKey2 = "objectKey2";
        final String objectKey3 = "objectKey3";

        final String strArrayKey = "strArrayKey";
        final String[] strArrayValue = {"value1", "value2", "value3"};

        final String intArrayKey = "intArrayKey";
        final int[] intArrayValue = {0, Integer.MAX_VALUE, Integer.MIN_VALUE};

        final String longArrayKey = "longArrayKey";
        final long[] longArrayValue = {0l, Long.MAX_VALUE, Long.MIN_VALUE};

        final String floatArrayKey = "floatArrayKey";
        final float[] floatArrayValue = {0f, Float.MAX_VALUE, Float.MIN_VALUE};

        final String doubleArrayKey = "doubleArrayKey";
        final double[] doubleArrayValue = {0d, Double.MAX_VALUE, Double.MIN_VALUE};

        final String booleanArrayKey = "booleanArrayKey";
        final boolean[] booleanArrayValue = {true, false, true};

        JSONObject object1 = new JSONObject();
        object1.put(strArrayKey, asArray(strArrayValue));
        object1.put(intArrayKey, asArray(intArrayValue));
        object1.put(longArrayKey, asArray(longArrayValue));
        object1.put(floatArrayKey, asArray(floatArrayValue));
        object1.put(doubleArrayKey, asArray(doubleArrayValue));
        object1.put(booleanArrayKey, asArray(booleanArrayValue));

        JSONObject object2 = new JSONObject();
        object2.put(objectKey1, object1);

        JSONObject object3 = new JSONObject();
        object3.put(objectKey2, object2);

        JSONObject json = new JSONObject();
        json.put(objectKey3, object3);

        DConnectMessage message = new BasicDConnectMessage(json);
        DConnectMessage obj3 = message.getMessage(objectKey3);
        DConnectMessage obj2 = obj3.getMessage(objectKey2);
        DConnectMessage obj = obj2.getMessage(objectKey1);
        assertThat(obj.getList(strArrayKey), equalTo(asList(strArrayValue)));
        assertThat(obj.getList(intArrayKey), equalTo(asList(intArrayValue)));
        assertThat(obj.getList(longArrayKey), equalTo(asList(longArrayValue)));
        List floatList = obj.getList(floatArrayKey);
        for (int i = 0; i < floatList.size(); i++) {
            Double a = (Double) floatList.get(i);
            assertThat(a, closeTo(floatArrayValue[i], 0.001d));
        }
        List doubleList = obj.getList(doubleArrayKey);
        for (int i = 0; i < doubleList.size(); i++) {
            Double a = (Double) doubleList.get(i);
            assertThat(a, closeTo(doubleArrayValue[i], 0.001d));
        }
        assertThat(obj.getList(booleanArrayKey), equalTo(asList(booleanArrayValue)));
    }


    /**
     * プリミティブ型の値が格納されているIntentからDConnectMessageを生成することを確認する。
     * <pre>
     * 【期待する動作】
     * ・BasicDConnectMessageのインスタンスが生成されること。
     * ・Intentの階層がBasicDConnectMessageに反映されていること。
     * </pre>
     */
    @Test
    public void BasicDConnectMessage_intent_primitive() throws Exception {
        final String strKey = "strKey";
        final String strValue = "value";

        final String intKey = "intKey";
        final int intValue = Integer.MAX_VALUE;

        final String longKey = "longKey";
        final long longValue = Long.MAX_VALUE;

        final String floatKey = "floatKey";
        final float floatValue = 1.23f;

        final String doubleKey = "doubleKey";
        final double doubleValue = 3.21d;

        final String booleanKey = "booleanKey";
        final boolean booleanValue = true;

        Intent intent = new Intent();
        intent.putExtra(strKey, strValue);
        intent.putExtra(intKey, intValue);
        intent.putExtra(longKey, longValue);
        intent.putExtra(floatKey, floatValue);
        intent.putExtra(doubleKey, doubleValue);
        intent.putExtra(booleanKey, booleanValue);

        DConnectMessage message = new BasicDConnectMessage(intent);
        assertThat(message.getString(strKey), is(strValue));
        assertThat(message.getInt(intKey), is(intValue));
        assertThat(message.getLong(longKey), is(longValue));
        assertThat(message.getFloat(floatKey), is(floatValue));
        assertThat(message.getDouble(doubleKey), is(doubleValue));
        assertThat(message.getBoolean(booleanKey), is(booleanValue));
    }

    /**
     * 配列が格納されているIntentからDConnectMessageを生成することを確認する。
     * <pre>
     * 【期待する動作】
     * ・BasicDConnectMessageのインスタンスが生成されること。
     * ・プリミティブ型のリストが生成されていること。
     * </pre>
     */
    @Test
    public void BasicDConnectMessage_intent_array() throws Exception {
        final String strArrayKey = "strArrayKey";
        final String[] strArrayValue = {"value1", "value2", "value3"};

        final String intArrayKey = "intArrayKey";
        final int[] intArrayValue = {0, Integer.MAX_VALUE, Integer.MIN_VALUE};

        final String longArrayKey = "longArrayKey";
        final long[] longArrayValue = {0l, Long.MAX_VALUE, Long.MIN_VALUE};

        final String floatArrayKey = "floatArrayKey";
        final float[] floatArrayValue = {0f, Float.MAX_VALUE, Float.MIN_VALUE};

        final String doubleArrayKey = "doubleArrayKey";
        final double[] doubleArrayValue = {0d, Double.MAX_VALUE, Double.MIN_VALUE};

        final String booleanArrayKey = "booleanArrayKey";
        final boolean[] booleanArrayValue = {true, false, true};

        Intent intent = new Intent();
        intent.putExtra(strArrayKey, strArrayValue);
        intent.putExtra(intArrayKey, intArrayValue);
        intent.putExtra(longArrayKey, longArrayValue);
        intent.putExtra(floatArrayKey, floatArrayValue);
        intent.putExtra(doubleArrayKey, doubleArrayValue);
        intent.putExtra(booleanArrayKey, booleanArrayValue);

        DConnectMessage message = new BasicDConnectMessage(intent);
        assertThat(message.getList(strArrayKey), is(asList(strArrayValue)));
        assertThat(message.getList(intArrayKey), is(asList(intArrayValue)));
        assertThat(message.getList(longArrayKey), is(asList(longArrayValue)));
        List floatList = message.getList(floatArrayKey);
        for (int i = 0; i < floatList.size(); i++) {
            Float a = (Float) floatList.get(i);
            assertThat(a.doubleValue(), closeTo(floatArrayValue[i], 0.001d));
        }
        List doubleList = message.getList(doubleArrayKey);
        for (int i = 0; i < doubleList.size(); i++) {
            Double a = (Double) doubleList.get(i);
            assertThat(a, closeTo(doubleArrayValue[i], 0.001d));
        }
        assertThat(message.getList(booleanArrayKey), is(asList(booleanArrayValue)));
    }

    /**
     * オブジェクトを含むIntentからDConnectMessageを生成することを確認する。
     * <pre>
     * 【期待する動作】
     * ・DConnectMessageのオブジェクト含まれたBasicDConnectMessageのインスタンスが生成されること。
     * </pre>
     */
    @Test
    public void BasicDConnectMessage_intent_object() throws Exception {
        final String bundleKey = "bundleKey";

        final String strKey = "strKey";
        final String strValue = "value";

        final String intKey = "intKey";
        final int intValue = Integer.MAX_VALUE;

        final String longKey = "longKey";
        final long longValue = Long.MAX_VALUE;

        final String floatKey = "floatKey";
        final float floatValue = 1.23f;

        final String doubleKey = "doubleKey";
        final double doubleValue = 3.21d;

        final String booleanKey = "booleanKey";
        final boolean booleanValue = true;

        Bundle bundle = new Bundle();
        bundle.putString(strKey, strValue);
        bundle.putInt(intKey, intValue);
        bundle.putLong(longKey, longValue);
        bundle.putFloat(floatKey, floatValue);
        bundle.putDouble(doubleKey, doubleValue);
        bundle.putBoolean(booleanKey, booleanValue);

        Intent intent = new Intent();
        intent.putExtra(bundleKey, bundle);

        DConnectMessage message = new BasicDConnectMessage(intent);
        DConnectMessage a = message.getMessage(bundleKey);
        assertThat(a, is(notNullValue()));
        assertThat(a.getString(strKey), is(strValue));
        assertThat(a.getInt(intKey), is(intValue));
        assertThat(a.getLong(longKey), is(longValue));
        assertThat(a.getFloat(floatKey), is(floatValue));
        assertThat(a.getDouble(doubleKey), is(doubleValue));
        assertThat(a.getBoolean(booleanKey), is(booleanValue));
    }

    /**
     * 3階層のオブジェクトを含むJSONからDConnectMessageを生成することを確認する。
     * <pre>
     * 【期待する動作】
     * ・3階層になっているDConnectMessageを含むBasicDConnectMessageのインスタンスが生成されること。
     * </pre>
     */
    @Test
    public void BasicDConnectMessage_intent_array_in_object() throws Exception {
        final String bundleKey = "bundleKey";

        final String strArrayKey = "strArrayKey";
        final String[] strArrayValue = {"value1", "value2", "value3"};

        final String intArrayKey = "intArrayKey";
        final int[] intArrayValue = {0, Integer.MAX_VALUE, Integer.MIN_VALUE};

        final String longArrayKey = "longArrayKey";
        final long[] longArrayValue = {0l, Long.MAX_VALUE, Long.MIN_VALUE};

        final String floatArrayKey = "floatArrayKey";
        final float[] floatArrayValue = {0f, Float.MAX_VALUE, Float.MIN_VALUE};

        final String doubleArrayKey = "doubleArrayKey";
        final double[] doubleArrayValue = {0d, Double.MAX_VALUE, Double.MIN_VALUE};

        final String booleanArrayKey = "booleanArrayKey";
        final boolean[] booleanArrayValue = {true, false, true};

        Bundle bundle = new Bundle();
        bundle.putStringArray(strArrayKey, strArrayValue);
        bundle.putIntArray(intArrayKey, intArrayValue);
        bundle.putLongArray(longArrayKey, longArrayValue);
        bundle.putFloatArray(floatArrayKey, floatArrayValue);
        bundle.putDoubleArray(doubleArrayKey, doubleArrayValue);
        bundle.putBooleanArray(booleanArrayKey, booleanArrayValue);

        Intent intent = new Intent();
        intent.putExtra(bundleKey, bundle);

        DConnectMessage message = new BasicDConnectMessage(intent);
        DConnectMessage obj = message.getMessage(bundleKey);
        assertThat(obj, is(notNullValue()));
        assertThat(obj.getList(strArrayKey), is(asList(strArrayValue)));
        assertThat(obj.getList(intArrayKey), is(asList(intArrayValue)));
        assertThat(obj.getList(longArrayKey), is(asList(longArrayValue)));
        List floatList = obj.getList(floatArrayKey);
        for (int i = 0; i < floatList.size(); i++) {
            Float a = (Float) floatList.get(i);
            assertThat(a.doubleValue(), closeTo(floatArrayValue[i], 0.001d));
        }
        List doubleList = obj.getList(doubleArrayKey);
        for (int i = 0; i < doubleList.size(); i++) {
            Double a = (Double) doubleList.get(i);
            assertThat(a, closeTo(doubleArrayValue[i], 0.001d));
        }
        assertThat(obj.getList(booleanArrayKey), is(asList(booleanArrayValue)));
    }

    private static JSONArray asArray(final String[] array) throws JSONException {
        JSONArray list = new JSONArray();
        for (int i = 0; i < array.length; i++) {
            list.put(array[i]);
        }
        return list;
    }

    private static JSONArray asArray(final int[] array) throws JSONException  {
        JSONArray list = new JSONArray();
        for (int i = 0; i < array.length; i++) {
            list.put(array[i]);
        }
        return list;
    }

    private static JSONArray asArray(final long[] array) throws JSONException  {
        JSONArray list = new JSONArray();
        for (int i = 0; i < array.length; i++) {
            list.put(array[i]);
        }
        return list;
    }

    private static JSONArray asArray(final float[] array) throws JSONException  {
        JSONArray list = new JSONArray();
        for (int i = 0; i < array.length; i++) {
            list.put(array[i]);
        }
        return list;
    }

    private static JSONArray asArray(final double[] array) throws JSONException  {
        JSONArray list = new JSONArray();
        for (int i = 0; i < array.length; i++) {
            list.put(array[i]);
        }
        return list;
    }

    private static JSONArray asArray(final boolean[] array) throws JSONException  {
        JSONArray list = new JSONArray();
        for (int i = 0; i < array.length; i++) {
            list.put(array[i]);
        }
        return list;
    }

    private static List<Object> asList(String[] array) {
        List<Object> list = new ArrayList<>(array.length);
        for (int i = 0; i < array.length; i++) {
            list.add(array[i]);
        }
        return list;
    }

    private static List<Object> asList(int[] array) {
        List<Object> list = new ArrayList<>(array.length);
        for (int i = 0; i < array.length; i++) {
            list.add(array[i]);
        }
        return list;
    }

    private static List<Object> asList(long[] array) {
        List<Object> list = new ArrayList<>(array.length);
        for (int i = 0; i < array.length; i++) {
            list.add(array[i]);
        }
        return list;
    }

    private static List<Object> asList(float[] array) {
        List<Object> list = new ArrayList<>(array.length);
        for (int i = 0; i < array.length; i++) {
            list.add(array[i]);
        }
        return list;
    }

    private static List<Object> asList(double[] array) {
        List<Object> list = new ArrayList<>(array.length);
        for (int i = 0; i < array.length; i++) {
            list.add(array[i]);
        }
        return list;
    }

    private static List<Object> asList(boolean[] array) {
        List<Object> list = new ArrayList<>(array.length);
        for (int i = 0; i < array.length; i++) {
            list.add(array[i]);
        }
        return list;
    }
}

