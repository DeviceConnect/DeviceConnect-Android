package org.deviceconnect.android.deviceplugin.hvc.humandetect;

public class HumanDetectEvent {
    private HumanDetectKind mKind;
    private String mSessionKey;
    
    public HumanDetectEvent(final HumanDetectKind kind, final String sessionKey) {
        mKind = kind;
        mSessionKey = sessionKey;
    }

    public HumanDetectKind getKind() {
        return mKind;
    }
    
    public String getSessionKey() {
        return mSessionKey;
    }
}
