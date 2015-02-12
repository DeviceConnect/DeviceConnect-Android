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
import android.content.Intent;
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

        Intent intent = getIntent();
        refreshImage(intent);
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        refreshImage(intent);
    }

    private void refreshImage(final Intent intent) {
        mCanvasDraw = CanvasDrawUtils.getCanvasDrawObjectFromIntent(intent);
        if (mCanvasDraw != null) {
            mCanvasView.setDrawObject(mCanvasDraw, true);
        }
    }
}
