package org.deviceconnect.android.srt_server_app;

import android.Manifest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import org.deviceconnect.android.libmedia.streaming.util.IpAddressManager;
import org.deviceconnect.android.libmedia.streaming.util.PermissionUtil;
import org.deviceconnect.android.libmedia.streaming.video.CameraSurfaceVideoEncoder;
import org.deviceconnect.android.libmedia.streaming.video.CameraVideoQuality;
import org.deviceconnect.android.libsrt.SRT;
import org.deviceconnect.android.libsrt.SRTSocket;
import org.deviceconnect.android.libsrt.server.SRTServer;
import org.deviceconnect.android.libsrt.server.SRTSession;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static org.deviceconnect.android.srt_server_app.BuildConfig.DEBUG;

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
            Manifest.permission.CAMERA
    };

    private Settings mSettings;

    private SRTServer mSRTServer;

    private final IpAddressManager mAddressManager = new IpAddressManager();

    private AutoFitSurfaceView mCameraView;

    private Timer mStatsTimer;

    private final SRTServer.ServerEventListener mServerEventListener = new SRTServer.ServerEventListener() {
        @Override
        public void onStart(final SRTServer server) {
            if (DEBUG) {
                Log.d(TAG, "Started SRT Server: address = " + server.getServerAddress() + ":" + server.getServerPort());
            }
            showServerAddress(server);

            mStatsTimer = new Timer();
            mStatsTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    for (SRTSocket socket : server.getSocketList()) {
                        socket.dumpStats();
                    }
                }
            }, 0, 5 * 1000);
        }

        @Override
        public void onStop(final SRTServer server) {
            if (DEBUG) {
                Log.d(TAG, "Stopped SRT Server: address = " + server.getServerAddress() + ":" + server.getServerPort());
            }

            mStatsTimer.cancel();
            mStatsTimer = null;
        }

        @Override
        public void onAcceptClient(final SRTServer server, final SRTSocket clientSocket) {
            if (DEBUG) {
                Log.d(TAG, "Accepted SRT Client: client address = " + clientSocket.getSocketAddress());
            }
        }

        @Override
        public void onErrorStart(final SRTServer server, final int error) {
            if (DEBUG) {
                Log.d(TAG, "onErrorStart: address = " + server.getServerAddress() + ":" + server.getServerPort());
            }
        }
    };

    private final SRTServer.ClientEventListener mClientEventListener = new SRTServer.ClientEventListener() {

        @Override
        public void onSendPacket(final SRTServer server,
                                 final SRTSocket clientSocket,
                                 final int payloadByteSize) {
//            if (DEBUG) {
//                Log.d(TAG, "onSendPacket: payloadByteSize = " + payloadByteSize);
//            }
        }

        @Override
        public void onErrorSendPacket(final SRTServer server, final SRTSocket clientSocket) {
            if (DEBUG) {
                Log.d(TAG, "onErrorSendPacket: clientSocket = " + clientSocket.getSocketAddress());
            }
        }
    };

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
            mSRTServer.setMaxClientNum(1);
            mSRTServer.addServerEventListener(mServerEventListener, new Handler(Looper.getMainLooper()));
            mSRTServer.addClientEventListener(mClientEventListener, new Handler(Looper.getMainLooper()));
            mSRTServer.setCallback(new SRTServer.Callback() {
                @Override
                public void createSession(final SRTSession session) {
                    Log.e("ABC", "AAAAAAAAAAAAA createSession");

                    CameraSurfaceVideoEncoder encoder = new CameraSurfaceVideoEncoder(getApplicationContext());
                    encoder.addSurface(mCameraView.getHolder().getSurface());

                    CameraVideoQuality videoQuality = (CameraVideoQuality) encoder.getVideoQuality();
                    int facing = mSettings.getCameraFacing();
                    int fps = mSettings.getEncoderFrameRate();
                    int biteRate = mSettings.getEncoderBitRate();
                    Size previewSize = mSettings.getCameraPreviewSize(facing);
                    videoQuality.setFacing(facing);
                    videoQuality.setBitRate(biteRate);
                    videoQuality.setFrameRate(fps);
                    videoQuality.setVideoWidth(previewSize.getWidth());
                    videoQuality.setVideoHeight(previewSize.getHeight());

                    session.setVideoEncoder(encoder);
                }

                @Override
                public void releaseSession(final SRTSession session) {
                    Log.e("ABC", "AAAAAAAAAAAAA releaseSession");

                }
            });
            mSRTServer.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void stopStreaming() {
        if (mSRTServer != null) {
            mSRTServer.stop();
            mSRTServer = null;
        }
    }

    private void showServerAddress(final SRTServer server) {
        runOnUiThread(() -> {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle("srt://" + server.getServerAddress()  + ":" + server.getServerPort());
            }
        });
    }

    private String getIpAddress() {
        mAddressManager.storeIPAddress();
        InetAddress address = mAddressManager.getWifiIPv4Address();
        if (address != null) {
            return address.getHostAddress();
        }
        return null;
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
