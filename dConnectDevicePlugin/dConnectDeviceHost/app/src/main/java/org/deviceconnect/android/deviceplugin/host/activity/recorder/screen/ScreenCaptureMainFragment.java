package org.deviceconnect.android.deviceplugin.host.activity.recorder.screen;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.activity.fragment.HostDevicePluginBindPreferenceFragment;
import org.deviceconnect.android.deviceplugin.host.activity.recorder.settings.SettingsActivity;
import org.deviceconnect.android.deviceplugin.host.recorder.Broadcaster;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceStreamRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostMediaRecorderManager;
import org.deviceconnect.android.deviceplugin.host.recorder.PreviewServer;
import org.deviceconnect.android.deviceplugin.host.recorder.screen.ScreenCastRecorder;

import java.util.List;

public class ScreenCaptureMainFragment extends HostDevicePluginBindPreferenceFragment{

    private HostMediaRecorderManager.OnEventListener mOnEventListener = new HostMediaRecorderManager.OnEventListener() {
        @Override
        public void onPreviewStarted(HostMediaRecorder recorder, List<PreviewServer> servers) {
            setPreviewButton();
        }

        @Override
        public void onPreviewStopped(HostMediaRecorder recorder) {
            setPreviewButton();
        }

        @Override
        public void onBroadcasterStarted(HostMediaRecorder recorder, Broadcaster broadcaster) {
            setBroadcastButton();
        }

        @Override
        public void onBroadcasterStopped(HostMediaRecorder recorder) {
            setBroadcastButton();
        }

        @Override
        public void onTakePhoto(HostMediaRecorder recorder, String uri, String filePath, String mimeType) {
        }

        @Override
        public void onRecordingStarted(HostMediaRecorder recorder, String fileName) {
            setRecordingButton();
        }

        @Override
        public void onRecordingPause(HostMediaRecorder recorder) {
        }

        @Override
        public void onRecordingResume(HostMediaRecorder recorder) {
        }

        @Override
        public void onRecordingStopped(HostMediaRecorder recorder, String fileName) {
            setRecordingButton();
        }

        @Override
        public void onError(HostMediaRecorder recorder, Exception e) {
        }
    };

    private HostMediaRecorderManager mMediaRecorderManager;
    private HostMediaRecorder mMediaRecorder;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.host_app_screen_capture, rootKey);
    }

    @Override
    public void onBindService() {
        if (mMediaRecorder != null) {
            mMediaRecorder.onConfigChange();
        } else {
            mMediaRecorderManager = getHostDevicePlugin().getHostMediaRecorderManager();
            mMediaRecorderManager.addOnEventListener(mOnEventListener);
            mMediaRecorder = getMediaRecorder();
            if (mMediaRecorder == null) {
                // TODO 画面キャプチャに対応していない
                return;
            }
            refreshUI();
        }
    }

    @Override
    public void onUnbindService() {
        if (mMediaRecorderManager != null) {
            mMediaRecorderManager.removeOnEventListener(mOnEventListener);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(final Preference preference) {
        if ("settings_capture".equals(preference.getKey())) {
            gotoRecorderSettings();
        } else if ("start_preview".equals(preference.getKey())) {
            SwitchPreferenceCompat pref = (SwitchPreferenceCompat) preference;
            if (pref.isChecked()) {
                startPreview();
            } else {
                stopPreview();
            }
        } else if ("start_broadcast".equals(preference.getKey())) {
            SwitchPreferenceCompat pref = (SwitchPreferenceCompat) preference;
            if (pref.isChecked()) {
                startBroadcast();
            } else {
                stopBroadcast();
            }
        } else if ("start_recording".equals(preference.getKey())) {
            SwitchPreferenceCompat pref = (SwitchPreferenceCompat) preference;
            if (pref.isChecked()) {
                startRecording();
            } else {
                stopRecording();
            }
        }
        return super.onPreferenceTreeClick(preference);
    }

    private HostMediaRecorder getMediaRecorder() {
        HostMediaRecorderManager manager = getHostDevicePlugin().getHostMediaRecorderManager();
        HostMediaRecorder[] recorders = manager.getRecorders();
        for (HostMediaRecorder recorder : recorders) {
            if (recorder instanceof ScreenCastRecorder) {
                return recorder;
            }
        }
        return null;
    }


    /**
     * レコーダの設定画面へ遷移します.
     */
    private void gotoRecorderSettings() {
        Context context = getContext();
        if (context != null) {
            SettingsActivity.startActivity(context, mMediaRecorder.getId(), null);
        }
    }

    private void requestPermission(Runnable run) {
        mMediaRecorder.requestPermission(new HostMediaRecorder.PermissionCallback() {
            @Override
            public void onAllowed() {
                run.run();
            }

            @Override
            public void onDisallowed() {
                refreshUI();
            }
        });
    }

    private void refreshUI() {
        setBroadcastButton();
        setPreviewButton();
        setRecordingButton();
    }

    private Handler mUIHandler = new Handler(Looper.getMainLooper());

    private void runThread(Runnable run) {
        mUIHandler.post(run);
    }

    private void setPreviewButton() {
        runThread(() -> {
            SwitchPreferenceCompat pref = findPreference("start_preview");
            if (pref != null) {
                pref.setChecked(mMediaRecorder.isPreviewRunning());
            }
        });
    }

    private void setBroadcastButton() {
        runThread(() -> {
            SwitchPreferenceCompat pref = findPreference("start_broadcast");
            if (pref != null) {
                pref.setChecked(mMediaRecorder.isBroadcasterRunning());
            }
        });
    }

    private void setRecordingButton() {
        runThread(() -> {
            SwitchPreferenceCompat pref = findPreference("start_recording");
            if (pref != null) {
                pref.setChecked(mMediaRecorder.getState() == HostMediaRecorder.State.RECORDING);
            }
        });
    }

    private void startPreview() {
        requestPermission(() -> mMediaRecorder.startPreview());
    }

    private void stopPreview() {
        mMediaRecorder.stopPreview();
    }

    private void startBroadcast() {
        requestPermission(() -> {
            HostMediaRecorder.Settings settings = mMediaRecorder.getSettings();
            String uri = settings.getBroadcastURI();
            uri = "rtmp://192.168.11.7:1935/live/abc";
            Broadcaster broadcaster = mMediaRecorder.startBroadcaster(uri);
            if (broadcaster == null) {

            }
        });
    }

    private void stopBroadcast() {
        mMediaRecorder.stopBroadcaster();
    }

    private void startRecording() {
        requestPermission(() -> {
            mMediaRecorder.startRecording(new HostDeviceStreamRecorder.RecordingCallback() {
                @Override
                public void onRecorded(HostDeviceStreamRecorder recorder, String fileName) {
                }

                @Override
                public void onFailed(HostDeviceStreamRecorder recorder, String errorMessage) {
                }
            });
        });
    }

    private void stopRecording() {
        mMediaRecorder.stopRecording(null);
    }
}
