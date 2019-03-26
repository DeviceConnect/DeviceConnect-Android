/*
 ServiceDiscoveryConverter.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.core.compat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import org.deviceconnect.android.compat.MessageConverter;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.deviceconnect.profile.ServiceDiscoveryProfileConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Service Discovery APIレスポンスに含まれるプロファイル名を新仕様に統一するクラス.
 *
 * @author NTT DOCOMO, INC.
 */
public class ServiceDiscoveryConverter implements MessageConverter,
    ServiceDiscoveryProfileConstants {

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
                serviceBundle.putStringArray(PARAM_SCOPES, supportsParam);
            }
        }
        response.putExtra(PARAM_SERVICES, serviceBundles);
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
