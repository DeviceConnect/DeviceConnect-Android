package org.deviceconnect.message;

import java.util.List;
import java.util.Map;
import java.util.Set;

abstract class AbstractMessageParser {

    void parse(final DConnectMessage message) {
        if (message == null) {
            throw new NullPointerException("message is null");
        }

        startParse();
        parseObject(message);
        endParse();
    }

    /**
     * パース開始.
     */
    abstract void startParse();

    /**
     * キー.
     *
     * @param key キー
     */
    abstract void onKey(String key);

    /**
     * 値.
     *
     * @param value 値
     */
    abstract void onValue(Object value);

    /**
     * 配列開始.
     */
    abstract void startArray();

    /**
     * 配列終了.
     */
    abstract void endArray();

    /**
     * マップ開始.
     */
    abstract void startMap();

    /**
     * マップ終了.
     */
    abstract void endMap();

    /**
     * パース終了.
     */
    abstract void endParse();


    @SuppressWarnings("unchecked")
    private Set<String> getKeySet(final Object message) {

        Set<String> keySet = null;

        if (message instanceof Map) {
            keySet = ((Map<String, Object>) message).keySet();
        }

        return keySet;
    }

    @SuppressWarnings("unchecked")
    private Object getValue(final Object message, final String key) {
        return ((Map<String, Object>) message).get(key);
    }

    private boolean isArray(final Object value) {
        return (value instanceof List);
    }

    private boolean isMap(final Object value) {
        return (value instanceof Map);
    }

    private List<?> toArray(final Object value) {
        return (List<?>) value;
    }

    /**
     * オブジェクトをパースする.
     *
     * @param object オブジェクト
     */
    private void parseObject(final Object object) {
        if (isMap(object)) {
            startMap();
            for (String key : getKeySet(object)) {
                onKey(key);
                parseObject(getValue(object, key));
            }
            endMap();
        } else if (isArray(object)) {
            startArray();
            for (Object obj : toArray(object)) {
                parseObject(obj);
            }
            endArray();
        } else {
            onValue(object);
        }
    }
}
