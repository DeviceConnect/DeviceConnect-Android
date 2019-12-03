/*
 DemoInstaller.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.demo;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Handler;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DemoInstaller {

    public static abstract class InstallCallback {

        public void onBeforeInstall(final File demoDir) {}

        public void onAfterInstall(final File demoDir) {}

        public void onFileError(final IOException e) {}

        public void onUnexpectedError(final Throwable e) {}
    }

    public static abstract class UpdateCallback {

        public void onBeforeUpdate(final File demoDir) {}

        public void onAfterUpdate(final File demoDir) {}

        public void onFileError(final IOException e) {}

        public void onUnexpectedError(final Throwable e) {}
    }

    public static abstract class UninstallCallback {

        public void onBeforeUninstall(final File demoDir) {}

        public void onAfterUninstall(final File demoDir) {}

        public void onFileError(final IOException e) {}

        public void onUnexpectedError(final Throwable e) {}
    }

    private static final String DOCUMENT_DIR_NAME = "org.deviceconnect.android.manager";

    private static final String PREFERENCE_NAME =  "demo_page_info";

    private static final String KEY_PLUGIN_VERSION_NAME = "plugin_version_name";

    private final Executor mExecutor = Executors.newSingleThreadExecutor();

    private final Context mContext;

    private final String mPluginPackageName;

    private final String mRelativeDirName;

    private final String mDemoZipName;

    public DemoInstaller(final Context context,
                         final String relativeDirName,
                         final String demoZipName) {
        this(context, context.getPackageName(), relativeDirName, demoZipName);
    }

    public DemoInstaller(final Context context,
                         final String packageName,
                         final String relativeDirName,
                         final String demoZipName) {
        mContext = context;
        mPluginPackageName = packageName;
        mRelativeDirName = relativeDirName;
        mDemoZipName = demoZipName;
    }

    Context getContext() {
        return mContext;
    }

    public void install(final InstallCallback callback,
                        final Handler handler) {
        final File demoDir = getDemoDirOnStorage();
        FileTask task = new InstallTask(mContext, mDemoZipName, demoDir, handler) {
            @Override
            protected void onBeforeTask() {
                callback.onBeforeInstall(demoDir);
            }

            @Override
            protected void onAfterTask() {
                callback.onAfterInstall(demoDir);
            }

            @Override
            protected void onFileError(IOException e) {
                callback.onFileError(e);
            }

            @Override
            protected void onUnexpectedError(Throwable e) {
                callback.onUnexpectedError(e);
            }
        };
        queueTask(task);
    }

    public void update(final UpdateCallback callback,
                       final Handler handler) {
        final File demoDir = getDemoDirOnStorage();
        FileTask task = new UpdateTask(mContext, mDemoZipName, demoDir, handler) {
            @Override
            protected void onBeforeTask() {
                callback.onBeforeUpdate(demoDir);
            }

            @Override
            protected void onAfterTask() {
                callback.onAfterUpdate(demoDir);
            }

            @Override
            protected void onFileError(IOException e) {
                callback.onFileError(e);
            }

            @Override
            protected void onUnexpectedError(Throwable e) {
                callback.onUnexpectedError(e);
            }
        };
        queueTask(task);
    }

    public void uninstall(final UninstallCallback callback,
                          final Handler handler) {
        final File demoDir = getDemoDirOnStorage();
        FileTask task = new UninstallTask(mContext, demoDir, handler) {
            @Override
            protected void onBeforeTask() {
                callback.onBeforeUninstall(demoDir);
            }

            @Override
            protected void onAfterTask() {
                callback.onAfterUninstall(demoDir);
            }

            @Override
            protected void onFileError(IOException e) {
                callback.onFileError(e);
            }

            @Override
            protected void onUnexpectedError(Throwable e) {
                callback.onUnexpectedError(e);
            }
        };
        queueTask(task);
    }

    private void queueTask(final FileTask task) {
        mExecutor.execute(task);
    }

    private boolean canInstallDemoPage() {
        return true; // TODO ストレージが無い場合は、UIを無効化
    }

    public String getPluginPackageName() {
        return mPluginPackageName;
    }

    public File getDemoDirOnStorage() {
        return new File(getDemoRootDir(), mRelativeDirName);
    }

    private File getDemoRootDir() {
        File documentDir = getDocumentDir(mContext);
        return new File(documentDir, mPluginPackageName);
    }

    private static File getDocumentDir(final Context context) {
        File rootDir = context.getExternalFilesDir(null);
        return new File(rootDir, DOCUMENT_DIR_NAME);
    }

    public static boolean isUpdateNeeded(final Context context) {
        String version = readInstalledVersion(context);
        if (version == null) {
            return false;
        }
        String currentVersion = getCurrentVersionName(context);
        return !version.equals(currentVersion);
    }

    public boolean isInstalledDemoPage() {
        String version = readInstalledVersion(mContext);
        if (version == null) {
            return false;
        }
        String currentVersion = getCurrentVersionName(mContext);
        return version.equals(currentVersion);
    }

    public boolean existsDemoDir() {
        File demoDir = getDemoDirOnStorage();
        return demoDir.exists();
    }

    static void storeInstalledVersion(final Context context) {
        String versionName = getCurrentVersionName(context);
        storeInstalledVersion(context, versionName);
    }

    static void storeInstalledVersion(final Context context, final String versionName) {
        SharedPreferences pref = getPreferences(context);
        pref.edit().putString(KEY_PLUGIN_VERSION_NAME, versionName).apply();
    }

    private static String readInstalledVersion(final Context context) {
        SharedPreferences pref = getPreferences(context);
        return pref.getString(KEY_PLUGIN_VERSION_NAME, null);
    }

    private static SharedPreferences getPreferences(final Context context) {
        return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    private static String getCurrentVersionName(final Context context){
        PackageManager pm = context.getPackageManager();
        try{
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException();
        }
    }

    public static class Notification {

        public static final String ACTON_UPDATE_DEMO = "org.deviceconnect.android.intent.action.UPDATE_DEMO";

        public static final String ACTON_CONFIRM_NEW_DEMO = "org.deviceconnect.android.intent.action.CONFIRM_NEW_DEMO";

        private final int mNotifyId;

        private final String mPluginName;

        private final int mPluginIcon;

        private final String mChannelId;

        private final String mChannelTitle;

        private final String mChannelDescription;

        public Notification(final int notifyId,
                            final String pluginName,
                            final int pluginIcon,
                            final String channelId,
                            final String channelTitle,
                            final String channelDescription) {
            mNotifyId = notifyId;
            mPluginName = pluginName;
            mPluginIcon = pluginIcon;
            mChannelId = channelId;
            mChannelTitle = channelTitle;
            mChannelDescription = channelDescription;
        }

        public void cancel(final Context context) {
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(mNotifyId);
        }

        public void showUpdateSuccess(final Context context) {
            Intent notifyIntent = new Intent(ACTON_CONFIRM_NEW_DEMO);
            show(context,
                 context.getString(R.string.demo_page_settings_message_update_completed),
                 notifyIntent);
        }

        public void showUpdateError(final Context context) {
            Intent notifyIntent = new Intent(ACTON_UPDATE_DEMO);
            show(context,
                 context.getString(R.string.demo_page_settings_message_update_error),
                 notifyIntent);
        }

        private void show(final Context context,
                          final String body,
                          final Intent notifyIntent) {
            String title = mPluginName;
            int iconType = mPluginIcon;
            int notifyId = mNotifyId;

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    notifyId, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            android.app.Notification notification;
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                NotificationCompat.Builder notificationBuilder =
                        new NotificationCompat.Builder(context)
                                .setSmallIcon(iconType)
                                .setContentTitle(title)
                                .setContentText(body)
                                .setContentIntent(pendingIntent);
                notification = notificationBuilder.build();
            } else {
                android.app.Notification.Builder notificationBuilder =
                        new android.app.Notification.Builder(context)
                                .setSmallIcon(Icon.createWithResource(context, iconType))
                                .setContentTitle(title)
                                .setContentText(body)
                                .setContentIntent(pendingIntent);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel(
                            mChannelId,
                            mChannelTitle,
                            NotificationManager.IMPORTANCE_DEFAULT);
                    channel.setDescription(mChannelDescription);
                    notificationManager.createNotificationChannel(channel);
                    notificationBuilder.setChannelId(mChannelId);
                }
                notification = notificationBuilder.build();
            }
            notificationManager.notify(notifyId, notification);
        }

    }
}
