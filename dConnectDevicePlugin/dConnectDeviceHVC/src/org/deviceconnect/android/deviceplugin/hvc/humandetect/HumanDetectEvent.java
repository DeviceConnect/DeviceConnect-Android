package org.deviceconnect.android.deviceplugin.hvc.humandetect;

import java.util.List;

public class HumanDetectEvent {
    private HumanDetectKind mKind;
    private String mSessionKey;
    private List<String> mOptions;
    
    public HumanDetectEvent(final HumanDetectKind kind, final String sessionKey, final List<String> options) {
        mKind = kind;
        mSessionKey = sessionKey;
        mOptions = options;
    }

    public HumanDetectKind getKind() {
        return mKind;
    }
    
    public String getSessionKey() {
        return mSessionKey;
    }
    
    public List<String> getOptions() {
        return mOptions;
    }
}
