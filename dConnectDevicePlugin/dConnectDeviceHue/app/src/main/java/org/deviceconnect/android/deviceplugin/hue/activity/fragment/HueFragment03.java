/*
HueFargment03
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.hue.activity.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.philips.lighting.hue.sdk.PHAccessPoint;

import org.deviceconnect.android.deviceplugin.hue.R;

/**
 * Hue設定画面(3)フラグメント.
 */
public class HueFragment03 extends Fragment implements OnClickListener {

    /** 接続したアクセスポイント. */
    private PHAccessPoint mAccessPoint;

    public static HueFragment03 newInstance(final PHAccessPoint accessPoint) {
        HueFragment03 fragment = new HueFragment03();
        fragment.setPHAccessPoint(accessPoint);
        return fragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.hue_fragment_03, container, false);
        if (rootView != null) {
            Button btn = rootView.findViewById(R.id.btnSearchLight);
            btn.setOnClickListener(this);
        }
        return rootView;
    }

    @Override
    public void onClick(final View view) {
        if (view.getId() == R.id.btnSearchLight) {
            moveNextFragment();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void setPHAccessPoint(final PHAccessPoint accessPoint) {
        mAccessPoint = accessPoint;
    }

    private void moveNextFragment() {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(R.anim.fragment_slide_right_enter, R.anim.fragment_slide_left_exit,
                R.anim.fragment_slide_left_enter, R.anim.fragment_slide_right_exit);
        transaction.replace(R.id.fragment_frame, HueFragment04.newInstance(mAccessPoint));
        transaction.commit();
    }
}
