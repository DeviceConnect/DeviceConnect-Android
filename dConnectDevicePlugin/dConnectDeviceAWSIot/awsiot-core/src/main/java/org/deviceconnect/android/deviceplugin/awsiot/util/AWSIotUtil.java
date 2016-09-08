package org.deviceconnect.android.deviceplugin.awsiot.util;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class AWSIotUtil {

    private AWSIotUtil() {
    }

    public static String hexToString(final byte[] buf) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : buf) {
            hexString.append(Integer.toHexString(b & 0xfF));
        }
        return hexString.toString();
    }

    public static String md5(final String s) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes("ASCII"));
            return hexToString(digest.digest());
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            Log.e("AWS", "AWSIotUtil#md5", e);
        }
        return null;
    }
}
