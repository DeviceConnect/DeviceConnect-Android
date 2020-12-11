package org.deviceconnect.android.deviceplugin.host.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceView;

import org.deviceconnect.android.deviceplugin.host.HostDevicePlugin;
import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorderManager;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceBase;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;

public class CameraActivity extends Activity {
    private HostDevicePlugin mHostDevicePlugin;
    private HostMediaRecorderManager mMediaRecorderManager;
    private HostMediaRecorder mMediaRecorder;
    private boolean mIsBound = false;

    private SurfaceView mSurfaceView;
    private EGLSurfaceDrawingThread mEGLSurfaceDrawingThread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        bindService();

        mSurfaceView = findViewById(R.id.surface_view);
    }

    @Override
    protected void onDestroy() {
        unbindService();
        super.onDestroy();
    }

    protected void onBindService() {
        mMediaRecorderManager = mHostDevicePlugin.getHostMediaRecorderManager();
        mMediaRecorder = mMediaRecorderManager.getRecorder(null);

        mEGLSurfaceDrawingThread = mMediaRecorder.getSurfaceDrawingThread();
        mEGLSurfaceDrawingThread.addOnDrawingEventListener(new EGLSurfaceDrawingThread.OnDrawingEventListener() {
            @Override
            public void onStarted() {
                EGLSurfaceBase eglSurfaceBase = mEGLSurfaceDrawingThread.createEGLSurfaceBase(mSurfaceView.getHolder().getSurface());
                eglSurfaceBase.setTag(mSurfaceView.getHolder().getSurface());
                mEGLSurfaceDrawingThread.addEGLSurfaceBase(eglSurfaceBase);
            }

            @Override
            public void onStopped() {

            }

            @Override
            public void onError(Exception e) {

            }

            @Override
            public void onDrawn(EGLSurfaceBase eglSurfaceBase) {

            }
        });
        mEGLSurfaceDrawingThread.start();
    }

    protected void onUnbindService() {
    }

    private void bindService() {
        Intent intent = new Intent(this, HostDevicePlugin.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    private void unbindService() {
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            mHostDevicePlugin = (HostDevicePlugin) ((HostDevicePlugin.LocalBinder) binder).getMessageService();
            onBindService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mHostDevicePlugin = null;
            onUnbindService();
        }
    };
}
