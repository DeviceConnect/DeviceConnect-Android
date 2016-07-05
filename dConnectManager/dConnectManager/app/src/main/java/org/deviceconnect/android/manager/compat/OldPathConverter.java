package org.deviceconnect.android.manager.compat;


import android.content.Intent;

import org.deviceconnect.android.compat.MessageConverter;

/**
 * リクエストパスを旧仕様に統一するクラス.
 * @author NTT DOCOMO, INC.
 */
public class OldPathConverter implements MessageConverter {

    @Override
    public boolean convert(final Intent request) {
        for (PathConversion conversion : PathConversionTable.NEW_TO_OLD) {
            if (conversion.canConvert(request)) {
                conversion.convert(request);
                return true;
            }
        }
        return false;
    }

}
