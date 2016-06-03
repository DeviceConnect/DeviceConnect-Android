package org.deviceconnect.android.profile;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

final class Util {

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHMMSS.sssZ", Locale.getDefault());

    private Util() {
    }

    public static String timeStampToText(long timeStamp) {
        return SIMPLE_DATE_FORMAT.format(new Date(timeStamp));
    }
}
