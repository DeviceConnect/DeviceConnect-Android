/*
 BaseMidiOutputProfile.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MIDI 出力用プロファイルのベースクラス.
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class BaseMidiOutputProfile extends BaseMidiProfile {

    private final Map<Class< ? extends MessageEvent>, MessageEvent> mLastEventList = new HashMap<>();

    boolean onEventCacheRequest(final Class<? extends MessageEvent> eventClass, final Intent response) {
        MessageEvent messageEvent = getLastMessageEvent(eventClass);
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

    public void sendEvent(final int port, final @NonNull MidiMessage message, final long timestamp) {
        List<MessageEvent> results = new ArrayList<>();
        convertMessageToEvent(port, message, timestamp, results);

        synchronized (mLastEventList) {
            for (MessageEvent result : results) {
                if (result != null) {
                    mLastEventList.put(result.getClass(), result);
                }
            }
        }

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

    abstract void convertMessageToEvent(final int port,
                                        final @NonNull MidiMessage message,
                                        final long timestamp,
                                        final @NonNull List<MessageEvent> results);

    private MessageEvent getLastMessageEvent(final Class<? extends MessageEvent> eventClass) {
        synchronized (mLastEventList) {
            return mLastEventList.get(eventClass);
        }
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
    }
}
