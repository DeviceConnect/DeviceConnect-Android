package org.deviceconnect.android.deviceplugin.theta;

public class ThetaFileInfo {

    public final String mName;
    public final int mSize;
    public final String mMimeType;
    final int mHandle;

    ThetaFileInfo(final String name, final String mimeType, final int size, final int handle) {
        mName = name;
        mSize = size;
        mMimeType = mimeType;
        mHandle = handle;
    }
}
