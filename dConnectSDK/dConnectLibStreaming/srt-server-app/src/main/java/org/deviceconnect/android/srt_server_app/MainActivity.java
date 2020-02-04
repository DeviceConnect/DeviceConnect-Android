package org.deviceconnect.android.srt_server_app;

import android.Manifest;
import android.media.AudioFormat;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import org.deviceconnect.android.libmedia.streaming.audio.AudioEncoder;
import org.deviceconnect.android.libmedia.streaming.audio.AudioQuality;
import org.deviceconnect.android.libmedia.streaming.audio.MicAACLATMEncoder;
import org.deviceconnect.android.libmedia.streaming.util.IpAddressManager;
import org.deviceconnect.android.libmedia.streaming.util.PermissionUtil;
import org.deviceconnect.android.libmedia.streaming.video.CameraSurfaceVideoEncoder;
import org.deviceconnect.android.libmedia.streaming.video.CameraVideoQuality;
import org.deviceconnect.android.libmedia.streaming.video.VideoEncoder;
import org.deviceconnect.android.libsrt.SRT;
import org.deviceconnect.android.libsrt.server.SRTServer;
import org.deviceconnect.android.libsrt.server.SRTSession;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

/**
 * SRTサーバからAndroid端末のカメラ映像を配信する画面.
 */
public class MainActivity extends AppCompatActivity
        implements SettingsDialogFragment.SettingsDialogListener {

    private static final String TAG = "SRTServer";

    /**
     * パーミッションのリクエストコード.
     */
    private static final int PERMISSION_REQUEST_CODE = 123456;

    /**
     * 使用するパーミッションのリスト.
     */
    private static final String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };

    private Settings mSettings;

    private SRTServer mSRTServer;

    private final IpAddressManager mAddressManager = new IpAddressManager();

    private AutoFitSurfaceView mCameraView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCameraView = findViewById(R.id.surface_view);
        mSettings = new Settings(getApplicationContext());

        SRT.startup();
    }

    @Override
    protected void onResume() {
        super.onResume();

        List<String> denies = PermissionUtil.checkPermissions(this, PERMISSIONS);
        if (!denies.isEmpty()) {
            PermissionUtil.requestPermissions(this, denies, PERMISSION_REQUEST_CODE);
        } else {
            startStreaming();
        }
    }

    @Override
    protected void onPause() {
        stopStreaming();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        SRT.cleanup();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            List<String> denies = PermissionUtil.checkRequestPermissionsResult(permissions, grantResults);
            if (!denies.isEmpty()) {
                Toast.makeText(this, getString(R.string.app_name) + "にはカメラの許可が必要です", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void startStreaming() {
        try {
            mSRTServer = new SRTServer(12345);
            mSRTServer.setCallback(new SRTServer.Callback() {
                @Override
                public void createSession(final SRTSession session) {
                    Log.d(TAG, "createSession");

                    session.setVideoEncoder(createVideoEncoder());
                    session.setAudioEncoder(createAudioEncoder());
                }

                @Override
                public void releaseSession(final SRTSession session) {
                    Log.d(TAG, "releaseSession");
                }
            });
            mSRTServer.start();

            showServerAddress(getIpAddress(), mSRTServer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private VideoEncoder createVideoEncoder() {
        CameraSurfaceVideoEncoder videoEncoder = new CameraSurfaceVideoEncoder(getApplicationContext());
        videoEncoder.addSurface(mCameraView.getHolder().getSurface());

        CameraVideoQuality videoQuality = (CameraVideoQuality) videoEncoder.getVideoQuality();
        int facing = mSettings.getCameraFacing();
        int fps = mSettings.getEncoderFrameRate();
        int biteRate = mSettings.getEncoderBitRate();
        Size previewSize = mSettings.getCameraPreviewSize(facing);
        videoQuality.setFacing(facing);
        videoQuality.setBitRate(biteRate);
        videoQuality.setFrameRate(fps);
        videoQuality.setVideoWidth(previewSize.getWidth());
        videoQuality.setVideoHeight(previewSize.getHeight());
        return videoEncoder;
    }

    private AudioEncoder createAudioEncoder() {
        MicAACLATMEncoder audioEncoder = new MicAACLATMEncoder();
        audioEncoder.setMute(false);
        AudioQuality audioQuality = audioEncoder.getAudioQuality();
        audioQuality.setChannel(AudioFormat.CHANNEL_IN_MONO);
        audioQuality.setSamplingRate(8000);
        audioQuality.setBitRate(64 * 1024);
        audioQuality.setUseAEC(true);
        return audioEncoder;
    }

    private void stopStreaming() {
        if (mSRTServer != null) {
            mSRTServer.stop();
            mSRTServer = null;
        }
    }

    private void showServerAddress(final String address, final SRTServer server) {
        runOnUiThread(() -> {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle("srt://" + address  + ":" + server.getServerPort());
            }
        });
    }

    private String getIpAddress() {
        mAddressManager.storeIPAddress();
        InetAddress address = mAddressManager.getWifiIPv4Address();
        if (address != null) {
            return address.getHostAddress();
        }
        return "127.0.0.1";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_settings) {
            DialogFragment settingsDialog = new SettingsDialogFragment();
            settingsDialog.setCancelable(false);
            settingsDialog.show(getSupportFragmentManager(), "settings");
        }
        return true;
    }

    @Override
    public void onSettingsDialogDismiss() {
        Log.d(TAG, "onSettingsDialogDismiss");

        Fragment settingsDialog = getSupportFragmentManager().findFragmentById(R.id.settings_fragment);
        if (settingsDialog != null) {
            getSupportFragmentManager().beginTransaction().remove(settingsDialog).commit();
        }

        stopStreaming();
        startStreaming();
    }
}
