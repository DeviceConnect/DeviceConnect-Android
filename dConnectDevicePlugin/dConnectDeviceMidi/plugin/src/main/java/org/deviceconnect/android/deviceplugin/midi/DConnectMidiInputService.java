package org.deviceconnect.android.deviceplugin.midi;

import android.content.Intent;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiInputPort;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.midi.core.MidiMessage;
import org.deviceconnect.android.deviceplugin.midi.profiles.MidiSoundModuleProfile;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.message.DConnectMessage;

import java.io.IOException;
import java.nio.ByteBuffer;

public class DConnectMidiInputService extends DConnectService implements MidiMessageSender {

    private final MidiDevice mMidiDevice;

    private final int mPortNumber;

    private final Object mLock = new Object();

    private final ByteBuffer mMessageBuffer;

    private final byte[] mMessageArray;

    private MidiInputPort mMidiInputPort;

    private DConnectMidiInputService(final String id,
                                     final MidiDevice midiDevice,
                                     final MidiDeviceInfo.PortInfo portInfo) {
        super(id);
        setName(createServiceName(midiDevice.getInfo(), portInfo));
        setOnline(true);
        mMidiDevice = midiDevice;
        mPortNumber = portInfo.getPortNumber();
        mMessageArray = new byte[1024];
        mMessageBuffer = ByteBuffer.wrap(mMessageArray);

        addProfile(new MidiSoundModuleProfile(this));
        addProfile(new DConnectProfile() {
            @Override
            public String getProfileName() {
                return "midi";
            }

            {
                // GET /midi/info
                addApi(new GetApi() {
                    @Override
                    public String getAttribute() {
                        return "info";
                    }

                    @Override
                    public boolean onRequest(final Intent request, final Intent response) {
                        response.putExtra("protocol", "1.0");
                        response.putExtra("direction", "input");
                        MidiDeviceInfo deviceInfo = mMidiDevice.getInfo();
                        Bundle props = deviceInfo.getProperties();
                        if (props != null) {
                            response.putExtra("productName", props.getString(MidiDeviceInfo.PROPERTY_PRODUCT));
                            response.putExtra("manufacturer", props.getString(MidiDeviceInfo.PROPERTY_MANUFACTURER));
                        }
                        setResult(response, DConnectMessage.RESULT_OK);
                        return true;
                    }
                });

                // POST /midi/message
                addApi(new PostApi() {
                    @Override
                    public String getAttribute() {
                        return "message";
                    }

                    @Override
                    public boolean onRequest(final Intent request, final Intent response) {
                        String messageParam = request.getStringExtra("message");
                        try {
                            byte[] message = parseMessage(messageParam);

                            MidiInputPort inputPort = getInputPort();
                            if (inputPort == null) {
                                MessageUtils.setIllegalDeviceStateError(response, "input port is not available");
                                return true;
                            }
                            inputPort.send(message, 0, message.length);

                            setResult(response, DConnectMessage.RESULT_OK);
                        } catch (NumberFormatException e) {
                            MessageUtils.setInvalidRequestParameterError(response, "invalid format message: " + messageParam);
                        } catch (IOException e) {
                            MessageUtils.setUnknownError(response, "Failed to send MIDI message: " + e.getMessage());
                        }
                        return true;
                    }
                });
            }
        });
    }

    static DConnectMidiInputService createService(final MidiDevice device,
                                                  final MidiDeviceInfo.PortInfo portInfo) {
        String serviceId = createServiceId(device.getInfo(), portInfo);
        return new DConnectMidiInputService(serviceId, device, portInfo);
    }

    static String createServiceId(final MidiDeviceInfo deviceInfo,
                                  final MidiDeviceInfo.PortInfo portInfo) {
        return deviceInfo.getId() + "-in-" + portInfo.getPortNumber();
    }

    static String createServiceName(final MidiDeviceInfo deviceInfo,
                                    final MidiDeviceInfo.PortInfo portInfo) {
        Bundle props = deviceInfo.getProperties();
        if (props != null) {
            String name = props.getString(MidiDeviceInfo.PROPERTY_NAME);
            if (name != null) {
                return name + " Input [" + portInfo.getPortNumber() + "]";
            }
        }
        return "Unknown";
    }

    MidiInputPort getInputPort() {
        MidiInputPort inputPort;
        synchronized (mLock) {
            inputPort = mMidiInputPort;
            if (inputPort == null) {
                inputPort = mMidiDevice.openInputPort(mPortNumber);
                if (inputPort == null) {
                    return null;
                }
                mMidiInputPort = inputPort;
            }
        }
        return inputPort;
    }

    @Override
    public void send(final MidiMessage message) throws IOException {
        MidiInputPort inputPort = getInputPort();
        if (inputPort == null) {
            throw new IOException("port is not available");
        }
        synchronized (mMessageBuffer) {
            mMessageBuffer.clear();
            message.append(mMessageBuffer);
            inputPort.send(mMessageArray, 0, mMessageBuffer.position());
        }
    }

    void destroy() {
        synchronized (mLock) {
            if (mMidiInputPort != null) {
                try {
                    mMidiInputPort.close();
                } catch (IOException ignored) {}
            }
        }
    }

    private byte[] parseMessage(final String message) {
        String[] array = message.split(",");
        byte[] result = new byte[array.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = Byte.parseByte(array[i]);
        }
        return result;
    }
}
