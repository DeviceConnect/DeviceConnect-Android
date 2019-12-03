/*
HueFragment01
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.hue.activity.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHMessageType;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueParsingError;

import org.deviceconnect.android.deviceplugin.hue.db.HueManager;
import org.deviceconnect.android.deviceplugin.hue.R;

import java.util.List;

/**
 * Hue設定画面(1)フラグメント.
 */
public class HueFragment01 extends Fragment implements OnClickListener, OnItemClickListener {

    /** ListViewのAdapter. */
    private CustomAdapter mAdapter;

    /** ProgressZone. */
    private View mProgressView;

    /** 再検索ボタン. */
    private Button mSearchButton;

    /**
     * hueブリッジのNotificationを受け取るためのリスナー.
     */
    private PHSDKListener mListener = new PHSDKListener() {

        @Override
        public void onAuthenticationRequired(final PHAccessPoint accessPoint) {
        }

        @Override
        public void onAccessPointsFound(final List<PHAccessPoint> accessPoint) {
            runOnUiThread(() -> {
                mAdapter.updateData(accessPoint);
                mProgressView.setVisibility(View.GONE);
                mSearchButton.setVisibility(View.VISIBLE);
            });
        }

        @Override
        public void onCacheUpdated(final List<Integer> list, final PHBridge bridge) {
        }

        @Override
        public void onBridgeConnected(final PHBridge phBridge, final String userName) {
        }

        @Override
        public void onConnectionLost(final PHAccessPoint point) {
        }

        @Override
        public void onConnectionResumed(final PHBridge bridge) {
        }

        @Override
        public void onError(final int code, final String message) {
            if (code == PHMessageType.BRIDGE_NOT_FOUND) {
                runOnUiThread(() -> {
                    mProgressView.setVisibility(View.GONE);
                    mSearchButton.setVisibility(View.VISIBLE);
                });
            }
        }

        @Override
        public void onParsingErrors(final List<PHHueParsingError> error) {
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HueManager.INSTANCE.init(getContext());
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(final LayoutInflater inflater,
            final ViewGroup container, final Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.hue_fragment_01, container, false);
        if (rootView != null) {
            mSearchButton =  rootView.findViewById(R.id.btnRefresh);
            mSearchButton.setOnClickListener(this);

            mProgressView = rootView.findViewById(R.id.progress_zone);
            mProgressView.setVisibility(View.VISIBLE);

            mAdapter = new CustomAdapter(getActivity().getBaseContext(),
                    HueManager.INSTANCE.getAccessPoint());

            ListView listView =  rootView.findViewById(R.id.bridge_list2);
            listView.setOnItemClickListener(this);
            View headerView = inflater.inflate(R.layout.hue_fragment_01_header, null, false);
            listView.addHeaderView(headerView, null, false);
            listView.setAdapter(mAdapter);
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        HueManager.INSTANCE.addSDKListener(mListener);

        if (isWifiEnabled()) {
            // ローカルBridgeのUPNP Searchを開始する.
            doBridgeSearch();
        } else {
            mProgressView.setVisibility(View.GONE);
            mSearchButton.setVisibility(View.VISIBLE);
            showWifiNotConnected();
        }
    }

    @Override
    public void onPause() {
        // リスナーを解除
        HueManager.INSTANCE.removeSDKListener(mListener);
        super.onPause();
    }

    @Override
    public void onClick(final View v) {
        // 検索処理を再度実行.
        if (isWifiEnabled()) {
            doBridgeSearch();
        } else {
            showWifiNotConnected();
        }
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        moveNextFragment((PHAccessPoint) mAdapter.getItem(position));
    }

    private void runOnUiThread(final Runnable run) {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(run);
        }
    }

    /**
     * ローカルBridgeのUPNP Searchを開始する.
     */
    private void doBridgeSearch() {
        mAdapter.updateData(HueManager.INSTANCE.getAccessPoint());
        HueManager.INSTANCE.searchHueBridge();

        runOnUiThread(() -> {
            mProgressView.setVisibility(View.VISIBLE);
            mSearchButton.setVisibility(View.GONE);
        });
    }

    /**
     * Wi-Fi接続が無効になっている場合のエラーダイアログを表示します.
     */
    private void showWifiNotConnected() {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(() -> {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.hue_dialog_network_error)
                            .setMessage(R.string.hue_dialog_not_connect_wifi)
                            .setPositiveButton(R.string.hue_dialog_ok, (dialog, which) -> {
                            })
                            .setCancelable(false)
                            .show();
            });
        }
    }

    /**
     * 指定されたアクセスポイントを指定して、次のフラグメントを開く.
     * @param accessPoint アクセスポイント
     */
    private void moveNextFragment(final PHAccessPoint accessPoint) {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(R.anim.fragment_slide_right_enter, R.anim.fragment_slide_left_exit,
                R.anim.fragment_slide_left_enter, R.anim.fragment_slide_right_exit);
        transaction.replace(R.id.fragment_frame, HueFragment02.newInstance(accessPoint));
        transaction.commit();
    }

    /**
     * Wi-Fi接続設定の状態を取得します.
     * @return trueの場合は有効、それ以外の場合は無効
     */
    private boolean isWifiEnabled() {
        WifiManager mgr = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return mgr.isWifiEnabled();
    }

    /**
     * カスタムAdapter.
     */
    private class CustomAdapter extends BaseAdapter {
        /** コンテキスト. */
        private final Context mContext;

        /** Access Point. */
        private List<PHAccessPoint> mAccessPoints;

        /**
         * コンストラクタ.
         * 
         * @param context コンテキスト
         * @param accessPoints Access Point
         */
        CustomAdapter(final Context context, final List<PHAccessPoint> accessPoints) {
            mContext = context;
            mAccessPoints = accessPoints;
        }

        /**
         * Access Pointリストのアップデートを行う.
         * 
         * @param accessPoints Access Point
         */
        private void updateData(final List<PHAccessPoint> accessPoints) {
            mAccessPoints = accessPoints;
            notifyDataSetChanged();
        }

        @SuppressLint("ViewHolder")
        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View rowView = inflater.inflate(R.layout.hue_list, parent, false);

            TextView mTextView = rowView.findViewById(R.id.row_textview1);

            String listTitle = mAccessPoints.get(position).getMacAddress() + "("
                    + mAccessPoints.get(position).getIpAddress() + ")";
            mTextView.setText(listTitle);

            return rowView;
        }

        @Override
        public int getCount() {
            return mAccessPoints.size();
        }

        @Override
        public Object getItem(final int position) {
            return mAccessPoints.get(position - 1);
        }

        @Override
        public long getItemId(final int position) {
            return 0;
        }
    }
}
