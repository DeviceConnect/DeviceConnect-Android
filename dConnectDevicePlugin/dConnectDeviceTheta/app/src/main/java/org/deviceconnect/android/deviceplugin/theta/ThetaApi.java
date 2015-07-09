package org.deviceconnect.android.deviceplugin.theta;

import com.theta360.lib.ThetaException;

import java.io.IOException;
import java.util.List;

/**
 * Theta API.
 *
 * @author NTT DOCOMO, INC.
 */
public interface ThetaApi {

    double getBatteryLevel() throws ThetaException, IOException;

    void takePhoto(ThetaPhotoEventListener listener) throws ThetaException, IOException;

    void startVideoRecording() throws ThetaException, IOException;

    void stopVideoRecording() throws ThetaException, IOException;

    List<ThetaFileInfo> getFileInfoListFromDefaultStorage() throws ThetaException, IOException;

    byte[] getFile(ThetaFileInfo info) throws ThetaException, IOException;
}
