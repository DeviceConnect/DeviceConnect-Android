package org.deviceconnect.android.profile.spec;


public class BooleanDataSpec extends DConnectDataSpec {

    private final String TRUE = "true";
    private final String FALSE = "false";

    BooleanDataSpec() {
        super(DataType.BOOLEAN);
    }

    @Override
    public boolean validate(final Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof String) {
            String strParam = (String) obj;
            return TRUE.equalsIgnoreCase(strParam) || FALSE.equalsIgnoreCase(strParam);
        } else if (obj instanceof Boolean) {
            return true;
        }
        return false;
    }

    public static class Builder {

        public BooleanDataSpec build() {
            return new BooleanDataSpec();
        }

    }
}
