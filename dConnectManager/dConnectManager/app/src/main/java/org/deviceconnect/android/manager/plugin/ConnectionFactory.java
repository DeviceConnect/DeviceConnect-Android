package org.deviceconnect.android.manager.plugin;


public interface ConnectionFactory {

    Connection createConnectionForPlugin(final DevicePlugin plugin);

}
