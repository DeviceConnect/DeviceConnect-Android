/*
 UninstallTask.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.demo;

import android.content.Context;
import android.os.Handler;

import java.io.File;
import java.io.IOException;

class UninstallTask extends FileTask {

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
        DemoInstaller.storeInstalledVersion(getContext(), null);
    }

}
