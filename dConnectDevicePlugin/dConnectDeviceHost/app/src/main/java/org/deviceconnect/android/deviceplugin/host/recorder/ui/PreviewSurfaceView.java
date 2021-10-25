package org.deviceconnect.android.deviceplugin.host.recorder.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.recorder.CropInterface;

import java.util.HashMap;
import java.util.Map;

public class PreviewSurfaceView extends FrameLayout {
    private int mPreviewWidth;
    private int mPreviewHeight;
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    private int mDragStartX;
    private int mDragStartY;
    private boolean mDragFlag;
    private boolean mScaleFlag;
    private ScaleGestureDetector mScaleGestureDetector;
    private final Map<Object, CropRectHolder> mCropRectMap = new HashMap<>();

    public PreviewSurfaceView(Context context) {
        super(context);
    }

    public PreviewSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public PreviewSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.host_preview_surface_view, this);

        mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                CropRectHolder holder = getFocusedHolder();
                if (holder == null) {
                    return false;
                }

                Rect cropRect = holder.mCropRect;
                Object tag = holder.mTag;

                float scaleFactor = Math.max(0.1f, Math.min(detector.getScaleFactor(), 5.0f));
                float newWidth = cropRect.width() * scaleFactor;
                float newHeight = cropRect.height() * scaleFactor;

                if (mPreviewWidth < newWidth) {
                    newWidth = mPreviewWidth;
                }

                if (mPreviewHeight < newHeight) {
                    newHeight = mPreviewHeight;
                }

                int diffW = (int) ((newWidth - cropRect.width()) / 2.0f);
                int diffH = (int) ((newHeight - cropRect.height()) / 2.0f);

                int newLeft = cropRect.left - diffW;
                int newRight = cropRect.right + diffW;
                int newTop = cropRect.top - diffH;
                int newBottom = cropRect.bottom + diffH;

                cropRect.set(newLeft, newTop, newRight, newBottom);

                int diffX = 0;
                int diffY = 0;
                if (newLeft < 0) {
                    diffX = -cropRect.left;
                }

                if (newRight >= mPreviewWidth) {
                    diffX = mPreviewWidth - cropRect.right;
                }

                if (newTop < 0) {
                    diffY = -cropRect.top;
                }

                if (newBottom >= mPreviewHeight) {
                    diffY = mPreviewHeight - cropRect.bottom;
                }

                cropRect.offset(diffX, diffY);
                onChangedCropRect(tag, cropRect);
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                mScaleFlag = true;
                mDragFlag = false;
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                mScaleFlag = false;
            }
        });

        SurfaceView surfaceView = getSurfaceView();
        surfaceView.setOnTouchListener((view, event) -> {
            mScaleGestureDetector.onTouchEvent(event);

            if (mScaleFlag || event.getPointerCount() > 1) {
                return true;
            }

            final int[] anchorPos = new int[2];
            view.getLocationOnScreen(anchorPos);

            float scale = getScale();
            int x = (int) event.getRawX() - anchorPos[0];
            int y = (int) event.getRawY() - anchorPos[1];
            int orgX = (int) (x / scale);
            int orgY = (int) (y / scale);

            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                {
                    for (CropRectHolder holder : mCropRectMap.values()) {
                        Rect cropRect = holder.mCropRect;
                        if (cropRect.contains(orgX, orgY)) {
                            clearFocusCropRect();
                            mDragStartX = x;
                            mDragStartY = y;
                            mDragFlag = true;
                            holder.mFocused = true;
                            break;
                        }
                    }
                }   break;

                case MotionEvent.ACTION_MOVE:
                {
                    CropRectHolder holder = getFocusedHolder();
                    if (mDragFlag && holder != null) {
                        Rect cropRect = holder.mCropRect;
                        Object tag = holder.mTag;

                        int diffX = (int) ((x - mDragStartX) / scale);
                        int diffY = (int) ((y - mDragStartY) / scale);

                        int newLeft = cropRect.left + diffX;
                        int newRight = cropRect.right + diffX;
                        int newTop = cropRect.top + diffY;
                        int newBottom = cropRect.bottom + diffY;

                        if (newLeft < 0) {
                            diffX = -cropRect.left;
                        }

                        if (newRight >= mPreviewWidth) {
                            diffX = mPreviewWidth - cropRect.right;
                        }

                        if (newTop < 0) {
                            diffY = -cropRect.top;
                        }

                        if (newBottom >= mPreviewHeight) {
                            diffY = mPreviewHeight - cropRect.bottom;
                        }

                        cropRect.offset(diffX, diffY);
                        onChangedCropRect(tag, cropRect);

                        mDragStartX = x;
                        mDragStartY = y;
                    }
                }   break;

                case MotionEvent.ACTION_UP:
                    mDragFlag = false;
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

    /**
     * プレビューの表示を行う SurfaceView を取得します.
     *
     * @return SurfaceView
     */
    public SurfaceView getSurfaceView() {
        View root = findViewById(R.id.preview_root);
        if (root != null) {
            return root.findViewById(R.id.preview_surface_view);
        }
        return null;
    }

    /**
     * 切り抜き範囲の枠を追加します.
     *
     * 既に同じキーが登録されている場合は、切り抜き範囲を変更します。
     *
     * @param key 切り抜き範囲の枠を識別するキー
     * @param cropRect 切り抜き範囲の枠
     */
    public void addCropRect(Object key, Rect cropRect) {
        if (key == null || cropRect == null) {
            return;
        }

        post(() -> {
            CropRectHolder holder = mCropRectMap.get(key);
            if (holder == null) {
                holder = new CropRectHolder();
                holder.mTag = key;
                holder.mCropRect = cropRect;
                holder.mView = inflate(getContext(), R.layout.item_crop_frame, null);

                TextView tv = holder.mView.findViewById(R.id.textview);
                if (tv != null && key instanceof CropInterface) {
                    tv.setText(((CropInterface) key).getName());
                }

                ConstraintLayout constraintLayout = findViewById(R.id.preview_root);
                constraintLayout.addView(holder.mView);

                // addView を行った後でないと LayoutParams が null になるので注意
                ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) holder.mView.getLayoutParams();
                layoutParams.leftToLeft = R.id.preview_surface_view;
                layoutParams.topToTop = R.id.preview_surface_view;
                holder.mView.setLayoutParams(layoutParams);

                mCropRectMap.put(key, holder);
            } else {
                holder.mCropRect = cropRect;
            }

            setCropRectView(holder);
        });
    }

    /**
     * 切り抜き範囲の枠を削除します.
     *
     * 対応する切り抜き範囲の枠が存在しない場合は何も処理を行いません。
     *
     * @param key 切り抜き範囲の枠を識別するキー
     */
    public void removeCropRange(Object key) {
        post(() -> {
            CropRectHolder holder = mCropRectMap.remove(key);
            if (holder != null && holder.mView != null) {
                ConstraintLayout constraintLayout = findViewById(R.id.preview_root);
                constraintLayout.removeView(holder.mView);
            }
        });
    }

    /**
     * 切り抜き範囲の枠の値が変更された時に呼び出されます.
     *
     * @param key キー
     * @param cropRect 新しい値
     */
    protected void onChangedCropRect(Object key, Rect cropRect) {
        CropRectHolder holder = mCropRectMap.get(key);
        if (holder != null) {
            setCropRectView(holder);
        }
    }

    private void setCropRectView(CropRectHolder holder) {
        Rect cropRect = holder.mCropRect;
        View frameView = holder.mView;
        if (frameView != null) {
            float scale = getScale();
            int left = (int) (cropRect.left * scale);
            int top = (int) (cropRect.top * scale);
            ViewGroup.LayoutParams layoutParams = frameView.getLayoutParams();
            layoutParams.width = (int) (cropRect.width() * scale);
            layoutParams.height = (int) (cropRect.height() * scale);
            MarginLayoutParams mlp = (MarginLayoutParams) layoutParams;
            mlp.setMargins(left, top, 0, 0);
            frameView.setLayoutParams(layoutParams);
            frameView.setVisibility(VISIBLE);
        }
    }

    private void clearFocusCropRect() {
        for (CropRectHolder h : mCropRectMap.values()) {
            h.mFocused = false;
        }
    }

    private CropRectHolder getFocusedHolder() {
        for (CropRectHolder h : mCropRectMap.values()) {
            if (h.mFocused) {
                return h;
            }
        }
        return null;
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
            surfaceView.getHolder().setFixedSize(mSurfaceWidth, mSurfaceHeight);
//            surfaceView.getHolder().setFixedSize(previewWidth, previewHeight);
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

            mPreviewWidth = previewWidth;
            mPreviewHeight = previewHeight;
            mSurfaceWidth = changeSize.getWidth();
            mSurfaceHeight = changeSize.getHeight();

            ViewGroup.LayoutParams layoutParams = surfaceView.getLayoutParams();
            layoutParams.width = mSurfaceWidth;
            layoutParams.height = mSurfaceHeight;
            surfaceView.setLayoutParams(layoutParams);
            surfaceView.getHolder().setFixedSize(mSurfaceWidth, mSurfaceHeight);
//            surfaceView.getHolder().setFixedSize(previewWidth, previewHeight);
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

    private static class CropRectHolder {
        private Object mTag;
        private Rect mCropRect;
        private View mView;
        private boolean mFocused;
    }
}
