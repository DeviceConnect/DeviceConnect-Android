package org.deviceconnect.android.deviceplugin.host.demo;

import android.content.Context;
import android.os.Handler;


import java.io.File;
import java.io.IOException;

public class UninstallTask extends FileTask {

    private final File mDirectory;

    UninstallTask(final Context context,
                         final File directory,
                         final Handler handler) {
        super(context, handler);
        mDirectory = directory;
    }

    @Override
    protected void execute() throws IOException {
        // デモページを指定されたディクレトリから削除.
        if (!deleteDir(mDirectory)) {
            throw new IOException("Failed to delete directory: " + mDirectory.getAbsolutePath());
        }

        // プラグインのバージョンを削除.
        DemoPageInstaller.storeInstalledVersion(getContext(), null);
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
