/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet
 */

package org.deviceconnect.android.localoauth.oauthserver;

/**
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class SampleUser {

    /** LocalOAuth用ログインユーザーID. */
    public static final String LOCALOAUTH_USER = "LocalOAuthUser";

    /** LocalOAuth用ログインパスワード. */
    public static final String LOCALOAUTH_PASS = "LocalOAuthPass";

    /** DBに保存するユーザー名. */
    public static final String USERNAME = "username";
    
    
    /** ID. */
    private final String mId;

    /** パスワード. */
    private char[] mPassword;

    /** ステータス. */
    private String mStatus;

    /** コンストラクタ. 
     * @param id ID
     */
    public SampleUser(final String id) {
        this.mId = id;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.mId != null ? this.mId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SampleUser other = (SampleUser) obj;
        if ((this.mId == null) ? (other.mId != null) : !this.mId.equals(other.mId)) {
            return false;
        }
        return true;
    }

    /**
     * @return the id
     */
    public String getId() {
        return mId;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return mStatus;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(final String status) {
        this.mStatus = status;
    }

    /**
     * @return the password
     */
    public char[] getPassword() {
        return mPassword;
    }

    /**
     * Set Password.
     * @param password
     *            the password to set
     */
    public void setPassword(final char[] password) {
        this.mPassword = password;
    }
}
