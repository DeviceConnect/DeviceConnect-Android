package org.deviceconnect.android.manager.compat;

import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.compat.MessageConverter;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.deviceconnect.profile.ServiceInformationProfileConstants;

public class ServiceInformationConverter implements MessageConverter {

    @Override
    public boolean convert(final Intent message) {
        if (!isResponse(message)) {
            return false;
        }
        if (!isServiceInformationResponse(message)) {
            return false;
        }
        return convertSupportsParam(message);
    }

    private boolean isResponse(final Intent message) {
        String method = message.getStringExtra(IntentDConnectMessage.EXTRA_METHOD);
        return IntentDConnectMessage.ACTION_RESPONSE.equals(method);
    }

    private boolean isServiceInformationResponse(final Intent message) {
        String profile = message.getStringExtra(DConnectMessage.EXTRA_PROFILE);
        return ServiceInformationProfileConstants.PROFILE_NAME.equalsIgnoreCase(profile);
    }

    private boolean convertSupportsParam(final Intent response) {
        final String key = ServiceInformationProfileConstants.PARAM_SUPPORTS;
        if (!response.hasExtra(key)) {
            return false;
        }
        Object array = response.getParcelableArrayExtra(key);
        if (array instanceof Bundle[]) {
            Bundle[] supports = (Bundle[]) array;
            for (int i = 0; i < supports.length; i++) {
                Bundle support = supports[i];
                String path = getPathFromBundle(support);
                Path forward = PathConversionTable.forwardPath(path);
                putPathToBundle(support, forward.toString());
            }
            response.putExtra(key, supports);
            return true;
        } else if (array instanceof String[]) {
            String[] supports = (String[]) array;
            for (int i = 0; i < supports.length; i++) {
                String profileName = supports[i];
                String forward = PathConversionTable.forwardProfileName(profileName);
                if (forward != null) {
                    supports[i] = forward;
                }
            }
            response.putExtra(key, supports);
            return true;
        } else {
            return false;
        }
    }

    private String getPathFromBundle(final Bundle support) {
        return support.getString("path"); // TODO "path"を定数化
    }

    private void putPathToBundle(final Bundle support, String path) {
        support.putString("path", path); // TODO "path"を定数化
    }
}
