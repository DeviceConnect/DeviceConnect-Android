package org.deviceconnect.android.libsrt;

public final class SRT {
    private SRT() {
    }

    // TODO srt.h で enum で宣言されているために、値がずれる可能性があります。

    public static final int SRTO_SNDSYN = 1;          // if sending is blocking
    public static final int SRTO_RCVSYN = 2;          // if receiving is blocking
    public static final int SRTO_MAXBW = 16;
    public static final int SRTO_SENDER = 21;
    public static final int SRTO_LATENCY = 23;
    public static final int SRTO_INPUTBW = 24;
    public static final int SRTO_OHEADBW = 25;
    public static final int SRTO_CONNTIMEO = 36;
    public static final int SRTO_LOSSMAXTTL = 42;
    public static final int SRTO_RCVLATENCY = 43;
    public static final int SRTO_PEERLATENCY = 44;
    public static final int SRTO_PEERIDLETIMEO = 55;
    public static final int SRTO_PACKETFILTER = 60;

    public static void startup() {
        NdkHelper.startup();
    }

    public static void cleanup() {
        NdkHelper.cleanup();
    }
}
