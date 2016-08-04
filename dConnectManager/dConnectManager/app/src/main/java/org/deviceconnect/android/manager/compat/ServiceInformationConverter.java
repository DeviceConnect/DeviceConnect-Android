package org.deviceconnect.android.manager.compat;

import android.content.Intent;

import org.deviceconnect.android.compat.MessageConverter;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.deviceconnect.profile.ServiceInformationProfileConstants;

public class ServiceInformationConverter implements MessageConverter,
    ServiceInformationProfileConstants {

    @Override
    public void convert(final Intent message) {
        if (!isResponse(message)) {
            return;
        }
        convertSupportsParam(message);
    }

    private boolean isResponse(final Intent message) {
        String action = message.getAction();
        return IntentDConnectMessage.ACTION_RESPONSE.equals(action);
    }

    private void convertSupportsParam(final Intent response) {
        String[] supports = response.getStringArrayExtra(PARAM_SUPPORTS);
        if (supports != null) {
            for (int i = 0; i < supports.length; i++) {
                String profileName = supports[i];
                String forward = PathConversionTable.forwardProfileName(profileName);
                if (forward != null) {
                    supports[i] = forward;
                }
            }
            response.putExtra(PARAM_SUPPORTS, supports);
        }
    }
}
