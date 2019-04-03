/*
 HostDeviceRecorder.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder;


import android.os.Parcel;
import android.os.Parcelable;
import android.util.Size;

import java.util.List;

/**
 * Host Device Recorder.
 *
 * @author NTT DOCOMO, INC.
 */
public interface HostDeviceRecorder {

    String MIME_TYPE_JPEG = "image/jpeg";

    void initialize();

    /**
     * プロセス起動時の状態に戻す.
     *
     * プラグイン再起動時に呼び出すこと.
     */
    void clean();

    /**
     * オブジェクトを破棄する.
     *
     * プロセス終了時に呼び出すこと.
     */
    void destroy();

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

    int getPreviewBitRate();

    void setPreviewBitRate(int bitRate);

    List<PictureSize> getSupportedPictureSizes();

    List<PictureSize> getSupportedPreviewSizes();

    List<String> getSupportedMimeTypes();

    boolean isSupportedPictureSize(int width, int height);

    boolean isSupportedPreviewSize(int width, int height);

    /**
     * 端末の画面が回転したタイミングで実行されるメソッド.
     * @param degree 角度を示す定数
     */
    void onDisplayRotation(int degree);

    enum RecorderState {
        INACTTIVE,
        PAUSED,
        RECORDING,
        ERROR
    }

    class PictureSize implements Parcelable {

        private final int mWidth;
        private final int mHeight;

        public PictureSize(final Size size) {
            this(size.getWidth(), size.getHeight());
        }

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
