/*
 DevicePlugin.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.uiapp.data;

/**
 * デバイスプラグイン.
 */
public class DCDevicePlugin {

    /**
     * デバイスプラグイン名.
     */
    private String mName;

    /**
     * デバイスプラグインID.
     */
    private String mId;

    /**
     * パッケージ名.
     */
    private String mPackageName;


    /**
     * コンストラクタ.
     * @param name デバイスプラグイン名
     * @param id デバイスプラグインID
     */
    public DCDevicePlugin(final String name, final String id) {
        mName = name;
        mId = id;
    }
    
    /**
     * デバイスプラグイン名を取得する.
     * @return デバイスプラグイン名
     */
    public String getName() {
        return mName;
    }

    /**
     * デバイスプラグインIDを取得する.
     * @return デバイスプラグインID
     */
    public String getId() {
        return mId;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public void setPackageName(String packageName) {
        mPackageName = packageName;
    }
}
