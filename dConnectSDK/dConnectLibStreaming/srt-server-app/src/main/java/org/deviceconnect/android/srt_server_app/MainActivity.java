package org.deviceconnect.android.srt_server_app;

import android.Manifest;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
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

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.List;

import org.deviceconnect.android.libmedia.streaming.MediaEncoder;
import org.deviceconnect.android.libmedia.streaming.MediaEncoderException;
import org.deviceconnect.android.libmedia.streaming.mpeg2ts.H264TsSegmenter;
import org.deviceconnect.android.libmedia.streaming.util.PermissionUtil;
import org.deviceconnect.android.libmedia.streaming.video.CameraSurfaceVideoEncoder;
import org.deviceconnect.android.libmedia.streaming.video.CameraVideoQuality;
import org.deviceconnect.android.libmedia.streaming.video.VideoQuality;
import org.deviceconnect.android.libsrt.SRTClientSocket;
import org.deviceconnect.android.libsrt.SRTServer;

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

    private H264TsSegmenter mH264TsSegmenter;

    private SRTServer mSRTServer;

    private final IpAddressManager mAddressManager = new IpAddressManager();

    private CameraSurfaceVideoEncoder mEncoder;

    private AutoFitSurfaceView mCameraView;

    /**
     * データを一時的に格納するためのバッファ.
     */
    private byte[] mVideoConfig;

    /**
     * SPS、PPS のデータを格納するバッファ.
     */
    private ByteBuffer mVideoSPSandPPS;

    private boolean mRequestedRestart;

    private MediaEncoder.Callback mEncoderCallback = new MediaEncoder.Callback() {
        @Override
        public void onStarted() {
            Log.d(TAG, "MediaEncoder.Callback: onStarted");
            runOnUiThread(() -> {
                VideoQuality q = mEncoder.getVideoQuality();
                mCameraView.setAspectRatio(q.getVideoHeight(), q.getVideoWidth());
            });
        }

        @Override
        public void onStopped() {
            Log.d(TAG, "MediaEncoder.Callback: onStopped");
            if (mRequestedRestart) {
                mRequestedRestart = false;
                startStreaming(false);
            }
        }

        @Override
        public void onFormatChanged(final MediaFormat newFormat) {
            Log.d(TAG, "MediaEncoder.Callback: onFormatChanged");
        }

        @Override
        public void onWriteData(final ByteBuffer encodedData, final MediaCodec.BufferInfo bufferInfo) {
            if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                createSPSandPPS(encodedData, bufferInfo);
                bufferInfo.size = 0;
            }

            if (bufferInfo.size != 0) {
                encodedData.position(bufferInfo.offset);
                encodedData.limit(bufferInfo.offset + bufferInfo.size);

                if (checkKeyFrame(bufferInfo)) {
                    packageSPSandPPS(encodedData, bufferInfo);
                    mH264TsSegmenter.generatePackets(mVideoSPSandPPS, bufferInfo.presentationTimeUs / 1000L);
                } else {
                    mH264TsSegmenter.generatePackets(encodedData, bufferInfo.presentationTimeUs / 1000L);
                }
            }
        }

        @Override
        public void onError(final MediaEncoderException e) {
            Log.e(TAG, "MediaEncoder.Callback: onError", e);
        }
    };

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
    };

    /**
     * 指定されたコーデックのバッファ情報がキーフレームか確認します.
     *
     * @param bufferInfo バッファ情報
     * @return キーフレームの場合はtrue、それ以外はfalse
     */
    @SuppressWarnings("deprecation")
    private boolean checkKeyFrame(MediaCodec.BufferInfo bufferInfo) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return (bufferInfo.flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0;
        } else {
            return (bufferInfo.flags & MediaCodec.BUFFER_FLAG_SYNC_FRAME) != 0;
        }
    }

    private void createSPSandPPS(final ByteBuffer encodedData, final MediaCodec.BufferInfo bufferInfo) {
        mVideoSPSandPPS = ByteBuffer.allocateDirect(bufferInfo.size);
        mVideoConfig = new byte[bufferInfo.size];
        encodedData.get(mVideoConfig, 0, bufferInfo.size);
        encodedData.position(bufferInfo.offset);
        mVideoSPSandPPS.put(mVideoConfig, 0, bufferInfo.size);
    }

    private void packageSPSandPPS(final ByteBuffer encodedData, final MediaCodec.BufferInfo bufferInfo) {
        if (mVideoSPSandPPS.capacity() < mVideoConfig.length + bufferInfo.size) {
            mVideoSPSandPPS = ByteBuffer.allocateDirect(mVideoConfig.length + bufferInfo.size);
            mVideoSPSandPPS.put(mVideoConfig);
            mVideoSPSandPPS.put(encodedData);
        } else {
            mVideoSPSandPPS.position(mVideoConfig.length);
            mVideoSPSandPPS.put(encodedData);
        }
        mVideoSPSandPPS.position(0);
    }


    private final H264TsSegmenter.BufferListener mBufferListener = (final byte[] result) -> {
        try {
            final int max = 188 * 7;
            if (result.length > max) {
                for (int offset = 0; offset < result.length; offset += max) {
                    final int length;
                    if (result.length - offset < max) {
                        length = result.length - offset;
                    } else {
                        length = max;
                    }
                    byte[] data = new byte[length];
                    System.arraycopy(result, offset, data, 0, length);
                    sendPacket(data);
                }
            } else {
                sendPacket(result);
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to send packet", e);
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

        mH264TsSegmenter = new H264TsSegmenter();
        mH264TsSegmenter.setBufferListener(mBufferListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

        List<String> denies = PermissionUtil.checkPermissions(this, PERMISSIONS);
        if (!denies.isEmpty()) {
            PermissionUtil.requestPermissions(this, denies, PERMISSION_REQUEST_CODE);
        } else {
            startStreaming(true);
        }
    }

    @Override
    protected void onPause() {
        stopStreaming(true);
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

    private void startStreaming(final boolean startServer) {
        String serverAddress = getIpAddress();
        if (serverAddress == null) {
            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "WiFi ルーターに接続してください", Toast.LENGTH_LONG).show());
            return;
        }

        try {
            mEncoder = new CameraSurfaceVideoEncoder(getApplicationContext());
            mEncoder.addSurface(mCameraView.getHolder().getSurface());
            mEncoder.setCallback(mEncoderCallback);

            int facing = mSettings.getCameraFacing();
            int fps = mSettings.getEncoderFrameRate();
            int biteRate = mSettings.getEncoderBitRate();
            Size previewSize = mSettings.getCameraPreviewSize(facing);

            if (startServer) {
                mSRTServer = new SRTServer(serverAddress, 12345);
                mSRTServer.addServerEventListener(mServerEventListener, new Handler(Looper.getMainLooper()));
                mSRTServer.addClientEventListener(mClientEventListener, new Handler(Looper.getMainLooper()));
                mSRTServer.open();
            }

            mH264TsSegmenter.initialize(0, 0,0, fps);

            CameraVideoQuality videoQuality = (CameraVideoQuality) mEncoder.getVideoQuality();
            videoQuality.setFacing(facing);
            videoQuality.setBitRate(biteRate);
            videoQuality.setFrameRate(fps);
            videoQuality.setVideoWidth(previewSize.getWidth());
            videoQuality.setVideoHeight(previewSize.getHeight());

            if (DEBUG) {
                Log.d(TAG, "Settings > Video Size: " + previewSize.getWidth() + " x " + previewSize.getHeight());
            }

            mEncoder.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void stopStreaming(final boolean stopServer) {
        if (mEncoder != null) {
            mEncoder.stop();
            mEncoder = null;
        }
        if (stopServer && mSRTServer != null) {
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

        mRequestedRestart = true;
        stopStreaming(false);
    }
}
