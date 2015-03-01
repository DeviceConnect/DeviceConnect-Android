package org.deviceconnect.android.deviceplugin.hvc.humandetect;

/**
 * Human Detect Kind.
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
    FACE("FACE");
    
    
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
