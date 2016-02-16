/*
 HostDeviceRecorder.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Host Device Recorder.
 *
 * @author NTT DOCOMO, INC.
 */
public interface HostDeviceRecorder {

    String getId();

    String getName();

    String getMimeType();

    String[] getSupportedMimeTypes();

    RecorderState getState();

    boolean usesCamera();

    int getCameraId();

    boolean mutableInputPictureSize();

    PictureSize getInputPictureSize();

    void setInputPictureSize(PictureSize size);

    enum RecorderState {
        INACTTIVE,
        PAUSED,
        RECORDING
    }

    enum CameraFacing {

        BACK("back"),
        FRONT("front"),
        UNKNOWN("unknown");

        private final String mName;

        CameraFacing(final String name) {
            mName = name;
        }

        public String getName() {
            return mName;
        }
    }

    class PictureSize implements Parcelable {

        private final int mWidth;
        private final int mHeight;

        public PictureSize(final int w, final int h) {
            mWidth = w;
            mHeight = h;
        }

        private PictureSize(final Parcel in) {
            this(in.readInt(), in.readInt());
        }

        public int getWidth() {
            return mWidth;
        }

        public int getHeight() {
            return mHeight;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(final Parcel out, final int flags) {
            out.writeInt(mWidth);
            out.writeInt(mHeight);
        }

        public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
            public PictureSize createFromParcel(Parcel in) {
                return new PictureSize(in);
            }

            public PictureSize[] newArray(int size) {
                return new PictureSize[size];
            }
        };
    }

}
