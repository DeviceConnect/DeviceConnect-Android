/*
 AudioCapturerExternalResource.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.webrtc.core;

import android.util.Log;

import org.deviceconnect.android.deviceplugin.webrtc.BuildConfig;
import org.deviceconnect.android.deviceplugin.webrtc.util.Resampler;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.webrtc.voiceengine.WebRtcAudioRecordModule;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Inputs the external resource to audio on WebRTC.
 *
 * @author NTT DOCOMO, INC.
 */
public class AudioCapturerExternalResource extends WebRtcAudioRecordModule {

    /**
     * Tag for debugging.
     */
    private static final String TAG = "ACER";

    private static final int BITS_PER_SAMPLE = 16;
    private static final int CALLBACK_BUFFER_SIZE_MS = 10;
    private static final int BUFFERS_PER_SECOND = 1000 / CALLBACK_BUFFER_SIZE_MS;

    /**
     * Defines the maximum number of retries .
     */
    private static final int RETRY_COUNT = 10;

    /**
     * Defines the time of retry.
     */
    private static final int RETRY_TIME = 10 * 1000;

    /**
     * Defines the base time of retry.
     */
    private static final int RETRY_FIRST_TIME = 5 * 1000;

    /**
     * Count of retry.
     */
    private int mRetryCount;

    /**
     * Uri of the server.
     */
    private String mUri;

    /**
     * Sample rate.
     */
    private PeerOption.AudioSampleRate mSampleRate = PeerOption.AudioSampleRate.RATE_48000;

    /**
     * Bit depth.
     */
    private PeerOption.AudioBitDepth mBitDepth = PeerOption.AudioBitDepth.PCM_FLOAT;

    /**
     * Channel.
     */
    private PeerOption.AudioChannel mChannel = PeerOption.AudioChannel.MONAURAL;

    /**
     * Client gets the audio data from server.
     */
    private AudioWebSocketClient mWebSocketClient;

    /**
     * ByteBuffer for passing data to WebRTC(JNI).
     */
    private ByteBuffer mByteBuffer;

    /**
     * Thread for receiving data from the server.
     */
    private AudioThread mAudioThread;

    /**
     * Defines the state of audio input.
     */
    private enum Status {
        OPEN,
        CLOSE,
    }

    /**
     * Current state of audio input.
     */
    private Status mStatus = Status.CLOSE;

    /**
     * Sets a uri of the server.
     * @param uri uri
     */
    public void setUri(final String uri) {
        mUri = uri;
    }

    /**
     * Sets a sample rate.
     * @param sampleRate Sample rate.
     */
    public void setSampleRate(final PeerOption.AudioSampleRate sampleRate) {
        mSampleRate = sampleRate;
    }

    /**
     * Sets a bit depth.
     * @param bitDepth Bit depth.
     */
    public void setBitDepth(final PeerOption.AudioBitDepth bitDepth) {
        mBitDepth = bitDepth;
    }

    /**
     * Sets a channel.
     * @param channel Channel.
     */
    public void setChannel(final PeerOption.AudioChannel channel) {
        mChannel = channel;
    }

    @Override
    public int initRecording(final int sampleRate, final int channels) {
        if (mUri == null) {
            return -1;
        }

        final int bytesPerFrame = channels * (BITS_PER_SAMPLE / 8);
        final int framesPerBuffer = sampleRate / BUFFERS_PER_SECOND;
        mByteBuffer = ByteBuffer.allocateDirect(bytesPerFrame * framesPerBuffer);
        cacheDirectBufferAddress(mByteBuffer);
        return framesPerBuffer;
    }

    @Override
    public synchronized boolean startRecording() {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "@@@ startRecording");
        }

        mStatus = Status.OPEN;

        connectWebSocket();

        mAudioThread = new AudioThread();
        mAudioThread.start();
        return true;
    }

    @Override
    public synchronized boolean stopRecording() {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "@@@ stopRecording");
        }

        mStatus = Status.CLOSE;

        if (mAudioThread != null) {
            mAudioThread.joinThread();
            mAudioThread = null;
        }

        if (mWebSocketClient != null) {
            mWebSocketClient.close();
            mWebSocketClient = null;
        }
        return true;
    }

    @Override
    public boolean enableBuiltInAEC(final boolean b) {
        // not support.
        return false;
    }

    @Override
    public boolean enableBuiltInAGC(boolean b) {
        return false;
    }

    @Override
    public boolean enableBuiltInNS(boolean b) {
        return false;
    }

    /**
     * Connect to the WebSocket server.
     */
    private synchronized void connectWebSocket() {
        if (mStatus != Status.CLOSE) {
            mWebSocketClient = new AudioWebSocketClient(URI.create(mUri));
            mWebSocketClient.connect();
        }
    }

    Resampler resampler = new Resampler();
    /**
     * Received an audio data from WebSocket server.
     * @param bytes audio data
     */
    private synchronized void receivedAudioData(final ByteBuffer bytes) {
        if (mAudioThread == null) {
            // audio record is already finished.
            return;
        }

        try {
            int capacity;
            short[] shortPCM;
            switch (mBitDepth) {
                case PCM_8BIT:
                    capacity = bytes.capacity();
                    shortPCM = new short[capacity];
                    for (int i = 0, count = 0; i < capacity; i++) {
                        switch (mChannel) {
                            case STEREO:
                                if (i % 2 == 0) {
                                    shortPCM[count++] = (short) bytes.get(i);
                                }
                                break;
                            case MONAURAL:
                            default:
                                shortPCM[i] = (short) bytes.get(i);
                                break;
                        }
                    }
                    break;
                case PCM_16BIT:
                    capacity = bytes.capacity() / 2;
                    switch (mChannel) {
                        case STEREO:
                            shortPCM = new short[capacity/2];
                            short[] stereoPCM = new short[capacity];
                            bytes.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(stereoPCM);
                            for (int i = 0, count = 0; i < capacity; i++) {
                                if (i % 2 == 0) {
                                    shortPCM[count++] = stereoPCM[i];
                                }
                            }
                            break;
                        case MONAURAL:
                        default:
                            shortPCM = new short[capacity];
                            bytes.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortPCM);
                            break;
                    }
                    break;
                case PCM_FLOAT:
                default:
                    // convert from float to short
                    capacity = bytes.capacity() / 4;
                    float[] floatPCM = new float[capacity];
                    shortPCM = new short[capacity];
                    bytes.order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().get(floatPCM);
                    for (int i = 0, count = 0; i < floatPCM.length; i++) {
                        switch (mChannel) {
                            case STEREO:
                                if (i % 2 == 0) {
                                    shortPCM[count++] = (short) (floatPCM[i] * 32768);
                                }
                                break;
                            case MONAURAL:
                            default:
                                shortPCM[i] = (short) (floatPCM[i] * 32768);
                                break;
                        }
                    }
                    break;
            }
            if (mBitDepth == PeerOption.AudioBitDepth.PCM_16BIT
                    && mChannel == PeerOption.AudioChannel.MONAURAL
                    && mSampleRate == PeerOption.AudioSampleRate.RATE_48000) {
                mAudioThread.offerAudioData(shortToByte(shortPCM));
            } else {
                mAudioThread.offerAudioData(resampler.reSample(shortToByte(shortPCM), 16, mSampleRate.getSampleRate(), 48000));
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "", e);
            }
        }
    }

    /**
     * Converts from an array of short to an array of byte.
     * @param shortArray array that be converted
     * @return array of byte
     */
    private static byte[] shortToByte(final short[] shortArray) {
        byte[] byteOut = new byte[shortArray.length * 2];
        ByteBuffer.wrap(byteOut).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(shortArray);
        return byteOut;
    }

    /**
     * Thread for receiving data from the server.
     */
    private class AudioThread extends Thread {
        /**
         * Queues for storing data that received from the server.
         */
        private final BlockingQueue<byte[]> mQueue = new ArrayBlockingQueue<>(8);

        /**
         * Keep alive flag.
         */
        private volatile boolean mKeepAlive = true;

        @Override
        public void run() {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "@@@@@@ AudioThread is started.");
            }

            try {
                while (mKeepAlive) {
                    sendAudioData(mQueue.take());
                }
            } catch (InterruptedException e) {
                // do nothing.
            }

            if (BuildConfig.DEBUG) {
                Log.i(TAG, "@@@@@@ AudioThread is stopped.");
            }
        }

        /**
         * Sends the audio data to WebRTC.
         * @param data audio data
         */
        private void sendAudioData(final byte[] data) {
            int p = 0;
            int l = data.length;
            while (p < data.length) {
                int pos = mByteBuffer.position();
                int capacity = mByteBuffer.capacity();
                int len;
                if (pos + l > capacity) {
                    len = capacity - pos;
                } else {
                    len = l;
                }
                mByteBuffer.put(data, p, len);

                p += len;
                l -= len;

                if (mByteBuffer.position() == mByteBuffer.capacity()) {
                    dataIsRecorded(mByteBuffer.capacity());
                    mByteBuffer.rewind();
                }
            }
        }

        /**
         * Inserts the audio data into the queue.
         * @param buf audio data
         */
        public void offerAudioData(final byte[] buf) {
            mQueue.offer(buf);
        }

        /**
         * Wait until this thread is finished.
         */
        public void joinThread() {
            mKeepAlive = false;
            interrupt();
            while (isAlive()) {
                try {
                    join();
                } catch (InterruptedException e) {
                    // do nothing.
                }
            }
        }
    }

    /**
     * This class communicate with the server.
     */
    private class AudioWebSocketClient extends WebSocketClient {

        /**
         * Constructor.
         * @param serverURI uri of the server
         */
        public AudioWebSocketClient(final URI serverURI) {
            super(serverURI);
        }

        @Override
        public void onOpen(final ServerHandshake handshakedata) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "@@@ onOpen");
            }
            mRetryCount = 0;
        }

        @Override
        public void onMessage(final String message) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "@@@ onMessage: this function is not call.");
            }
        }

        @Override
        public void onMessage(final ByteBuffer bytes) {
            receivedAudioData(bytes);
        }

        @Override
        public void onClose(final int code, final String reason, final boolean remote) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "@@@ onClose: code[" + code + "] reason[" + reason + "] remote[" + remote + "]");
            }

            if (mStatus != Status.CLOSE) {
                reconnect();
            }
        }

        @Override
        public synchronized void onError(final Exception ex) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "@@@ onError", ex);
            }
        }

        /**
         * Retries to reconnect WebSocket.
         * <p>
         * If count of retries has exceeded the {@code RETRY_COUNT}, give up the reconnection.
         * </p>
         */
        private void reconnect() {
            int wait = RETRY_FIRST_TIME + RETRY_TIME * mRetryCount;

            mRetryCount++;
            if (mRetryCount <= RETRY_COUNT) {
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "@@@ reconnect: " + mRetryCount + " " + wait);
                }
                new Thread(new RetryRunnable(wait)).start();
            } else {
                if (BuildConfig.DEBUG) {
                    Log.w(TAG, "@@@ Give up the reconnection.");
                }
            }
        }
    }

    /**
     * Runnable for retry.
     */
    private class RetryRunnable implements Runnable {
        /**
         * Time that wait until the retry.
         */
        private long mWait;

        /**
         * Constructor.
         * @param wait wait time
         */
        public RetryRunnable(final long wait) {
            mWait = wait;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(mWait);
            } catch (InterruptedException e) {
                // do nothing.
            }

            connectWebSocket();
        }
    }
}
