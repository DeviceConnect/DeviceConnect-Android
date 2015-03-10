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

import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.provider.FileManager;
import org.deviceconnect.profile.FileDescriptorProfileConstants.Flag;

import android.util.Log;

/**
 * ファイル操作を行うクラス.
 */
public class FileDataManager {
    /** バッファーサイズ. */
    private static final int BUFFER_SIZE = 1024;

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
     * @param mgr ファイルマネージャー
     */
    public FileDataManager(final FileManager mgr) {
        mFileManager = mgr;
        startTimer();
    }
    
    /**
     * ファイルを開く.
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
     * @param path パス
     * @return FileDataオブジェクト
     */
    public FileData getFileData(final String path) {
        return mFiles.get(path);
    }
    
    /**
     * ファイルを読み込む.
     * @param file 読み込むファイル
     * @param position 読み込む位置
     * @param length 読み込む長さ
     * @return 読み込んだデータ
     */
    public String readFile(final FileData file, final int position, final int length) {
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
            return new String(baos.toByteArray());
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    // no operation
                    if (BuildConfig.DEBUG) {
                        Log.e("Host", "", e);
                    }
                }
            }
        }
    }

    /**
     * ファイルにデータを書き込む.
     * @param file 書き込み先のファイル
     * @param data 書き込むデータ
     * @param pos 書き込む位置
     * @return 書き込みに成功した場合はtrue、それ以外はfalse
     */
    public boolean writeFile(final FileData file, final byte[] data, final int pos) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file.getPath());
            fos.write(data, pos, data.length - pos);
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            return false;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    // no operation
                    if (BuildConfig.DEBUG) {
                        Log.e("Host", "", e);
                    }
                }
            }
        }
        return true;
    }
    
    /**
     * Readモードでファイルを開く.
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
    private ScheduledExecutorService mExecutor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture mFuture;
    public void startTimer() {
        mFuture = mExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                List<File> files = new ArrayList<File>();
                File mBaseDir = mFileManager.getBasePath();
                test(mBaseDir, files);
                if (files.size() > 0) {
                    if (mModifiedListener != null) {
                        mModifiedListener.onWatchFile(files);
                    }
                }
            }
        }, 5, 3, TimeUnit.SECONDS);
    }
    
    public void stopTimer() {
        if (mFuture != null) {
            mFuture.cancel(true);
            mFuture = null;
        }
    }
    
    private Map<String, Long> mLastModified = new HashMap<String, Long>();
    
    private void test(File file, List<File> modifyFiles) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                test(f, modifyFiles);
            }
        }
        long date = file.lastModified();
        Long last = mLastModified.get(file.getAbsolutePath());
        if (last == null) {
            mLastModified.put(file.getAbsolutePath(), date);
        } else if (last < date) {
            modifyFiles.add(file);
            mLastModified.put(file.getAbsolutePath(), date);
        }
    }
    
    private FileModifiedListener mModifiedListener;
    
    public void setFileModifiedListener(FileModifiedListener listener) {
        mModifiedListener = listener;
    }
    
    public interface FileModifiedListener {
        void onWatchFile(List<File> files);
    }
}
