/*
 MidiVolumeControllerProfile.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.midi.profiles;

import android.content.Intent;

import androidx.annotation.NonNull;

import org.deviceconnect.android.deviceplugin.midi.core.ControlChangeMessage;
import org.deviceconnect.android.deviceplugin.midi.core.MidiMessage;
import org.deviceconnect.android.profile.api.DeleteApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.api.PutApi;

import java.util.List;
import java.util.logging.Logger;

import static org.deviceconnect.android.deviceplugin.midi.BuildConfig.DEBUG;

/**
 * VolumeController プロファイルの実装.
 *
 * @author NTT DOCOMO, INC.
 */
public class MidiVolumeControllerProfile extends BaseMidiOutputProfile {

    private final Logger mLogger = Logger.getLogger("midi-plugin");

    public MidiVolumeControllerProfile() {

        // GET /gotapi/volumeController/onVolumeChange
        addApi(new GetApi() {
            @Override
            public String getAttribute() {
                return "onVolumeChange";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                return onEventCacheRequest(VolumeChangeEvent.class, response);
            }
        });

        // PUT /gotapi/volumeController/onVolumeChange
        addApi(new PutApi() {
            @Override
            public String getAttribute() {
                return "onVolumeChange";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                return onAddEventRequest(request, response);
            }
        });

        // DELETE /gotapi/volumeController/onVolumeChange
        addApi(new DeleteApi() {
            @Override
            public String getAttribute() {
                return "onVolumeChange";
            }

            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                return onRemoveEventRequest(request, response);
            }
        });

    }

    @Override
    public String getProfileName() {
        return "volumeController";
    }

    @Override
    void convertMessageToEvent(final int port, final @NonNull MidiMessage message, final long timestamp, final @NonNull List<MessageEvent> results) {
        if (message instanceof ControlChangeMessage) {
            if (DEBUG) {
                mLogger.info("convertMessageToEvent: ControlChange:"
                        + "channel=" + ((ControlChangeMessage) message).getChannelNumber()
                        + ", value=" + ((ControlChangeMessage) message).getControlValue());
            }

            final int channel = ((ControlChangeMessage) message).getChannelNumber() + port * MidiMessage.CHANNEL_MAX_COUNT;
            final int value = ((ControlChangeMessage) message).getControlValue();
            final double normalized = value / 127.0d;

            results.add(new VolumeChangeEvent(timestamp, channel, normalized));
        }
    }

    private class VolumeChangeEvent extends BaseMidiOutputProfile.MessageEvent {

        private final int mChannel;

        private final double mValue;

        VolumeChangeEvent(final long timestamp, final int channel, final double value) {
            super(timestamp);
            mChannel = channel;
            mValue = value;
        }

        @Override
        void putExtras(final Intent intent) {
            intent.putExtra("channel", mChannel);
            intent.putExtra("value", mValue);
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
            return "onVolumeChange";
        }
    }
}