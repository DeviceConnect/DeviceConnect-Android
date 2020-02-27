package org.deviceconnect.android.rtspserver.screen;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MediaProjectionActivity extends AppCompatActivity {

    private static final int REQUEST_CAPTURE = 10001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        MediaProjectionManager manager = (MediaProjectionManager) getSystemService(Service.MEDIA_PROJECTION_SERVICE);
        if (manager != null) {
            startActivityForResult(manager.createScreenCaptureIntent(), REQUEST_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CAPTURE) {
            Bundle response = new Bundle();
            response.putParcelable("result", data);
            ResultReceiver callback = getIntent().getParcelableExtra("callback");
            callback.send(Activity.RESULT_OK, response);
            finish();
        }
    }
}
