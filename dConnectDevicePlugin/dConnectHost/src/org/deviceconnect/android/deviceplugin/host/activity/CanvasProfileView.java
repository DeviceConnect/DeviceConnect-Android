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

    /**
     * paint(default).
     */
    private Paint mPaint;
    
    /**
     * paint(for canvas clear).
     */
    private Paint mPaintForCanvasClear;
    
    /**
     * bitmap.
     */
    private Bitmap mBitmap;
    
    /**
     * clear canvas flag.
     */
    private boolean mIsClearCanvas = true;
    
    /**
     * canvas draw object.
     */
    private CanvasDrawObjectInterface mCanvasDrawObject;
    
    /**
     * constructor for layout xml.
     * @param context context
     * @param attrs attributes
     */
    public CanvasProfileView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        setFocusable(true);
        initPaint();
    }
    
    /**
     * set draw object.
     * @param canvasDrawObject draw object.
     * @param isClearCanvas 
     */
    public void setDrawObject(final CanvasDrawObjectInterface canvasDrawObject, final boolean isClearCanvas) {
        this.mCanvasDrawObject = canvasDrawObject;
        redraw(isClearCanvas);
    }
    
    /**
     * init paint.
     */
    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFFFF0000);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);
        
        mPaintForCanvasClear = new Paint();
        mPaintForCanvasClear.setStyle(Style.FILL);
        mPaintForCanvasClear.setColor(Color.WHITE);
    }
    
    /**
     * redraw.
     * @param isClearCanvas 
     */
    private void redraw(final boolean isClearCanvas) {
        this.mIsClearCanvas = isClearCanvas;
        
        if (mBitmap == null) {
            return;
        }
        
        if (isClearCanvas) {
            clearCanvas(mBitmap);
        }
        
        if (mCanvasDrawObject != null) {
            mCanvasDrawObject.draw(mBitmap);
        }
        
        invalidate();
    }
    
    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        redraw(mIsClearCanvas);
    }
    
    @Override
    protected void onDraw(final Canvas canvas) {
        canvas.drawBitmap(mBitmap, 0, 0, mPaint);
    }
    
    /**
     * Clear canvas.
     * @param bitmap bitmap
     */
    private void clearCanvas(final Bitmap bitmap) {
        Canvas canvas = new Canvas(bitmap);
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), mPaintForCanvasClear);
    }

}
