/*
 HmacUtils.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.utils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * HMACを生成するたのユーティリティクラス.
 *
 * @author NTT DOCOMO, INC.
 */
public final class HmacUtils {

    /**
     * The hash algorithm.
     */
    private static final String HASH_ALGORITHM = "HmacSHA256";

    private HmacUtils() {
    }

    /**
     * HMACを生成する.
     * @param origin オリジン
     * @param nonce nonce
     * @param key HMACを生成するためのキー
     * @return HMAC
     * @throws RuntimeException 生成するためのアルゴリズムが無い、もしくは、キーの値が不正の場合に発生
     */
    public static String generateHmac(final String origin, final String nonce, final String key) {
        if (origin == null) {
            throw new IllegalArgumentException("origin is null.");
        }
        if (nonce == null) {
            throw new IllegalArgumentException("nonce is null.");
        }
        try {
            Mac mac = Mac.getInstance(HASH_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(toByteArray(key), HASH_ALGORITHM);
            mac.init(keySpec);
            return toHexString(mac.doFinal(toByteArray(nonce)));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(HASH_ALGORITHM + " is not supported.");
        } catch (InvalidKeyException e) {
            throw new RuntimeException("keySpec is null.");
        }
    }

    /**
     * Parse a hex string expression of a byte array to raw.
     * @param b a hex string expression of a byte array
     * @return A raw byte array
     */
    private static byte[] toByteArray(final String b) {
        String c = b;
        if (c.length() % 2 != 0) {
            c = "0" + c;
        }
        byte[] array = new byte[b.length() / 2];
        for (int i = 0; i < b.length() / 2; i++) {
            String hex = b.substring(2 * i, 2 * i + 2);
            array[i] = (byte) Integer.parseInt(hex, 16);
        }
        return array;
    }

    /**
     * Returns a hex string expression of a byte array.
     *
     * @param b A byte array
     * @return A string expression of a byte array
     */
    private static String toHexString(final byte[] b) {
        if (b == null) {
            throw new IllegalArgumentException("b is null.");
        }
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            String substr = Integer.toHexString(b[i] & 0xff);
            if (substr.length() < 2) {
                str.append("0");
            }
            str.append(substr);
        }
        return str.toString();
    }
}
