/*
 MidiSoundModuleProfile.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.midi.profiles;

import android.content.Intent;

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

/**
 * SoundModule プロファイルの実装.
 *
 * @author NTT DOCOMO, INC.
 */
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
                Integer channelParam = parseInteger(request, "channel");

                Integer noteNumber = NoteNameTable.nameToNumber(note);
                if (noteNumber == null) {
                    MessageUtils.setInvalidRequestParameterError(response,
                            note + " is not supported as note name");
                    return true;
                }

                NoteOnMessage message = new NoteOnMessage.Builder()
                        .setChannelNumber(parseMidiChannel(channelParam))
                        .setNoteNumber(noteNumber)
                        .build();
                try {
                    mMessageSender.send(parseMidiPort(channelParam), message);
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
                Integer channelParam = parseInteger(request, "channel");

                Integer noteNumber = NoteNameTable.nameToNumber(note);
                if (noteNumber == null) {
                    MessageUtils.setInvalidRequestParameterError(response,
                            note + " is not supported as note name");
                    return true;
                }

                NoteOffMessage message = new NoteOffMessage.Builder()
                        .setChannelNumber(parseMidiChannel(channelParam))
                        .setNoteNumber(noteNumber)
                        .build();
                try {
                    mMessageSender.send(parseMidiPort(channelParam), message);
                    setResult(response, DConnectMessage.RESULT_OK);
                } catch (IOException e) {
                    MessageUtils.setUnknownError(response, "Failed to send MIDI message: " + e.getMessage());
                }
                return true;
            }
        });

    }

    private int parseMidiPort(final Integer channelParam) {
        if (channelParam == null) {
            return 0;
        }
        return channelParam / MidiMessage.CHANNEL_MAX_COUNT;
    }

    private int parseMidiChannel(final Integer channelParam) {
        if (channelParam == null) {
            return 0;
        }
        return channelParam % MidiMessage.CHANNEL_MAX_COUNT;
    }

    @Override
    public String getProfileName() {
        return "soundModule";
    }
}