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
    
    /**
     * Intent ACTION (ready receive draw request).
     */
    public static final String ACTION_READY_RECEIVE_DRAW_REQUEST = "ACTION_READY_RECEIVE_DRAW_REQUEST";
    
    /**
     * Intent ACTION (draw to canvas).
     */
    public static final String ACTION_DRAW_TO_CANVAS = "ACTION_DRAW_TO_CANVAS";
    
    /**
     * Close button object.
     */
    private Button mCloseButton;
    
    /**
     * Canvas view object.
     */
    private CanvasProfileView mCanvasView;
    
    /**
     * Canvas draw object.
     */
    private CanvasDrawObjectInterface mCanvasDraw;
    
    
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canvas_profile);
        
        mCloseButton = (Button) findViewById(R.id.buttonClose);
        mCanvasView = (CanvasProfileView) findViewById(R.id.canvasProfileView);
        
        mCloseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                finish();
            }
        });
        
        // Initialize draw request receiver.
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                
                if (intent.getAction().equals(ACTION_DRAW_TO_CANVAS)) {
                    mCanvasDraw = CanvasDrawUtils.getCanvasDrawObjectFromIntent(intent);
                    if (mCanvasDraw != null) {
                        mCanvasView.setDrawObject(mCanvasDraw, true);
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
