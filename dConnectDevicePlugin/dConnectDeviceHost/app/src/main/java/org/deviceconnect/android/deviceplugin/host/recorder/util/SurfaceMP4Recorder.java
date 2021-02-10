package org.deviceconnect.android.deviceplugin.host.recorder.util;

import android.media.MediaRecorder;
import android.util.Size;
import android.view.Surface;

import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceBase;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;

import java.io.File;
import java.io.IOException;

public class SurfaceMP4Recorder extends MP4Recorder {
    private final EGLSurfaceDrawingThread mEGLSurfaceDrawingThread;
    private final HostMediaRecorder.Settings mSettings;
    private Surface mSurface;

    private final EGLSurfaceDrawingThread.OnDrawingEventListener mOnDrawingEventListener = new EGLSurfaceDrawingThread.OnDrawingEventListener() {
        @Override
        public void onStarted() {
            if (mSurface != null) {
                if (mEGLSurfaceDrawingThread.findEGLSurfaceBaseByTag(mSurface) != null) {
                    return;
                }
                mEGLSurfaceDrawingThread.addEGLSurfaceBase(mSurface);
            }
        }

        @Override
        public void onStopped() {
        }

        @Override
        public void onError(Exception e) {
        }

        @Override
        public void onDrawn(EGLSurfaceBase eglSurfaceBase) {
            // ignore.
        }
    };

    public SurfaceMP4Recorder(File filePath, HostMediaRecorder.Settings settings, EGLSurfaceDrawingThread drawingThread) {
        super(filePath);
        mSettings = settings;
        mEGLSurfaceDrawingThread = drawingThread;
    }

    @Override
    public MediaRecorder setUpMediaRecorder(final File outputFile) throws IOException {
        boolean isSwap = mEGLSurfaceDrawingThread.isSwappedDimensions();
        Size previewSize = mSettings.getPreviewSize();
        int width = isSwap ? previewSize.getHeight() : previewSize.getWidth();
        int height = isSwap ? previewSize.getWidth() : previewSize.getHeight();

        // TODO 録画するときの設定を可変にすること。

        MediaRecorder mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setOutputFile(outputFile.getAbsolutePath());
        mediaRecorder.setVideoEncodingBitRate(10000000);
        mediaRecorder.setVideoFrameRate(30);
        mediaRecorder.setVideoSize(width, height);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.prepare();

        if (mEGLSurfaceDrawingThread != null) {
            mSurface = mediaRecorder.getSurface();
            mEGLSurfaceDrawingThread.addOnDrawingEventListener(mOnDrawingEventListener);
            mEGLSurfaceDrawingThread.start();
        }
        return mediaRecorder;
    }

    @Override
    public void tearDownMediaRecorder() {
        if (mEGLSurfaceDrawingThread != null) {
            mEGLSurfaceDrawingThread.removeEGLSurfaceBase(mSurface);
            mEGLSurfaceDrawingThread.stop(false);
            mEGLSurfaceDrawingThread.removeOnDrawingEventListener(mOnDrawingEventListener);
        }
    }
}
