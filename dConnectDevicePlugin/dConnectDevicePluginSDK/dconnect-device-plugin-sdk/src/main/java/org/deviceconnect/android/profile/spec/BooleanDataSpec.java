package org.deviceconnect.android.profile.spec;


public class BooleanDataSpec extends DConnectDataSpec {

    BooleanDataSpec() {
        super(DataType.BOOLEAN);
    }

    public static class Builder {

        public BooleanDataSpec build() {
            return new BooleanDataSpec();
        }

    }
}
