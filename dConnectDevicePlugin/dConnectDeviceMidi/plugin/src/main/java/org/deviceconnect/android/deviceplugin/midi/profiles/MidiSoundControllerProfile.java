package org.deviceconnect.android.deviceplugin.midi.profiles;

import android.content.Intent;

import androidx.annotation.NonNull;

import org.deviceconnect.android.deviceplugin.midi.NoteNameTable;
import org.deviceconnect.android.deviceplugin.midi.core.MidiMessage;
import org.deviceconnect.android.deviceplugin.midi.core.NoteMessage;
import org.deviceconnect.android.deviceplugin.midi.core.NoteOnMessage;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;

import java.util.List;

public class MidiSoundControllerProfile extends BaseMidiOutputProfile {

    public MidiSoundControllerProfile() {

        // GET /gotapi/soundController/onNote
        addApi(new GetApi() {
            @Override
            public String getAttribute() {
                return "onNote";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                return onEventCacheRequest(request, response);
            }
        });

        // PUT /gotapi/soundController/onNote
        addApi(new PutApi() {
            @Override
            public String getAttribute() {
                return "onNote";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                return onAddEventRequest(request, response);
            }
        });

        // DELETE /gotapi/soundController/onNote
        addApi(new DeleteApi() {
            @Override
            public String getAttribute() {
                return "onNote";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                return onRemoveEventRequest(request, response);
            }
        });

    }

    @Override
    public String getProfileName() {
        return "soundController";
    }

    @Override
    void convertMessageToEvent(final @NonNull MidiMessage message, final long timestamp, final @NonNull List<MessageEvent> results) {
        if (message instanceof NoteMessage) {
            int channel = ((NoteMessage) message).getChannelNumber();
            int noteNumber = ((NoteMessage) message).getChannelNumber();
            boolean isOn = message instanceof NoteOnMessage;
            String noteName = NoteNameTable.numberToName(noteNumber);
            if (noteName != null) {
                results.add(new NoteEvent(timestamp, channel, noteName, isOn));
            }
        }
    }

    private class NoteEvent extends BaseMidiOutputProfile.MessageEvent {

        static final String STATE_ON = "on";

        static final String STATE_OFF = "off";

        private final int mChannel;

        private final String mNoteName;

        private final String mState;

        NoteEvent(final long timestamp, final int channel, final String noteName, final String state) {
            super(timestamp);
            mChannel = channel;
            mNoteName = noteName;
            mState = state;
        }

        NoteEvent(final long timestamp, final int channel, final String noteName, final boolean isOn) {
            this(timestamp, channel, noteName, isOn ? STATE_ON : STATE_OFF);
        }

        @Override
        void putExtras(final Intent intent) {
            intent.putExtra("channel", mChannel);
            intent.putExtra("note", mNoteName);
            intent.putExtra("state", mState);
        }

        @NonNull
        @Override
        String getProfile() {
            return getProfileName();
        }

        @Override
        String getInterface() {
            return null;
        }

        @Override
        String getAttribute() {
            return "onNote";
        }
    }
}