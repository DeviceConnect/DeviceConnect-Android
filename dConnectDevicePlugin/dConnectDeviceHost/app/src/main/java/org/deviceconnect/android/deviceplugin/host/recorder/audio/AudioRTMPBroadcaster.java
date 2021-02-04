package org.deviceconnect.android.deviceplugin.host.recorder.audio;

import android.graphics.Canvas;
import android.graphics.Color;

import org.deviceconnect.android.deviceplugin.host.recorder.AbstractRTMPBroadcaster;
import org.deviceconnect.android.libmedia.streaming.video.CanvasVideoEncoder;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;

public class AudioRTMPBroadcaster extends AbstractRTMPBroadcaster {
    private final HostAudioRecorder mRecorder;
    public AudioRTMPBroadcaster(HostAudioRecorder recorder, String broadcastURI) {
        super(recorder, broadcastURI);
        mRecorder = recorder;
    }

    @Override
    protected VideoEncoder createVideoEncoder() {
        if (!mRecorder.hasVideo()) {
            return null;
        }

        return new CanvasVideoEncoder(mRecorder.getSurfaceDrawingThread()) {
            @Override
            public void draw(Canvas canvas, int width, int height) {
                canvas.drawColor(Color.BLACK);
            }
        };
    }
}
