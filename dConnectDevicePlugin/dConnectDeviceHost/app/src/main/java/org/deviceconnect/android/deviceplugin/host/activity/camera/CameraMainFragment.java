package org.deviceconnect.android.deviceplugin.host.activity.camera;

import android.os.Bundle;
import android.util.Log;
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
import org.deviceconnect.android.deviceplugin.host.connection.HostTrafficMonitor;
import org.deviceconnect.android.deviceplugin.host.recorder.BroadcasterProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorderManager;
import org.deviceconnect.android.deviceplugin.host.recorder.PreviewServer;
import org.deviceconnect.android.deviceplugin.host.recorder.PreviewServerProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.camera.Camera2Recorder;
import org.deviceconnect.android.deviceplugin.host.recorder.ui.PreviewSurfaceView;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceBase;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;

import java.util.List;

import static androidx.navigation.fragment.NavHostFragment.findNavController;

public class CameraMainFragment extends CameraBaseFragment {
    private HostMediaRecorderManager mMediaRecorderManager;
    private HostConnectionManager mHostConnectionManager;
    private HostMediaRecorder mMediaRecorder;
    private EGLSurfaceDrawingThread mEGLSurfaceDrawingThread;
    private Surface mSurface;
    private String mRecorderId;
    private int mIndex;
    private HostTrafficMonitor mMonitor;

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
        public void onChangedNetwork() {
            Log.e("ABC", "onChangedMobileNetwork: ");
            CameraMainFragment.this.onChangeMobileNetwork();
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

        SurfaceView surfaceView = view.findViewById(R.id.preview_surface_view);
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

        view.findViewById(R.id.fragment_rotation_button).setOnClickListener(
                v -> {
                    toggleScreenRotation();
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
        setRecorder(getRecorderId());

        startTimer();

        onChangeMobileNetwork();
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
        if (mMonitor == null) {
            mMonitor = new HostTrafficMonitor(getContext(), INTERVAL_PERIOD);
            mMonitor.setOnTrafficListener((long rx, long bitrateRx, long tx, long bitrateTx) -> {
                HostDevicePlugin plugin = getHostDevicePlugin();
                if (plugin == null) {
                    return;
                }

                // バッテリー温度の設定
                HostBatteryManager battery = plugin.getHostBatteryManager();
                battery.getBatteryInfo();
                float temperature = battery.getTemperature();

                runOnUiThread(() -> {
                    View view = getView();
                    if (view != null) {
                        TextView t = view.findViewById(R.id.fragment_host_temperature);
                        if (t != null) {
                            t.setText(String.valueOf(temperature));
                        }

                        TextView b = view.findViewById(R.id.fragment_host_bitrate);
                        if (b != null) {
                            b.setText(String.valueOf(tx));
                        }
                    }
                });
            });
            mMonitor.startTimer();
        }
    }

    private synchronized void stopTimer() {
        if (mMonitor != null) {
            mMonitor.stopTimer();
            mMonitor = null;
        }
    }

    private void onChangeMobileNetwork() {
        runOnUiThread(() -> {
            View view = getView();
            if (view != null) {
                TextView t = view.findViewById(R.id.fragment_host_network_type);
                if (t != null) {
                    HostConnectionManager.NetworkType n = mHostConnectionManager.getActivityNetwork();
                    switch (n) {
                        case TYPE_MOBILE:
                            t.setText("MOBILE");
                            break;
                        case TYPE_WIFI:
                            t.setText("Wi-Fi");
                            break;
                        case TYPE_ETHERNET:
                            t.setText("Ethernet");
                            break;
                        case TYPE_BLUETOOTH:
                            t.setText("Bluetooth");
                            break;
                        case TYPE_LTE_CA:
                            t.setText("LTE CA");
                            break;
                        case TYPE_LTE_ADVANCED_PRO:
                            t.setText("LTE Advanced Pro（5Ge)");
                            break;
                        case TYPE_NR_NSA:
                            t.setText("5G Sub-6");
                            break;
                        case TYPE_NR_NSA_MMWAV:
                            t.setText("5G ミリ波");
                            break;
                        default:
                            t.setText("No connect");
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
        runOnUiThread(() -> {
            View view = getView();
            if (view != null) {
                PreviewSurfaceView surfaceView = view.findViewById(R.id.fragment_host_camera_surface_view);
                if (surfaceView != null) {
                    surfaceView.adjustSurfaceView(mEGLSurfaceDrawingThread.isSwappedDimensions(),
                            mMediaRecorder.getSettings().getPreviewSize());
                }
            }
        });
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

            for (PreviewServer s : servers) {
                s.unMute();
            }
        }
    }
}
