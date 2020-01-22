package org.deviceconnect.android.libmedia.streaming.camera2;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/**
 * カメラのパーミッション許可を受け取るためActivity.
 */
public abstract class CameraPermissionActivity extends AppCompatActivity {

    /**
     * カメラ用パーミッションのリクエストコード.
     */
    static final int CAMERA_REQUEST_CODE = 123456;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_REQUEST_CODE) {
            for (int i = 0; i < permissions.length && i < grantResults.length; i++) {
                String permission = permissions[i];
                if (Manifest.permission.CAMERA.equals(permission)) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        onAcceptCameraPermission();
                        return;
                    }
                }
            }
            // カメラのパーミッションが許可されていない
            onDenyCameraPermission();
        }
    }

    /**
     * カメラのパーミッショに許可が下りているか確認します.
     * <p>
     * 端末の SDK レベルが 23 未満の場合には、パーミッションが不要なので常にtrueを返却します。
     * </p>
     * @return カメラのパーミッションに許可が下りている場合はtrue、それ以外はfalse
     */
    public boolean checkCameraPermission() {
        return Camera2WrapperManager.checkCameraPermission(this);
    }

    /**
     * カメラのパーミッションを要求します.
     * <p>
     * パーミッションの結果は、{@link #onAcceptCameraPermission()} もしくは、{@link #onDenyCameraPermission()} に通知されます。
     * </p>
     */
    public void requestCameraPermission() {
        Camera2WrapperManager.requestCameraPermission(this, CAMERA_REQUEST_CODE);
    }

    /**
     * カメラのパーミッションが許可された場合に呼び出されます。
     */
    protected void onAcceptCameraPermission() {
    }

    /**
     * カメラのパーミッションが拒否された場合に呼び出されます。
     */
    protected void onDenyCameraPermission() {
    }
}
