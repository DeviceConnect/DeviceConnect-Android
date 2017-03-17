/*
 HumanDetectKind
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvcp.manager.data;

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
    HUMAN("HUMAN"),
    /**
     * Face Recognition.
     */
    RECOGNIZE("RECOGNIZE");



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
