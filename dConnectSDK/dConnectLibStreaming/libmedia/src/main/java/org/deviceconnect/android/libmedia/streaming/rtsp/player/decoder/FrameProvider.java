package org.deviceconnect.android.libmedia.streaming.rtsp.player.decoder;

import java.util.ArrayList;
import java.util.List;

public class FrameProvider {
    private final List<Frame> mFrameList = new ArrayList<>();

    public void init() {
        mFrameList.clear();
        for (int i = 0; i < 30; i++) {
            mFrameList.add(new Frame(4096));
        }
    }

    public Frame get() {
        for (Frame f : mFrameList) {
            if (!f.isConsumable()) {
                f.consume();
                return f;
            }
        }
        return null;
    }
}
