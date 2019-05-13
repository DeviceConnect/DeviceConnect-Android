/*
 DConnectSpec.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec.models;

import android.os.Bundle;

/**
 * プロファイル定義書.
 *
 * @author NTT DOCOMO, INC.
 */
public interface DConnectSpec {
    /**
     * プロファイル定義書を Bundle に変換します.
     *
     * @return プロファイル定義書
     */
    Bundle toBundle();
}
