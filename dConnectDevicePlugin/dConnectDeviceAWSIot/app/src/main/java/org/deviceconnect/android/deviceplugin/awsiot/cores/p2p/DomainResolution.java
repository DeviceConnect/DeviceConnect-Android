/*
 DomainResolution.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot.cores.p2p;

import java.net.InetAddress;
import java.net.UnknownHostException;

public final class DomainResolution {

    private DomainResolution() {
    }

    public static String lookup(final String host) {
        InetAddress thisComputer;
        byte[] address;
        try {
            thisComputer = InetAddress.getByName(host);
            address = thisComputer.getAddress();
        } catch (UnknownHostException e) {
            return null;
        }

        if (isHostName(host)) {
            String dottedQuad = "";
            for (int i = 0; i < address.length; i++) {
                int unsignedByte = address[i] < 0 ? address[i] + 256 : address[i];
                dottedQuad += unsignedByte;
                if (i != address.length - 1) {
                    dottedQuad += ".";
                }
            }
            return dottedQuad;
        } else {
            return thisComputer.getHostName();
        }
    }

    private static boolean isHostName(final String host) {
        char[] ca = host.toCharArray();
        for (int i = 0; i < ca.length; i++) {
            if (!Character.isDigit(ca[i])) {
                if (ca[i] != '.') {
                    return true;
                }
            }
        }
        return false;
    }
}
