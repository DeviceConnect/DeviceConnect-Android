/*
 HumanDetectKind.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.humandetect;

/**
 * Human Detect Kind.
 * 
 * @author NTT DOCOMO, INC.
 */
public enum HumanDetectKind {
    
    /**
     * Body Detection.
     */
    BODY("BODY"),
    /**
     * Hand Detection.
     */
    HAND("HAND"),
    /**
     * Face Detection.
     */
    FACE("FACE"),
    /**
     * Human Detection.
     */
    HUMAN("HUMAN");
    
    
    /**
     * Id.
     */
    private String mId;
    
    /**
     * Constructor.
     * @param id id
     */
    private HumanDetectKind(final String id) {
        mId = id;
    }
    
    @Override
    public String toString() {
        return mId;
    }
    
}
