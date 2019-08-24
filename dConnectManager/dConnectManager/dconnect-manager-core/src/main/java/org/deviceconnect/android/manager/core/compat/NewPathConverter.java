/*
 NewPathConverter.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.core.compat;


import android.content.Intent;

import org.deviceconnect.android.compat.MessageConverter;


/**
 * リクエストパスを新仕様に統一するクラス.
 *
 * @author NTT DOCOMO, INC.
 */
class NewPathConverter implements MessageConverter {

    @Override
    public void convert(final Intent request) {
        for (PathConversion conversion : PathConversionTable.OLD_TO_NEW) {
            if (conversion.canConvert(request)) {
                conversion.convert(request);
                return;
            }
        }
    }

}
