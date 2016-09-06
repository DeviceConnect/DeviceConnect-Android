package org.deviceconnect.android.deviceplugin.awsiot;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class AWSIotUtil {

    private AWSIotUtil() {

    }

    public static String hexToString(final byte[] buf) {
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < buf.length; i++) {
            hexString.append(Integer.toHexString(0xff & buf[i]));
        }
        return hexString.toString();
    }

    public static String md5(final String s) {
        try {
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes("ASCII"));
            return hexToString(digest.digest());
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            Log.e("", "", e);
        }
        return null;
    }
}
