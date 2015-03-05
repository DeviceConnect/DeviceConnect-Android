/*
 HumanDetectEvent.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.humandetect;


/** 
 * HVC Device Event.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HumanDetectEvent {
    /**
     * detect kind.
     */
    private HumanDetectKind mKind;
    /**
     * session key.
     */
    private String mSessionKey;
    /**
     * request parameters.
     */
    private HumanDetectRequestParams mRequestParams; 
    
    /**
     * Constructor.
     * @param kind kind
     * @param sessionKey session key
     * @param requestParams request params
     */
    public HumanDetectEvent(final HumanDetectKind kind, final String sessionKey,
            final HumanDetectRequestParams requestParams) {
        mKind = kind;
        mSessionKey = sessionKey;
        mRequestParams = requestParams;
    }

    /**
     * get kind.
     * @return kind
     */
    public HumanDetectKind getKind() {
        return mKind;
    }
    
    /**
     * get session key.
     * @return session key
     */
    public String getSessionKey() {
        return mSessionKey;
    }
    
    /**
     * get request parameters.
     * @return request parameters.
     */
    public HumanDetectRequestParams getRequestParams() {
        return mRequestParams;
    }
}
