package org.deviceconnect.android.profile.spec;


import org.deviceconnect.message.intent.message.IntentDConnectMessage;

public interface DConnectSpecConstants {

    enum Type {

        ONESHOT("one-shot"),
        EVENT("event"),
        STREAMING("streaming");

        private String mName;

        Type(final String name) {
            mName = name;
        }

        public String getName() {
            return mName;
        }

        public static Type parse(final String value) {
            for (Type type : values()) {
                if (type.mName.equalsIgnoreCase(value)) {
                    return type;
                }
            }
            return null;
        }
    }

    enum Method {

        GET("GET"),
        PUT("PUT"),
        POST("POST"),
        DELETE("DELETE");

        private String mName;

        Method(final String name) {
            mName = name;
        }

        public String getName() {
            return mName;
        }

        public static Method parse(final String value) {
            for (Method method : values()) {
                if (method.mName.equalsIgnoreCase(value)) {
                    return method;
                }
            }
            return null;
        }

        public static Method fromAction(final String action) {
            if (IntentDConnectMessage.ACTION_GET.equals(action)) {
                return GET;
            } else if (IntentDConnectMessage.ACTION_PUT.equals(action)) {
                return PUT;
            } else if (IntentDConnectMessage.ACTION_POST.equals(action)) {
                return POST;
            } else if (IntentDConnectMessage.ACTION_DELETE.equals(action)) {
                return DELETE;
            }
            return null;
        }

        public boolean isValid(final String name) {
            return parse(name) != null;
        }
    }

    enum DataType {

        ARRAY("array"),
        BOOLEAN("boolean"),
        INTEGER("integer"),
        NUMBER("number"),
        STRING("string"),
        FILE("file");

        private final String mName;

        DataType(final String name) {
            mName = name;
        }

        public String getName() {
            return mName;
        }

        public static DataType fromName(final String name) {
            for (DataType type : DataType.values()) {
                if (type.mName.equalsIgnoreCase(name)) {
                    return type;
                }
            }
            return null;
        }
    }

    enum DataFormat {

        INT32("int32"),
        INT64("int64"),

        FLOAT("float"),
        DOUBLE("double"),

        TEXT("text"),
        BYTE("byte"),
        BINARY("binary"),
        DATE("date"),
        DATE_TIME("date-time"),
        PASSWORD("password"),
        RGB("rgb");

        private final String mName;

        DataFormat(final String name) {
            mName = name;
        }

        public String getName() {
            return mName;
        }

        public static DataFormat fromName(final String name) {
            for (DataFormat format : DataFormat.values()) {
                if (format.mName.equalsIgnoreCase(name)) {
                    return format;
                }
            }
            return null;
        }
    }
}
