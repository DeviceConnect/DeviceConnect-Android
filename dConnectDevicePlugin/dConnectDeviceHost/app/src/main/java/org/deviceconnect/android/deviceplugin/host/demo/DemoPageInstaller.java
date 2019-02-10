package org.deviceconnect.android.deviceplugin.host.demo;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DemoPageInstaller {

    public static abstract class InstallCallback {

        public void onBeforeInstall(final File demoDir) {}

        public void onAfterInstall(final File demoDir) {}

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

    private static final String PLUGIN_DIR_NAME = "org.deviceconnect.android.deviceplugin.host";

    private static final String PREFERENCE_NAME =  "demo_page_info";

    private static final String KEY_PLUGIN_VERSION_NAME = "plugin_version_name";

    private final Executor mExecutor = Executors.newSingleThreadExecutor();

    private final String mRelativeDirName;

    public DemoPageInstaller(final String relativeDirName) {
        mRelativeDirName = relativeDirName;
    }

    public void install(final Context context,
                        final InstallCallback callback,
                        final Handler handler) {
        final File demoDir = getDemoDirOnExternalStorage();
        FileTask task = new InstallTask(context, mRelativeDirName, demoDir, handler) {
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

    public void uninstall(final Context context,
                          final UninstallCallback callback,
                          final Handler handler) {
        final File demoDir = getDemoDirOnExternalStorage();
        FileTask task = new UninstallTask(context, demoDir, handler) {
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
        return true; // TODO 外部ストレージが無い場合は、UIを無効化
    }

    private File getDemoDirOnExternalStorage() {
        return new File(getDemoRootDir(), mRelativeDirName /*"demo/camera"*/);
    }

    private static File getDemoRootDir() {
        File documentDir = getDocumentDir();
        return new File(documentDir, PLUGIN_DIR_NAME);
    }

    private static File getDocumentDir() {
        File rootDir = Environment.getExternalStorageDirectory();
        return new File(rootDir, DOCUMENT_DIR_NAME);
    }

    public static boolean isInstalledDemoPage(final Context context) {
        String version = readInstalledVersion(context);
        if (version == null) {
            return false;
        }
        String currentVersion = getCurrentVersionName(context);
        return version.equals(currentVersion);
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
}
