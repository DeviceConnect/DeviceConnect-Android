/*
 HostDeviceRecorderManager.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host;


/**
 * Host Device Recorder Manager.
 *
 * @author NTT DOCOMO, INC.
 */
public interface HostDeviceRecorderManager {

    HostDeviceRecorder[] getRecorders();

    HostDevicePhotoRecorder getPhotoRecorder();

    HostDevicePhotoRecorder getPhotoRecorder(final String id);

    HostDeviceStreamRecorder getStreamRecorder(final String id);

}
