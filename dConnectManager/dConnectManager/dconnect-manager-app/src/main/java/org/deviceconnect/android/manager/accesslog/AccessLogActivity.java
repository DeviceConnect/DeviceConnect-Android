/*
 AccessLogActivity.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.accesslog;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
         * アクセスログ管理クラス.
         */
        private AccessLogProvider mAccessLogProvider;

        @Override
        public void onResume() {
            super.onResume();

            AccessLogActivity activity = (AccessLogActivity) getActivity();
            if (activity != null) {
                mAccessLogProvider = activity.getAccessLogProvider();
            }
        }

        /**
         * AccessLogProvider のインスタンスを取得します.
         * <p>
         * Activityがアタッチされていない場合は null を返却します。
         * </p>
         * @return AccessLogProvider のインスタンス
         */
        AccessLogProvider getAccessLogProvider() {
            return mAccessLogProvider;
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
     * RecyclerViewで使用する RecyclerView.Adapter の基底となるクラス.
     *
     * @param <A> データリストの型
     * @param <T> RecyclerView.ViewHolderを継承した型
     */
    private static abstract class BaseAdapter<A, T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T> {
        /**
         * データのリスト.
         */
        List<A> mDataList = new ArrayList<>();

        /**
         * インフレータ.
         */
        LayoutInflater mInflater;

        /**
         * リストがクリックされたことを通知するリスナー.
         */
        OnItemClickListener mOnItemClickListener;

        /**
         * アクセスログの日付リストの削除を通知するリスナー.
         */
        OnItemRemoveListener<A> mOnItemRemoveListener;

        /**
         * Undo確認用のSnackbar.
         */
        Snackbar mSnackbar;

        /**
         * コンストラクタ.
         * @param inflater インフレータ
         */
        BaseAdapter(LayoutInflater inflater) {
            mInflater = inflater;
        }

        /**
         * Undo確認用のSnackbar を非表示にします.
         */
        void dismissSnackbar() {
            if (mSnackbar != null) {
                mSnackbar.dismiss();
            }
        }

        /**
         * データリストを更新します.
         *
         * @param dataList 更新するアクセスログの日付リスト
         */
        void updateDataList(List<A> dataList) {
            if (dataList == null || mDataList.equals(dataList)) {
                return;
            }
            mDataList = dataList;
            notifyDataSetChanged();
        }

        /**
         * 指定された位置のデータを取得します.
         *
         * @param position 位置
         * @return データ
         */
        A getItem(int position) {
            return mDataList.get(position);
        }

        @Override
        public int getItemCount() {
            return mDataList.size();
        }

        /**
         * スワイプされて削除処理が行われた時の処理.
         *
         * @param viewHolder viewホルダー
         * @param recyclerView リサイクルView
         */
        void onItemRemove(final RecyclerView.ViewHolder viewHolder, final RecyclerView recyclerView) {
            final int adapterPosition = viewHolder.getAdapterPosition();
            final A removeData = mDataList.get(adapterPosition);
            mSnackbar = Snackbar
                    .make(recyclerView, R.string.activity_accesslog_remove_date, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.activity_accesslog_undo_remove_date, (View view) -> {
                        mDataList.add(adapterPosition, removeData);
                        notifyItemInserted(adapterPosition);
                        recyclerView.scrollToPosition(adapterPosition);
                    })
                    .addCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                                if (mOnItemRemoveListener != null) {
                                    mOnItemRemoveListener.onItemDelete(removeData);
                                }
                            }
                        }
                    });
            mSnackbar.show();
            mDataList.remove(adapterPosition);
            notifyItemRemoved(adapterPosition);
        }

        /**
         * リストがクリックされたことを通知するリスナーを設定します.
         *
         * @param listener リスナー
         */
        void setOnItemClickListener(OnItemClickListener listener) {
            mOnItemClickListener = listener;
        }

        /**
         * アイテムが削除されたことを通知するリスナーを設定します.
         *
         * @param listener リスナー
         */
        void setOnItemRemoveListener(OnItemRemoveListener<A> listener) {
            mOnItemRemoveListener = listener;
        }

        /**
         * アイテムクリックリスナー.
         */
        interface OnItemClickListener {
            void onItemClick(View view, int position);
        }

        /**
         * アイテム削除リスナー.
         */
        interface OnItemRemoveListener<A> {
            void onItemDelete(A data);
        }
    }

    /**
     * アクセスログの日付のリストを表示するフラグメント.
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
            mListAdapter.setOnItemClickListener((v, position) ->
                    gotoAccessLogListFragment(mListAdapter.getItem(position)));
            mListAdapter.setOnItemRemoveListener((data) -> {
                AccessLogProvider provider = getAccessLogProvider();
                if (provider != null) {
                    provider.remove(data);
                }
            });

            RecyclerView recyclerView = root.findViewById(R.id.recycler_view_accesslog_date_list);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(mListAdapter);
            recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

            ItemTouchHelper helper = new ItemTouchHelper(new SwipeToDeleteCallback(getContext()) {
                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    mListAdapter.onItemRemove(viewHolder, recyclerView);
                }
            });
            helper.attachToRecyclerView(recyclerView);

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

        @Override
        public void onPause() {
            mListAdapter.dismissSnackbar();
            super.onPause();
        }

        /**
         * リストを更新します.
         *
         * @param dateList 更新するリスト
         */
        private void updateDateList(List<String> dateList) {
            runOnUiThread(() -> mListAdapter.updateDataList(dateList));
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
     * アクセスログの日付リストを管理するアダプタ.
     */
    private static class DateListAdapter extends BaseAdapter<String, DateListAdapter.ViewHolder> {
        /**
         * コンストラクタ.
         * @param inflater インフレータ
         */
        DateListAdapter(LayoutInflater inflater) {
            super(inflater);
        }

        @Override
        public DateListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(mInflater.inflate(R.layout.item_accesslog_date_list, parent, false));
        }

        @Override
        public void onBindViewHolder(DateListAdapter.ViewHolder holder, int position) {
            holder.mTextView.setText(mDataList.get(position));
        }

        /**
         * View を保持するクラス.
         */
        class ViewHolder extends RecyclerView.ViewHolder {
            /**
             * 日付の文字列を表示するView.
             */
            TextView mTextView;

            /**
             * コンストラクタ.
             * @param itemView RecyclerViewのルートView
             */
            ViewHolder(View itemView) {
                super(itemView);
                mTextView = itemView.findViewById(R.id.accesslog_date_name);
                itemView.setOnClickListener((v) -> {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(itemView, getAdapterPosition());
                    }
                });
                itemView.setOnTouchListener((v, event) -> {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            dismissSnackbar();
                            break;
                    }
                    return false;
                });
            }
        }
    }

    /**
     * RecyclerView スワイプコールバック.
     */
    private static abstract class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
        /**
         * ゴミ箱のアイコン.
         */
        private Drawable mDeleteIcon;

        /**
         * 背景色.
         */
        private ColorDrawable mBackground;

        /**
         * コンストラクタ.
         * @param context コンテキスト
         */
        SwipeToDeleteCallback(Context context) {
            super(0, (ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT));
            mDeleteIcon = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_delete);
            mBackground = new ColorDrawable(Color.RED);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dx, float dy, int actionState, boolean isCurrentlyActive) {
            View itemView = viewHolder.itemView;
            int backgroundCornerOffset = 20;
            int iconMargin = (itemView.getHeight() - mDeleteIcon.getIntrinsicHeight()) / 2;
            int iconTop = itemView.getTop() + (itemView.getHeight() - mDeleteIcon.getIntrinsicHeight()) / 2;
            int iconBottom = iconTop + mDeleteIcon.getIntrinsicHeight();

            if (dx > 0) { // Swiping to the right
                int iconLeft = itemView.getLeft() + iconMargin + mDeleteIcon.getIntrinsicWidth();
                int iconRight = itemView.getLeft() + iconMargin;
                mDeleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                mBackground.setBounds(itemView.getLeft(), itemView.getTop(),
                        itemView.getLeft() + ((int) dx) + backgroundCornerOffset,
                        itemView.getBottom());
            } else if (dx < 0) { // Swiping to the left
                int iconLeft = itemView.getRight() - iconMargin - mDeleteIcon.getIntrinsicWidth();
                int iconRight = itemView.getRight() - iconMargin;
                mDeleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                mBackground.setBounds(itemView.getRight() + ((int) dx) - backgroundCornerOffset,
                        itemView.getTop(), itemView.getRight(), itemView.getBottom());
            } else { // view is unSwiped
                mBackground.setBounds(0, 0, 0, 0);
            }

            mBackground.draw(c);

            if (Math.abs(dx) > 96) {
                mDeleteIcon.draw(c);
            }

            super.onChildDraw(c, recyclerView, viewHolder, dx, dy, actionState, isCurrentlyActive);
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
            mListAdapter.setOnItemClickListener((view, position) ->
                    gotoAccessLogFragment(mListAdapter.getItem(position)));
            mListAdapter.setOnItemRemoveListener((data) -> {
                AccessLogProvider provider = getAccessLogProvider();
                if (provider != null) {
                    provider.remove(data);
                }
            });

            mIpAddress = root.findViewById(R.id.fragment_search_edit_text);

            RecyclerView recyclerView = root.findViewById(R.id.list_view_accesslog_list);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(mListAdapter);
            recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

            ItemTouchHelper helper = new ItemTouchHelper(new SwipeToDeleteCallback(getContext()) {
                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    mListAdapter.onItemRemove(viewHolder, recyclerView);
                }
            });
            helper.attachToRecyclerView(recyclerView);

            root.findViewById(R.id.fragment_search_btn).setOnClickListener((v) ->
                    search(mIpAddress.getText().toString()));

            return root;
        }

        @Override
        public void onResume() {
            super.onResume();
            searchAccessLog();
        }

        @Override
        public void onPause() {
            mListAdapter.dismissSnackbar();
            super.onPause();
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
            runOnUiThread(() -> mListAdapter.updateDataList(accessLogList));
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
    private static class AccessLogListAdapter extends BaseAdapter<AccessLog, AccessLogListAdapter.ViewHolder> {
        AccessLogListAdapter(LayoutInflater inflater) {
            super(inflater);
        }

        @Override
        public AccessLogListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(mInflater.inflate(R.layout.item_accesslog_list, parent, false));
        }

        @Override
        public void onBindViewHolder(AccessLogListAdapter.ViewHolder holder, int position) {
            AccessLog accessLog = mDataList.get(position);
            holder.mIpAddress.setText(getIpAddress(accessLog));
            holder.mPath.setText(getPath(accessLog));
            holder.mDate.setText(getDate(accessLog));
        }

        /**
         * View を保持するクラス.
         */
        class ViewHolder extends RecyclerView.ViewHolder {
            /**
             * IPアドレスを表示するTextView.
             */
            TextView mIpAddress;

            /**
             * パスを表示するTextView.
             */
            TextView mPath;

            /**
             * 日付を表示するTextView.
             */
            TextView mDate;

            /**
             * コンストラクタ.
             * @param itemView RecyclerViewのルートView
             */
            ViewHolder(View itemView) {
                super(itemView);
                mIpAddress = itemView.findViewById(R.id.item_access_log_ip_address);
                mPath = itemView.findViewById(R.id.item_access_log_path);
                mDate = itemView.findViewById(R.id.item_access_log_date);
                itemView.setOnClickListener((v) -> {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(itemView, getAdapterPosition());
                    }
                });
                itemView.setOnTouchListener((v, event) -> {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            dismissSnackbar();
                            break;
                    }
                    return false;
                });
            }
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
            Log.e("ABC", "#### id " + id);
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
            if (accessLog == null) {
                return;
            }

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
