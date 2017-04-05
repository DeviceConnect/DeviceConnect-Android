/*
 FileDescriptorProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.profile.FileDescriptorProfileConstants;

/**
 * File Descriptor プロファイル.
 * 
 * <p>
 * ファイルディスクリプタ操作機能を提供するAPI.<br>
 * ファイルディスクリプタ操作機能を提供するデバイスプラグインは当クラスを継承し、対応APIを実装すること。 <br>
 * </p>
 *
 * @deprecated
 *  swagger定義ファイルで定数を管理することになったので、このクラスは使用しないこととする。
 *  プロファイルを実装する際は本クラスではなく、{@link DConnectProfile} クラスを継承すること。
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class FileDescriptorProfile extends DConnectProfile implements FileDescriptorProfileConstants {

    @Override
    public final String getProfileName() {
        return PROFILE_NAME;
    }

    // ------------------------------------
    // セッターメソッド群
    // ------------------------------------

    /**
     * ファイルデータに現在更新時間を設定する.
     * 
     * @param file ファイルデータ
     * @param curr 現在の更新時間
     */
    public static void setCurr(final Bundle file, final String curr) {
        file.putString(PARAM_CURR, curr);
    }

    /**
     * ファイルデータに以前の更新時間を設定する.
     * 
     * @param file ファイルデータ
     * @param prev 以前の更新時間
     */
    public static void setPrev(final Bundle file, final String prev) {
        file.putString(PARAM_PREV, prev);
    }

    /**
     * レスポンスに読み込んだファイルのサイズを設定する.
     * 
     * @param response レスポンスパラメータ
     * @param size 読み込んだファイルのサイズ
     */
    public static void setSize(final Intent response, final int size) {
        response.putExtra(PARAM_SIZE, size);
    }

    /**
     * レスポンスにファイルのパスを設定する.
     * 
     * @param file ファイルデータ
     * @param path ファイルのパス
     */
    public static void setPath(final Bundle file, final String path) {
        file.putString(PARAM_PATH, path);
    }
    
    /**
     * レスポンスにファイルデータを設定する.
     * 
     * @param response レスポンスパラメータ
     * @param fileData ファイルデータ
     */
    public static void setFileData(final Intent response, final String fileData) {
        response.putExtra(PARAM_FILE_DATA, fileData);
    }
    
    /**
     * メッセージにファイル情報を設定する.
     * 
     * @param message メッセージパラメータ
     * @param file ファイル情報
     */
    public static void setFile(final Intent message, final Bundle file) {
        message.putExtra(PARAM_FILE, file);
    }

    // ------------------------------------
    // リクエストゲッターメソッド群
    // ------------------------------------

    /**
     * リクエストからPATHを取得する.
     * 
     * @param request リクエストパラメータ
     * @return PATH文字列。無い場合はnullを返す。
     */
    public static String getPath(final Intent request) {
        String path = request.getStringExtra(PARAM_PATH);
        return path;
    }

    /**
     * リクエストからフラグを取得する.
     * 
     * @param request リクエストパラメータ
     * @return フラグ文字列。無い場合はnullを返す。
     */
    public static Flag getFlag(final Intent request) {
        String value = request.getStringExtra(PARAM_FLAG);
        return Flag.getInstance(value);
    }

    /**
     * リクエストから読み込みサイズを取得する.
     * 
     * @param request リクエストパラメータ
     * @return ファイルサイズ。無い場合はnullを返す。
     */
    public static Long getLength(final Intent request) {
        return parseLong(request, PARAM_LENGTH);
    }

    /**
     * リクエストからファイルの読み込み開始位置を取得する.
     * 
     * @param request リクエストパラメータ
     * @return ファイル読み込み開始位置。無い場合は-1を返す。
     */
    public static Long getPosition(final Intent request) {
        return parseLong(request, PARAM_POSITION);
    }

    /**
     * リクエストからURIを取得する.
     * 
     * @param request リクエストパラメータ
     * @return ファイルのURI
     */
    public static String getUri(final Intent request) {
        return request.getStringExtra(PARAM_URI);
    }
}
