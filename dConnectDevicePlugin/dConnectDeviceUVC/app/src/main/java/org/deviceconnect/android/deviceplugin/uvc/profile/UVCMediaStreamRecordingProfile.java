/*
 UVCMediaStreamRecordingProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.uvc.profile;


import android.content.Intent;

import org.deviceconnect.android.deviceplugin.uvc.UVCDevice;
import org.deviceconnect.android.deviceplugin.uvc.UVCDeviceManager;
import org.deviceconnect.android.deviceplugin.uvc.utils.MixedReplaceMediaServer;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.MediaStreamRecordingProfile;
import org.deviceconnect.message.DConnectMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * UVC MediaStream Recording Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class UVCMediaStreamRecordingProfile extends MediaStreamRecordingProfile {

    private final Logger mLogger = Logger.getLogger("uvc.dplugin");

    private final UVCDeviceManager mDeviceMgr;

    private final Map<String, MixedReplaceMediaServer> mServers
        = new HashMap<String, MixedReplaceMediaServer>();

    private final UVCDeviceManager.PreviewListener mPreviewListener
        = new UVCDeviceManager.PreviewListener() {
        @Override
        public void onFrame(final UVCDevice device, final byte[] frame) {
            mLogger.info("onFrame: " + frame.length);
            MixedReplaceMediaServer server = mServers.get(device.getId());
            if (server != null) {
                server.offerMedia(frame);
            }
        }
    };

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    public UVCMediaStreamRecordingProfile(final UVCDeviceManager deviceMgr) {
        mDeviceMgr = deviceMgr;
        mDeviceMgr.addPreviewListener(mPreviewListener);
    }

    @Override
    protected boolean onPutPreview(final Intent request, final Intent response,
                                   final String serviceId) {
        final UVCDevice device = mDeviceMgr.getDevice(serviceId);
        if (device == null) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                String uri = startMediaServer(device.getId());
                device.startPreview();

                setResult(response, DConnectMessage.RESULT_OK);
                setUri(response, uri);
                sendResponse(response);
            }
        });
        return false;
    }

    @Override
    protected boolean onDeletePreview(final Intent request, final Intent response,
                                      final String serviceId) {
        final UVCDevice device = mDeviceMgr.getDevice(serviceId);
        if (device == null) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                device.stopPreview();
                stopMediaServer(device.getId());

                setResult(response, DConnectMessage.RESULT_OK);
                sendResponse(response);
            }
        });
        return false;
    }

    private synchronized String startMediaServer(final String id) {
        MixedReplaceMediaServer server = mServers.get(id);
        if (server == null) {
            server = new MixedReplaceMediaServer();
            server.setServerName("UVC Video Server");
            server.setContentType("image/jpg");
            server.start();
            mServers.put(id, server);
        }
        return server.getUrl();
    }

    public synchronized void stopMediaServer(final String id) {
        MixedReplaceMediaServer server = mServers.remove(id);
        if (server != null) {
            server.stop();
        }
    }
}
