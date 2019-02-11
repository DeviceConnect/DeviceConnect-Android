/*
 FileTask.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.demo;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Handler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * ファイル操作タスク.
 */
class FileTask implements Runnable {

    private final Context mContext;

    private final Handler mCallbackHandler;

    FileTask(final Context context, final Handler handler) {
        mContext = context;
        mCallbackHandler = handler;
    }

    Context getContext() {
        return mContext;
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

    static void copyAssetFileOrDir(final AssetManager assetManager,
                                           final String assetPath,
                                           final File dest) throws IOException {
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

    private static void copyAssetFile(final InputStream in, final File destFile) throws IOException {
        OutputStream out = null;
        try {
            if (!destFile.exists()) {
                if (!destFile.createNewFile()) {
                    throw new IOException("Failed to create file: " + destFile.getAbsolutePath());
                }
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

    static boolean deleteDir(final File dir) {
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
