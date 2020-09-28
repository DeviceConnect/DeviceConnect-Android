/*
 MidiDemoSettingFragment.java
 Copyright (c) 2020 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.midi.fragment;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.demo.DemoInstaller;
import org.deviceconnect.android.deviceplugin.demo.DemoSettingFragment;
import org.deviceconnect.android.deviceplugin.midi.DConnectMidiSettingsListActivity;
import org.deviceconnect.android.deviceplugin.midi.MidiDemoInstaller;
import org.deviceconnect.android.deviceplugin.midi.R;

/**
 * This fragment do setting of the MIDI demo setting.
 *
 * @author NTT DOCOMO, INC.
 */
public class MidiDemoSettingFragment extends DemoSettingFragment {

    private static final String FILE_PROVIDER_AUTHORITY = "org.deviceconnect.android.deviceplugin.midi.provider";

    private static final String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected DemoInstaller createDemoInstaller(final Context context) {
        return new MidiDemoInstaller(context);
    }

    @Override
    protected String getDemoDescription(final DemoInstaller demoInstaller) {
        return getString(R.string.demo_page_description);
    }

    @Override
    protected int getShortcutIconResource(final DemoInstaller demoInstaller) {
        return R.drawable.ic_launcher;
    }

    @Override
    protected String getShortcutShortLabel(final DemoInstaller demoInstaller) {
        return getString(R.string.demo_page_shortcut_label);
    }

    @Override
    protected String getShortcutLongLabel(final DemoInstaller demoInstaller) {
        return getString(R.string.demo_page_shortcut_label);
    }

    @Override
    protected String getShortcutUri(final DemoInstaller demoInstaller) {
        String rootPath;
        String filePath = "/demo/index.html";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            rootPath = "/" + FILE_PROVIDER_AUTHORITY;
        } else {
            rootPath = "/" + demoInstaller.getPluginPackageName();
        }
        return "gotapi://shortcut" + rootPath + filePath;
    }

    @Override
    protected ComponentName getMainActivity(final Context context) {
        return new ComponentName(context, DConnectMidiSettingsListActivity.class);
    }

    @Override
    protected void onInstall(final Context context, final boolean createsShortcut) {
        requestPermission(context, new PermissionUtility.PermissionRequestCallback() {
            @Override
            public void onSuccess() {
                install(createsShortcut);
            }

            @Override
            public void onFail(final @NonNull String deniedPermission) {
                showInstallErrorDialog("Denied permission: " + deniedPermission);
            }
        });
    }

    @Override
    protected void onOverwrite(final Context context) {
        requestPermission(context, new PermissionUtility.PermissionRequestCallback() {
            @Override
            public void onSuccess() {
                overwrite();
            }

            @Override
            public void onFail(final @NonNull String deniedPermission) {
                showOverwriteErrorDialog("Denied permission: " + deniedPermission);
            }
        });
    }

    @Override
    protected void onUninstall(final Context context) {
        requestPermission(context, new PermissionUtility.PermissionRequestCallback() {
            @Override
            public void onSuccess() {
                uninstall();
            }

            @Override
            public void onFail(final @NonNull String deniedPermission) {
                showUninstallErrorDialog("Denied permission: " + deniedPermission);
            }
        });
    }

    private void requestPermission(final Context context,
                                   final PermissionUtility.PermissionRequestCallback callback) {
        PermissionUtility.requestPermissions(context, getMainHandler(), PERMISSIONS, callback);
    }
}
