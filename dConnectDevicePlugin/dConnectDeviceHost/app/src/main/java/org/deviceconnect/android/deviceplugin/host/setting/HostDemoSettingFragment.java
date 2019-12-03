/*
 HostDemoSettingFragment.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.setting;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.demo.DemoInstaller;
import org.deviceconnect.android.deviceplugin.demo.DemoSettingFragment;
import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.demo.HostDemoInstaller;

/**
 * デモページの設定を行うフラグメント.
 *
 * @author NTT DOCOMO, INC.
 */
public class HostDemoSettingFragment extends DemoSettingFragment implements View.OnClickListener {

    private static final String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        Activity activity = getActivity();
        if (activity != null) {
            ((AppCompatActivity) activity).getSupportActionBar().setTitle(getString(R.string.demo_page_settings_title));
        }
    }

    @Override
    protected DemoInstaller createDemoInstaller(final Context context) {
        return new HostDemoInstaller(context);
    }

    @Override
    protected String getDemoDescription(DemoInstaller demoInstaller) {
        return getString(R.string.demo_page_description);
    }

    @Override
    protected int getShortcutIconResource(final DemoInstaller installer) {
        return R.drawable.dconnect_icon;
    }

    @Override
    protected String getShortcutShortLabel(final DemoInstaller installer) {
        return getString(R.string.demo_page_shortcut_label);
    }

    @Override
    protected String getShortcutLongLabel(final DemoInstaller installer) {
        return getString(R.string.demo_page_shortcut_label);
    }

    @Override
    protected String getShortcutUri(final DemoInstaller installer) {
        return "gotapi://shortcut/" + installer.getPluginPackageName() + "/demo/camera/index.html";
    }

    @Override
    protected ComponentName getMainActivity(final Context context) {
        return null;
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

    private void requestPermission(final Context context, final PermissionUtility.PermissionRequestCallback callback) {
        PermissionUtility.requestPermissions(context, getMainHandler(), PERMISSIONS, callback);
    }
}
