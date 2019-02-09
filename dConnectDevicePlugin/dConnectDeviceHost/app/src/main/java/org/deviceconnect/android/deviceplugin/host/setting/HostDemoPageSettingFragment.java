/*
 HostDemoPageSettingFragment.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.setting;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.pm.ShortcutInfoCompat;
import android.support.v4.content.pm.ShortcutManagerCompat;
import android.support.v4.graphics.drawable.IconCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * デモページの設定を行うフラグメント.
 *
 * @author NTT DOCOMO, INC.
 */
public class HostDemoPageSettingFragment extends BaseHostSettingPageFragment implements View.OnClickListener {

    private static final boolean DEBUG = BuildConfig.DEBUG;

    private static final String TAG = "host.dplugin";

    private static final String DOCUMENT_DIR_NAME = "org.deviceconnect.android.manager";

    private static final String PLUGIN_DIR_NAME = "org.deviceconnect.android.deviceplugin.host";

    private static final String PREFERENCE_NAME =  "demo_page_info";

    private static final String KEY_PLUGIN_VERSION_NAME = "plugin_version_name";

    private static final String CAMERA_DEMO_SHORTCUT_ID = "1";

    private static final String TAG_INSTALL = "install";

    private static final String TAG_DELETION = "deletion";

    private static final String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    private Button mDeleteButton;

    private Button mInstallButton;

    private Button mOpenButton;

    private Button mCreateShortcutButton;

    private Handler mHandler;

    private final Executor mExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected String getPageTitle() {
        return getString(R.string.demo_page_settings_title);
    }

    @Nullable
    @Override
    public View onCreateView(final @NonNull LayoutInflater inflater, final @Nullable ViewGroup container,
                             final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.host_setting_demo_page, null);
        mHandler = new Handler(Looper.getMainLooper());
        mDeleteButton = rootView.findViewById(R.id.button_delete_demo_page);
        mDeleteButton.setOnClickListener(this);
        mInstallButton = rootView.findViewById(R.id.button_install_demo_page);
        mInstallButton.setOnClickListener(this);
        mOpenButton = rootView.findViewById(R.id.button_open_demo_page);
        mOpenButton.setOnClickListener(this);
        mCreateShortcutButton = rootView.findViewById(R.id.button_create_demo_page_shortcut);
        mCreateShortcutButton.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateView(getActivity());
    }

    @Override
    public void onClick(final View v) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        if (v == mDeleteButton) {
            showDeletionDialog(activity);
        } else if (v == mInstallButton) {
            showInstallDialog(activity);
        } else if (v == mOpenButton) {
            openDemoPage(activity);
        } else if (v == mCreateShortcutButton) {
            createShortcut(activity);
        }
        updateView(activity);
    }

    private void showInstallDialog(final Activity activity) {
        String title = getString(R.string.demo_page_settings_title_install);
        String positive = getString(R.string.demo_page_settings_button_install);
        String negative = getString(R.string.demo_page_settings_button_cancel);

        LayoutInflater inflater = activity.getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_host_demo_page_install, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setView(view);
        builder.setPositiveButton(positive,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        onPositiveButton(TAG_INSTALL, view);
                    }
                });
        builder.setNegativeButton(negative,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        onNegativeButton(TAG_INSTALL, view);
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDeletionDialog(final Activity activity) {
        String title = getString(R.string.demo_page_settings_title_delete);
        String positive = getString(R.string.demo_page_settings_button_delete);
        String negative = getString(R.string.demo_page_settings_button_cancel);

        LayoutInflater inflater = activity.getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_host_demo_page_delete, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setView(view);
        builder.setPositiveButton(positive,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        onPositiveButton(TAG_DELETION, view);
                    }
                });
        builder.setNegativeButton(negative,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        onNegativeButton(TAG_DELETION, view);
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void onPositiveButton(final String tag, final View dialogView) {
        final Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        final File demoDir = getDemoDir();
        if (TAG_INSTALL.equals(tag)) {
            requestPermission(activity, new PermissionUtility.PermissionRequestCallback() {
                @Override
                public void onSuccess() {
                    // デモページをインストール
                    asyncTask(new InstallTask(activity.getAssets(), "demo/camera", getDemoDir(), mHandler) {
                        @Override
                        protected void onBeforeTask() {
                            if (DEBUG) {
                                Log.d(TAG, "Start to install demo: path=" + demoDir.getAbsolutePath());
                            }
                        }

                        @Override
                        protected void onAfterTask() {
                            if (DEBUG) {
                                Log.d(TAG, "Installed demo: path=" + demoDir.getAbsolutePath());
                            }

                            storeInstalledVersion(activity, getCurrentVersionName(activity));
                            updateView(activity);

                            CheckBox checkBox = dialogView.findViewById(R.id.checkbox_create_shortcut);
                            if (checkBox.isChecked() && isCreatedShortcut(activity)) {
                                createShortcut(activity);
                            }
                        }

                        @Override
                        protected void onUnexpectedError(final Throwable e) {
                            if (DEBUG) {
                                Log.e(TAG, "Failed to install demo on external storage.", e);
                            }
                            // TODO インストール失敗を表示
                        }
                    });
                }

                @Override
                public void onFail(final @NonNull String deniedPermission) {
                    // TODO インストール失敗を表示
                }
            });
        } else if (TAG_DELETION.equals(tag)) {
            requestPermission(activity, new PermissionUtility.PermissionRequestCallback() {
                @Override
                public void onSuccess() {
                    // デモページをアンインストール
                    asyncTask(new UninstallTask(demoDir, mHandler) {
                        @Override
                        protected void onBeforeTask() {
                            if (DEBUG) {
                                Log.d(TAG, "Start to uninstall demo: path=" + demoDir.getAbsolutePath());
                            }
                        }

                        @Override
                        protected void onAfterTask() {
                            if (DEBUG) {
                                Log.d(TAG, "Uninstalled demo: path=" + demoDir.getAbsolutePath());
                            }

                            deleteShortcut(activity);
                            storeInstalledVersion(activity, null);
                            updateView(activity);
                        }

                        @Override
                        protected void onUnexpectedError(final Throwable e) {
                            if (DEBUG) {
                                Log.e(TAG, "Failed to delete demo from external storage.");
                            }
                            // TODO 削除失敗を表示
                        }
                    });
                }

                @Override
                public void onFail(final @NonNull String deniedPermission) {
                    // TODO 削除失敗を表示
                }
            });
        }
    }

    private void asyncTask(final Runnable task) {
        mExecutor.execute(task);
    }

    private void requestPermission(final Context context, final PermissionUtility.PermissionRequestCallback callback) {
        PermissionUtility.requestPermissions(context, mHandler, PERMISSIONS, callback);
    }

    private void onNegativeButton(final String tag, final View content) {
        // NOP.
    }

    private void updateView(final Context context) {
        if (isInstalledDemoPage(context)) {
            mDeleteButton.setVisibility(View.VISIBLE);
            mDeleteButton.setEnabled(true);

            mInstallButton.setVisibility(View.GONE);
            mInstallButton.setEnabled(false);

            mOpenButton.setVisibility(View.VISIBLE);
            mOpenButton.setEnabled(true);

            if (isCreatedShortcut(context)) {
                mCreateShortcutButton.setVisibility(View.VISIBLE);
                mCreateShortcutButton.setEnabled(false);
            } else {
                mCreateShortcutButton.setVisibility(View.VISIBLE);
                mCreateShortcutButton.setEnabled(true);
            }
        } else {
            mDeleteButton.setVisibility(View.VISIBLE);
            mDeleteButton.setEnabled(false);

            mInstallButton.setVisibility(View.VISIBLE);
            mInstallButton.setEnabled(true);

            mOpenButton.setVisibility(View.GONE);
            mOpenButton.setEnabled(false);

            mCreateShortcutButton.setVisibility(View.GONE);
            mCreateShortcutButton.setEnabled(false);
        }
    }

    private boolean isInstalledDemoPage(final Context context) {
        String version = readInstalledVersion(context);
        if (version == null) {
            return false;
        }
        String currentVersion = getCurrentVersionName(context);
        return version.equals(currentVersion);
    }

    private boolean isCreatedShortcut(final Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ShortcutManager shortcutManager = (ShortcutManager) context.getSystemService(Context.SHORTCUT_SERVICE);
            List<ShortcutInfo> infoList = shortcutManager.getPinnedShortcuts();
            for (ShortcutInfo info : infoList) {
                if (info.getId().equals(CAMERA_DEMO_SHORTCUT_ID)) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }

    private boolean canInstallDemoPage() {
        return true; // TODO 外部ストレージが無い場合は、UIを無効化
    }

    private File getDemoDir() {
        return new File(getDemoRootDir(), "demo/camera");
    }

    private File getDemoRootDir() {
        File documentDir = getDocumentDir();
        return new File(documentDir, PLUGIN_DIR_NAME);
    }

    private File getDocumentDir() {
        File rootDir = Environment.getExternalStorageDirectory();
        if (DEBUG) {
            Log.d(TAG, "Checked External storage path: " + rootDir.getAbsolutePath());
        }
        return new File(rootDir, DOCUMENT_DIR_NAME);
    }

    private void openDemoPage(final Activity activity) {
        Intent intent = createDemoPageIntent();
        activity.startActivity(intent);
    }

    private Intent createDemoPageIntent() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("gotapi://shortcut/" + PLUGIN_DIR_NAME + "/demo/camera/index.html"));
        return intent;
    }

    private void storeInstalledVersion(final Context context, final String versionName) {
        SharedPreferences pref = getPreferences(context);
        pref.edit().putString(KEY_PLUGIN_VERSION_NAME, versionName).apply();
    }

    private String readInstalledVersion(final Context context) {
        SharedPreferences pref = getPreferences(context);
        return pref.getString(KEY_PLUGIN_VERSION_NAME, null);
    }

    private SharedPreferences getPreferences(final Context context) {
        return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    private String getCurrentVersionName(final Context context){
        PackageManager pm = context.getPackageManager();
        try{
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException();
        }
    }

    private void createShortcut(final Context context) {
        Intent shortcut = createDemoPageIntent();

        ShortcutInfoCompat info = new ShortcutInfoCompat.Builder(context, CAMERA_DEMO_SHORTCUT_ID)
                .setIcon(IconCompat.createWithResource(context, R.drawable.dconnect_icon))
                .setShortLabel(context.getString(R.string.demo_page_shortcut_label))
                .setIntent(shortcut).build();
        boolean result = ShortcutManagerCompat.requestPinShortcut(context, info, null);
        if (result) {
            // TODO 成功ダイアログを表示
        } else {
            // TODO 失敗ダイアログを表示
        }
    }

    private void deleteShortcut(final Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // NOTE: API Level 26 以上ではショートカットを削除できない
//            ShortcutManager shortcutManager = (ShortcutManager) context.getSystemService(Context.SHORTCUT_SERVICE);
//            List<String> list = new ArrayList<>();
//            list.add(CAMERA_DEMO_SHORTCUT_ID);
//            shortcutManager.disableShortcuts(list);
        } else {

        }
    }

    private static abstract class FileTask implements Runnable {

        private final Handler mCallbackHandler;

        FileTask(final Handler handler) {
            mCallbackHandler = handler;
        }

        @Override
        public void run() {
            try {
                post(new Runnable() {
                    @Override
                    public void run() {
                        onBeforeTask();
                    }
                });
                execute();
                post(new Runnable() {
                    @Override
                    public void run() {
                        onAfterTask();
                    }
                });
            } catch (final IOException e) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        onFileError(e);
                    }
                });
            } catch (final Throwable e) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        onUnexpectedError(e);
                    }
                });
            }
        }

        private void post(final Runnable r) {
            mCallbackHandler.post(r);
        }

        protected void onBeforeTask() {}

        protected void execute() throws IOException {}

        protected void onAfterTask() {}

        protected void onFileError(final IOException e) {}

        protected void onUnexpectedError(final Throwable e) {}
    }

    private static class InstallTask extends FileTask {

        private final AssetManager mAssetManager;

        private final String mAssetPath;

        private final File mDirectory;

        InstallTask(final AssetManager assetManager,
                    final String assetPath,
                    final File directory,
                    final Handler handler) {
            super(handler);
            mAssetManager = assetManager;
            mAssetPath = assetPath;
            mDirectory = directory;
        }

        @Override
        protected void execute() throws IOException {
            copyAssetFileOrDir(mAssetManager, mAssetPath, mDirectory);
        }

        private void copyAssetFileOrDir(final AssetManager assetManager, final String assetPath, final File dest) throws IOException {
            String[] files = assetManager.list(assetPath);
            if (files.length == 0) {
                copyAssetFile(assetManager.open(assetPath), dest);
            } else {
                if (!dest.exists()) {
                    if (!dest.mkdirs()) {
                        throw new IOException("Failed to create directory: " + dest.getAbsolutePath());
                    }
                }
                for (String file : files) {
                    copyAssetFileOrDir(assetManager, assetPath + "/" + file, new File(dest, file));
                }
            }
        }

        private void copyAssetFile(final InputStream in, final File destFile) throws IOException {
            OutputStream out = null;
            try {
                if (!destFile.exists()) {
                    if (!destFile.createNewFile()) {
                        throw new IOException("Failed to create file: " + destFile.getAbsolutePath());
                    }
                }
                if (DEBUG) {
                    Log.d(TAG, "Created File: " + destFile.getAbsolutePath());
                }
                out = new FileOutputStream(destFile);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            } finally {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            }
        }
    }

    private static class UninstallTask extends FileTask {

        private final File mDirectory;

        UninstallTask(final File directory, final Handler handler) {
            super(handler);
            mDirectory = directory;
        }

        @Override
        protected void execute() throws IOException {
            if (!deleteDir(mDirectory)) {
                throw new IOException("Failed to delete directory: " + mDirectory.getAbsolutePath());
            }
        }

        private boolean deleteDir(final File dir) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDir(file);
                    } else if (file.isFile()) {
                        if (!file.delete()) {
                            return false;
                        }
                    }
                }
            }
            return dir.delete();
        }
    }
}
