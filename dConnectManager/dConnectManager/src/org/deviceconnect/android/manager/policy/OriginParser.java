/*
 OriginParser.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.policy;

/**
 * A parser of a string expression of origins.
 * 
 * @author NTT DOCOMO, INC.
 */
public final class OriginParser {

    /**
     * Private constructor.
     */
    private OriginParser() {
    }

    /**
     * Returns an instance of {@link Origin}.
     * @param originExp a string expression of origins
     * @return an instance of {@link Origin}
     */
    public static Origin parse(final String originExp) {
        Origin origin = WebAppOrigin.parse(originExp);
        if (origin != null) {
            return origin;
        }
        return new LiteralOrigin(originExp);
    }

}
