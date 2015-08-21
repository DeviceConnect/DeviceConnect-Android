package org.deviceconnect.android.deviceplugin.host.file;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.deviceconnect.android.activity.PermissionRequestActivity;
import org.deviceconnect.android.provider.FileManager;
import org.deviceconnect.profile.FileDescriptorProfileConstants.Flag;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;

/**
 * ファイル操作を行うクラス.
 */
public class FileDataManager {
    /** バッファーサイズ. */
    private static final int BUFFER_SIZE = 1024;

    /**
     * ファイル更新チェックの間隔を定義する.
     * <p>
     * ここの時間を短くすることで監視時間が早まる。
     */
    private static final int PERIOD = 10;

    /**
     * ファイル更新タイマー.
     */
    private ScheduledExecutorService mExecutor = Executors.newSingleThreadScheduledExecutor();

    /**
     * ファイル更新タイマーキャンセル用Future.
     */
    private ScheduledFuture<?> mFuture;

    /**
     * 前回更新確認した時間.
     */
    private long mLastModifiedDate;

    /**
     * ファイルの更新通知用リスナー.
     */
    private FileModifiedListener mModifiedListener;

    /**
     * ファイルマネージャー.
     */
    private FileManager mFileManager;

    /**
     * 開いたファイルを保持するマップ.
     */
    private Map<String, FileData> mFiles = new HashMap<String, FileData>();

    /**
     * コンストラクタ.
     * 
     * @param mgr ファイルマネージャー
     */
    public FileDataManager(final FileManager mgr) {
        mFileManager = mgr;
    }

    /**
     * パスを変換する.
     * 
     * @param file パス変換するファイル
     * @return ファイル名
     */
    public String getPath(final File file) {
        File mBaseDir = mFileManager.getBasePath();
        String path = file.getAbsolutePath();
        String base = mBaseDir.getAbsolutePath();
        if (path.startsWith(base)) {
            return path.substring(base.length() + 1);
        }
        return null;
    }

    /**
     * ファイルを開く.
     * 
     * @param path 開くファイルのパス
     * @param flag フラグ
     * @return FileDataオブジェクト
     * @throws IOException ファイルのオープンに失敗した場合に発生
     */
    public FileData openFileData(final String path, final Flag flag) throws IOException {
        if (mFiles.containsKey(path)) {
            throw new IllegalStateException("file is already open.");
        }

        FileData file = null;
        switch (flag) {
        case R:
            file = openReadFileData(path, flag);
            break;
        case RW:
            file = openReadWriteFileData(path, flag);
            break;
        default:
            break;
        }
        if (file != null) {
            mFiles.put(path, file);
        }
        return file;
    }

    /**
     * ファイルを閉じる.
     * 
     * @param path 閉じるファイルのパス
     * @return 閉じるのに成功した場合はtrue、それ以外はfalse
     */
    public boolean closeFileData(final String path) {
        FileData file = mFiles.remove(path);
        return file != null;
    }

    /**
     * 指定されたFileデータを取得する.
     * <p>
     * 指定されたパスのファイルが存在しない場合にはnullを返却する.
     * 
     * @param path パス
     * @return FileDataオブジェクト
     */
    public FileData getFileData(final String path) {
        return mFiles.get(path);
    }

    /**
     * ファイルを読み込む.
     * 
     * @param file 読み込むファイル
     * @param position 読み込む位置
     * @param length 読み込む長さ
     * @return 読み込んだデータ
     */
    public void readFile(@NonNull final FileData file, final int position, final int length,
            @NonNull final ReadFileCallback callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Context context = mFileManager.getContext();

            final String[] requiredPermissions = new String[] { Manifest.permission.READ_EXTERNAL_STORAGE };
            final List<String> missingPermissions = new ArrayList<>();
            for (String requiredPermission : requiredPermissions) {
                if (context.checkSelfPermission(requiredPermission) != PackageManager.PERMISSION_GRANTED) {
                    missingPermissions.add(requiredPermission);
                }
            }
            if (missingPermissions.size() != 0) {
                PermissionRequestActivity.requestPermissions(context,
                        missingPermissions.toArray(new String[missingPermissions.size()]),
                        new ResultReceiver(new Handler(Looper.getMainLooper())) {
                            @Override
                            protected void onReceiveResult(final int resultCode, final Bundle resultData) {
                                String[] permissions = resultData
                                        .getStringArray(PermissionRequestActivity.EXTRA_PERMISSIONS);
                                int[] grantResults = resultData
                                        .getIntArray(PermissionRequestActivity.EXTRA_GRANT_RESULTS);

                                if (permissions == null || permissions.length != missingPermissions.size()
                                        || grantResults == null || grantResults.length != missingPermissions.size()) {
                                    callback.onFail();
                                    return;
                                }

                                int count = missingPermissions.size();
                                for (String requiredPermission : missingPermissions) {
                                    for (int i = 0; i < permissions.length; ++i) {
                                        if (permissions[i].equals(requiredPermission)) {
                                            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                                                --count;
                                            } else {
                                                callback.onFail();
                                                return;
                                            }
                                        }
                                    }
                                }

                                if (count == 0) {
                                    readFileInternal(file, position, length, callback);
                                } else {
                                    callback.onFail();
                                    return;
                                }
                            }
                        });
            } else {
                readFileInternal(file, position, length, callback);
            }
        } else {
            readFileInternal(file, position, length, callback);
        }
    }

    /**
     * @see FileDataManager#readFile(FileData, int, int, ReadFileCallback)
     */
    private void readFileInternal(@NonNull FileData file, int position, int length,
            @NonNull ReadFileCallback callback) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FileInputStream fis = null;
            try {
                byte[] buffer = new byte[BUFFER_SIZE];
                int count = 0;
                int len = 0;
                fis = new FileInputStream(file.getPath());
                fis.skip(position);
                while (((len = fis.read(buffer)) != -1)) {
                    if (count + len < length) {
                        baos.write(buffer, 0, len);
                    } else {
                        int l = length - count;
                        baos.write(buffer, 0, l);
                        break;
                    }
                    count += len;
                }
                callback.onSuccess(new String(baos.toByteArray()));
            } catch (FileNotFoundException e) {
                callback.onFail();
                return;
            } catch (IOException e) {
                callback.onFail();
                return;
            } finally {
                if (fis != null) {
                    fis.close();
                }
            }
        } catch (Throwable throwable) {
            callback.onFail();
        }
    }

    /**
     * ファイルにデータを書き込む.
     *
     * @param file 書き込み先のファイル
     * @param data 書き込むデータ
     * @param pos 書き込む位置
     * @param callback
     */
    public void writeFile(@NonNull final FileData file, @NonNull final byte[] data, final int pos,
            @NonNull final WriteFileCallback callback) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Context context = mFileManager.getContext();

            final String[] requiredPermissions = new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE };
            final List<String> missingPermissions = new ArrayList<>();
            for (String requiredPermission : requiredPermissions) {
                if (context.checkSelfPermission(requiredPermission) != PackageManager.PERMISSION_GRANTED) {
                    missingPermissions.add(requiredPermission);
                }
            }
            if (missingPermissions.size() != 0) {
                PermissionRequestActivity.requestPermissions(context,
                        missingPermissions.toArray(new String[missingPermissions.size()]),
                        new ResultReceiver(new Handler(Looper.getMainLooper())) {
                            @Override
                            protected void onReceiveResult(final int resultCode, final Bundle resultData) {
                                String[] permissions = resultData
                                        .getStringArray(PermissionRequestActivity.EXTRA_PERMISSIONS);
                                int[] grantResults = resultData
                                        .getIntArray(PermissionRequestActivity.EXTRA_GRANT_RESULTS);

                                if (permissions == null || permissions.length != missingPermissions.size()
                                        || grantResults == null || grantResults.length != missingPermissions.size()) {
                                    callback.onFail();
                                    return;
                                }

                                int count = missingPermissions.size();
                                for (String requiredPermission : missingPermissions) {
                                    for (int i = 0; i < permissions.length; ++i) {
                                        if (permissions[i].equals(requiredPermission)) {
                                            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                                                --count;
                                            } else {
                                                callback.onFail();
                                                return;
                                            }
                                        }
                                    }
                                }

                                if (count == 0) {
                                    writeFileInternal(file, data, pos, callback);
                                } else {
                                    callback.onFail();
                                    return;
                                }
                            }
                        });
            } else {
                writeFileInternal(file, data, pos, callback);
            }
        } else {
            writeFileInternal(file, data, pos, callback);
        }
    }

    /**
     * ファイルにデータを書き込む.
     *
     * @param file 書き込み先のファイル
     * @param data 書き込むデータ
     * @param pos 書き込む位置
     * @param callback
     * @see FileDataManager#writeFile(FileData, byte[], int, WriteFileCallback)
     */
    private void writeFileInternal(@NonNull FileData file, @NonNull byte[] data, int pos,
            @NonNull WriteFileCallback callback) {
        try {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file.getPath());
                fos.write(data, pos, data.length - pos);
            } catch (FileNotFoundException e) {
                callback.onFail();
                return;
            } catch (IOException e) {
                callback.onFail();
                return;
            } finally {
                if (fos != null) {
                    fos.close();
                }
            }
            callback.onSuccess();
        } catch (Throwable throwable) {
            callback.onFail();
        }
    }

    /**
     * Readモードでファイルを開く.
     * 
     * @param path パス
     * @param flag フラグ
     * @return FileDataオブジェクト
     * @throws FileNotFoundException ファイルが見つからない場合に発生する
     */
    private FileData openReadFileData(final String path, final Flag flag) throws FileNotFoundException {
        File mBaseDir = mFileManager.getBasePath();
        String tmpPath = path;
        if (!tmpPath.startsWith("/")) {
            tmpPath = "/" + path;
        }

        FileData file = new FileData();
        File f = new File(mBaseDir + tmpPath);
        file.setFile(f);
        file.setFlag(flag);
        return file;
    }

    /**
     * Read/Writeモードでファイルを開く.
     * 
     * @param path パス
     * @param flag フラグ
     * @return FileDataオブジェクト
     * @throws FileNotFoundException ファイルが見つからない場合に発生する
     */
    private FileData openReadWriteFileData(final String path, final Flag flag) throws FileNotFoundException {
        File mBaseDir = mFileManager.getBasePath();
        String tmpPath = path;
        if (!tmpPath.startsWith("/")) {
            tmpPath = "/" + path;
        }

        FileData file = new FileData();
        File f = new File(mBaseDir + tmpPath);
        file.setFile(f);
        file.setFlag(flag);
        return file;
    }

    /**
     * ファイル監視タイマーを開始する.
     */
    public void startTimer() {
        if (mFuture != null) {
            return;
        }

        mLastModifiedDate = System.currentTimeMillis();
        mFuture = mExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                List<File> files = checkUpdateFile();
                if (files.size() > 0) {
                    if (mModifiedListener != null) {
                        mModifiedListener.onWatchFile(files);
                    }
                }
            }
        }, PERIOD, PERIOD, TimeUnit.SECONDS);
    }

    /**
     * ファイル監視タイマーを停止する.
     */
    public void stopTimer() {
        if (mFuture != null) {
            mFuture.cancel(true);
            mFuture = null;
        }
    }

    /**
     * ファイルの更新チェックを行う.
     * 
     * @return 更新されたファイル一覧
     */
    public synchronized List<File> checkUpdateFile() {
        List<File> files = new ArrayList<File>();
        File mBaseDir = mFileManager.getBasePath();
        checkUpdateFile(mBaseDir, files);
        mLastModifiedDate = System.currentTimeMillis();
        return files;
    }

    /**
     * ファイルが更新されている場合には、リストに追加する.
     * <p>
     * ファイルがディレクトリの場合には、中のファイルも再起的に行う。
     * 
     * @param file 更新確認を行うファイル
     * @param modifyFiles 更新されたファイルを追加するリスト
     */
    private void checkUpdateFile(final File file, final List<File> modifyFiles) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                checkUpdateFile(f, modifyFiles);
            }
        }

        // ファイルの更新時間が前回チェック時よりも新しい場合
        long date = file.lastModified();
        if (mLastModifiedDate < date) {
            modifyFiles.add(file);
        }
    }

    /**
     * ファイル更新通知リスナーを設定する.
     * 
     * @param listener リスナー
     */
    public void setFileModifiedListener(final FileModifiedListener listener) {
        mModifiedListener = listener;
    }

    /**
     * ファイルの更新通知用リスナー.
     */
    public interface FileModifiedListener {
        /**
         * ファイルの更新が発見された場合に通知される.
         * 
         * @param files 更新されたファイル
         */
        void onWatchFile(List<File> files);
    }

    public interface ReadFileCallback {
        void onSuccess(@NonNull String data);

        void onFail();
    }

    public interface WriteFileCallback {
        void onSuccess();

        void onFail();
    }
}
