/*
 SmartService.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.uiapp.data;

import java.util.ArrayList;
import java.util.List;

/**
 * スマートサービス(プロファイル).
 */
public class DCProfile {

    /**
     * プロファイル名.
     */
    private String mName;

    /**
     * APIリスト.
     */
    private List<DCApi> mApiList = new ArrayList<>();

    /**
     * コンストラクタ.
     * @param name プロファイル名
     */
    public DCProfile(final String name) {
        mName = name;
    }

    /**
     * プロファイル名を設定する.
     * @param name プロファイル名
     */
    public void setName(final String name) {
        mName = name;
    }

    /**
     * プロファイル名を取得する.
     * @return プロファイル名
     */
    public String getName() {
        return mName;
    }

    /**
     * アイコンを取得する.
     * @return アイコンID
     */
    public int getIconId() {
        return android.R.drawable.ic_menu_info_details;
    }

    /**
     * APIリストにapiを追加する.
     * @param api API
     */
    public void addApi(final DCApi api) {
        mApiList.add(api);
    }

    /**
     * APIリストを取得する.
     * @return APIリスト
     */
    public List<DCApi> getApiList() {
        return mApiList;
    }

    @Override
    public String toString() {
        return mName;
    }

}
