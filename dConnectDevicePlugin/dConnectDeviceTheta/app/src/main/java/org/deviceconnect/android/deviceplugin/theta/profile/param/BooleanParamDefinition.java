package org.deviceconnect.android.deviceplugin.theta.profile.param;


import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.message.MessageUtils;

public class BooleanParamDefinition extends ParamDefinition {

    private static final String TRUE = "true";
    private static final String FALSE = "false";

    public BooleanParamDefinition(final String name, final boolean isOptional) {
        super(name, isOptional);
    }

    public BooleanParamDefinition(final String name) {
        this(name, true);
    }

    @Override
    public boolean validate(final Bundle extras, final Intent response) {
        Object value = extras.get(mName);
        if (value == null) {
            if (mIsOptional) {
                return true;
            } else {
                MessageUtils.setInvalidRequestParameterError(response, mName + " is not specified.");
                return false;
            }
        }
        if (value instanceof Boolean) {
            return true;
        } else if (value instanceof String) {
            String stringValue = (String) value;
            if (!TRUE.equals(stringValue) && !FALSE.equals(stringValue)) {
                MessageUtils.setInvalidRequestParameterError(response, "Format of " + mName + " is invalid.");
                return false;
            }
            try {
                Boolean.parseBoolean(stringValue);
                return true;
            } catch (NumberFormatException e) {
                // Nothing to do.
            }
        }
        MessageUtils.setInvalidRequestParameterError(response, "Format of " + mName + " is invalid.");
        return false;
    }
}
