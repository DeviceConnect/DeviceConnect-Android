package org.deviceconnect.android.deviceplugin.theta.profile.param;


import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.message.MessageUtils;

import java.util.ArrayList;
import java.util.List;

public class ParamDefinitionSet {

    private final List<ParamDefinition> mDefinitions = new ArrayList<ParamDefinition>();

    public void add(final ParamDefinition definition) {
        mDefinitions.add(definition);
    }

    public boolean validateRequest(final Intent request, final Intent response) {
        Bundle extras = request.getExtras();
        if (extras == null) {
            MessageUtils.setUnknownError(response, "request has no parameter.");
            return false;
        }
        for (ParamDefinition definition : mDefinitions) {
            if (!definition.validate(extras, response)) {
                return false;
            }
        }
        return true;
    }

}
