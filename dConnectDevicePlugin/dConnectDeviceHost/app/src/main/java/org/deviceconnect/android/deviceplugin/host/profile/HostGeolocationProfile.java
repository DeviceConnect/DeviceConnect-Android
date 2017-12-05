/*
 HostGeolocationProfile.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.profile;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import org.deviceconnect.android.activity.PermissionUtility;
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
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.utils.RFC3339DateUtils;

import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

/**
 * Geolocation Profile.
 * @author NTT DOCOMO, INC.
 */
public class HostGeolocationProfile extends GeolocationProfile implements LocationListener {
    /** LocationManager. */
    private LocationManager mLocationManager;

    /** ServiceID. */
    private String mServiceId;

    /** 前回の位置情報の計測時間を保持する. */
    private long mLocationLastTime;

    /** 前回の位置情報を保持する. */
    private Bundle mLocationCache;

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
                        new Handler(Looper.getMainLooper()), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        new PermissionUtility.PermissionRequestCallback() {
                            @Override
                            public void onSuccess() {
                                long maximumAge = (long) getMaximumAge(request);
                                if (System.currentTimeMillis() - mLocationLastTime < maximumAge) {
                                    DConnectProfile.setResult(response, DConnectMessage.RESULT_OK);
                                    response.putExtra(GeolocationProfile.PARAM_POSITION, mLocationCache);
                                    sendResponse(response);
                                } else {
                                    getLocationManager(response);
                                    getGPS(getHighAccuracy(request), response);
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
                        new Handler(Looper.getMainLooper()), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        new PermissionUtility.PermissionRequestCallback() {
                            @Override
                            public void onSuccess() {
                                getLocationManager(response);
                                String serviceId = getServiceID(request);
                                // イベントの登録
                                EventError error = EventManager.INSTANCE.addEvent(request);
                                if (error == EventError.NONE) {
                                    startGPS(getHighAccuracy(request), (int) getInterval(request));
                                    mServiceId = serviceId;
                                    DConnectProfile.setResult(response, DConnectMessage.RESULT_OK);
                                    response.putExtra(DConnectMessage.EXTRA_VALUE,
                                            "Register OnWatchPosition event");
                                } else {
                                    MessageUtils.setUnknownError(response, "Can not register event.");
                                }
                                sendResponse(response);
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
                        new Handler(Looper.getMainLooper()), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        new PermissionUtility.PermissionRequestCallback() {
                            @Override
                            public void onSuccess() {
                                getLocationManager(response);
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
     * @return 位置情報管理クラス
     */
    private LocationManager getLocationManager(final Intent response) {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        }
        // GPSセンサー利用可否判定.
        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent intent = new Intent(getContext(), GeolocationAlertDialogActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Bundle bundle = new Bundle();
            bundle.putParcelable("response", response);
            intent.putExtra("Intent", bundle);
            getContext().startActivity(intent);
        }
        return mLocationManager;
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

        Criteria criteria = new Criteria();
        if (accuracy) {
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
        } else {
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        }

        mLocationManager.requestSingleUpdate(mLocationManager.getBestProvider(criteria, true), new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Bundle position = createPositionObject(location);

                DConnectProfile.setResult(response, DConnectMessage.RESULT_OK);
                response.putExtra(GeolocationProfile.PARAM_POSITION, position);
                sendResponse(response);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // NOP
            }

            @Override
            public void onProviderEnabled(String provider) {
                // NOP
            }

            @Override
            public void onProviderDisabled(String provider) {
                // NOP
            }
        }, Looper.getMainLooper());
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

        Criteria criteria = new Criteria();
        if (accuracy) {
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
        } else {
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        }

        mLocationManager.requestLocationUpdates(mLocationManager.getBestProvider(criteria, true), interval, 0, this, Looper.getMainLooper());
    }

    /**
     * 位置情報取得停止.
     */
    private void stopGPS() {
        if (mLocationManager != null) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getContext(), ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mLocationManager.removeUpdates(this);
        }
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

    @Override
    public void onLocationChanged(Location location) {
        Bundle position = createPositionObject(location);

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

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // NOP
    }

    @Override
    public void onProviderEnabled(String provider) {
        // NOP
    }

    @Override
    public void onProviderDisabled(String provider) {
        // NOP
    }

}
