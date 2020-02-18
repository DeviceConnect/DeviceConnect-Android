package org.deviceconnect.android.libsrt;

public final class SRT {
    private SRT() {
    }

    public static final int SRTO_SNDSYN = 1;          // if sending is blocking
    public static final int SRTO_RCVSYN = 2;          // if receiving is blocking
    public static final int SRTO_SENDER = 21;
    public static final int SRTO_MAXBW = 16;
    public static final int SRTO_INPUTBW = 24;
    public static final int SRTO_OHEADBW = 25;

    public static void startup() {
        NdkHelper.startup();
    }

    public static void cleanup() {
        NdkHelper.cleanup();
    }
}
