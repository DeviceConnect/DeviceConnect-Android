/*
 WebAppOrigin.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.policy;

/**
 * An origin of requests which is sent by web applications.
 * 
 * @author NTT DOCOMO, INC.
 */
abstract class WebAppOrigin implements Origin {

    /**
     * The separator which indicates the beginning of a host name.
     */
    private static final String SEP_HOST = "://";

    /**
     * The separator which indicates the beginning of a port number.
     */
    private static final String SEP_PORT = ":";

    /**
     * Indicates a port number is not specified.
     */
    private static final int NOT_SPECIFIED = -1;


    /**
     * Returns an instance of {@link WebAppOrigin}.
     * 
     * @param originExp the string expression of an origin
     * @return an instance of {@link WebAppOrigin}
     */
    static Origin parse(final String originExp) {
        // Scheme
        int end = originExp.indexOf(SEP_HOST);
        if (end == -1) {
            return null;
        }
        final String scheme = originExp.substring(0, end);
        
        // Host and port
        final String authority = originExp.substring(end + SEP_HOST.length());
        final String host;
        final int port;
        end = authority.indexOf(SEP_PORT);
        try {
            if (end == -1) {
                host = authority;
                port = NOT_SPECIFIED;
            } else {
                host = authority.substring(0, end);
                String portExp = authority.substring(end + SEP_PORT.length());
                port = Integer.parseInt(portExp);
                if (port < 0) {
                    return null;
                }
            }
        } catch (NumberFormatException e) {
            return null;
        }

        if (HttpOrigin.SCHEME.equals(scheme)) {
            return new HttpOrigin(host, port);
        } else if (HttpsOrigin.SCHEME.equals(scheme)) {
            return new HttpsOrigin(host, port);
        } else {
            return null;
        }
    }

    /**
     * Scheme.
     */
    private final String mScheme;

    /**
     * Host name.
     */
    private final String mHost;

    /**
     * Port number.
     */
    private final int mPort;

    /**
     * Constructor.
     * 
     * @param scheme the scheme
     * @param host the host name
     * @param port the port number
     */
    protected WebAppOrigin(final String scheme, final String host, final int port) {
        mScheme = scheme;
        mHost = host;
        mPort = port;
    }

    /**
     * Gets the port number.
     * <p>
     * If the port number was not specified, then the default port number
     * of the scheme will be returned.
     * </p>
     * @return the port number
     * @see {@link #getDefaultPort()}
     */
    private int getPort() {
        if (mPort == NOT_SPECIFIED) {
            return getDefaultPort();
        }
        return mPort;
    }

    /**
     * Gets the default port number for the scheme.
     * @return the default port number for the scheme
     */
    protected abstract int getDefaultPort();

    @Override
    public boolean matches(final Origin origin) {
        if (!(origin instanceof WebAppOrigin)) {
            return false;
        }
        WebAppOrigin other = (WebAppOrigin) origin;
        if (!mScheme.equals(other.mScheme)) {
            return false;
        }
        if (!mHost.equals(other.mHost)) {
            return false;
        }
        if (getPort() != other.getPort()) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(mScheme);
        sb.append("://");
        sb.append(mHost);
        if (mPort != NOT_SPECIFIED) {
            sb.append(":");
            sb.append(String.valueOf(getPort()));
        }
        return sb.toString();
    }

}
