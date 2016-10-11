/*
 PebbleSettingActivity.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.pebble.setting;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import org.deviceconnect.android.deviceplugin.pebble.R;
import org.deviceconnect.android.ui.activity.DConnectSettingPageFragmentActivity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Pebbleの設定画面.
 * @author NTT DOCOMO, INC.
 */
public class PebbleSettingActivity extends DConnectSettingPageFragmentActivity {
    /** googleStorId. */
    private static final String PACKAGE_PEBBLE = "com.getpebble.android.basalt";


    /**
     * フラグメント一覧.
     */
    private List<BaseFragment> mFragments = new ArrayList<BaseFragment>();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mFragments.size() == 0) {
            mFragments.add(new BluetoothActivationFragment());
            mFragments.add(new BluetoothSettingPromptFragment());
            mFragments.add(new AppInstrallationFragmentA());
            mFragments.add(new AppInstrallationFragmentP());
            mFragments.add(new SettingFinishFragment());
        }
    }

    @Override
    public int getPageCount() {
        return 5;
    }

    @Override
    public Fragment createPage(final int position) {
        return mFragments.get(position);
    }

    /**
     * BaseFragment クラス.
     *
     */
    public static abstract class BaseFragment extends Fragment {

    }

    /**
     * 手順1 PebbleをBluetooth検出可能にする.
     */
    public static class BluetoothActivationFragment extends BaseFragment {
        @Override
        public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                                       final Bundle savedInstanceState) {
            return inflater.inflate(R.layout.dconnect_settings_step_1, container, false);
        }
    }

    /**
     * 手順2 端末標準の設定画面でPebbleとのペアリングを実行する.
     */
    public static class BluetoothSettingPromptFragment extends BaseFragment {
        @Override
        public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                                      final Bundle savedInstanceState) {
            View root = inflater.inflate(R.layout.dconnect_settings_step_2, container, false);
            Button button = (Button) root.findViewById(R.id.dconnect_settings_step_2_button_launch_bluetooth_setting);
            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View v) {
                    startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
                }
            });
            return root;
        }
    }

    /**
     * 手順3 必須アプリのインストール.
     */
    public static class AppInstrallationFragmentA extends BaseFragment {
        @Override
        public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                                                    final Bundle savedInstanceState) {
            View root = inflater.inflate(R.layout.dconnect_settings_step_3, container, false);
            Button btn = (Button) root.findViewById(R.id.dconnect_settings_step_3_button_install_pebble);
            btn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View v) {
                    Uri uri = Uri.parse("market://details?id=" + PACKAGE_PEBBLE);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            });
            return root;
        }
    }

    /**
     * 手順4 必須アプリのインストール.
     */
    public static class AppInstrallationFragmentP extends BaseFragment {
        @Override
        public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                                                    final Bundle savedInstanceState) {
            View root = inflater.inflate(R.layout.dconnect_settings_step_4, container, false);
            Button btn = (Button) root.findViewById(R.id.dconnect_settings_step_4_button_install_plugin);
            btn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View v) {
                    Activity activity = getActivity();
                    installPebbleApprication(activity, getPbwFileName(activity));
                }
            });
            return root;
        }
    }
    
    /**
     * uri で指定した Pebble側アプリケーションをインストールする.
     * @param uri URI
     */
    private static void installPebbleApprication(final Activity activity, final Uri uri) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/octet-stream");
            activity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, R.string.page04_error01, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * リソースから pbw ファイルを作成し、その uri を返す.
     * @return uri を返す.
     */
    @SuppressLint("WorldReadableFiles")
	@SuppressWarnings("deprecation")
    private static Uri getPbwFileName(final Activity activity) {
        File file = activity.getFileStreamPath("dc_pebble.pbw");
        try {
            fileCopy(activity.getResources().openRawResource(R.raw.dc_pebble),
                   activity.openFileOutput(file.getName(), MODE_WORLD_READABLE));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Uri.fromFile(file);
    }

    /**
     * 手順最終. 
     *
     */
    public static class SettingFinishFragment extends BaseFragment {
        @Override
        public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                                                 final Bundle savedInstanceState) {
            return inflater.inflate(R.layout.dconnect_settings_step_finish, container, false);
        }
    }

    /**
     * ファイルをコピーする.
     * @param is 入力
     * @param os 出力
     * @throws IOException IO Exception
     */
    private static void fileCopy(final InputStream is, final OutputStream os) throws IOException {
        byte[] b = new byte[1024];
        while (is.read(b) > 0) {
            os.write(b);
        }
        is.close();
        os.close();
    }
}
