/*
 PermissionReceiverActivity.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder.screen;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.view.Window;

/**
 * Permission Receiver Activity.
 *
 * @author NTT DOCOMO, INC.
 */
@TargetApi(21)
public class PermissionReceiverActivity extends Activity {

    private static final int REQUEST_CODE = 1;

    private MediaProjectionManager mManager;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = mManager.createScreenCaptureIntent();
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
                                    final Intent data) {
        if (requestCode != REQUEST_CODE) {
            return;
        }

        Bundle response = new Bundle();
        response.putParcelable(HostDeviceScreenCastRecorder.RESULT_DATA, data);

        ResultReceiver callback = getIntent().getParcelableExtra(HostDeviceScreenCastRecorder.EXTRA_CALLBACK);
        callback.send(Activity.RESULT_OK, response);
        finish();
    }
}
