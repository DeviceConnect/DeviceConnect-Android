package org.deviceconnect.android.deviceplugin.host.activity.camera;

import android.os.Bundle;
import android.telephony.TelephonyDisplayInfo;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.host.HostDevicePlugin;
import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.battery.HostBatteryManager;
import org.deviceconnect.android.deviceplugin.host.connection.HostConnectionManager;
import org.deviceconnect.android.deviceplugin.host.recorder.BroadcasterProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorderManager;
import org.deviceconnect.android.deviceplugin.host.recorder.PreviewServer;
import org.deviceconnect.android.deviceplugin.host.recorder.PreviewServerProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.camera.Camera2Recorder;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceBase;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static androidx.navigation.fragment.NavHostFragment.findNavController;

public class CameraMainFragment extends CameraBaseFragment {
    private HostMediaRecorderManager mMediaRecorderManager;
    private HostConnectionManager mHostConnectionManager;
    private HostMediaRecorder mMediaRecorder;
    private EGLSurfaceDrawingThread mEGLSurfaceDrawingThread;
    private Surface mSurface;
    private String mRecorderId;
    private int mIndex;
    private Timer mTimer;

    private final EGLSurfaceDrawingThread.OnDrawingEventListener mOnDrawingEventListener = new EGLSurfaceDrawingThread.OnDrawingEventListener() {
        @Override
        public void onStarted() {
            if (mSurface != null) {
                addSurface(mSurface);
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

    private final HostConnectionManager.ConnectionEventListener mConnectionEventListener = new HostConnectionManager.ConnectionEventListener() {
        @Override
        public void onChangedMobileNetwork(int type) {
            Log.e("ABC", "onChangedMobileNetwork: " + type);
            CameraMainFragment.this.onChangeMobileNetwork(type);
        }

        @Override
        public void onChangedWifiStatus() {
            Log.e("ABC", "onChangedWifiStatus: ");
        }

        @Override
        public void onChangedBluetoothStatus() {
            Log.e("ABC", "onChangedBluetoothStatus: " + this);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_host_camera_main, container, false);

        SurfaceView surfaceView = view.findViewById(R.id.fragment_host_camera_surface_view);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mSurface = holder.getSurface();
                if (mEGLSurfaceDrawingThread != null && mEGLSurfaceDrawingThread.isRunning()) {
                    addSurface(mSurface);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (mSurface != null) {
                    if (mEGLSurfaceDrawingThread != null) {
                        mEGLSurfaceDrawingThread.removeEGLSurfaceBase(mSurface);
                    }
                    mSurface = null;
                }
            }
        });

        view.findViewById(R.id.fragment_camera_button).setOnClickListener(
                v -> {
                    HostMediaRecorder[] recorders = mMediaRecorderManager.getRecorders();
                    do {
                        mIndex = (mIndex + 1) % recorders.length;
                    } while (!(recorders[mIndex] instanceof Camera2Recorder));
                    setRecorder(recorders[mIndex].getId());
                });

        view.findViewById(R.id.fragment_start_button).setOnClickListener(
                v -> {
                    if (mMediaRecorder != null) {
                        new Thread(this::togglePreviewServer).start();
                    }
                });

        view.findViewById(R.id.fragment_settings_button).setOnClickListener(
                v -> {
                    Bundle bundle = new Bundle();
                    if (mRecorderId != null) {
                        bundle.putString("recorder_id", mRecorderId);
                    }
                    findNavController(this).navigate(R.id.action_main_to_settings, bundle);
                });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mMediaRecorder != null) {
            startEGLSurfaceDrawingThread();
        }
    }

    @Override
    public void onPause() {
        stopEGLSurfaceDrawingThread();
        super.onPause();
    }

    @Override
    public void onBindService() {
        mHostConnectionManager = getHostDevicePlugin().getHostConnectionManager();
        mHostConnectionManager.addHostConnectionEventListener(mConnectionEventListener);

        mMediaRecorderManager = getHostDevicePlugin().getHostMediaRecorderManager();
        setRecorder(null);

        startTimer();
    }

    @Override
    public void onUnbindService() {
        if (mHostConnectionManager != null) {
            mHostConnectionManager.removeHostConnectionEventListener(mConnectionEventListener);
        }
        stopEGLSurfaceDrawingThread();
        stopTimer();
    }

    private static final long INTERVAL_PERIOD = 30 * 1000;

    private synchronized void startTimer() {
        if (mTimer == null) {
            mTimer = new Timer();
            mTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    onSensor();
                }
            }, 0, INTERVAL_PERIOD);
        }
    }

    private synchronized void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    private void onSensor() {
        HostDevicePlugin plugin = getHostDevicePlugin();
        if (plugin == null) {
            return;
        }

        // バッテリー温度の設定
        HostBatteryManager battery = plugin.getHostBatteryManager();
        battery.getBatteryInfo();
        float temperature = battery.getTemperature();

        // ネットワークビットレート
        HostConnectionManager connection = plugin.getHostConnectionManager();
        long current = System.currentTimeMillis();
        HostConnectionManager.Stats stats = connection.getNetworkStats(current - INTERVAL_PERIOD, current);

        runOnUiThread(() -> {
            View view = getView();
            if (view != null) {
                TextView t = view.findViewById(R.id.fragment_host_temperature);
                if (t != null) {
                    t.setText(String.valueOf(temperature));
                }

                TextView b = view.findViewById(R.id.fragment_host_bitrate);
                if (b != null) {
                    b.setText(String.valueOf(stats.getTxBitRate()));
                }
            }
        });
    }

    private void onChangeMobileNetwork(int type) {
        runOnUiThread(() -> {
            View view = getView();
            if (view != null) {
                TextView t = view.findViewById(R.id.fragment_host_network_type);
                if (t != null) {
                    switch (type) {
                        case TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_LTE_CA:
                            t.setText("LTE CA");
                            break;
                        case TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_LTE_ADVANCED_PRO:
                            // LTE Advanced Pro（5Ge)
                            t.setText("LTE Advanced Pro（5Ge)");
                            break;
                        case TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA:
                            // 5G Sub-6
                            t.setText("5G Sub-6");
                            break;
                        case TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA_MMWAVE:
                            // 5G ミリ波
                            t.setText("5G ミリ波");
                            break;
                        default:
                            // 5G 以外
                            t.setText("No 5G");
                            break;
                    }
                }
            }
        });
    }

    /**
     * カメラの映像描画を開始します.
     */
    private void startEGLSurfaceDrawingThread() {
        mEGLSurfaceDrawingThread = mMediaRecorder.getSurfaceDrawingThread();
        mEGLSurfaceDrawingThread.addOnDrawingEventListener(mOnDrawingEventListener);
        mEGLSurfaceDrawingThread.start();
    }

    /**
     * カメラの映像描画を停止します.
     */
    private void stopEGLSurfaceDrawingThread() {
        if (mEGLSurfaceDrawingThread != null) {
            mEGLSurfaceDrawingThread.removeEGLSurfaceBase(mSurface);
            mEGLSurfaceDrawingThread.removeOnDrawingEventListener(mOnDrawingEventListener);
            mEGLSurfaceDrawingThread.stop(false);
            mEGLSurfaceDrawingThread = null;
        }
    }

    private void addSurface(Surface surface) {
        if (mEGLSurfaceDrawingThread.findEGLSurfaceBaseByTag(surface) != null) {
            return;
        }
        mEGLSurfaceDrawingThread.addEGLSurfaceBase(surface);
        runOnUiThread(() -> adjustSurfaceView(mEGLSurfaceDrawingThread.isSwappedDimensions()));
    }

    private void setRecorder(String recorderId) {
        stopEGLSurfaceDrawingThread();
        mRecorderId = recorderId;
        mMediaRecorder = mMediaRecorderManager.getRecorder(mRecorderId);
        startEGLSurfaceDrawingThread();
    }

    private void startBroadcaster() {
        BroadcasterProvider provider = mMediaRecorder.getBroadcasterProvider();
        provider.startBroadcaster("", new BroadcasterProvider.OnBroadcasterListener() {
            @Override
            public void onStarted() {
            }

            @Override
            public void onStopped() {
            }

            @Override
            public void onError(Exception e) {
            }
        });
    }

    private void stopBroadcaster() {
        BroadcasterProvider provider = mMediaRecorder.getBroadcasterProvider();
        provider.stopBroadcaster();
    }

    private void togglePreviewServer() {
        PreviewServerProvider provider = mMediaRecorder.getServerProvider();
        if (provider.isRunning()) {
            provider.stopServers();
        } else {
            List<PreviewServer> servers = provider.startServers();
            if (servers.isEmpty()) {
                // TODO: 起動できなかった場合の処理
            }
        }
    }

    /**
     * Surface のサイズを画面のサイズに合わせて調整します.
     *
     * @param isSwappedDimensions 縦横の切り替えフラグ
     */
    private void adjustSurfaceView(boolean isSwappedDimensions) {
        runOnUiThread(() -> {
            View root = getView();
            if (root == null) {
                return;
            }

            HostMediaRecorder.Settings settings = mMediaRecorder.getSettings();
            Size previewSize = settings.getPreviewSize();

            SurfaceView surfaceView = root.findViewById(R.id.fragment_host_camera_surface_view);
            int cameraWidth = isSwappedDimensions ? previewSize.getHeight() : previewSize.getWidth();
            int cameraHeight = isSwappedDimensions ? previewSize.getWidth() : previewSize.getHeight();
            Size viewSize = new Size(root.getWidth(), root.getHeight());
            Size changeSize = calculateViewSize(cameraWidth, cameraHeight, viewSize);

            ViewGroup.LayoutParams layoutParams = surfaceView.getLayoutParams();
            layoutParams.width = changeSize.getWidth();
            layoutParams.height = changeSize.getHeight();
            surfaceView.setLayoutParams(layoutParams);

            surfaceView.getHolder().setFixedSize(previewSize.getWidth(), previewSize.getHeight());
        });
    }

    /**
     * 指定された View のサイズにフィットするサイズを計算します.
     *
     * @param width 横幅
     * @param height 縦幅
     * @param viewSize View のサイズ
     * @return View にフィットするサイズ
     */
    private Size calculateViewSize(int width, int height, Size viewSize) {
        int h =  (int) (height * (viewSize.getWidth() / (float) width));
        if (viewSize.getHeight() < h) {
            int w = (int) (width * (viewSize.getHeight() / (float) height));
            if (w % 2 != 0) {
                w--;
            }
            return new Size(w, viewSize.getHeight());
        }
        return new Size(viewSize.getWidth(), h);
    }
}
