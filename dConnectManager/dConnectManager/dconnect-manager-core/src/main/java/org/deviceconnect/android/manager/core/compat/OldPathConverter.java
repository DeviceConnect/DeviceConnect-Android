/*
 OldPathConverter.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.core.compat;


import android.content.Intent;

import org.deviceconnect.android.compat.MessageConverter;

/**
 * リクエストパスを旧仕様に統一するクラス.
 *
 * @author NTT DOCOMO, INC.
 */
class OldPathConverter implements MessageConverter {

    @Override
    public void convert(final Intent request) {
        for (PathConversion conversion : PathConversionTable.NEW_TO_OLD) {
            if (conversion.canConvert(request)) {
                conversion.convert(request);
            }
        }
    }

}
