package org.deviceconnect.android.profile.spec.models;

/**
 * パラメータのタイプ.
 */
public enum In {
    QUERY("query"),
    HEADER("header"),
    PATH("path"),
    FORM("formData"),
    BODY("body");

    private String mName;

    In(final String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public static In parse(final String value) {
        for (In in : values()) {
            if (in.mName.equalsIgnoreCase(value)) {
                return in;
            }
        }
        return null;
    }
}
