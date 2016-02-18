/*
 HostDevicePreviewServer.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host;


import android.support.annotation.NonNull;

import java.util.List;

/**
 * Host Device Preview Server.
 *
 * @author NTT DOCOMO, INC.
 */
public interface HostDevicePreviewServer extends HostDeviceRecorder {

    void startWebServer(OnWebServerStartCallback callback);

    void stopWebServer();

    List<PictureSize> getSupportedPreviewSizes();

    boolean supportsPreviewSize(int width, int height);

    PictureSize getPreviewSize();

    void setPreviewSize(PictureSize size);

    /**
     * Callback interface used to receive the result of starting a web server.
     */
    interface OnWebServerStartCallback {
        /**
         * Called when a web server successfully started.
         *
         * @param uri An ever-updating, static image URI.
         */
        void onStart(@NonNull String uri);

        /**
         * Called when a web server failed to start.
         */
        void onFail();
    }
}
