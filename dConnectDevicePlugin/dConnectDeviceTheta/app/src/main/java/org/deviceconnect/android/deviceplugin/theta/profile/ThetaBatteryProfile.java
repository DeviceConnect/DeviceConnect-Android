package org.deviceconnect.android.deviceplugin.theta.profile;

import android.content.Intent;

import com.theta360.lib.ThetaException;

import org.deviceconnect.android.deviceplugin.theta.ThetaApi;
import org.deviceconnect.android.deviceplugin.theta.ThetaApiTask;
import org.deviceconnect.android.deviceplugin.theta.ThetaDeviceService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.BatteryProfile;
import org.deviceconnect.message.DConnectMessage;

import java.io.IOException;

/**
 * Theta Battery Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaBatteryProfile extends BatteryProfile {

    @Override
    protected boolean onGetAll(final Intent request, final Intent response, final String serviceId) {
        getService().execute(new ThetaApiTask() {
            @Override
            public void run(final ThetaApi api)  {
                try {
                    setLevel(response, api.getBatteryLevel());
                    setCharging(response, false);
                    setResult(response, DConnectMessage.RESULT_OK);
                }  catch (ThetaException e) {
                    MessageUtils.setUnknownError(response, e.getMessage());
                } catch (IOException e) {
                    MessageUtils.setUnknownError(response, e.getMessage());
                }
                getService().sendResponse(response);
            }
        });
        return false;
    }

    @Override
    protected boolean onGetLevel(final Intent request, final Intent response, final String serviceId) {
        getService().execute(new ThetaApiTask() {
            @Override
            public void run(final ThetaApi api) {
                try {
                    setLevel(response, api.getBatteryLevel());
                    setResult(response, DConnectMessage.RESULT_OK);
                }  catch (ThetaException e) {
                    MessageUtils.setUnknownError(response, e.getMessage());
                } catch (IOException e) {
                    MessageUtils.setUnknownError(response, e.getMessage());
                }
                getService().sendResponse(response);
            }
        });
        return false;
    }

    private ThetaDeviceService getService() {
        return ((ThetaDeviceService) getContext());
    }

}
