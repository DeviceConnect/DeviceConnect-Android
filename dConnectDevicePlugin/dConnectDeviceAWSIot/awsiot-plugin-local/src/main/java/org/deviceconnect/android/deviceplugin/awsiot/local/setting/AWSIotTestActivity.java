package org.deviceconnect.android.deviceplugin.awsiot.local.setting;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amazonaws.regions.Regions;

import org.deviceconnect.android.deviceplugin.awsiot.local.AWSIotDeviceService;
import org.deviceconnect.android.deviceplugin.awsiot.local.AWSIotLocalManager;
import org.deviceconnect.android.deviceplugin.awsiot.local.R;

public class AWSIotTestActivity extends Activity {

    private AWSIotLocalManager mAWSIoTLocalManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

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
                    Intent intent = new Intent();
                    intent.setClass(AWSIotTestActivity.this, AWSIotDeviceService.class);
                    intent.setAction(AWSIotDeviceService.ACTION_START);
                    startService(intent);
                }
            });
        }

        Button p2pBtn = (Button) findViewById(R.id.aws_p2p);
        if (p2pBtn != null) {
            p2pBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setClass(AWSIotTestActivity.this, AWSIotDeviceService.class);
                    intent.setAction(AWSIotDeviceService.ACTION_STOP);
                    startService(intent);
                }
            });
        }
    }

    private void startAWSIoT() {
        mAWSIoTLocalManager = new AWSIotLocalManager(this, "abc", "test");
        mAWSIoTLocalManager.connectAWSIoT("ACCESS_KEY", "SECRET_KEY", Regions.AP_NORTHEAST_1);
        mAWSIoTLocalManager.setOnEventListener(new AWSIotLocalManager.OnEventListener() {
            @Override
            public void onConnected() {
                log("connected");
            }

            @Override
            public void onDisconnected() {
                log("onDisconnected");
            }

            @Override
            public void onReconnecting() {
                log("onReconnecting");
            }

            @Override
            public void onReceivedMessage(String topic, String message) {
                log("topic: " + topic + " message=" + message);
            }
        });
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
