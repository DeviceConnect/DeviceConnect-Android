/*
 HostRecorderSettingFragment.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.setting;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import org.deviceconnect.android.deviceplugin.host.HostDeviceService;
import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.recorder.AbstractPreviewServerProvider;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceRecorder;
import org.deviceconnect.android.deviceplugin.host.recorder.HostDeviceRecorderManager;
import org.deviceconnect.android.deviceplugin.host.recorder.PreviewServer;
import org.deviceconnect.android.message.DConnectMessageService;

/**
 * レコーダーの設定を行うフラグメント.
 *
 * @author NTT DOCOMO, INC.
 */
public class HostRecorderSettingFragment extends BaseHostSettingPageFragment {

    private static final String PREVIEW_JPEG_MIME_TYPE = "video/x-mjpeg";

    private HostDeviceRecorderManager mRecorderManager;

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
            Intent intent = new Intent(context, HostDeviceService.class);
            context.bindService(intent, new ServiceConnection() {
                @Override
                public void onServiceConnected(final ComponentName name, final IBinder service) {
                    DConnectMessageService.LocalBinder binder = (DConnectMessageService.LocalBinder) service;
                    HostDeviceService hostService = (HostDeviceService) binder.getMessageService();
                    mRecorderManager = hostService.getRecorderManager();

                    ViewGroup container = getView().findViewById(R.id.host_recorder_container);
                    for (HostDeviceRecorder recorder : mRecorderManager.getRecorders())
                        if (recorder instanceof AbstractPreviewServerProvider) {
                            PreviewServer server = ((AbstractPreviewServerProvider) recorder).getServerForMimeType(PREVIEW_JPEG_MIME_TYPE);
                            if (server != null) {
                                View recorderSettingsView = inflater.inflate(R.layout.host_setting_recorder_photo_jpeg_item, null);
                                final TextView nameView = recorderSettingsView.findViewById(R.id.host_recorder_name);
                                final TextView qualityView = recorderSettingsView.findViewById(R.id.host_recorder_photo_jpeg_quality_text);
                                final SeekBar qualityBar = recorderSettingsView.findViewById(R.id.host_recorder_photo_jpeg_quality_seek_bar);
                                nameView.setText(recorder.getName());
                                int q = server.getQuality();
                                qualityView.setText(q + "%");
                                qualityBar.setMax(99);
                                qualityBar.setProgress(q - 1);
                                qualityBar.setTag(recorder.getId());
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

                                        AbstractPreviewServerProvider provider = (AbstractPreviewServerProvider) mRecorderManager.getRecorder((String) seekBar.getTag());
                                        PreviewServer server = provider.getServerForMimeType(PREVIEW_JPEG_MIME_TYPE);
                                        provider.setPreviewQuality(server, q);
                                    }
                                });
                                container.addView(recorderSettingsView);
                            }
                        }
                }

                @Override
                public void onServiceDisconnected(final ComponentName name) {

                }
            }, Context.BIND_AUTO_CREATE);
        }

        return rootView;
    }

}
