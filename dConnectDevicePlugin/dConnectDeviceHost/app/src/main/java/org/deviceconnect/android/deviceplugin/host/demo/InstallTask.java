package org.deviceconnect.android.deviceplugin.host.demo;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Handler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class InstallTask extends FileTask {

    private final AssetManager mAssetManager;

    private final String mAssetPath;

    private final File mDirectory;

    InstallTask(final Context context,
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
        // デモページを指定されたディレクトリにインストール.
        copyAssetFileOrDir(mAssetManager, mAssetPath, mDirectory);

        // プラグインのバージョンを保存.
        DemoPageInstaller.storeInstalledVersion(getContext());
    }

    private static void copyAssetFileOrDir(final AssetManager assetManager,
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

}
