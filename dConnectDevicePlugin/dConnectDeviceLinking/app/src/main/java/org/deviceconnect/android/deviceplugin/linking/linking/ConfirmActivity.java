package org.deviceconnect.android.deviceplugin.linking.linking;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.linking.BuildConfig;
import org.deviceconnect.android.deviceplugin.linking.R;

public class ConfirmActivity extends Activity {

    private int mCurrentRequestType = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.DEBUG) {
            Log.i("LinkingPlugIn", "ConfirmActivity:onCreate");
        }
        setContentView(R.layout.activity_confirm);
        startSensor(mCurrentRequestType);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (BuildConfig.DEBUG) {
            Log.i("LinkingPlugIn", "ConfirmActivity:onActivityResult");
            Log.i("LinkingPlugIn", "requestCode:" + requestCode);
            Log.i("LinkingPlugIn", "resultCode:" + resultCode);
            Log.i("LinkingPlugIn", "mCurrentRequestType:" + mCurrentRequestType);
        }

        if (requestCode != 4) {
            finish();
            return;
        }
        //RESULT_OK, RESULT_SENSOR_UNSUPPORTED
        if (resultCode != -1 && resultCode != 8) {
            finish();
            return;
        }
        if (mCurrentRequestType == 2) {
            finish();
            return;
        }
        mCurrentRequestType += 1;
        startSensor(mCurrentRequestType);
    }

    private void startSensor(int type) {
        if (BuildConfig.DEBUG) {
            Log.i("LinkingPlugIn", "ConfirmActivity:startSensor type:" + type);
        }
        Intent intent = new Intent("com.nttdocomo.android.smartdeviceagent.action.START_SENSOR");
        intent.setComponent(new ComponentName("com.nttdocomo.android.smartdeviceagent", "com.nttdocomo.android.smartdeviceagent.RequestStartActivity"));
        intent.putExtras(getIntent().getExtras());
        intent.putExtra("com.nttdocomo.android.smartdeviceagent.extra.SENSOR_TYPE", type);
        try {
            startActivityForResult(intent, 4);
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }

}
