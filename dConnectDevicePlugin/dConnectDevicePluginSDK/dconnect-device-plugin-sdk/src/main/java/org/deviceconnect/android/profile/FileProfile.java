/*
 FileProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.provider.FileManager;
import org.deviceconnect.profile.FileProfileConstants;

import java.io.IOException;
import java.util.List;

/**
 * File プロファイル.
 * 
 * <p>
 * スマートデバイスに対してのファイル操作機能を提供するAPI.<br>
 * スマートデバイスに対してのファイル操作機能を提供するデバイスプラグインは当クラスを継承し、対応APIを実装すること。 <br>
 * </p>
 *
 * @deprecated
 *  swagger定義ファイルで定数を管理することになったので、このクラスは使用しないこととする。
 *  プロファイルを実装する際は本クラスではなく、{@link DConnectProfile} クラスを継承すること。
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class FileProfile extends DConnectProfile implements FileProfileConstants {

    /** ファイル管理クラス. */
    private FileManager mFileMgr;

    /**
     * コンストラクタ.
     * @param fileMgr ファイル管理クラス
     */
    public FileProfile(final FileManager fileMgr) {
        if (fileMgr == null) {
            throw new IllegalArgumentException("fileMgr is null.");
        }
        mFileMgr = fileMgr;
    }

    @Override
    public final String getProfileName() {
        return PROFILE_NAME;
    }

    @Override
    public boolean onRequest(final Intent request, final Intent response) {
        String uri = request.getStringExtra(PARAM_URI);
        if (uri != null) {
            if (uri.startsWith("content://")) {
                byte[] data = getContentData(uri);
                request.putExtra(PARAM_DATA, data);
                request.removeExtra(PARAM_URI);
            }
        }
        return super.onRequest(request, response);
    }

    // ------------------------------------
    // セッターメソッド群
    // ------------------------------------

    /**
     * レスポンスにファイルのURIを設定する.
     * 
     * @param response レスポンスパラメータ
     * @param uri ファイルのURI
     */
    public static void setURI(final Intent response, final String uri) {
        response.putExtra(PARAM_URI, uri);
    }

    /**
     * レスポンスにファイルを設定する.
     * 
     * この中で引数に指定したファイルをFileManagerに保存し、uriを作成して レスポンスにファイルのURIを設定する。
     * 
     * @param response レスポンスパラメータ
     * @param name ファイル名
     * @param data uriパラメータから取得できるファイルのデータ。uriパラメータが省略された場合はnullが渡される。
     * 
     * @throws IOException ファイルの保存に失敗した場合に発生
     */
    public final void setURI(final Intent response, final String name, final byte[] data) throws IOException {
        FileManager fileMgr = getFileManager();
        if (fileMgr == null) {
            throw new IOException("FileManager is not implemented.");
        }

        String uri = fileMgr.saveFile(name, data);
        if (uri == null) {
            throw new IOException("Failed to save a file.");
        }
        setURI(response, uri);
    }

    /**
     * ファイルデータにファイルパスを設定する.
     * 
     * @param file ファイルデータ
     * @param path ファイルパス
     */
    public static void setPath(final Bundle file, final String path) {
        file.putString(PARAM_PATH, path);
    }
    
    /**
     * ファイルデータに更新日を設定する.
     * 
     * @param file ファイルデータ
     * @param updateDate 更新日
     */
    public static void setUpdateDate(final Bundle file, final String updateDate) {
        file.putString(PARAM_UPDATE_DATE, updateDate);
    }

    /**
     * ファイルデータにファイルのMIMEタイプを設定する.
     * 
     * @param file ファイルデータ
     * @param mimeType MIMEタイプ
     */
    public static void setMIMEType(final Bundle file, final String mimeType) {
        file.putString(PARAM_MIME_TYPE, mimeType);
    }
    
    /**
     * メッセージににファイルのMIMEタイプを設定する.
     * 
     * @param response メッセージ
     * @param mimeType MIMEタイプ
     */
    public static void setMIMEType(final Intent response, final String mimeType) {
        response.putExtra(PARAM_MIME_TYPE, mimeType);
    }

    /**
     * レスポンスにファイルデータ一覧を設定する.
     * 
     * @param response レスポンスパラメータ
     * @param files ファイルデータ一覧
     */
    public static void setFiles(final Intent response, final Bundle[] files) {
        response.putExtra(PARAM_FILES, files);
    }
    
    /**
     * レスポンスにファイルデータ一覧を設定する.
     * 
     * @param response レスポンスパラメータ
     * @param files ファイルデータ一覧
     */
    public static void setFiles(final Intent response, final List<Bundle> files) {
        setFiles(response, files.toArray(new Bundle[files.size()]));
    }

    /**
     * ファイルデータにファイル名を設定する.
     * 
     * @param file ファイルデータ
     * @param fileName ファイル名
     */
    public static void setFileName(final Bundle file, final String fileName) {
        file.putString(PARAM_FILE_NAME, fileName);
    }

    /**
     * ファイルデータにファイルサイズを設定する.
     * 
     * @param file ファイルデータ
     * @param fileSize ファイルサイズ
     */
    public static void setFileSize(final Bundle file, final long fileSize) {
        file.putLong(PARAM_FILE_SIZE, fileSize);
    }
    
    /**
     * レスポンスにカウントを設定する.
     * 
     * @param response レスンポンスデータ
     * @param count カウント
     */
    public static void setCount(final Intent response, final int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Count must be larger than 0.");
        }
        response.putExtra(PARAM_COUNT, count);
    }
    
    /**
     * レスポンスにファイルタイプを設定する.
     * 
     * @param response レスポンスデータ
     * @param fileType ファイルタイプ
     */
    public static void setFileType(final Intent response, final FileType fileType) {
        response.putExtra(PARAM_FILE_TYPE, fileType.getValue());
    }

    // ------------------------------------
    // ゲッターメソッド群
    // ------------------------------------

    /**
     * リクエストからファイル名を取得する.
     * 
     * @param request リクエストパラメータ
     * @return ファイル名。無い場合はnullを返す。
     */
    public static String getFileName(final Intent request) {
        return request.getStringExtra(PARAM_FILE_NAME);
    }

    /**
     * リクエストからファイルのURIを取得する.
     * 
     * @param request リクエストパラメータ
     * @return ファイルのURI。無い場合はnullを返す。
     */
    public static String getURI(final Intent request) {
        return request.getStringExtra(PARAM_URI);
    }

    /**
     * リクエストからファイルのMIMEタイプを取得する.
     * 
     * @param request リクエストパラメータ
     * @return ファイルのMIMEタイプ。無い場合はnullを返す。
     */
    public static String getMIMEType(final Intent request) {
        return request.getStringExtra(PARAM_MIME_TYPE);
    }

    /**
     * リクエストからファイルパスを取得する.
     * 
     * @param request リクエストパラメータ
     * @return ファイルパス。無い場合はnullを返す。
     */
    public static String getPath(final Intent request) {
        return request.getStringExtra(PARAM_PATH);
    }

    /**
     * リクエストから画像ファイルのバイナリを取得する.
     *
     * @param request リクエストパラメータ
     * @return 画像ファイルのバイナリ。無い場合はnullを返す。
     */
    public static byte[] getData(final Intent request) {
        return request.getByteArrayExtra(PARAM_DATA);
    }
    
    /**
     * リクエストからオーダーを取得する.
     * 
     * @param request リクエストデータ
     * @return オーダー。無い場合はnullを返す。
     */
    public static final String getOrder(final Intent request) {
        return request.getStringExtra(PARAM_ORDER);
    }
    
    /**
     * リクエストからオフセットを取得する.
     * 
     * @param request リクエストパラメータ
     * @return オフセット。無い場合はnullを返す。
     */
    public static Integer getOffset(final Intent request) {
        return parseInteger(request, PARAM_OFFSET);
    }
    
    /**
     * リクエストからリミットを取得する.
     * 
     * @param request リクエストパラメータ
     * @return リミット。無い場合は0を返す。
     */
    public static Integer getLimit(final Intent request) {
        return parseInteger(request, PARAM_LIMIT);
    }

    /**
     * FileManagerのインスタンスを取得する.
     * 
     * このクラスで実装されたFileManagerを経由してDevice Connect Managerにファイルを送信する。
     * 
     * @return FileManagerのインスタンス
     */
    protected FileManager getFileManager() {
        return mFileMgr;
    }
}
