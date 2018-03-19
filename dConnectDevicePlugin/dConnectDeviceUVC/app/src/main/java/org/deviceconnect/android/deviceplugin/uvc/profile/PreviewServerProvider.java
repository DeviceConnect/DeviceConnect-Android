package org.deviceconnect.android.deviceplugin.uvc.profile;


import java.util.List;

public interface PreviewServerProvider {

    List<PreviewServer> getServers();

    void stopAll();
}
