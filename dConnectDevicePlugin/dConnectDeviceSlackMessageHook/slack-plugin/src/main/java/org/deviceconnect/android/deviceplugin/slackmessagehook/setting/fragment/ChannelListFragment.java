/*
 ChannelListFragment.java
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
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.deviceconnect.android.deviceplugin.slackmessagehook.R;
import org.deviceconnect.android.deviceplugin.slackmessagehook.slack.SlackManager;

import java.util.List;

/**
 * Channel一覧画面のFragment
 */
public class ChannelListFragment extends ListFragment implements ShowMenuFragment {

    /** メニュー */
    private Menu mainMenu;
    /** アダプター */
    private ChannelAdapter adapter;
    /** 現在の表示位置 */
    private static int currentPos = 0;
    /** 現在の表示位置 */
    private static int currentY = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        final View view = inflater.inflate(R.layout.fragment_channel_list, container, false);
        final Context context = view.getContext();
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
        getChannelList(context, emptyLayout);

        return view;
    }

    /**
     * Channelリストを取得
     * @param context Context
     */
    private void getChannelList(final Context context, final LinearLayout emptyLayout) {
        final SlackManager.FinishCallback<Boolean> finishCallback = new SlackManager.FinishCallback<Boolean>() {
            @Override
            public void onFinish(Boolean retry, Exception error) {
                if (retry) {
                    getChannelList(context, emptyLayout);
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
            // ONLineの場合はChannelリスト取得
            SlackManager.INSTANCE.getAllChannelList(new SlackManager.FinishCallback<List<SlackManager.ListInfo>>() {
                @Override
                public void onFinish(List<SlackManager.ListInfo> listInfos, Exception error) {
                    // プログレスダイアログを閉じる
                    dialog.dismiss();

                    if (error == null) {
                        adapter = new ChannelAdapter(context, listInfos);
                        setListAdapter(adapter);
                        // 以前の表示位置までスクロール
                        getListView().setSelectionFromTop(currentPos, currentY);
                    } else {
                        if (error instanceof SlackManager.SlackAuthException) {
                            // エラー表示
                            Utils.showSlackAuthErrorDialog(context, getFragmentManager(), finishCallback);
                        } else if (error instanceof SlackManager.SlackConnectionException) {
                            // エラー表示
                            Utils.showSlackErrorDialog(context, finishCallback);
                        } else {
                            // エラー表示
                            Utils.showErrorDialog(context, finishCallback);
                        }
                    }
                }
            }, new Handler(Looper.getMainLooper()));
        } else {
            // OFFLineメッセージを表示
            emptyLayout.setVisibility(View.VISIBLE);
            adapter = null;
            setListAdapter(null);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.channel_list_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
        mainMenu = menu;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        currentPos = l.getFirstVisiblePosition();
        currentY = l.getChildAt(0).getTop();
        Fragment fragment = new MessageListFragment();
        SlackManager.ListInfo info = adapter.getItem(position);
        Bundle bundle = new Bundle();
        bundle.putString("name", info.name);
        bundle.putString("id", info.id);
        fragment.setArguments(bundle);
        Utils.transition(fragment, getFragmentManager(), true);
    }

    /**
     * メニューを表示
     */
    public void showMenu() {
        mainMenu.performIdentifierAction(R.id.overflow_options, 0);
    }

    /**
     * アダプター
     */
    public class ChannelAdapter extends BaseAdapter {

        List<SlackManager.ListInfo> list = null;
        LayoutInflater inflater;
        static final int resource = R.layout.list_item_channel;

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public SlackManager.ListInfo getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public ChannelAdapter(Context context, List<SlackManager.ListInfo> list) {
            this.list = list;
            this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                v = inflater.inflate(resource, parent, false);
            }
            Context context = v.getContext();
            TextView text = (TextView) v.findViewById(R.id.textChannelName);
            SlackManager.ListInfo info = this.getItem(position);
            text.setText(info.name);
            ImageView imageView = (ImageView) v.findViewById(R.id.imageIcon);
            Picasso.with(context).cancelRequest(imageView);
            if (info.icon != null) {
                Picasso.with(context).setIndicatorsEnabled(true);
                Picasso.with(context).load(info.icon).into(imageView);
            } else {
                imageView.setImageResource(R.drawable.slack_icon);
            }
            return v;
        }
    }
}
