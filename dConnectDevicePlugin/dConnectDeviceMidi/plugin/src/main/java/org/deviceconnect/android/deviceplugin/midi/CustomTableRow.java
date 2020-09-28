/*
 CustomTableRow.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.midi;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * テーブル行表示.
 *
 * @author NTT DOCOMO, INC.
 */
public class CustomTableRow extends TableRow {

    private String mItemTitle;

    private String mItemContent;

    public CustomTableRow(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.service_detail_table_row, this, true);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CustomTableRow,
                0, 0);

        try {
            mItemTitle = a.getString(R.styleable.CustomTableRow_itemTitle);
            mItemContent = a.getString(R.styleable.CustomTableRow_itemContent);
            setItemTitle(mItemTitle);
            setItemContent(mItemContent);
        } finally {
            a.recycle();
        }
    }

    public String getItemTitle() {
        return mItemTitle;
    }

    public void setItemTitle(final String itemTitle) {
        mItemTitle = itemTitle;
        ((TextView) findViewById(R.id.item_title)).setText(itemTitle);
        invalidate();
        requestLayout();
    }

    public String getItemContent() {
        return mItemContent;
    }

    public void setItemContent(final String itemContent) {
        mItemContent = itemContent;
        ((TextView) findViewById(R.id.item_content)).setText(itemContent);
        invalidate();
        requestLayout();
    }
}
