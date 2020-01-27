package org.deviceconnect.android.libsrt;

public final class SRT {
    private SRT() {
    }

    public static void startup() {
        NdkHelper.startup();
    }

    public static void cleanup() {
        NdkHelper.cleanup();
    }
}
