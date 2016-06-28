package org.deviceconnect.android.manager.compat;


import android.content.Intent;

import org.deviceconnect.android.compat.MessageConverter;
import org.deviceconnect.android.manager.DevicePlugin;
import org.deviceconnect.android.manager.util.VersionName;

import java.util.ArrayList;
import java.util.List;

public class DeliveryRequestConverter implements MessageConverter {

    private static final VersionName OLD_SDK = VersionName.parse("1.0.0");

    final List<PathConversion> mPathConversions;

    final DevicePlugin mPlugin;

    private DeliveryRequestConverter(final DevicePlugin plugin, final List<PathConversion> conversions) {
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

    public static DeliveryRequestConverter create(final DevicePlugin plugin) {
        List<String> profileNames = plugin.getSupportProfiles();
        List<PathConversion> allConversions = new ArrayList<PathConversion>();
        for (String profileName : profileNames) {
            allConversions.addAll(PathConversionTable.getConversions(profileName));
        }
        if (OLD_SDK.equals(plugin.getPluginSdkVersionName())) {
            allConversions.add(PathConversionTable.BATTERY_CHARGING_TIME);
            allConversions.add(PathConversionTable.BATTERY_DISCHARGING_TIME);
        }
        if (allConversions.size() == 0) {
            return null;
        }
        return new DeliveryRequestConverter(plugin, allConversions);
    }

}
