package org.deviceconnect.android.manager.compat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import org.deviceconnect.android.compat.MessageConverter;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;

import java.util.ArrayList;
import java.util.List;

public class ServiceDiscoveryConverter implements MessageConverter,
    ServiceDiscoveryProfileConstants {

    @Override
    public boolean convert(final Intent message) {
        if (!isResponse(message)) {
            return false;
        }
        convertSupportsParam(message);
        return true;
    }

    private boolean isResponse(final Intent message) {
        String action = message.getAction();
        return IntentDConnectMessage.ACTION_RESPONSE.equals(action);
    }

    private void convertSupportsParam(final Intent response) {
        Bundle[] serviceBundles = getBundleExtra(response, PARAM_SERVICES);
        if (serviceBundles == null) {
            return;
        }
        for (Bundle serviceBundle : serviceBundles) {
            String[] supportsParam = serviceBundle.getStringArray(PARAM_SCOPES);
            if (supportsParam != null) {
                for (int i = 0; i < supportsParam.length; i++) {
                    String profileName = supportsParam[i];
                    String forward = PathConversionTable.forwardProfileName(profileName);
                    if (forward != null) {
                        supportsParam[i] = forward;
                    }
                }
                response.putExtra(PARAM_SCOPES, supportsParam);
            }
        }
    }

    private Bundle[] getBundleExtra(final Intent intent, final String key) {
        return getBundleExtra(intent.getParcelableArrayExtra(key));
    }

    private Bundle[] getBundleExtra(final Parcelable[] parcelables) {
        if (parcelables == null) {
            return null;
        }
        List<Bundle> bundleList = new ArrayList<Bundle>();
        for (Parcelable parcelable : parcelables) {
            if (parcelable instanceof Bundle) {
                bundleList.add((Bundle) parcelable);
            }
        }
        return bundleList.toArray(new Bundle[bundleList.size()]);
    }
}
