package org.deviceconnect.android.deviceplugin.host.activity.recorder.screencast;

import android.content.Context;
import android.os.Bundle;

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

public class ScreencastMainFragment extends HostDevicePluginBindPreferenceFragment {

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
        public void onBroadcasterStarted(HostMediaRecorder recorder, Broadcaster broadcaster) {
            setBroadcastButton();
        }

        @Override
        public void onBroadcasterStopped(HostMediaRecorder recorder, Broadcaster broadcaster) {
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
        if (mMediaRecorder == null) {
            mMediaRecorderManager = getHostDevicePlugin().getHostMediaRecorderManager();
            mMediaRecorderManager.addOnEventListener(mOnEventListener);
            mMediaRecorder = getScreenCastRecorder();
            if (mMediaRecorder == null) {
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

    /**
     * ScreenCastRecorder のインスタンスを取得します.
     *
     * スクリーンキャストに対応していない場合には、null を返却します。
     *
     * @return ScreenCastRecorder のインスタンス
     */
    private HostMediaRecorder getScreenCastRecorder() {
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

    /**
     * スクリーンキャストのパーミッションを取得します.
     *
     * @param run パーミッション取得時の処理を行う Runnable
     */
    private void requestPermission(Runnable run) {
        mMediaRecorder.requestPermission(new HostMediaRecorder.PermissionCallback() {
            @Override
            public void onAllowed() {
                run.run();
            }

            @Override
            public void onDisallowed() {
                refreshUI();
                showToast(R.string.host_recorder_deny_permission);
            }
        });
    }

    private void refreshUI() {
        setBroadcastButton();
        setPreviewButton();
        setRecordingButton();
    }

    private void setPreviewButton() {
        runOnUiThread(() -> {
            SwitchPreferenceCompat pref = findPreference("start_preview");
            if (pref != null) {
                pref.setChecked(mMediaRecorder.isPreviewRunning());
            }
        });
    }

    private void setBroadcastButton() {
        runOnUiThread(() -> {
            SwitchPreferenceCompat pref = findPreference("start_broadcast");
            if (pref != null) {
                pref.setChecked(mMediaRecorder.isBroadcasterRunning());
            }
        });
    }

    private void setRecordingButton() {
        runOnUiThread(() -> {
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
            Broadcaster broadcaster = mMediaRecorder.startBroadcaster(uri);
            if (broadcaster == null) {
                showToast(R.string.host_recorder_failed_to_broadcast);
                setBroadcastButton();
            }
        });
    }

    private void stopBroadcast() {
        mMediaRecorder.stopBroadcaster();
    }

    private void startRecording() {
        requestPermission(() -> mMediaRecorder.startRecording(new HostDeviceStreamRecorder.RecordingCallback() {
            @Override
            public void onRecorded(HostDeviceStreamRecorder recorder, String fileName) {
            }

            @Override
            public void onFailed(HostDeviceStreamRecorder recorder, String errorMessage) {
                showToast(R.string.host_recorder_failed_to_start_recording);
            }
        }));
    }

    private void stopRecording() {
        mMediaRecorder.stopRecording(null);
    }
}
