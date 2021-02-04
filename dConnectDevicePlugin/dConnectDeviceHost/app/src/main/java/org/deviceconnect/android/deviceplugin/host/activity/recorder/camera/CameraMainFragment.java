package org.deviceconnect.android.deviceplugin.host.activity.recorder.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

import androidx.databinding.DataBindingUtil;

import com.google.android.material.tabs.TabLayout;

import org.deviceconnect.android.deviceplugin.host.HostDevicePlugin;
import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.activity.fragment.HostDevicePluginBindFragment;
import org.deviceconnect.android.deviceplugin.host.activity.recorder.settings.SettingsActivity;
import org.deviceconnect.android.deviceplugin.host.battery.HostBatteryManager;
import org.deviceconnect.android.deviceplugin.host.connection.HostConnectionManager;
import org.deviceconnect.android.deviceplugin.host.connection.HostTraffic;
import org.deviceconnect.android.deviceplugin.host.databinding.FragmentHostCameraMainBinding;
import org.deviceconnect.android.deviceplugin.host.recorder.Broadcaster;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDevicePhotoRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceStreamRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorderManager;
import org.deviceconnect.android.deviceplugin.host.recorder.PreviewServer;
import org.deviceconnect.android.deviceplugin.host.recorder.camera.Camera2Recorder;
import org.deviceconnect.android.deviceplugin.host.recorder.ui.PreviewSurfaceView;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceBase;
import org.deviceconnect.android.libmedia.streaming.gles.EGLSurfaceDrawingThread;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.List;

public class CameraMainFragment extends HostDevicePluginBindFragment {
    private final CameraMainViewModel mViewModel = new CameraMainViewModel();

    private HostMediaRecorderManager mMediaRecorderManager;
    private HostConnectionManager mHostConnectionManager;
    private HostMediaRecorder mMediaRecorder;
    private EGLSurfaceDrawingThread mEGLSurfaceDrawingThread;
    private Surface mSurface;
    private String mRecorderId;
    private int mIndex = -1;
    private boolean mDrawFlag = false;
    private boolean mAdjustViewFlag = true;
    private boolean mMuted = false;
    private int mSelectedMode = 0;
    private String mPhotoUri;

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
                mViewModel.setSurfaceVisibility(View.VISIBLE);
            }
        }
    };

    private final HostMediaRecorderManager.OnEventListener mOnEventListener = new HostMediaRecorderManager.OnEventListener() {
        @Override
        public void onPreviewStarted(HostMediaRecorder recorder, List<PreviewServer> servers) {
            setPreviewButton();
        }

        @Override
        public void onPreviewStopped(HostMediaRecorder recorder) {
            setPreviewButton();
        }

        @Override
        public void onPreviewError(HostMediaRecorder recorder, Exception e) {
        }

        @Override
        public void onBroadcasterStarted(HostMediaRecorder recorder, Broadcaster broadcaster) {
            setBroadcastButton();
        }

        @Override
        public void onBroadcasterStopped(HostMediaRecorder recorder, Broadcaster broadcaster) {
            setBroadcastButton();
        }

        @Override
        public void onBroadcasterError(HostMediaRecorder recorder, Broadcaster broadcaster, Exception e) {
        }

        @Override
        public void onTakePhoto(HostMediaRecorder recorder, String uri, String filePath, String mimeType) {
            setPhoto(uri);
        }

        @Override
        public void onRecordingStarted(HostMediaRecorder recorder, String fileName) {
            setRecordingButton();
        }

        @Override
        public void onRecordingPause(HostMediaRecorder recorder) {
            setRecordingButton();
        }

        @Override
        public void onRecordingResume(HostMediaRecorder recorder) {
            setRecordingButton();
        }

        @Override
        public void onRecordingStopped(HostMediaRecorder recorder, String fileName) {
            setRecordingButton();
        }

        @Override
        public void onError(HostMediaRecorder recorder, Exception e) {
        }
    };

    private final HostConnectionManager.TrafficEventListener mTrafficEventListener = this::onChangeRecorderParam;

    public class Presenter {
        public void onClickToggleDisplayRotationButton() {
            toggleDisplayRotation();
        }
        public void onClickToggleMuteButton() {
            toggleMute();
        }
        public void onClickSettingButton() {
            gotoCameraSettings();
        }
        public void onClickSwitchCameraButton() {
            switchCameraRecorder();
        }
        public void onClickTogglePreviewButton() {
            togglePreviewServer();
        }
        public void onClickToggleBroadcastButton() {
            toggleBroadcaster();
        }
        public void onClickToggleRecordingButton() {
            toggleRecording();
        }
        public void onClickTakePhoto() {
            takePhoto();
        }
        public void onClickPhoto() {
            showPhoto();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentHostCameraMainBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_host_camera_main, container, false);
        binding.setViewModel(mViewModel);
        binding.setPresenter(new Presenter());

        View view = binding.getRoot();
        SurfaceView surfaceView = view.findViewById(R.id.preview_surface_view);
        surfaceView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (mMediaRecorder instanceof Camera2Recorder) {
                    Camera2Recorder r = (Camera2Recorder) mMediaRecorder;
                    r.getCameraWrapper().startFocus(event.getX(), event.getY(),
                            surfaceView.getWidth(), surfaceView.getHeight());
                }
            }
            return true;
        });
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mSurface = holder.getSurface();
                if (mEGLSurfaceDrawingThread != null && mEGLSurfaceDrawingThread.isInitCompleted()) {
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

        final int[] idList = {
                R.id.fragment_host_camera_toggle_preview_button,
                R.id.fragment_host_camera_toggle_broadcaster_button,
                R.id.fragment_host_camera_toggle_photo_button,
                R.id.fragment_host_camera_toggle_recording_button
        };

        TabLayout tabLayout = binding.fragmentHostCameraTabLayout;
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mSelectedMode = tab.getPosition();

                View v = view.findViewById(idList[mSelectedMode]);
                new ButtonFadeInTransition(v).start();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View v = view.findViewById(idList[tab.getPosition()]);
                new ButtonFadeOutTransition(v).start();
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        if (savedInstanceState != null) {
            mIndex = savedInstanceState.getInt("recorder_index");
            mSelectedMode = savedInstanceState.getInt("selected_mode");
            TabLayout.Tab tab = tabLayout.getTabAt(mSelectedMode);
            if (tab != null) {
                tab.select();
            }
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("recorder_index", mIndex);
        outState.putInt("selected_mode", mSelectedMode);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshUI();

        Context context = getContext();
        if (context != null && !HostConnectionManager.checkUsageAccessSettings(context)) {
            HostConnectionManager.openUsageAccessSettings(context);
        }

        CameraActivity a = (CameraActivity) getActivity();
        if (a != null) {
            a.hideSystemUI();
        }
    }

    @Override
    public void onPause() {
        stopEGLSurfaceDrawingThread();
        super.onPause();
    }

    @Override
    public void onBindService() {
        if (mMediaRecorder != null) {
            startEGLSurfaceDrawingThread();
        } else {
            mMediaRecorderManager = getHostDevicePlugin().getHostMediaRecorderManager();
            mMediaRecorderManager.addOnEventListener(mOnEventListener);

            mHostConnectionManager = getHostDevicePlugin().getHostConnectionManager();
            mHostConnectionManager.addTrafficEventListener(mTrafficEventListener);

            HostMediaRecorder[] recorders = mMediaRecorderManager.getRecorders();
            if (mIndex == -1) {
                // カメラが選択されていない場合には、使用されているカメラを選択
                mIndex = getUsedCamera(recorders);
                if (mIndex == -1) {
                    return;
                }
            }
            setRecorder(recorders[mIndex].getId());
        }

        if (mHostConnectionManager != null) {
            onChangeRecorderParam(mHostConnectionManager.getTrafficList());
        }
    }

    @Override
    public void onUnbindService() {
        if (mHostConnectionManager != null) {
            mHostConnectionManager.removeTrafficEventListener(mTrafficEventListener);
        }
        if (mMediaRecorderManager != null) {
            mMediaRecorderManager.removeOnEventListener(mOnEventListener);
        }
        stopEGLSurfaceDrawingThread();
    }

    /**
     * 既に使用されているカメラがあれば、そのレコーダを洗濯します.
     *
     * 複数のカメラを同時に使用できない端末があるために、使用されているカメラ
     * を画面に表示するようにします。
     *
     * @param recorders レコーダ一覧
     * @return 使用されているレコーダのインデックス
     */
    private int getUsedCamera(HostMediaRecorder[] recorders) {
        int index = -1;
        for (int i = 0; i < recorders.length; i++) {
            if (recorders[i] instanceof Camera2Recorder) {
                Camera2Recorder camera2Recorder = (Camera2Recorder) recorders[i];
                if (index == -1) {
                    index = i;
                }
                if (camera2Recorder.getCameraWrapper().isPreview()) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }

    /**
     * カメラ用レコーダの設定画面へ遷移します.
     */
    private void gotoCameraSettings() {
        Context context = getContext();
        if (context != null) {
            SettingsActivity.startActivity(context, mRecorderId, getDisplayOrientation());
        }
    }

    /**
     * カメラ用レコーダを切り替えます.
     */
    private void switchCameraRecorder() {
        if (mMediaRecorder instanceof Camera2Recorder) {
            // カメラが使用されている場合は切り替えられないようにしたい.
        }

        HostMediaRecorder[] recorders = mMediaRecorderManager.getRecorders();
        do {
            mIndex = (mIndex + 1) % recorders.length;
        } while (!(recorders[mIndex] instanceof Camera2Recorder));
        setRecorder(recorders[mIndex].getId());
    }

    /**
     * 指定された ID のレコーダに設定します.
     *
     * @param recorderId レコーダID
     */
    private void setRecorder(String recorderId) {
        stopEGLSurfaceDrawingThread();

        mMediaRecorder = mMediaRecorderManager.getRecorder(recorderId);
        mRecorderId = mMediaRecorder.getId();

        // 古い端末では、カメラを停止した後に直ぐに開始すると起動できないことがあります。
        // ここでは、停止から少しだけ開始を送らせておきます。
        postDelay(() -> {
            startEGLSurfaceDrawingThread();
            refreshUI();
        }, 100);
    }

    /**
     * 画面に表示するパラメータが変化した時に呼び出されます.
     *
     * @param trafficList 通信量
     */
    private void onChangeRecorderParam(List<HostTraffic> trafficList) {
        HostDevicePlugin plugin = getHostDevicePlugin();
        if (plugin == null) {
            return;
        }

        int bitrate = 0;
        if (trafficList != null) {
            for (HostTraffic t : trafficList) {
                bitrate += t.getBitrateTx();
            }
        }

        HostBatteryManager battery = plugin.getHostBatteryManager();
        battery.getBatteryInfo();
        float temperature = battery.getTemperature();
        mViewModel.setTemperature(temperature + "℃");
        mViewModel.setBitRate((bitrate / 1024) + "kbps");
        mViewModel.setParamVisibility(View.VISIBLE);
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
            mEGLSurfaceDrawingThread.stop(false);
            mEGLSurfaceDrawingThread.removeOnDrawingEventListener(mOnDrawingEventListener);
            mEGLSurfaceDrawingThread = null;
        }

        mViewModel.setSurfaceVisibility(View.INVISIBLE);
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

        adjustCameraSurfaceView();
    }

    /**
     * 写真を他のアプリで表示します.
     */
    private void showPhoto() {
        if (mPhotoUri != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("jpeg");
            intent.setDataAndType(Uri.parse(mPhotoUri), mimeType);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                startActivity(intent);
            } catch (Exception e) {
                // ignore.
            }
        }
    }

    /**
     * 撮影した写真を画面に表示します.
     *
     * @param uri 撮影した写真の URI
     */
    private void setPhoto(String uri) {
        new Thread(() -> {
            Context context = getContext();
            if (context == null) {
                return;
            }
            try (InputStream stream = context.getContentResolver().openInputStream(Uri.parse(uri))) {
                Bitmap bitmap = BitmapFactory.decodeStream(new BufferedInputStream(stream));
                if (bitmap == null) {
                    return;
                }
                runOnUiThread(() -> {
                    View root = getView();
                    if (root != null) {
                        mPhotoUri = uri;

                        ImageView iv = root.findViewById(R.id.fragment_host_camera_photo_image);
                        if (iv != null) {
                            iv.setImageBitmap(bitmap);
                            iv.setVisibility(View.VISIBLE);
                        }
                    }
                });
            } catch (Throwable e) {
                // ignore.
            }
        }).start();
    }

    /**
     * 写真を撮影します.
     *
     * すでに録画している場合などは、撮影が行えないので、処理を無視します。
     */
    private void takePhoto() {
        setTakePhotoButton(false);
        mMediaRecorder.takePhoto(new HostDevicePhotoRecorder.OnPhotoEventListener() {
            @Override
            public void onTakePhoto(String uri, String filePath, String mimeType) {
                setTakePhotoButton(true);
            }

            @Override
            public void onFailedTakePhoto(String errorMessage) {
                setTakePhotoButton(true);
                showToast(R.string.host_recorder_failed_to_take_photo);
            }
        });
    }

    private void setTakePhotoButton(boolean enable) {
        runOnUiThread(() -> {
            View root = getView();
            if (root != null) {
                View view = root.findViewById(R.id.fragment_host_camera_toggle_photo_button);
                if (view != null) {
                    view.setEnabled(enable);
                }
            }
        });
    }

    /**
     * 録画の開始・停止を切り替えます.
     */
    private void toggleRecording() {
        if (mMediaRecorder.getState() == HostMediaRecorder.State.RECORDING) {
            stopRecordingInternal();
        } else {
            if (mMediaRecorder.getSettings().getPreviewAudioSource() == HostMediaRecorder.AudioSource.APP) {
                mMediaRecorder.requestPermission(new HostMediaRecorder.PermissionCallback() {
                    @Override
                    public void onAllowed() {
                        startRecordingInternal();
                    }

                    @Override
                    public void onDisallowed() {
                        showToast(R.string.host_recorder_failed_to_start_recording);
                        setRecordingButton();
                    }
                });
            } else {
                startRecordingInternal();
            }
        }
    }

    private void stopRecordingInternal() {
        mMediaRecorder.stopRecording(new HostDeviceStreamRecorder.StoppingCallback() {
            @Override
            public void onStopped(HostDeviceStreamRecorder recorder, String fileName) {
                setRecordingButton();
            }

            @Override
            public void onFailed(HostDeviceStreamRecorder recorder, String errorMessage) {
                setRecordingButton();
            }
        });
    }

    private void startRecordingInternal() {
        mMediaRecorder.startRecording(new HostDeviceStreamRecorder.RecordingCallback() {
            @Override
            public void onRecorded(HostDeviceStreamRecorder recorder, String fileName) {
                setRecordingButton();
            }

            @Override
            public void onFailed(HostDeviceStreamRecorder recorder, String errorMessage) {
                showToast(R.string.host_recorder_failed_to_start_recording);
                setRecordingButton();
            }
        });
    }

    /**
     * ブロードキャストの開始・停止を切り替えます.
     */
    private void toggleBroadcaster() {
        HostMediaRecorder.Settings settings = mMediaRecorder.getSettings();
        if (mMediaRecorder.isBroadcasterRunning()) {
            mMediaRecorder.stopBroadcaster();
            setBroadcastButton();
        } else {
            mMediaRecorder.requestPermission(new HostMediaRecorder.PermissionCallback() {
                @Override
                public void onAllowed() {
                    String uri = settings.getBroadcastURI();
                    Broadcaster broadcaster = mMediaRecorder.startBroadcaster(uri);
                    if (broadcaster == null) {
                        showToast(R.string.host_recorder_failed_to_broadcast);
                    }
                    setBroadcastButton();
                }

                @Override
                public void onDisallowed() {
                    showToast(R.string.host_recorder_failed_to_broadcast);
                    setBroadcastButton();
                }
            });
        }
    }

    /**
     * プレビュー配信の開始・停止を切り替えます.
     */
    private void togglePreviewServer() {
        if (mMediaRecorder.isPreviewRunning()) {
            mMediaRecorder.stopPreview();
            setPreviewButton();
        } else {
            mMediaRecorder.requestPermission(new HostMediaRecorder.PermissionCallback() {
                @Override
                public void onAllowed() {
                    List<PreviewServer> servers = mMediaRecorder.startPreview();
                    if (servers.isEmpty()) {
                        showToast(R.string.host_recorder_failed_to_start_preview);
                    }
                    setPreviewButton();
                }

                @Override
                public void onDisallowed() {
                    showToast(R.string.host_recorder_failed_to_start_preview);
                    setPreviewButton();
                }
            });
        }
    }

    @Override
    public void toggleDisplayRotation() {
        super.toggleDisplayRotation();
        setDisplayRotationButton();
    }

    private void toggleAdjustView() {
        mAdjustViewFlag = !mAdjustViewFlag;
        adjustCameraSurfaceView();
    }

    private void toggleMute() {
        mMuted = !mMuted;

        if (mMediaRecorder != null) {
            mMediaRecorder.setMute(mMuted);
        }

        setMuteButton();
    }

    private void refreshUI() {
        setDisplayRotationButton();
        setPreviewButton();
        setBroadcastButton();
        setRecordingButton();
        setMuteButton();
    }

    private void adjustCameraSurfaceView() {
        runOnUiThread(() -> {
            View view = getView();
            if (view == null) {
                return;
            }

            PreviewSurfaceView surfaceView = view.findViewById(R.id.fragment_host_camera_surface_view);
            if (surfaceView != null && mEGLSurfaceDrawingThread != null) {
                Rect rect = mMediaRecorder.getSettings().getDrawingRange();
                Size size = mMediaRecorder.getSettings().getPreviewSize();
                int w = mEGLSurfaceDrawingThread.isSwappedDimensions() ? size.getHeight() : size.getWidth();
                int h = mEGLSurfaceDrawingThread.isSwappedDimensions() ? size.getWidth() : size.getHeight();
                if (rect != null) {
                    w = rect.width();
                    h = rect.height();
                }
                if (mAdjustViewFlag) {
                    surfaceView.adjustSurfaceView(w, h);
                } else {
                    surfaceView.fullSurfaceView(w, h);
                }
            }
        });
    }

    private void setDisplayRotationButton() {
        if (mMediaRecorder == null) {
            return;
        }

        if (isDisplayRotationFixed()) {
            mViewModel.setRotationResId(R.drawable.ic_baseline_sync_disabled_24);
        } else {
            mViewModel.setRotationResId(R.drawable.ic_baseline_sync_24);
        }
    }

    private void setMuteButton() {
        if (mMediaRecorder == null) {
            return;
        }

        HostMediaRecorder.Settings settings = mMediaRecorder.getSettings();
        if (settings.isMute() || !settings.isAudioEnabled()) {
            mViewModel.setMuteResId(R.drawable.ic_baseline_mic_off_24);
        } else {
            mViewModel.setMuteResId(R.drawable.ic_baseline_mic_24);
        }
    }

    private void setPreviewButton() {
        if (mMediaRecorder == null) {
            return;
        }

        if (mMediaRecorder.isPreviewRunning()) {
            mViewModel.setTogglePreviewResId(R.drawable.ic_baseline_stop_24);
        } else {
            mViewModel.setTogglePreviewResId(R.drawable.ic_baseline_tap_and_play_48);
        }
    }

    private void setBroadcastButton() {
        if (mMediaRecorder == null) {
            return;
        }

        if (mMediaRecorder.isBroadcasterRunning()) {
            mViewModel.setToggleBroadcastResId(R.drawable.ic_baseline_stop_24);
        } else {
            mViewModel.setToggleBroadcastResId(R.drawable.ic_baseline_cloud_upload_48);
        }
    }

    private void setRecordingButton() {
        if (mMediaRecorder == null) {
            return;
        }

        if (mMediaRecorder.getState() == HostMediaRecorder.State.RECORDING) {
            mViewModel.setToggleRecordingResId(R.drawable.ic_baseline_stop_24);
        } else {
            mViewModel.setToggleRecordingResId(R.drawable.ic_baseline_videocam_48);
        }
    }

    private class ButtonFadeInTransition {
        private final View mView;

        ButtonFadeInTransition(View view) {
            mView = view;
        }

        private void start() {
            Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.scale_up);
            mView.setVisibility(View.VISIBLE);
            mView.startAnimation(anim);
        }
    }

    private class ButtonFadeOutTransition {
        private final View mView;

        ButtonFadeOutTransition(View view) {
            mView = view;
        }

        private void start() {
            Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.scale_down);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mView.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            mView.startAnimation(anim);
        }
    }
}
