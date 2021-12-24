/*
 HostGeolocationProfile.java
 Copyright (c) 2021 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.profile;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.activity.GeolocationAlertDialogActivity;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.GeolocationProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.android.util.NotificationUtils;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.utils.RFC3339DateUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static com.google.android.gms.common.ConnectionResult.RESOLUTION_REQUIRED;

/**
 * Geolocation Profile.
 * @author NTT DOCOMO, INC.
 */
public class HostGeolocationProfile extends GeolocationProfile {
    interface GeolocationDiaglogCallback {
        void onSuccess();
    }

    // Fused Location Provider API
    private FusedLocationProviderClient mFusedLocationClient;

    // Location Setting APIs.
    private SettingsClient mSettingsClient;
    // Event用のCallback
    private LocationCallback mLocationEventCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull @NotNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Bundle position = createPositionObject(locationResult.getLastLocation());

            if (isEmptyEventList()) {
                stopGPS();
                return;
            }

            List<Event> events = EventManager.INSTANCE.getEventList(mServiceId,
                    GeolocationProfile.PROFILE_NAME, null,
                    GeolocationProfile.ATTRIBUTE_ON_WATCH_POSITION.toLowerCase());

            for (int i = 0; i < events.size(); i++) {
                Event event = events.get(i);
                Intent intent = EventManager.createEventMessage(event);
                intent.putExtra(GeolocationProfile.PARAM_POSITION, position);
                sendEvent(intent, event.getAccessToken());
            }
        }
    };
    // OneShot用Callback
    private Intent mOneShotResponse = null;
    private LocationCallback mOneShotLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull @NotNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (mOneShotResponse != null) {
                Bundle position = createPositionObject(locationResult.getLastLocation());

                DConnectProfile.setResult(mOneShotResponse, DConnectMessage.RESULT_OK);
                mOneShotResponse.putExtra(GeolocationProfile.PARAM_POSITION, position);
                sendResponse(mOneShotResponse);
                mOneShotResponse = null;
            }
            mFusedLocationClient.removeLocationUpdates(this);
        }
    };
    /** ServiceID. */
    private String mServiceId;

    /** 前回の位置情報の計測時間を保持する. */
    private long mLocationLastTime;

    /** 前回の位置情報を保持する. */
    private Bundle mLocationCache;

    /** Notification Id */
    private final int NOTIFICATION_ID = 3533;

    /**
     * Constructor.
     */
    public HostGeolocationProfile() {

        DConnectApi mGetOnGeolocationApi = new GetApi() {

            @Override
            public String getAttribute() {
                return ATTRIBUTE_CURRENT_POSITION;
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                PermissionUtility.requestPermissions(getContext(),
                        new Handler(Looper.getMainLooper()), new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                        new PermissionUtility.PermissionRequestCallback() {
                            @Override
                            public void onSuccess() {
                                long maximumAge = (long) getMaximumAge(request);
                                if (System.currentTimeMillis() - mLocationLastTime < maximumAge) {
                                    DConnectProfile.setResult(response, DConnectMessage.RESULT_OK);
                                    response.putExtra(GeolocationProfile.PARAM_POSITION, mLocationCache);
                                    sendResponse(response);
                                } else {
                                    getLocationManager(response, () -> getGPS(getHighAccuracy(request), response));

                                }
                            }

                            @Override
                            public void onFail(@NonNull String deniedPermission) {
                                MessageUtils.setIllegalDeviceStateError(response,
                                        "ACCESS_FINE_LOCATION permission not granted.");
                                sendResponse(response);
                            }
                        });

                return false;
            }
        };
        addApi(mGetOnGeolocationApi);

        DConnectApi mPutOnGeolocationApi = new PutApi() {

            @Override
            public String getAttribute() {
                return ATTRIBUTE_ON_WATCH_POSITION;
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                PermissionUtility.requestPermissions(getContext(),
                        new Handler(Looper.getMainLooper()), new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                        new PermissionUtility.PermissionRequestCallback() {
                            @Override
                            public void onSuccess() {
                                getLocationManager(response, () -> {
                                    String serviceId = getServiceID(request);
                                    // イベントの登録
                                    EventError error = EventManager.INSTANCE.addEvent(request);
                                    if (error == EventError.NONE) {
                                        // デフォルトは5000msec
                                        startGPS(getHighAccuracy(request), (int) getInterval(request));
                                        mServiceId = serviceId;
                                        DConnectProfile.setResult(response, DConnectMessage.RESULT_OK);
                                        response.putExtra(DConnectMessage.EXTRA_VALUE,
                                                "Register OnWatchPosition event");
                                    } else {
                                        MessageUtils.setUnknownError(response, "Can not register event.");
                                    }
                                    sendResponse(response);
                                });
                            }

                            @Override
                            public void onFail(@NonNull String deniedPermission) {
                                MessageUtils.setIllegalDeviceStateError(response,
                                        "ACCESS_FINE_LOCATION permission not granted.");
                                sendResponse(response);
                            }
                        });

                return false;
            }
        };
        addApi(mPutOnGeolocationApi);

        DConnectApi mDeleteOnGeolocationApi = new DeleteApi() {

            @Override
            public String getAttribute() {
                return ATTRIBUTE_ON_WATCH_POSITION;
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                PermissionUtility.requestPermissions(getContext(),
                        new Handler(Looper.getMainLooper()), new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                        new PermissionUtility.PermissionRequestCallback() {
                            @Override
                            public void onSuccess() {
                                getLocationManager(response, () -> {
                                    // イベントの解除
                                    EventError error = EventManager.INSTANCE.removeEvent(request);
                                    if (error == EventError.NONE) {
                                        stopGPS();
                                        DConnectProfile.setResult(response, DConnectMessage.RESULT_OK);
                                        response.putExtra(DConnectMessage.EXTRA_VALUE,
                                                "Unregister OnWatchPosition event");
                                    } else {
                                        MessageUtils.setUnknownError(response, "Can not unregister event.");
                                    }
                                    sendResponse(response);

                                });
                            }

                            @Override
                            public void onFail(@NonNull String deniedPermission) {
                                MessageUtils.setIllegalDeviceStateError(response,
                                        "ACCESS_FINE_LOCATION permission not granted.");
                                sendResponse(response);
                            }
                        });

                return false;
            }
        };
        addApi(mDeleteOnGeolocationApi);
    }

    /**
     * 位置情報管理クラスを取得する.
     */
    private void getLocationManager(final Intent response, final GeolocationDiaglogCallback callback) {
        if (mFusedLocationClient == null) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        }
        if (mSettingsClient == null) {
            mSettingsClient = LocationServices.getSettingsClient(getContext());
        }

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        LocationRequest locationRequest = createLocationRequest(true, 5000);
        builder.addLocationRequest(locationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        mSettingsClient.checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener(locationSettingsResponse -> callback.onSuccess()).addOnFailureListener(e -> {
                    Intent intent = new Intent(getContext(), GeolocationAlertDialogActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("response", response);
                    intent.putExtra("Intent", bundle);
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        getContext().startActivity(intent);
                    } else {
                        NotificationUtils.createNotificationChannel(getContext());
                        NotificationUtils.notify(getContext(), NOTIFICATION_ID, 0, intent,
                                getContext().getString(R.string.host_notification_geolocation_warnning));
                    }
                    //レスポンスはGeolocationAlertDialogActivity側で返す
                });
    }

    /**
     * 位置情報を取得する。（１回のみ）.
     * @param accuracy 精度.
     * @param response レスポンス.
     */
    private void getGPS(final boolean accuracy, final Intent response) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            MessageUtils.setIllegalDeviceStateError(response, "ACCESS_FINE_LOCATION permission not granted.");
            sendResponse(response);
            return;
        }

        LocationRequest locationRequest = createLocationRequest(accuracy, 5000);
        mOneShotResponse = response;
        mFusedLocationClient.requestLocationUpdates(locationRequest, mOneShotLocationCallback, Looper.getMainLooper());
    }

    /**
     * 位置情報取得開始.
     * @param accuracy 精度.
     * @param interval 受信間隔.
     */
    private void startGPS(final boolean accuracy, final int interval) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationRequest locationRequest = createLocationRequest(accuracy, interval);
        mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationEventCallback, Looper.getMainLooper());
    }

    /**
     * 位置情報取得停止.
     */
    private void stopGPS() {
        if (mFusedLocationClient != null) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getContext(), ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mFusedLocationClient.removeLocationUpdates(mLocationEventCallback);
        }
    }
    private LocationRequest createLocationRequest(boolean accuracy, int interval) {
        LocationRequest locationRequest = LocationRequest.create();

        if (accuracy) {
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        } else  {
            locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        }

        locationRequest.setInterval(interval);
        locationRequest.setFastestInterval(5000);
        return locationRequest;
    }
    /**
     * イベント登録が空か確認する.
     * @return 空の場合はtrue、それ以外はfalse
     */
    private boolean isEmptyEventList() {
        List<Event> events = EventManager.INSTANCE.getEventList(mServiceId,
                GeolocationProfile.PROFILE_NAME, null,
                GeolocationProfile.ATTRIBUTE_ON_WATCH_POSITION.toLowerCase());
        return events == null || events.size() == 0;
    }

    /**
     * 座標情報生成.
     * @param location 位置情報
     * @return 位置情報オブジェクト
     */
    private Bundle createPositionObject(final Location location) {
        Bundle coordinates = new Bundle();
        setLatitude(coordinates, location.getLatitude());
        setLongitude(coordinates, location.getLongitude());
        setAltitude(coordinates, location.getAltitude());
        setAccuracy(coordinates, location.getAccuracy());
        setHeading(coordinates, location.getBearing());
        setSpeed(coordinates, location.getSpeed());

        Bundle position = new Bundle();
        setCoordinates(position, coordinates);
        setTimeStamp(position, location.getTime());
        setTimeStampString(position, RFC3339DateUtils.toString(location.getTime()));
        mLocationCache = position;
        mLocationLastTime = location.getTime();

        return position;
    }

}
