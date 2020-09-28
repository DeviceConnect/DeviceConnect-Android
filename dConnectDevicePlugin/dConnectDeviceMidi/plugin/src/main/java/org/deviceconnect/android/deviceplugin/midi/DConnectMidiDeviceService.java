/*
 DConnectMidiDeviceService.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.midi;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiInputPort;
import android.media.midi.MidiOutputPort;
import android.media.midi.MidiReceiver;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseArray;

import androidx.annotation.NonNull;

import org.deviceconnect.android.deviceplugin.midi.core.MidiMessage;
import org.deviceconnect.android.deviceplugin.midi.core.MidiMessageParser;
import org.deviceconnect.android.deviceplugin.midi.core.file.MidiFilePlayer;
import org.deviceconnect.android.deviceplugin.midi.profiles.BaseMidiOutputProfile;
import org.deviceconnect.android.deviceplugin.midi.profiles.MidiKeyEventProfile;
import org.deviceconnect.android.deviceplugin.midi.profiles.MidiSoundControllerProfile;
import org.deviceconnect.android.deviceplugin.midi.profiles.MidiSoundModuleProfile;
import org.deviceconnect.android.deviceplugin.midi.profiles.MidiVolumeControllerProfile;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.android.profile.api.PutApi;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.message.DConnectMessage;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.logging.Logger;

/**
 * MIDI デバイス操作用サービス.
 *
 * author NTT DOCOMO, INC.
 */
public class DConnectMidiDeviceService extends DConnectService implements MidiMessageSender {

    /**
     * ロガー.
     */
    private static final Logger LOGGER = Logger.getLogger("midi-plugin");

    /**
     * MIDI デバイス情報.
     */
    private final MidiDeviceInfo mMidiDeviceInfo;

    /**
     * MIDI デバイス.
     * これが null の場合、MIDI デバイスとは未接続.
     */
    private MidiDevice mMidiDevice;

    /**
     * 接続済みの入力ポートのリスト.
     */
    private final SparseArray<MidiInputBuffer> mMidiInputBuffers = new SparseArray<>();

    /**
     * 接続済みの出力ポートのリスト.
     */
    private final SparseArray<MidiOutputPort> mMidiOutputPorts = new SparseArray<>();

    /**
     * MIDI ファイルプレイヤー.
     */
    private final MidiFilePlayer mMidiFilePlayer = new MidiFilePlayer();

    /**
     * MIDI メッセージの解析器.
     */
    private final MidiMessageParser mMessageParser =  new MidiMessageParser();

    /**
     * サービス情報.
     */
    private final ServiceInfo mServiceInfo;

    /**
     * 直前の MIDI メッセージ受信イベント.
     */
    private MidiMessageEvent mLastMessageEvent;

    private DConnectMidiDeviceService(final @NonNull String serviceId, final MidiDeviceInfo deviceInfo) {
        super(serviceId);
        mMidiDeviceInfo = deviceInfo;
        mServiceInfo = new ServiceInfo(deviceInfo);

        if (deviceInfo.getInputPortCount() > 0) {
            addProfile(new MidiSoundModuleProfile(this));
        }
        if (deviceInfo.getOutputPortCount() > 0) {
            addProfile(new MidiKeyEventProfile());
            addProfile(new MidiSoundControllerProfile());
            addProfile(new MidiVolumeControllerProfile());
        }

        addProfile(new DConnectProfile() {
            @Override
            public String getProfileName() {
                return "midi";
            }

            {
                // GET /gotapi/midi/info
                addApi(new GetApi() {
                    @Override
                    public String getAttribute() {
                        return "info";
                    }

                    @Override
                    public boolean onRequest(final Intent request, final Intent response) {
                        response.putExtra("protocol", "1.0");
                        response.putExtra("direction", getDirectionName());
                        response.putExtra("inputCount", deviceInfo.getInputPortCount());
                        response.putExtra("outputCount", deviceInfo.getOutputPortCount());
                        Bundle props = mMidiDeviceInfo.getProperties();
                        if (props != null) {
                            response.putExtra("productName", props.getString(MidiDeviceInfo.PROPERTY_PRODUCT));
                            response.putExtra("manufacturer", props.getString(MidiDeviceInfo.PROPERTY_MANUFACTURER));
                        }
                        setResult(response, DConnectMessage.RESULT_OK);
                        return true;
                    }
                });

                // POST /gotapi/midi/message
                addApi(new PostApi() {
                    @Override
                    public String getAttribute() {
                        return "message";
                    }

                    @Override
                    public boolean onRequest(final Intent request, final Intent response) {
                        int portParam = getPort(request, 0);
                        if (portParam >= deviceInfo.getInputPortCount()) {
                            MessageUtils.setInvalidRequestParameterError(response, "parameter `port` must be less than " + deviceInfo.getInputPortCount());
                            return true;
                        }

                        String messageParam = request.getStringExtra("message");
                        try {
                            byte[] message = parseMessage(messageParam);

                            MidiInputBuffer inputBuffer = getInputBuffer(portParam);
                            if (inputBuffer == null) {
                                MessageUtils.setIllegalDeviceStateError(response, "input is not available");
                                return true;
                            }
                            inputBuffer.getInputPort().send(message, 0, message.length);

                            setResult(response, DConnectMessage.RESULT_OK);
                        } catch (NumberFormatException e) {
                            MessageUtils.setInvalidRequestParameterError(response, "invalid format message: " + messageParam);
                        } catch (IOException e) {
                            MessageUtils.setUnknownError(response, "Failed to send MIDI message: " + e.getMessage());
                        }
                        return true;
                    }
                });

                // POST /gotapi/midi/playFile
                addApi(new PostApi() {
                    @Override
                    public String getAttribute() {
                        return "playFile";
                    }

                    @Override
                    public boolean onRequest(final Intent request, final Intent response) {
                        int portParam = getPort(request, 0);
                        if (portParam >= deviceInfo.getInputPortCount()) {
                            MessageUtils.setInvalidRequestParameterError(response, "parameter `port` must be less than " + deviceInfo.getInputPortCount());
                            return true;
                        }
                        String uriParam = request.getStringExtra("uri");
                        if (uriParam == null) {
                            MessageUtils.setInvalidRequestParameterError(response, "parameter `uri` or `data` must be specified.");
                            return true;
                        }
                        MidiInputBuffer inputBuffer = getInputBuffer(portParam);
                        if (inputBuffer == null) {
                            MessageUtils.setIllegalDeviceStateError(response, "input is not available");
                            return true;
                        }
                        try {
                            InputStream in = getInputStream(uriParam);
                            if (in != null) {
                                synchronized (mMidiFilePlayer) {
                                    if (mMidiFilePlayer.isStarted()) {
                                        MessageUtils.setIllegalDeviceStateError(response, "Already started.");
                                        return true;
                                    }
                                    mMidiFilePlayer.load(in);
                                    mMidiFilePlayer.start(inputBuffer.getInputPort());
                                    setResult(response, DConnectMessage.RESULT_OK);
                                }
                            } else {
                                MessageUtils.setIllegalDeviceStateError(response, "Failed to read data: uri = " + uriParam);
                            }
                            return true;
                        } catch (IOException e) {
                            MessageUtils.setInvalidRequestParameterError(response, "Failed to read data: uri = " + uriParam + ", message = " + e.getMessage());
                            return true;
                        }
                    }
                });

                // GET /gotapi/midi/onMessage
                addApi(new GetApi() {
                    @Override
                    public String getAttribute() {
                        return "onMessage";
                    }

                    @Override
                    public boolean onRequest(final Intent request, final Intent response) {
                        MidiMessageEvent lastEvent = mLastMessageEvent;
                        if (lastEvent != null) {
                            response.putExtra("message", lastEvent.getMessage());
                            response.putExtra("port", lastEvent.getPort());
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

            private byte[] parseMessage(final String message) {
                String[] array = message.split(",");
                byte[] result = new byte[array.length];
                for (int i = 0; i < result.length; i++) {
                    result[i] = Byte.parseByte(array[i]);
                }
                return result;
            }

            private InputStream getInputStream(final String uriParam) throws IOException {
                try {
                    if (uriParam.startsWith("http://") || uriParam.startsWith("https://")) {
                        URL url = new URL(uriParam);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        return connection.getInputStream();
                    } else if (uriParam.startsWith("content://")) {
                        return getContext().getContentResolver().openInputStream(Uri.parse(uriParam));
                    } else {
                        return null;
                    }
                } catch (MalformedURLException e) {
                    return null;
                }
            }

            private int getPort(final Intent request, final int defaultValue) {
                String strParam = request.getStringExtra("port");
                if (strParam != null) {
                    return Integer.parseInt(strParam);
                }
                return request.getIntExtra("port", defaultValue);
            }
        });
    }

    ServiceInfo getServiceInfo() {
        return mServiceInfo;
    }

    private String getDirectionName() {
        int inputs = mMidiDeviceInfo.getInputPortCount();
        int outputs = mMidiDeviceInfo.getOutputPortCount();
        if (inputs > 0 && outputs > 0) {
            return "bidirectional";
        }
        if (inputs > 0) {
            return "input";
        }
        if (outputs > 0) {
            return "output";
        }
        return null;
    }

    void destroy() {
        mMidiDevice = null;
        setOnline(false);

        mMidiFilePlayer.stop();
        synchronized (mMidiInputBuffers) {
            for (int index = 0; index < mMidiInputBuffers.size(); index++) {
                MidiInputBuffer inputBuffer = mMidiInputBuffers.valueAt(index);
                inputBuffer.close();
            }
            mMidiInputBuffers.clear();
        }
        synchronized (mMidiOutputPorts) {
            for (int index = 0; index < mMidiOutputPorts.size(); index++) {
                MidiOutputPort outputPort = mMidiOutputPorts.valueAt(index);
                try {
                    outputPort.close();
                } catch (IOException ignored) {}
            }
            mMidiOutputPorts.clear();
        }
    }

    @Override
    public void send(final int port, final MidiMessage message) throws IOException {
        MidiInputBuffer inputBuffer = getInputBuffer(port);
        if (inputBuffer == null) {
            throw new IOException("input is not available");
        }
        inputBuffer.send(message);
    }

    private MidiInputBuffer getInputBuffer(final int port) {
        MidiDevice midiDevice = mMidiDevice;
        if (midiDevice == null) {
            return null;
        }
        MidiInputBuffer inputBuffer;
        synchronized (mMidiInputBuffers) {
            inputBuffer = mMidiInputBuffers.get(port);
            if (inputBuffer == null) {
                MidiInputPort inputPort = mMidiDevice.openInputPort(port);
                if (inputPort == null) {
                    return null;
                }
                inputBuffer = new MidiInputBuffer(inputPort);
                mMidiInputBuffers.put(port, inputBuffer);
            }
        }
        return inputBuffer;
    }

    synchronized void setMidiDevice(final MidiDevice device) {
        mMidiDevice = device;
        for (int port = 0; port < device.getInfo().getOutputPortCount(); port++) {
            connectOutputPort(port);
        }

        setOnline(mMidiDevice != null);
    }

    private void connectOutputPort(final int port) {
        MidiOutputPort outputPort = mMidiDevice.openOutputPort(port);
        if (outputPort != null) {
            outputPort.connect(createMidiReceiver(port));
            mMidiOutputPorts.put(port, outputPort);
        }
    }

    private MidiReceiver createMidiReceiver(final int port) {
        return new MidiReceiver() {
            @Override
            public void onSend(final byte[] data, final int offset, final int count, final long timestamp) throws IOException {
                LOGGER.info("MidiReceiver.onSend: port = " + port + ", count = " + count);

                List<Event> events = EventManager.INSTANCE.getEventList(getId(), "midi", null, "onMessage");
                String message = stringify(data, offset, count);
                LOGGER.info("Event: /midi/onMessage: size = " + events.size() + ", message = " + message);

                MidiMessageEvent messageEvent = new MidiMessageEvent(port, message);
                for (Event event : events) {
                    Intent intent = EventManager.createEventMessage(event);
                    intent.putExtra("message", messageEvent.getMessage());
                    intent.putExtra("port", messageEvent.getPort());
                    getPluginContext().sendEvent(intent, event.getAccessToken());
                }

                MidiMessage midiMessage = mMessageParser.parse(data, offset, count);
                if (midiMessage != null) {
                    handleParsedMessage(port, midiMessage, timestamp);
                }

                mLastMessageEvent = messageEvent;
            }
        };
    }

    private void handleParsedMessage(final int port, final MidiMessage message, final long timestamp) {
        for (DConnectProfile profile : getProfileList()) {
            if (profile instanceof BaseMidiOutputProfile) {
                ((BaseMidiOutputProfile) profile).sendEvent(port, message, timestamp);
            }
        }
    }

    @Override
    public boolean isOnline() {
        return mMidiDevice != null;
    }

    static DConnectMidiDeviceService getInstance(final MidiDeviceInfo deviceInfo) {
        String serviceId = createServiceId(deviceInfo);
        if (serviceId == null) {
            return null;
        }
        DConnectMidiDeviceService service = new DConnectMidiDeviceService(serviceId, deviceInfo);
        service.setName(createServiceName(deviceInfo));
        return service;
    }

    static String createServiceId(final MidiDeviceInfo deviceInfo) {
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
                    LOGGER.info("createServiceId: USB Device ID = " + device.getDeviceId() + ", MIDI ID = " + deviceInfo.getId());
                    String deviceName = createServiceName(deviceInfo);
                    deviceId = device.getDeviceId() + "_" + md5(deviceName);
                } else {
                    
                    LOGGER.warning("createServiceId: NO USB DEVICE INFO; MIDI ID = " + deviceInfo.getId());
                    deviceId = "midi_" + deviceInfo.getId();
                }
                break;
            }
            case MidiDeviceInfo.TYPE_BLUETOOTH: {
                deviceType = "bluetooth";
                BluetoothDevice device = props.getParcelable(MidiDeviceInfo.PROPERTY_BLUETOOTH_DEVICE);
                if (device != null) {
                    deviceId = device.getAddress();
                } else {
                    return null;
                }
                break;
            }
            case MidiDeviceInfo.TYPE_VIRTUAL:
                deviceType = "virtual";
                deviceId = Integer.toString(deviceInfo.getId());
                break;
            default:
                return null;
        }

        final String[] array = {
                deviceType,
                deviceId
        };
        return concat(array);
    }

    public static String md5(final String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(s.getBytes());
            byte[] messageDigest = md.digest();

            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < messageDigest.length; i++) {
                hex.append(String.format("%02x", messageDigest[i] & 0xFF));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            return s;
        }
    }

    private static String createServiceName(final MidiDeviceInfo deviceInfo) {
        Bundle props = deviceInfo.getProperties();
        if (props != null) {
            String name = props.getString(MidiDeviceInfo.PROPERTY_NAME);
            if (name != null) {
                return name;
            }
        }
        return "Unknown";
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

    private static class MidiMessageEvent {
        private final int mPort;
        private final String mMessage;

        public MidiMessageEvent(final int port, final String message) {
            mPort = port;
            mMessage = message;
        }

        public int getPort() {
            return mPort;
        }

        public String getMessage() {
            return mMessage;
        }
    }
}
