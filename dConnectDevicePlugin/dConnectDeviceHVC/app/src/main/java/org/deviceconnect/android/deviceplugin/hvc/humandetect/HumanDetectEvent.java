/*
 HumanDetectEvent.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.humandetect;

import android.content.Intent;


/** 
 * HVC Device Event.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HumanDetectEvent {
    /**
     * detect kind.
     */
    private final HumanDetectKind mDetectKind;
    /**
     * origin.
     */
    private final String mOrigin;
    /**
     * request parameters.
     */
    private final HumanDetectRequestParams mRequestParams;
    
    /**
     * response.
     */
    private final Intent mResponse;
    
    /**
     * Constructor.
     * @param detectKind detect kind
     * @param origin origin
     * @param requestParams request params
     * @param response response
     */
    public HumanDetectEvent(final HumanDetectKind detectKind, final String origin,
            final HumanDetectRequestParams requestParams, final Intent response) {
        mDetectKind = detectKind;
        mOrigin = origin;
        mRequestParams = requestParams;
        mResponse = response;
    }

    /**
     * get kind.
     * @return kind
     */
    public HumanDetectKind getKind() {
        return mDetectKind;
    }
    
    /**
     * get origin.
     * @return origin
     */
    public String getOrigin() {
        return mOrigin;
    }
    
    /**
     * get request parameters.
     * @return request parameters.
     */
    public HumanDetectRequestParams getRequestParams() {
        return mRequestParams;
    }
    
    /**
     * get response.
     * @return response
     */
    public Intent getResponse() {
        return mResponse;
    }
}
