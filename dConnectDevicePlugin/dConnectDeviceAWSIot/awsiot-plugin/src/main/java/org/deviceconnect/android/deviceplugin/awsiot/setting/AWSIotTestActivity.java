package org.deviceconnect.android.deviceplugin.awsiot.setting;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amazonaws.regions.Regions;

import org.deviceconnect.android.deviceplugin.awsiot.AWSIotRemoteManager;
import org.deviceconnect.android.deviceplugin.awsiot.core.RemoteDeviceConnectManager;
import org.deviceconnect.android.deviceplugin.awsiot.remote.R;

public class AWSIotTestActivity extends Activity {
    private RemoteDeviceConnectManager mRemoteManager;
    private AWSIotRemoteManager mAWSIotRemoteManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        mRemoteManager = new RemoteDeviceConnectManager("abc", "test");

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
                    mAWSIotRemoteManager.publish(mRemoteManager, "test");
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
        mAWSIotRemoteManager.setOnEventListener(new AWSIotRemoteManager.OnEventListener() {
            @Override
            public void onConnected() {
                log("onConnected");
            }

            @Override
            public void onReconnecting() {
                log("onReconnecting");
            }

            @Override
            public void onDisconnected() {
                log("onDisconnected");
            }

            @Override
            public void onReceivedMessage(String topic, String message) {
                log("topic: " + topic + " message=" + message);
            }
        });
    }

    private void connectP2P() {
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
