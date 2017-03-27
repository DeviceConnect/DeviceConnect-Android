package org.deviceconnect.android.deviceplugin.host.file;

import java.io.File;


/**
 * ファイルデータクラス.
 */
public class FileData {
    /**
     * ファイルフラグ.
     */
    public enum Flag {
        /**
         * 未定義値.
         */
        UNKNOWN("Unknown"),
        /**
         * 読み込みのみ.
         */
        R("r"),
        /**
         * 読み込み書き込み.
         */
        RW("rw");

        /**
         * 定義値.
         */
        private String mValue;

        /**
         * 指定された文字列を定義する列挙値を生成する.
         *
         * @param value 定義値
         */
        private Flag(final String value) {
            mValue = value;
        }

        /**
         * 定義値を取得する.
         *
         * @return 定義値
         */
        public String getValue() {
            return mValue;
        }

        /**
         * 定義値から定数を取得する.
         *
         * @param value 定義値
         * @return 定数。無い場合はnullを返す。
         */
        public static Flag getInstance(final String value) {

            for (Flag flag : values()) {
                if (flag.mValue.equals(value)) {
                    return flag;
                }
            }

            return UNKNOWN;
        }
    };

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
