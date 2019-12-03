/*
 HostRecorderSettingFragment.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.setting;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.recorder.util.RecorderSettingData;

/**
 * レコーダーの設定を行うフラグメント.
 *
 * @author NTT DOCOMO, INC.
 */
public class HostRecorderSettingFragment extends BaseHostSettingPageFragment {
    @Override
    protected String getPageTitle() {
        return getString(R.string.recorder_settings_preview_jpeg_title);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.host_setting_recorder_photo_jpeg, null);

        Context context = getContext();
        if (context != null) {
            ViewGroup c = rootView.findViewById(R.id.host_recorder_container);
            final RecorderSettingData setting = RecorderSettingData.getInstance(getContext().getApplicationContext());
            setting.loadTargetIds();
            final String[] targets = setting.getTargets();
            for (int i = 0; i < targets.length; i++) {
                View recorderSettingsView = inflater.inflate(R.layout.host_setting_recorder_photo_jpeg_item, null);
                final TextView nameView = recorderSettingsView.findViewById(R.id.host_recorder_name);
                final TextView qualityView = recorderSettingsView.findViewById(R.id.host_recorder_photo_jpeg_quality_text);
                final SeekBar qualityBar = recorderSettingsView.findViewById(R.id.host_recorder_photo_jpeg_quality_seek_bar);
                nameView.setText(setting.readPreviewName(targets[i]));
                int q = setting.readPreviewQuality(targets[i]);
                qualityView.setText(q + "%");
                qualityBar.setMax(99);
                qualityBar.setProgress(q - 1);
                qualityBar.setTag(targets[i]);
                qualityBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
                        qualityView.setText((progress + 1) + "%");
                    }

                    @Override
                    public void onStartTrackingTouch(final SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(final SeekBar seekBar) {
                        int q = seekBar.getProgress() + 1;
                        qualityView.setText(q + "%");
                        String tag = (String) seekBar.getTag();
                        setting.storePreviewQuality(tag, q);
                    }
                });
                c.addView(recorderSettingsView);
            }
        }

        return rootView;
    }

}
