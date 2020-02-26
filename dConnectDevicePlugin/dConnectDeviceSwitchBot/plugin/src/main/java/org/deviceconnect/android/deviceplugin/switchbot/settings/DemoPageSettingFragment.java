package org.deviceconnect.android.deviceplugin.switchbot.settings;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.demo.DemoInstaller;
import org.deviceconnect.android.deviceplugin.demo.DemoSettingFragment;
import org.deviceconnect.android.deviceplugin.switchbot.BuildConfig;
import org.deviceconnect.android.deviceplugin.switchbot.R;
import org.deviceconnect.android.deviceplugin.switchbot.demo.SwitchBotDemoInstaller;

public class DemoPageSettingFragment extends DemoSettingFragment {
    private static final String TAG = "SwitchBotDemoSetting";
    private static final Boolean DEBUG = BuildConfig.DEBUG;

    private static final String FILE_PROVIDER_AUTHORITY = "org.deviceconnect.android.deviceplugin.switchbot.provider";

    private static final String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        Activity activity = getActivity();
        if (activity != null) {
            ActionBar actionBar = ((AppCompatActivity) activity).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(getString(R.string.demo_page_settings_title));
            }
        }
    }

    @Override
    protected DemoInstaller createDemoInstaller(final Context context) {
        return new SwitchBotDemoInstaller(context);
    }

    @Override
    protected String getDemoDescription(DemoInstaller demoInstaller) {
        return getString(R.string.demo_page_description);
    }

    @Override
    protected int getShortcutIconResource(final DemoInstaller installer) {
        return R.drawable.ic_launcher;
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
        String rootPath;
        String filePath = "/demo/index.html";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            rootPath = "/" + FILE_PROVIDER_AUTHORITY;
        } else {
            rootPath = "/" + installer.getPluginPackageName();
        }
        if (DEBUG) {
            Log.d(TAG, "shortcut uri : " + "gotapi://shortcut" + rootPath + filePath);
        }
        return "gotapi://shortcut" + rootPath + filePath;
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
