/*
 TestLightProfileConstants.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.test.plugin.profile;

import android.graphics.Color;

/**
 * JUnit用テストデバイスプラグイン、Lightプロファイル定数群.
 * 
 * @author NTT DOCOMO, INC.
 */
public interface TestLightProfileConstants {

    /**
     * テストで使用するライトID.
     */
    String LIGHT_ID = "test_light_id";

    /**
     * テストで使用するライト名.
     */
    String LIGHT_NAME = "test_light_name";

    /**
     * テストで使用するon属性値.
     */
    boolean LIGHT_ON = true;

    /**
     * テストで使用するconfig値.
     */
    String LIGHT_CONFIG = "";

    /**
     * テストで使用する色情報.
     */
    Integer LIGHT_COLOR = Color.argb(0, 255, 0, 0);

    /**
     * テストで使用する明るさ情報.
     */
    Double LIGHT_BRIGHTNESS = 0.5;

    /**
     * テストで使用する点滅情報.
     */
    long[] LIGHT_FLASHING = {1000, 1001, 1002};

    /**
     * テストで使用する登録用の名前.
     */
    String LIGHT_NEW_NAME = "test_new_light_name";

    /**
     * テストで使用するグループID.
     */
    String LIGHT_GROUP_ID = "test_group_id";

    /**
     * テストで使用するグループ名.
     */
    String LIGHT_GROUP_NAME = "test_group_name";

    /**
     * テストで使用するライトIDリスト.
     */
    String[] LIGHT_IDS = {
            "test_light_id1", "test_light_id2", "test_light_id3"
    };

    /**
     * テストで使用するグループ名.
     */
    String LIGHT_NEW_GROUP_ID = "test_new_group_id";

    /**
     * テストで使用するグループ名.
     */
    String LIGHT_NEW_GROUP_NAME = "test_new_group_name";
}
