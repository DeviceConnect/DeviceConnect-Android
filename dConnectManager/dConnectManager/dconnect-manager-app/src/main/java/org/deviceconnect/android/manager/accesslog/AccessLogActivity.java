/*
 AccessLogActivity.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.accesslog;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.deviceconnect.android.manager.R;
import org.deviceconnect.android.manager.core.accesslog.AccessLog;
import org.deviceconnect.android.manager.core.accesslog.AccessLogProvider;
import org.deviceconnect.android.manager.setting.BaseSettingActivity;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * アクセスログを表示するための Activity.
 *
 * @author NTT DOCOMO, INC.
 */
public class AccessLogActivity extends BaseSettingActivity {
    /**
     * アクセスログ管理クラス.
     */
    private AccessLogProvider mAccessLogProvider;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAccessLogProvider = new AccessLogProvider(getApplicationContext());

        setTitle(R.string.activity_settings_accesslog);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            addFragment(DateListFragment.create(), false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                FragmentManager manager = getSupportFragmentManager();
                if (manager.getBackStackEntryCount() > 0) {
                    manager.popBackStack();
                } else {
                    finish();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * フラグメントを追加します.
     *
     * @param fragment 追加するフラグメント
     * @param isBackStack バックスタックに追加する場合はtrue、それ以外はfalse
     */
    private void addFragment(Fragment fragment, boolean isBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(android.R.id.content, fragment, "container");
        if (isBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    /**
     * フラグメントを追加します.
     *
     * @param fragment 追加するフラグメント
     */
    private void addFragment(Fragment fragment) {
        addFragment(fragment, true);
    }

    /**
     * AccessLogProvider のインスタンスを取得します.
     *
     * @return AccessLogProvider のインスタンス
     */
    private AccessLogProvider getAccessLogProvider() {
        return mAccessLogProvider;
    }

    /**
     * アクセスログ用の基底フラグメント.
     */
    public static abstract class BaseFragment extends Fragment {
        /**
         * メインスレッドで動作するハンドラ.
         */
        private Handler mHandler = new Handler(Looper.getMainLooper());

        /**
         * AccessLogProvider のインスタンスを取得します.
         * <p>
         * Activityがアタッチされていない場合は null を返却します。
         * </p>
         * @return AccessLogProvider のインスタンス
         */
        AccessLogProvider getAccessLogProvider() {
            AccessLogActivity activity = (AccessLogActivity) getActivity();
            if (activity != null) {
                return activity.getAccessLogProvider();
            }
            return null;
        }

        /**
         * 指定されて Runnable をメインスレッドで実行します.
         *
         * @param runnable メインスレッドで実行するRunnable
         */
        void runOnUiThread(Runnable runnable) {
            mHandler.post(runnable);
        }

        /**
         * フラグメントを移動します.
         *
         * @param fragment フラグメンt
         */
        void gotoFragment(Fragment fragment) {
            AccessLogActivity activity = (AccessLogActivity) getActivity();
            if (activity != null) {
                activity.addFragment(fragment);
            }
        }
    }

    /**
     * 日付の一覧を表示するフラグメント.
     *
     */
    public static class DateListFragment extends BaseFragment {
        /**
         * 日付のリストを管理するクラス.
         */
        private DateListAdapter mListAdapter;

        /**
         * DateListFragment のインスタンスを作成します.
         *
         * @return DateListFragmentのインスタンス
         */
        static DateListFragment create() {
            return new DateListFragment();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View root = inflater.inflate(R.layout.fragment_accesslog_date_list, container, false);

            mListAdapter = new DateListAdapter(inflater);

            ListView listView = root.findViewById(R.id.list_view_accesslog_date_list);
            listView.setAdapter(mListAdapter);
            listView.setOnItemClickListener((parent, view, position, id) ->
                    gotoAccessLogListFragment((String) mListAdapter.getItem(position)));

            return root;
        }

        @Override
        public void onResume() {
            super.onResume();

            AccessLogProvider provider = getAccessLogProvider();
            if (provider != null) {
                provider.getDateList(this::updateDateList);
            }
        }

        /**
         * リストを更新します.
         *
         * @param dateList 更新するリスト
         */
        private void updateDateList(List<String> dateList) {
            runOnUiThread(() -> mListAdapter.updateDateList(dateList));
        }

        /**
         * AccessLogListFragment を表示します.
         *
         * @param date 日付の文字列
         */
        private void gotoAccessLogListFragment(String date) {
            gotoFragment(AccessLogListFragment.create(date));
        }
    }

    /**
     * 日付リストの描画を管理するクラス.
     */
    private static class DateListAdapter extends BaseAdapter {
        /**
         * 日付のリスト.
         */
        private List<String> mDateList = new ArrayList<>();
        private LayoutInflater mInflater;

        DateListAdapter(LayoutInflater inflater) {
            mInflater = inflater;
        }

        /**
         * 日付のリストを更新します.
         *
         * @param dateList 更新する日付
         */
        void updateDateList(List<String> dateList) {
            if (dateList == null || mDateList.equals(dateList)) {
                return;
            }
            mDateList = dateList;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mDateList.size();
        }

        @Override
        public Object getItem(int position) {
            return mDateList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = mInflater.inflate(R.layout.item_accesslog_date_list, parent, false);
            }

            String name = (String) getItem(position);

            TextView textView = view.findViewById(R.id.accesslog_date_name);
            textView.setText(name);

            return view;
        }
    }

    /**
     * 指定された日付のアクセスログのリストを表示するフラグメント.
     */
    public static class AccessLogListFragment extends BaseFragment {
        /**
         * 引数に渡す日付を識別するキーを定義.
         */
        private static final String ARGS_DATE = "date";

        /**
         * アクセスログの描画を管理するクラス.
         */
        private AccessLogListAdapter mListAdapter;

        /**
         * 検索窓.
         */
        private EditText mIpAddress;

        /**
         * AccessLogListFragment を作成します.
         *
         * @param date フラグメントに渡す日付
         * @return AccessLogListFragmentのインスタンス
         */
        static AccessLogListFragment create(String date) {
            Bundle args = new Bundle();
            args.putString(ARGS_DATE, date);

            AccessLogListFragment fragment = new AccessLogListFragment();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View root = inflater.inflate(R.layout.fragment_accesslog_list, container, false);

            mListAdapter = new AccessLogListAdapter(inflater);

            mIpAddress = root.findViewById(R.id.fragment_search_edit_text);

            ListView listView = root.findViewById(R.id.list_view_accesslog_list);
            listView.setAdapter(mListAdapter);
            listView.setOnItemClickListener((parent, view, position, id) ->
                    gotoAccessLogFragment((AccessLog) mListAdapter.getItem(position)));

            root.findViewById(R.id.fragment_search_btn).setOnClickListener((v) ->
                    search(mIpAddress.getText().toString()));

            return root;
        }

        @Override
        public void onResume() {
            super.onResume();
            searchAccessLog();
        }

        /**
         * 引数に渡された日付を取得します.
         * <p>
         * 引数に日付が渡されていない場合には null を返します。
         * </p>
         * @return 日付
         */
        private String getDateString() {
            Bundle args = getArguments();
            if (args != null) {
                return args.getString(ARGS_DATE);
            }
            return null;
        }

        /**
         * アクセスログのリストを更新します.
         *
         * @param accessLogList アクセスログの更新
         */
        private void updateAccessLogList(List<AccessLog> accessLogList) {
            runOnUiThread(() -> mListAdapter.updateAccessLogList(accessLogList));
        }

        /**
         * 指定された日付のアクセスログを検索します.
         */
        private void searchAccessLog() {
            String date = getDateString();
            AccessLogProvider provider = getAccessLogProvider();
            if (provider != null && date != null) {
                provider.getAccessLogsOfDate(date, this::updateAccessLogList);
            }
        }

        /**
         * IPアドレスの検索を行います.
         *
         * @param ipAddress IPアドレス
         */
        private void search(String ipAddress) {
            if (ipAddress == null || ipAddress.isEmpty()) {
                searchAccessLog();
            } else {
                String date = getDateString();
                AccessLogProvider provider = getAccessLogProvider();
                if (provider != null && date != null) {
                    provider.getAccessLogsFromIpAddress(date, ipAddress, this::updateAccessLogList);
                }
            }
        }

        /**
         * アクセスログの詳細画面を開きます.
         *
         * @param accessLog アクセスログ
         */
        private void gotoAccessLogFragment(AccessLog accessLog) {
            gotoFragment(AccessLogFragment.create(accessLog.getId()));
        }
    }

    /**
     * アクセスログリストの表示を管理するクラス.
     */
    private static class AccessLogListAdapter extends BaseAdapter {
        /**
         * アクセスログのリスト.
         */
        private List<AccessLog> mAccessLogList = new ArrayList<>();
        private LayoutInflater mInflater;

        AccessLogListAdapter(LayoutInflater inflater) {
            mInflater = inflater;
        }

        /**
         * アクセスログのリストを更新します.
         *
         * @param accessLog アクセスログのリスト
         */
        void updateAccessLogList(List<AccessLog> accessLog) {
            if (accessLog == null || mAccessLogList.equals(accessLog)) {
                return;
            }
            mAccessLogList = accessLog;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mAccessLogList.size();
        }

        @Override
        public Object getItem(int position) {
            return mAccessLogList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            View view = convertView;
            if (view == null) {
                view = mInflater.inflate(R.layout.item_accesslog_list, parent, false);
                holder = new ViewHolder();
                holder.mIpAddress = view.findViewById(R.id.item_access_log_ip_address);
                holder.mPath = view.findViewById(R.id.item_access_log_path);
                holder.mDate = view.findViewById(R.id.item_access_log_date);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            AccessLog accessLog = (AccessLog) getItem(position);
            holder.mIpAddress.setText(getIpAddress(accessLog));
            holder.mPath.setText(getPath(accessLog));
            holder.mDate.setText(getDate(accessLog));

            return view;
        }

        /**
         * アクセスログから IP アドレスを取得します.
         *
         * @param accessLog アクセスログ
         * @return IPアドレス
         */
        private String getIpAddress(AccessLog accessLog) {
            String ipAddress = accessLog.getRemoteIpAddress();
            if (ipAddress == null) {
                ipAddress = "none";
            }
            return ipAddress;
        }

        /**
         * アクセスログからHTTPメソッドとパスを取得します.
         *
         * @param accessLog アクセスログ
         * @return HTTPメソッドとパス
         */
        private String getPath(AccessLog accessLog) {
            String method = accessLog.getRequestMethod();
            String path = accessLog.getRequestPath();
            if (path == null) {
                path = "/";
            }
            return method + " " + path;
        }

        /**
         * アクセスログからリクエスト受信時刻を取得します.
         *
         * @param accessLog アクセスログ
         * @return リクエスト受信時刻
         */
        private String getDate(AccessLog accessLog) {
            return AccessLogProvider.dateToString(accessLog.getRequestReceivedTime());
        }

        class ViewHolder {
            TextView mIpAddress;
            TextView mPath;
            TextView mDate;
        }
    }

    /**
     * アクセスログの詳細を表示するフラグメント.
     */
    public static class AccessLogFragment extends BaseFragment {
        /**
         * アクセスログのIDを格納するキーを定義.
         */
        private static final String ARGS_ID = "id";

        /**
         * 詳細を表示するTextView.
         */
        private TextView mDetailView;

        /**
         * AccessLogFragment を作成します.
         *
         * @param id アクセスログのID
         * @return AccessLogFragmentのインスタンス
         */
        static AccessLogFragment create(long id) {
            Bundle args = new Bundle();
            args.putLong(ARGS_ID, id);

            AccessLogFragment fragment = new AccessLogFragment();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View root = inflater.inflate(R.layout.fragment_accesslog_detail, container, false);
            mDetailView = root.findViewById(R.id.fragment_accesslog_detail);
            return root;
        }

        @Override
        public void onResume() {
            super.onResume();

            long id = getAccessLogId();
            AccessLogProvider provider = getAccessLogProvider();
            if (provider != null && id != -1) {
                provider.getAccessLog(id, this::updateAccessLog);
            }
        }

        /**
         * アクセスログの詳細を更新します.
         *
         * @param accessLog アクセスログ
         */
        private void updateAccessLog(AccessLog accessLog) {
            String sb ="Request: " + AccessLogProvider.dateToString(accessLog.getRequestReceivedTime()) + "\r\n";
            sb += "IP: " + accessLog.getRemoteIpAddress() + "\r\n";
            sb += "HostName: " + accessLog.getRemoteHostName() + "\r\n";
            sb += "\r\n";
            sb += accessLog.getRequestMethod() + " " + accessLog.getRequestPath() + "\r\n";
            Map<String, String> headers = accessLog.getRequestHeader();
            if (headers != null) {
                for (String key : headers.keySet()) {
                    sb += key + ": " + headers.get(key) + "\r\n";
                }
            }
            if (accessLog.getRequestBody() != null) {
                sb += accessLog.getRequestBody() + "\r\n";
            }
            sb += "\r\n";

            sb += "Response: " + AccessLogProvider.dateToString(accessLog.getResponseSendTime()) + "\r\n";
            sb += "\r\n";
            sb += "HTTP/1.1 " + accessLog.getResponseStatusCode() + "\r\n";
            if (accessLog.getResponseContentType() != null) {
                sb += "Content-Type: " + accessLog.getResponseContentType() + "\r\n";
            }
            sb += "\r\n";
            if (accessLog.getResponseBody() != null) {
                if (isContentType(accessLog.getResponseContentType())) {
                    sb += reshapeJson(accessLog.getResponseBody()) + "\r\n";
                } else {
                    sb += accessLog.getResponseBody() + "\r\n";
                }
            }
            updateAccessLog(sb);
        }

        /**
         * アクセスログの文字列を TextView に反映します.
         *
         * @param accessLog アクセスログ
         */
        private void updateAccessLog(String accessLog) {
            runOnUiThread(() -> mDetailView.setText(accessLog));
        }

        /**
         * コンテンツタイプがJSONか確認します.
         *
         * @param contentType コンテントタイプ
         * @return JSON形式の場合はtrue、それ以外はfalse
         */
        private boolean isContentType(String contentType) {
            return contentType != null && contentType.startsWith("application/json");
        }

        /**
         * JSONの文字列を整形して取得します.
         *
         * @param body JSONの文字列
         * @return 整形されたJSONの文字列
         */
        private String reshapeJson(String body) {
            try {
                JSONObject object = new JSONObject(body);
                return object.toString(2);
            } catch (JSONException e) {
                return body;
            }
        }

        /**
         * 引数に渡されたアクセスログのIDを取得します.
         *
         * @return アクセスログのID
         */
        private long getAccessLogId() {
            Bundle args = getArguments();
            if (args != null) {
                return args.getLong(ARGS_ID);
            }
            return -1;
        }
    }
}
