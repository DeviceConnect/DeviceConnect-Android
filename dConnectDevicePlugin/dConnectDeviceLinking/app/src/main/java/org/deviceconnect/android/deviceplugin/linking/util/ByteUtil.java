/*
 ByteUtil.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.util;

import java.io.UnsupportedEncodingException;

public class ByteUtil {
    public static String binaryToString(byte[] binary) {
        try {
            return new String(binary, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    public static String binaryToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    public static String byteToHex(byte b) {
        return String.format("%02x", b & 0xff);
    }
}
