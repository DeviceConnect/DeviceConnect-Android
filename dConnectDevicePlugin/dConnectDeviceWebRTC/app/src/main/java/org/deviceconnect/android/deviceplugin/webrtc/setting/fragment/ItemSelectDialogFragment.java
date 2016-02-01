/*
 ItemSelectDialogFragment.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.webrtc.setting.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import org.deviceconnect.android.deviceplugin.webrtc.R;

import java.util.List;

public class ItemSelectDialogFragment extends DialogFragment {

    private String mTitle;
    private String[] mItems;
    private int mSelected;
    private OnSelectItemListener mOnSelectItemListener;

    public static ItemSelectDialogFragment create(final String title, final String[] items, final int selected) {
        if (title == null) {
            throw new NullPointerException("title is null.");
        }

        if (items == null) {
            throw new NullPointerException("items is null.");
        }

        if (selected < 0 || selected >= items.length) {
            throw new IllegalArgumentException("selected is invalid.");
        }

        ItemSelectDialogFragment dialog = new ItemSelectDialogFragment();
        dialog.setTitle(title);
        dialog.setItems(items);
        dialog.setSelected(selected);
        return dialog;
    }

    public static ItemSelectDialogFragment create(final String title, final List<String> items) {
        return create(title, items, 0);
    }

    public static ItemSelectDialogFragment create(final String title, final List<String> items, final int selected) {
        if (title == null) {
            throw new NullPointerException("title is null.");
        }

        if (items == null) {
            throw new NullPointerException("items is null.");
        }

        String[] str = new String[items.size()];
        items.toArray(str);
        return create(title, str, selected);
    }

    private void setTitle(final String title) {
        mTitle = title;
    }

    private void setItems(final String[] items) {
        mItems = items;
    }

    private void setSelected(final int selected) {
        mSelected = selected;
    }

    public void setOnSelectItemListener(OnSelectItemListener listener) {
        mOnSelectItemListener = listener;
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mTitle);
        builder.setSingleChoiceItems(mItems, mSelected, new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int which) {
                if (mOnSelectItemListener != null) {
                    mOnSelectItemListener.onSelected(mItems[which], which);
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.settings_dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                if (mOnSelectItemListener != null) {
                    mOnSelectItemListener.onCanceled();
                }
                dialog.dismiss();
            }
        });
        return builder.create();
    }

    public interface OnSelectItemListener {
        void onSelected(String text, int which);
        void onCanceled();
    }
}
