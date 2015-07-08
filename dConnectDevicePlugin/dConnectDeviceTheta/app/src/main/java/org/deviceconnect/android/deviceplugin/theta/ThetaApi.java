package org.deviceconnect.android.deviceplugin.theta;

import com.theta360.lib.ThetaException;

import java.io.IOException;

/**
 * Theta API.
 *
 * @author NTT DOCOMO, INC.
 */
public interface ThetaApi {

    double getBatteryLevel() throws ThetaException, IOException;

    void takePhoto(ThetaPhotoEventListener listener) throws ThetaException, IOException;

}
