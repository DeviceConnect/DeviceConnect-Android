package org.deviceconnect.android.deviceplugin.webrtc.core;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.webrtc.BuildConfig;
import org.deviceconnect.android.deviceplugin.webrtc.util.WebSocketServer;
import org.webrtc.voiceengine.WebRtcAudioTrackModule;

import java.nio.ByteBuffer;

import fi.iki.elonen.WebSocket;

public class AudioTrackExternal extends WebRtcAudioTrackModule {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "WebAudioTrack";
    private final AudioManager mAudioManager;

    // Default audio data format is PCM 16 bit per sample.
    // Guaranteed to be supported by all devices.
    private static final int BITS_PER_SAMPLE = 16;

    // Requested size of each recorded buffer provided to the client.
    private static final int CALLBACK_BUFFER_SIZE_MS = 10;

    // Average number of callbacks per second.
    private static final int BUFFERS_PER_SECOND = 1000 / CALLBACK_BUFFER_SIZE_MS;

    private ByteBuffer mByteBuffer;
    private AudioTrackThread mTrackThread;
    private WebSocketServer mWebSocketServer;

    public AudioTrackExternal(Context context, int port) {
        mAudioManager = (AudioManager) context.getSystemService(
                Context.AUDIO_SERVICE);

        WebSocketServer.Config.Builder builder = new WebSocketServer.Config.Builder();
        builder.port(port).isSsl(false)
                .documentRootPath(context.getFilesDir().getAbsolutePath());

        mWebSocketServer = new WebSocketServer(builder.build(), context);
        mWebSocketServer.setServerEventListener(new WebSocketServer.EventListener() {
            @Override
            public void onLaunched() {
                if (DEBUG) {
                    Log.i(TAG, "WebSocketServer#onLaunched");
                }
            }

            @Override
            public void onError(final Exception e) {
                if (DEBUG) {
                    Log.i(TAG, "WebSocketServer#onError ", e);
                }
                shutdownWebSocketServer();
            }

            @Override
            public void openSocket(final WebSocket socket) {
                if (DEBUG) {
                    Log.i(TAG, "WebSocketServer#openSocket " + socket);
                }
            }

            @Override
            public void closeSocket(final WebSocket socket) {
                if (DEBUG) {
                    Log.i(TAG, "WebSocketServer#closeSocket " + socket);
                }
            }
        });
        mWebSocketServer.start();
    }

    @Override
    public void initPlayout(int sampleRate, int channels) {
        if (DEBUG) {
            Log.i(TAG, "AudioTrackExternal#initPlayout " + sampleRate + ", " + channels);
        }

        int bytesPerFrame = channels * (BITS_PER_SAMPLE / 8);
        int bytesSize = bytesPerFrame * (sampleRate / BUFFERS_PER_SECOND);
        mByteBuffer = ByteBuffer.allocateDirect(bytesSize);
        cacheDirectBufferAddress(mByteBuffer);
    }

    @Override
    public boolean startPlayout() {
        if (DEBUG) {
            Log.i(TAG, "AudioTrackExternal#startPlayout ");
        }

        if (mTrackThread != null) {
            mTrackThread.joinThread();
            mTrackThread = null;
        }
        mTrackThread = new AudioTrackThread("Audio");
        mTrackThread.start();
        return true;
    }

    @Override
    public boolean stopPlayout() {
        if (DEBUG) {
            Log.i(TAG, "AudioTrackExternal#stopPlayout ");
        }
        if (mTrackThread != null) {
            mTrackThread.joinThread();
            mTrackThread = null;
        }
        shutdownWebSocketServer();
        return true;
    }

    @Override
    public int getStreamMaxVolume() {
        if (DEBUG) {
            Log.i(TAG, "AudioTrackExternal#getStreamMaxVolume ");
        }
        return mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
    }

    @Override
    public boolean setStreamVolume(int volume) {
        if (DEBUG) {
            Log.i(TAG, "AudioTrackExternal#setStreamVolume " + volume);
        }
        return true;
    }

    @Override
    public int getStreamVolume() {
        if (DEBUG) {
            Log.i(TAG, "AudioTrackExternal#getStreamVolume ");
        }
        return mAudioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
    }

    private void shutdownWebSocketServer() {
        if (mWebSocketServer != null) {
            mWebSocketServer.shutdown();
            mWebSocketServer = null;
        }
    }

    private class AudioTrackThread extends Thread {
        private volatile boolean keepAlive = true;

        public AudioTrackThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            android.os.Process.setThreadPriority(
                    android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

            long time;
            final int sizeInBytes = mByteBuffer.capacity();
            final byte[] buf = new byte[sizeInBytes];
            while (keepAlive) {
                time = System.currentTimeMillis();
                getPlayoutData(sizeInBytes);

                mByteBuffer.get(buf);
                mWebSocketServer.send(buf);
                mByteBuffer.rewind();

                long dt = System.currentTimeMillis() - time;
                if (dt < CALLBACK_BUFFER_SIZE_MS) {
                    try {
                        Thread.sleep(CALLBACK_BUFFER_SIZE_MS - dt - 1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void joinThread() {
            keepAlive = false;
            while (isAlive()) {
                try {
                    join();
                } catch (InterruptedException e) {
                    // Ignore.
                }
            }
        }
    }
}
