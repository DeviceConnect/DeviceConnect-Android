package org.deviceconnect.android.deviceplugin.host.recorder.camera;

public class CameraException extends Exception {

    public CameraException(String message) {
        super(message);
    }

    public CameraException(String message, Throwable cause) {
        super(message, cause);
    }

    public CameraException(Throwable cause) {
        super(cause);
    }
}
