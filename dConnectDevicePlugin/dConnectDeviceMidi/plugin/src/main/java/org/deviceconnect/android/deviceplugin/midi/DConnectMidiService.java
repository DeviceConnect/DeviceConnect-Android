package org.deviceconnect.android.deviceplugin.midi;

import android.hardware.usb.UsbDevice;
import android.media.midi.MidiDeviceInfo;
import android.os.Bundle;

import org.deviceconnect.android.service.DConnectService;

public abstract class DConnectMidiService extends DConnectService {

    private final ServiceInfo mServiceInfo;

    DConnectMidiService(final String id,
                        final MidiDeviceInfo deviceInfo,
                        final MidiDeviceInfo.PortInfo portInfo) {
        super(id);
        mServiceInfo = new ServiceInfo(deviceInfo, portInfo);
    }

    public ServiceInfo getServiceInfo() {
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
            return null;
        }

        switch (deviceInfo.getType()) {
            case MidiDeviceInfo.TYPE_USB: {
                deviceType = "usb";
                UsbDevice device = props.getParcelable(MidiDeviceInfo.PROPERTY_USB_DEVICE);
                if (device != null) {
                    deviceId = Integer.toString(device.getDeviceId());
                } else {
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
                return null;
        }

        final String[] array = {
                "midi",
                deviceType,
                deviceId,
                portDirection,
                Integer.toString(portInfo.getPortNumber())
        };
        return concat(array, "-");
    }

    private static String concat(final String[] parts, final String separator) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                result.append(separator);
            }
            result.append(parts[i]);
        }
        return result.toString();
    }
}
