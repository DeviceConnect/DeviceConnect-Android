package org.deviceconnect.android.profile.spec;


public abstract class DConnectRequestParamSpec {

    String mName;

    Type mType;

    boolean mIsMandatory;

    protected DConnectRequestParamSpec() {

    }

    public String getName() {
        return mName;
    }

    public Type getType() {
        return mType;
    }

    public boolean isMandatory() {
        return mIsMandatory;
    }

    public static DConnectRequestParamSpec fromType(final Type type) {
        switch (type) {
            case BOOLEAN:
                return new BooleanRequestParamSpec();
            case STRING:
                return new StringRequestParamSpec();
            case INTEGER:
                return new IntegerRequestParamSpec();
            case NUMBER:
                return new NumberRequestParamSpec();
            default:
                throw new IllegalArgumentException();
        }
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

}
