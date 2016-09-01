package org.deviceconnect.android.deviceplugin.awsiot.core;

import android.content.Context;

import com.amazonaws.regions.Regions;

import org.deviceconnect.android.deviceplugin.awsiot.util.PreferenceUtil;

public class AWSIotPrefUtil extends PreferenceUtil {
    public static final String KEY_ACCESS_KEY = "awsAccessKey";
    public static final String KEY_SECRET_KEY = "awsSecretKey";
    public static final String KEY_REGIONS = "awsRegions";

    public AWSIotPrefUtil(final Context context) {
        super(context);
    }

    public void setAccessKey(final String accessKey) {
        putValue(KEY_ACCESS_KEY, accessKey);
    }

    public String getAccessKey() {
        return getString(KEY_ACCESS_KEY);
    }

    public void setSecretKey(final String secretKey) {
        putValue(KEY_SECRET_KEY, secretKey);
    }

    public String getSecretKey() {
        return getString(KEY_SECRET_KEY);
    }

    public void setRegions(final Regions regions) {
        putValue(KEY_REGIONS, regions.getName());
    }

    public Regions getRegions() {
        String regions = getString(KEY_REGIONS);
        if (regions != null) {
            return Regions.fromName(regions);
        }
        return null;
    }
}
