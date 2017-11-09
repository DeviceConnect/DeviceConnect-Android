/*
 HostDeviceRecorder.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Host Device Recorder.
 *
 * @author NTT DOCOMO, INC.
 */
public interface HostDeviceRecorder {

    void initialize();

    void clean();

    String getId();

    String getName();

    String getMimeType();

    RecorderState getState();

    PictureSize getPictureSize();

    void setPictureSize(PictureSize size);

    PictureSize getPreviewSize();

    void setPreviewSize(PictureSize size);

    double getMaxFrameRate();

    void setMaxFrameRate(double frameRate);

    List<PictureSize> getSupportedPictureSizes();

    List<PictureSize> getSupportedPreviewSizes();

    List<String> getSupportedMimeTypes();

    boolean isSupportedPictureSize(int width, int height);

    boolean isSupportedPreviewSize(int width, int height);

    enum RecorderState {
        INACTTIVE,
        PAUSED,
        RECORDING,
        ERROR
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
            @Override
            public PictureSize createFromParcel(Parcel in) {
                return new PictureSize(in);
            }
            @Override
            public PictureSize[] newArray(int size) {
                return new PictureSize[size];
            }
        };

        @Override
        public String toString() {
            return "(width = " + getWidth() + ", height = " + getHeight() + ")";
        }
    }
}
