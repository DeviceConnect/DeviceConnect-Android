/*
HueFragment01
Copyright (c) 2014 NTT DOCOMO,INC.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.hue.activity.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueParsingError;

import org.deviceconnect.android.deviceplugin.hue.HueConstants;
import org.deviceconnect.android.deviceplugin.hue.R;

import java.util.List;

/**
 * Hue設定画面(1)フラグメント.
 */
public class HueFragment01 extends Fragment implements OnClickListener, OnItemClickListener {

    /** ListViewのAdapter. */
    private CustomAdapter mAdapter;

    /** HueSDKオブジェクト. */
    private PHHueSDK mPhHueSDK;

    /** ProgressZone. */
    private View mProgressView;

    /** 再検索ボタン. */
    private Button mSearchButton;

    /**
     * hueブリッジのNotificationを受け取るためのリスナー.
     */
    private PHSDKListener mListener = new PHSDKListener() {

        @Override
        public void onBridgeConnected(final PHBridge b) {
        }

        @Override
        public void onAuthenticationRequired(final PHAccessPoint accessPoint) {
        }

        @Override
        public void onAccessPointsFound(final List<PHAccessPoint> accessPoint) {
            if (accessPoint != null && accessPoint.size() > 0) {

                mPhHueSDK.getAccessPointsFound().clear();
                mPhHueSDK.getAccessPointsFound().addAll(accessPoint);

                final Activity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.updateData(mPhHueSDK.getAccessPointsFound());
                            mProgressView.setVisibility(View.GONE);
                            mSearchButton.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        }

        @Override
        public void onCacheUpdated(final List<Integer> list, final PHBridge bridge) {
        }

        @Override
        public void onConnectionLost(final PHAccessPoint point) {
        }

        @Override
        public void onConnectionResumed(final PHBridge bridge) {
        }

        @Override
        public void onError(final int code, final String message) {
        }

        @Override
        public void onParsingErrors(final List<PHHueParsingError> error) {
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hueのインスタンスの取得.
        mPhHueSDK = PHHueSDK.create();
        // アプリ名の登録.
        mPhHueSDK.setDeviceName(HueConstants.APNAME);
        // HueブリッジからのCallbackを受け取るためのリスナーを登録.
        mPhHueSDK.getNotificationManager().registerSDKListener(mListener);
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(final LayoutInflater inflater,
            final ViewGroup container, final Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.hue_fragment_01, container, false);
        if (rootView != null) {
            mSearchButton = (Button) rootView.findViewById(R.id.btnRefresh);
            mSearchButton.setOnClickListener(this);

            mProgressView = rootView.findViewById(R.id.progress_zone);
            mProgressView.setVisibility(View.VISIBLE);

            mAdapter = new CustomAdapter(this.getActivity().getBaseContext(), mPhHueSDK.getAccessPointsFound());

            ListView listView = (ListView) rootView.findViewById(R.id.bridge_list2);
            listView.setOnItemClickListener(this);
            listView.setAdapter(mAdapter);

            View headerView = inflater.inflate(R.layout.hue_fragment_01_header, null, false);
            listView.addHeaderView(headerView, null, false);

            // アクセスポイントのキャッシュを取得.
            mPhHueSDK.getAccessPointsFound();
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        // ローカルBridgeのUPNP Searchを開始する.
        doBridgeSearch();
    }

    @Override
    public void onDestroy() {
        // リスナーを解除
        mPhHueSDK.getNotificationManager().unregisterSDKListener(mListener);
        super.onDestroy();
    }

    @Override
    public void onClick(final View v) {
        // 検索処理を再度実行.
        mProgressView.setVisibility(View.VISIBLE);
        doBridgeSearch();
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        moveNextFragment(position);
    }

    /**
     * ローカルBridgeのUPNP Searchを開始する.
     */
    public void doBridgeSearch() {
        // ローカルBridgeのUPNP Searchを開始
        PHBridgeSearchManager sm = (PHBridgeSearchManager) mPhHueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);
        sm.search(true, true);

        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProgressView.setVisibility(View.VISIBLE);
                    mSearchButton.setVisibility(View.GONE);
                }
            });
        }
    }

    private void moveNextFragment(final int position) {
        PHAccessPoint accessPoint = (PHAccessPoint) mAdapter.getItem(position);

        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(R.anim.fragment_slide_right_enter, R.anim.fragment_slide_left_exit,
                R.anim.fragment_slide_left_enter, R.anim.fragment_slide_right_exit);
        transaction.replace(R.id.fragment_frame, HueFragment02.newInstance(accessPoint));
        transaction.commit();
    }

    /**
     * カスタムAdapter.
     */
    private class CustomAdapter extends BaseAdapter {
        /** コンテキスト. */
        private final Context mContext;
        /** Access Point. */
        private List<PHAccessPoint> mAccessPoint;

        /**
         * コンストラクタ.
         * 
         * @param context コンテキスト
         * @param accessPoint Access Point
         */
        public CustomAdapter(final Context context, final List<PHAccessPoint> accessPoint) {
            this.mContext = context;
            this.mAccessPoint = accessPoint;
        }

        /**
         * Access Pointリストのアップデートを行う.
         * 
         * @param accessPoint Access Point
         */
        public void updateData(final List<PHAccessPoint> accessPoint) {
            this.mAccessPoint = accessPoint;
            notifyDataSetChanged();
        }

        @SuppressLint("ViewHolder")
        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View rowView = inflater.inflate(R.layout.hue_list, parent, false);

            TextView mTextView = (TextView) rowView.findViewById(R.id.row_textview1);

            String listTitle = mAccessPoint.get(position).getMacAddress() + "("
                    + mAccessPoint.get(position).getIpAddress() + ")";
            mTextView.setText(listTitle);

            return rowView;
        }

        @Override
        public int getCount() {
            return mAccessPoint.size();
        }

        @Override
        public Object getItem(final int position) {
            return mAccessPoint.get(position - 1);
        }

        @Override
        public long getItemId(final int position) {
            return 0;
        }
    }
}
