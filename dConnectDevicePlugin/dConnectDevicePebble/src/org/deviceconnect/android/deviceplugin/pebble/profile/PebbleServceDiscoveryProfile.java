/*
 PebbleServceDiscoveryProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.pebble.profile;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;

import com.getpebble.android.kit.PebbleKit;

import org.deviceconnect.android.deviceplugin.pebble.PebbleDeviceService;
import org.deviceconnect.android.deviceplugin.pebble.util.PebbleManager.OnConnectionStatusListener;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Pebble用 Network Service Discoveryプロファイル.
 * @author NTT DOCOMO, INC.
 */
public class PebbleServceDiscoveryProfile extends ServiceDiscoveryProfile {
    /**
     * サービスIDのプレフィックス.
     */
    public static final String SERVICE_ID = "Pebble";
    /**
     * デバイス名を定義.
     */
    public static final String DEVICE_NAME = "Pebble";

    /**
     * コンストラクタ.
     * @param service サービス
     * @param provider プロファイルプロバイダ
     */
    public PebbleServceDiscoveryProfile(final PebbleDeviceService service,
            final DConnectProfileProvider provider) {
        super(provider);
        service.getPebbleManager().addConnectStatusListener(new OnConnectionStatusListener() {
            @Override
            public void onConnect() {
                Bundle service = new Bundle();
                setName(service, DEVICE_NAME);
                setType(service, NetworkType.BLUETOOTH);
                setOnline(service, true);
                setScopes(service, getProfileProvider());

                List<Event> evts = EventManager.INSTANCE.getEventList(
                        PROFILE_NAME, null, ATTRIBUTE_ON_SERVICE_CHANGE);
                for (Event evt : evts) {
                    Intent intent = EventManager.createEventMessage(evt);
                    intent.putExtra(ServiceDiscoveryProfile.PARAM_NETWORK_SERVICE, service);
                    sendEvent(intent, evt.getAccessToken());
                }
            }
            @Override
            public void onDisconnect() {
                Bundle service = new Bundle();
                setName(service, DEVICE_NAME);
                setType(service, NetworkType.BLUETOOTH);
                setOnline(service, false);
                setScopes(service, getProfileProvider());

                List<Event> evts = EventManager.INSTANCE.getEventList(
                        PROFILE_NAME, null, ATTRIBUTE_ON_SERVICE_CHANGE);
                for (Event evt : evts) {
                    Intent intent = EventManager.createEventMessage(evt);
                    intent.putExtra(ServiceDiscoveryProfile.PARAM_NETWORK_SERVICE, service);
                    sendEvent(intent, evt.getAccessToken());
                }
            }
        });
    }
    @Override
    public boolean onGetServices(final Intent request, final Intent response) {
        boolean connected = PebbleKit.isWatchConnected(getContext());
        boolean supported = PebbleKit.areAppMessagesSupported(getContext());
        if (connected && supported) {
            List<Bundle> services = new ArrayList<Bundle>();
            BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
            Set<BluetoothDevice> bondedDevices = defaultAdapter.getBondedDevices();
            if (bondedDevices.size() > 0) {
                for (BluetoothDevice device : bondedDevices) {
                    String deviceName = device.getName();
                    String deviceAddress = device.getAddress();
                    // URIに使えるように、Macアドレスの":"を取り除いて小文字に変換する 
                    String serviceId = deviceAddress.replace(":", "")
                            .toLowerCase(Locale.getDefault());
                    if (deviceName.indexOf("Pebble") != -1) {
                        Bundle service = new Bundle();
                        setId(service, SERVICE_ID + serviceId);
                        setName(service, deviceName);
                        setType(service, NetworkType.BLUETOOTH);
                        setOnline(service, true);
                        setScopes(service, getProfileProvider());
                        services.add(service);
                    }
                }
            }
            setServices(response, services);
            setResult(response, DConnectMessage.RESULT_OK);
        } else {
            MessageUtils.setNotFoundServiceError(response);
        }
        return true;
    }

    @Override
    protected boolean onPutOnServiceChange(final Intent request, final Intent response,
                 final String serviceId, final String sessionKey) {
        EventError error = EventManager.INSTANCE.addEvent(request);
        switch (error) {
        case NONE:
            setResult(response, DConnectMessage.RESULT_OK);
            break;
        case INVALID_PARAMETER:
            MessageUtils.setInvalidRequestParameterError(response);
            break;
        default:
            MessageUtils.setUnknownError(response);
            break;
        }
        return true;
    }

    @Override
    protected boolean onDeleteOnServiceChange(final Intent request, final Intent response,
                       final String serviceId, final String sessionKey) {
        EventError error = EventManager.INSTANCE.removeEvent(request);
        switch (error) {
        case NONE:
            setResult(response, DConnectMessage.RESULT_OK);
            break;
        case INVALID_PARAMETER:
            MessageUtils.setInvalidRequestParameterError(response);
            break;
        default:
            MessageUtils.setUnknownError(response);
            break;
        }
        return true;
    }
}
