/*
 CanvasProfileView.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.host.activity;

import org.deviceconnect.android.deviceplugin.host.canvas.CanvasDrawObjectInterface;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Canvas Profile View.
 * 
 * @author NTT DOCOMO, INC.
 */
public class CanvasProfileView extends View {

    private Paint paint = null;
    private Bitmap bitmap = null;

    /**
     * canvas draw object.
     */
    private CanvasDrawObjectInterface canvasDrawObject = null;
    
    /**
     * constructor for layout xml.
     * @param context context
     * @param attrs attributes
     */
    public CanvasProfileView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusable(true);
        initPaint();
    }
    
    /**
     * set draw object.
     * @param canvasDrawObject draw object.
     */
    public void setDrawObject(CanvasDrawObjectInterface canvasDrawObject) {
        this.canvasDrawObject = canvasDrawObject;
        redraw();
    }
    
    /**
     * init paint.
     */
    private void initPaint(){
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(0xFFFF0000);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(12);
    }
    
    /**
     * redraw.
     */
    private void redraw() {
        
        if (bitmap == null) {
            return;
        }
        
        if (canvasDrawObject != null) {
            canvasDrawObject.draw(bitmap);
        }
        
        invalidate();
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        redraw();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(bitmap, 0, 0, paint);
    }
    
}
