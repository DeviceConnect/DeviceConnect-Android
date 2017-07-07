package org.deviceconnect.android.manager.plugin;


import android.content.Intent;

interface Connection {

    ConnectionType getType();

    ConnectionState getState();

    void connect() throws ConnectingException;

    void disconnect();

    void setConnectionListener(ConnectionListener listener);

    void send(Intent message) throws MessagingException;

    void setMessageListener(MessageListener listener);

}
