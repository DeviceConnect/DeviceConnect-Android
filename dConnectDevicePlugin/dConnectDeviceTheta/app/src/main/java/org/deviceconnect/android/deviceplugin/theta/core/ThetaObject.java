package org.deviceconnect.android.deviceplugin.theta.core;


public interface ThetaObject {

    void fetch() throws ThetaDeviceException;

    void remove() throws ThetaDeviceException;

    String getMimeType();

    boolean isImage();

    boolean isVideo();

    boolean isFetched();

    boolean isRemoved();

    long getCreationTime();

    String getFileName();

    byte[] getThumbnail();

    byte[] getData();

}
