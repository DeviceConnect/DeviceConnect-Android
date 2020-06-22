package org.deviceconnect.android.deviceplugin.midi.profiles;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.deviceconnect.android.deviceplugin.midi.core.MidiMessage;
import org.deviceconnect.android.event.Event;
import org.deviceconnect.android.event.EventError;
import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.message.DConnectMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public abstract class BaseMidiOutputProfile extends BaseMidiProfile {

    private final Logger mLogger = Logger.getLogger("midi-plugin");

    private List<MessageEvent> mLastEventList;

    boolean onEventCacheRequest(final Intent request, final Intent response) {
        MessageEvent messageEvent = getLastMessageEvent(request);
        if (messageEvent != null) {
            messageEvent.putExtras(response);
        }
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    boolean onAddEventRequest(final Intent request, final Intent response) {
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

    boolean onRemoveEventRequest(final Intent request, final Intent response) {
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

    public void sendEvent(final @NonNull MidiMessage message, final long timestamp) {
        List<MessageEvent> results = new ArrayList<>();
        convertMessageToEvent(message, timestamp, results);
        mLastEventList = results;
        for (MessageEvent result : results) {
            List<Event> events = EventManager.INSTANCE.getEventList(
                    getService().getId(),
                    result.getProfile(),
                    result.getInterface(),
                    result.getAttribute());
            for (Event event : events) {
                Intent intent = EventManager.createEventMessage(event);
                result.putExtras(intent);
                sendEvent(intent, event.getAccessToken());
            }
        }
    }

    abstract void convertMessageToEvent(final @NonNull MidiMessage message,
                                        final long timestamp,
                                        final @NonNull List<MessageEvent> results);

    private MessageEvent getLastMessageEvent(final Intent request) {
        List<MessageEvent> messageEventList = mLastEventList;
        if (messageEventList != null) {
            String profileName = request.getStringExtra("profile");
            String interfaceName = request.getStringExtra("interface");
            String attributeName = request.getStringExtra("attribute");
            mLogger.info("getLastMessageEvent: profile=" + profileName + ", interface=" + interfaceName + ", attribute=" + attributeName);
            if (profileName != null) {
                for (MessageEvent messageEvent : mLastEventList) {
                    if (messageEvent.isSameEvent(profileName, interfaceName, attributeName)) {
                        return messageEvent;
                    }
                }
            }
        }
        return null;
    }

    static abstract class MessageEvent {
        final long mTimestamp;

        MessageEvent(final long timestamp) {
            if (timestamp <= 0) {
                throw new IllegalArgumentException("timestamp is not a positive integer: " + timestamp);
            }
            mTimestamp = timestamp;
        }

        long getTimestamp() {
            return mTimestamp;
        }

        long getElapsedTime() {
            return System.currentTimeMillis() - getTimestamp();
        }

        abstract void putExtras(final Intent intent);

        @NonNull
        abstract String getProfile();

        @Nullable
        abstract String getInterface();

        @Nullable
        abstract String getAttribute();

        boolean isSameEvent(final @NonNull String profileName,
                            final @Nullable String interfaceName,
                            final @Nullable String attributeName) {
            return isSame(profileName, getProfile())
                    && isSame(interfaceName, getInterface())
                    && isSame(attributeName, getAttribute());
        }

        private boolean isSame(final String a, final String b) {
            if (a == null && b == null) {
                return true;
            }
            if (a == null) {
                return false;
            }
            if (b == null) {
                return false;
            }
            return a.equalsIgnoreCase(b);
        }
    }
}
