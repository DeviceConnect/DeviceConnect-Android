/*
 HoldableViewPager.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.irkit.settings.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

/**
 * スクロールを制限できるViewPager.
 * @author NTT DOCOMO, INC.
 */
public class HoldableViewPager extends ViewPager {
    
    
    /** 
     * スクロールできるかのフラグ.
     */
    private boolean mScrollable;

    /**
     * コンストラクタ.
     * 
     * @param context コンテキスト
     */
    public HoldableViewPager(final Context context) {
        super(context);
        mScrollable = true;
    }

    /**
     * コンストラクタ.
     * 
     * @param context コンテキスト
     * @param attrs 属性値
     */
    public HoldableViewPager(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        mScrollable = true;
    }

    @SuppressLint("ClickableViewAccessibility")
	@Override
    public boolean onTouchEvent(final MotionEvent event) {
        
        if (!mScrollable) {
            return false;
        }
        
        return super.onTouchEvent(event);
    }
 
    @Override
    public boolean onInterceptTouchEvent(final MotionEvent event) {
        if (!mScrollable) {
            return false;
        }
        return super.onInterceptTouchEvent(event);
    }

    /**
     * スクロールの制限を設定する.
     * 
     * @param s 制限
     */
    public void setScrollable(final boolean s) {
        this.mScrollable = s;
    }
}
