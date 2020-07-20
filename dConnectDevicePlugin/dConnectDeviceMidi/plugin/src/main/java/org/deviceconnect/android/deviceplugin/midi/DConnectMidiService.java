package org.deviceconnect.android.deviceplugin.midi;

import android.hardware.usb.UsbDevice;
import android.media.midi.MidiDeviceInfo;
import android.os.Bundle;
import android.util.Log;

import org.deviceconnect.android.service.DConnectService;

import static org.deviceconnect.android.deviceplugin.midi.BuildConfig.DEBUG;

public abstract class DConnectMidiService extends DConnectService {

    private static final String TAG = "MIDI";

    private final ServiceInfo mServiceInfo;

    DConnectMidiService(final String id,
                        final MidiDeviceInfo deviceInfo,
                        final MidiDeviceInfo.PortInfo portInfo) {
        super(id);
        mServiceInfo = new ServiceInfo(deviceInfo, portInfo);
    }

    ServiceInfo getServiceInfo() {
        return mServiceInfo;
    }

    public void destroy() {
    }

    static String createServiceId(final MidiDeviceInfo deviceInfo,
                                  final MidiDeviceInfo.PortInfo portInfo) {
        final String deviceType;
        final String deviceId;
        final Bundle props = deviceInfo.getProperties();
        if (props == null) {
            if (DEBUG) {
                Log.e(TAG, "createServiceId: No properties for MIDI Device.");
            }
            return null;
        }

        switch (deviceInfo.getType()) {
            case MidiDeviceInfo.TYPE_USB: {
                deviceType = "usb";
                UsbDevice device = props.getParcelable(MidiDeviceInfo.PROPERTY_USB_DEVICE);
                if (device != null) {
                    deviceId = Integer.toString(device.getDeviceId());
                } else {
                    if (DEBUG) {
                        Log.e(TAG, "createServiceId: Failed to get USB device info.");
                    }
                    return null;
                }
                break;
            }
            case MidiDeviceInfo.TYPE_BLUETOOTH:
                deviceType = "bluetooth";
                deviceId = Integer.toString(deviceInfo.getId()); // TODO BT接続切断後の再接続でIDが変わってしまわないかどうかを確認
                break;
            case MidiDeviceInfo.TYPE_VIRTUAL:
                deviceType = "virtual";
                deviceId = Integer.toString(deviceInfo.getId());
                break;
            default:
                if (DEBUG) {
                    Log.e(TAG, "createServiceId: Failed to detect device type.");
                }
                return null;
        }

        final String portDirection;
        switch (portInfo.getType()) {
            case MidiDeviceInfo.PortInfo.TYPE_INPUT:
                portDirection = "input";
                break;
            case MidiDeviceInfo.PortInfo.TYPE_OUTPUT:
                portDirection = "output";
                break;
            default:
                if (DEBUG) {
                    Log.e(TAG, "createServiceId: Failed to detect port direction.");
                }
                return null;
        }

        final String[] array = {
                "midi",
                deviceType,
                deviceId,
                portDirection,
                Integer.toString(portInfo.getPortNumber())
        };
        return concat(array);
    }

    private static String concat(final String[] parts) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                result.append("-");
            }
            result.append(parts[i]);
        }
        return result.toString();
    }
}
