/*
 FileTask.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.demo;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * ファイル操作タスク.
 */
class FileTask implements Runnable {

    private static final boolean DEBUG = BuildConfig.DEBUG;

    private static final String TAG = "host.dplugin";

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
            post(this::onBeforeTask);
            execute();
            post(this::onAfterTask);
        } catch (final IOException e) {
            post(() -> {
                onFileError(e);
            });
        } catch (final Throwable e) {
            post(() -> {
                onUnexpectedError(e);
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

    static void unzip(final InputStream src, final File destDir) throws IOException {
        try (ZipInputStream in = new ZipInputStream(src)) {
            ZipEntry entry;
            while ((entry = in.getNextEntry()) != null) {
                String name = entry.getName();
                File targetFile = new File(destDir, name).getCanonicalFile();
                if (DEBUG) {
                    Log.d(TAG, "Zip Entry: name=" + name);
                }

                if (entry.isDirectory()) {
                    if (!targetFile.mkdirs()) {
                        throw new IOException("Failed to create new directory: " + targetFile.getAbsolutePath());
                    }
                } else {
                    if (!targetFile.exists()) {
                        if (!targetFile.createNewFile()) {
                            throw new IOException("Failed to create new file: " + targetFile.getAbsolutePath());
                        }
                    }
                    try (FileOutputStream out = new FileOutputStream(targetFile)) {
                        copy(in, out);
                    }
                }
            }
        }
    }

    private static void copy(final InputStream src, final OutputStream dest) throws IOException {
        int len;
        byte[] buf = new byte[1024];
        while ((len = src.read(buf)) > 0) {
            dest.write(buf, 0, len);
        }
        dest.flush();
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
