package org.deviceconnect.android.deviceplugin.uvc.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.deviceconnect.android.deviceplugin.uvc.R;

public class PreviewSurfaceView extends FrameLayout {

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

    private void initView(Context context, AttributeSet attrs, int defStyle) {
        LayoutInflater.from(context).inflate(R.layout.surface_view_preview, this);
    }

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

            int width = widthRatio * previewGcd;
            int height = heightRatio * previewGcd;

            ViewGroup.LayoutParams layoutParams = surfaceView.getLayoutParams();
            layoutParams.width = width;
            layoutParams.height = height;
            surfaceView.setLayoutParams(layoutParams);
            surfaceView.getHolder().setFixedSize(previewWidth, previewHeight);
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
        });
    }

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
