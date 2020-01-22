package org.deviceconnect.android.libmedia.streaming.sdp;

public class Information {

    private String mText;

    public Information() {
    }

    public Information(String text) {
        mText = text;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    @Override
    public String toString() {
        return "i=" + mText;
    }
}
