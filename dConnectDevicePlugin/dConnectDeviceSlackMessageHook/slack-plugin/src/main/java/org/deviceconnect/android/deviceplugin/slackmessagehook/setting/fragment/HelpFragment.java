/*
 HelpFragment.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.slackmessagehook.setting.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.slackmessagehook.R;

/**
 * Help画面表示用のFragment
 */
public class HelpFragment extends Fragment {

    /**
     * 画面のタイプ
     */
    private int type = 0;
    /**
     * 回転時の画面切り替え用にrootを保持しておく
     */
    private FrameLayout rootLayout;

    /**
     * 画面のタイプを設定
     *
     * @param type タイプ
     */
    public void setType(int type) {
        this.type = type;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // View作成
        View view = initView();
        rootLayout = new FrameLayout(view.getContext());
        rootLayout.addView(view);
        return rootLayout;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 画面回転時にレイアウトを作り直す
        rootLayout. removeAllViews();
        View view = initView();
        rootLayout.addView(view);
    }

    /**
     * View作成
     *
     * @return View
     */
    private View initView() {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.fragment_help, null);

        TextView titleView = (TextView) view.findViewById(R.id.textHelpTitle);
        TextView textView = (TextView) view.findViewById(R.id.textHelp);
        ImageView imageView = (ImageView) view.findViewById(R.id.imageHelp);

        String text = "";
        int image = 0;
        switch (type) {
            case 0:
                text = getString(R.string.help01);
                image = R.drawable.help01;
                break;
            case 1:
                text = getString(R.string.help02);
                image = R.drawable.help02;
                break;
            case 2:
                text = getString(R.string.help03);
                image = R.drawable.help03;
                break;
            case 3:
                text = getString(R.string.help04);
                image = R.drawable.help04;
                break;
            case 4:
                text = getString(R.string.help05);
                image = R.drawable.help05;
                break;
            case 5:
                text = getString(R.string.help06);
                image = R.drawable.help06;
            default:
        }
        String title = getString(R.string.help_title) + "[" + (type + 1) + "/6]";
        titleView.setText(title);
        textView.setText(text);
        imageView.setImageResource(image);

        return view;
    }
}
