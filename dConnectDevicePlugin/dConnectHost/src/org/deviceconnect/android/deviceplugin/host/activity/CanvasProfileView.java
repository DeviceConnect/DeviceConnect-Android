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
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

/**
 * Canvas Profile View.
 * 
 * @author NTT DOCOMO, INC.
 */
public class CanvasProfileView extends View {

    private Paint paint = null;
    private Paint paintForCanvasClear = null;
    private Bitmap bitmap = null;
    private boolean isClearCanvas = true;
    
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
     * @param isClearCanvas 
     */
    public void setDrawObject(CanvasDrawObjectInterface canvasDrawObject, boolean isClearCanvas) {
        this.canvasDrawObject = canvasDrawObject;
        redraw(isClearCanvas);
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
        
        paintForCanvasClear = new Paint();
        paintForCanvasClear.setStyle(Style.FILL);
        paintForCanvasClear.setColor(Color.WHITE);
    }
    
    /**
     * redraw.
     * @param isClearCanvas 
     */
    private void redraw(boolean isClearCanvas) {
        this.isClearCanvas = isClearCanvas;
        
        if (bitmap == null) {
            return;
        }
        
        if (isClearCanvas) {
            clearCanvas(bitmap);
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
        redraw(isClearCanvas);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(bitmap, 0, 0, paint);
    }
    
    /**
     * Clear canvas.
     * @param bitmap bitmap
     */
    private void clearCanvas(Bitmap bitmap) {
        Canvas canvas = new Canvas(bitmap);
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paintForCanvasClear);
    }

}
