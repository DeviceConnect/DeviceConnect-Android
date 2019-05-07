/*
 FileLoader.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * resourcesのファイルを読み込むクラス。
 */
public final class FileLoader {

    /**
     * 指定されたファイルの文字列を読み込みます.
     *
     * @param file ファイルへのパス
     * @return 文字列
     * @throws RuntimeException ファイルの読み込みに失敗した場合
     */
    public static String readString(String file) {
        byte[] data = readFile(file);
        if (data != null) {
            return new String(data);
        }
        throw new RuntimeException("Not found a " + file);
    }

    /**
     * 指定されたファイルを読み込みます.
     *
     * @param file ファイルへのパス
     * @return ファイルのデータ
     */
    public static byte[] readFile(String file) {
        InputStream input = null;
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            input = FileLoader.class.getClassLoader().getResourceAsStream(file);
            byte[] buffer = new byte[1024];
            int size;
            while ((size = input.read(buffer)) != -1) {
                output.write(buffer, 0, size);
            }

            return output.toByteArray();

        } catch (Exception e) {
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
