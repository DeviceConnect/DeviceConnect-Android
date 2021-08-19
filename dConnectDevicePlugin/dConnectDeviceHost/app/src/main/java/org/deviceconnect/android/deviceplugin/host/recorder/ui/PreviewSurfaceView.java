package org.deviceconnect.android.deviceplugin.host.recorder.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.deviceconnect.android.deviceplugin.host.R;

public class PreviewSurfaceView extends FrameLayout {
    private Rect mDrawingRect;
    private int mPreviewWidth;
    private int mPreviewHeight;
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    private int mDragStartX;
    private int mDragStartY;

    public PreviewSurfaceView(Context context) {
        super(context);
    }

    public PreviewSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs, 0);
    }

    public PreviewSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs, defStyleAttr);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView(Context context, AttributeSet attrs, int defStyle) {
        LayoutInflater.from(context).inflate(R.layout.host_preview_surface_view, this);

        SurfaceView surfaceView = getSurfaceView();
        surfaceView.setOnTouchListener((v, event) -> {
            Rect drawingRect = getDrawingRect();
            if (drawingRect == null) {
                return false;
            }

            float scale = getScale();
            int x = (int) event.getRawX();
            int y = (int) event.getRawY();
            int orgX = (int) (x / scale);
            int orgY = (int) (y / scale);

            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (drawingRect.contains(orgX, orgY)) {
                        mDragStartX = x;
                        mDragStartY = y;
                    } else {
                        return false;
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    int diffX = (int) ((x - mDragStartX) / scale);
                    int diffY = (int) ((y - mDragStartY) / scale);

                    int newLeft = drawingRect.left + diffX;
                    int newRight = drawingRect.right + diffX;
                    int newTop = drawingRect.top + diffY;
                    int newBottom = drawingRect.bottom + diffY;

                    if (newLeft < 0) {
                        diffX = -drawingRect.left;
                    }

                    if (newRight >= mPreviewWidth) {
                        diffX = mPreviewWidth - drawingRect.right;
                    }

                    if (newTop < 0) {
                        diffY = -drawingRect.top;
                    }

                    if (newBottom >= mPreviewHeight) {
                        diffY = mPreviewHeight - drawingRect.bottom;
                    }

                    drawingRect.offset(diffX, diffY);
                    mDragStartX = x;
                    mDragStartY = y;
                    setDrawingRange(drawingRect);
                    break;

                case MotionEvent.ACTION_UP:
                    break;
            }
            return true;
        });
    }

    public float getScale() {
        if (mPreviewHeight == 0) {
            return 1.0f;
        } else {
            return mSurfaceHeight / (float) mPreviewHeight;
        }
    }

    public SurfaceView getSurfaceView() {
        View root = findViewById(R.id.preview_root);
        if (root != null) {
            return root.findViewById(R.id.preview_surface_view);
        }
        return null;
    }

    public void setDrawingRange(Rect drawingRange) {
        mDrawingRect = drawingRange;
        setDrawingRangeView(drawingRange);
    }

    public Rect getDrawingRect() {
        return mDrawingRect;
    }

    private void setDrawingRangeView(Rect drawingRange) {
        View root = findViewById(R.id.preview_root);
        View frameView = root.findViewById(R.id.preview_drawing_range);
        if (drawingRange == null || mPreviewHeight == 0) {
            frameView.setVisibility(GONE);
        } else {
            float scale = getScale();
            int left = (int) (drawingRange.left * scale);
            int top = (int) (drawingRange.top * scale);
            ViewGroup.LayoutParams layoutParams = frameView.getLayoutParams();
            layoutParams.width = (int) (drawingRange.width() * scale);
            layoutParams.height = (int) (drawingRange.height() * scale);
            MarginLayoutParams mlp = (MarginLayoutParams) layoutParams;
            mlp.setMargins(left, top, 0, 0);
            frameView.setLayoutParams(layoutParams);
            frameView.setVisibility(VISIBLE);
        }
    }

    /**
     * アスペクトを保持したまま画面全体にプレビューを表示するように調整します.
     *
     * @param previewWidth プレビューの横幅
     * @param previewHeight プレビューの縦幅
     */
    public void fullSurfaceView(int previewWidth, int previewHeight) {
        post(() -> {
            int gcd = calculatedGcd(previewWidth, previewHeight);

            View root = findViewById(R.id.preview_root);
            SurfaceView surfaceView = root.findViewById(R.id.preview_surface_view);

            int widthRatio = previewWidth / gcd;
            int heightRatio = previewHeight / gcd;

            int largeRate = widthRatio;
            if (widthRatio < heightRatio) {
                largeRate = heightRatio;
            }

            int largeSize = root.getWidth();
            if (root.getWidth() < root.getHeight()) {
                largeSize = root.getHeight();
            }

            int previewGcd = (int) Math.ceil(largeSize / (double) largeRate);

            mPreviewWidth = previewWidth;
            mPreviewHeight = previewHeight;
            mSurfaceWidth = widthRatio * previewGcd;
            mSurfaceHeight = heightRatio * previewGcd;

            ViewGroup.LayoutParams layoutParams = surfaceView.getLayoutParams();
            layoutParams.width = mSurfaceWidth;
            layoutParams.height = mSurfaceHeight;
            surfaceView.setLayoutParams(layoutParams);
            surfaceView.getHolder().setFixedSize(previewWidth, previewHeight);

            setDrawingRangeView(mDrawingRect);
        });
    }

    /**
     * Surface のサイズを画面のサイズに収まるように合わせて調整します.
     *
     * @param previewWidth プレビューの横幅
     * @param previewHeight プレビューの縦幅
     */
    public void adjustSurfaceView(int previewWidth, int previewHeight) {
        post(() -> {
            View root = findViewById(R.id.preview_root);

            SurfaceView surfaceView = root.findViewById(R.id.preview_surface_view);
            Size viewSize = new Size(root.getWidth(), root.getHeight());
            Size changeSize = calculateViewSize(previewWidth, previewHeight, viewSize);

            ViewGroup.LayoutParams layoutParams = surfaceView.getLayoutParams();
            layoutParams.width = changeSize.getWidth();
            layoutParams.height = changeSize.getHeight();
            surfaceView.setLayoutParams(layoutParams);
            surfaceView.getHolder().setFixedSize(previewWidth, previewHeight);

            mPreviewWidth = previewWidth;
            mPreviewHeight = previewHeight;
            mSurfaceWidth = changeSize.getWidth();
            mSurfaceHeight = changeSize.getHeight();

            setDrawingRangeView(mDrawingRect);
        });
    }

    /**
     * a と b の最大公約数を計算します.
     * @param a 値1
     * @param b 値2
     * @return a と b の最大公約数
     */
    private int calculatedGcd(int a, int b) {
        if (b == 0) {
            return a;
        } else {
            return calculatedGcd(b, a % b);
        }
    }

    /**
     * 指定された View のサイズにフィットするサイズを計算します.
     *
     * @param width 横幅
     * @param height 縦幅
     * @param viewSize View のサイズ
     * @return View にフィットするサイズ
     */
    private Size calculateViewSize(int width, int height, Size viewSize) {
        int h =  (int) (height * (viewSize.getWidth() / (float) width));
        if (viewSize.getHeight() < h) {
            int w = (int) (width * (viewSize.getHeight() / (float) height));
            if (w % 2 != 0) {
                w--;
            }
            return new Size(w, viewSize.getHeight());
        }
        return new Size(viewSize.getWidth(), h);
    }
}
