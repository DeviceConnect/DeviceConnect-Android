/*
 MediaFile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.chromecast.core;


import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

/**
 * Media file.
 * @author NTT DOCOMO, INC.
 */
public interface MediaFile {

    /**
     * Get the name to expose a media file.
     * @return the path to expose a media file
     */
    String getName();

    /**
     * メディアの内容を取得するための入力ストリームを開く.
     *
     * @param context コンテキスト
     * @return 入力ストリーム
     * @throws IOException 入力ストリームの取得に失敗した場合
     */
    InputStream open(Context context) throws IOException;
}
