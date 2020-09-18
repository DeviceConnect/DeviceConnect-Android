package org.deviceconnect.android.deviceplugin.host.recorder.util;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import org.deviceconnect.android.deviceplugin.host.R;

import androidx.annotation.Nullable;

/**
 * オーバーレイの利用許可を求めるダイアログを表示するための Activity.
 */
public class OverlayPermissionActivity extends Activity implements SimpleDialogFragment.Callback {
    /**
     * オーバーレイ許可用のリクエストコードを定義.
     */
    private static final int REQUEST_CODE_OVERLAY = 1234;

    /**
     * オーバーレイ許可設定画面を表示して良いかの確認ダイアログのタグを定義.
     */
    private static final String TAG_NO_PERMISSION_DIALOG = "overlay-permission";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // ステータスバーを消して、フルスクリーンにします
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        );

        showOverlayDisabled();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_OVERLAY) {
            if (isOverlayAllowed()) {
                sendShowOverlayPreview();
            }
        }

        finish();
    }

    @Override
    public void onDialogPositiveButtonClicked(SimpleDialogFragment dialog) {
        requestOverlayPermission();
    }

    @Override
    public void onDialogNegativeButtonClicked(SimpleDialogFragment dialog) {
        finish();
    }

    /**
     * オーバーレイ表示のオーバーレイを送信します.
     */
    private void sendShowOverlayPreview() {
        Intent intent = new Intent();
        intent.setAction("org.deviceconnect.android.deviceplugin.host.SHOW_OVERLAY_PREVIEW");
        sendBroadcast(intent);
    }

    /**
     * オーバーレイの表示許可が下りていない場合のエラーダイアログを表示します.
     */
    private void showOverlayDisabled() {
        try {
            SimpleDialogFragment f = new SimpleDialogFragment.Builder()
                    .setTitle(getString(R.string.overlay_no_permission_title))
                    .setMessage(getString(R.string.overlay_no_permission_message))
                    .setPositive(getString(R.string.overlay_no_permission_ok))
                    .setNegative(getString(R.string.overlay_no_permission_no))
                    .setCancelable(false)
                    .create();
            f.show(getFragmentManager(), TAG_NO_PERMISSION_DIALOG);
        } catch (Exception e) {
            finish();
        }
    }

    /**
     * オーバーレイの表示許可を確認します.
     *
     * @return オーバーレイの表示許可がある場合はtrue、それ以外はfalse
     */
    public boolean isOverlayAllowed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this);
        } else {
            return true;
        }
    }

    /**
     * オーバーレイの表示設定画面を開きます.
     *
     * Android OS 標準の設定画面になります。
     */
    private void requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE_OVERLAY);
        }
    }
}
