package org.deviceconnect.android.deviceplugin.midi.profiles;

import android.content.Intent;
import android.media.midi.MidiDevice;
import android.media.midi.MidiInputPort;

import org.deviceconnect.android.deviceplugin.midi.MidiMessageSender;
import org.deviceconnect.android.deviceplugin.midi.NoteNameTable;
import org.deviceconnect.android.deviceplugin.midi.core.MidiMessage;
import org.deviceconnect.android.deviceplugin.midi.core.NoteOffMessage;
import org.deviceconnect.android.deviceplugin.midi.core.NoteOnMessage;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.PostApi;
import org.deviceconnect.message.DConnectMessage;

import java.io.IOException;

public class MidiSoundModuleProfile extends BaseMidiProfile {

    private final MidiMessageSender mMessageSender;

    public MidiSoundModuleProfile(final MidiMessageSender messageSender) {
        mMessageSender = messageSender;

        // POST /gotapi/soundModule/note
        addApi(new PostApi() {
            @Override
            public String getAttribute() {
                return "note";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String note = request.getStringExtra("note");
                Integer channel = parseInteger(request, "channel");

                Integer noteNumber = NoteNameTable.nameToNumber(note);
                if (noteNumber == null) {
                    MessageUtils.setInvalidRequestParameterError(response,
                            note + " is not supported as note name");
                    return true;
                }

                NoteOnMessage message = new NoteOnMessage.Builder()
                        .setChannelNumber(channel != null ? channel : 0)
                        .setNoteNumber(noteNumber)
                        .build();
                try {
                    mMessageSender.send(message);
                    setResult(response, DConnectMessage.RESULT_OK);
                } catch (IOException e) {
                    MessageUtils.setUnknownError(response, "Failed to send MIDI message: " + e.getMessage());
                }
                return true;
            }
        });

        // DELETE /gotapi/soundModule/note
        addApi(new DeleteApi() {
            @Override
            public String getAttribute() {
                return "note";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                String note = request.getStringExtra("note");
                Integer channel = parseInteger(request, "channel");

                Integer noteNumber = NoteNameTable.nameToNumber(note);
                if (noteNumber == null) {
                    MessageUtils.setInvalidRequestParameterError(response,
                            note + " is not supported as note name");
                    return true;
                }

                NoteOffMessage message = new NoteOffMessage.Builder()
                        .setChannelNumber(channel != null ? channel : 0)
                        .setNoteNumber(noteNumber)
                        .build();
                try {
                    mMessageSender.send(message);
                    setResult(response, DConnectMessage.RESULT_OK);
                } catch (IOException e) {
                    MessageUtils.setUnknownError(response, "Failed to send MIDI message: " + e.getMessage());
                }
                return true;
            }
        });

    }

    @Override
    public String getProfileName() {
        return "soundModule";
    }
}