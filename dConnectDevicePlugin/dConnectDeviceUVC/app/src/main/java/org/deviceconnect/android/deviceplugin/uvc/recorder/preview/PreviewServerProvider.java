package org.deviceconnect.android.deviceplugin.uvc.recorder.preview;


import java.util.List;

public interface PreviewServerProvider {

    List<PreviewServer> getServers();

    void stopAll();
}
