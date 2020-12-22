package org.deviceconnect.android.deviceplugin.host.activity.camera;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.host.HostDevicePlugin;
import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.battery.HostBatteryManager;
import org.deviceconnect.android.deviceplugin.host.connection.HostConnectionManager;
import org.deviceconnect.android.deviceplugin.host.connection.HostTrafficMonitor;
import org.deviceconnect.android.deviceplugin.host.recorder.Broadcaster;
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
    private boolean mDrawFlag = false;
    private boolean mAdjustViewFlag = false;
    private boolean mMuted = false;

    private final EGLSurfaceDrawingThread.OnDrawingEventListener mOnDrawingEventListener = new EGLSurfaceDrawingThread.OnDrawingEventListener() {
        @Override
        public void onStarted() {
            mDrawFlag = false;
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
            if (!mDrawFlag) {
                mDrawFlag = true;

                runOnUiThread(() -> {
                    View view = getView();
                    if (view == null) {
                        return;
                    }

                    PreviewSurfaceView surfaceView = view.findViewById(R.id.fragment_host_camera_surface_view);
                    if (surfaceView != null) {
                        surfaceView.setVisibility(View.VISIBLE);
                    }
                });
            }
        }
    };

    private final HostConnectionManager.ConnectionEventListener mConnectionEventListener = new HostConnectionManager.ConnectionEventListener() {
        @Override
        public void onChangedNetwork() {
            CameraMainFragment.this.onChangeMobileNetwork();
        }

        @Override
        public void onChangedWifiStatus() {
        }

        @Override
        public void onChangedBluetoothStatus() {
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_host_camera_main, container, false);

        SurfaceView surfaceView = view.findViewById(R.id.preview_surface_view);
        surfaceView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                toggleAdjustView();
            }
            return true;
        });
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

        view.findViewById(R.id.fragment_host_camera_rotation_button).setOnClickListener(
                v -> {
                    toggleScreenRotation();
                    setDisplayRotationButton();
                });

        view.findViewById(R.id.fragment_host_camera_mute_button).setOnClickListener(v -> toggleMute());
        view.findViewById(R.id.fragment_host_camera_switch_button).setOnClickListener(v -> switchRecorder());

        view.findViewById(R.id.fragment_host_camera_toggle_button).setOnClickListener(
                v -> {
                    if (mMediaRecorder != null) {
                        new Thread(this::togglePreviewServer).start();
                    }
                });

        view.findViewById(R.id.fragment_host_camera_settings_button).setOnClickListener(v -> gotoCameraSettings());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshUI();
        startTimer();
    }

    @Override
    public void onStart() {
        super.onStart();

        CameraActivity a = (CameraActivity) getActivity();
        if (a != null) {
            a.hideSystemUI();
        }
    }

    @Override
    public void onPause() {
        stopTimer();
        stopEGLSurfaceDrawingThread();
        super.onPause();
    }

    @Override
    public void onBindService() {
        if (mMediaRecorder != null) {
            mMediaRecorder.onConfigChange();
            startEGLSurfaceDrawingThread();
        } else {
            mMediaRecorderManager = getHostDevicePlugin().getHostMediaRecorderManager();
            mHostConnectionManager = getHostDevicePlugin().getHostConnectionManager();
            mHostConnectionManager.addHostConnectionEventListener(mConnectionEventListener);
            setRecorder(getRecorderId());
        }

        onChangeMobileNetwork();
    }

    @Override
    public void onUnbindService() {
        if (mHostConnectionManager != null) {
            mHostConnectionManager.removeHostConnectionEventListener(mConnectionEventListener);
        }
        stopEGLSurfaceDrawingThread();
    }

    private void gotoCameraSettings() {
        Bundle bundle = new Bundle();
        if (mRecorderId != null) {
            bundle.putString("recorder_id", mRecorderId);
        }
        findNavController(this).navigate(R.id.action_main_to_settings, bundle);
    }

    private void switchRecorder() {
        HostMediaRecorder[] recorders = mMediaRecorderManager.getRecorders();
        do {
            mIndex = (mIndex + 1) % recorders.length;
        } while (!(recorders[mIndex] instanceof Camera2Recorder));
        setRecorder(recorders[mIndex].getId());
    }

    private void setRecorder(String recorderId) {
        stopEGLSurfaceDrawingThread();

        mMediaRecorder = mMediaRecorderManager.getRecorder(recorderId);
        mRecorderId = mMediaRecorder.getId();

        // 古い端末では、カメラを停止した後に直ぐに開始すると起動できないことがあります。
        // ここでは、停止から少しだけ開始を送らせておきます。
        postDelay(() -> {
            startEGLSurfaceDrawingThread();
            setCameraStartButton();
        }, 100);
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
                int batteryLevel = battery.getBatteryLevel();

                runOnUiThread(() -> {
                    View view = getView();
                    if (view != null) {
                        TextView l = view.findViewById(R.id.fragment_host_camera_battery);
                        if (l != null) {
                            l.setText(batteryLevel + "%");
                        }

                        TextView t = view.findViewById(R.id.fragment_host_camera_temperature);
                        if (t != null) {
                            t.setText(temperature + "℃");
                        }

                        TextView b = view.findViewById(R.id.fragment_host_camera_bitrate);
                        if (b != null) {
                            b.setText(String.valueOf(bitrateTx));
                        }

                        View a = view.findViewById(R.id.fragment_host_camera_parameter);
                        if (a != null) {
                            a.setVisibility(View.VISIBLE);
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
                TextView t = view.findViewById(R.id.fragment_host_camera_network_type);
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

        runOnUiThread(() -> {
            View view = getView();
            if (view == null) {
                return;
            }

            PreviewSurfaceView surfaceView = view.findViewById(R.id.fragment_host_camera_surface_view);
            if (surfaceView != null) {
                surfaceView.setVisibility(View.INVISIBLE);
            }

            View a = view.findViewById(R.id.fragment_host_camera_parameter);
            if (a != null) {
                a.setVisibility(View.INVISIBLE);
            }
        });
    }

    /**
     * Surface を EGLSurfaceDrawingThread に追加します.
     *
     * @param surface 追加する Surface
     */
    private void addSurface(Surface surface) {
        if (mEGLSurfaceDrawingThread.findEGLSurfaceBaseByTag(surface) != null) {
            return;
        }
        mEGLSurfaceDrawingThread.addEGLSurfaceBase(surface);

        setCameraSurfaceView();
    }

    private void togglePreviewServer() {
        HostMediaRecorder.Settings settings = mMediaRecorder.getSettings();
        if (settings.isBroadcastEnabled()) {
            BroadcasterProvider provider = mMediaRecorder.getBroadcasterProvider();
            if (provider.isRunning()) {
                provider.stopBroadcaster();
            } else {
                String uri = settings.getBroadcastURI();
                uri = "rtmp://192.168.11.7:1935/live/abc";
                Broadcaster broadcaster = provider.startBroadcaster(uri);
                if (broadcaster == null) {
                    Log.e("ABC", "############## ERROR");
                } else {
                    provider.setMute(mMuted);
                }
            }
        } else {
            PreviewServerProvider provider = mMediaRecorder.getServerProvider();
            if (provider.isRunning()) {
                provider.stopServers();
            } else {
                List<PreviewServer> servers = provider.startServers();
                if (servers.isEmpty()) {
                    // TODO: 起動できなかった場合の処理
                } else {
                    provider.setMute(mMuted);
                }
            }
        }
        setCameraStartButton();
    }

    private void toggleAdjustView() {
        mAdjustViewFlag = !mAdjustViewFlag;
        setCameraSurfaceView();
    }

    private void toggleMute() {
        mMuted = !mMuted;

        if (mMediaRecorder != null) {
            BroadcasterProvider broadcasterProvider = mMediaRecorder.getBroadcasterProvider();
            broadcasterProvider.setMute(mMuted);

            PreviewServerProvider previewServerProvider = mMediaRecorder.getServerProvider();
            previewServerProvider.setMute(mMuted);
        }

        setMuteButton();
    }

    private void refreshUI() {
        setDisplayRotationButton();
        setMuteButton();
        setCameraStartButton();
    }

    private void setCameraSurfaceView() {
        runOnUiThread(() -> {
            View view = getView();
            if (view == null) {
                return;
            }

            PreviewSurfaceView surfaceView = view.findViewById(R.id.fragment_host_camera_surface_view);
            if (surfaceView != null && mEGLSurfaceDrawingThread != null) {
                if (mAdjustViewFlag) {
                    surfaceView.adjustSurfaceView(mEGLSurfaceDrawingThread.isSwappedDimensions(),
                            mMediaRecorder.getSettings().getPreviewSize());
                } else {
                    surfaceView.fullSurfaceView(mEGLSurfaceDrawingThread.isSwappedDimensions(),
                            mMediaRecorder.getSettings().getPreviewSize());
                }
            }
        });
    }

    private void setDisplayRotationButton() {
        runOnUiThread(() -> {
            View v = getView();
            if (v == null) {
                return;
            }

            ImageButton button = v.findViewById(R.id.fragment_host_camera_rotation_button);
            if (button != null) {
                if (isScreenRotationFixed()) {
                    button.setImageResource(R.drawable.ic_baseline_sync_disabled_24);
                } else {
                    button.setImageResource(R.drawable.ic_baseline_sync_24);
                }
            }
        });
    }

    private void setMuteButton() {
        runOnUiThread(() -> {
            View v = getView();
            if (v == null || mMediaRecorder == null) {
                return;
            }

            ImageButton button = v.findViewById(R.id.fragment_host_camera_mute_button);
            if (button != null) {
                HostMediaRecorder.Settings settings = mMediaRecorder.getSettings();
                if (mMuted || !settings.isAudioEnabled()) {
                    button.setImageResource(R.drawable.ic_baseline_mic_off_24);
                } else {
                    button.setImageResource(R.drawable.ic_baseline_mic_24);
                }
            }
        });
    }

    private void setCameraStartButton() {
        runOnUiThread(() -> {
            View v = getView();
            if (v == null || mMediaRecorder == null) {
                return;
            }

            ImageButton button = v.findViewById(R.id.fragment_host_camera_toggle_button);
            if (button != null) {
                boolean running;
                HostMediaRecorder.Settings settings = mMediaRecorder.getSettings();
                if (settings.isBroadcastEnabled()) {
                    running = mMediaRecorder.getBroadcasterProvider().isRunning();
                } else {
                    running = mMediaRecorder.getServerProvider().isRunning();
                }

                if (running) {
                    button.setImageResource(R.drawable.ic_baseline_stop_24);
                } else {
                    button.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                }
            }
        });
    }
}
