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

import org.deviceconnect.android.libmedia.streaming.util.PermissionUtil;
import org.deviceconnect.android.libmedia.streaming.video.CameraSurfaceVideoEncoder;
import org.deviceconnect.android.libmedia.streaming.video.CameraVideoQuality;
import org.deviceconnect.android.libsrt.SRTClientSocket;
import org.deviceconnect.android.libsrt.server.SRTServer;
import org.deviceconnect.android.libsrt.server.SRTSession;
import org.deviceconnect.android.libsrt.server.video.CameraVideoStream;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.List;

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

    private final SRTServer.ServerEventListener mServerEventListener = new SRTServer.ServerEventListener() {
        @Override
        public void onOpen(final SRTServer server) {
            if (DEBUG) {
                Log.d(TAG, "Started SRT Server: address = " + server.getServerAddress() + ":" + server.getServerPort());
            }
            showServerAddress(server);
        }

        @Override
        public void onClose(final SRTServer server) {
            if (DEBUG) {
                Log.d(TAG, "Stopped SRT Server: address = " + server.getServerAddress() + ":" + server.getServerPort());
            }
        }

        @Override
        public void onAcceptClient(final SRTServer server, final SRTClientSocket clientSocket) {
            if (DEBUG) {
                Log.d(TAG, "Accepted SRT Client: client address = " + clientSocket.getSocketAddress());
            }
        }

        @Override
        public void onErrorOpen(final SRTServer server, final int error) {
            if (DEBUG) {
                Log.d(TAG, "onErrorOpen: address = " + server.getServerAddress() + ":" + server.getServerPort());
            }
        }
    };

    private final SRTServer.ClientEventListener mClientEventListener = new SRTServer.ClientEventListener() {

        @Override
        public void onSendPacket(final SRTServer server,
                                 final SRTClientSocket clientSocket,
                                 final int payloadByteSize) {
//            if (DEBUG) {
//                Log.d(TAG, "onSendPacket: payloadByteSize = " + payloadByteSize);
//            }
        }

        @Override
        public void onErrorSendPacket(final SRTServer server, final SRTClientSocket clientSocket) {
            if (DEBUG) {
                Log.d(TAG, "onErrorSendPacket: clientSocket = " + clientSocket.getSocketAddress());
            }
        }
    };

    private void sendPacket(final byte[] packet) throws IOException {
        SRTServer server = mSRTServer;
        if (server != null) {
            server.sendPacket(packet);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCameraView = findViewById(R.id.surface_view);

        mSettings = new Settings(getApplicationContext());
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
        String serverAddress = getIpAddress();
        if (serverAddress == null) {
            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "WiFi ルーターに接続してください", Toast.LENGTH_LONG).show());
            return;
        }

        try {
            mSRTServer = new SRTServer(serverAddress, 12345);
            mSRTServer.addServerEventListener(mServerEventListener, new Handler(Looper.getMainLooper()));
            mSRTServer.addClientEventListener(mClientEventListener, new Handler(Looper.getMainLooper()));
            mSRTServer.setCallback(new SRTServer.Callback() {
                @Override
                public void createSession(final SRTSession session) {
                    Log.e("ABC", "AAAAAAAAAAAAA createSession");

                    CameraVideoStream videoMediaStream = new CameraVideoStream(getApplicationContext());
                    CameraSurfaceVideoEncoder encoder = (CameraSurfaceVideoEncoder) videoMediaStream.getVideoEncoder();
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

                    session.setVideoStream(videoMediaStream);
                }

                @Override
                public void releaseSession(final SRTSession session) {
                    Log.e("ABC", "AAAAAAAAAAAAA releaseSession");

                }
            });
            mSRTServer.open();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void stopStreaming() {
        if (mSRTServer != null) {
            mSRTServer.close();
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
