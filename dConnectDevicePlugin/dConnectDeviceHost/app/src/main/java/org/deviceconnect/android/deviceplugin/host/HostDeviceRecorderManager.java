/*
 HostDeviceRecorderManager.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host;


import org.deviceconnect.android.deviceplugin.host.camera.HostDevicePhotoRecorder;

/**
 * Host Device Recorder Manager.
 *
 * @author NTT DOCOMO, INC.
 */
public interface HostDeviceRecorderManager {

    HostDeviceRecorder[] getRecorders();

    HostDeviceRecorder getRecorder(String id);

    HostDevicePhotoRecorder getPhotoRecorder(String id);

    HostDeviceStreamRecorder getStreamRecorder(String id);

    HostDevicePreviewServer getPreviewServer(String id);

}
