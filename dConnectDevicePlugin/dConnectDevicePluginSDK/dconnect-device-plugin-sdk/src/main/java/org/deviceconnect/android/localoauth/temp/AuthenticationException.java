/*
 AuthenticationException.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.localoauth.temp;

/**
 * AuthenticationException(Restletの同名のクラスが複雑で切り離しにくいので、簡略化した別クラスを追加して置き換えた).
 * @author NTT DOCOMO, INC.
 */
public class AuthenticationException extends Exception {

    /**
     * Serial Version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * getExplanation.
     * @return Explanation.
     */
    public String getExplanation() {
        return "Explanation - dummy";
    }

}
