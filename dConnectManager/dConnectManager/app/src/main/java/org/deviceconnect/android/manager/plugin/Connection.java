package org.deviceconnect.android.manager.plugin;


import android.content.Intent;

public interface Connection {

    String getPluginId();

    ConnectionType getType();

    ConnectionState getState();

    void connect() throws ConnectingException;

    void disconnect();

    void addConnectionStateListener(ConnectionStateListener listener);

    void removeConnectionStateListener(ConnectionStateListener listener);

    void send(Intent message) throws MessagingException;

}
