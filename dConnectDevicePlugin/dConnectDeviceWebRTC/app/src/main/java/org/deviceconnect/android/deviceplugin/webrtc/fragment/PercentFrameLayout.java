package org.deviceconnect.android.deviceplugin.webrtc.fragment;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class PercentFrameLayout extends ViewGroup {

    private int xPercent = 0;
    private int yPercent = 0;
    private int widthPercent = 100;
    private int heightPercent = 100;

    public PercentFrameLayout(Context context) {
        super(context);
    }

    public PercentFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PercentFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setPosition(int xPercent, int yPercent, int widthPercent, int heightPercent) {
        this.xPercent = xPercent;
        this.yPercent = yPercent;
        this.widthPercent = widthPercent;
        this.heightPercent = heightPercent;
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = getDefaultSize(Integer.MAX_VALUE, widthMeasureSpec);
        final int height = getDefaultSize(Integer.MAX_VALUE, heightMeasureSpec);

        setMeasuredDimension(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));

        final int childWidthMeasureSpec =
                MeasureSpec.makeMeasureSpec(width * widthPercent / 100, MeasureSpec.AT_MOST);
        final int childHeightMeasureSpec =
                MeasureSpec.makeMeasureSpec(height * heightPercent / 100, MeasureSpec.AT_MOST);

        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int width = right - left;
        final int height = bottom - top;
        final int subWidth = width * widthPercent / 100;
        final int subHeight = height * heightPercent / 100;
        final int subLeft = left + width * xPercent / 100;
        final int subTop = top + height * yPercent / 100;

        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final int childWidth = child.getMeasuredWidth();
                final int childHeight = child.getMeasuredHeight();
                final int childLeft = subLeft + (subWidth - childWidth) / 2;
                final int childTop = subTop + (subHeight - childHeight) / 2;
                child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
            }
        }
    }
}
