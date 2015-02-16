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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

/**
 * Canvas Profile Activity.
 * 
 * @author NTT DOCOMO, INC.
 */
public class CanvasProfileActivity extends Activity {

    /**
     * background color.
     */
    private static final int CANVAS_BACKGROUND_COLOR = Color.WHITE; 

    /**
     * Close button object.
     */
    private Button mCloseButton;
    
    /**
     * Canvas view object.
     */
    private ImageView mCanvasView;
    
    /**
     * Canvas draw object.
     */
    private CanvasDrawObjectInterface mCanvasDraw;
    
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canvas_profile);

        mCloseButton = (Button) findViewById(R.id.buttonClose);
        mCanvasView = (ImageView) findViewById(R.id.canvasProfileView);

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
    public void onWindowFocusChanged(final boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        refreshImageView();
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        refreshImage(intent);
    }

    /**
     * refresh image.
     * @param intent Intent
     */
    private void refreshImage(final Intent intent) {
        mCanvasDraw = CanvasDrawUtils.getCanvasDrawObjectFromIntent(intent);
        if (mCanvasView.getWidth() > 0 && mCanvasView.getHeight() > 0) {
            refreshImageView();
        }
    }
    
    /**
     * refresh image view.
     */
    private void refreshImageView() {
        Bitmap viewBitmap = Bitmap.createBitmap(mCanvasView.getWidth(), mCanvasView.getHeight(), Bitmap.Config.RGB_565);
        drawClearBackground(viewBitmap);
        mCanvasDraw.draw(viewBitmap);
        mCanvasView.setImageBitmap(viewBitmap);
    }
    
    /**
     * fill backbroundcolor to viewBitmap.
     * @param viewBitmap viewBitmap
     */
    private void drawClearBackground(final Bitmap viewBitmap) {
        Canvas canvas = new Canvas(viewBitmap);
        Rect rect = new Rect(0, 0, viewBitmap.getWidth(), viewBitmap.getHeight());
        Paint paint = new Paint();
        paint.setColor(CANVAS_BACKGROUND_COLOR);
        paint.setStyle(Style.FILL);
        canvas.drawRect(rect, paint);
    }
}
