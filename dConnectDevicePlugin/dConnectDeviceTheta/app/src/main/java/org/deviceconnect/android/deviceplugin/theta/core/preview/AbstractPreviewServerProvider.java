/*
 HostDevicePreviewServer.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.core.preview;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;

import androidx.annotation.NonNull;
import org.deviceconnect.android.deviceplugin.theta.core.ThetaDevice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Host Device Preview Server.
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class AbstractPreviewServerProvider implements PreviewServerProvider {
    /**
     * プレビュー配信サーバーのリスト.
     */
    private final List<PreviewServer> mPreviewServers = new ArrayList<>();


    /**
     * コンストラクタ.
     */
    public AbstractPreviewServerProvider() {

    }

    // PreviewServerProvider

    @Override
    public List<String> getSupportedMimeType() {
        List<String> mimeType = new ArrayList<>();
        for (PreviewServer server : getServers()) {
            mimeType.add(server.getMimeType());
        }
        return mimeType;
    }

    @Override
    public void addServer(PreviewServer server) {
        mPreviewServers.add(server);
    }

    @Override
    public List<PreviewServer> getServers() {
        return mPreviewServers;
    }

    @Override
    public PreviewServer getServerForMimeType(String mimeType) {
        for (PreviewServer server : getServers()) {
            if (server.getMimeType().equalsIgnoreCase(mimeType)) {
                return server;
            }
        }
        return null;
    }

    @Override
    public List<PreviewServer> startServers() {
        List<PreviewServer> results = new ArrayList<>();

        CountDownLatch latch = new CountDownLatch(mPreviewServers.size());
        for (PreviewServer server : mPreviewServers) {
            server.startWebServer(new PreviewServer.OnWebServerStartCallback() {
                @Override
                public void onStart(@NonNull String uri) {
                    results.add(server);
                    latch.countDown();
                }

                @Override
                public void onFail() {
                    latch.countDown();
                }
            });
        }

        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                // TODO タイムアウト処理
            }
        } catch (InterruptedException e) {
            // ignore.
        }
        return results;
    }

    @Override
    public void stopServers() {

        for (PreviewServer server : getServers()) {
            server.stopWebServer();
        }
    }

    @Override
    public void onConfigChange() {
        for (PreviewServer server : getServers()) {
            server.onConfigChange();
        }
    }

}
