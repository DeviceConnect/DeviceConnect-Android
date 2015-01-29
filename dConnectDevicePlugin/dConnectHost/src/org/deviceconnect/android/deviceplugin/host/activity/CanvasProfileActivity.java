/*
 HostCanvasProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.host.activity;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.canvas.CanvasDrawObjectInterface;
import org.deviceconnect.android.deviceplugin.host.canvas.CanvasDrawUtils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * Canvas Profile Activity.
 * 
 * @author NTT DOCOMO, INC.
 */
public class CanvasProfileActivity extends Activity {
    
    public static final String ACTION_READY_RECEIVE_DRAW_REQUEST = "ACTION_READY_RECEIVE_DRAW_REQUEST";
    public static final String ACTION_DRAW_TO_CANVAS = "ACTION_DRAW_TO_CANVAS";
    
    private Button closeButton = null;
    private CanvasProfileView canvasView = null;
    
    private CanvasDrawObjectInterface canvasDraw = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canvas_profile);
        
        closeButton = (Button) findViewById(R.id.buttonClose);
        canvasView = (CanvasProfileView)findViewById(R.id.canvasProfileView);
        
        closeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        // Initialize draw request receiver.
        BroadcastReceiver receiver = new BroadcastReceiver() {
             @Override
            public void onReceive(Context context, Intent intent) {
                
                if (intent.getAction().equals(ACTION_DRAW_TO_CANVAS)) {
                    canvasDraw = CanvasDrawUtils.getCanvasDrawObjectFromIntent(intent);
                    if (canvasDraw != null) {
                        canvasView.setDrawObject(canvasDraw, true);
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_DRAW_TO_CANVAS);
        registerReceiver(receiver, intentFilter);
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        
        // send ready receiver broadcast to HostCanvasProfile.
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(CanvasProfileActivity.ACTION_READY_RECEIVE_DRAW_REQUEST);
        sendBroadcast(broadcastIntent);
    }
    
}
