package org.deviceconnect.android.deviceplugin.host.recorder.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.deviceconnect.android.deviceplugin.host.R;

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
        LayoutInflater.from(context).inflate(R.layout.host_preview_overlay, this);
    }

    /**
     * Surface のサイズを画面のサイズに合わせて調整します.
     *
     * @param isSwappedDimensions 縦横の切り替えフラグ
     */
    public void adjustSurfaceView(boolean isSwappedDimensions, Size previewSize) {
        post(() -> {
            View root = findViewById(R.id.preview_root);

            SurfaceView surfaceView = root.findViewById(R.id.preview_surface_view);
            int cameraWidth = isSwappedDimensions ? previewSize.getHeight() : previewSize.getWidth();
            int cameraHeight = isSwappedDimensions ? previewSize.getWidth() : previewSize.getHeight();
            Size viewSize = new Size(root.getWidth(), root.getHeight());
            Size changeSize = calculateViewSize(cameraWidth, cameraHeight, viewSize);

            ViewGroup.LayoutParams layoutParams = surfaceView.getLayoutParams();
            layoutParams.width = changeSize.getWidth();
            layoutParams.height = changeSize.getHeight();
            surfaceView.setLayoutParams(layoutParams);

            surfaceView.getHolder().setFixedSize(previewSize.getWidth(), previewSize.getHeight());
        });
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
