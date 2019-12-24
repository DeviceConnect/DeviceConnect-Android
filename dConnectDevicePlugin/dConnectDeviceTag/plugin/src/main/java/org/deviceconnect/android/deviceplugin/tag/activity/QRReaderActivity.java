/*
 QRReaderActivity.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.tag.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;

import org.deviceconnect.android.deviceplugin.tag.R;
import org.deviceconnect.android.deviceplugin.tag.TagMessageService;
import org.deviceconnect.android.deviceplugin.tag.camera2.AutoFitTextureView;
import org.deviceconnect.android.deviceplugin.tag.camera2.Camera2StateMachine;
import org.deviceconnect.android.deviceplugin.tag.services.TagConstants;
import org.deviceconnect.android.deviceplugin.tag.services.TagInfo;
import org.deviceconnect.android.deviceplugin.tag.services.TagService;
import org.deviceconnect.android.deviceplugin.tag.services.qr.QRReader;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * QRコードを読み込むための Activity.
 *
 * @author NTT DOCOMO, INC.
 */
public class QRReaderActivity extends BindServiceActivity implements TagConstants {
    /**
     * リクエストコード.
     */
    private static final int REQUEST_CODE = 1000;

    /**
     * QRコードを動作させるために必要なパーミッションのリスト.
     */
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.CAMERA
    };

    /**
     * カメラを操作するクラス.
     */
    private Camera2StateMachine mCamera2;

    /**
     * カメラのプレビューを表示するView.
     */
    private AutoFitTextureView mTextureView;

    /**
     * QRコードを解析処理を行うスレッド.
     */
    private ExecutorService mQRProcessExecutorService = Executors.newSingleThreadExecutor();

    /**
     * QRコードの解析フラグ.
     */
    private boolean mProcessing;

    /**
     * QRコード読み込み用クラス.
     */
    private QRReader mQRReader = new QRReader();

    /**
     * QRコードが動作フラグ.
     * <p>
     * true: QRコードが動作中
     * false: QRコード動作前
     * </p>
     */
    private boolean mRunning;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_reader);

        // カメラをサポートしていない場合の処理
        if (!checkCameraHardware()) {
            postTagReaderActivityResult(TagConstants.RESULT_NOT_SUPPORT, null);
            finish();
        }

        mTextureView = findViewById(R.id.activity_qr_reader_texture_view);
    }

    @Override
    protected void onPause() {
        stopCapture();

        // QRコードが動作中に終了された時に、まだレスポンスを返却していない場合にはエラーを返しておく
        if (mRunning && !isReturnedResponse()) {
            postTagReaderActivityResult(TagConstants.RESULT_FAILED, null);
        }

        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            int granted = 0;
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_GRANTED) {
                    granted++;
                }
            }
            if (permissions.length != granted) {
                openNoPermissions();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected TagService getTagService() {
        TagMessageService service = getBoundService();
        return service == null ? null : service.getQRService();
    }

    @Override
    protected void onServiceConnected() {
        if (checkRequestPermission()) {
            return;
        }

        mRunning = true;
        startCapture();
    }

    /**
     * カメラのサポート状況を取得します.
     *
     * @return サポートしている場合にはtrue、それ以外はfalse
     */
    private boolean checkCameraHardware() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    /**
     * QRコードの読み込みを開始します.
     */
    private void startCapture() {
        if (mCamera2 == null) {
            mCamera2 = new Camera2StateMachine(this);
            mCamera2.getSettings().setPreviewSize(mCamera2.getSettings().getApproximatePreviewSize(1200, 1500));
            mCamera2.setErrorCallback((errorCode) -> {
                postTagReaderActivityResult(TagConstants.RESULT_FAILED, null);
                finish();
            });
            mCamera2.setTextureView(mTextureView);
            mCamera2.startPreview(mPreviewListener);
        }
    }

    /**
     * QRコードの読み込みを停止します.
     */
    private void stopCapture() {
        if (mCamera2 != null) {
            mCamera2.close();
        }
    }

    /**
     * パーミッションのリクエストがあるか確認します.
     * <p>
     * パーミッションのリクエストがある場合には、パーミッションのリクエストを送信します。
     * </p>
     * @return パーミッションのリクエストがある場合はtrue、それ以外はfalse
     */
    private boolean checkRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> requests = getRequestPermission(this);
            if (!requests.isEmpty()) {
                requestPermissions(requests.toArray(new String[0]), REQUEST_CODE);
                return true;
            }
        }
        return false;
    }

    /**
     * QRコードを動作させるために必要なパーミッションのリストを取得します.
     * <p>
     * 必要なパーミッションが無い場合には空のリストを返却します.
     * </p>
     * @param context コンテキスト
     * @return パーミッションのリスト
     */
    private static List<String> getRequestPermission(final Context context) {
        List<String> requests = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : REQUIRED_PERMISSIONS) {
                if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    requests.add(permission);
                }
            }
        }
        return requests;
    }

    /**
     * パーミッションが無いことをダイアログで通知します.
     */
    private void openNoPermissions() {
        postTagReaderActivityResult(TagConstants.RESULT_NO_PERMISSION, null);
        finish();
    }

    /**
     * 回数設定を取得します.
     *
     * @return １回の場合はtrue、それ以外はfalse
     */
    private boolean isOnce() {
        Intent intent = getIntent();
        if (intent != null) {
            return intent.getBooleanExtra(EXTRA_ONCE, true);
        }
        return true;
    }

    /**
     * プレビューを受け取るためのリスナー.
     */
    private final ImageReader.OnImageAvailableListener mPreviewListener = (imageReader) -> {
        try (Image image = imageReader.acquireNextImage()) {
            if (image != null && image.getPlanes() != null) {
                if (!mProcessing) {
                    mProcessing = true;
                    processQR(image);
                }
            }
        }
    };

    /**
     * 画像からQRコードを解析します.
     *
     * @param image 解析する画像
     */
    private void processQR(final Image image) {
        Bitmap bitmap = createBitmapFromImage(image);
        if (bitmap != null) {
            mQRProcessExecutorService.execute(() -> {
                try {
                    TagInfo tagInfo = mQRReader.read(bitmap);
                    if (isOnce()) {
                        stopCapture();
                    }
                    postTagReaderActivityResult(TagConstants.RESULT_SUCCESS, tagInfo);
                } catch (Throwable t) {
                    // ignore.
                } finally {
                    mProcessing = false;
                }
            });
        } else {
            mProcessing = false;
        }
    }

    /**
     * {@link Image} から {@link Bitmap} を作成します.
     * <p>
     *     デコードに失敗した場合は null を返却します。
     * </p>
     * @param image カメラのプレビュー
     * @return {@link Bitmap} のインスタンス
     */
    private Bitmap createBitmapFromImage(final Image image) {
        try {
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] jpeg = new byte[buffer.remaining()];
            buffer.get(jpeg);
            return BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);
        } catch (Throwable t) {
            return null;
        }
    }
}
