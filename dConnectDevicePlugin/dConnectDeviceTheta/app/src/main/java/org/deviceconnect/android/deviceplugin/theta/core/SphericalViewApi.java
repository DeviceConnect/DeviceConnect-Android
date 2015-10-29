package org.deviceconnect.android.deviceplugin.theta.core;


public enum SphericalViewApi {

    INSTANCE;

    public synchronized void startImageView(final String imageUri, final Param param,
                                            final SphericalViewRenderer renderer) {
        // TODO Implement.
    }

    public synchronized void updateImageView(final Param param) {
        // TODO Implement.
    }

    public synchronized void stopImageView() {
        // TODO Implement.
    }

    public boolean isStarted() {
        // TODO Implement.
        return false;
    }

    public static class Param {

        private int mViewWidth;

        private int mViewHeight;

        private double mFOV;

        private boolean mIsVRMode;
    }

}
