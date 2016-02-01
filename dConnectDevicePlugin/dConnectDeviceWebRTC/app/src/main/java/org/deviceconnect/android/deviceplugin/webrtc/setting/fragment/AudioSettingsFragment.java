/*
 AudioSettingsFragment.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.webrtc.setting.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.webrtc.R;
import org.deviceconnect.android.deviceplugin.webrtc.setting.SettingUtil;
import org.deviceconnect.android.deviceplugin.webrtc.util.AudioUtils;

/**
 * 音声設定画面.
 *
 * @author NTT DOCOMO, INC.
 */
public class AudioSettingsFragment extends Fragment {

    private SettingsAdapter mAdapter;
    private AudioUtils.AudioFormat mAudioFormat;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        String audio = SettingUtil.getAudioParam(getActivity());
        mAudioFormat = AudioUtils.textToFormat(audio);
        if (mAudioFormat == null) {
            mAudioFormat = AudioUtils.getDefaultFormat();
        }

        mAdapter = new SettingsAdapter();

        View root = inflater.inflate(R.layout.settings_audio, null);
        ListView listView = (ListView) root.findViewById(R.id.listView);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                if (position == 3) {
                    CheckBox cb = (CheckBox) view.findViewById(R.id.checkbox_id);
                    if (cb != null) {
                        cb.setChecked(mAudioFormat.isNoAudioProcessing());
                    }
                }
            }
        });
        return root;
    }

    private void setEnabledAudioProcess(final boolean enabled) {
        mAudioFormat.setNoAudioProcessing(!enabled);
        SettingUtil.setAudioParam(getActivity(), AudioUtils.formatToText(mAudioFormat));
    }

    private class SettingsAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        private SettingsAdapter() {
            mInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public Object getItem(final int position) {
            return null;
        }

        @Override
        public long getItemId(final int position) {
            return position;
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            View view = null;
            if (position == 0) {
                view = mInflater.inflate(R.layout.item_settings_param, parent, false);
                setViewText(view, getString(R.string.settings_audio_sample_rate), "" + mAudioFormat.getSampleRate());
            } else if (position == 1) {
                view = mInflater.inflate(R.layout.item_settings_param, parent, false);
                String value;
                if (mAudioFormat.getChannels() == AudioUtils.MONO) {
                    value = getString(R.string.settings_audio_channels_mono);
                } else {
                    value = getString(R.string.settings_audio_channels_stereo);
                }
                setViewText(view, getString(R.string.settings_audio_channels), value);
            } else if (position == 2) {
                view = mInflater.inflate(R.layout.item_settings_param, parent, false);
                String value;
                if (mAudioFormat.getBitDepth() == AudioUtils.BIT_DEPTH_8BYTE) {
                    value = getString(R.string.settings_audio_bit_depth_8);
                } else if (mAudioFormat.getBitDepth() == AudioUtils.BIT_DEPTH_16SHORT) {
                    value = getString(R.string.settings_audio_bit_depth_16);
                } else {
                    value = getString(R.string.settings_audio_bit_depth_32);
                }
                setViewText(view, getString(R.string.settings_audio_bit_depth), value);
            } else if (position == 3) {
                view = mInflater.inflate(R.layout.item_settings_checkbox, parent, false);
                CheckBox cb = (CheckBox) view.findViewById(R.id.checkbox_id);
                cb.setChecked(!mAudioFormat.isNoAudioProcessing());
                cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                        setEnabledAudioProcess(isChecked);
                    }
                });
                String title = getString(R.string.settings_audio_processing);
                String description = getString(R.string.settings_audio_processing_description);
                setViewText(view, title, description);
            }
            return view;
        }

        private void setViewText(final View view, final String title, final String value) {
            TextView titleView = (TextView) view.findViewById(R.id.title);
            if (titleView != null) {
                titleView.setText(title);
            }

            TextView valueView = (TextView) view.findViewById(R.id.value);
            if (valueView != null) {
                valueView.setText(value);
            }
        }
    }
}
