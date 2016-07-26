package org.deviceconnect.android.manager.setting;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.deviceconnect.android.manager.DConnectApplication;
import org.deviceconnect.android.manager.R;
import org.deviceconnect.android.manager.WebSocketInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WebSocketListActivity extends Activity {
    private WebSocketInfoAdapter mWebSocketInfoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_websocket_list);

        setTitle(R.string.activity_settings_manage_websocket);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        DConnectApplication app = (DConnectApplication) getApplication();

        mWebSocketInfoAdapter = new WebSocketInfoAdapter();
        mWebSocketInfoAdapter.mWebSocketInfoList = app.getWebSocketInfoManager().getWebSocketInfos();

        ListView listView = (ListView) findViewById(R.id.activity_websocket_list);
        if (listView != null) {
            listView.setAdapter(mWebSocketInfoAdapter);
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class WebSocketInfoAdapter extends BaseAdapter {
        private List<WebSocketInfo> mWebSocketInfoList = new ArrayList<>();
        private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.JAPAN);

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
                sessionKeyView.setText(info.getEventKey());
            }

            TextView timeView = (TextView) view.findViewById(R.id.item_websocket_connect_time);
            if (timeView != null) {
                timeView.setText(mDateFormat.format(new Date(info.getConnectTime())));
            }

            return view;
        }
    }
}
