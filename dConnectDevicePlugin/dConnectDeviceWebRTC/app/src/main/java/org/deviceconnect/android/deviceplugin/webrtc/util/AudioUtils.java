/*
 AudioUtils.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.webrtc.util;

import android.util.Log;

import org.deviceconnect.android.deviceplugin.webrtc.BuildConfig;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author NTT DOCOMO, INC.
 */
public final class AudioUtils {
    /**
     * Tag for debugging.
     */
    private static final String TAG = "AUDIO";

    /**
     * Defined the monaural.
     */
    public static final int MONO = 1;

    /**
     * Defined the stereo.
     */
    public static final int STEREO = 2;

    /**
     * Defined the 8bit depth.
     */
    public static final int BIT_DEPTH_8BYTE = 1;

    /**
     * Defined the 16bit depth.
     */
    public static final int BIT_DEPTH_16SHORT = 2;

    /**
     * Defined the 32bit depth.
     */
    public static final int BIT_DEPTH_32FLOAT = 3;

    /**
     * Defined the sample rate.
     */
    private static final int[] SAMPLE_RATE = {
            48000,
            44100,
            32000,
            22050
    };

    /**
     * Defined the channels.
     */
    private static final int[] CHANNELS = {
            MONO, STEREO
    };

    private static final int[] BIT_DEPTH = {
            BIT_DEPTH_8BYTE,
            BIT_DEPTH_16SHORT,
            BIT_DEPTH_32FLOAT
    };

    /**
     * Convert from array of byte to array of short.
     * @param byteArray byte array
     * @return short array
     */
    public static short[] byteToShort(final byte[] byteArray) {
        short[] shortOut = new short[byteArray.length / 2];
        ByteBuffer byteBuffer = ByteBuffer.wrap(byteArray);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortOut);
        return shortOut;
    }

    /**
     * Convert from array of short to array of float.
     * @param shortArray short array
     * @return float array
     */
    public static float[] shortToFloat(final short[] shortArray) {
        float[] floatOut = new float[shortArray.length];
        for (int i = 0; i < shortArray.length; i++) {
            floatOut[i] = shortArray[i];
        }
        return floatOut;
    }

    /**
     * Convert from singed 16-bit PCM to 32-bit float PCM.
     * @param byteArray byte array
     * @return byte array
     */
    public static byte[] convert16BitTo32Bit(final byte[] byteArray) {
        float[] audioDataF = shortToFloat(byteToShort(byteArray));
        for (int i = 0; i < audioDataF.length; i++) {
            audioDataF[i] /= 32768.0;
        }

        FloatBuffer fb = FloatBuffer.wrap(audioDataF);
        ByteBuffer byteBuffer = ByteBuffer.allocate(fb.capacity() * 4);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.asFloatBuffer().put(fb);
        return byteBuffer.array();
    }
    
    /**
     * Gets the list of AudioFormat.
     * @return list of AudioFormat
     */
    public static List<AudioFormat> getSupportedFormats() {
        List<AudioFormat> list = new ArrayList<>();
        for (int channel : CHANNELS) {
            for (int rate : SAMPLE_RATE) {
                AudioFormat format = new AudioFormat(rate, channel, BIT_DEPTH_32FLOAT, false);
                list.add(format);
            }
        }
        return list;
    }

    /**
     * Gets the default AudioFormat.
     * @return AudioFormat
     */
    public static AudioFormat getDefaultFormat() {
        return new AudioFormat(SAMPLE_RATE[0], CHANNELS[0], BIT_DEPTH_32FLOAT, false);
    }

    /**
     * Converts the AudioFormat to String.
     * @param format AudioFormat
     * @return String
     */
    public static String formatToText(final AudioFormat format) {
        StringBuilder builder = new StringBuilder();
        builder.append("SampleRate:[" + format.getSampleRate() + "]");
        builder.append(",");
        builder.append("Channels:[" + format.getChannels() + "]");
        builder.append(",");
        builder.append("BitDepth:[" + format.getBitDepth() + "]");
        builder.append(",");
        builder.append("Processing:[" + format.isNoAudioProcessing() + "]");
        return builder.toString();
    }

    /**
     * Converts the String to AudioFormat.
     * @param text String
     * @return AudioFormat
     */
    public static AudioFormat textToFormat(final String text) {
        if (text == null || text.length() == 0) {
            return null;
        }

        String[] txt = text.split(",");
        if (txt.length != 4) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "text format is invalid.");
            }
            return null;
        }

        try {
            String sampleRateStr = txt[0].substring("SampleRate:[".length(), txt[0].length() - 1);
            String channelsStr = txt[1].substring("Channels:[".length(), txt[1].length() - 1);
            String depthStr = txt[2].substring("BitDepth:[".length(), txt[2].length() - 1);
            String noAudioProcessingStr = txt[3].substring("Processing:[".length(), txt[3].length() - 1);

            int sampleRate = Integer.parseInt(sampleRateStr);
            int channels = Integer.parseInt(channelsStr);
            int depth = Integer.parseInt(depthStr);
            boolean noAudioProcessing = noAudioProcessingStr.equals("true");

            return new AudioFormat(sampleRate, channels, depth, noAudioProcessing);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "", e);
            }
            return null;
        }
    }

    /**
     * AudioFormat.
     *
     * @author NTT DOCOMO, INC.
     */
    public static class AudioFormat {
        /**
         * The Sample Rate.
         */
        private int mSampleRate;

        /**
         * The number of channels.
         */
        private int mChannels;

        /**
         * The bit depth.
         */
        private int mBitDepth;

        /**
         * The Audio processing flag.
         */
        private boolean mNoAudioProcessing;

        public AudioFormat(final int sampleRate, final int channels, final int bitDepth, final boolean noAudioProcessing) {
            mSampleRate = sampleRate;
            mChannels = channels;
            mBitDepth = bitDepth;
            mNoAudioProcessing = noAudioProcessing;
        }

        /**
         * Gets the sample rate.
         * @return sample rate
         */
        public int getSampleRate() {
            return mSampleRate;
        }

        /**
         * Gets the number of channels.
         * @return the number of channels
         */
        public int getChannels() {
            return mChannels;
        }

        /**
         * Gets the bit depth.
         * @return the bit depth.
         */
        public int getBitDepth() {
            return mBitDepth;
        }

        public boolean isNoAudioProcessing() {
            return mNoAudioProcessing;
        }

        public void setNoAudioProcessing(boolean noAudioProcessing) {
            mNoAudioProcessing = noAudioProcessing;
        }

        @Override
        public String toString() {
            return formatToText(this);
        }
    }
}
