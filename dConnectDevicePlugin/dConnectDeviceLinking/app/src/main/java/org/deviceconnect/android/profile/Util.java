/*
 Util.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

final class Util {

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHMMSS.sssZ", Locale.getDefault());

    private Util() {
    }

    public static String timeStampToText(final long timeStamp) {
        return SIMPLE_DATE_FORMAT.format(new Date(timeStamp));
    }
}
