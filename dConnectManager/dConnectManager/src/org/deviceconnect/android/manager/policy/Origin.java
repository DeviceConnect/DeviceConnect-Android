/*
 Origin.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.policy;

/**
 * An origin of requests which is sent by applications.
 * 
 * @author NTT DOCOMO, INC.
 */
public interface Origin {

    /**
     * Returns whether the specified origin is same or not.
     * @param origin another origin
     * @return <code>true</code> if the specified origin is same, otherwise <code>false</code>.
     */
    boolean matches(Origin origin);

}
