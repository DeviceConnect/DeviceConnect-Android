package org.deviceconnect.android.deviceplugin.theta.profile.param;


import android.content.Intent;
import android.os.Bundle;

public abstract class ParamDefinition {

    protected final String mName;

    protected final boolean mIsOptional;

    protected ParamDefinition(final String name, final boolean isOptional) {
        mName = name;
        mIsOptional = isOptional;
    }

    public abstract boolean validate(final Bundle extras, final Intent response);

}
