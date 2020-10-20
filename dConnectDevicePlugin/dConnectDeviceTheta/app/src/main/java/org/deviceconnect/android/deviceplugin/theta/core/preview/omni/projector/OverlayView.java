package org.deviceconnect.android.deviceplugin.theta.core.preview.omni.projector;


import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import org.deviceconnect.android.deviceplugin.theta.core.SphericalViewRenderer;

public class OverlayView extends ViewGroup {

    private final GLSurfaceView mSurfaceView;

    private SphericalViewRenderer mRenderer;

    public OverlayView(final Context context) {
        super(context);

        mSurfaceView = new GLSurfaceView(context);
        mSurfaceView.setEGLContextClientVersion(2);
    }

    public OverlayView(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        mSurfaceView = new GLSurfaceView(context);
        mSurfaceView.setEGLContextClientVersion(2);
    }

    public SphericalViewRenderer getRenderer() {
        return mRenderer;
    }

    public void setRenderer(final SphericalViewRenderer renderer) {
        mRenderer = renderer;
        mSurfaceView.setRenderer(mRenderer);
        addView(mSurfaceView);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        // We purposely disregard child measurements because act as a
        // wrapper to a SurfaceView that centers the camera preview instead
        // of stretching it.
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b) {
        if (changed && getChildCount() > 0) {
            final View child = getChildAt(0);

            final int width = r - l;
            final int height = b - t;

            int previewWidth = width;
            int previewHeight = height;

            // Center the child SurfaceView within the parent.
            if (width * previewHeight > height * previewWidth) {
                final int scaledChildWidth = previewWidth * height / previewHeight;
                child.layout((width - scaledChildWidth) / 2, 0, (width + scaledChildWidth) / 2, height);
            } else {
                final int scaledChildHeight = previewHeight * width / previewWidth;
                child.layout(0, (height - scaledChildHeight) / 2, width, (height + scaledChildHeight) / 2);
            }
        }
    }

}
