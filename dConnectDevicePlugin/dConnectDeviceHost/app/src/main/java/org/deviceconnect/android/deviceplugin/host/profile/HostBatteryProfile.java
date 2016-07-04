/*
 HostBatteryProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.host.profile;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.host.HostDeviceService;
import org.deviceconnect.android.deviceplugin.host.manager.HostBatteryManager;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.BatteryProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

/**
 * Battery Profile.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HostBatteryProfile extends BatteryProfile {
    /** エラーコード. */
    private static final int ERROR_CODE = 100;

    private final DConnectApi mBatteryLevelApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_LEVEL;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            int mLevel = ((HostDeviceService) getContext()).getBatteryLevel();
            int mScale = ((HostDeviceService) getContext()).getBatteryScale();
            if (mScale <= 0) {
                MessageUtils.setUnknownError(response, "Scale of battery level is unknown.");
            } else if (mLevel < 0) {
                MessageUtils.setUnknownError(response, "Battery level is unknown.");
            } else {
                setResult(response, IntentDConnectMessage.RESULT_OK);
                setLevel(response, mLevel / (float) mScale);
            }
            return true;
        }
    };

    private final DConnectApi mBatteryChargingApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_CHARGING;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            int mStatus = ((HostDeviceService) getContext()).getBatteryStatus();
            setResult(response, IntentDConnectMessage.RESULT_OK);
            setCharging(response, getBatteryChargingStatus(mStatus));
            return true;
        }
    };

    private final DConnectApi mBatteryAllApi = new GetApi() {
        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            int mLevel = ((HostDeviceService) getContext()).getBatteryLevel();
            int mScale = ((HostDeviceService) getContext()).getBatteryScale();
            if (mScale <= 0) {
                MessageUtils.setUnknownError(response, "Scale of battery level is unknown.");
            } else if (mLevel < 0) {
                MessageUtils.setUnknownError(response, "Battery level is unknown.");
            } else {
                setLevel(response, mLevel / (float) mScale);
                int mStatus = ((HostDeviceService) getContext()).getBatteryStatus();
                setCharging(response, getBatteryChargingStatus(mStatus));

                setResult(response, IntentDConnectMessage.RESULT_OK);
            }
            return true;
        }
    };

    private final DConnectApi mPutOnChargingChangeApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_CHARGING_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String sessionKey = getSessionKey(request);
            String serviceId = getServiceID(request);
            if (sessionKey == null) {
                MessageUtils.setInvalidRequestParameterError(response);
            } else {
                // Add event
                EventError error = EventManager.INSTANCE.addEvent(request);
                if (error == EventError.NONE) {
                    ((HostDeviceService) getContext()).setServiceId(serviceId);
                    ((HostDeviceService) getContext()).registerBatteryConnectBroadcastReceiver();
                    setResult(response, DConnectMessage.RESULT_OK);
                } else {
                    setResult(response, DConnectMessage.RESULT_ERROR);
                }
            }
            return true;
        }
    };

    private final DConnectApi mDeleteOnChargingChangeApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_CHARGING_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String sessionKey = getSessionKey(request);
            if (sessionKey == null) {
                MessageUtils.setInvalidRequestParameterError(response);
            } else {
                ((HostDeviceService) getContext()).unregisterBatteryConnectBroadcastReceiver();
                // イベントの解除
                EventError error = EventManager.INSTANCE.removeEvent(request);
                if (error == EventError.NONE) {
                    setResult(response, DConnectMessage.RESULT_OK);
                } else {
                    MessageUtils.setError(response, ERROR_CODE, "Can not unregister event.");
                }
            }
            return true;
        }
    };

    private final DConnectApi mPutOnBatteryChangeApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_BATTERY_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String sessionKey = getSessionKey(request);
            String serviceId = getServiceID(request);
            if (sessionKey == null) {
                MessageUtils.setInvalidRequestParameterError(response);
            } else {
                // Add event
                EventError error = EventManager.INSTANCE.addEvent(request);
                if (error == EventError.NONE) {
                    ((HostDeviceService) getContext()).setServiceId(serviceId);
                    ((HostDeviceService) getContext()).registerBatteryChargeBroadcastReceiver();
                    setResult(response, DConnectMessage.RESULT_OK);
                } else {
                    setResult(response, DConnectMessage.RESULT_ERROR);
                }
            }
            return true;
        }
    };

    private final DConnectApi mDeleteOnBatteryChangeApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_BATTERY_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String sessionKey = getSessionKey(request);
            if (sessionKey == null) {
                MessageUtils.setInvalidRequestParameterError(response);
            } else {
                // イベントの解除
                ((HostDeviceService) getContext()).unregisterBatteryChargeBroadcastReceiver();
                EventError error = EventManager.INSTANCE.removeEvent(request);
                if (error == EventError.NONE) {
                    setResult(response, DConnectMessage.RESULT_OK);
                } else {
                    MessageUtils.setError(response, ERROR_CODE, "Can not unregister event.");
                }
            }
            return true;
        }
    };

    public HostBatteryProfile() {
        addApi(mBatteryLevelApi);
        addApi(mBatteryChargingApi);
        addApi(mBatteryAllApi);
        addApi(mPutOnChargingChangeApi);
        addApi(mDeleteOnChargingChangeApi);
        addApi(mPutOnBatteryChangeApi);
        addApi(mDeleteOnBatteryChangeApi);
    }

    /**
     * Get status of charging.
     * 
     * @param mStatus BatteryStatus
     * @return true:charging false:not charging
     */
    private boolean getBatteryChargingStatus(final int mStatus) {
        switch (mStatus) {
        case HostBatteryManager.BATTERY_STATUS_CHARGING:
        case HostBatteryManager.BATTERY_STATUS_FULL:
            return true;
        case HostBatteryManager.BATTERY_STATUS_UNKNOWN:
        case HostBatteryManager.BATTERY_STATUS_DISCHARGING:
        case HostBatteryManager.BATTERY_STATUS_NOT_CHARGING:
        default:
            return false;
        }
    }
}
