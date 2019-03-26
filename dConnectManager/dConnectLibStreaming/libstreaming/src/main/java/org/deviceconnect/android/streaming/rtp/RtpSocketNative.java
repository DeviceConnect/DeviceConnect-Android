package org.deviceconnect.android.streaming.rtp;

class RtpSocketNative {
    static {
        System.loadLibrary("rtp-native-lib");
    }

    static native long open(String destAddress, int destPort, int rtcpPort);
    static native void setClockFrequency(long nativePtr, long clock);
    static native void setTTL(long nativePtr, int ttl);
    static native void setSSRC(long nativePtr, int ssrc);
    static native void setPayloadType(long nativePtr, int payloadType);
    static native void send(long nativePtr, int packetType, byte[] data, int length, long rtpts, long ntpts);
    static native void close(long nativePtr);
}
