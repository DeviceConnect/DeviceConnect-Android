package org.deviceconnect.android.manager.setting;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.deviceconnect.android.manager.DConnectApplication;
import org.deviceconnect.android.manager.DConnectService;
import org.deviceconnect.android.manager.R;
import org.deviceconnect.android.manager.WebSocketInfo;
import org.deviceconnect.android.manager.WebSocketInfoManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WebSocketListActivity extends Activity implements AlertDialogFragment.OnAlertDialogListener,
        WebSocketInfoManager.OnWebSocketEventListener {

    private static final String TAG_DELETE_WEB_SOCKET = "delete_web_socket";

    private WebSocketInfoAdapter mWebSocketInfoAdapter;
    private WebSocketInfo mWebSocketInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_websocket_list);

        setTitle(R.string.activity_settings_manage_websocket);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mWebSocketInfoAdapter = new WebSocketInfoAdapter();
        mWebSocketInfoAdapter.setWebSocketInfoList(getWebSocketInfoManager().getWebSocketInfos());

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
    protected void onResume() {
        super.onResume();
        getWebSocketInfoManager().setOnWebSocketEventListener(this);
    }

    @Override
    protected void onPause() {
        getWebSocketInfoManager().setOnWebSocketEventListener(null);
        super.onPause();
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
            Intent intent = new Intent();
            intent.setClass(this, DConnectService.class);
            intent.setAction(DConnectService.ACTION_DISCONNECT_WEB_SOCKET);
            intent.putExtra(DConnectService.EXTRA_WEBSOCKET_ID, mWebSocketInfo.getRawId());
            startService(intent);
        }
    }

    @Override
    public void onNegativeButton(final String tag) {

    }

    @Override
    public void onDisconnect(final String origin) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mWebSocketInfoAdapter.setWebSocketInfoList(getWebSocketInfoManager().getWebSocketInfos());
                mWebSocketInfoAdapter.notifyDataSetChanged();
            }
        });
    }

    private void showDeleteDialog(final int position) {
        mWebSocketInfo = (WebSocketInfo) mWebSocketInfoAdapter.getItem(position);

        String title = getString(R.string.activity_websocket_delete_title);
        String message = getString(R.string.activity_websocket_delete_message);
        String positive = getString(R.string.activity_websocket_delete_positive);
        String negative = getString(R.string.activity_websocket_delete_negative);
        AlertDialogFragment dialog = AlertDialogFragment.create(TAG_DELETE_WEB_SOCKET, title, message, positive, negative);
        dialog.show(getFragmentManager(), TAG_DELETE_WEB_SOCKET);
    }

    private WebSocketInfoManager getWebSocketInfoManager() {
        DConnectApplication app = (DConnectApplication) getApplication();
        return app.getWebSocketInfoManager();
    }

    private class WebSocketInfoAdapter extends BaseAdapter {
        private final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.JAPAN);
        private List<WebSocketInfo> mWebSocketInfoList = new ArrayList<>();

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
