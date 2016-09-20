/*
 ResultData.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.app.simplebot.data;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.deviceconnect.android.app.simplebot.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 結果データ管理クラス
 */
public class ResultData {

    /** 結果収納キャパシティ */
    private static final int CAPACITY = 100;

    /** シングルトンなResultDataのインスタンス */
    public static final ResultData INSTANCE = new ResultData();

    /** 結果リスト */
    private List<Result> list = new ArrayList<>();

    /** アダプター */
    private ResultAdapter adapter = null;

    /**
     * データクラス
     */
    public static class Result {
        public DataManager.Data data;
        public String text;
        public String channel;
        public String from;
        public String response;
        public String responseUri;
        public Date date = new Date();
    }

    /**
     * 結果追加
     * @param result 結果
     */
    public void add(Result result) {
        // 先頭に追加
        list.add(0, result);
        // 一定数以上は保持しない
        if (list.size() > CAPACITY) {
            list.remove(list.size()-1);
        }
        // UI更新
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }


    /**
     * アダプター取得
     * @param context Context
     * @return アダプター
     */
    public ResultAdapter getAdapter(Context context) {
        if (adapter == null) {
            adapter = new ResultAdapter(context, list);
        }
        return adapter;

    }

    /**
     * 結果アダプター
     */
    public class ResultAdapter extends BaseAdapter {

        List<Result> list = null;
        LayoutInflater inflater;
        static final int resource = R.layout.result_list_item;

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Result getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public ResultAdapter(Context context, List<Result> list) {
            this.list = list;
            this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                v = inflater.inflate(resource, parent, false);
            }
            TextView textCmd = (TextView) v.findViewById(R.id.textResultCommand);
            TextView textRes = (TextView) v.findViewById(R.id.textResultResponse);
            TextView textFrom = (TextView) v.findViewById(R.id.textResultFrom);
            TextView textDate = (TextView) v.findViewById(R.id.textResultDate);
            Result result = this.getItem(position);
            textCmd.setText(result.text);
            textRes.setText(result.response);
            textFrom.setText(result.from);
            textDate.setText(DateFormat.format("yyyy/MM/dd kk:mm:ss", result.date));

            return v;
        }
    }

}
