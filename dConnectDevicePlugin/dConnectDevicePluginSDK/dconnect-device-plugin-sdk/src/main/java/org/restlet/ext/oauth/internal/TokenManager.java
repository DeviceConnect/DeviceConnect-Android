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

package org.restlet.ext.oauth.internal;

import org.restlet.ext.oauth.OAuthException;

/**
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public interface TokenManager {

    /**
     * Generate a new token for the client and the username. If the token has
     * already issued for the client and the username, the token will be
     * replaced or updated. If username is null, token will issued for the
     * client itself.
     * 
     * @param client
     * @param username
     * @param scope
     * @param applicationName アプリケーション名(追加)
     * @return
     * @throws OAuthException
     */
    public Token generateToken(Client client, String username, Scope[] scope, String applicationName)
            throws OAuthException;

    /**
     * Call
     * {@link #generateToken(org.restlet.ext.oauth.internal.Client, java.lang.String, java.lang.String[], java.lang.String)}
     * with username=null.
     * 
     * @param client
     * @param scope
     * @return
     * @throws OAuthException
     */
    public Token generateToken(Client client, Scope[] scope, String applicationName)
            throws OAuthException;

    public String storeSession(AuthSession session) throws OAuthException;

    public AuthSession restoreSession(String code) throws OAuthException;

    /**
     * Find a token issued for the client and the username. For those tokens
     * issued for 'client_credentials' grant type, username must be null.
     * 
     * @param client
     *            the client that bound to token.
     * @param username
     *            the username that bound to token. null if the token was issued
     *            for the client itself.
     * @return null if not found.
     */
    public Token findToken(Client client, String username);

    /**
     * Call
     * {@link #findToken(org.restlet.ext.oauth.internal.Client, java.lang.String)}
     * with username=null.
     * 
     * @param client
     *            the client that bound to token.
     * @return null if not found.
     */
    public Token findToken(Client client);
    
	/**
     * Find all tokens bound to the username.
     * 
     * @param username
     *            the username that bound to tokens.
     * @return 0 length if not found.
     */
    public Token[] findTokens(String username);

    /**
     * Find all tokens bound to the client.
     * 
     * @param client
     *            the client that bound to tokens.
     * @return 0 length if not found.
     */
    public Token[] findTokens(Client client);

    /**
     * Revoke a token issued for the client and the username. For those tokens
     * issued for 'client_credentials' grant type, username must be null.
     * 
     * @param client
     *            the client that bound to token.
     * @param username
     *            the username that bound to token. null if the token was issued
     *            for the client itself.
     */
    public void revokeToken(Client client, String username);
	
    /* MEMO: LocalOAuthで追加 */
    /**
     * アクセストークンが一致するトークンを探す.
     * @param accessToken	アクセストークン
     * @return	not null: アクセストークンが一致するトークンデータ / null: アクセストークンが一致するトークンデータが見つからない 
     */
    public Token findTokenByAccessToken(final String accessToken);

    /**
     * Call
     * {@link #revokeToken(org.restlet.ext.oauth.internal.Client, java.lang.String)}
     * with username=null.
     * 
     * @param client
     *            the client that bound to token.
     */
    public void revokeToken(Client client);

    /**
     * Revoke all tokens bound to the username.
     * 
     * @param username
     *            the username that bound to tokens.
     * @return 0 length if not found.
     */
    public void revokeAllTokens(String username);

    /**
     * Revoke all tokens bound to the client.
     * 
     * @param client
     *            the client that bound to tokens.
     * @return 0 length if not found.
     */
    public void revokeAllTokens(Client client);

}
