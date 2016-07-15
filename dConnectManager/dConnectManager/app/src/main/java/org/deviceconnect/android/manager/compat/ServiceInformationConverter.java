package org.deviceconnect.android.manager.compat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

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

        Bundle supportApisParam = response.getBundleExtra(PARAM_SUPPORT_APIS);
        if (supportApisParam != null) {
            for (String key : supportApisParam.keySet()) {
                Parcelable[] apiSpecs = supportApisParam.getParcelableArray(key);

                // APIのパスを新仕様に統一
                for (Parcelable apiSpec : apiSpecs) {
                    if (apiSpec instanceof Bundle) {
                        String pathParam = getPathFromBundle((Bundle) apiSpec);
                        if (pathParam != null) {
                            try {
                                Path path = Path.parsePath(pathParam);
                                Path newPath = PathConversionTable.forwardPath(path);
                                String newPathParam = newPath.mExpression;
                                putPathToBundle((Bundle) apiSpec, newPathParam);
                            } catch (IllegalArgumentException e) {
                                continue;
                            }
                        }
                    }
                }

                // プロファイル名を新仕様に統一
                String convertedKey = PathConversionTable.forwardProfileName(key);
                supportApisParam.putParcelableArray(convertedKey, apiSpecs);
            }
        }
    }

    private String getPathFromBundle(final Bundle support) {
        return support.getString(PARAM_PATH);
    }

    private void putPathToBundle(final Bundle support, String path) {
        support.putString(PARAM_PATH, path);
    }
}
