/*
 UserDataObject
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvcc2w.manager.data;

/**
 * HVC User Data Object.
 * @author NTT DOCOMO, INC.
 */
public interface UserDataObject {

    /**
     * Get User Email.
     */
    String getEmail();
    /**
     * Set User Email.
     * @param email Email Address
     */
    void setEmail(final String email);

    /**
     * Get User Password.
     */
    String getPassword();

    /**
     * Set User Password.
     * @param password password
     */
    void setPassword(final String password);

    /**
     * Get Access Token.
     */
    String getAccessToken();

    /**
     * Set Access Token.
     * @param accessToken Access Token
     */
    void setAccessToken(final String accessToken);
}
