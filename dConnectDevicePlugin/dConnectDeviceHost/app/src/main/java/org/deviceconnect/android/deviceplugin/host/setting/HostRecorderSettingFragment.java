/*
 HostRecorderSettingFragment.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.setting;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.recorder.util.RecorderSetting;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

/**
 * レコーダーの設定を行うフラグメント.
 *
 * @author NTT DOCOMO, INC.
 */
public class HostRecorderSettingFragment extends BaseHostSettingPageFragment {

    private RecorderSetting mRecorderSetting;

    private MyAdapter mMyAdapter;

    @Override
    protected String getPageTitle() {
        return getString(R.string.recorder_settings_preview_jpeg_title);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.host_setting_recorder_photo_jpeg, null);

        mRecorderSetting = RecorderSetting.getInstance(getContext());

        List<RecorderSetting.Target> targets = new ArrayList<>();

        for (RecorderSetting.Target target : mRecorderSetting.getTargets()) {
            if (target.getMimeType().startsWith("image/")) {
                targets.add(target);
            }
        }

        mMyAdapter = new MyAdapter(targets);

        ListView listView = rootView.findViewById(R.id.list_view);
        listView.setAdapter(mMyAdapter);

        return rootView;
    }

    private class ViewHolder {
        TextView mNameView;
        TextView mValueView;
        SeekBar mSeekBar;
    }

    private class MyAdapter extends BaseAdapter {

        private List<RecorderSetting.Target> mTargets;

        MyAdapter(List<RecorderSetting.Target> targets) {
            mTargets = targets;
        }

        @Override
        public int getCount() {
            return mTargets.size();
        }

        @Override
        public Object getItem(int position) {
            return mTargets.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.host_setting_recorder_photo_jpeg_item, parent, false);
                holder = new ViewHolder();
                holder.mNameView = convertView.findViewById(R.id.host_recorder_name);
                holder.mValueView = convertView.findViewById(R.id.host_recorder_photo_jpeg_quality_text);
                holder.mSeekBar = convertView.findViewById(R.id.host_recorder_photo_jpeg_quality_seek_bar);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            RecorderSetting.Target target = (RecorderSetting.Target) getItem(position);

            int quality = mRecorderSetting.getJpegQuality(target.getTarget(), 40);

            holder.mNameView.setText(target.getName());
            holder.mValueView.setText(String.valueOf(quality));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                holder.mSeekBar.setMin(1);
            }
            holder.mSeekBar.setMax(100);
            holder.mSeekBar.setProgress(quality);
            holder.mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean forUser) {
                    holder.mValueView.setText(String.valueOf(progress));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    mRecorderSetting.setJpegQuality(target.getTarget(), seekBar.getProgress());
                }
            });

            return convertView;
        }
    }
}
