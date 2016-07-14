package org.deviceconnect.android.profile.spec;


import org.deviceconnect.message.intent.message.IntentDConnectMessage;

public interface DConnectApiSpecConstants {

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

}
