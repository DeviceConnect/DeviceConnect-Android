package org.deviceconnect.android.profile.spec;


public class BooleanRequestParamSpec extends DConnectRequestParamSpec {

    public BooleanRequestParamSpec(final String name, final boolean isRequired) {
        super(name, Type.BOOLEAN, isRequired);
    }
}
