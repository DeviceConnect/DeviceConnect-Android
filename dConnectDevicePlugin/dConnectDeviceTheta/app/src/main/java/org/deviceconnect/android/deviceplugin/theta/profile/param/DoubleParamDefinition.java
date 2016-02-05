package org.deviceconnect.android.deviceplugin.theta.profile.param;


import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.message.MessageUtils;

public class DoubleParamDefinition extends ParamDefinition {

    private final Range mRange;

    public DoubleParamDefinition(final String name, final boolean isOptional,
                                 final Range range) {
        super(name, isOptional);
        mRange = range;
    }

    public DoubleParamDefinition(final String name, final Range range) {
        this(name, true, range);
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
        if (value instanceof Double) {
            if (validateRange((Double) value)) {
                return true;
            } else {
                MessageUtils.setInvalidRequestParameterError(response, mName + " is out of range.");
                return false;
            }
        } else if (value instanceof String) {
            try {
                double doubleValue = Double.parseDouble((String) value);
                if (validateRange(doubleValue)) {
                    return true;
                } else {
                    MessageUtils.setInvalidRequestParameterError(response, mName + " is out of range.");
                    return false;
                }
            } catch (NumberFormatException e) {
                // Nothing to do.
            }
        }
        MessageUtils.setInvalidRequestParameterError(response, "Format of " + mName + " is invalid.");
        return false;
    }

    private boolean validateRange(double value) {
        return mRange == null || mRange.validate(value);
    }

    public interface Range {
        boolean validate(double value);
    }
}
