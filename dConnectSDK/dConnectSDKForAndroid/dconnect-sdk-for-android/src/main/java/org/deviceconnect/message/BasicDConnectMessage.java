/*
 BasicDConnectMessage.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.message;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class BasicDConnectMessage extends HashMap<String, Object> implements DConnectMessage {

    /**
     * シリアルバージョン.
     */
    private static final long serialVersionUID = 1L;

    /**
     * 空っぽのメッセージを生成する.
     */
    BasicDConnectMessage() {
    }

    /**
     * メッセージをMapから生成する.
     *
     * @param map メッセージMap
     */
    BasicDConnectMessage(final Map<String, Object> map) {
        super(map);
    }

    /**
     * メッセージをJSONから生成する.
     *
     * @param json メッセージJSON
     * @throws JSONException JSONエラー.
     */
    BasicDConnectMessage(final String json) throws JSONException {
        this(new JSONObject(json));
    }

    /**
     * メッセージをJSONから生成する.
     *
     * @param json メッセージJSON
     * @throws JSONException JSONエラー.
     */
    @SuppressWarnings("unchecked")
    BasicDConnectMessage(final JSONObject json) throws JSONException {
        this((Map<String, Object>) parseJSONObject(json));
    }

    /**
     * メッセージをIntentから生成する.
     * @param intent メッセージIntent
     */
    BasicDConnectMessage(final Intent intent) throws JSONException {
        this((JSONObject) parseIntent(intent));
    }

    /**
     * Stringを取得する.
     * @param key キー
     * @return 値
     */
    public String getString(final String key) {
        if (!containsKey(key)) {
            return null;
        }

        Object value = get(key);
        if (value != null && !(value instanceof String)) {
            return null;
        }
        return (String) value;
    }

    /**
     * intを取得する.
     * @param key キー
     * @return 値
     */
    public int getInt(final String key) {
        if (!containsKey(key)) {
            return 0;
        }

        Object value = get(key);
        if (value == null || !(value instanceof Integer)) {
            return 0;
        }
        return (Integer) value;
    }

    /**
     * booleanを取得する.
     * @param key キー
     * @return 値
     */
    public boolean getBoolean(final String key) {
        if (!containsKey(key)) {
            return false;
        }

        Object value = get(key);
        if (value == null || !(value instanceof Boolean)) {
            return false;
        }
        return (Boolean) value;
    }

    /**
     * doubleを取得する.
     * @param key キー
     * @return 値
     */
    public float getFloat(final String key) {
        if (!containsKey(key)) {
            return 0;
        }

        Object value = get(key);
        if (value == null || !(value instanceof Float)) {
            return 0f;
        }
        return (Float) value;
    }

    @Override
    public List<Object> getList(final String key) {
        if (!containsKey(key)) {
            return null;
        }

        Object value = get(key);
        if (value == null ||  !(value instanceof List<?>)) {
            return null;
        }
        return castListObject((List<?>) value);
    }

    @Override
    public String toString(int indent) {
        return "";
    }

    /**
     * JSONObjectをパースしてオブジェクトとして返却する.
     * @param json パース対象JSONObject
     * @return オブジェクト
     * @throws JSONException JSONエラーが発生した場合
     */
    private static Object parseJSONObject(final Object json) throws JSONException {

        Object object;

        if (json == JSONObject.NULL) {
            return null;
        } else if (json instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) json;

            Map<String, Object> map = new HashMap<>();
            JSONArray names = jsonObject.names();
            if (names != null) {
                int length = names.length();
                for (int i = 0; i < length; i++) {
                    String name = names.getString(i);
                    map.put(name, parseJSONObject(jsonObject.get(name)));
                }
            }

            object = map;
        } else if (json instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) json;

            int length = jsonArray.length();
            List<Object> array = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                array.add(parseJSONObject(jsonArray.get(i)));
            }

            object = array;
        } else {
            object = json;
        }

        return object;
    }

    private static JSONObject parseIntent(final Intent intent) {
        JSONObject obj = new JSONObject();
        try {
            convertBundleToJSON(obj, intent.getExtras());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static void convertBundleToJSON(
            final JSONObject root, final Bundle b) throws JSONException {

        if (root == null || b == null) {
            return;
        }

        for (String key : b.keySet()) {
            Object value = b.get(key);
            if (key.equals(IntentDConnectMessage.EXTRA_REQUEST_CODE)) {
                // request_codeはRESTfulにはいらないので削除しておく
                continue;
            } else if (value instanceof Integer[] || value instanceof Long[] || value instanceof Short[]
                    || value instanceof Byte[] || value instanceof Character[] || value instanceof Float[]
                    || value instanceof Double[] || value instanceof Boolean[] || value instanceof String[]) {
                Object[] bb = (Object[]) value;
                JSONArray array = new JSONArray();
                for (int i = 0; i < bb.length; i++) {
                    array.put(bb[i]);
                }
                root.put(key, array);
            } else if (value instanceof Integer) {
                root.put(key, ((Integer) value).intValue());
            } else if (value instanceof int[]) {
                int[] bb = (int[]) value;
                JSONArray array = new JSONArray();
                for (int i = 0; i < bb.length; i++) {
                    array.put(bb[i]);
                }
                root.put(key, array);
            } else if (value instanceof Short) {
                root.put(key, ((Short) value).shortValue());
            } else if (value instanceof short[]) {
                short[] bb = (short[]) value;
                JSONArray array = new JSONArray();
                for (int i = 0; i < bb.length; i++) {
                    array.put(bb[i]);
                }
                root.put(key, array);
            } else if (value instanceof Character) {
                root.put(key, ((Character) value).charValue());
            } else if (value instanceof char[]) {
                char[] bb = (char[]) value;
                JSONArray array = new JSONArray();
                for (int i = 0; i < bb.length; i++) {
                    array.put(bb[i]);
                }
                root.put(key, array);
            } else if (value instanceof Byte) {
                root.put(key, ((Byte) value).byteValue());
            } else if (value instanceof byte[]) {
                byte[] bb = (byte[]) value;
                JSONArray array = new JSONArray();
                for (int i = 0; i < bb.length; i++) {
                    array.put(bb[i]);
                }
                root.put(key, array);
            } else if (value instanceof Long) {
                root.put(key, ((Long) value).longValue());
            } else if (value instanceof long[]) {
                long[] bb = (long[]) value;
                JSONArray array = new JSONArray();
                for (int i = 0; i < bb.length; i++) {
                    array.put(bb[i]);
                }
                root.put(key, array);
            } else if (value instanceof Float) {
                root.put(key, ((Float) value).floatValue());
            } else if (value instanceof float[]) {
                float[] bb = (float[]) value;
                JSONArray array = new JSONArray();
                for (int i = 0; i < bb.length; i++) {
                    array.put(bb[i]);
                }
                root.put(key, array);
            } else if (value instanceof Double) {
                root.put(key, ((Double) value).doubleValue());
            } else if (value instanceof double[]) {
                double[] bb = (double[]) value;
                JSONArray array = new JSONArray();
                for (int i = 0; i < bb.length; i++) {
                    array.put(bb[i]);
                }
                root.put(key, array);
            } else if (value instanceof Boolean) {
                root.put(key, ((Boolean) value).booleanValue());
            } else if (value instanceof boolean[]) {
                boolean[] bb = (boolean[]) value;
                JSONArray array = new JSONArray();
                for (int i = 0; i < bb.length; i++) {
                    array.put(bb[i]);
                }
                root.put(key, array);
            } else if (value instanceof String) {
                root.put(key, (String) value);
            } else if (value instanceof Bundle) {
                JSONObject obj = new JSONObject();
                convertBundleToJSON(obj, (Bundle) value);
                root.put(key, obj);
            } else if (value instanceof Bundle[]) {
                Bundle[] bb = (Bundle[]) value;
                JSONArray array = new JSONArray();
                for (int i = 0; i < bb.length; i++) {
                    JSONObject obj = new JSONObject();
                    convertBundleToJSON(obj, bb[i]);
                    array.put(obj);
                }
                root.put(key, array);
            } else if (value instanceof Parcelable[]) {
                Parcelable[] bb = (Parcelable[]) value;
                JSONArray array = new JSONArray();
                for (int i = 0; i < bb.length; i++) {
                    JSONObject obj = new JSONObject();
                    if (bb[i] instanceof Bundle) {
                        convertBundleToJSON(obj, (Bundle) bb[i]);
                    }
                    array.put(obj);
                }
                root.put(key, array);
            } else if (value instanceof Object[]) {
                // プリミティブ型のラッパークラスの配列がObject[]として扱われる場合への対処
                Object[] bb = (Object[]) value;
                if (isPrimitiveWrapperArray(bb)) {
                    JSONArray array = new JSONArray();
                    for (int i = 0; i < bb.length; i++) {
                        array.put(bb[i]);
                    }
                    root.put(key, array);
                }
            } else if (value instanceof List<?>) {
                List<?> bb = (List<?>) value;
                JSONArray array = new JSONArray();
                for (int i = 0; i < bb.size(); i++) {
                    Object v = bb.get(i);
                    if (v instanceof Bundle) {
                        JSONObject obj = new JSONObject();
                        convertBundleToJSON(obj, (Bundle) bb.get(i));
                        array.put(obj);
                    } else if (v instanceof Parcelable) {
                        JSONObject obj = new JSONObject();
                        convertBundleToJSON(obj, (Bundle) bb.get(i));
                        array.put(obj);
                    } else {
                        array.put(bb.get(i));
                    }
                }
                root.put(key, array);
            }
        }
    }

    /**
     * 指定したObject[]がプリミティブ型のラッパークラスの配列であるかどうかをチェックする.
     * <p>
     * なお、配列のすべての要素の型が同一でない場合、falseを返す.
     * 例えば、以下のような場合.
     * </p>
     * <pre>
     * {new Integer(0), new Double(0.0d)} // falseを返す
     * </pre>
     * @param array チェックするオブジェクト配列
     * @return プリミティブ型のラッパークラスの配列である場合はtrue、そうでない場合はfalse
     */
    private static boolean isPrimitiveWrapperArray(final Object[] array) {
        String classNameCache = null;
        for (int i = 0; i < array.length; i++) {
            Object obj = array[i];
            if (obj != null) {
                if (isPrimitiveWrapper(obj)) {
                    String className = obj.getClass().getName();
                    if (classNameCache != null) {
                        if (!classNameCache.equals(className)) {
                            return false;
                        }
                    } else {
                        classNameCache = className;
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 指定したObjectがプリミティブ型のラッパークラスであるかどうかをチェックする.
     *
     * @param obj チェックするオブジェクト
     * @return プリミティブ型のラッパークラスである場合はtrue、そうでない場合はfalse
     */
    private static boolean isPrimitiveWrapper(final Object obj) {
        return obj instanceof Byte || obj instanceof Short || obj instanceof Integer
                || obj instanceof Long || obj instanceof Float || obj instanceof Double
                || obj instanceof Character || obj instanceof Boolean;
    }

    /**
     * List<Object>へキャストする.
     * @param list リスト
     * @return キャストされたリスト
     */
    @SuppressWarnings("unchecked")
    private List<Object> castListObject(final List<?> list) {
        return (List<Object>) list;
    }
}
