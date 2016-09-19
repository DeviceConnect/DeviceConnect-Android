package org.deviceconnect.android.deviceplugin.awsiot.core;

import android.content.Context;

import com.amazonaws.regions.Regions;

import org.deviceconnect.android.deviceplugin.awsiot.util.PreferenceUtil;

public class AWSIotPrefUtil extends PreferenceUtil {
    public static final String KEY_ACCESS_KEY = "awsAccessKey";
    public static final String KEY_SECRET_KEY = "awsSecretKey";
    public static final String KEY_REGIONS = "awsRegions";

    public static final String KEY_AWS_LOGIN_FLAG = "awsLoginFlag";

    public static final String KEY_MANAGER_NAME = "awsManagerName";
    public static final String KEY_MANAGER_UUID = "awsManagerUuid";
    public static final String KEY_MANAGER_REGISTER = "awsManagerRegister";
    public static final String KEY_SYNC_TIME = "awsSyncTime";
    public static final String KEY_SYNC_TIME_SET = "awsSyncTimeSet";
    public static final long DEFAULT_SYNC_TIME = 10;

    public static final String KEY_AUTH_ACCESS_TOKEN = "awsAuthAccessToken";
    public static final String KEY_AUTH_CLIENT_ID = "awsAuthClientId";

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

    public void setManagerName(final String name) {
        putValue(KEY_MANAGER_NAME, name);
    }

    public String getManagerName() {
        return getString(KEY_MANAGER_NAME);
    }

    public void setManagerUuid(final String uuid) {
        putValue(KEY_MANAGER_UUID, uuid);
    }

    public String getManagerUuid() {
        return getString(KEY_MANAGER_UUID);
    }

    public void setManagerRegister(final boolean regist) {
        putValue(KEY_MANAGER_REGISTER, regist);
    }

    public boolean getManagerRegister()
    {
        return getBoolean(KEY_MANAGER_REGISTER);
    }

    public long getSyncTime() {
        boolean setFlag = getBoolean(KEY_SYNC_TIME_SET);
        if (!setFlag) {
            setSyncTime(DEFAULT_SYNC_TIME);
            putValue(KEY_SYNC_TIME_SET, true);
        }
        return getLong(KEY_SYNC_TIME);
    }

    public void setSyncTime(final long syncTime) {
        putValue(KEY_SYNC_TIME, syncTime);
        putValue(KEY_SYNC_TIME_SET, true);
    }

    public boolean isAWSLoginFlag() {
        return getBoolean(KEY_AWS_LOGIN_FLAG);
    }

    public void setAWSLoginFlag(final boolean flag) {
        putValue(KEY_AWS_LOGIN_FLAG, flag);
    }

    public String getAuthAccessToken() {
        return getString(KEY_AUTH_ACCESS_TOKEN);
    }

    public void setAuthAccessToken(final String accessToken) {
        putValue(KEY_AUTH_ACCESS_TOKEN, accessToken);
    }

    public String getAuthClientId() {
        return getString(KEY_AUTH_CLIENT_ID);
    }

    public void setAuthClientId(final String clientId) {
        putValue(KEY_AUTH_CLIENT_ID, clientId);
    }
}
