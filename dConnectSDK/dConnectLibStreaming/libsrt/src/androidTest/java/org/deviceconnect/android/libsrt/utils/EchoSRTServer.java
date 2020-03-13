package org.deviceconnect.android.libsrt.utils;

import org.deviceconnect.android.libsrt.SRTSocket;
import org.deviceconnect.android.libsrt.SRTSocketException;

public class EchoSRTServer extends TestSRTServer {
    public EchoSRTServer(int port) {
        super(port);
    }

    @Override
    public void execute(SRTSocket socket) throws SRTSocketException {
        byte[] buf = new byte[1500];
        int len = socket.recv(buf);
        if (len > 0) {
            socket.send(buf, 0, len);
        }
    }
}
