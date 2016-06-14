package org.deviceconnect.android.manager.compat;


import android.content.Intent;

import org.deviceconnect.android.manager.DevicePlugin;

import java.util.ArrayList;
import java.util.List;

public class RequestConverter {

    final List<PathConversion> mPathConversions;

    private RequestConverter(final List<PathConversion> conversions) {
        if (conversions == null) {
            throw new IllegalArgumentException("conversions is null.");
        }
        mPathConversions = conversions;
    }

    public boolean convert(final Intent request) {
        for (PathConversion conversion : mPathConversions) {
            if (conversion.canConvert(request)) {
                conversion.convert(request);
                return true;
            }
        }
        return false;
    }

    public static RequestConverter create(final DevicePlugin plugin) {
        List<String> profileNames = plugin.getSupportProfiles();
        List<PathConversion> allConversions = new ArrayList<PathConversion>();
        for (String profileName : profileNames) {
            allConversions.addAll(PathConversionTable.INSTANCE.getConversions(profileName));
        }
        if (allConversions.size() == 0) {
            return null;
        }
        return new RequestConverter(allConversions);
    }

}
