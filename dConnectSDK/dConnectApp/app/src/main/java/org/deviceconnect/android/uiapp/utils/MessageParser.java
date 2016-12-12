package org.deviceconnect.android.uiapp.utils;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.view.View;

import org.deviceconnect.message.DConnectMessage;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class MessageParser {
    private final SpannableStringBuilder builder = new SpannableStringBuilder();

    private boolean mFirstKey;
    private int mParseInArray;
    private int mFirstArrayValue;
    private int mIndent;
    private boolean mUriFlag;

    private int indent;

    private OnClickLinkListener mClickLinkListener;

    public void setClickLinkListener(OnClickLinkListener clickLinkListener) {
        mClickLinkListener = clickLinkListener;
    }

    public SpannableStringBuilder parse(final DConnectMessage message, int indent) {
        if (message == null) {
            throw new NullPointerException("message is null");
        }

        this.indent = indent;
        startParse();
        parseObject(message);
        endParse();

        return builder;
    }

    private void startParse() {
    }

    private void onKey(final String key) {
        if (!mFirstKey) {
            builder.append(",");
        }
        appendIndentSpace();

        mUriFlag = key.equals("uri");

        mFirstKey = false;
        builder.append("\"");
        builder.append(key);
        builder.append("\"");
        builder.append(":");
    }

    private void onValue(final Object value) {
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
            builder.append(String.valueOf(value));
        } else if (value == null) {
            builder.append("null");
        } else if (String.class.isInstance(value)) {
            builder.append("\"");
            builder.append(String.valueOf(value));
            builder.append("\"");

            if (mUriFlag) {
                final String uri = (String) value;
                builder.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(final View widget) {
                        if (mClickLinkListener != null) {
                            mClickLinkListener.onClick(uri);
                        }
                    }
                }, builder.length() - uri.getBytes().length - 1, builder.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        } else {
            builder.append("\"");
            builder.append(String.valueOf(value));
            builder.append("\"");
        }

        mUriFlag = false;
    }

    private void startMap() {
        appendIndentSpace();
        builder.append("{");

        mFirstKey = true;
        mIndent++;
    }

    private void endMap() {
        mFirstArrayValue &= ~(1 << mIndent);
        mIndent--;
        mFirstArrayValue |= (1 << mIndent);

        appendIndentSpace();
        builder.append("}");
    }

    private void startArray() {
        mIndent++;
        if ((mFirstArrayValue & (1 << mIndent)) != 0) {
            builder.append(",");
        }
        mParseInArray |= (1 << mIndent);
        builder.append("[");
    }

    private void endArray() {
        mParseInArray &= ~(1 << mIndent);
        mFirstArrayValue &= ~(1 << mIndent);
        mIndent--;
        mFirstArrayValue |= (1 << mIndent);

        appendIndentSpace();
        builder.append("]");
    }

    private void endParse() {
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

    public interface OnClickLinkListener {
        void onClick(final String uri);
    }
}
