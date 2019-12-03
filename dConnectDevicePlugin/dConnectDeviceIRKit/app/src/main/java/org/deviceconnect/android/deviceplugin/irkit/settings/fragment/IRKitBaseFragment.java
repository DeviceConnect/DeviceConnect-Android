/*
 IRKitBaseFragment.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.irkit.settings.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import org.deviceconnect.android.deviceplugin.irkit.R;
import org.deviceconnect.android.deviceplugin.irkit.settings.activity.IRKitSettingActivity;

/**
 * 設定画面のベース.
 * @author NTT DOCOMO, INC.
 */
public class IRKitBaseFragment extends Fragment {
    
    /**
     * インジケーター.
     */
    private AlertDialog mIndView;
    
    @Override
    public View onCreateView(final LayoutInflater inflater, 
            final ViewGroup container, 
            final Bundle savedInstanceState) {
        setRetainInstance(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View v = inflater.inflate(R.layout.dialog_progress, null);
        TextView titleView = v.findViewById(R.id.title);
        TextView messageView = v.findViewById(R.id.message);
        titleView.setText(getString(R.string.ind_message_prepare_title));
        messageView.setText(getString(R.string.ind_message_prepare));
        mIndView = builder.setView(v).create();

        return null;
    }

    /**
     * 画面が表示された場合のコールバック.
     */
    public void onAppear() {
    }
    
    /**
     * 画面が非表示になった場合のコールバック.
     */
    public void onDisapper() {
    }
    
    /**
     * フォアグラウンドになった場合に通知される.
     */
    public void onEnterForeground() {
    }
    
    /**
     * バックグラウンドになった場合に通知される.
     */
    public void onEnterBackground() {
    }
    
    /**
     * アラートダイアログを表示する.
     * 
     * @param title タイトル
     * @param message メッセージ
     * @param closeBtn 閉じるボタン
     * @param listener リスナー
     */
    protected void showAlert(final int title, final int message, final int closeBtn,
            final android.content.DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(getString(closeBtn), listener);
        builder.setCancelable(false);
        builder.show();
    }
    
    /**
     * スクロールの有無.
     * 
     * @param enable 有無
     */
    protected void switchViewEnable(final boolean enable) {
        Activity a = getActivity();
        if (a != null && a instanceof IRKitSettingActivity) {
            ((IRKitSettingActivity) a).setTouchEnable(enable);
        }
    }
    
    /**
     * インジケーターを表示する.
     */
    protected void showProgress() {
        if (mIndView == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View v = inflater.inflate(R.layout.dialog_progress, null);
            TextView titleView = v.findViewById(R.id.title);
            TextView messageView = v.findViewById(R.id.message);
            titleView.setText(getString(R.string.ind_message_prepare_title));
            messageView.setText(getString(R.string.ind_message_prepare));
            mIndView = builder.setView(v).create();
            mIndView.setCancelable(false);
        }
        mIndView.show();
    }

    /**
     * インジケーターを閉じる.
     */
    protected void closeProgress() {
        if (mIndView != null) {
            mIndView.dismiss();
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mIndView = null;
    }
}
