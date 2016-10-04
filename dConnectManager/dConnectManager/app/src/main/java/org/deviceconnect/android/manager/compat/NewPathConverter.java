package org.deviceconnect.android.manager.compat;


import android.content.Intent;

import org.deviceconnect.android.compat.MessageConverter;


/**
 * リクエストパスを新仕様に統一するクラス.
 * @author NTT DOCOMO, INC.
 */
public class NewPathConverter implements MessageConverter {

    @Override
    public boolean convert(final Intent request) {
        for (PathConversion conversion : PathConversionTable.OLD_TO_NEW) {
            if (conversion.canConvert(request)) {
                conversion.convert(request);
                return true;
            }
        }
        return false;
    }

}
