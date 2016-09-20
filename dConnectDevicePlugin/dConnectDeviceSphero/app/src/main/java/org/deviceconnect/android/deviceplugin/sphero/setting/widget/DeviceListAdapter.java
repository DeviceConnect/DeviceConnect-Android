/*
 DeviceListAdapter.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.sphero.setting.widget;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.sphero.R;
import org.deviceconnect.android.deviceplugin.sphero.data.SpheroParcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * デバイス一覧用のアダプタ.
 * @author NTT DOCOMO, INC.
 */
public class DeviceListAdapter extends ArrayAdapter<SpheroParcelable> implements OnClickListener {
    
    /** 
     * レイアウトインフレーター.
     */
    private LayoutInflater mInflater;
    
    /** 
     * クリックリスナー.
     */
    private OnConnectButtonClickListener mListener;
    
    /**
     * アダプタを生成する.
     * 
     * @param context コンテキスト
     */
    public DeviceListAdapter(final Context context) {
        this(context, new ArrayList<SpheroParcelable>());
    }

    /**
     * アダプタを生成する.
     * 
     * @param context コンテキスト
     * @param devices デバイスリスト
     */
    public DeviceListAdapter(final Context context, final List<SpheroParcelable> devices) {
        super(context, R.layout.list_item_layout, devices);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        
        View view = null;
        if (convertView == null) {
            view = mInflater.inflate(R.layout.list_item_layout, null);
        } else {
            view = convertView;
        }

        SpheroParcelable device = getItem(position);
        if (device != null) {
            TextView name = (TextView) view.findViewById(R.id.name);
            name.setText(device.getSpheroName());
            
            Button connBtn = (Button) view.findViewById(R.id.connect);
            connBtn.setOnClickListener(this);
            connBtn.setTag(position);
            if (device.isConnected() == SpheroParcelable.SpheroState.Connected) {
                connBtn.setText(R.string.state_off);
            } else if (device.isConnected() == SpheroParcelable.SpheroState.Disconnected
                    || device.isConnected() == SpheroParcelable.SpheroState.Remember) {
                connBtn.setText(R.string.state_on);
            } else if (device.isConnected() == SpheroParcelable.SpheroState.Delete){
                connBtn.setText(R.string.state_remove);
            }
        }
        
        return view;
    }
    
    /**
     * アダプタが管理しているデータを取得する.
     * 
     * @param device 検索データ
     * @return 実データ. 0 : データ, 1 : インデックス
     */
    private Object[] getRowData(final SpheroParcelable device) {
        
        for (int i = 0; i < getCount(); i++) {
            SpheroParcelable s = getItem(i);
            if (s.getSpheroId().equals(device.getSpheroId())) {
                return new Object[]{s, i};
            }
        }
        
        return null;
    }
    
    @Override
    public void remove(final SpheroParcelable object) {
        Object[] data = getRowData(object);
        if (data != null) {
            super.remove((SpheroParcelable) data[0]);
        }
    }
    /**
     * リスナーを設定する.
     * 
     * @param listener リスナー
     */
    public void setOnConnectButtonClickListener(final OnConnectButtonClickListener listener) {
        mListener = listener;
    }

    /**
     * 接続(解除)ボタン押下イベントリスナー.
     */
    public interface OnConnectButtonClickListener {
        
        /**
         * ボタンが押された場合に呼び出される.
         * 
         * @param position 押されたボタンの行番号
         * @param device 選択された行のデータ
         */
        void onClicked(int position, SpheroParcelable device);
    }

    @Override
    public void onClick(final View v) {
        if (mListener != null) {
            int position = (Integer) v.getTag();
            SpheroParcelable device = getItem(position);
            mListener.onClicked(position, device);
        }
    }
    
    /**
     * 接続状態の表示を切り替える.
     * 
     * @param device デバイス
     */
    public void changeConnectionState(final SpheroParcelable device) {
        Object[] data = getRowData(device);
        if (data != null) {
            ((SpheroParcelable) data[0]).setConnected(device.isConnected());
            notifyDataSetChanged();
        }
    }
    
}
