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

package org.restlet.ext.oauth;

/**
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public interface OAuthResourceDefs {

    /*
     * OAuth 2.0 (RFC6749) parameters.
     */
    public static final String CLIENT_ID = "client_id";

    public static final String CLIENT_SECRET = "client_secret";

    public static final String RESPONSE_TYPE = "response_type";

    public static final String SCOPE = "scope";

    public static final String STATE = "state";

    public static final String REDIR_URI = "redirect_uri";

    // XXX [MEMO]追加。
    public static final String APPLICATION_NAME = "application_name";

    public static final String ERROR = "error";

    public static final String ERROR_DESC = "error_description";

    public static final String ERROR_URI = "error_uri";

    public static final String GRANT_TYPE = "grant_type";

    public static final String CODE = "code";

    public static final String ACCESS_TOKEN = "access_token";

    public static final String TOKEN_TYPE = "token_type";

    public static final String EXPIRES_IN = "expires_in";

    public static final String USERNAME = "username";

    public static final String PASSWORD = "password";

    public static final String REFRESH_TOKEN = "refresh_token";

    /*
     * Token Types
     */
    public static final String TOKEN_TYPE_BEARER = "Bearer";

    public static final String TOKEN_TYPE_MAC = "mac";
}
