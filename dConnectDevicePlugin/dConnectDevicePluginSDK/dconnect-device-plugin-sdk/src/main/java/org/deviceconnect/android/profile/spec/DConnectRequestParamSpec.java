package org.deviceconnect.android.profile.spec;


import org.json.JSONException;
import org.json.JSONObject;

public abstract class DConnectRequestParamSpec {

    String mName;

    Type mType;

    boolean mIsRequired;

    protected DConnectRequestParamSpec(final String name, final Type type, final boolean isRequired) {
        mName = name;
        mType = type;
        mIsRequired = isRequired;
    }

    public String getName() {
        return mName;
    }

    public Type getType() {
        return mType;
    }

    public boolean isRequired() {
        return mIsRequired;
    }

    void loadJson(final JSONObject json) {
    }

    public enum Type {

        STRING,
        INTEGER,
        NUMBER,
        BOOLEAN;

        public String getName() {
            return this.name().toLowerCase();
        }

        public static Type fromName(final String name) {
            for (Type type : Type.values()) {
                if (type.getName().equalsIgnoreCase(name)) {
                    return type;
                }
            }
            return null;
        }
    }

    static DConnectRequestParamSpec fromJson(final JSONObject json) throws JSONException {
        String type = json.getString("type");
        DConnectRequestParamSpec.Type paramType = DConnectRequestParamSpec.Type.fromName(type);
        if (paramType == null) {
            return null;
        }
        String name = json.getString("name");
        boolean required = json.getBoolean("required");
        DConnectRequestParamSpec spec;
        switch (paramType) {
            case BOOLEAN:
                spec = new BooleanRequestParamSpec(name, required);
                break;
            case STRING:
                spec = new StringRequestParamSpec(name, required);
                break;
            case INTEGER:
                spec = new IntegerRequestParamSpec(name, required);
                break;
            case NUMBER:
                spec = new NumberRequestParamSpec(name, required);
                break;
            default:
                throw new IllegalArgumentException();
        }
        spec.loadJson(json);
        return spec;
    }
}
