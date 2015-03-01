/*
 HumanDetectEvent.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.humandetect;

import java.util.List;

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
     * options.
     */
    private List<String> mOptions;
    
    /**
     * Constructor.
     * @param kind kind
     * @param sessionKey session key
     * @param options options
     */
    public HumanDetectEvent(final HumanDetectKind kind, final String sessionKey, final List<String> options) {
        mKind = kind;
        mSessionKey = sessionKey;
        mOptions = options;
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
     * get options.
     * @return options
     */
    public List<String> getOptions() {
        return mOptions;
    }
}
