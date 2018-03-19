/*
SonyCameraBaseFragment
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sonycamera.activity;

import org.deviceconnect.android.deviceplugin.sonycamera.R;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * Sony Camera ベースフラグメント.
 * @author NTT DOCOMO, INC.
 */
public class SonyCameraBaseFragment extends Fragment {

    /** 検索中のダイアログ. */
    private AlertDialog mDialog;

    /**
     * サービスIDを設定する.
     * 
     * @param id デバイスプラグインID
     */
    public void setServiceId(final String id) {
        ((SonyCameraSettingActivity) getActivity()).setServiceId(id);
    }

    /**
     * サービスIDを取得する.
     * 
     * @return サービスID
     */
    public String getServiceId() {
        return ((SonyCameraSettingActivity) getActivity()).getServiceId();
    }

    /**
     * プログレスバーが表示されているか.
     * 
     * @return 表示されている場合はtrue,それ以外はfalse
     */
    public boolean isShowProgressDialog() {
        return mDialog != null;
    }

    /**
     * プログレスバーを表示する.
     */
    public void showProgressDialog() {
        if (mDialog != null) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_progress, null);
        TextView titleView = v.findViewById(R.id.title);
        TextView messageView = v.findViewById(R.id.message);
        titleView.setText(getString(R.string.sonycamera_proccessing));
        messageView.setText(getString(R.string.sonycamera_now_loading));
        builder.setView(v);

        mDialog = builder.create();
        mDialog.setCancelable(true);
        mDialog.show();
    }

    /**
     * プログレスバーを非表示にする.
     */
    public void dismissProgressDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }
}
