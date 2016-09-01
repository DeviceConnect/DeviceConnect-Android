package org.deviceconnect.android.deviceplugin.awsiot.setting;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amazonaws.regions.Regions;

import org.deviceconnect.android.deviceplugin.awsiot.AWSIotRemoteManager;
import org.deviceconnect.android.deviceplugin.awsiot.core.RemoteDeviceConnectManager;
import org.deviceconnect.android.deviceplugin.local.R;

public class AWSIotTestActivity extends Activity {
    private RemoteDeviceConnectManager mRemoteManager;
    private AWSIotRemoteManager mAWSIotRemoteManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        mRemoteManager = new RemoteDeviceConnectManager("nobu", "abc");

        Button connectBtn = (Button) findViewById(R.id.aws_connect);
        if (connectBtn != null) {
            connectBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startAWSIoT();
                }
            });
        }

        Button sendBtn = (Button) findViewById(R.id.aws_send);
        if (sendBtn != null) {
            sendBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }

        Button p2pBtn = (Button) findViewById(R.id.aws_p2p);
        if (p2pBtn != null) {
            p2pBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    connectP2P();
                }
            });
        }
    }

    private void startAWSIoT() {
        mAWSIotRemoteManager = new AWSIotRemoteManager(this);
        mAWSIotRemoteManager.connectAWSIoT("ACCESS_KEY", "SECRET_KEY", Regions.AP_NORTHEAST_1);
    }

    private void connectP2P() {
        mAWSIotRemoteManager.createWebServer(mRemoteManager, "localhost:4035");
    }

    public void log(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tv = (TextView) findViewById(R.id.text_view);
                if (tv != null) {
                    String text = tv.getText().toString();
                    text = message + "\n" + text;
                    tv.setText(text);
                }
            }
        });
    }
}
