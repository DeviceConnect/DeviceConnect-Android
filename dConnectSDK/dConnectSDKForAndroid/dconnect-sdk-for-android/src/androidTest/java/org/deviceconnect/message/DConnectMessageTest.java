package org.deviceconnect.message;

import android.support.test.runner.AndroidJUnit4;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class DConnectMessageTest {

    @Test
    public void BasicDConnectMessage() {
        try {
            JSONObject test2 = new JSONObject();
            test2.put("key2", "value2");

            JSONObject test = new JSONObject();
            test.put("key1", "value1");
            test.put("test2", test2);

            JSONArray array = new JSONArray();

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("key", "value");
            jsonObject.put("test", test);
            jsonObject.put("array", array);

            DConnectMessage message = new BasicDConnectMessage(jsonObject.toString());

            assertThat(message, is(notNullValue()));
            assertThat(message.getString("key"), is("value"));

            DConnectMessage t = message.getMessage("test");
            assertThat(t, is(notNullValue()));
            assertThat(t.getString("key1"), is("value1"));
        } catch (JSONException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void BasicDConnectMessage_array1() {
        try {
            final int size = 5;
            JSONArray array = new JSONArray();
            for (int i = 0; i < size; i++) {
                array.put(i);
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("array", array);

            DConnectMessage message = new BasicDConnectMessage(jsonObject.toString());
            assertThat(message, is(notNullValue()));

            List list = message.getList("array");
            assertThat(list, is(notNullValue()));
            assertThat(list.size(), is(size));
            for (int i = 0; i < size; i++) {
                assertThat((Integer) list.get(i), is(i));
            }
        } catch (JSONException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void BasicDConnectMessage_array2() {
        try {
            JSONObject test = new JSONObject();
            test.put("key1", "value1");

            JSONArray array = new JSONArray();
            array.put(test);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("array", array);

            DConnectMessage message = new BasicDConnectMessage(jsonObject.toString());
            assertThat(message, is(notNullValue()));

            List list = message.getList("array");
            assertThat(list, is(notNullValue()));
            assertThat(list.size(), is(1));

            DConnectMessage t = (DConnectMessage) list.get(0);
            assertThat(t, is(notNullValue()));
            assertThat(t.getString("key1"), is("value1"));
        } catch (JSONException e) {
            fail(e.getMessage());
        }
    }
}
