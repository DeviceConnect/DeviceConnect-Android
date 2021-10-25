/*
 HostConnectionProfile.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.host.profile;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.host.HostDevicePlugin;
import org.deviceconnect.android.deviceplugin.host.connection.HostConnectionManager;
import org.deviceconnect.android.deviceplugin.host.connection.HostTraffic;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.ConnectionProfile;
import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Connection プロファイル.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HostConnectionProfile extends ConnectionProfile {
    // GET /gotapi/connection/wifi
    private final DConnectApi mGetWifiApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_WIFI;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            setResult(response, IntentDConnectMessage.RESULT_OK);
            response.putExtra(PARAM_ENABLE, mHostConnectionManager.isWifiEnabled());
            return true;
        }
    };

    // PUT /gotapi/connection/wifi
    private final DConnectApi mPutWifiApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_WIFI;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            setEnabledOfWiFi(request, response, true);
            return false;
        }
    };

    // DELETE /gotapi/connection/wifi
    private final DConnectApi mDeleteWifiApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_WIFI;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            setEnabledOfWiFi(request, response, false);
            return false;
        }
    };

    // PUT /gotapi/connection/onWifiChange
    private final DConnectApi mPutOnWifiChangeApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_WIFI_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                setResult(response, DConnectMessage.RESULT_ERROR);
            }
            return true;
        }
    };

    // DELETE /gotapi/connection/onWifiChange
    private final DConnectApi mDeleteOnWifiChange = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_WIFI_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response, "Can not unregister event.");
            }
            return true;
        }
    };

    // GET /gotapi/connection/bluetooth
    private final DConnectApi mGetBluetoothApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_BLUETOOTH;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            setResult(response, IntentDConnectMessage.RESULT_OK);
            response.putExtra(PARAM_ENABLE, mHostConnectionManager.isBluetoothEnabled());
            return true;
        }
    };

    // PUT /gotapi/connection/bluetooth
    private final DConnectApi mPutBluetoothApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_BLUETOOTH;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            setEnabledBluetooth(request, response, true);
            return false;
        }
    };

    // DELETE /gotapi/connection/bluetooth
    private final DConnectApi mDeleteBluetoothApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_BLUETOOTH;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            setEnabledBluetooth(request, response, false);
            return false;
        }
    };

    // PUT /gotapi/connection/onBluetoothChange
    private final DConnectApi mPutOnBluetoothChangeApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_BLUETOOTH_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.addEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                setResult(response, DConnectMessage.RESULT_ERROR);
            }
            return true;
        }
    };

    // DELETE /gotapi/connection/onBluetoothChange
    private final DConnectApi mDeleteOnBluetoothChange = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_ON_BLUETOOTH_CHANGE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            EventError error = EventManager.INSTANCE.removeEvent(request);
            if (error == EventError.NONE) {
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setInvalidRequestParameterError(response, "Can not unregister event.");
            }
            return true;
        }
    };

    // GET /gotapi/connection/ble
    private final DConnectApi mGetBleApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_BLE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            setResult(response, IntentDConnectMessage.RESULT_OK);
            response.putExtra(PARAM_ENABLE, mHostConnectionManager.getEnabledOfBluetoothLowEnergy());
            return true;
        }
    };

    // PUT /gotapi/connection/ble
    private final DConnectApi mPutBleApi = new PutApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_BLE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            setEnabledBluetooth(request, response, true);
            return false;
        }
    };

    // DELETE /gotapi/connection/ble
    private final DConnectApi mDeleteBleApi = new DeleteApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_BLE;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            setEnabledBluetooth(request, response, false);
            return false;
        }
    };

    // GET /gotapi/connection/nfc
    private final DConnectApi mGetNfcApi = new GetApi() {

        @Override
        public String getAttribute() {
            return ATTRIBUTE_NFC;
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            NfcAdapter adapter = NfcAdapter.getDefaultAdapter(getContext());
            if (adapter != null) {
                setResult(response, IntentDConnectMessage.RESULT_OK);
                response.putExtra(PARAM_ENABLE, adapter.isEnabled());
            } else {
                MessageUtils.setNotSupportAttributeError(response, "NFC is not supported.");
            }
            return true;
        }
    };

    // GET /gotapi/connection/network
    private final DConnectApi mGetNetworkApi = new GetApi() {
        @Override
        public String getAttribute() {
            return "network";
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            mHostConnectionManager.requestPermission(new HostConnectionManager.PermissionCallback() {
                @Override
                public void onAllowed() {
                    setResult(response, DConnectMessage.RESULT_OK);

                    response.putExtra("network", createNetworkCaps());
                    sendResponse(response);
                }

                @Override
                public void onDisallowed() {
                    MessageUtils.setIllegalServerStateError(response, "Permission denied.");
                    sendResponse(response);
                }
            });
            return false;
        }
    };

    // PUT /gotapi/connection/network/onChange
    private final DConnectApi mPutNetworkApi = new PutApi() {
        @Override
        public String getInterface() {
            return "network";
        }

        @Override
        public String getAttribute() {
            return "onChange";
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            mHostConnectionManager.requestPermission(new HostConnectionManager.PermissionCallback() {
                @Override
                public void onAllowed() {
                    EventError error = EventManager.INSTANCE.addEvent(request);
                    if (error == EventError.NONE) {
                        setResult(response, DConnectMessage.RESULT_OK);
                    } else {
                        setResult(response, DConnectMessage.RESULT_ERROR);
                    }
                    sendResponse(response);
                }

                @Override
                public void onDisallowed() {
                    MessageUtils.setIllegalServerStateError(response, "Permission denied.");
                    sendResponse(response);
                }
            });
            return false;
        }
    };

    // DELETE /gotapi/connection/network/onChange
    private final DConnectApi mDeleteNetworkApi = new DeleteApi() {
        @Override
        public String getInterface() {
            return "network";
        }

        @Override
        public String getAttribute() {
            return "onChange";
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            mHostConnectionManager.requestPermission(new HostConnectionManager.PermissionCallback() {
                @Override
                public void onAllowed() {
                    EventError error = EventManager.INSTANCE.removeEvent(request);
                    if (error == EventError.NONE) {
                        setResult(response, DConnectMessage.RESULT_OK);
                    } else {
                        MessageUtils.setInvalidRequestParameterError(response, "Can not unregister event.");
                    }
                    sendResponse(response);
                }

                @Override
                public void onDisallowed() {
                    MessageUtils.setIllegalServerStateError(response, "Permission denied.");
                    sendResponse(response);
                }
            });
            return false;
        }
    };

    // GET /gotapi/connection/network/bitrate
    private final DConnectApi mGetNetworkBitrateApi = new GetApi() {
        @Override
        public String getInterface() {
            return "network";
        }

        @Override
        public String getAttribute() {
            return "bitrate";
        }

        @Override
        public boolean onRequest(final Intent request, final Intent response) {
            String sinceString = request.getStringExtra("since");
            String untilString = request.getStringExtra("until");
            Long since = null;
            Long until = null;

            if (sinceString != null) {
                try {
                    since = df.parse(sinceString).getTime();
                } catch (ParseException e) {
                    // ignore.
                }
            }

            if (untilString != null) {
                try {
                    until = df.parse(untilString).getTime();
                } catch (ParseException e) {
                    // ignore.
                }
            }

            if (!HostConnectionManager.checkUsageAccessSettings(getContext())) {
                HostConnectionManager.openUsageAccessSettings(getContext());

                // 使用履歴が有効になるのをポーリングしながら待機します。
                for (int i = 0; i < 30; i++) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        break;
                    }

                    if (HostConnectionManager.checkUsageAccessSettings(getContext())) {
                        break;
                    }
                }
            }

            if (HostConnectionManager.checkUsageAccessSettings(getContext())) {
                final int[] networkTypeList = {
                        ConnectivityManager.TYPE_MOBILE,
                        ConnectivityManager.TYPE_WIFI
                };

                for (int networkType : networkTypeList) {
                    List<HostTraffic> trafficList = mHostConnectionManager.getTrafficList(networkType);
                    ArrayList<Bundle> trafficArray = new ArrayList<>();
                    for (HostTraffic traffic : trafficList) {
                        if (until != null) {
                            if (until < traffic.getStartTime()) {
                                continue;
                            }
                        }

                        if (since != null) {
                            if (traffic.getEndTime() < since) {
                                continue;
                            }
                        }

                        trafficArray.add(createNetworkBitrate(traffic));
                    }
                    response.putExtra(convertNetworkTypeToString(networkType), trafficArray);
                }
                setResult(response, DConnectMessage.RESULT_OK);
            } else {
                MessageUtils.setIllegalServerStateError(response, "Failed to start collecting a traffic.");
            }

            return true;
        }
    };

    /**
     * 接続管理クラスからのイベントを受信するリスナー.
     */
    private HostConnectionManager.ConnectionEventListener mConnectionListener;

    /**
     * 通信量計測結果を受信するリスナー.
     */
    private HostConnectionManager.TrafficEventListener mTrafficEventListener;

    /**
     * 接続管理クラス.
     */
    private final HostConnectionManager mHostConnectionManager;

    /**
     * コンストラクタ.
     * 
     * @param manager 接続管理クラス.
     */
    public HostConnectionProfile(HostConnectionManager manager) {
        mConnectionListener = new HostConnectionManager.ConnectionEventListener() {
            @Override
            public void onChangedNetwork() {
                postOnChangeNetwork();
            }

            @Override
            public void onChangedWifiStatus() {
                postOnChangedWifiStatus();
            }

            @Override
            public void onChangedBluetoothStatus() {
                postOnChangedBluetoothStatus();
            }
        };

        mTrafficEventListener = this::postOnBitrate;

        mHostConnectionManager = manager;
        mHostConnectionManager.addConnectionEventListener(mConnectionListener);
        mHostConnectionManager.addTrafficEventListener(mTrafficEventListener);

        addApi(mGetWifiApi);
        addApi(mGetBluetoothApi);
        addApi(mGetBleApi);
        addApi(mGetNfcApi);
        addApi(mPutBluetoothApi);
        addApi(mPutBleApi);
        addApi(mDeleteBluetoothApi);
        addApi(mDeleteBleApi);
        addApi(mPutOnWifiChangeApi);
        addApi(mPutOnBluetoothChangeApi);
        addApi(mDeleteOnWifiChange);
        addApi(mDeleteOnBluetoothChange);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            addApi(mPutWifiApi);
            addApi(mDeleteWifiApi);
        }

        addApi(mGetNetworkApi);
        addApi(mPutNetworkApi);
        addApi(mDeleteNetworkApi);
        addApi(mGetNetworkBitrateApi);
    }

    public void destroy() {
        if (mHostConnectionManager != null && mConnectionListener != null) {
            mHostConnectionManager.removeConnectionEventListener(mConnectionListener);
        }
        if (mHostConnectionManager != null && mTrafficEventListener != null) {
            mHostConnectionManager.removeTrafficEventListener(mTrafficEventListener);
        }
        mConnectionListener = null;
        mTrafficEventListener = null;
    }

    /**
     * WiFi接続の状態を設定する.
     * 
     * @param request リクエスト
     * @param response レスポンス
     * @param enabled WiFi接続状態
     */
    private void setEnabledOfWiFi(final Intent request, final Intent response, final boolean enabled) {
        mHostConnectionManager.setWifiEnabled(enabled, new HostConnectionManager.Callback() {
            @Override
            public void onSuccess() {
                setResult(response, IntentDConnectMessage.RESULT_OK);
                sendResponse(response);
            }

            @Override
            public void onFailure() {
                String msg;
                if (enabled) {
                    msg = "Failed to enable WiFi.";
                } else {
                    msg = "Failed to disable WiFi.";
                }
                MessageUtils.setUnknownError(response, msg);
                sendResponse(response);
            }
        });
    }

    /**
     * Bluetooth接続の状態を設定する.
     *
     * @param request リクエスト
     * @param response レスポンス
     * @param enabled Bluetooth接続状態
     */
    private void setEnabledBluetooth(final Intent request, final Intent response, final boolean enabled) {
        mHostConnectionManager.setBluetoothEnabled(enabled, new HostConnectionManager.Callback() {
            @Override
            public void onSuccess() {
                setResult(response, DConnectMessage.RESULT_OK);
                sendResponse(response);
            }

            @Override
            public void onFailure() {
                MessageUtils.setIllegalDeviceStateError(response, "Failed to enabled a bluetooth.");
                sendResponse(response);
            }
        });
    }

    private String convertNetworkTypeToString(int networkType) {
        switch (networkType) {
            case ConnectivityManager.TYPE_MOBILE:
                return "mobile";
            case ConnectivityManager.TYPE_WIFI:
                return "wifi";
            case ConnectivityManager.TYPE_BLUETOOTH:
                return "bluetooth";
            case ConnectivityManager.TYPE_ETHERNET:
                return "ethernet";
            default:
                return "unknown";
        }
    }

    private Bundle createNetworkBitrate(HostTraffic traffic) {
        Bundle send = new Bundle();
        send.putLong("bytes", traffic.getTx() / 1024);
        send.putLong("bitrate", traffic.getBitrateTx() / 1024);

        Bundle receive = new Bundle();
        receive.putLong("bytes", traffic.getRx() / 1024);
        receive.putLong("bitrate", traffic.getBitrateRx() / 1024);

        Bundle data = new Bundle();
        data.putBundle("send", send);
        data.putBundle("receive", receive);
        data.putString("start", df.format(new Date(traffic.getStartTime())));
        data.putString("end", df.format(new Date(traffic.getEndTime())));
        return data;
    }

    private final DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());

    private HostTraffic getNetworkBitrate(int type) {
        List<HostTraffic> trafficList = mHostConnectionManager.getLastTrafficForAllNetwork();
        for (HostTraffic traffic : trafficList) {
            if (type == traffic.getNetworkType()) {
                return traffic;
            }
        }
        return null;
    }

    private Bundle createNetworkCaps() {
        HostConnectionManager.NetworkCaps networkCaps = mHostConnectionManager.getNetworkCaps();
        Bundle network = new Bundle();
        network.putString("type", networkCaps.getTypeString());
        network.putInt("strengthLevel", networkCaps.getStrengthLevel());
        network.putInt("upstream", networkCaps.getUpstreamBW());
        network.putInt("downstream", networkCaps.getDownstreamBW());

        if (HostConnectionManager.checkUsageAccessSettings(getContext())) {
            switch (networkCaps.getType()) {
                case TYPE_BLUETOOTH:
                case TYPE_ETHERNET:
                    break;
                case TYPE_WIFI:
                {
                    HostTraffic traffic = getNetworkBitrate(ConnectivityManager.TYPE_WIFI);
                    if (traffic != null) {
                        network.putBundle("traffic", createNetworkBitrate(traffic));
                    }
                }   break;
                case TYPE_MOBILE:
                case TYPE_LTE_CA:
                case TYPE_NR_NSA:
                case TYPE_NR_NSA_MMWAV:
                case TYPE_LTE_ADVANCED_PRO:
                {
                    HostTraffic traffic = getNetworkBitrate(ConnectivityManager.TYPE_MOBILE);
                    if (traffic != null) {
                        network.putBundle("traffic", createNetworkBitrate(traffic));
                    }
                }   break;
            }
        }

        return network;
    }

    /**
     * ネットワークが変更されたことを通知します.
     */
    private void postOnChangeNetwork() {
        List<Event> events = EventManager.INSTANCE.getEventList(HostDevicePlugin.SERVICE_ID,
                HostConnectionProfile.PROFILE_NAME, "network", "onChange");

        Bundle network = createNetworkCaps();
        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            Intent intent = EventManager.createEventMessage(event);
            intent.putExtra("network", network);
            sendEvent(intent, event.getAccessToken());
        }
    }

    /**
     * ビットレートの計測イベントを通知します.
     *
     * @param trafficList ビットレートの計測結果のリスト
     */
    private void postOnBitrate(List<HostTraffic> trafficList) {
    }

    /**
     * Bluetooth の状態が変更されたことを通知します.
     */
    private void postOnChangedBluetoothStatus() {
        List<Event> events = EventManager.INSTANCE.getEventList(HostDevicePlugin.SERVICE_ID,
                HostConnectionProfile.PROFILE_NAME, null, HostConnectionProfile.ATTRIBUTE_ON_BLUETOOTH_CHANGE);

        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            Intent mIntent = EventManager.createEventMessage(event);
            HostConnectionProfile.setAttribute(mIntent, HostConnectionProfile.ATTRIBUTE_ON_BLUETOOTH_CHANGE);
            Bundle bluetoothConnecting = new Bundle();
            HostConnectionProfile.setEnable(bluetoothConnecting, mHostConnectionManager.isBluetoothEnabled());
            HostConnectionProfile.setConnectStatus(mIntent, bluetoothConnecting);
            sendEvent(mIntent, event.getAccessToken());
        }
    }

    /**
     * Wifi の状態が変更されたことを通知します.
     */
    private void postOnChangedWifiStatus() {
        List<Event> events = EventManager.INSTANCE.getEventList(HostDevicePlugin.SERVICE_ID,
                HostConnectionProfile.PROFILE_NAME, null, HostConnectionProfile.ATTRIBUTE_ON_WIFI_CHANGE);

        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            Intent mIntent = EventManager.createEventMessage(event);
            HostConnectionProfile.setAttribute(mIntent, HostConnectionProfile.ATTRIBUTE_ON_WIFI_CHANGE);
            Bundle wifiConnecting = new Bundle();
            HostConnectionProfile.setEnable(wifiConnecting, mHostConnectionManager.isWifiEnabled());
            HostConnectionProfile.setConnectStatus(mIntent, wifiConnecting);
            sendEvent(mIntent, event.getAccessToken());
        }
    }
}
