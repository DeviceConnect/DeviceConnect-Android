package org.deviceconnect.android.deviceplugin.theta;

public class ThetaPhoto {
    public final byte[] mData;
    public final String mFilename;
    public final String mMimeType;
    public final String mServiceId;

    ThetaPhoto(final byte[] data, final String filename,
          final String mimeType, final String serviceId) {
        mData = data;
        mFilename = filename;
        mMimeType = mimeType;
        mServiceId = serviceId;
    }
}
