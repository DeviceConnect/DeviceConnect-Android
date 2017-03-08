/*
 JSONConversionTest.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.test;

import android.os.Bundle;
import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.deviceconnect.android.profile.restful.test.RESTfulDConnectTestCase;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.DConnectResponseMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


/**
 * {@link Bundle}を{@link JSONObject}に変換するテストを実行する.
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class JSONConversionTest extends RESTfulDConnectTestCase {

    /**
     * テスト用浮動小数点値: {@value}.
     */
    private static final double TEST_FLOATING_VALUE = 0.01;

    /**
     * dConnectManagerがBundleをJSONへ正しく変換していることを確認するテスト.
     * 
     * @throws JSONException レスポンスの解析に失敗した場合
     */
    @Test
    public void testConversion() throws JSONException {
        StringBuilder builder = new StringBuilder();
        builder.append(MANAGER_URI);
        builder.append("/jsonTest");
        builder.append("?");
        builder.append(DConnectMessage.EXTRA_SERVICE_ID + "=" + getServiceId());
        builder.append("&");
        builder.append(DConnectMessage.EXTRA_ACCESS_TOKEN + "=" + getAccessToken());

        DConnectResponseMessage response = mDConnectSDK.get(builder.toString());
        assertThat(response, is(notNullValue()));

        DConnectMessage root = response.getMessage("extra");
        Assert.assertNotNull("root is null.", root);
        Assert.assertFalse(root.containsKey(IntentDConnectMessage.EXTRA_REQUEST_CODE));
        Assert.assertEquals("http://localhost:8080", root.getString("uri"));
        Assert.assertEquals(0, root.getInt("byte"));
        Assert.assertEquals('0', root.getInt("char"));
        Assert.assertEquals(0, root.getInt("int"));
        Assert.assertEquals(0L, root.getLong("long"));
        Assert.assertEquals(0.0, root.getDouble("float"), TEST_FLOATING_VALUE);
        Assert.assertEquals(0.0, root.getDouble("double"), TEST_FLOATING_VALUE);
        Assert.assertEquals(false, root.getBoolean("boolean"));
        Assert.assertEquals(0, root.getInt(Byte.class.getName()));
        Assert.assertEquals('0', root.getInt(Character.class.getName()));
        Assert.assertEquals(0, root.getInt(Integer.class.getName()));
        Assert.assertEquals(0L, root.getLong(Long.class.getName()));
        Assert.assertEquals(0.0, root.getDouble(Float.class.getName()), TEST_FLOATING_VALUE);
        Assert.assertEquals(0.0, root.getDouble(Double.class.getName()), TEST_FLOATING_VALUE);
        Assert.assertEquals(false, root.getBoolean(Boolean.class.getName()));
        Assert.assertEquals(String.class.getName(), root.getString(String.class.getName()));
        Assert.assertEquals(1, root.getList(int[].class.getName()).size());
        Assert.assertEquals(0, root.getList(int[].class.getName()).get(0));
        Assert.assertEquals(1, root.getList(long[].class.getName()).size());
        Assert.assertEquals(0, root.getList(long[].class.getName()).get(0));
        Assert.assertEquals(1, root.getList(float[].class.getName()).size());
        Assert.assertEquals(0.0f, ((Integer)root.getList(float[].class.getName()).get(0)).floatValue(), TEST_FLOATING_VALUE);
        Assert.assertEquals(1, root.getList(double[].class.getName()).size());
        Assert.assertEquals(0.0d, ((Integer)root.getList(double[].class.getName()).get(0)).doubleValue(), TEST_FLOATING_VALUE);
        Assert.assertEquals(1, root.getList(boolean[].class.getName()).size());
        Assert.assertEquals(false, root.getList(boolean[].class.getName()).get(0));
        Assert.assertEquals(1, root.getList(Integer[].class.getName()).size());
        Assert.assertEquals(0, root.getList(Integer[].class.getName()).get(0));
        Assert.assertEquals(1, root.getList(Long[].class.getName()).size());
        Assert.assertEquals(0, root.getList(Long[].class.getName()).get(0));
        Assert.assertEquals(1, root.getList(Float[].class.getName()).size());
        Assert.assertEquals(0.0f, ((Integer) root.getList(Float[].class.getName()).get(0)).floatValue(), TEST_FLOATING_VALUE);
        Assert.assertEquals(1, root.getList(Double[].class.getName()).size());
        Assert.assertEquals(0.0d, ((Integer)root.getList(Double[].class.getName()).get(0)).doubleValue(), TEST_FLOATING_VALUE);
        Assert.assertEquals(1, root.getList(Boolean[].class.getName()).size());
        Assert.assertEquals(false, root.getList(Boolean[].class.getName()).get(0));
        Assert.assertEquals(1, root.getList(String[].class.getName()).size());
        Assert.assertEquals("String", root.getList(String[].class.getName()).get(0));
        Assert.assertNotNull(root.getMessage(Bundle.class.getName()));
        Assert.assertEquals(1, root.getList(Bundle[].class.getName()).size());
        Assert.assertNotNull(root.getList(Bundle[].class.getName()).get(0));
        Assert.assertEquals(1, root.getList("ArrayList<Integer>").size());
        Assert.assertEquals(0, root.getList("ArrayList<Integer>").get(0));
    }

}
