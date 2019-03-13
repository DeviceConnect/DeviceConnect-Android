/*
 AccessLogActivity.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.accesslog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import org.deviceconnect.android.manager.R;
import org.deviceconnect.server.nanohttpd.accesslog.AccessLog;
import org.deviceconnect.server.nanohttpd.accesslog.AccessLogProvider;
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            callFragment();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                callFragment();
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
     * Fragment の {@link BaseFragment#onReturn()} を呼び出します.
     */
    private void callFragment() {
        FragmentManager manager = getSupportFragmentManager();
        List<Fragment> fragments = manager.getFragments();
        for (Fragment fragment : fragments) {
            if (fragment instanceof BaseFragment) {
                ((BaseFragment) fragment).onReturn();
            }
        }
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
         * 前の画面に戻る時に呼び出されます.
         */
        void onReturn() {
        }

        /**
         * 前の Fragment に戻ります.
         * <p>
         * 前の Fragment がない場合には Activity を終了します。
         * </p>
         */
        void popFragment() {
            FragmentManager manager = getFragmentManager();
            if (manager.getBackStackEntryCount() > 0) {
                manager.popBackStack();
            } else {
                getActivity().finish();
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
         * Undo確認用の Snackbar を非表示にします.
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
         * スワイプされて削除処理が行われた時の処理を行います.
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
                    })
                    .addCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            // 元に戻すの Action 以外のイベントで閉じられた場合には、削除処理を行う
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
         * 削除ダイアログ用リクエストコード.
         */
        private static final int REQUEST_CODE = 100;

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

        @SuppressLint("ClickableViewAccessibility")
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
            recyclerView.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mListAdapter.dismissSnackbar();
                        break;
                }
                return false;
            });

            ItemTouchHelper helper = new ItemTouchHelper(new SwipeToDeleteCallback(getContext()) {
                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    mListAdapter.onItemRemove(viewHolder, recyclerView);
                }
            });
            helper.attachToRecyclerView(recyclerView);

            setHasOptionsMenu(true);

            return root;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            inflater.inflate(R.menu.fragment_date_list, menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item){
            switch (item.getItemId()) {
                case R.id.fragment_accesslog_all_delete:
                    new DeleteDialogFragment.Builder()
                            .requestCode(REQUEST_CODE)
                            .title(getString(R.string.fragment_accesslog_delete_all_title))
                            .message(getString(R.string.fragment_accesslog_delete_all_message))
                            .positive(getString(R.string.fragment_accesslog_delete_all_positive))
                            .negative(getString(R.string.fragment_accesslog_delete_all_nagetive))
                            .show(getFragmentManager(), this);
                    break;
            }
            return true;
        }

        @Override
        public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
            if (requestCode == REQUEST_CODE) {
                if (resultCode == DialogInterface.BUTTON_POSITIVE) {
                    deleteAll();
                }
            }
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
        void onReturn() {
            mListAdapter.dismissSnackbar();
        }

        /**
         * リストを更新します.
         *
         * @param dateList 更新するリスト
         */
        private void updateDateList(List<String> dateList) {
            runOnUiThread(() -> {
                if (dateList.isEmpty()) {
                    setNoDateView(View.VISIBLE);
                } else {
                    setNoDateView(View.GONE);
                    mListAdapter.updateDataList(dateList);
                }
            });
        }

        /**
         * データがないことを表示するViewの表示状態を設定します.
         *
         * @param visibility 表示状態
         */
        private void setNoDateView(int visibility) {
            View root = getView();
            if (root != null) {
                View v = root.findViewById(R.id.fragment_accesslog_no_data);
                if (v != null) {
                    v.setVisibility(visibility);
                }
            }
        }

        /**
         * AccessLogListFragment を表示します.
         *
         * @param date 日付の文字列
         */
        private void gotoAccessLogListFragment(String date) {
            gotoFragment(AccessLogListFragment.create(date));
        }

        /**
         * アクセスログ全削除を行います.
         */
        void deleteAll() {
            AccessLogProvider provider = getAccessLogProvider();
            if (provider != null) {
                provider.removeAll((Boolean value) -> getActivity().finish());
            }
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
                        v.postDelayed(() -> mOnItemClickListener.onItemClick(itemView, getAdapterPosition()), 300);
                    }
                });

                // 画面がタッチされた時に Snackbar を非表示にする
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
     * 指定された日付のアクセスログのリストを表示するフラグメント.
     */
    public static class AccessLogListFragment extends BaseFragment {
        /**
         * 削除ダイアログ用リクエストコード.
         */
        private static final int REQUEST_CODE = 101;

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
        private EditText mCondition;

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

        @SuppressLint("ClickableViewAccessibility")
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

            mCondition = root.findViewById(R.id.fragment_search_edit_text);

            RecyclerView recyclerView = root.findViewById(R.id.list_view_accesslog_list);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(mListAdapter);
            recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
            recyclerView.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mListAdapter.dismissSnackbar();
                        break;
                }
                return false;
            });

            ItemTouchHelper helper = new ItemTouchHelper(new SwipeToDeleteCallback(getContext()) {
                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    mListAdapter.onItemRemove(viewHolder, recyclerView);
                }
            });
            helper.attachToRecyclerView(recyclerView);

            root.findViewById(R.id.fragment_search_btn).setOnClickListener((v) -> {
                searchAccessLogs(mCondition.getText().toString());
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            });

            setHasOptionsMenu(true);

            return root;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            inflater.inflate(R.menu.fragment_date_list, menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item){
            switch (item.getItemId()) {
                case R.id.fragment_accesslog_all_delete:
                    new DeleteDialogFragment.Builder()
                            .requestCode(REQUEST_CODE)
                            .title(getString(R.string.fragment_accesslog_delete_accesslog_title))
                            .message(getString(R.string.fragment_accesslog_delete_accesslog_message, getDateString()))
                            .positive(getString(R.string.fragment_accesslog_delete_accesslog_positive))
                            .negative(getString(R.string.fragment_accesslog_delete_accesslog_negative))
                            .show(getFragmentManager(), this);
                    break;
            }
            return true;
        }

        @Override
        public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
            if (requestCode == REQUEST_CODE) {
                if (resultCode == DialogInterface.BUTTON_POSITIVE) {
                    deleteAccessLogs();
                }
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            searchAccessLogs(mCondition.getText().toString());
        }

        @Override
        void onReturn() {
            mListAdapter.dismissSnackbar();
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
         * 指定されている日付のアクセスログを全て削除します.
         */
        private void deleteAccessLogs() {
            String date = getDateString();
            AccessLogProvider provider = getAccessLogProvider();
            if (date != null && provider != null) {
                provider.remove(date, (value) -> popFragment());
            }
        }

        /**
         * アクセスログのリストを更新します.
         *
         * @param accessLogList アクセスログの更新
         */
        private void updateAccessLogList(List<AccessLog> accessLogList) {
            runOnUiThread(() -> {
                if (accessLogList.isEmpty()) {
                    setNoDateView(View.VISIBLE);
                } else {
                    setNoDateView(View.GONE);
                    mListAdapter.updateDataList(accessLogList);
                }
            });
        }

        /**
         * 条件に合うアクセスログの検索を行います.
         *
         * @param condition 条件
         */
        private void searchAccessLogs(String condition) {
            mListAdapter.dismissSnackbar();
            String date = getDateString();
            AccessLogProvider provider = getAccessLogProvider();
            if (provider != null && date != null) {
                if (condition == null || condition.isEmpty()) {
                    provider.getAccessLogsOfDate(date, this::updateAccessLogList);
                } else {
                    provider.getAccessLogsFromCondition(date, condition, this::updateAccessLogList);
                }
            }
        }

        /**
         * データがないことを表示するViewの表示状態を設定します.
         *
         * @param visibility 表示状態
         */
        private void setNoDateView(int visibility) {
            View root = getView();
            if (root != null) {
                View v = root.findViewById(R.id.fragment_accesslog_no_data);
                if (v != null) {
                    v.setVisibility(visibility);
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

        private static final Object[][] METHOD_COLORS = {
                {"get", R.drawable.access_log_method_get},
                {"put", R.drawable.access_log_method_put},
                {"post", R.drawable.access_log_method_post},
                {"delete", R.drawable.access_log_method_delete},
        };

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

            String method = accessLog.getRequestMethod();
            for (Object[] v : METHOD_COLORS) {
                String m = (String) v[0];
                if (m.equalsIgnoreCase(method)) {
                    holder.mMethod.setBackgroundResource((int) v[1]);
                }
            }
            holder.mMethod.setText(method);
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
             * メソッドを表示するTextView.
             */
            TextView mMethod;

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
                mMethod = itemView.findViewById(R.id.item_access_log_method);
                mPath = itemView.findViewById(R.id.item_access_log_path);
                mDate = itemView.findViewById(R.id.item_access_log_date);
                itemView.setOnClickListener((v) -> {
                    if (mOnItemClickListener != null) {
                        v.postDelayed(() -> mOnItemClickListener.onItemClick(itemView, getAdapterPosition()), 300);
                    }
                });

                // 画面がタッチされた時に Snackbar を非表示にする
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
            String path = accessLog.getRequestPath();
            if (path == null) {
                path = "/";
            }
            return path;
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
         * リクエストの詳細を表示するTextView.
         */
        private TextView mRequestView;

        /**
         * レスポンスの詳細を表示するTextView.
         */
        private TextView mResponseView;

        /**
         * リクエストを受信した時刻を表示するTextView.
         */
        private TextView mRequestTimeView;

        /**
         * リクエストのリモート先のIPアドレスを表示するTextView.
         */
        private TextView mIpAddressView;

        /**
         * リクエストのリモート先のホスト名を表示するTextView.
         */
        private TextView mHostNameView;

        /**
         * レスポンスを送信した時刻を表示するTextView.
         */
        private TextView mSendTimeView;

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
            mRequestView = root.findViewById(R.id.fragment_accesslog_detail_request);
            mRequestTimeView = root.findViewById(R.id.fragment_accesslog_detail_request_time);
            mIpAddressView = root.findViewById(R.id.fragment_accesslog_detail_request_ip_address);
            mHostNameView = root.findViewById(R.id.fragment_accesslog_detail_request_host_name);
            mResponseView = root.findViewById(R.id.fragment_accesslog_detail_response);
            mSendTimeView = root.findViewById(R.id.fragment_accesslog_detail_response_time);
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
            if (accessLog == null) {
                return;
            }

            updateAccessLog(AccessLogProvider.dateToString(accessLog.getRequestReceivedTime()),
                    AccessLogProvider.dateToString(accessLog.getResponseSendTime()),
                    accessLog.getRemoteIpAddress(),
                    accessLog.getRemoteHostName(),
                    createRequestString(accessLog),
                    createResponseString(accessLog));
        }

        /**
         * アクセスログからリクエスト詳細の文字列を作成します.
         *
         * @param accessLog アクセスログ
         * @return リクエスト詳細の文字列
         */
        private String createRequestString(AccessLog accessLog) {
            StringBuilder request = new StringBuilder();
            request.append(accessLog.getRequestMethod()).append(" ").append(accessLog.getRequestPath()).append("\r\n");
            Map<String, String> headers = accessLog.getRequestHeader();
            if (headers != null) {
                for (String key : headers.keySet()) {
                    request.append(key).append(": ").append(headers.get(key)).append("\r\n");
                }
            }
            if (accessLog.getRequestBody() != null) {
                request.append("\r\n");
                request.append(accessLog.getRequestBody()).append("\r\n");
            }
            return request.toString();
        }

        /**
         * アクセスログからレスポンス詳細の文字列を作成します.
         *
         * @param accessLog アクセスログ
         * @return レスポンス詳細
         */
        private String createResponseString(AccessLog accessLog) {
            StringBuilder response = new StringBuilder();
            response.append("HTTP/1.1 ").append(accessLog.getResponseStatusCode()).append("\r\n");
            if (accessLog.getResponseContentType() != null) {
                response.append("Content-Type: ").append(accessLog.getResponseContentType()).append("\r\n");
            }
            response.append("\r\n");
            if (accessLog.getResponseBody() != null) {
                if (isContentType(accessLog.getResponseContentType())) {
                    response.append(reshapeJson(accessLog.getResponseBody())).append("\r\n");
                } else {
                    response.append(accessLog.getResponseBody()).append("\r\n");
                }
            }
            return response.toString();
        }

        /**
         * アクセスログの文字列を TextView に反映します.
         *
         * @param receivedTime 受信時刻
         * @param sendTime 送信時刻
         * @param ipAddress IPアドレス
         * @param hostName ホスト名
         * @param request リクエスト
         * @param response レスポンス
         */
        private void updateAccessLog(String receivedTime, String sendTime, String ipAddress, String hostName, String request, String response) {
            runOnUiThread(() -> {
                mRequestTimeView.setText(receivedTime);
                mSendTimeView.setText(sendTime);
                mIpAddressView.setText(ipAddress);
                mHostNameView.setText(hostName);
                mRequestView.setText(request);
                mResponseView.setText(response);
            });
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

    /**
     * 削除確認用のダイアログ.
     */
    public static class DeleteDialogFragment extends DialogFragment {
        /**
         * ダイアログのボタンクリックリスナー.
         */
        private final DialogInterface.OnClickListener mOnClickListener = (dialog, id) -> {
            dismiss();

            Fragment targetFragment = getTargetFragment();
            if (targetFragment != null) {
                targetFragment.onActivityResult(getTargetRequestCode(), id, null);
            }
        };

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            String title = getArguments().getString("title");
            String  message = getArguments().getString("message");
            String  positive = getArguments().getString("positive");
            String  negative = getArguments().getString("negative");

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            if (title != null) {
                builder.setTitle(title);
            }
            if (message != null) {
                builder.setMessage(message);
            }
            builder.setPositiveButton(positive, mOnClickListener);
            if (negative != null) {
                builder.setNegativeButton(negative, mOnClickListener);
            }
            return builder.create();
        }

        /**
         * インスタンス作成用ビルダー.
         */
        public static class Builder {
            /**
             * ダイアログのタイトル.
             */
            private String mTitle;

            /**
             * ダイアログのメッセージ.
             */
            private String mMessage;

            /**
             * ダイアログの Positive ボタンのラベル.
             */
            private String mPositive;

            /**
             * ダイアログの Negative ボタンのラベル.
             */
            private String mNegative;

            /**
             * リクエストコード.
             */
            private int mRequestCode;

            /**
             * リクエストコードを設定します.
             *
             * @param requestCode リクエストコード
             * @return Builder
             */
            Builder requestCode(int requestCode) {
                mRequestCode = requestCode;
                return this;
            }

            /**
             * タイトルを設定します.
             *
             * @param title タイトル
             * @return Builder
             */
            Builder title(String title) {
                mTitle = title;
                return this;
            }

            /**
             * メッセージを設定します.
             *
             * @param message メッセージ
             * @return Builder
             */
            Builder message(String message) {
                mMessage = message;
                return this;
            }

            /**
             * Positive ボタンのラベルを設定します.
             *
             * @param positive Positive ボタンのラベル
             * @return Builder
             */
            Builder positive(String positive) {
                mPositive = positive;
                return this;
            }

            /**
             * Negative ボタンのラベルを設定します.
             *
             * @param negative negative ボタンのラベル
             * @return Builder
             */
            Builder negative(String negative) {
                mNegative = negative;
                return this;
            }

            /**
             * ダイアログを表示します.
             * <p>
             * targetFragment に指定した Fragment の {@link Fragment#onActivityResult(int, int, Intent)}
             * にレスポンスを返却します。
             * </p>
             * @param fragmentManager 表示するFragmentManager
             * @param targetFragment ターゲットとなるFragment
             */
            void show(FragmentManager fragmentManager, Fragment targetFragment) {
                Bundle bundle = new Bundle();
                if (mTitle != null) {
                    bundle.putString("title", mTitle);
                }
                if (mMessage != null) {
                    bundle.putString("message", mMessage);
                }
                if (mPositive != null) {
                    bundle.putString("positive", mPositive);
                }
                if (mNegative != null) {
                    bundle.putString("negative", mNegative);
                }
                DialogFragment dialog = new DeleteDialogFragment();
                dialog.setArguments(bundle);
                dialog.setTargetFragment(targetFragment, mRequestCode);
                dialog.show(fragmentManager, "test");
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
            mBackground = new ColorDrawable(Color.rgb(235, 55, 35));
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
            mDeleteIcon.draw(c);

            super.onChildDraw(c, recyclerView, viewHolder, dx, dy, actionState, isCurrentlyActive);
        }
    }
}
