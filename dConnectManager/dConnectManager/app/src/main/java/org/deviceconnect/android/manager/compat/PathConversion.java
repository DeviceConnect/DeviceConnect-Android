package org.deviceconnect.android.manager.compat;


import android.content.Intent;

import org.deviceconnect.message.DConnectMessage;

class PathConversion {

    final Path mFrom;
    final Path mTo;

    PathConversion(final Path from, final Path to) {
        if (from == null) {
            throw new IllegalArgumentException("from is null.");
        }
        if (to == null) {
            throw new IllegalArgumentException("to is null.");
        }
        mFrom = from;
        mTo = to;
    }

    boolean canConvert(final Intent request) {
        String profileName = request.getStringExtra(DConnectMessage.EXTRA_PROFILE);
        String interfaceName = request.getStringExtra(DConnectMessage.EXTRA_INTERFACE);
        String attributeName = request.getStringExtra(DConnectMessage.EXTRA_ATTRIBUTE);
        return mFrom.equals(new Path(profileName, interfaceName, attributeName));
    }

    void convert(final Intent request) {
        request.putExtra(DConnectMessage.EXTRA_PROFILE, mTo.mProfileName);
        request.putExtra(DConnectMessage.EXTRA_INTERFACE, mTo.mInterfaceName);
        request.putExtra(DConnectMessage.EXTRA_ATTRIBUTE, mTo.mAttributeName);
    }

}
