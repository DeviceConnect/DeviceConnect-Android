package org.deviceconnect.android.manager.compat;


import android.content.Intent;

import org.deviceconnect.android.compat.MessageConverter;
import org.deviceconnect.android.manager.DevicePlugin;

import java.util.ArrayList;
import java.util.List;

public class RequestConverter implements MessageConverter {

    final List<PathConversion> mPathConversions;

    final DevicePlugin mPlugin;

    private RequestConverter(final DevicePlugin plugin, final List<PathConversion> conversions) {
        if (conversions == null) {
            throw new IllegalArgumentException("conversions is null.");
        }
        mPlugin = plugin;
        mPathConversions = conversions;
    }

    @Override
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
            allConversions.addAll(PathConversionTable.getConversions(profileName));
        }
        if (allConversions.size() == 0) {
            return null;
        }
        return new RequestConverter(plugin, allConversions);
    }

}
