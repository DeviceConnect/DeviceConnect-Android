/*
 WebSocketListActivity.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.setting;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.deviceconnect.android.manager.DConnectService;
import org.deviceconnect.android.manager.R;
import org.deviceconnect.android.manager.WebSocketInfo;
import org.deviceconnect.android.manager.WebSocketInfoManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * WebSocketを管理するためのActivity.
 *
 * @author NTT DOCOMO, INC.
 */
public class WebSocketListActivity extends BaseSettingActivity implements AlertDialogFragment.OnAlertDialogListener,
        WebSocketInfoManager.OnWebSocketEventListener {

    /**
     * WebSocket切断確認用ダイアログのタグ名を定義する.
     */
    private static final String TAG_DELETE_WEB_SOCKET = "delete_web_socket";

    /**
     * WebSocket管理クラス.
     */
    private WebSocketInfoAdapter mWebSocketInfoAdapter;

    /**
     * 切断するWebSocketを一時的に保持するための変数.
     */
    private WebSocketInfo mWebSocketInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_websocket_list);

        setTitle(R.string.activity_settings_manage_websocket);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mWebSocketInfoAdapter = new WebSocketInfoAdapter();

        ListView listView = (ListView) findViewById(R.id.activity_websocket_list);
        if (listView != null) {
            listView.setAdapter(mWebSocketInfoAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                    showDeleteDialog(position);
                }
            });
        }
    }

    @Override
    protected void onManagerBonded(final DConnectService manager) {
        mWebSocketInfoAdapter.setWebSocketInfoList(getWebSocketInfoManager().getWebSocketInfos());
        getWebSocketInfoManager().addOnWebSocketEventListener(this);
        mWebSocketInfoAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        WebSocketInfoManager mgr = getWebSocketInfoManager();
        if (mgr != null) {
            mgr.removeOnWebSocketEventListener(this);
        }
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPositiveButton(final String tag) {
        if (TAG_DELETE_WEB_SOCKET.equals(tag) && mWebSocketInfo != null) {
            DConnectService service = getManagerService();
            if (service != null) {
                service.disconnectWebSocket(mWebSocketInfo.getRawId());
            }
        }
    }

    @Override
    public void onNegativeButton(final String tag) {
        // ダイアログを閉じるだけで何もしない
    }

    @Override
    public void onDisconnect(final String origin) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                WebSocketInfoManager mgr = getWebSocketInfoManager();
                if (mgr != null) {
                    mWebSocketInfoAdapter.setWebSocketInfoList(mgr.getWebSocketInfos());
                    mWebSocketInfoAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    /**
     * 指定されたWebSocketを切断するか確認するダイアログを表示する.
     * @param position 切断するWebSocketのリストの位置
     */
    private void showDeleteDialog(final int position) {
        mWebSocketInfo = (WebSocketInfo) mWebSocketInfoAdapter.getItem(position);

        String title = getString(R.string.activity_websocket_delete_title);
        String message = getString(R.string.activity_websocket_delete_message);
        String positive = getString(R.string.activity_websocket_delete_positive);
        String negative = getString(R.string.activity_websocket_delete_negative);
        AlertDialogFragment dialog = AlertDialogFragment.create(TAG_DELETE_WEB_SOCKET, title, message, positive, negative);
        dialog.show(getFragmentManager(), TAG_DELETE_WEB_SOCKET);
    }

    /**
     * WebSocketリストを表示するためのアダプタ.
     *
     * @author NTT DOCOMO, INC.
     */
    private class WebSocketInfoAdapter extends BaseAdapter {
        /**
         * 接続を開始した時間のフォーマットを定義する.
         */
        private final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.JAPAN);

        /**
         * WebSocketのリスト.
         */
        private List<WebSocketInfo> mWebSocketInfoList = new ArrayList<>();

        /**
         * WebSocketのリストを設定する.
         * @param webSocketInfoList WebSocketのリスト
         */
        public void setWebSocketInfoList(List<WebSocketInfo> webSocketInfoList) {
            mWebSocketInfoList = webSocketInfoList;
        }

        @Override
        public int getCount() {
            if (mWebSocketInfoList == null) {
                return 0;
            }
            return mWebSocketInfoList.size();
        }

        @Override
        public Object getItem(int position) {
            return mWebSocketInfoList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.item_websocket_list, null);
            }

            WebSocketInfo info = (WebSocketInfo) getItem(position);

            TextView uriView = (TextView) view.findViewById(R.id.item_websocket_uri);
            if (uriView != null) {
                uriView.setText(info.getUri());
            }

            TextView sessionKeyView = (TextView) view.findViewById(R.id.item_websocket_session_key);
            if (sessionKeyView != null) {
                sessionKeyView.setText(info.getRawId());
            }

            TextView timeView = (TextView) view.findViewById(R.id.item_websocket_connect_time);
            if (timeView != null) {
                timeView.setText(mDateFormat.format(new Date(info.getConnectTime())));
            }

            return view;
        }
    }
}
