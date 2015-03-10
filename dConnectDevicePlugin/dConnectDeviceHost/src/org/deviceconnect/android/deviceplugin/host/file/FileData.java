package org.deviceconnect.android.deviceplugin.host.file;

import java.io.File;

import org.deviceconnect.profile.FileDescriptorProfileConstants.Flag;

/**
 * ファイルデータクラス.
 */
public class FileData {

    /** Fileを保持する変数. */
    private File mFile;
    
    /** File mode. */
    private Flag mFlag = null;
    
    /**
     * ファイルを設定する.
     * @param file ファイル
     */
    public void setFile(final File file) {
        mFile = file;
    }
    
    /**
     * ファイルモードを設定する.
     * @param flag ファイルモード
     */
    public void setFlag(final Flag flag) {
        mFlag = flag;
    }
    
    /**
     * ファイルモードを取得する.
     * @return ファイルモード
     */
    public Flag getFlag() {
        return mFlag;
    }
    
    /**
     * ファイルパスを取得する.
     * @return ファイルパス
     */
    public String getPath() {
        return mFile.getAbsolutePath();
    }
    
    /**
     * ファイル名を取得する.
     * @return ファイル名
     */
    public String getName() {
        return mFile.getName();
    }
}
