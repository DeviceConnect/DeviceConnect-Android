/*
 UserDataModel
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvcc2w.manager.data;

/**
 * HVC User Data Model.
 * @author NTT DOCOMO, INC.
 */
public class UserDataModel implements UserDataObject {
    /** Email. */
    private String mEmail;
    /** Password. */
    private String mPassword;
    /** AccessToken. */
    private String mAccessToken;

    /**
     * Constructor.
     * @param email Email
     * @param password Password
     * @param accessToken AccessToken
     */
    public UserDataModel(final String email, final String password, final String accessToken) {
        mEmail = email;
        mPassword = password;
        mAccessToken = accessToken;
    }

    @Override
    public String getEmail() {
        return mEmail;
    }

    @Override
    public void setEmail(String email) {
        mEmail = email;
    }

    @Override
    public String getPassword() {
        return mPassword;
    }

    @Override
    public void setPassword(String password) {
        mPassword = password;
    }

    @Override
    public String getAccessToken() {
        return mAccessToken;
    }

    @Override
    public void setAccessToken(String accessToken) {
        mAccessToken = accessToken;
    }
}
