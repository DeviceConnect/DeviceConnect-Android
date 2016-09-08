/*
 MessageListFragment.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.slackmessagehook.setting.fragment;

import android.app.Fragment;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.deviceconnect.android.deviceplugin.slackmessagehook.BuildConfig;
import org.deviceconnect.android.deviceplugin.slackmessagehook.R;
import org.deviceconnect.android.deviceplugin.slackmessagehook.slack.SlackManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * メッセージ一覧画面のFragment
 */
public class MessageListFragment extends ListFragment implements SlackManager.SlackEventListener {

    /** TAG. */
    private final static String TAG = "MessageListFragment";
    /** アダプター */
    private MessageAdapter mAdapter;
    /** Picasso */
    private Picasso mPicasso;
    /** ChannelID */
    private String mChannelId;
    /** ユーザー情報 */
    private HashMap<String, SlackManager.ListInfo> mUserMap;
    /** 最後のセルを表示中かどうか */
    private boolean mIsLastCell = false;
    /** 読み込み不可能フラグ */
    boolean mCannotLoading = false;

    //---------------------------------------------------------------------------------------
    //region View

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message_list, container, false);
        final Context context = view.getContext();
        final TextView emptyText = (TextView)view.findViewById(android.R.id.empty);
        TextView titleText = (TextView)view.findViewById(R.id.textViewTitle);

        // OFFLineメッセージを非表示
        LinearLayout emptyLayout = (LinearLayout)view.findViewById(R.id.empty);
        emptyLayout.setVisibility(View.GONE);
        // 設定ボタンイベント
        Button emptyButton = (Button)view.findViewById(R.id.emptyButton);
        emptyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 設定画面へ
                Fragment fragment = new SettingFragment();
                Utils.transition(fragment, getFragmentManager(), true);
            }
        });

        // スクロールイベント
        final ListView listView = (ListView) view.findViewById(android.R.id.list);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {}
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // 最後のセルを表示した
                mIsLastCell = (totalItemCount != 0 && totalItemCount == firstVisibleItem + visibleItemCount);
                // 先頭のセルを表示した
                if (firstVisibleItem == 0 && !mCannotLoading && mAdapter != null) {
                    mCannotLoading = true;
                    SlackManager.HistoryInfo info = mAdapter.getItem(0);
                    // 続きの履歴を取得
                    SlackManager.INSTANCE.getHistory(mChannelId, info.ts, new SlackManager.FinishCallback<ArrayList<SlackManager.HistoryInfo>>() {
                        @Override
                        public void onFinish(ArrayList<SlackManager.HistoryInfo> historyInfos, Exception error) {
                            if (error == null) {
                                // もう続きがない
                                if (historyInfos.size() == 0) {
                                    mCannotLoading = true;
                                    return;
                                }
                                mCannotLoading = false;
                                // フォーマット変換
                                for (Object obj : historyInfos) {
                                    formatHistory((SlackManager.HistoryInfo)obj);
                                }
                                // 表示順を逆順に
                                Collections.reverse(historyInfos);
                                // 最初に挿入
                                mAdapter.insertToFirst(historyInfos);
                                mAdapter.notifyDataSetChanged();
                                // 挿入前の位置まで移動
                                getListView().setSelection(historyInfos.size());
                            } else {
                                Log.e(TAG, "Error on getHistory", error);
                            }
                        }
                    });
                }
            }
        });

        // イベントリスナーを登録
        SlackManager.INSTANCE.addSlackEventListener(this);

        // Picassoで認証するためにヘッダを追加
        final String token = Utils.getAccessToken(context);
        File cacheDir = Utils.getCacheDir(context);
        OkHttpClient httpClient = new OkHttpClient.Builder().cache(new Cache(cacheDir, Integer.MAX_VALUE)).addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request newRequest = chain.request().newBuilder().
                        cacheControl(new CacheControl.Builder().maxStale(365, TimeUnit.DAYS).build()).
                        addHeader("Authorization", "Bearer " + token).build();
                return chain.proceed(newRequest);
            }
        }).build();
        mPicasso = new Picasso.Builder(context)
                .downloader(new OkHttp3Downloader(httpClient))
                .build();
        if (BuildConfig.DEBUG) mPicasso.setIndicatorsEnabled(true);
        if (BuildConfig.DEBUG) mPicasso.setLoggingEnabled(true);

        // パラメータ取得
        Bundle bundle = getArguments();
        String title = bundle.getString("name");
        mChannelId = bundle.getString("id");
        // タイトル設定
        titleText.setText(title);

        // 送信ボタンイベント
        final EditText editText = (EditText)view.findViewById(R.id.editText);
        Button sendButton = (Button)view.findViewById(R.id.buttonSend);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = editText.getText().toString();
                if (msg.length() > 0) {
                    SlackManager.INSTANCE.sendMessage(msg, mChannelId);
                    editText.setText(null);
                    //
                    SlackManager.HistoryInfo history = new SlackManager.HistoryInfo();
                    history.text = msg;
                    history.channel = mChannelId;
                    history.ts = new Date().getTime() / 1000.0;
                    history.user = SlackManager.INSTANCE.getBotInfo().id;
                    // 名前とアイコン
                    SlackManager.ListInfo info = mUserMap.get(history.user);
                    if (info != null) {
                        history.name = info.name;
                        history.icon = info.icon;
                    }
                    mAdapter.add(history);
                    // 最後の行を表示
                    getListView().setSelection(mAdapter.getCount());
                }
            }
        });

        getMessageList(context, listView, emptyText, emptyLayout);

        return view;
    }

    /**
     * メッセージ一覧を取得
     * @param context Context
     * @param listView ListView
     * @param emptyText EmptyText
     * @param emptyLayout EmptyLayout
     */
    private void getMessageList(final Context context, final ListView listView, final TextView emptyText, final LinearLayout emptyLayout) {
        final SlackManager.FinishCallback<Boolean> finishCallback = new SlackManager.FinishCallback<Boolean>() {
            @Override
            public void onFinish(Boolean retry, Exception error) {
                if (retry) {
                    getMessageList(context, listView, emptyText, emptyLayout);
                }
            }
        };
        // ネットワーク接続チェック
        if (!Utils.onlineCheck(context)) {
            // エラー表示
            Utils.showNetworkErrorDialog(context, finishCallback);
            return;
        }

        if (SlackManager.INSTANCE.isConnected()) {
            // プログレスダイアログを表示
            final ProgressDialog dialog = Utils.showProgressDialog(context);
            final Handler handler = new Handler();

            new Thread() {
                @Override
                public void run() {
                    final CountDownLatch latch = new CountDownLatch(2);
                    final HashMap<String, ArrayList> resMap = new HashMap<>();
                    final Exception[] err = new Exception[1];

                    // ユーザーリスト取得
                    SlackManager.INSTANCE.getUserList(new SlackManager.FinishCallback<ArrayList<SlackManager.ListInfo>>() {
                        @Override
                        public void onFinish(ArrayList<SlackManager.ListInfo> listInfos, Exception error) {
                            if (error == null) {
                                resMap.put("user", listInfos);
                            } else {
                                err[0] = error;
                                Log.e(TAG, "Error on getUserList", error);
                            }
                            latch.countDown();
                        }
                    });

                    // 履歴を取得
                    SlackManager.INSTANCE.getHistory(mChannelId, null, new SlackManager.FinishCallback<ArrayList<SlackManager.HistoryInfo>>() {
                        @Override
                        public void onFinish(ArrayList<SlackManager.HistoryInfo> historyInfos, Exception error) {
                            if (error == null) {
                                resMap.put("history", historyInfos);
                            } else {
                                err[0] = error;
                                Log.e(TAG, "Error on getHistory", error);
                            }
                            latch.countDown();
                        }
                    });

                    // 処理終了を待つ
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // メッセージを整える
                    ArrayList users = resMap.get("user");
                    final ArrayList histories = resMap.get("history");
                    if (users != null && histories != null) {
                        // UserをHashMapへ
                        mUserMap = new HashMap<>();
                        for (Object obj : users) {
                            SlackManager.ListInfo info = (SlackManager.ListInfo)obj;
                            mUserMap.put(info.id, info);
                        }
                        for (Object obj : histories) {
                            formatHistory((SlackManager.HistoryInfo)obj);
                        }

                        // 表示順を逆順に
                        Collections.reverse(histories);

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (getView()==null) return;
                                // アダプターを生成
                                mAdapter = new MessageAdapter(context, histories);
                                setListAdapter(mAdapter);
                                // 最後の行を表示
                                listView.setSelection(histories.size());
                            }
                        });

                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (getView()==null) return;
                                if (err[0] != null && err[0] instanceof SlackManager.SlackAuthException) {
                                    // エラー表示
                                    Utils.showSlackAuthErrorDialog(context, getFragmentManager(), finishCallback);
                                } else if (err[0] != null && err[0] instanceof SlackManager.SlackConnectionException) {
                                    // エラー表示
                                    Utils.showSlackErrorDialog(context, finishCallback);
                                } else {
                                    // エラー表示
                                    Utils.showErrorDialog(context, finishCallback);
                                }
                            }
                        });
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (getView()==null) return;
                            // プログレスダイアログを閉じる
                            dialog.dismiss();
                            // 空メッセージを設定
                            emptyText.setText(context.getString(R.string.empty_message));
                        }
                    });
                }
            }.start();
        } else {
            // OFFLineメッセージを表示
            emptyLayout.setVisibility(View.VISIBLE);
            setListAdapter(null);
        }
    }

    @Override
    public void onDestroyView() {
        // イベントリスナーを解除
        SlackManager.INSTANCE.removeSlackEventListener(this);
        super.onDestroyView();
    }

    //endregion
    //---------------------------------------------------------------------------------------
    //region SlackEvent

    @Override
    public void OnConnect() {

    }

    @Override
    public void OnConnectLost() {

    }

    @Override
    public void OnReceiveSlackMessage(SlackManager.HistoryInfo info) {
        if (!info.channel.equals(mChannelId)) return;
        formatHistory(info);
        mAdapter.add(info);
        mAdapter.notifyDataSetChanged();
        if (mIsLastCell) {
            // 最後の行を表示
            getListView().setSelection(mAdapter.getCount());
        }
    }

    //endregion
    //---------------------------------------------------------------------------------------
    //region etc.

    /**
     * 履歴情報をフォーマットする
     * @param history 履歴
     */
    private void formatHistory(SlackManager.HistoryInfo history) {
        // 名前とアイコン
        SlackManager.ListInfo info = mUserMap.get(history.user);
        if (info != null) {
            history.name = info.name;
            history.icon = info.icon;
        }
        // メンション処理
        if (history.text == null) return;
        Pattern p = Pattern.compile("<@(\\w*)>");
        Matcher m = p.matcher(history.text);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String uid = m.group(1);
            info = mUserMap.get(uid);
            if (info != null) {
                m.appendReplacement(sb, "@" + info.name);
            } else {
                m.appendReplacement(sb, m.group());
            }
        }
        m.appendTail(sb);
        history.text = sb.toString();
        // <@UserID|UserName>の形式をUserNameのみに置き換え
        p = Pattern.compile("<@\\w*\\|([\\w._-]*)>");
        m = p.matcher(history.text);
        sb = new StringBuffer();
        if (m.find()) {
            m.appendReplacement(sb, m.group(1));
        }
        m.appendTail(sb);
        history.text = sb.toString();
    }

    /**
     * 履歴リストアダプター
     */
    public class MessageAdapter extends BaseAdapter {

        /** ListItemのID */
        static final int sResource = R.layout.list_item_message;
        /** 履歴リスト */
        List<SlackManager.HistoryInfo> mList = null;
        /** inflater */
        LayoutInflater mInflater;

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public SlackManager.HistoryInfo getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public MessageAdapter(Context context, List<SlackManager.HistoryInfo> list) {
            this.mList = list;
            this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        /**
         * 履歴を追加する
         * @param info 履歴
         */
        public void add(SlackManager.HistoryInfo info) {
            mList.add(info);
        }

        /**
         * 履歴を最初に追加する
         * @param infos 履歴
         */
        public void insertToFirst(List<SlackManager.HistoryInfo> infos) {
            mList.addAll(0, infos);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                v = mInflater.inflate(sResource, parent, false);
            }
            Context context = v.getContext();
            SlackManager.HistoryInfo info = this.getItem(position);

            boolean myself = false;
            if (info.user != null) {
                myself = info.user.equals(SlackManager.INSTANCE.getBotInfo().id);
            }
            RelativeLayout rLayout = (RelativeLayout) v.findViewById(R.id.rightLayout);
            RelativeLayout lLayout = (RelativeLayout) v.findViewById(R.id.leftLayout);
            TextView textMessage;
            TextView textDate;
            ImageView iconImage;
            final ImageView imageImage;
            // 自分と他人では項目の配置が反対になる
            if (myself) {
                rLayout.setVisibility(View.VISIBLE);
                lLayout.setVisibility(View.GONE);
                textMessage = (TextView) v.findViewById(R.id.textMessageR);
                textDate = (TextView) v.findViewById(R.id.textDateR);
                iconImage = (ImageView) v.findViewById(R.id.imageIconR);
                imageImage = (ImageView) v.findViewById(R.id.imageImageR);
            } else {
                rLayout.setVisibility(View.GONE);
                lLayout.setVisibility(View.VISIBLE);
                textMessage = (TextView) v.findViewById(R.id.textMessage);
                textDate = (TextView) v.findViewById(R.id.textDate);
                iconImage = (ImageView) v.findViewById(R.id.imageIcon);
                imageImage = (ImageView) v.findViewById(R.id.imageImage);
            }

            // メッセージ
            if (info.text != null) {
                textMessage.setVisibility(View.VISIBLE);
                textMessage.setText(info.text);
            } else {
                textMessage.setVisibility(View.GONE);
            }
            // 日時を表示
            Date date = new Date((long) (info.ts * 1000));
            Calendar cal = Calendar.getInstance();
            int thisDayOfYear = cal.get(Calendar.DAY_OF_YEAR);
            cal.setTime(date);
            int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
            SimpleDateFormat format;
            if (thisDayOfYear == dayOfYear) {
                // 今日なら時間
                format = new SimpleDateFormat("hh:mm", Locale.ENGLISH);
            } else {
                // 月日
                format = new SimpleDateFormat("MM/dd", Locale.ENGLISH);
            }
            textDate.setText(format.format(date));
            // アイコン
            mPicasso.cancelRequest(iconImage);
            if (info.icon != null) {
                mPicasso.load(info.icon).error(android.R.drawable.ic_delete).into(iconImage);
            } else {
                iconImage.setImageResource(R.drawable.slack_icon);
            }
            // イメージ
            mPicasso.cancelRequest(imageImage);
            if (info.thumb != null) {
                imageImage.setVisibility(View.VISIBLE);
                imageImage.setLayoutParams(new LinearLayout.LayoutParams(info.thumbWidth, info.thumbHeight));
                mPicasso.load(info.thumb).error(android.R.drawable.ic_delete).into(imageImage);
            } else {
                imageImage.setVisibility(View.GONE);
            }

            return v;
        }
    }
    //endregion
    //---------------------------------------------------------------------------------------
}
