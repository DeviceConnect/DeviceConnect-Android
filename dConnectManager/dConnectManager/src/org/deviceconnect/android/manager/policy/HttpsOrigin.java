/*
 HttpOrigins.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.policy;

/**
 * An origin of HTTPS requests.
 * 
 * @author NTT DOCOMO, INC.
 */
class HttpsOrigin extends WebAppOrigin {

    /**
     * Scheme: {@value}.
     */
    protected static final String SCHEME = "https";

    /**
     * Default port number: {@value}.
     */
    private static final int DEFAULT_PORT = 443;

    /**
     * Constructor.
     * 
     * @param host The host name of an origin
     * @param port The port number of an origin
     */
    protected HttpsOrigin(final String host, final int port) {
        super(SCHEME, host, port);
    }

    @Override
    protected int getDefaultPort() {
        return DEFAULT_PORT;
    }

}
