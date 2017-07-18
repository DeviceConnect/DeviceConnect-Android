package org.deviceconnect.android.manager.plugin;


public interface ConnectionStateListener {

    void onConnectionStateChanged(String pluginId, ConnectionState state);

}
