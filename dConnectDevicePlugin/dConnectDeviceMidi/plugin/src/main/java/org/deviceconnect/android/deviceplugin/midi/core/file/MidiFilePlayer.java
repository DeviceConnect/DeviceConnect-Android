/*
 MidiFilePlayer.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.midi.core.file;

import android.media.midi.MidiInputPort;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;

import jp.kshoji.javax.sound.midi.InvalidMidiDataException;
import jp.kshoji.javax.sound.midi.MetaMessage;
import jp.kshoji.javax.sound.midi.MidiEvent;
import jp.kshoji.javax.sound.midi.MidiMessage;
import jp.kshoji.javax.sound.midi.Sequence;
import jp.kshoji.javax.sound.midi.Track;
import jp.kshoji.javax.sound.midi.io.StandardMidiFileReader;
import jp.kshoji.javax.sound.midi.spi.MidiFileReader;

/**
 * MIDI ファイルプレイヤー.
 *
 * NOTE: 再生処理については、以下の実装を参考にした。
 * https://github.com/kshoji/javax.sound.midi-for-Android/blob/develop/javax.sound.midi/src/jp/kshoji/javax/sound/midi/impl/SequencerImpl.java
 *
 * @author NTT DOCOMO, INC.
 */
public class MidiFilePlayer {

    private final MidiFileReader mMidiFileReader = new StandardMidiFileReader();

    private Sequence mSequence;

    private PlayerThread mPlayerThread;

    public synchronized void load(final InputStream inputStream) throws IOException {
        if (mPlayerThread != null) {
            throw new IllegalStateException("sequence is playing");
        }
        try {
            mSequence = mMidiFileReader.getSequence(inputStream);
        } catch (InvalidMidiDataException e) {
            throw new IOException(e);
        }
    }

    public boolean isStarted() {
        return mPlayerThread != null;
    }

    public synchronized void start(final MidiInputPort inputPort) {
        if (mSequence == null) {
            throw new IllegalStateException("sequence is not loaded");
        }
        if (mPlayerThread == null) {
            mPlayerThread = new PlayerThread(inputPort, mSequence);
            mPlayerThread.start();
        }
    }

    public synchronized void stop() {
        if (mPlayerThread != null) {
            mPlayerThread.interrupt();
            mPlayerThread = null;
        }
    }

    private static Track mergeTracks(@NonNull final Sequence sourceSequence) {
        final Track mergedTrack = new Track();
        for (Track track : sourceSequence.getTracks()) {
            for (int eventIndex = 0; eventIndex < track.size(); eventIndex++) {
                mergedTrack.add(track.get(eventIndex));
            }
        }
        Track.TrackUtils.sortEvents(mergedTrack);
        return mergedTrack;
    }

    private class PlayerThread extends Thread {

        private final MidiInputPort mMidiInputPort;

        private final Sequence mSequence;

        private final Track mPlayingTrack;

        private long mTickPosition;

        private float mTempoFactor = 1.0f;

        private float mTempoInBPM = 120.0f;

        PlayerThread(final MidiInputPort inputPort, final Sequence sequence) {
            mMidiInputPort = inputPort;
            mSequence = sequence;
            mPlayingTrack = mergeTracks(sequence);
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < mPlayingTrack.size(); i++) {
                    MidiEvent midiEvent = mPlayingTrack.get(i);
                    MidiMessage midiMessage = midiEvent.getMessage();

                    long sleepLength = (long) ((1.0f / getTicksPerMicrosecond()) * (midiEvent.getTick() - mTickPosition) / 1000f / getTempoFactor());
                    if (sleepLength > 0) {
                        sleep(sleepLength);
                    }
                    mTickPosition = midiEvent.getTick();

                    if (midiMessage instanceof MetaMessage) {
                        final MetaMessage metaMessage = (MetaMessage) midiMessage;
                        if (processTempoChange(metaMessage)) {

                            // do not send tempo message to the receivers.
                            continue;
                        }
                    }

                    send(midiMessage);
                }

                MidiFilePlayer.this.stop();
            } catch (InterruptedException e) {
                // ignore.
            } catch (IOException e) {
                // ignore.
            }
        }

        private void send(final MidiMessage midiMessage) throws IOException {
            byte[] msg = midiMessage.getMessage();
            mMidiInputPort.send(msg, 0, midiMessage.getLength());
        }

        private float getTicksPerMicrosecond() {
            final float ticksPerMicrosecond;
            if (mSequence.getDivisionType() == Sequence.PPQ) {
                // PPQ : tempoInBPM / 60f * resolution / 1000000 ticks per microsecond
                ticksPerMicrosecond = mTempoInBPM / 60.0f * mSequence.getResolution() / 1000000.0f;
            } else {
                // SMPTE : divisionType * resolution / 1000000 ticks per microsecond
                ticksPerMicrosecond = mSequence.getDivisionType() * mSequence.getResolution() / 1000000.0f;
            }
            return ticksPerMicrosecond;
        }

        private float getTempoFactor() {
            return mTempoFactor;
        }

        private void setTempoInMPQ(final float mpq) {
            mTempoInBPM = 60000000.0f / mpq;
        }

        private boolean processTempoChange(@NonNull final MetaMessage metaMessage) {
            if (metaMessage.getLength() == 6 && metaMessage.getStatus() == MetaMessage.META) {
                final byte[] message = metaMessage.getMessage();
                if (message != null && (message[1] & 0xff) == MetaMessage.TYPE_TEMPO && message[2] == 3) {
                    final int tempo = (message[5] & 0xff) | //
                            ((message[4] & 0xff) << 8) | //
                            ((message[3] & 0xff) << 16);

                    setTempoInMPQ(tempo);
                    return true;
                }
            }
            return false;
        }
    }
}
