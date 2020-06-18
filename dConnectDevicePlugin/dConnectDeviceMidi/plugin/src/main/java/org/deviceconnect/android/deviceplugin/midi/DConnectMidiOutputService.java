package org.deviceconnect.android.deviceplugin.midi;

import android.content.Intent;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiOutputPort;
import android.media.midi.MidiReceiver;
import android.os.Bundle;

import org.deviceconnect.android.deviceplugin.midi.core.ControlChangeMessage;
import org.deviceconnect.android.deviceplugin.midi.core.MidiMessage;
import org.deviceconnect.android.deviceplugin.midi.core.MidiMessageParser;
import org.deviceconnect.android.deviceplugin.midi.core.NoteMessage;
import org.deviceconnect.android.deviceplugin.midi.core.NoteOnMessage;
import org.deviceconnect.android.deviceplugin.midi.profiles.MidiKeyEventProfile;
import org.deviceconnect.android.deviceplugin.midi.profiles.MidiSoundControllerProfile;
import org.deviceconnect.android.deviceplugin.midi.profiles.MidiVolumeControllerProfile;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.message.DConnectMessage;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class DConnectMidiOutputService extends DConnectMidiService {

    /**
     * キーイベントのキータイプ定義.
     */
    private static final int KEY_TYPE_USER = 0x00008000;

    private final Logger mLogger = Logger.getLogger("midi-plugin");

    private final MidiDevice mMidiDevice;

    private final int mPortNumber;

    private final MidiMessageParser mMessageParser =  new MidiMessageParser();

    private final MidiReceiver mMidiReceiver = new MidiReceiver() {

        @Override
        public void onSend(final byte[] data, final int offset, final int count, final long timestamp) throws IOException {
            mLogger.info("MidiReceiver.onSend: count = " + count);

            List<Event> events = EventManager.INSTANCE.getEventList(getId(), "midi", null, "onMessage");
            mLogger.info("Event: /midi/onMessage: size = " + events.size());
            for (Event event : events) {
                Intent intent = EventManager.createEventMessage(event);
                intent.putExtra("message", stringify(data, offset, count));
                getPluginContext().sendEvent(intent, event.getAccessToken());
            }

            MidiMessage message = mMessageParser.parse(data, offset, count);
            if (message instanceof NoteMessage) {
                int channel = ((NoteMessage) message).getChannelNumber();
                int noteNumber = ((NoteMessage) message).getChannelNumber();
                boolean isOn = message instanceof NoteOnMessage;
                String noteName = NoteNameTable.numberToName(noteNumber);
                if (noteName != null) {
                    sendNoteEvent(channel, noteName, isOn);
                }
                sendKeyChangeEvent(channel, noteNumber, isOn);
            } else if (message instanceof ControlChangeMessage) {
                sendVolumeChangeEvent((ControlChangeMessage) message);
            }

        }
    };

    private MidiOutputPort mMidiOutputPort;

    static DConnectMidiOutputService createService(final MidiDevice device,
                                                   final MidiDeviceInfo.PortInfo portInfo) {
        String serviceId = createServiceId(device.getInfo(), portInfo);
        return new DConnectMidiOutputService(serviceId, device, portInfo);
    }

    static String createServiceId(final MidiDeviceInfo deviceInfo,
                                  final MidiDeviceInfo.PortInfo portInfo) {
        return deviceInfo.getId() + "-out-" + portInfo.getPortNumber();
    }

    static String createServiceName(final MidiDeviceInfo deviceInfo,
                                    final MidiDeviceInfo.PortInfo portInfo) {
        Bundle props = deviceInfo.getProperties();
        if (props != null) {
            String name = props.getString(MidiDeviceInfo.PROPERTY_NAME);
            if (name != null) {
                return name + " Output [" + portInfo.getPortNumber() + "]";
            }
        }
        return "Unknown";
    }

    DConnectMidiOutputService(final String id,
                              final MidiDevice midiDevice,
                              final MidiDeviceInfo.PortInfo portInfo) {
        super(id);
        setName(createServiceName(midiDevice.getInfo(), portInfo));
        mMidiDevice = midiDevice;
        mPortNumber = portInfo.getPortNumber();
        connect();

        addProfile(new MidiOutputProfile(midiDevice));
        addProfile(new MidiKeyEventProfile());
        addProfile(new MidiSoundControllerProfile());
        addProfile(new MidiVolumeControllerProfile());
    }

    public void connect() {
        mMidiOutputPort = mMidiDevice.openOutputPort(mPortNumber);
        if (mMidiOutputPort != null) {
            mMidiOutputPort.connect(mMidiReceiver);
            setOnline(true);
        }
    }

    @Override
    public void destroy() {
        MidiOutputPort outputPort = mMidiOutputPort;
        if (outputPort != null) {
            outputPort.disconnect(mMidiReceiver);
            try {
                outputPort.close();
            } catch (IOException ignored) {}
            setOnline(false);

            mMidiOutputPort = null;
        }
    }

    private void sendNoteEvent(final int channel, final String noteName, final boolean isOn) {
        List<Event> events = EventManager.INSTANCE.getEventList(getId(), "soundController", null, "onNote");
        for (Event event : events) {
            Intent intent = EventManager.createEventMessage(event);
            intent.putExtra("channel", channel);
            intent.putExtra("note", noteName);
            intent.putExtra("state", isOn ? "on" : "off");
            getPluginContext().sendEvent(intent, event.getAccessToken());
        }
    }

    private void sendKeyChangeEvent(final int channel, final int noteNumber, final boolean isOn) {
        final int id = createKeyId(channel, noteNumber);

        for (Event event : EventManager.INSTANCE.getEventList(getId(), "keyEvent", null, "onKeyChange")) {
            Intent intent = EventManager.createEventMessage(event);
            intent.putExtra("id", id);
            intent.putExtra("state", isOn ? "down" : "up");
            getPluginContext().sendEvent(intent, event.getAccessToken());
        }

        String attribute = isOn ? "onDown" : "onUp";
        for (Event event : EventManager.INSTANCE.getEventList(getId(), "keyEvent", null, attribute)) {
            Intent intent = EventManager.createEventMessage(event);
            intent.putExtra("id", id);
            getPluginContext().sendEvent(intent, event.getAccessToken());
        }
    }

    private static int createKeyId(final int channel, final int noteNumber) {
        return KEY_TYPE_USER + (channel * 0x7F) + noteNumber;
    }

    private void sendVolumeChangeEvent(final ControlChangeMessage message) {
        int channel = message.getChannelNumber();
        int value = message.getControlValue();
        List<Event> events = EventManager.INSTANCE.getEventList(getId(), "volumeController", null, "onVolumeChange");
        for (Event event : events) {
            Intent intent = EventManager.createEventMessage(event);
            intent.putExtra("channel", channel);
            intent.putExtra("value", value);
            getPluginContext().sendEvent(intent, event.getAccessToken());
        }
    }

    private String stringify(final byte[] message, final int offset, final int count) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                result.append(",");
            }
            result.append(message[offset + i]);
        }
        return result.toString();
    }

    private static class MidiOutputProfile extends DConnectProfile {

        private final MidiDevice mMidiDevice;

        MidiOutputProfile(final MidiDevice midiDevice) {
            mMidiDevice = midiDevice;

            // GET /gotapi/midi/info
            addApi(new GetApi() {
                @Override
                public String getAttribute() {
                    return "info";
                }

                @Override
                public boolean onRequest(final Intent request, final Intent response) {
                    response.putExtra("protocol", "1.0");
                    response.putExtra("direction", "output");
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

            // PUT /gotapi/midi/onMessage
            addApi(new PutApi() {
                @Override
                public String getAttribute() {
                    return "onMessage";
                }

                @Override
                public boolean onRequest(final Intent request, final Intent response) {
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
            });

            // DELETE /gotapi/midi/onMessage
            addApi(new DeleteApi() {
                @Override
                public String getAttribute() {
                    return "onMessage";
                }

                @Override
                public boolean onRequest(final Intent request, final Intent response) {
                    EventError error = EventManager.INSTANCE.removeEvent(request);
                    switch (error) {
                        case NONE:
                            setResult(response, DConnectMessage.RESULT_OK);
                            break;
                        case INVALID_PARAMETER:
                            MessageUtils.setInvalidRequestParameterError(response);
                            break;
                        case NOT_FOUND:
                            MessageUtils.setUnknownError(response, "Event is not registered.");
                            break;
                        default:
                            MessageUtils.setUnknownError(response);
                            break;
                    }
                    return true;
                }
            });
        }

        @Override
        public String getProfileName() {
            return "midi";
        }
    }
}
