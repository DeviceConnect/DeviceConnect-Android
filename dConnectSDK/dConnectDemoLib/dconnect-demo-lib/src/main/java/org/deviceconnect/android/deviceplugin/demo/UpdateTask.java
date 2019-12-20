/*
 UpdateTask.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.demo;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Handler;

import java.io.File;
import java.io.IOException;

class UpdateTask extends FileTask {

    private final AssetManager mAssetManager;

    private final String mAssetPath;

    private final File mDirectory;

    UpdateTask(final Context context,
               final String assetPath,
               final File directory,
               final Handler handler) {
        super(context, handler);
        mAssetManager = context.getAssets();
        mAssetPath = assetPath;
        mDirectory = directory;
    }

    @Override
    protected void execute() throws IOException {
        if (mDirectory.isDirectory()) {
            if (!deleteDir(mDirectory)) {
                throw new IOException("Failed to delete directory: path = " + mDirectory.getAbsolutePath());
            }
        } else if (mDirectory.isFile()) {
            File file = mDirectory;
            if (file.delete()) {
                throw new IOException("Failed to delete same-named file: path = " + file.getAbsolutePath());
            }
        }

        // デモページを指定されたディレクトリにインストール.
        unzip(mAssetManager.open(mAssetPath), mDirectory);

        // プラグインのバージョンを保存.
        DemoInstaller.storeInstalledVersion(getContext());
    }
}
