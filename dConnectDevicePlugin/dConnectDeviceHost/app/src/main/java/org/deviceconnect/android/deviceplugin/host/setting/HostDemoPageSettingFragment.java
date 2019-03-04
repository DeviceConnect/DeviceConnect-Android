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
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.pm.ShortcutInfoCompat;
import android.support.v4.content.pm.ShortcutManagerCompat;
import android.support.v4.graphics.drawable.IconCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import org.deviceconnect.android.activity.PermissionUtility;
import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.R;
import org.deviceconnect.android.deviceplugin.host.demo.DemoPageInstaller;

import java.io.File;
import java.io.IOException;
import java.util.List;

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

    private static final String TAG_INSTALL_PROMPT = "install";

    private static final String TAG_DELETION_PROMPT = "deletion";

    private static final String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    private Button mDeleteButton;

    private Button mInstallButton;

    private Button mOpenButton;

    private Button mCreateShortcutButton;

    private Handler mHandler;

    private final DemoPageInstaller mDemoInstaller = new DemoPageInstaller("demo");

    @Override
    protected String getPageTitle() {
        return getString(R.string.demo_page_settings_title);
    }

    @Override
    protected String getPageTag() {
        return "demo";
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
            showDeletionPromptDialog();
        } else if (v == mInstallButton) {
            showInstallPromptDialog();
        } else if (v == mOpenButton) {
            openDemoPage(activity);
        } else if (v == mCreateShortcutButton) {
            createShortcut(activity);
        }
        updateView(activity);
    }

    private void showMessageDialog(final int titleId,
                                   final int messageId) {
        MessageDialogFragment.Builder b = new MessageDialogFragment.Builder();
        b.title(getString(titleId));
        b.message(getString(messageId));
        b.positive(getString(R.string.host_ok));
        b.build().show(getFragmentManager());
    }

    private void showErrorDialog(final int titleId,
                                 final int summaryId,
                                 final String detail) {
        ErrorDialogFragment.Builder b = new ErrorDialogFragment.Builder();
        b.title(getString(titleId));
        b.summary(getString(summaryId));
        b.detail(detail);
        b.positive(getString(R.string.host_confirm));
        b.build().show(getFragmentManager());
    }

    private void showInstallPromptDialog() {
        InstallDialogFragment.Builder b = new InstallDialogFragment.Builder();
        b.tag(TAG_INSTALL_PROMPT);
        b.title(getString(R.string.demo_page_settings_title_install));
        b.positive(getString(R.string.demo_page_settings_button_install));
        b.negative(getString(R.string.demo_page_settings_button_cancel));
        b.build().show(getFragmentManager());
    }

    private void showInstallSuccessDialog() {
        showMessageDialog(
                R.string.demo_page_settings_title_install,
                R.string.demo_page_settings_message_install_completed);
    }

    private void showInstallErrorDialog(final String detail) {
        showErrorDialog(R.string.demo_page_settings_title_error,
                R.string.demo_page_settings_message_install_error, detail);
    }

    private void showDeletionPromptDialog() {
        DeletionDialogFragment.Builder b = new DeletionDialogFragment.Builder();
        b.tag(TAG_DELETION_PROMPT);
        b.title(getString(R.string.demo_page_settings_title_delete));
        b.positive(getString(R.string.demo_page_settings_button_delete));
        b.negative(getString(R.string.demo_page_settings_button_cancel));
        b.build().show(getFragmentManager());
    }

    private void showDeletionSuccessDialog() {
        showMessageDialog(
                R.string.demo_page_settings_title_delete,
                R.string.demo_page_settings_message_delete_completed);
    }

    private void showDeletionErrorDialog(final String detail) {
        showErrorDialog(R.string.demo_page_settings_title_error,
                R.string.demo_page_settings_message_delete_error, detail);
    }

    public void onPositiveButton(final String tag, final MessageDialogFragment dialogFragment) {
        final Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        if (TAG_INSTALL_PROMPT.equals(tag)) {
            requestPermission(activity, new PermissionUtility.PermissionRequestCallback() {
                @Override
                public void onSuccess() {
                    // デモページをインストール
                    mDemoInstaller.install(activity.getApplicationContext(), new DemoPageInstaller.InstallCallback() {
                        @Override
                        public void onBeforeInstall(final File demoDir) {
                            if (DEBUG) {
                                Log.d(TAG, "Start to install demo: path=" + demoDir.getAbsolutePath());
                            }
                        }

                        @Override
                        public void onAfterInstall(final File demoDir) {
                            if (DEBUG) {
                                Log.d(TAG, "Installed demo: path=" + demoDir.getAbsolutePath());
                            }

                            updateView(activity);
                            showInstallSuccessDialog();

                            if (((InstallDialogFragment) dialogFragment).isChecked()) {
                                createShortcut(activity);
                            }
                        }

                        @Override
                        public void onFileError(final IOException e) {
                            if (DEBUG) {
                                Log.e(TAG, "Failed to install demo on external storage.", e);
                            }
                            showInstallErrorDialog(e.getMessage());
                        }

                        @Override
                        public void onUnexpectedError(final Throwable e) {
                            if (DEBUG) {
                                Log.e(TAG, "Failed to install demo on external storage.", e);
                            }
                            showInstallErrorDialog(e.getMessage());
                        }
                    }, mHandler);
                }

                @Override
                public void onFail(final @NonNull String deniedPermission) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showInstallErrorDialog("Denied permission: " + deniedPermission);
                        }
                    });
                }
            });
        } else if (TAG_DELETION_PROMPT.equals(tag)) {
            requestPermission(activity, new PermissionUtility.PermissionRequestCallback() {
                @Override
                public void onSuccess() {
                    // デモページをアンインストール
                    mDemoInstaller.uninstall(activity.getApplicationContext(), new DemoPageInstaller.UninstallCallback() {
                        @Override
                        public void onBeforeUninstall(final File demoDir) {
                            if (DEBUG) {
                                Log.d(TAG, "Start to uninstall demo: path=" + demoDir.getAbsolutePath());
                            }
                        }

                        @Override
                        public void onAfterUninstall(final File demoDir) {
                            if (DEBUG) {
                                Log.d(TAG, "Uninstalled demo: path=" + demoDir.getAbsolutePath());
                            }

                            deleteShortcut(activity);

                            updateView(activity);
                            showDeletionSuccessDialog();
                        }

                        @Override
                        public void onFileError(final IOException e) {
                            if (DEBUG) {
                                Log.e(TAG, "Failed to install demo on external storage.", e);
                            }
                            showDeletionErrorDialog(e.getMessage());
                        }

                        @Override
                        public void onUnexpectedError(final Throwable e) {
                            if (DEBUG) {
                                Log.e(TAG, "Failed to delete demo from external storage.");
                            }
                            showDeletionErrorDialog(e.getMessage());
                        }
                    }, mHandler);
                }

                @Override
                public void onFail(final @NonNull String deniedPermission) {
                    showDeletionErrorDialog("Denied permission: " + deniedPermission);
                }
            });
        }
    }

    private void requestPermission(final Context context, final PermissionUtility.PermissionRequestCallback callback) {
        PermissionUtility.requestPermissions(context, mHandler, PERMISSIONS, callback);
    }

    public void onNegativeButton(final String tag, final MessageDialogFragment dialogFragment) {
        // NOP.
    }

    private void updateView(final Context context) {
        if (DemoPageInstaller.isInstalledDemoPage(context)) {
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

    private void openDemoPage(final Activity activity) {
        Intent intent = createDemoPageIntent();
        activity.startActivity(intent);
    }

    private Intent createDemoPageIntent() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("gotapi://shortcut/" + PLUGIN_DIR_NAME + "/demo/camera/index.html"));
        return intent;
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

    public static class InstallDialogFragment extends MessageDialogFragment {

        private View mView;

        @Override
        protected void onExtendDialog(final @NonNull AlertDialog.Builder builder,
                                      final @NonNull LayoutInflater layoutInflater,
                                      final @NonNull Bundle arguments) {
            mView = layoutInflater.inflate(R.layout.dialog_host_demo_page_install, null);
            builder.setView(mView);
        }

        static class Builder extends MessageDialogFragment.Builder {
            InstallDialogFragment build() {
                InstallDialogFragment f = new InstallDialogFragment();
                f.setArguments(mArguments);
                return f;
            }
        }

        boolean isChecked() {
            CheckBox checkBox = mView.findViewById(R.id.checkbox_create_shortcut);
            return checkBox.isChecked();
        }
    }

    public static class DeletionDialogFragment extends MessageDialogFragment {

        @Override
        protected void onExtendDialog(final @NonNull AlertDialog.Builder builder,
                                      final @NonNull LayoutInflater layoutInflater,
                                      final @NonNull Bundle arguments) {
            View view = layoutInflater.inflate(R.layout.dialog_host_demo_page_delete, null);
            builder.setView(view);
        }

        static class Builder extends MessageDialogFragment.Builder {
            DeletionDialogFragment build() {
                DeletionDialogFragment f = new DeletionDialogFragment();
                f.setArguments(mArguments);
                return f;
            }
        }
    }

    public static class ErrorDialogFragment extends MessageDialogFragment {

        static final String KEY_ERROR_SUMMARY = "error_summary";

        static final String KEY_ERROR_DETAIL = "error_detail";

        @Override
        protected void onExtendDialog(final @NonNull AlertDialog.Builder builder,
                                      final @NonNull LayoutInflater layoutInflater,
                                      final @NonNull Bundle arguments) {

            View view = layoutInflater.inflate(R.layout.dialog_host_demo_page_error, null);
            TextView summaryView = view.findViewById(R.id.error_summary);
            summaryView.setText(arguments.getString(KEY_ERROR_SUMMARY));
            TextView detailView  = view.findViewById(R.id.error_detail);
            detailView.setText(arguments.getString(KEY_ERROR_DETAIL));

            builder.setView(view);
        }

        static class Builder extends MessageDialogFragment.Builder {

            Builder summary(final String summary) {
                mArguments.putString(KEY_ERROR_SUMMARY, summary);
                return this;
            }
            Builder detail(final String detail) {
                mArguments.putString(KEY_ERROR_DETAIL, detail);
                return this;
            }
            ErrorDialogFragment build() {
                ErrorDialogFragment f = new ErrorDialogFragment();
                f.setArguments(mArguments);
                return f;
            }
        }
    }

    public static class MessageDialogFragment extends DialogFragment {

        static final String KEY_TAG = "tag";

        static final String KEY_TITLE = "title";

        static final String KEY_MESSAGE = "message";

        static final String KEY_POSITIVE = "positive";

        static final String KEY_NEGATIVE = "negative";

        protected void onExtendDialog(final AlertDialog.Builder builder,
                                      final LayoutInflater layoutInflater,
                                      final Bundle arguments) {}

        @NonNull
        @Override
        public Dialog onCreateDialog(final @Nullable Bundle savedInstanceState) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final Activity activity = getActivity();
            final Bundle args = getArguments();
            if (activity != null && args != null) {
                builder.setTitle(args.getString(KEY_TITLE));
                String message = args.getString(KEY_MESSAGE);
                if (message != null) {
                    builder.setMessage(message);
                }
                String positive = args.getString(KEY_POSITIVE);
                if (positive != null) {
                    builder.setPositiveButton(positive,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, final int which) {
                                    HostDemoPageSettingFragment l = (HostDemoPageSettingFragment) getFragmentManager().findFragmentByTag("demo");
                                    l.onPositiveButton(args.getString(KEY_TAG), MessageDialogFragment.this);
                                }
                            });
                }
                String negative = args.getString(KEY_NEGATIVE);
                if (negative != null) {
                    builder.setNegativeButton(negative,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, final int which) {
                                    HostDemoPageSettingFragment l = (HostDemoPageSettingFragment) getFragmentManager().findFragmentByTag("demo");
                                    l.onNegativeButton(args.getString(KEY_TAG), MessageDialogFragment.this);
                                }
                            });
                }
                onExtendDialog(builder, activity.getLayoutInflater(), args);
            }
            return builder.create();
        }

        void show(final FragmentManager fragmentManager) {
            FragmentTransaction t = fragmentManager.beginTransaction();
            t.add(this, null);
            t.addToBackStack(null);
            t.commitAllowingStateLoss();
        }

        static class Builder {

            final Bundle mArguments = new Bundle();

            Builder tag(final String tag) {
                mArguments.putString(KEY_TAG, tag);
                return this;
            }
            Builder title(final String title) {
                mArguments.putString(KEY_TITLE, title);
                return this;
            }
            Builder message(final String message) {
                mArguments.putString(KEY_MESSAGE, message);
                return this;
            }
            Builder positive(final String positive) {
                mArguments.putString(KEY_POSITIVE, positive);
                return this;
            }
            Builder negative(final String positive) {
                mArguments.putString(KEY_NEGATIVE, positive);
                return this;
            }
            MessageDialogFragment build() {
                MessageDialogFragment f = new MessageDialogFragment();
                f.setArguments(mArguments);
                return f;
            }
        }
    }
}