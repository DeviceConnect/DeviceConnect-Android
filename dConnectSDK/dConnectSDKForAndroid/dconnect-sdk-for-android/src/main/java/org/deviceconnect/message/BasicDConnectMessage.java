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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 共通で使用するメッセージ.
 * @author NTT DOCOMO, INC.
 */
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
        convertJSONToMap(json, this);
    }

    /**
     * メッセージをIntentから生成する.
     * @param intent メッセージIntent
     */
    BasicDConnectMessage(final Intent intent) throws JSONException {
        convertBundleToMap(intent.getExtras(), this);
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
        return ((Number) value).intValue();
    }

    @Override
    public long getLong(String key) {
        if (!containsKey(key)) {
            return 0;
        }

        Object value = get(key);
        if (value == null || !(value instanceof Long)) {
            return 0;
        }
        return ((Number) value).longValue();
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
        if (value == null || !(value instanceof Number)) {
            return 0f;
        }
        return ((Number) value).floatValue();
    }

    @Override
    public double getDouble(String key) {
        if (!containsKey(key)) {
            return 0;
        }

        Object value = get(key);
        if (value == null || !(value instanceof Number)) {
            return 0f;
        }
        return ((Number) value).doubleValue();
    }

    @Override
    public List<Object> getList(final String key) {
        if (!containsKey(key)) {
            return null;
        }

        Object value = get(key);
        if (value == null || !(value instanceof List<?>)) {
            return null;
        }
        return castListObject((List<?>) value);
    }

    @Override
    public DConnectMessage getMessage(final String key) {
        if (!containsKey(key)) {
            return null;
        }

        Object value = get(key);
        if (value == null || !(value instanceof DConnectMessage)) {
            return null;
        }
        return (DConnectMessage) value;
    }

    @Override
    public String toString(final int indent) {
        final StringBuilder builder = new StringBuilder();
        AbstractMessageParser parser = new AbstractMessageParser() {
            private boolean mFirstKey;
            private int mParseInArray;
            private int mFirstArrayValue;
            private int mIndent;

            @Override
            public void startParse() {
            }

            @Override
            public void onKey(final String key) {
                if (!mFirstKey) {
                    builder.append(",");
                }
                appendIndentSpace();

                mFirstKey = false;
                builder.append("\"");
                builder.append(key);
                builder.append("\"");
                builder.append(":");
            }
            @Override
            public void onValue(final Object value) {
                if ((mParseInArray & (1 << mIndent)) != 0) {
                    if ((mFirstArrayValue & (1 << mIndent)) != 0) {
                        builder.append(",");
                    }
                    appendIndentSpace();
                }
                mFirstArrayValue |= (1 << mIndent);
                if (Integer.class.isInstance(value)
                        || Float.class.isInstance(value)
                        || Double.class.isInstance(value)
                        || Long.class.isInstance(value)
                        || Byte.class.isInstance(value)
                        || Short.class.isInstance(value)
                        || Boolean.class.isInstance(value)) {
                    builder.append(value);
                } else if (value == null) {
                    builder.append("null");
                } else {
                    builder.append("\"");
                    builder.append(value);
                    builder.append("\"");
                }
            }
            @Override
            public void startMap() {
                appendIndentSpace();
                builder.append("{");

                mFirstKey = true;
                mIndent++;
            }
            @Override
            public void endMap() {
                mFirstArrayValue &= ~(1 << mIndent);
                mIndent--;
                mFirstArrayValue |= (1 << mIndent);

                appendIndentSpace();
                builder.append("}");
            }
            @Override
            public void startArray() {
                mIndent++;
                if ((mFirstArrayValue & (1 << mIndent)) != 0) {
                    builder.append(",");
                }
                mParseInArray |= (1 << mIndent);
                builder.append("[");
            }
            @Override
            public void endArray() {
                mParseInArray &= ~(1 << mIndent);
                mFirstArrayValue &= ~(1 << mIndent);
                mIndent--;
                mFirstArrayValue |= (1 << mIndent);

                appendIndentSpace();
                builder.append("]");
            }
            @Override
            public void endParse() {
            }

            private void appendIndentSpace() {
                if (indent <= 0) {
                    return;
                }
                if (builder.length() > 0) {
                    appendBreakLine();
                }
                for (int i = 0; i < mIndent * indent; i++) {
                    builder.append(" ");
                }
            }
            private void appendBreakLine() {
                if (indent <= 0) {
                    return;
                }
                builder.append("\n");
            }
        };
        parser.parse(this);

        return builder.toString();
    }

    private static void convertBundleToMap(final Bundle bundle, final DConnectMessage message) throws JSONException {
        if (bundle == null || message == null) {
            return;
        }

        for (String key : bundle.keySet()) {
            Object object = bundle.get(key);
            if (object instanceof Bundle) {
                DConnectMessage m = new BasicDConnectMessage();
                convertBundleToMap((Bundle) object, m);
                message.put(key, m);
            } else if (object instanceof Integer[] || object instanceof Long[] || object instanceof Short[]
                    || object instanceof Byte[] || object instanceof Character[] || object instanceof Float[]
                    || object instanceof Double[] || object instanceof Boolean[] || object instanceof String[]) {
                Object[] bb = (Object[]) object;
                List array = new ArrayList();
                for (int i = 0; i < bb.length; i++) {
                    array.add(bb[i]);
                }
                message.put(key, array);
            } else if (object instanceof Integer) {
                message.put(key, ((Integer) object).intValue());
            } else if (object instanceof int[]) {
                int[] bb = (int[]) object;
                List array = new ArrayList();
                for (int i = 0; i < bb.length; i++) {
                    array.add(bb[i]);
                }
                message.put(key, array);
            } else if (object instanceof Short) {
                message.put(key, ((Short) object).shortValue());
            } else if (object instanceof short[]) {
                short[] bb = (short[]) object;
                List array = new ArrayList();
                for (int i = 0; i < bb.length; i++) {
                    array.add(bb[i]);
                }
                message.put(key, array);
            } else if (object instanceof Character) {
                message.put(key, ((Character) object).charValue());
            } else if (object instanceof char[]) {
                char[] bb = (char[]) object;
                List array = new ArrayList();
                for (int i = 0; i < bb.length; i++) {
                    array.add(bb[i]);
                }
                message.put(key, array);
            } else if (object instanceof Byte) {
                message.put(key, ((Byte) object).byteValue());
            } else if (object instanceof byte[]) {
                byte[] bb = (byte[]) object;
                List array = new ArrayList();
                for (int i = 0; i < bb.length; i++) {
                    array.add(bb[i]);
                }
                message.put(key, array);
            } else if (object instanceof Long) {
                message.put(key, ((Long) object).longValue());
            } else if (object instanceof long[]) {
                long[] bb = (long[]) object;
                List array = new ArrayList();
                for (int i = 0; i < bb.length; i++) {
                    array.add(bb[i]);
                }
                message.put(key, array);
            } else if (object instanceof Float) {
                message.put(key, ((Float) object).floatValue());
            } else if (object instanceof float[]) {
                float[] bb = (float[]) object;
                List array = new ArrayList();
                for (int i = 0; i < bb.length; i++) {
                    array.add(bb[i]);
                }
                message.put(key, array);
            } else if (object instanceof Double) {
                message.put(key, ((Double) object).doubleValue());
            } else if (object instanceof double[]) {
                double[] bb = (double[]) object;
                List array = new ArrayList();
                for (int i = 0; i < bb.length; i++) {
                    array.add(bb[i]);
                }
                message.put(key, array);
            } else if (object instanceof Boolean) {
                message.put(key, ((Boolean) object).booleanValue());
            } else if (object instanceof boolean[]) {
                boolean[] bb = (boolean[]) object;
                List array = new ArrayList();
                for (int i = 0; i < bb.length; i++) {
                    array.add(bb[i]);
                }
                message.put(key, array);
            } else if (object instanceof String) {
                message.put(key, (String) object);
            } else if (object instanceof Bundle) {
                DConnectMessage obj = new BasicDConnectMessage();
                convertBundleToMap((Bundle) object, obj);
                message.put(key, obj);
            } else if (object instanceof Bundle[]) {
                Bundle[] bb = (Bundle[]) object;
                List array = new ArrayList();
                for (int i = 0; i < bb.length; i++) {
                    DConnectMessage obj = new BasicDConnectMessage();
                    convertBundleToMap(bb[i], obj);
                    array.add(obj);
                }
                message.put(key, array);
            } else if (object instanceof Parcelable[]) {
                Parcelable[] bb = (Parcelable[]) object;
                List array = new ArrayList();
                for (int i = 0; i < bb.length; i++) {
                    DConnectMessage obj = new BasicDConnectMessage();
                    if (bb[i] instanceof Bundle) {
                        convertBundleToMap((Bundle) bb[i], obj);
                    }
                    array.add(obj);
                }
                message.put(key, array);
            } else if (object instanceof Object[]) {
                // プリミティブ型のラッパークラスの配列がObject[]として扱われる場合への対処
                Object[] bb = (Object[]) object;
                if (isPrimitiveWrapperArray(bb)) {
                    List array = new ArrayList();
                    for (int i = 0; i < bb.length; i++) {
                        array.add(bb[i]);
                    }
                    message.put(key, array);
                }
            } else if (object instanceof List<?>) {
                List<?> bb = (List<?>) object;
                List array = new ArrayList();
                for (int i = 0; i < bb.size(); i++) {
                    Object v = bb.get(i);
                    if (v instanceof Bundle) {
                        DConnectMessage obj = new BasicDConnectMessage();
                        convertBundleToMap((Bundle) v, obj);
                        array.add(obj);
                    } else if (v instanceof Parcelable) {
                        DConnectMessage obj = new BasicDConnectMessage();
                        convertBundleToMap((Bundle) v, obj);
                        array.add(obj);
                    } else {
                        array.add(bb.get(i));
                    }
                }
                message.put(key, array);
            }
        }
    }

    private static void convertJSONToMap(final JSONObject root, final DConnectMessage message) throws JSONException {
        if (root == null || message == null) {
            return;
        }

        Iterator it = root.keys();
        while (it.hasNext()) {
            String key = (String) it.next();
            Object object = root.get(key);
            if (object instanceof JSONObject) {
                DConnectMessage m = new BasicDConnectMessage();
                convertJSONToMap((JSONObject) object, m);
                message.put(key, m);
            } else if (object instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) object;

                int length = jsonArray.length();
                List<Object> array = new ArrayList<>(length);
                for (int i = 0; i < length; i++) {
                    Object obj = jsonArray.get(i);
                    if (obj instanceof JSONObject) {
                        DConnectMessage m = new BasicDConnectMessage();
                        convertJSONToMap((JSONObject) obj, m);
                        array.add(m);
                    } else {
                        array.add(obj);
                    }
                }

                message.put(key, array);
            } else {
                message.put(key, object);
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
